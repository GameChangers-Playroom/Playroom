package io.github.flameyheart.playroom.tiltify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.JsonParseException;
import io.github.flameyheart.playroom.Constants;
import io.github.flameyheart.playroom.Playroom;
import io.github.flameyheart.playroom.config.ServerConfig;
import io.github.flameyheart.playroom.tiltify.webhook.DonationUpdated;
import io.github.flameyheart.playroom.tiltify.webhook.WebhookEvent;
import io.github.flameyheart.playroom.tiltify.webhook.WebhookStructure;
import io.github.flameyheart.playroom.util.ClassUtils;
import io.github.flameyheart.playroom.util.LinedStringBuilder;
import io.github.flameyheart.playroom.util.PredicateUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles all incoming traffic from the Tiltify webhook
 **/
public class TiltifyWebhookConnection extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger("Playroom Tiltify Webhook Connection");
    private static final AtomicReference<Boolean> CAN_RUN = new AtomicReference<>();
    public transient int requestsLastMinute = 0;
    private transient long lastWarning = 0;

    /**
     * Jackson JSON mapper for the parsing of the JSON data into Java objects
     **/
    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();
    /**
     * Whether the thread is running or not, this is a failsafe to ensure the thread closes properly
     **/
    private boolean running = true;
    /**
     * The server socket used to listen for incoming connections
     **/
    private SSLServerSocket serverSocket;

    /**
     * The constructor for the Tiltify Webhook Connection thread
     * it already sets the thread name and daemon status
     **/
    private TiltifyWebhookConnection() {
        super("Tiltify Webhook Connection");
        setDaemon(true);

        setUncaughtExceptionHandler((t, e) -> {
            LOGGER.error("Uncaught exception in Tiltify Webhook thread " + t.getName(), e);
            LOGGER.error("Closing Tiltify Webhook thread due to unhandled exception");
            this.interrupt();
        });
    }

    @Nullable
    public static TiltifyWebhookConnection create() {
        Boolean canRun = CAN_RUN.get();
        if (canRun == null) {
            boolean hasBc = ClassUtils.exists("org.bouncycastle.util.io.pem.PemReader");
            CAN_RUN.set(hasBc);
            canRun = hasBc;
            if (!canRun) LOGGER.warn("Bouncy Castle is not installed, the Tiltify Webhook integration will not be available");
        }
        if (canRun) {
            return new TiltifyWebhookConnection();
        } else {
            return null;
        }
    }

    /**
     * Handles the incoming connections on the configured port
     * <p>
     *     This method will load the certificate and private key
     *     then it will create an SSL server socket factory
     *     and finally it will create an SSL server socket
     *     and listen for incoming connections
     *     when a connection is received {@link #handleClientConnection(SSLSocket)} is called
     *     to handle the request
     *     if an exception occurs while handling the request
     *     it will be logged and the thread will continue listening for incoming connections
     *     if an exception occurs while starting the server
     *     it will be logged and the thread will stop
     * </p>
     **/
    @Override
    public void run() {
        LOGGER.info("Starting Tiltify Webhook thread");
        // Load certificate and private key
        Path certificatePath = Playroom.getConfigPath().resolve("cert.pem"),
          privateKeyPath = Playroom.getConfigPath().resolve("key.pem");

        // Get the port from the config
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
            Playroom.getCommandSource().sendFeedback(() -> Text.translatable("feedback.playroom.webhook.ready"), true);

            // Listen for incoming connections
            while (running) {
                try {
                    if (serverSocket.isClosed()) break;
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    // Warn if more than 50 requests are received within a minute,
                    // letting us know if there is a request spam or DoS/DDoS attack,
                    // the warning is only printed once every 5 seconds
                    // to prevent spamming the console
                    if (++requestsLastMinute > 50 && lastWarning < System.currentTimeMillis() - 5000) {
                        lastWarning = System.currentTimeMillis();
                        LOGGER.warn("Received {} requests within a minute!", requestsLastMinute);
                    }
                    handleClientConnection(clientSocket);
                } catch (Throwable e) {
                    // Handle exceptions accordingly
                    if (e instanceof SSLHandshakeException) {
                        String message = e.getMessage();
                        // Don't ppprint full stack trace for "Remote host terminated the handshake" exception
                        if (message.equals("Remote host terminated the handshake")) {
                            LOGGER.warn("Got a \"Remote host terminated the handshake\"");
                            continue;
                        } else if (message.startsWith("Insufficient buffer remaining for AEAD cipher fragment")) { // Ignore this exception
                            continue;
                        }
                    } else if (e instanceof SocketException) {
                        String message = e.getMessage();
                        // Ignore "Socket closed" exception if the thread is not running
                        if (message.equals("Socket closed") && !running) {
                            return;
                        }
                        if (message.equals("Connection reset")) {
                            LOGGER.warn("Connection reset");
                            continue;
                        }
                    } else if (e instanceof SSLException) {
                        String message = e.getMessage();
                        // Don't print full stack trace for "Unsupported or unrecognized SSL message" exception
                        if (message.equals("Unsupported or unrecognized SSL message")) {
                            LOGGER.warn("Received invalid SSL message");
                            continue;
                        }
                    }
                    // Print full stack trace for all other exceptions
                    LOGGER.error("Error while handling Tiltify Webhook event", e);
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Error while starting Tiltify Webhook server", e);
            LOGGER.error("Closing thread due to startup error");
            this.interrupt();
        }
    }

    /**
     * Handle incoming message, pre-processing headers and body
     *
     * @param sslSocket The socket to read from
     *
     * @throws IOException If an I/O error occurs
     **/
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
            // Split header line into key-value
            String[] headerContent = headerLine.split(": ", 2);
            if (headerContent.length != 2) continue;
            // Store headers in a map, with a lowercase key
            headers.put(headerContent[0].toLowerCase(), headerContent[1]);

            // Check for Content-Length header
            if (headerContent[0].equalsIgnoreCase("content-length")) {
                // Get the content length
                contentLength = getContentLength(headerLine);
            }
        }

        // Read the message body
        StringBuilder requestBody = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            requestBody.append((char) reader.read());
        }

        // Process the request
        boolean error;
        try {
            // Process the request
            error = !processData(requestLine, headers, requestBody.toString());
        } catch (JsonParseException | JsonProcessingException e) {
            LOGGER.error("Failed to parse Tiltify Webhook event", e);
            error = true;
        } catch (Throwable e) {
            LOGGER.error("Error while processing Tiltify Webhook event", e);
            error = true;
        }

        // Send a basic HTTP response with the received message body
        String httpResponse = error ? """
          HTTP/1.1 400 Bad Request\r
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

    /**
     * Processes the request
     * <p>
     *     This method is called when a request is received
     *     It will parse the request and verify the signature
     *     Then it will parse the JSON body and process the donation
     *     Finally it will add the donation to the list of donations
     * </p>
     *
     * @param request The raw request content
     * @param headers The request headers
     * @param body The request body
     *
     * @throws JsonProcessingException If the JSON body could not be parsed
     *
     * @see #verifySignature(String, String, String, String)
     **/
    private boolean processData(String request, Map<String, String> headers, String body) throws JsonProcessingException {
        request = request.toLowerCase();
        // Only process POST requests to the root path
        if (!request.startsWith("post / ")) return false;
        // Certificate required headers are present
        if (!headers.containsKey("x-tiltify-signature")) return false;
        if (!headers.containsKey("x-tiltify-timestamp")) return false;

        // Get required headers
        // The signature is the encoded hash of the timestamp and body using the secret as the key
        String signature = headers.get("x-tiltify-signature");
        String timestamp = headers.get("x-tiltify-timestamp");

        // The secret used to encode/decode the signature
        String secret = ServerConfig.instance().tiltifySecret;

        //Validates the signature, making sure the request is from Tiltify
        if (!verifySignature(secret, signature, timestamp, body)) return false;

        //Parse the body JSON using Jackson
        WebhookEvent<DonationUpdated> event = JSON_MAPPER.readValue(body, WebhookEvent.DonationUpdatedEvent.class);
        // Certify the required fields are present
        if (event == null || event.data == null || event.meta == null) return false;
        // Certify the event type is donation_updated
        if (!event.meta.eventType.type.equalsIgnoreCase("donation_updated")) return false;

        // Certify that the donation hasn't been processed already
        if (Playroom.hasDonation(event.meta.id)) return false;

        // Build the reward list to send to the player
        List<Donation.Reward> rewards = new ArrayList<>();

        // if the donation has a reward claim, execute the corresponding action for the target player
        if (event.data.rewardClaims != null) {
            for (WebhookStructure.RewardClaim claim : event.data.rewardClaims) {
                Automation.Task<?> task = Automation.get(claim.rewardId);
                ServerPlayerEntity target = null;
                if (task == null) {
                    LOGGER.warn("Failed to find task for reward \"{}\", {}'s donation could not be fully processed", claim.rewardId, event.data.donorName);
                    MutableText message = generateFailedMessageForTask(claim, event);
                    Playroom.sendToPlayers(p -> p.sendMessage(message), PredicateUtils.permission("playroom.webhook.fail", 4));
                    rewards.add(new Donation.Reward(claim.rewardId, claim.id, "Not found", claim.customQuestion, Donation.Reward.Status.TASK_NOT_FOUND));
                    continue;
                }

                if (task.requiresPlayer()) {
                    String playerName = claim.customQuestion.replaceAll(" ", "");
                    if (Constants.ALTERNATIVE_NAMES.containsKey(playerName)) {
                        playerName = Constants.ALTERNATIVE_NAMES.get(playerName);
                    }
                    target = Playroom.getServer().getPlayerManager().getPlayer(playerName);
                    if (target == null) {
                        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();
                        int threshold = 3;

                        int minDistance = Integer.MAX_VALUE;
                        String closestMatch = "";

                        for (String word : Constants.POSSIBLE_NAMES) {
                            int distance = levenshteinDistance.apply(playerName, word);
                            if (distance < minDistance) {
                                minDistance = distance;
                                closestMatch = word;
                            }

                            if (minDistance > threshold) break;
                        }

                        if (minDistance <= threshold) {
                            playerName = closestMatch;
                            target = Playroom.getServer().getPlayerManager().getPlayer(playerName);
                        }
                    }

                    if (target == null) {
                        LOGGER.warn("Failed to find player \"{}\", {}'s donation could not be fully processed", claim.customQuestion, event.data.donorName);
                        MutableText message = generateFailedMessageForPlayer(claim, event, task, playerName);
                        Playroom.sendToPlayers(p -> p.sendMessage(message), PredicateUtils.permission("playroom.webhook.fail", 4));
                        rewards.add(new Donation.Reward(claim.rewardId, claim.id, task.name(), claim.customQuestion, Donation.Reward.Status.PLAYER_NOT_FOUND));
                        continue;
                    }

                    Playroom.queueTask(new ExecuteAction(task, target, event.data.donorName));
                } else {
                    Playroom.queueTask(new ExecuteAction(task, null, event.data.donorName));
                }

                Donation.Reward.Status status = Donation.Reward.Status.AUTO_APPROVED;
                if (target != null && Permissions.check(target, "playroom.bypass-rewards", 4)) {
                    status = Donation.Reward.Status.BYPASSED;
                }

                rewards.add(new Donation.Reward(claim.rewardId, claim.id, task.name(), claim.customQuestion, target == null ? Donation.Reward.NULL_UUID : target.getUuid(), status));
            }
        }

        // Add the event to the list of donations, with its according status of automatically executed
        Playroom.addDonation(new Donation(event.meta.id, event.data.donorName, event.data.donorComment, rewards, event.data.amount.value, event.data.amount.currency));
        return true;
    }

    @NotNull
    private static MutableText generateFailedMessageForPlayer(WebhookStructure.RewardClaim claim, WebhookEvent<DonationUpdated> event, Automation.Task<?> task, String closeMatch) {
        MutableText message = Text.translatable("feedback.playroom.webhook.donation.failed.player");
        message.styled(style -> {
            LinedStringBuilder text = formatFailedMessage(claim, event, closeMatch);
            text.appendLine("Task: ").append(task.name());
            text.appendLine().appendLine("Click to copy the task class");
            return style
              .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(text.toString())))
              .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, task.className()));
        });
        return message;
    }

    @NotNull
    private static MutableText generateFailedMessageForTask(WebhookStructure.RewardClaim claim, WebhookEvent<DonationUpdated> event) {
        MutableText message = Text.translatable("feedback.playroom.webhook.donation.failed.task");
        message.styled(style -> {
            LinedStringBuilder text = formatFailedMessage(claim, event, null);
            return style
              .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(text.toString())))
              .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, claim.rewardId.toString()));
        });
        return message;
    }

    private static LinedStringBuilder formatFailedMessage(WebhookStructure.RewardClaim claim, WebhookEvent<DonationUpdated> event, @Nullable String closeMatch) {
        LinedStringBuilder text = new LinedStringBuilder();
        text.append("Donation: ").append(event.meta.id);
        text.appendLine("Donor: ").append(event.data.donorName);
        text.appendLine("Message: ").append(claim.customQuestion);
        text.appendLine("Close match: ").append(closeMatch);
        text.appendLine("Reward: ").append(claim.rewardId.toString());
        return text;
    }

    /**
     * Gets the content length from the request line
     *
     * @param header The Content-Length header
     *
     * @return The content length according to the header
     **/
    private int getContentLength(String header) {
        String[] parts = header.split(" ", 2);
        if (parts[0].equalsIgnoreCase("Content-Length:")) {
            return Integer.parseInt(parts[1]);
        }
        return 0;
    }

    /**
     * Validates the signature of the request
     * <p>
     *     The method gets the secret, timestamp and body
     *     it uses the timestamp and body to create the message to encode
     *     than it encodes the message using the secret as the key
     *     finally it compares the encoded message with the signature
     *     if they match, the signature is valid
     * </p>
     *
     * @param secret The secret used to encode/decode the signature
     * @param signature The signature to validate
     * @param timestamp The timestamp of the request
     * @param body The body of the request
     *
     * @return Whether the signature is valid or not
     **/
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
        private final Certificate certificate;

        public X509KeyManagerImpl(PrivateKey privateKey, Certificate certificate) {
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
