package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import net.zileo.ninja.auth0.handlers.Auth0TokenHandler;
import net.zileo.ninja.auth0.subject.Subject;
import net.zileo.ninja.auth0.utils.Auth0SubjectTokenHandler;

@Singleton
public class Module extends AbstractModule {

    protected void configure() {

        bind(new TypeLiteral<Auth0TokenHandler<? extends Subject>>() {}).to(Auth0SubjectTokenHandler.class);

    }
}
