package net.zileo.ninja.auth0.filters;

import com.google.inject.Inject;

import net.zileo.ninja.auth0.controllers.Auth0Controller;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.ReverseRouter;
import ninja.utils.NinjaProperties;

/**
 * Filter allowing to check if the current user has been authenticated.
 * 
 * @author jlannoy
 */
public class CheckAuthenticatedFilter implements Filter {

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private ReverseRouter reverseRouter;

    /**
     * If there is no JSON ID Token in current session, then the current requested path is saved and the response
     * redirects to the Auth0 controller login route.
     * 
     * @see ninja.Filter#filter(ninja.FilterChain, ninja.Context)
     */
    @Override
    public Result filter(FilterChain filterChain, Context context) {
        if (context.getSession() == null || context.getSession().get(Auth0Controller.SESSION_ID_TOKEN) == null) {

            context.getSession().put(Auth0Controller.SESSION_TARGET_URL, context.getRequestPath());
            if (ninjaProperties.isProd()) {
                return Results.redirect(reverseRouter.with(Auth0Controller::login).build());
            } else {
                return Results.redirect(reverseRouter.with(Auth0Controller::simulateLogin).build());
            }

        } else {

            return filterChain.next(context);

        }
    }

}