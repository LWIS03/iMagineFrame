package be.uantwerpen.fti.se.imagineframe_backend.security;

import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JWTUtil {
    @Value("${jwt_secret}")
    private String secret;

    /**
     * Creates a JWT (JSON Web Token) which holds information about the currently authenticated user.
     * This token will be encoded with information about the users privileges (groups) and identification information (unique email).
     *
     * @param user The user for which the token will be created
     * @return The JWT token as a string
     */
    public String generateToken(User user) throws IllegalArgumentException, JWTCreationException {
        Set<String> privileges = user.getGroups().stream()
                .map(Group::getPrivileges).flatMap(Set::stream).map(Privilege::getName).collect(Collectors.toSet());

        int expirationTime = 1000 * 60 * 60 * 24;

        return JWT.create()
                .withSubject("User Details")
                .withClaim("id", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("privileges", new ArrayList<>(privileges))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime)) // expires in 24 hours
                .withIssuer("UA_FTI_SE_IMAGINEFRAME")
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * Verifies a given JWT token. If the token is verified, the id of the user will be returned.
     *
     * @param token The token to be verified
     * @return The id of the verified user
     */
    public String validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer("UA_FTI_SE_IMAGINEFRAME")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return String.valueOf(jwt.getClaim("id"));
    }
}
