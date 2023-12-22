package io.github.flameyheart.playroom.tiltify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
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
    private boolean running = true;
    private SSLServerSocket serverSocket;
    private final Gson GSON = new GsonBuilder().serializeNulls().create();

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

        short port = ServerConfig.instance().tiltifyWebhookPort;
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
        } catch (Throwable e) {
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

    private void processData(String request, Map<String, String> headers, String body) {
        request = request.toLowerCase();
        if (!request.startsWith("post / ")) return;
        if (!headers.containsKey("x-tiltify-signature")) return;
        if (!headers.containsKey("x-tiltify-timestamp")) return;

        String signature = headers.get("x-tiltify-signature");
        String timestamp = headers.get("x-tiltify-timestamp");
        String secret = ServerConfig.instance().tiltifySecret;

        if (!verifySignature(secret, signature, timestamp, body)) return;

        //Parse the body JSON
        JsonElement jsonElement = GSON.fromJson(body, JsonElement.class);
        if (!jsonElement.isJsonObject()) return;
        JsonObject json = jsonElement.getAsJsonObject();
        if (!json.has("data")) return;
        if (!json.has("meta")) return;
        if (!json.getAsJsonObject("meta").has("event_type")) return;
        String eventType = json.getAsJsonObject("meta").get("event_type").getAsString();
        if (!eventType.equalsIgnoreCase("private:direct:donation_updated") && !eventType.equalsIgnoreCase("public:direct:donation_updated")) return;

        JsonObject data = json.getAsJsonObject("data");
        if (!data.has("amount")) return;
        if (!data.has("id")) return;
        if (!data.has("completed_at")) return;
        if (!data.has("donor_name")) return;
        if (!data.has("donor_comment")) return;

        String donor = data.get("donor_name").getAsString();
        String comment = data.get("donor_comment").getAsString();
        UUID id = UUID.fromString(data.get("id").getAsString());
        JsonObject amount = data.get("amount").getAsJsonObject();
        if (!amount.has("currency")) return;
        if (!amount.has("value")) return;

        String currency = amount.get("currency").getAsString();
        double value = amount.get("value").getAsDouble();

        LOGGER.info("Received Tiltify donation: " + donor + " (" + comment + ") " + value + " " + currency);

        Playroom.addDonation(new Donation(id, donor, comment, (float) value, currency, false));
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
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
