package io.github.flameyheart.playroom.tiltify;

import blue.endless.jankson.Jankson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.tiltify.websocket.DonationUpdated;
import io.github.flameyheart.playroom.tiltify.websocket.WebhookEvent;
import io.github.flameyheart.playroom.tiltify.websocket.WebhookStructure;
import io.github.flameyheart.playroom.util.DynamicPlaceholders;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class TiltifyWebhookConnection extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger("Playroom Tiltify Webhook Connection");
    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();
    private boolean running = true;
    private SSLServerSocket serverSocket;

    public TiltifyWebhookConnection() {
        super("Tiltify Webhook Connection");
        setDaemon(true);

        setUncaughtExceptionHandler((t, e) -> {
            LOGGER.error("Uncaught exception in Tiltify Webhook thread " + t.getName(), e);
            LOGGER.error("Closing Tiltify Webhook thread due to unhandled exception");
            this.interrupt();
        });
    }

    @Override
    public void run() {
        LOGGER.info("Starting Tiltify Webhook thread");
        Path certificatePath = Playroom.getConfigPath().resolve("cert.pem"),
          privateKeyPath = Playroom.getConfigPath().resolve("key.pem");

        int port = ServerConfig.instance().tiltifyWebhookPort;
        try {
            // Load private key using Bouncy Castle
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);

            // Load certificate
            byte[] certificateBytes = readBytes(certificatePath);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{new X509KeyManagerImpl(privateKey, certFactory.generateCertificate(new ByteArrayInputStream(certificateBytes)))}, null, null);

            // Create an SSL server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create an SSL server socket
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            LOGGER.info("Tiltify Webhook server started");

            while (running) {
                try {
                    if (serverSocket.isClosed()) break;
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    handleClientConnection(clientSocket);
                } catch (Throwable e) {
                    if (e instanceof SSLHandshakeException) {
                        String message = e.getMessage();
                        if (message.equals("Remote host terminated the handshake")) {
                            LOGGER.warn("Got a \"Remote host terminated the handshake\"");
                            continue;
                        } else if (message.startsWith("Insufficient buffer remaining for AEAD cipher fragment")) {
                            continue;
                        }
                    }
                    LOGGER.error("Error while handling Tiltify Webhook event", e);
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Error while starting Tiltify Webhook server", e);
        }
    }

    private void handleClientConnection(SSLSocket sslSocket) throws IOException {
        LOGGER.info("Handling Tiltify Webhook event");
        // Get the input and output streams from the SSL socket
        BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);

        // Read the request line
        String requestLine = reader.readLine();

        // Read headers
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        int contentLength = 0;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerContent = headerLine.split(": ", 2);
            if (headerContent.length != 2) continue;
            headers.put(headerContent[0].toLowerCase(), headerContent[1]);

            // Check for Content-Length header
            if (headerContent[0].equalsIgnoreCase("content-length")) {
                contentLength = getContentLength(headerLine);
            }
        }

        // Read the message body
        StringBuilder requestBody = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            requestBody.append((char) reader.read());
        }

        // Process the request
        boolean error = false;
        try {
            processData(requestLine, headers, requestBody.toString());
        } catch (JsonParseException | JsonProcessingException ignored) {
            LOGGER.error("Failed to parse Tiltify Webhook event", ignored);
        } catch (Throwable e) {
            LOGGER.error("Error while processing Tiltify Webhook event", e);
            error = true;
        }

        // Send a basic HTTP response with the received message body
        String httpResponse = error ? """
          HTTP/1.1 401 Unauthorized\r
          \r
          """ : """
          HTTP/1.1 200 OK\r
          \r
          """;
        writer.println(httpResponse);

        // Close the streams and socket
        reader.close();
        writer.close();
        sslSocket.close();
    }

    private void processData(String request, Map<String, String> headers, String body) throws JsonProcessingException {
        LOGGER.info("Received Tiltify Webhook event");
        request = request.toLowerCase();
        if (!request.startsWith("post / ")) return;
        if (!headers.containsKey("x-tiltify-signature")) return;
        if (!headers.containsKey("x-tiltify-timestamp")) return;

        String signature = headers.get("x-tiltify-signature");
        String timestamp = headers.get("x-tiltify-timestamp");
        String secret = ServerConfig.instance().tiltifySecret;

        if (!verifySignature(secret, signature, timestamp, body)) return;

        //Parse the body JSON
        WebhookEvent<DonationUpdated> event = JSON_MAPPER.readValue(body, WebhookEvent.DonationUpdatedEvent.class);
        if (event == null || event.data == null || event.meta == null) return;
        if (!event.meta.eventType.type.equalsIgnoreCase("donation_updated")) return;

        if (Playroom.hasDonation(event.meta.id)) return;

        if (event.data.rewardClaims != null) {
            for (WebhookStructure.RewardClaim claim : event.data.rewardClaims) {
                ServerPlayerEntity player = Playroom.getServer().getPlayerManager().getPlayer(claim.customQuestion);
                if (player == null) {
                    LOGGER.warn("Failed to find player \"{}\", {}'s donation could not be fully processed", claim.customQuestion, event.data.donorName);
                    continue; //TODO inform the team it failed to find the player
                }

                ServerConfig.instance().commands.get(claim.rewardId.toString()).forEach(command -> {
                    Playroom.getServer().getCommandManager().executeWithPrefix(Playroom.getCommandSource(), DynamicPlaceholders.parseText(command, player).getString());
                });
            }
        }

        //Playroom.addDonation(new Donation());
    }

    private int getContentLength(String requestLine) {
        String[] parts = requestLine.split(" ", 2);
        if (parts[0].equalsIgnoreCase("Content-Length:")) {
            return Integer.parseInt(parts[1]);
        }
        return 0;
    }

    public boolean verifySignature(String secret, String signature, String timestamp, String body) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            hmacSha256.init(secretKey);

            String dataToHash = timestamp + "." + body;
            byte[] hashedBytes = hmacSha256.doFinal(dataToHash.getBytes());

            String computedSignature = Base64.getEncoder().encodeToString(hashedBytes);

            return signature.equals(computedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Handle exceptions accordingly
            LOGGER.error("Error while verifying Tiltify signature", e);
            return false;
        }
    }

    @Override
    public void interrupt() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.error("Error while closing Tiltify Webhook server", e);
            }
        }
        LOGGER.info("Closing Tiltify Webhook thread");
        super.interrupt();
        LOGGER.info("Tiltify Webhook thread closed");
    }

    private static byte[] readBytes(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = fis.read(buf)) > 0) {
                bos.write(buf, 0, read);
            }
            return bos.toByteArray();
        }
    }

    private static PrivateKey loadPrivateKey(Path filePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (PemReader pemReader = new PemReader(new FileReader(filePath.toFile()))) {
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
            return keyFactory.generatePrivate(keySpec);
        }
    }

    static class X509KeyManagerImpl implements X509KeyManager {
        private final PrivateKey privateKey;
        private final java.security.cert.Certificate certificate;

        public X509KeyManagerImpl(PrivateKey privateKey, java.security.cert.Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }

        @Override
        public String[] getClientAliases(String s, Principal[] principals) {
            return new String[]{""};
        }

        @Override
        public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
            return "";
        }

        @Override
        public String[] getServerAliases(String s, Principal[] principals) {
            return new String[]{""};
        }

        @Override
        public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
            return "";
        }

        @Override
        public X509Certificate[] getCertificateChain(String s) {
            return new X509Certificate[]{(X509Certificate) certificate};
        }

        @Override
        public PrivateKey getPrivateKey(String s) {
            return privateKey;
        }
    }
}
