package net.zileo.ninja.auth0.subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import net.zileo.ninja.auth0.Auth0Controller;
import ninja.Context;

/**
 * This is your base Subject / profile generator. Extends this class by specifying your User class (implementing the Subject interface). Based on the decoded Json Web Token, grab the claims you need
 * to build one instance of your authenticated user. By using the {@link Auth0} annotation on your controllers methods, you'll be able to get it back.
 * 
 * @author jlannoy
 */
public abstract class Auth0TokenHandler<P extends Subject> {

    private final static Logger logger = LoggerFactory.getLogger(Auth0TokenHandler.class);

    public final static String CLAIM_SUBJECT = "sub";

    public final static String CLAIM_SIMULATED = "__simulated";

    /**
     * Defines the scope corresponding to the need claims for this handler.
     * 
     * @return "openid"
     */
    public String getScope() {
        return "openid";
    }

    /**
     * Extracts the user id from the Json Web Token.
     * 
     * @return a user id
     */
    public static String getUserId(DecodedJWT jwt) {
        return jwt.getClaim(CLAIM_SUBJECT) != null ? jwt.getClaim(CLAIM_SUBJECT).asString() : null;
    }

    /**
     * Checks the ID Token, decodes it, then call {@link Auth0TokenHandler#buildSubjectFromJWT(DecodedJWT)}.
     * 
     * @return authenticated User
     */
    public P buildSubject(Context context) {
        String idToken = context.getSession().get(Auth0Controller.SESSION_ID_TOKEN);
        if (idToken == null) {
            logger.warn("Trying to generate profile while no ID TOKEN in current session.");
            return null;
        }

        DecodedJWT jwt = JWT.decode(idToken);
        if (jwt == null) {
            logger.warn("Unable to decode current ID TOKEN.");
            return null;
        }

        return buildSubjectFromJWT(jwt, getUserId(jwt));
    }

    /**
     * Implement this method to build your Subject.
     * 
     * @return authenticated User
     */
    public abstract P buildSubjectFromJWT(DecodedJWT jwt, String userId);

    /**
     * Creates a fake web token (for the simulated dev/test action).
     * 
     * @return a JWT
     */
    public String buildSimulatedJWT(String value, Algorithm algorithm) {
        return JWT.create().withClaim(Auth0TokenHandler.CLAIM_SUBJECT, value).withClaim(Auth0TokenHandler.CLAIM_SIMULATED, Boolean.TRUE).sign(algorithm);
    }

}
