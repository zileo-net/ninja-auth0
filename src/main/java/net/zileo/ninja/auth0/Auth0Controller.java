package net.zileo.ninja.auth0;

import java.io.UnsupportedEncodingException;

import javax.inject.Named;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;

import net.zileo.ninja.auth0.subject.Auth0TokenHandler;
import net.zileo.ninja.auth0.subject.Subject;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.ReverseRouter;
import ninja.exceptions.NinjaException;
import ninja.jpa.UnitOfWork;
import ninja.params.PathParam;
import ninja.session.Session;

/**
 * Ninja's controller offering routes that can handle Auth0 login and logout.
 * 
 * @author jlannoy
 */
public class Auth0Controller {

    public static final String SESSION_ACCESS_TOKEN = "access_token";

    public static final String SESSION_ID_TOKEN = "id_token";

    public static final String SESSION_TARGET_URL = "target_url";

    @Inject
    private ReverseRouter reverseRouter;

    @Inject
    @Named("auth0.loggedOut")
    private String loggedOutPage;

    @Inject
    Auth0TokenHandler<? extends Subject> tokenHandler;

    private AuthAPI auth;

    private Algorithm algorithm;

    @Inject
    public void init(@Named("auth0.domain") String domain,
                     @Named("auth0.clientId") String clientId,
                     @Named("auth0.clientSecret") String clientSecret)
            throws IllegalArgumentException, UnsupportedEncodingException {
        this.auth = new AuthAPI(domain, clientId, clientSecret);
        this.algorithm = Algorithm.HMAC256(clientSecret);
    }

    private String getCallbackUrl(Context context) {
        return reverseRouter.with(Auth0Controller::callback).absolute(context).build();
    }

    /**
     * Redirect user to the Auth0 login page of the configured Auth0 domain.
     */
    @UnitOfWork
    public Result login(Context context, Session session) {
        return Results.redirect(auth.authorizeUrl(getCallbackUrl(context)).withResponseType("code").withScope(tokenHandler.getScope()).build());
    }

    /**
     * Callback called by Auth0 once a user has been authenticated. The Auth0 code will be exchanged with an id token.
     */
    @UnitOfWork
    public Result callback(Context context, Session session) {

        if (context.getParameter("error") != null) {
            throw new NinjaException(Result.SC_401_UNAUTHORIZED, context.getParameter("error_description"));
        }

        if (context.getParameter("code") == null) {
            throw new NinjaException(Result.SC_401_UNAUTHORIZED, "No authorization code received.");
        }

        try {

            TokenHolder token = auth.exchangeCode(context.getParameter("code"), getCallbackUrl(context)).execute();
            session.put(SESSION_ACCESS_TOKEN, token.getAccessToken());
            session.put(SESSION_ID_TOKEN, token.getIdToken());
            session.setExpiryTime(token.getExpiresIn() * 1000);

            String targetUrl = session.remove(SESSION_TARGET_URL);
            return Results.redirect(targetUrl == null ? "/" : targetUrl);

        } catch (Auth0Exception e) {
            throw new NinjaException(Result.SC_500_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @UnitOfWork
    public Result logout(Context context, Session session) {
        String redirectUrl = reverseRouter.with(Auth0Controller::loggedOut).absolute(context).build();

        if (session.get(SESSION_ID_TOKEN) != null && session.get(SESSION_ACCESS_TOKEN) != null) {
            session.clear();
            return Results.redirect(auth.logoutUrl(redirectUrl, true).useFederated(true).build());

        } else {
            session.clear();
            return Results.redirect(redirectUrl);
        }

    }

    @UnitOfWork
    public Result loggedOut(Context context) {
        if (loggedOutPage != null) {
            return Results.redirect(loggedOutPage);
        } else {
            return Results.text().render("Logged Out");
        }
    }

    @UnitOfWork
    public Result simulate(Context context, Session session, @PathParam("value") String value) {
        session.put(SESSION_ID_TOKEN, tokenHandler.buildSimulatedJWT(value, algorithm));
        return Results.text().render("Signed In");
    }

}
