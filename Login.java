/**
 * Registration and login. Users can only send messages after loginUser() succeeds.
 * (If your Part 1 Login class is already finished, drop it in here instead.)
 */
public class Login {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellNumber;

    /** Username needs an underscore and at most five characters. */
    public boolean checkUserName(String name) {
        return name != null && name.length() <= 5 && name.contains("_");
    }

    /** Password needs 8+ characters, a capital letter, a number and a special character. */
    public boolean checkPasswordComplexity(String pass) {
        if (pass == null || pass.length() < 8) {
            return false;
        }
        boolean capital = false;
        boolean number = false;
        boolean special = false;
        for (int i = 0; i < pass.length(); i++) {
            char c = pass.charAt(i);
            if (Character.isUpperCase(c)) {
                capital = true;
            } else if (Character.isDigit(c)) {
                number = true;
            } else if (!Character.isLetter(c)) {
                special = true;
            }
        }
        return capital && number && special;
    }

    public boolean checkCellPhoneNumber(String cell) {
        return Message.isValidCell(cell);
    }

    public String registerUser(String first, String last, String name, String pass, String cell) {
        if (!checkUserName(name)) {
            return "Username is not correctly formatted, please ensure that your username "
                    + "contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(pass)) {
            return "Password is not correctly formatted, please ensure that the password contains "
                    + "at least eight characters, a capital letter, a number and a special character.";
        }
        if (!checkCellPhoneNumber(cell)) {
            return Message.CELL_BAD;
        }
        this.firstName = first;
        this.lastName = last;
        this.username = name;
        this.password = pass;
        this.cellNumber = cell;
        return "User successfully registered.";
    }

    public boolean loginUser(String name, String pass) {
        return username != null && username.equals(name) && password.equals(pass);
    }

    public String returnLoginStatus(boolean loggedIn) {
        if (loggedIn) {
            return "Welcome " + firstName + " " + lastName + ", it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }
}
