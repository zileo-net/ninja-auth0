package net.zileo.ninja.auth0.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import net.zileo.ninja.auth0.controllers.Auth0Controller;
import net.zileo.ninja.auth0.handlers.Auth0TokenHandler;
import net.zileo.ninja.auth0.subject.Subject;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.ReverseRouter;
import ninja.exceptions.ForbiddenRequestException;
import ninja.utils.NinjaProperties;

/**
 * Filter allowing to check if the current user has been authenticated.
 * 
 * @author jlannoy
 */
public class AuthenticateFilter implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticateFilter.class);

    public final static String SUBJECT_CTX_KEY = "contextSubject";

    @Inject
    private ReverseRouter reverseRouter;

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private Auth0TokenHandler<? extends Subject> tokenHandler;

    /**
     * If there is no JSON ID Token in current session, then the current requested path is saved and the response
     * redirects to the Auth0 controller login route.
     * 
     * @see ninja.Filter#filter(ninja.FilterChain, ninja.Context)
     */
    @Override
    public Result filter(FilterChain filterChain, Context context) {

        if (context.getSession().get(Auth0Controller.SESSION_ID_TOKEN) != null) {

            try {

                logger.debug("ID token found, creating related subject");
                
                Subject subject = tokenHandler.buildSubject(context);
                context.setAttribute(SUBJECT_CTX_KEY, subject);
                return filterChain.next(context);

            } catch (IllegalArgumentException e) {

                logger.warn(e.getMessage());
                throw new ForbiddenRequestException(e.getMessage(), e);

            }

        } else {

            logger.debug("ID token not found, saving requested path ({}) and going to login page", context.getRequestPath());
            
            // No Id Token or Subject = redirect to login page
            context.getSession().put(Auth0Controller.SESSION_TARGET_URL, context.getRequestPath());
            if (ninjaProperties.isProd()) {
                return Results.redirect(reverseRouter.with(Auth0Controller::login).build());
            } else {
                return Results.redirect(reverseRouter.with(Auth0Controller::simulateLogin).build());
            }

        }

    }

    /**
     * Retrieves the authenticated subject stored in current Ninja's context.
     * 
     * @param context
     *            current Ninja's context
     * @param clazz
     *            Subject implementation class
     * @return current Subject
     */
    public static <T extends Subject> T get(Context context, Class<T> clazz) {
        return context.getAttribute(SUBJECT_CTX_KEY, clazz);
    }

}