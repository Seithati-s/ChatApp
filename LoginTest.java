package com.chatapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginTest {

    // Test data
    private static final String GOOD_USER = "kyl_1";
    private static final String BAD_USER = "kyle!!!!!!!";
    private static final String GOOD_PASS = "Ch&&sec@ke99!";
    private static final String BAD_PASS = "password";
    private static final String GOOD_CELL = "+27838968976";
    private static final String BAD_CELL = "08966553";

    private Login make(String user, String pass, String cell) {
        return new Login("Kyle", "Smith", user, pass, cell);
    }

    // ---------- assertEquals: messages ----------

    @Test
    void usernameCorrectlyFormattedMessage() {
        assertEquals("Username successfully captured.",
                make(GOOD_USER, GOOD_PASS, GOOD_CELL).usernameMessage());
    }

    @Test
    void usernameIncorrectlyFormattedMessage() {
        assertEquals("Username is not correctly formatted; please ensure that your username "
                        + "contains an underscore and is no more than five characters in length.",
                make(BAD_USER, GOOD_PASS, GOOD_CELL).usernameMessage());
    }

    @Test
    void passwordMeetsComplexityMessage() {
        assertEquals("Password successfully captured.",
                make(GOOD_USER, GOOD_PASS, GOOD_CELL).passwordMessage());
    }

    @Test
    void passwordDoesNotMeetComplexityMessage() {
        assertEquals("Password is not correctly formatted; please ensure that the password "
                        + "contains at least eight characters, a capital letter, a number, "
                        + "and a special character.",
                make(GOOD_USER, BAD_PASS, GOOD_CELL).passwordMessage());
    }

    @Test
    void cellPhoneCorrectlyFormattedMessage() {
        assertEquals("Cell number successfully captured.",
                make(GOOD_USER, GOOD_PASS, GOOD_CELL).cellMessage());
    }

    @Test
    void cellPhoneIncorrectlyFormattedMessage() {
        assertEquals("Cell number is incorrectly formatted or does not contain an "
                        + "international code; please correct the number and try again.",
                make(GOOD_USER, GOOD_PASS, BAD_CELL).cellMessage());
    }

    @Test
    void registerUserReturnsFirstError() {
        assertEquals(Login.USERNAME_BAD, make(BAD_USER, GOOD_PASS, GOOD_CELL).registerUser());
        assertEquals(Login.PASSWORD_BAD, make(GOOD_USER, BAD_PASS, GOOD_CELL).registerUser());
        assertEquals(Login.CELL_BAD, make(GOOD_USER, GOOD_PASS, BAD_CELL).registerUser());
    }

    @Test
    void registerUserSuccess() {
        assertEquals("Username successfully captured.\nPassword successfully captured.\n"
                        + "Cell number successfully captured.",
                make(GOOD_USER, GOOD_PASS, GOOD_CELL).registerUser());
    }

    @Test
    void loginSuccessMessage() {
        Login user = make(GOOD_USER, GOOD_PASS, GOOD_CELL);
        assertEquals("Welcome Kyle Smith it is great to see you again.",
                user.returnLoginStatus(user.loginUser(GOOD_USER, GOOD_PASS)));
    }

    @Test
    void loginFailedMessage() {
        Login user = make(GOOD_USER, GOOD_PASS, GOOD_CELL);
        assertEquals("Username or password incorrect, please try again.",
                user.returnLoginStatus(user.loginUser(GOOD_USER, "wrong")));
    }

    // ---------- assertTrue / assertFalse ----------

    @Test
    void loginSuccessful() {
        assertTrue(make(GOOD_USER, GOOD_PASS, GOOD_CELL).loginUser(GOOD_USER, GOOD_PASS));
    }

    @Test
    void loginFailed() {
        assertFalse(make(GOOD_USER, GOOD_PASS, GOOD_CELL).loginUser(GOOD_USER, "WrongPass1!"));
        assertFalse(make(GOOD_USER, GOOD_PASS, GOOD_CELL).loginUser("other", GOOD_PASS));
    }

    @Test
    void usernameCorrectlyFormatted() {
        assertTrue(make(GOOD_USER, GOOD_PASS, GOOD_CELL).checkUserName());
    }

    @Test
    void usernameIncorrectlyFormatted() {
        assertFalse(make(BAD_USER, GOOD_PASS, GOOD_CELL).checkUserName());
        assertFalse(make("kyle", GOOD_PASS, GOOD_CELL).checkUserName());      // no underscore
        assertFalse(make("ky_le1", GOOD_PASS, GOOD_CELL).checkUserName());    // longer than 5
    }

    @Test
    void passwordMeetsComplexity() {
        assertTrue(make(GOOD_USER, GOOD_PASS, GOOD_CELL).checkPasswordComplexity());
    }

    @Test
    void passwordDoesNotMeetComplexity() {
        assertFalse(make(GOOD_USER, BAD_PASS, GOOD_CELL).checkPasswordComplexity());
        assertFalse(make(GOOD_USER, "Ab1!", GOOD_CELL).checkPasswordComplexity());       // too short
        assertFalse(make(GOOD_USER, "password1!", GOOD_CELL).checkPasswordComplexity()); // no capital
        assertFalse(make(GOOD_USER, "Password!!", GOOD_CELL).checkPasswordComplexity()); // no number
        assertFalse(make(GOOD_USER, "Password12", GOOD_CELL).checkPasswordComplexity()); // no special
    }

    @Test
    void cellPhoneNumberCorrectlyFormatted() {
        assertTrue(make(GOOD_USER, GOOD_PASS, GOOD_CELL).checkCellPhoneNumber());
    }

    @Test
    void cellPhoneNumberIncorrectlyFormatted() {
        assertFalse(make(GOOD_USER, GOOD_PASS, BAD_CELL).checkCellPhoneNumber());
        assertFalse(make(GOOD_USER, GOOD_PASS, "27838968976").checkCellPhoneNumber()); // no +
    }
}
