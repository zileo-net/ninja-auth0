package models;

import java.util.Date;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.interfaces.DecodedJWT;

import net.zileo.ninja.auth0.Auth0Controller;
import net.zileo.ninja.auth0.subject.Auth0EmailHandler;
import net.zileo.ninja.auth0.subject.Subject;

/**
 * Dummy model.
 * 
 * @author jlannoy
 */
public class User implements Subject {

    private final String email;

    private final Date createdAt;

    public User(String email) {
        this.email = email;
        this.createdAt = new Date();
    }

    public String getEmail() {
        return email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public static class UserAuth0TokenHandler extends Auth0EmailHandler<User> {

        private final static Logger logger = LoggerFactory.getLogger(Auth0Controller.class);

        @Override
        public User buildSubjectFromEmail(DecodedJWT jwt, String userId, String email) {
            logger.info("Token = {}", jwt.getClaims());
            
            Assert.assertNotNull(userId);
            Assert.assertNotNull(email);
            
            return new User(email);
        }

    }

}
