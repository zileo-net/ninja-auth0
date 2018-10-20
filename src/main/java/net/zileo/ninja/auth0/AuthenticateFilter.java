package net.zileo.ninja.auth0;

import com.google.inject.Inject;

import net.zileo.ninja.auth0.subject.Auth0TokenHandler;
import net.zileo.ninja.auth0.subject.Subject;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.ReverseRouter;

/**
 * Filter allowing to check if the current user has been authenticated.
 * 
 * @author jlannoy
 */
public class AuthenticateFilter implements Filter {

    public final static String SUBJECT_CTX_KEY = "contextSubject";

    @Inject
    private ReverseRouter reverseRouter;

    @Inject
    private Auth0TokenHandler<? extends Subject> tokenHandler;

    /**
     * If there is no JSON ID Token in current session, then the current requested path is saved and the response redirects to the Auth0 controller login route.
     * 
     * @see ninja.Filter#filter(ninja.FilterChain, ninja.Context)
     */
    @Override
    public Result filter(FilterChain filterChain, Context context) {

        if (context.getSession().get(Auth0Controller.SESSION_ID_TOKEN) != null) {

            try {

                Subject subject = tokenHandler.buildSubject(context);
                context.setAttribute(SUBJECT_CTX_KEY, subject);
                return filterChain.next(context);

            } catch (IllegalArgumentException e) {

                context.getFlashScope().error(e.getMessage());

            }
        }

        // No Id Token or Subject = redirect to login page
        context.getSession().put(Auth0Controller.SESSION_TARGET_URL, context.getRequestPath());
        return Results.redirect(reverseRouter.with(Auth0Controller::login).build());

    }

    /**
     * Retrieves the authenticated subject stored in current Ninja's context. 
     * 
     * @param context current Ninja's context
     * @param clazz Subject implementation class
     * @return current Subject
     */
    public static <T extends Subject> T get(Context context, Class<T> clazz) {
        return context.getAttribute(SUBJECT_CTX_KEY, clazz);
    }

}