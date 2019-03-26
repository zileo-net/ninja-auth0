package net.zileo.ninja.auth0.handlers;

import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.interfaces.DecodedJWT;

import net.zileo.ninja.auth0.controllers.Auth0Controller;
import net.zileo.ninja.auth0.subject.Auth0;
import net.zileo.ninja.auth0.subject.Subject;
import ninja.Context;

/**
 * This is your base Subject / profile generator. Extends this class by specifying your User class (implementing the
 * Subject interface). Based on the decoded Json Web Token, grab the claims you need to build one instance of your
 * authenticated user. By using the {@link Auth0} annotation on your controllers methods, you'll be able to get it back.
 * 
 * @author jlannoy
 */
public abstract class Auth0TokenHandler<P extends Subject> {

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
     * @param jwt
     *            a JSON Web Token
     * @return a user id
     */
    public String getUserId(DecodedJWT jwt) {

        return jwt.getClaim(CLAIM_SUBJECT) != null ? jwt.getClaim(CLAIM_SUBJECT).asString() : null;

    }

    /**
     * Checks the ID Token, decodes it, then call
     * {@link Auth0TokenHandler#buildSubjectFromJWT(Context, DecodedJWT, String)}.
     * 
     * @param context
     *            Ninja's current context
     * @throws IllegalArgumentException
     *             if a mandatory data is missing
     * @return authenticated User
     */
    public final P buildSubject(Context context) throws IllegalArgumentException {

        return this.buildSubject(context, context.getSession().get(Auth0Controller.SESSION_ID_TOKEN));

    }

    /**
     * Checks the ID Token, decodes it, then call
     * {@link Auth0TokenHandler#buildSubjectFromJWT(Context, DecodedJWT, String)}.
     * 
     * @param context
     *            Ninja's current context
     * @param idToken
     *            Auth0 Id Token
     * @throws IllegalArgumentException
     *             if a mandatory data is missing
     * @return authenticated User
     */
    public final P buildSubject(Context context, String idToken) throws IllegalArgumentException {

        if (idToken == null) {
            throw new IllegalArgumentException("No Id Token provided");
        }

        DecodedJWT jwt = JWT.decode(idToken);
        if (jwt == null) {
            throw new IllegalArgumentException("Unable to decode provided Id Token");
        }

        String userId = getUserId(jwt);
        if (userId == null) {
            throw new IllegalArgumentException("No User Id in provided Id Token");
        }

        P subject = buildSubjectFromJWT(context, jwt, userId);
        if (subject == null) {
            throw new IllegalArgumentException("Unable to create Subject from provided Id Token");
        }

        return subject;

    }

    /**
     * Implement this method to build your Subject.
     * 
     * @param context
     *            Ninja's current context
     * @param jwt
     *            a JSON Web Token
     * @param userId
     *            user id (as a String)
     * @return authenticated User
     */
    public abstract P buildSubjectFromJWT(Context context, DecodedJWT jwt, String userId);

    /**
     * Creates a fake web token (for the simulated dev/test action).
     * 
     * @param context
     *            Ninja's current context
     * @param value
     *            a user id or email (depending on client choice)
     * @param additionalClaims
     *            additional claims of the simulated user
     * @return a JWT
     */
    public Builder buildSimulatedJWT(Context context, String value, Map<String, String> additionalClaims) {

        Builder builder = JWT.create().withClaim(Auth0TokenHandler.CLAIM_SUBJECT, value).withClaim(Auth0TokenHandler.CLAIM_SIMULATED, Boolean.TRUE);

        for (Map.Entry<String, String> entry : additionalClaims.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue());
        }

        return builder;

    }

}
