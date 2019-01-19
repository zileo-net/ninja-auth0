package net.zileo.ninja.auth0.controllers;

import java.io.UnsupportedEncodingException;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import net.zileo.ninja.auth0.handlers.Auth0TokenHandler;
import net.zileo.ninja.auth0.subject.Subject;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.ReverseRouter;
import ninja.exceptions.ForbiddenRequestException;
import ninja.exceptions.InternalServerErrorException;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.session.Session;
import ninja.utils.NinjaProperties;

/**
 * Ninja's controller offering routes that can handle Auth0 login and logout.
 * 
 * @author jlannoy
 */
public class Auth0Controller {

    private final static Logger logger = LoggerFactory.getLogger(Auth0Controller.class);

    public static final String SESSION_ID_TOKEN = "id_token";

    public static final String SESSION_TARGET_URL = "target_url";

    @Inject
    protected ReverseRouter reverseRouter;

    private boolean forceHttps;

    private String loggedOutPage;

    @Inject
    private Auth0TokenHandler<? extends Subject> tokenHandler;

    private AuthAPI auth;

    private Algorithm algorithm;

    @Inject
    public void init(NinjaProperties properties,
                     @Named("auth0.domain") String domain,
                     @Named("auth0.clientId") String clientId,
                     @Named("auth0.clientSecret") String clientSecret)
            throws IllegalArgumentException, UnsupportedEncodingException {

        this.auth = new AuthAPI(domain, clientId, clientSecret);
        this.algorithm = Algorithm.HMAC256(clientSecret);

        this.loggedOutPage = properties.getWithDefault("auth0.loggedOut", "/");
        this.forceHttps = properties.getBooleanWithDefault("auth0.forceHttps", false);

    }

    /**
     * Get configured Auth0 callback route.
     * 
     * @param context
     *            current Ninja's context
     * @return callbak path
     */
    protected String getCallbackUrl(Context context) {

        return reverseRouter.with(Auth0Controller::callback)
                .absolute(forceHttps ? "https" : context.getScheme(), context.getHostname()).build();

    }

    /**
     * Get configured Auth0 logged out route.
     * 
     * @param context
     *            current Ninja's context
     * @return callbak path
     */
    protected String getLoggedOutUrl(Context context) {

        return reverseRouter.with(Auth0Controller::loggedOut)
                .absolute(forceHttps ? "https" : context.getScheme(), context.getHostname()).build();

    }

    /**
     * Redirect user to the Auth0 login page of the configured Auth0 domain.
     * 
     * @param context
     *            current Ninja's context
     * @param session
     *            current Ninja's session
     * @return a request Result
     */
    public Result login(Context context, Session session) {
        return Results.redirect(auth.authorizeUrl(getCallbackUrl(context)).withResponseType("code").withScope(tokenHandler.getScope()).build());
    }

    /**
     * Callback called by Auth0 once a user has been authenticated. The Auth0 code will be exchanged with an id token.
     * 
     * @param context
     *            current Ninja's context
     * @param session
     *            current Ninja's session
     * @return a request Result
     */
    public Result callback(Context context, Session session) {

        if (context.getParameter("error") != null) {
            throw new ForbiddenRequestException(context.getParameter("error_description"));
        }

        if (context.getParameter("code") == null) {
            throw new ForbiddenRequestException("No authorization code received");
        }

        try {

            TokenHolder token = auth.exchangeCode(context.getParameter("code"), getCallbackUrl(context)).execute();

            // Check that we are able to provision one Subject from the received id token
            try {

                tokenHandler.buildSubject(token.getIdToken());

            } catch (IllegalArgumentException e) {

                throw new ForbiddenRequestException(e.getMessage(), e);

            }

            session.put(SESSION_ID_TOKEN, token.getIdToken());
            session.setExpiryTime(token.getExpiresIn() * 1000);

            String targetUrl = session.remove(SESSION_TARGET_URL);
            logger.debug("ID token set, redirecting to requested path ({})", targetUrl);
            return Results.redirect(context.getContextPath() + (targetUrl == null ? "" : targetUrl));

        } catch (Auth0Exception e) {

            throw new InternalServerErrorException(e.getMessage());

        }

    }

    /**
     * Redirects to the Auth0 log out URL.
     * 
     * @param context
     *            current Ninja's context
     * @param session
     *            current Ninja's session
     * @return a request Result
     */
    public Result logout(Context context, Session session) {

        if (session.get(SESSION_ID_TOKEN) != null) {
            session.clear();
            return Results.redirect(auth.logoutUrl(getLoggedOutUrl(context), true).useFederated(true).build());

        } else {
            session.clear();
            return Results.redirect(getLoggedOutUrl(context));
        }

    }

    /**
     * Callback page once Auth0 as logged out current user.
     * 
     * @param context
     *            current Ninja's context
     * @return a request Result
     */
    public Result loggedOut(Context context) {

        if (loggedOutPage != null) {

            return Results.redirect(context.getContextPath() + loggedOutPage);

        } else {

            return Results.text().render("Logged Out");

        }

    }

    /**
     * Simulates a user authentication without calling Auth0. Only available in dev or test mode.
     * 
     * @param context
     *            current Ninja's context
     * @param session
     *            current Ninja's session
     * @param value
     *            a user id or email to simulate
     * @return a request Result
     */
    public Result simulate(Context context, Session session, @PathParam("value") String value) {

        session.put(SESSION_ID_TOKEN, tokenHandler.buildSimulatedJWT(value, Maps.newHashMap()).sign(algorithm));

        String targetUrl = session.remove(SESSION_TARGET_URL);
        return Results.redirect(context.getContextPath() + (targetUrl == null ? "" : targetUrl));

    }

    /**
     * Simulates a user authentication without calling Auth0. Only available in dev or test mode.
     * 
     * @return a request Result
     */
    public Result simulateLogin() {

        return Results.html();

    }

    /**
     * Simulates a user authentication without calling Auth0. Only available in dev or test mode.
     * 
     * @param context
     *            current Ninja's context
     * @param session
     *            current Ninja's session
     * @param value
     *            a user id or email to simulate
     * @return a request Result
     */
    public Result doSimulate(Context context, Session session, @Param("value") String value) {

        return simulate(context, session, value);

    }
}
