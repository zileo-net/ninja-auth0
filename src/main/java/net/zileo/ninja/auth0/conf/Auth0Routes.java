package net.zileo.ninja.auth0.conf;

import com.google.inject.Inject;

import net.zileo.ninja.auth0.controllers.Auth0Controller;
import ninja.Filter;
import ninja.Router;
import ninja.utils.NinjaProperties;

public class Auth0Routes {

    @Inject
    private NinjaProperties ninjaProperties;

    /**
     * @param router
     *            Ninja's router instance
     * @param filters
     *            global filters
     */
    @SuppressWarnings("unchecked")
    public void init(Router router, Class<? extends Filter>... filters) {

        router.GET().route("/auth0/login").globalFilters().filters(filters).with(Auth0Controller::login);
        router.GET().route("/auth0/callback").globalFilters().filters(filters).with(Auth0Controller::callback);
        router.GET().route("/auth0/logout").globalFilters().filters(filters).with(Auth0Controller::logout);
        router.GET().route("/auth0/out").globalFilters().filters(filters).with(Auth0Controller::loggedOut);

        if (!ninjaProperties.isProd()) {
            router.GET().route("/auth0/simulate").globalFilters().filters(filters).with(Auth0Controller::simulateLogin);
            router.GET().route("/auth0/simulate/{value}").globalFilters().filters(filters).with(Auth0Controller::simulate);
            router.POST().route("/auth0/doSimulate").globalFilters().filters(filters).with(Auth0Controller::doSimulate);
        }

    }

}