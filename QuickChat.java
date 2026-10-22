import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * QuickChat console app.
 * Order of events: banner -> register -> log in -> welcome -> how many messages -> menu loop.
 */
public class QuickChat {

    private static final int LOGIN_TRIES = 3;
    private static final String REGISTERED = "User successfully registered.";

    private final Scanner keyboard;
    private final Login login = new Login();

    private int messageQuota;
    private boolean quotaUsed = false;

    private QuickChat(Scanner keyboard) {
        this.keyboard = keyboard;
    }

    public static void main(String[] args) {
        try {
            new QuickChat(new Scanner(System.in)).start();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input ended. Goodbye!");
        }
    }

    // ------------------------------------------------------------ flow

    private void start() {
        printBanner();

        if (!signUpThenSignIn()) {
            System.out.println("Too many failed attempts. QuickChat is closing.");
            return;
        }

        System.out.println();
        System.out.println("Welcome to QuickChat.");
        messageQuota = askWholeNumber("How many messages do you want to enter? ", 1, Integer.MAX_VALUE);

        menuLoop();

        System.out.println();
        System.out.println("Total messages sent this session: " + Message.returnTotalMessages());
        System.out.println("Goodbye!");
    }

    private void menuLoop() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");

            int pick = askWholeNumber("Choose an option (1-3): ", 1, 3);

            if (pick == 1) {
                if (quotaUsed) {
                    System.out.println("You have already entered all " + messageQuota
                            + " messages for this session.");
                } else {
                    writeMessages();
                    quotaUsed = true;
                    System.out.println();
                    System.out.println("All messages processed. Total messages sent: "
                            + Message.returnTotalMessages());
                }
            } else if (pick == 2) {
                System.out.println("Coming Soon.");
            } else {
                running = false;
            }
        }
    }

    // ----------------------------------------------- register and login

    private void printBanner() {
        System.out.println("  ____        _      _     ____ _           _   ");
        System.out.println(" / __ \\ _   _(_) ___| | __/ ___| |__   __ _| |_ ");
        System.out.println("| |  | | | | | |/ __| |/ / |   | '_ \\ / _` | __|");
        System.out.println("| |__| | |_| | | (__|   <| |___| | | | (_| | |_ ");
        System.out.println(" \\___\\_\\\\__,_|_|\\___|_|\\_\\\\____|_| |_|\\__,_|\\__|");
        System.out.println();
    }

    private boolean signUpThenSignIn() {
        System.out.println("=== QuickChat registration ===");
        String first = prompt("First name: ");
        String last = prompt("Last name: ");

        String result;
        do {
            String user = prompt("Username (max 5 characters, include _): ");
            String pass = prompt("Password (8+ characters, capital, number, special): ");
            String cell = prompt("Cell number with international code (e.g. +27718693002): ");
            result = login.registerUser(first, last, user, pass, cell);
            System.out.println(result);
        } while (!result.equals(REGISTERED));

        System.out.println();
        System.out.println("=== Log in ===");
        for (int go = 1; go <= LOGIN_TRIES; go++) {
            String user = prompt("Username: ");
            String pass = prompt("Password: ");
            boolean ok = login.loginUser(user, pass);
            System.out.println(login.returnLoginStatus(ok));
            if (ok) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------- writing messages

    /** One pass of the for loop = one message. The counter becomes the message number. */
    private void writeMessages() {
        for (int counter = 0; counter < messageQuota; counter++) {
            System.out.println();
            System.out.println("--- Message " + (counter + 1) + " of " + messageQuota + " ---");

            String recipient = askRecipient();
            String body = askBody();

            Message msg = new Message(counter, recipient, body);
            System.out.println(msg.messageIdGenerated());

            System.out.println();
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int choice = askWholeNumber("Choose an option (1-3): ", 1, 3);

            System.out.println(msg.sentMessage(choice));

            if (choice == 1) {
                System.out.println();
                System.out.print(msg.getDetails());
            } else if (choice == 2) {
                waitForZero();
            }
        }
    }

    private void waitForZero() {
        String key = prompt("");
        while (!key.trim().equals("0")) {
            System.out.println("Press 0 to delete the message.");
            key = prompt("");
        }
        System.out.println("Message deleted.");
    }

    private String askRecipient() {
        String cell = prompt("Recipient cell number (with international code): ");
        while (!Message.isValidCell(cell)) {
            System.out.println(Message.CELL_BAD);
            cell = prompt("Recipient cell number (with international code): ");
        }
        System.out.println(Message.CELL_OK);
        return cell;
    }

    private String askBody() {
        String body = prompt("Message: ");
        while (body.trim().isEmpty() || body.length() > Message.MAX_LENGTH) {
            if (body.trim().isEmpty()) {
                System.out.println("The message cannot be empty.");
            } else {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(Message.checkMessageLength(body));
            }
            body = prompt("Message: ");
        }
        System.out.println(Message.checkMessageLength(body));
        return body;
    }

    // ------------------------------------------------------ input helpers

    private String prompt(String question) {
        System.out.print(question);
        return keyboard.nextLine();
    }

    /** Repeats the question until a whole number from min to max is typed. */
    private int askWholeNumber(String question, int min, int max) {
        while (true) {
            String typed = prompt(question).trim();
            try {
                int number = Integer.parseInt(typed);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException notANumber) {
                // handled by the reminder below
            }
            System.out.println(max == Integer.MAX_VALUE
                    ? "Please enter a whole number of " + min + " or more."
                    : "Please enter a whole number from " + min + " to " + max + ".");
        }
    }
}
