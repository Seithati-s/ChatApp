package com.chatapp;

import java.util.regex.Pattern;

/**
 * Handles registration and login for the chat app.
 *
 * Each check is its own small method so it can be unit tested on its own.
 */
public class Login {

    // ---- Messages (kept in one place so they are easy to change) ----
    public static final String USERNAME_OK = "Username successfully captured.";
    public static final String USERNAME_BAD =
            "Username is not correctly formatted; please ensure that your username "
            + "contains an underscore and is no more than five characters in length.";

    public static final String PASSWORD_OK = "Password successfully captured.";
    public static final String PASSWORD_BAD =
            "Password is not correctly formatted; please ensure that the password "
            + "contains at least eight characters, a capital letter, a number, "
            + "and a special character.";

    public static final String CELL_OK = "Cell number successfully captured.";
    public static final String CELL_BAD =
            "Cell number is incorrectly formatted or does not contain an "
            + "international code; please correct the number and try again.";

    public static final String LOGIN_BAD = "Username or password incorrect, please try again.";

    /*
     * Cell phone pattern:
     *  ^        start of the text
     *  \+       a plus sign (the international code starts with +)
     *  \d{1,3}  country code, 1 to 3 digits (for example 27 for South Africa)
     *  \d{1,10} the number itself, no more than ten digits
     *  $        end of the text
     *
     * Reference (pattern syntax and Pattern class):
     * Oracle (2024) Pattern (Java Platform SE 8). Available at:
     * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
     * (Accessed: <add the date you accessed it>).
     *
     * Goyvaerts, J. (n.d.) Regular-Expressions.info. Available at:
     * https://www.regular-expressions.info/ (Accessed: <add the date you accessed it>).
     */
    private static final Pattern CELL_PATTERN = Pattern.compile("^\\+\\d{1,3}\\d{1,10}$");

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellPhone;

    public Login(String firstName, String lastName, String username,
                 String password, String cellPhone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.cellPhone = cellPhone;
    }

    // ---- Setters (used by the console app to re-enter a wrong value) ----
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }

    /** Username must contain an underscore and be no more than 5 characters long. */
    public boolean checkUserName() {
        return username != null
                && username.contains("_")
                && username.length() <= 5;
    }

    /** Password needs 8+ characters, a capital letter, a number and a special character. */
    public boolean checkPasswordComplexity() {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasCapital = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                hasSpecial = true;
            }
        }
        return hasCapital && hasNumber && hasSpecial;
    }

    /** Cell number must start with an international code (+) and fit the pattern above. */
    public boolean checkCellPhoneNumber() {
        return cellPhone != null && CELL_PATTERN.matcher(cellPhone).matches();
    }

    // ---- Message helpers ----
    public String usernameMessage() {
        return checkUserName() ? USERNAME_OK : USERNAME_BAD;
    }

    public String passwordMessage() {
        return checkPasswordComplexity() ? PASSWORD_OK : PASSWORD_BAD;
    }

    public String cellMessage() {
        return checkCellPhoneNumber() ? CELL_OK : CELL_BAD;
    }

    /**
     * Checks username, password and cell number in that order.
     * Returns the first error message, or all three success messages if everything is fine.
     */
    public String registerUser() {
        if (!checkUserName()) {
            return USERNAME_BAD;
        }
        if (!checkPasswordComplexity()) {
            return PASSWORD_BAD;
        }
        if (!checkCellPhoneNumber()) {
            return CELL_BAD;
        }
        return USERNAME_OK + "\n" + PASSWORD_OK + "\n" + CELL_OK;
    }

    /** True when the details entered match the registered username and password. */
    public boolean loginUser(String enteredUsername, String enteredPassword) {
        return username != null && password != null
                && username.equals(enteredUsername)
                && password.equals(enteredPassword);
    }

    /** Message shown after a login attempt. */
    public String returnLoginStatus(boolean loggedIn) {
        if (loggedIn) {
            return "Welcome " + firstName + " " + lastName
                    + " it is great to see you again.";
        }
        return LOGIN_BAD;
    }
}
