import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests built from the test data in the brief.
 * The IDs below all start with "00" so the expected hashes match the brief (00:0:HITONIGHT).
 */
class MessageTest {

    // Test data - message 1 and message 2
    private static final String ID_1 = "0012345678";
    private static final String CELL_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";

    private static final String ID_2 = "0087654321";
    private static final String CELL_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    @BeforeEach
    void freshStart() {
        Message.clearSentMessages();
    }

    // ---------- message length ----------

    @Test
    void messageUnder250CharactersIsAccepted() {
        assertEquals("Message ready to send.", Message.checkMessageLength(TEXT_1));
    }

    @Test
    void messageOfExactly250CharactersIsAccepted() {
        assertEquals("Message ready to send.", Message.checkMessageLength("a".repeat(250)));
    }

    @Test
    void messageOver250CharactersIsRejected() {
        String tooLong = "a".repeat(260);
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength(tooLong));
    }

    // ---------- recipient number ----------

    @Test
    void recipientNumberWithInternationalCodeSucceeds() {
        Message m = new Message(ID_1, 0, CELL_1, TEXT_1);
        assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
    }

    @Test
    void recipientNumberWithoutInternationalCodeFails() {
        Message m = new Message(ID_2, 1, CELL_2, TEXT_2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.",
                m.checkRecipientCell());
    }

    // ---------- message hash ----------

    @Test
    void hashForTestCaseOne() {
        Message m = new Message(ID_1, 0, CELL_1, TEXT_1);
        assertEquals("00:0:HITONIGHT", m.createMessageHash());
    }

    @Test
    void hashIgnoresPunctuationOnFirstWord() {
        Message m = new Message("0099999999", 0, CELL_1, "Hi, thanks");
        assertEquals("00:0:HITHANKS", m.createMessageHash());
    }

    @Test
    void hashesForAllTestMessagesInALoop() {
        String[] ids = {ID_1, ID_2};
        String[] cells = {CELL_1, CELL_2};
        String[] texts = {TEXT_1, TEXT_2};
        String[] expected = {"00:0:HITONIGHT", "00:1:HIPAYMENT"};

        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(ids[i], i, cells[i], texts[i]);
            assertEquals(expected[i], m.createMessageHash(), "hash for message " + i);
        }
    }

    // ---------- message ID ----------

    @Test
    void generatedMessageIdIsTenDigits() {
        Message m = new Message(0, CELL_1, TEXT_1);
        assertEquals(10, m.getMessageId().length());
        assertTrue(m.checkMessageID());
        assertEquals("Message ID generated: " + m.getMessageId(), m.messageIdGenerated());
    }

    @Test
    void messageIdLongerThanTenCharactersFails() {
        Message m = new Message("00123456789", 0, CELL_1, TEXT_1);
        assertFalse(m.checkMessageID());
    }

    // ---------- send / disregard / store ----------

    @Test
    void sendOptionReturnsSentMessage() {
        Message m = new Message(ID_1, 0, CELL_1, TEXT_1);
        assertEquals("Message successfully sent.", m.sentMessage(1));
    }

    @Test
    void disregardOptionAsksToPressZero() {
        Message m = new Message(ID_2, 1, CELL_2, TEXT_2);
        assertEquals("Press 0 to delete the message.", m.sentMessage(2));
    }

    @Test
    void storeOptionWritesJsonFile() throws IOException {
        Path temp = Files.createTempFile("quickchat", ".json");
        Files.deleteIfExists(temp);
        Message.setStoreFile(temp);

        Message first = new Message(ID_1, 0, CELL_1, TEXT_1);
        Message second = new Message(ID_2, 1, "+27831234567", TEXT_2);

        assertEquals("Message successfully stored.", first.sentMessage(3));
        assertEquals("Message successfully stored.", second.sentMessage(3));

        String json = new String(Files.readAllBytes(temp), StandardCharsets.UTF_8);
        assertTrue(json.trim().startsWith("["));
        assertTrue(json.trim().endsWith("]"));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"messageHash\": \"00:1:HIPAYMENT\""));
        Files.deleteIfExists(temp);
    }

    // ---------- totals and printing ----------

    @Test
    void onlySentMessagesAreCounted() {
        Message sent = new Message(ID_1, 0, CELL_1, TEXT_1);
        Message discarded = new Message(ID_2, 1, CELL_2, TEXT_2);

        sent.sentMessage(1);
        discarded.sentMessage(2);

        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void printMessagesShowsDetailsInTheRequiredOrder() {
        new Message(ID_1, 0, CELL_1, TEXT_1).sentMessage(1);
        String printed = Message.printMessages();

        int id = printed.indexOf(ID_1);
        int hash = printed.indexOf("00:0:HITONIGHT");
        int recipient = printed.indexOf(CELL_1);
        int text = printed.indexOf(TEXT_1);

        assertTrue(id >= 0 && id < hash && hash < recipient && recipient < text);
    }

    @Test
    void messageWithQuotesIsEscapedInJson() throws IOException {
        Path temp = Files.createTempFile("quickchat", ".json");
        Files.deleteIfExists(temp);
        Message.setStoreFile(temp);

        new Message(ID_1, 0, CELL_1, "Say \"hi\" now").sentMessage(3);

        String json = new String(Files.readAllBytes(temp), StandardCharsets.UTF_8);
        assertTrue(json.contains("Say \\\"hi\\\" now"));
        Files.deleteIfExists(temp);
    }
}
