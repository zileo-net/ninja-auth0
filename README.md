# Ninja Auth0 module

This library provides an integration strategy for [Auth0](https://auth0.com) authentication inside Ninja Framework project.

This module refer to the [Auth0 WebApp Single Sign On guide](https://auth0.com/docs/architecture-scenarios/web-app-sso), please read it to understand what it can do for your application. Then follow this README to quickly have an authentication system working. Note that all our classes mentioned here resides in sub-packages of `net.zileo.ninja.auth0`.

---

## Quick start guide

### Auth0 configuration

First, obviously, creates an account at Auth0. Thanks to their [free pricing plan](https://auth0.com/pricing) you'll be able to test and use this module for free on little applications. Then in your Dashboard create a new **Regular Web Application**. Don't follow the Java Quick Start guide, even if well written, you won't need those steps here.

For the test purposes :
* Allows theses Callback URLs: `http://localhost:8080/auth0/callback, https://localhost:8080/auth0/callback`
* Allows theses Logout URLs: `http://localhost:8080/auth0/out, https://localhost:8080/auth0/out`

And... That's all. The default configuration will be enough to test the module, check the **Connections** settings of your Auth0 application to test identity providers like Facebook or Google.

### JAVA configuration

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

Add our `AuthenticateFilter` on methods or controllers you want to protect, via annotation or route definition. For example :

    router.GET().route("/helloPrivate").filters(AuthenticateFilter.class).with(ExampleController::helloPrivate);

Finally, configure your Auth0 application settings inside Ninja's `application.conf` :

    auth0.domain = your.domain.auth0.com
    auth0.clientId = yourAuth0ClientId
    auth0.clientSecret = yourAuth0Client-Secret

### JAVA subject injection

Your routes are now protected. But you still have to bridge Auth0 authentication process with a user representative class. We provide a default way of working, with a simple `Auth0Subject` class containing some information about the authenticated user. To use it, simply configure Guice in your `conf/Module.java` to use the corresponding token handler :

    protected void configure() {

        bind(new TypeLiteral<Auth0TokenHandler<? extends Subject>>() {}).to(Auth0SubjectTokenHandler.class);

    }

Now you'll be able to received a `Auth0Subject` instance in a controller's method by adding `@Auth0 Auth0Subject subject` as a parameter.

---

## Way of working details

Ninja Auth0 will combine with Auth0 authentication SaaS to provide your application a quick authentication management. It will get back a JSON Web Token from Auth0 and store it inside Ninja's cookie session. For each request, the presence of one JWT will be checked and according to your requirements a User or Subject will be popuated in your `Context`.

Ninja Auth0 module creates the following routes :
* `/auth0/login` : triggers the Auth0 login process
* `/auth0/callback` : callback when Auth0 has authenticated the user
* `/auth0/logout` : triggers a Auth0 log out request
* `/auth0/out` : default end route after logging out
* `/auth0/simulate/{value}` : only in test or dev, allows you to bypass the Auth0 login procedure.

When a user tries to reach one of your protected routes :
* Either the session contains a JWT, and then it's decoded (see below)
* Or the user is redirected to the Auth0's configured login page

Once the user authenticates itself, Auth0 will call back this module in your application providing a JSON Web Token. The module tries to decode that Token and creates a corresponding Subject. `Subject` is an interface that should represents your user data ; it can be a plain Java object if Auth0's JWT provides you enough information, or more commonly it can be a DTO enriched by data from your database. If a Subject have been successfully created, then the JWT is stored as a cookie session (see [Ninja's basic concepts](http://www.ninjaframework.org/documentation/basic_concepts/sessions.html)). **Note that this cookie's expiry time is set to the JWT expiry time, which means you can (must) control your session's life time only via your Auth0's configuration.**

---

## Advanced usage

### In-app user profiles

Depending on your needs, you'll want to have authenticated users be related to some model class in your application. Two steps are needed for this. First, make your user representative class implements our `Subject` interface. It doesn't require any method to implement, but it will allows Guice injection to work with the second needed step (see below). Here is a an example of what could be your user entity class :

    package models;
    
    import java.util.Date;
    import java.util.UUID;
    
    import javax.persistence.Column;
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;
    
    import org.hibernate.annotations.GenericGenerator;
    
    import net.zileo.ninja.auth0.subject.Subject;
    
    @Entity
    public class User implements Subject {
    
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(updatable = false, nullable = false)
        public UUID id;
        
        public String email;
    
        public Date createdAt;
        
        public String auth0Id;
    
    } 

Then, you'll need to provide your own an implementation of a `Auth0TokenHandler`, a class that will create your own `User` according to the Auth0 JSON Web Toolkit. If you only want to base your authentication model on the verified e-mail address of your users, you can also extend our `Auth0EmailHandler`, that provides a quick abstract e-mail based method. Here is a simplified example, based on previous entity class :
    
    import javax.persistence.EntityManager;
    import javax.persistence.NoResultException;
    
    import com.auth0.jwt.interfaces.DecodedJWT;
    import com.google.inject.Inject;
    import com.google.inject.Provider;
    import com.google.inject.persist.Transactional;
    
    import models.User;
    import net.zileo.ninja.auth0.handlers.Auth0EmailHandler;
    
    public class UserAuth0TokenHandler extends Auth0EmailHandler<User> {
    
        @Inject
        private Provider<EntityManager> emProvider;
    
        @Transactional
        @Override
        public User buildSubjectFromEmail(DecodedJWT jwt, String userId, String email) {
    
            EntityManager em = emProvider.get();
    
            try {
    
                return emProvider.get().createQuery("select u from User u where email = ?1", User.class).setParameter(1, email).getSingleResult();
    
            } catch (NoResultException e) {
    
                User user = new User();
                user.email = email;
                user.auth0Id = userId;
                emProvider.get().persist(user);
                return user;
    
            }
        }
    
    }
    
Important note : this example allows user registration, as it will create a user in your database. According to this, note the `@Transactional` annotation used.

Last thing to do, configure Guice in your `conf/Module.java` to recognized your token handler :

    protected void configure() {

        bind(new TypeLiteral<Auth0TokenHandler<? extends Subject>>() {}).to(UserAuth0TokenHandler.class);

    }

Now you'll be able to received a `User` instance in a controller's method by adding `@Auth0 User user` as a parameter. You can also retrieve this instance by calling `AuthenticateFilter.get(context, User.class);` ; useful if you want to access it in an other filter (for permissions check for example).

### Filters & Global filters

To protect your routes, this module provides two filters : 
* `AuthenticateFilter` : Will check if user is authenticated and then use a token handler to populate Ninja's context.
* `CheckAuthenticatedFilter` : Will only check if your user is authenticated. Useful if your token handler use for example a database connection but you don't need a Subject instance.

### More configuration

You can also configure these settings inside Ninja's `application.conf` :

    auth0.loggedOut = Path to redirect to once logged out


---

Proudly provided by [Zileo.net](https://zileo.net)
