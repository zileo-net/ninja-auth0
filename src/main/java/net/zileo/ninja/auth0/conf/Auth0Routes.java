package net.zileo.ninja.auth0.conf;

import com.google.inject.Inject;

import net.zileo.ninja.auth0.controllers.Auth0Controller;
import ninja.Router;
import ninja.utils.NinjaProperties;

public class Auth0Routes {

    @Inject
    protected NinjaProperties ninjaProperties;

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