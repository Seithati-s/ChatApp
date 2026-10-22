import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * A single QuickChat message, plus the shared list of everything sent this session.
 *
 * Sources:
 *  - JSON syntax and string escaping: https://www.json.org
 *  - Files, SecureRandom, StringBuilder: Oracle Java SE 17 API documentation
 */
public class Message {

    public static final int MAX_LENGTH = 250;

    public static final String LENGTH_OK = "Message ready to send.";
    public static final String CELL_OK = "Cell phone number successfully captured.";
    public static final String CELL_BAD = "Cell phone number is incorrectly formatted or does not "
            + "contain an international code. Please correct the number and try again.";

    private static final String NL = System.lineSeparator();
    private static final SecureRandom DICE = new SecureRandom();

    /** Messages that were actually sent while the program has been running. */
    private static final List<Message> outbox = new ArrayList<>();

    /** Target file for "store message". Tests swap this for a temp file. */
    private static Path vaultFile = Paths.get("storedMessages.json");

    private final String messageId;
    private final int numMessagesSent;
    private final String recipient;
    private final String text;
    private final String messageHash;

    /** Everyday constructor: a fresh random ID is created. */
    public Message(int numMessagesSent, String recipient, String text) {
        this(generateMessageId(), numMessagesSent, recipient, text);
    }

    /** Test constructor: a known ID makes the hash predictable. */
    public Message(String messageId, int numMessagesSent, String recipient, String text) {
        this.messageId = messageId;
        this.numMessagesSent = numMessagesSent;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    // ---------------------------------------------------------------- ID

    /** Rolls ten digits, one at a time, and glues them together. */
    public static String generateMessageId() {
        StringBuilder digits = new StringBuilder(10);
        for (int slot = 0; slot < 10; slot++) {
            digits.append(DICE.nextInt(10));
        }
        return digits.toString();
    }

    /** True when the ID is 1 to 10 characters long and made of digits only. */
    public boolean checkMessageID() {
        if (messageId == null || messageId.isEmpty() || messageId.length() > 10) {
            return false;
        }
        for (char c : messageId.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public String messageIdGenerated() {
        return "Message ID generated: " + messageId;
    }

    // ------------------------------------------------------ recipient / text

    /** +, a 1-3 digit international code, then at most ten more digits. */
    public static boolean isValidCell(String cell) {
        return cell != null && cell.matches("\\+\\d{1,3}\\d{1,10}");
    }

    public String checkRecipientCell() {
        return isValidCell(recipient) ? CELL_OK : CELL_BAD;
    }

    public static String checkMessageLength(String text) {
        int extra = text.length() - MAX_LENGTH;
        return extra > 0
                ? "Message exceeds 250 characters by " + extra + "; please reduce the size."
                : LENGTH_OK;
    }

    // -------------------------------------------------------------- hash

    /** First two ID digits : message number : FIRSTWORD + LASTWORD (all capitals). */
    public String createMessageHash() {
        String idStart = messageId.substring(0, 2);
        String[] words = text.trim().split("\\s+");
        String head = stripPunctuation(words[0]);
        String tail = stripPunctuation(words[words.length - 1]);
        return (idStart + ":" + numMessagesSent + ":" + head + tail).toUpperCase();
    }

    private static String stripPunctuation(String word) {
        StringBuilder kept = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                kept.append(c);
            }
        }
        return kept.toString();
    }

    // ----------------------------------------------------- send / store

    /** 1 = send, 2 = disregard, 3 = store for later. */
    public String sentMessage(int choice) {
        if (choice == 1) {
            outbox.add(this);
            return "Message successfully sent.";
        }
        if (choice == 2) {
            return "Press 0 to delete the message.";
        }
        if (choice == 3) {
            try {
                storeMessage();
                return "Message successfully stored.";
            } catch (IOException e) {
                return "Message could not be stored: " + e.getMessage();
            }
        }
        return "Invalid option. Please choose 1, 2 or 3.";
    }

    public static String printMessages() {
        List<String> blocks = new ArrayList<>();
        for (Message m : outbox) {
            blocks.add(m.getDetails());
        }
        return String.join(NL, blocks);
    }

    public static int returnTotalMessages() {
        return outbox.size();
    }

    /** Adds this message to the JSON array on disk (creating the file if needed). */
    public void storeMessage() throws IOException {
        String old = Files.exists(vaultFile)
                ? new String(Files.readAllBytes(vaultFile), StandardCharsets.UTF_8).trim()
                : "";

        List<String> entries = new ArrayList<>();
        if (old.startsWith("[") && old.endsWith("]") && old.length() > 2) {
            entries.add(old.substring(1, old.length() - 1).strip());
        }
        entries.add(toJson());

        String file = "[" + NL + String.join("," + NL, entries) + NL + "]" + NL;
        Files.write(vaultFile, file.getBytes(StandardCharsets.UTF_8));
    }

    // ----------------------------------------------------------- output

    /** Boxed view in the order the brief asks: ID, hash, recipient, message. */
    public String getDetails() {
        return "+------------------------------------------------" + NL
                + "| Message ID   : " + messageId + NL
                + "| Message Hash : " + messageHash + NL
                + "| Recipient    : " + recipient + NL
                + "| Message      : " + text + NL
                + "+------------------------------------------------" + NL;
    }

    private String toJson() {
        List<String> fields = new ArrayList<>();
        fields.add(jsonField("messageID", quote(messageId)));
        fields.add(jsonField("messageNumber", String.valueOf(numMessagesSent)));
        fields.add(jsonField("messageHash", quote(messageHash)));
        fields.add(jsonField("recipient", quote(recipient)));
        fields.add(jsonField("message", quote(text)));
        return "  {" + NL + String.join("," + NL, fields) + NL + "  }";
    }

    private static String jsonField(String key, String valueAlreadyJson) {
        return "    \"" + key + "\": " + valueAlreadyJson;
    }

    private static String quote(String raw) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : raw.toCharArray()) {
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.append('"').toString();
    }

    // ------------------------------------------------ getters / test hooks

    public String getMessageId() { return messageId; }
    public int getNumMessagesSent() { return numMessagesSent; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
    public String getMessageHash() { return messageHash; }

    public static void setStoreFile(Path path) { vaultFile = path; }

    public static void clearSentMessages() { outbox.clear(); }
}
