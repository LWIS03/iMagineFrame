package be.uantwerpen.fti.se.imagineframe_backend.security;

import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Component
public class UrlSecurity {
    @Value("${urlSignToken}")
    private String urlSignToken;

    private final int validityHours = 1;
    private final String dateTimeFormat = "yyyyMMddHHmm";

    public String createUrlToken(User user) throws NoSuchAlgorithmException {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateTimeFormat));
        return user.getId() + "?token=" + createUrlTokenTime(user, now) + "&time=" + now;
    }

    public String createUrlTokenTime(User user, String time) throws NoSuchAlgorithmException {
        String regex = "[^a-zA-Z0-9\\s]";
        String tokenString = user.getFullName() + time + urlSignToken;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String token = Base64.getEncoder().encodeToString(md.digest(tokenString.getBytes()));
        return token.replaceAll(regex, "");
    }

    public Boolean isUrlTokenValid(String token, String urlTime, User user) throws NoSuchAlgorithmException {
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateTimeFormat));
        LocalDateTime now = LocalDateTime.parse(nowStr, DateTimeFormatter.ofPattern(dateTimeFormat));
        LocalDateTime creationTime = LocalDateTime.parse(urlTime, DateTimeFormatter.ofPattern(dateTimeFormat));
        if (now.isAfter(creationTime.plusHours(validityHours))) {
            return false;
        }
        String testToken = createUrlTokenTime(user, urlTime);
        return testToken.equals(token);
    }
}
