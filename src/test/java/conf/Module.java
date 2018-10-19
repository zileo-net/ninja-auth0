package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import models.User.UserAuth0TokenHandler;
import net.zileo.ninja.auth0.subject.Auth0TokenHandler;
import net.zileo.ninja.auth0.subject.Subject;

@Singleton
public class Module extends AbstractModule {

    protected void configure() {

        bind(new TypeLiteral<Auth0TokenHandler<? extends Subject>>() {}).to(UserAuth0TokenHandler.class);

    }
}
