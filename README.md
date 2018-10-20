# Ninja Auth0 module

** NOT YET WORKING**

This library provides an integration strategy for [Auth0](https://auth0.com) authentication inside Ninja Framework project.

Follow this README to learn how to use it in your project, and quickly have an authentication system working. Note that all our classes mentioned here resides in packages `net.zileo.ninja.auth0` or `net.zileo.ninja.auth0.subject`.

## How to use it - Auth0 configuration



## How to use it - JAVA configuration

First, copy this dependency into your `pom.xml` file.

    <dependency>
        <groupId>net.zileo</groupId>
        <artifactId>ninja-auth0</artifactId>
        <version>1.0.0</version>
    </dependency>

Then instantiates the modules routes by calling adding this line to your `conf/Routes.java` file :

    @Inject
    private Auth0Routes auth0Routes;
    
    @Override
    public void init(Router router) {
        ...
        auth0Routes.init(router);
        ...
    }

Add our `CheckAuthenticatedFilter` on methods or controllers you want to protect, via annotation or route definition. For example :

    router.GET().route("/helloPrivate").filters(CheckAuthenticatedFilter.class).with(ExampleController::helloPrivate);

Finally, configure your Auth0 account inside Ninja's `application.conf` :

    auth0.domain = your.domain.auth0.com
    auth0.clientId = yourAuth0ClientId
    auth0.clientSecret = yourAuth0Client-Secret

## How to use it - JAVA Subject creation

If you followed the previous chapter, your routes are now protected. But you still have to bridge Auth0 authentication process with your user representative class. Replace in your routes definition the `CheckAuthenticatedFilter` by our `AuthenticateFilter`. This one will populate Ninja's context with an instance of your user.

Two steps are needed for this. First, make your user representative class implements our `Subject` interface. It doesn't require any method to implement, but it will allows Guice injection to work with the second needed step : provide your own an implementation of a `Auth0TokenHandler`,to create your own `Subject` class according to the Auth0 JSON Web Toolkit. If you only want to base your authentication model on the verified e-mail address of your users, You can also extend our `Auth0EmailHandler`. This handler provides a quick abstract e-mail based method.

Here is a very light example :

    package models;
    
    import java.util.Date;
    
    import com.auth0.jwt.interfaces.DecodedJWT;
    
    import net.zileo.ninja.auth0.subject.Auth0EmailHandler;
    import net.zileo.ninja.auth0.subject.Subject;
    
    public class User implements Subject {
    
        private final String email;
    
        private final Date createdAt;
    
        public User(String email) {
            this.email = email;
            this.createdAt = new Date();
        }
    
        public String getEmail() {
            return email;
        }
    
        public Date getCreatedAt() {
            return createdAt;
        }
    
        public static class UserAuth0TokenHandler extends Auth0EmailHandler<User> {
    
            @Override
            public User buildSubjectFromEmail(DecodedJWT jwt, String userId, String email) {
                return new User(email);
            }
    
        }
    
    }

## More details on how it works

Ninja Auth0 will combine with Auth0 authentication SaaS to provide your application a quick authentication management. It will get back a JSON Web Token from Auth0 and store it inside Ninja's cookie session. For each request, the presence of one JWT will be checked and according to your requirements a User or Subject will be popuated in your `Context`.

Ninja Auth0 module creates the following routes :
* `/auth0/login` : triggers the Auth0 login process
* `/auth0/callback` : callback when Auth0 has authenticated the user
* `/auth0/logout` : triggers a Auth0 log out request
* `/auth0/out` : 
* `/auth0/simulate/{value}` : only in test or dev, allows you to bypass the Auth0 login procedure.

When a user tries to reach one of your protected routes :
* Either the session contains a JWT, and the

## Global filters

## Populating an identified Subject

## Advanced configuration

---

Proudly provided by [Zileo.net](https://zileo.net)
