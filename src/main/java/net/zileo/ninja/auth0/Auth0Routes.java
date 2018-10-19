package net.zileo.ninja.auth0;

import com.google.inject.Inject;

import ninja.Router;
import ninja.application.ApplicationRoutes;
import ninja.utils.NinjaProperties;

public class Auth0Routes implements ApplicationRoutes {

    @Inject
    protected NinjaProperties ninjaProperties;

    /**
     * @see ninja.application.ApplicationRoutes#init(ninja.Router)
     */
    public void init(Router router) {
        router.GET().route("/auth0/login").with(Auth0Controller::login);
        router.GET().route("/auth0/callback").with(Auth0Controller::callback);
        router.GET().route("/auth0/logout").with(Auth0Controller::logout);
        router.GET().route("/auth0/out").with(Auth0Controller::loggedOut);

        if (!ninjaProperties.isProd()) {
            router.GET().route("/auth0/simulate/{value}").with(Auth0Controller::simulate);
        }
    }

}