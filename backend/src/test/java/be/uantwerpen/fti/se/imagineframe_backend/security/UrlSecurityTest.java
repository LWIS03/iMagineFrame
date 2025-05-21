package be.uantwerpen.fti.se.imagineframe_backend.security;

import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class UrlSecurityTest {

    private UrlSecurity urlSecurity;
    private User user;

    @BeforeEach
    void setUp() {
        urlSecurity = new UrlSecurity();
        user = Mockito.mock(User.class);
    }

    @Test
    void createUrlToken_ShouldReturnValidToken() throws NoSuchAlgorithmException {
        // Arrange
        Mockito.when(user.getId()).thenReturn(123L);
        Mockito.when(user.getFullName()).thenReturn("John Doe");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        // Act
        String token = urlSecurity.createUrlToken(user);

        // Assert
        assertTrue(token.contains("?token="));
        assertTrue(token.contains("&time=" + now));
        assertTrue(token.startsWith("123?token=")); // Since we mocked the user ID as 123
    }

    @Test
    void createUrlTokenTime_ShouldReturnValidTokenTime() throws NoSuchAlgorithmException {
        // Arrange
        Mockito.when(user.getFullName()).thenReturn("John Doe");
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        // Act
        String tokenTime = urlSecurity.createUrlTokenTime(user, time);

        // Assert
        assertNotNull(tokenTime);
        assertTrue(tokenTime.length() > 0); // Ensuring that a token is generated
    }

    @Test
    void isUrlTokenValid_ShouldReturnTrue_WhenTokenIsValid() throws NoSuchAlgorithmException {
        // Arrange
        Mockito.when(user.getId()).thenReturn(123L);
        Mockito.when(user.getFullName()).thenReturn("John Doe");
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String validToken = urlSecurity.createUrlTokenTime(user, time);

        // Act
        Boolean isValid = urlSecurity.isUrlTokenValid(validToken, time, user);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isUrlTokenValid_ShouldReturnFalse_WhenTokenIsInvalid() throws NoSuchAlgorithmException {
        // Arrange
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String invalidToken = "invalidToken";
        Mockito.when(user.getId()).thenReturn(123L);
        Mockito.when(user.getFullName()).thenReturn("John Doe");

        // Act
        Boolean isValid = urlSecurity.isUrlTokenValid(invalidToken, time, user);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isUrlTokenValid_ShouldReturnFalse_WhenTokenIsExpired() throws NoSuchAlgorithmException {
        // Arrange
        String time = LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")); // Expired token
        String validToken = urlSecurity.createUrlTokenTime(user, time);
        Mockito.when(user.getId()).thenReturn(123L);
        Mockito.when(user.getFullName()).thenReturn("John Doe");

        // Act
        Boolean isValid = urlSecurity.isUrlTokenValid(validToken, time, user);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isUrlTokenValid_ShouldReturnFalse_WhenTimeIsInvalid() throws NoSuchAlgorithmException {
        // Arrange
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String nextTime = LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String validToken = urlSecurity.createUrlTokenTime(user, time);
        Mockito.when(user.getId()).thenReturn(123L);
        Mockito.when(user.getFullName()).thenReturn("John Doe");

        // Act
        Boolean isValid = urlSecurity.isUrlTokenValid(validToken, nextTime, user);

        // Assert
        assertFalse(isValid);
    }
}
