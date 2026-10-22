import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginTest {

    @Test
    void goodUserIsRegisteredAndCanLogIn() {
        Login login = new Login();
        String result = login.registerUser("Star", "Student", "st_r", "Passw0rd!", "+27718693002");
        assertEquals("User successfully registered.", result);
        assertTrue(login.loginUser("st_r", "Passw0rd!"));
        assertEquals("Welcome Star Student, it is great to see you again.", login.returnLoginStatus(true));
    }

    @Test
    void wrongPasswordCannotLogIn() {
        Login login = new Login();
        login.registerUser("Star", "Student", "st_r", "Passw0rd!", "+27718693002");
        assertFalse(login.loginUser("st_r", "wrong"));
    }

    @Test
    void usernameWithoutUnderscoreIsRejected() {
        assertFalse(new Login().checkUserName("star"));
    }

    @Test
    void weakPasswordIsRejected() {
        assertFalse(new Login().checkPasswordComplexity("password"));
    }
}
