package net.zileo.ninja.auth0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Maps;

import net.zileo.ninja.auth0.utils.Auth0Subject;
import net.zileo.ninja.auth0.utils.Auth0SubjectTokenHandler;

public class Auth0SubjectTokenHandlerTest {

    private final static Logger logger = LoggerFactory.getLogger(Auth0SubjectTokenHandlerTest.class);

    private final static String DEFAULT_CLIENT_SECRET = "default-client-secret";

    private final static String USER_EMAIL = "test@test.com";

    private Auth0SubjectTokenHandler handler;

    private Algorithm algorithm;

    @Before
    public void init() throws IllegalArgumentException, UnsupportedEncodingException {
        handler = new Auth0SubjectTokenHandler();
        algorithm = Algorithm.HMAC256(DEFAULT_CLIENT_SECRET);
    }

    @Test
    public void testGenerateToken() {
        String idToken = handler.buildSimulatedJWT(USER_EMAIL, Maps.newHashMap()).sign(algorithm);
        assertNotNull(idToken);
        logger.info("JWT = {}", idToken);

        DecodedJWT jwt = JWT.decode(idToken);
        assertNotNull(idToken);

        String email = handler.getEmail(jwt);
        assertNotNull(email);
        assertEquals(USER_EMAIL, email);
        
        String userId = handler.getUserId(jwt);
        assertNotNull(userId);
        assertEquals(USER_EMAIL, userId);
        
        Auth0Subject user = handler.buildSubject(idToken);
        assertNotNull(user);
        assertEquals(USER_EMAIL, user.getEmail());
    }

}
