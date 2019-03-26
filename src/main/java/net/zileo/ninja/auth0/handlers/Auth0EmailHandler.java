package net.zileo.ninja.auth0.handlers;

import java.util.Map;

import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.interfaces.DecodedJWT;

import net.zileo.ninja.auth0.subject.Subject;
import ninja.Context;

/**
 * Same as {@link Auth0TokenHandler} but with a little shortcut allowing to generate a Subject based on a given verified
 * email address.
 * 
 * @author jlannoy
 */
public abstract class Auth0EmailHandler<P extends Subject> extends Auth0TokenHandler<P> {

    public final static String CLAIM_EMAIL = "email";

    public final static String CLAIM_EMAIL_VERIFIED = "email_verified";

    /**
     * Defines the scope corresponding to the need claims for this handler.
     * 
     * @return "openid email"
     */
    @Override
    public String getScope() {
        return "openid email";
    }

    /**
     * Extracts the user email address from the Json Web Token.
     * 
     * @param jwt
     *            a JSON Web Token
     * @return an email address
     */
    public String getEmail(DecodedJWT jwt) {

        return jwt.getClaim(CLAIM_EMAIL) != null ? jwt.getClaim(CLAIM_EMAIL).asString() : null;

    }

    /**
     * Extracts the fact that the email address used by the user to register Auth0 has already been validated.
     * 
     * @param jwt
     *            a JSON Web Token
     * @return true if already verified
     */
    public boolean isVerifiedEmail(DecodedJWT jwt) {

        return jwt.getClaim(CLAIM_EMAIL_VERIFIED) != null && jwt.getClaim(CLAIM_EMAIL_VERIFIED).asBoolean();

    }

    /**
     * @see net.zileo.ninja.auth0.handlers.Auth0TokenHandler#buildSubjectFromJWT(ninja.Context,
     *      com.auth0.jwt.interfaces.DecodedJWT, java.lang.String)
     */
    @Override
    public P buildSubjectFromJWT(Context context, DecodedJWT jwt, String userId) {

        if (!isVerifiedEmail(jwt)) {
            throw new IllegalArgumentException("E-mail adress not (yet) verified");
        }

        return buildSubjectFromEmail(context, jwt, userId, getEmail(jwt));

    }

    /**
     * Implement this method to build your Subject from a verified e-mail address.
     * 
     * @param jwt
     *            a JSON Web Token
     * @param userId
     *            user id
     * @param email
     *            user email
     * @return authenticated User
     */
    public abstract P buildSubjectFromEmail(Context context, DecodedJWT jwt, String userId, String email);

    /**
     * Creates a fake web token (for the simulated dev/test action).
     * 
     * @param value
     *            a user id or email (depending on client choice)
     * @param additionalClaims
     *            additional claims of the simulated user
     * @return a JWT
     */
    @Override
    public Builder buildSimulatedJWT(Context context, String value, Map<String, String> additionalClaims) {
        return super.buildSimulatedJWT(context, value, additionalClaims).withClaim(CLAIM_EMAIL, value).withClaim(CLAIM_EMAIL_VERIFIED, Boolean.TRUE);
    }

}
