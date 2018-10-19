package net.zileo.ninja.auth0;

import static org.fluentlenium.core.filter.FilterConstructor.withClass;
import static org.fluentlenium.core.filter.FilterConstructor.withName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.fluentlenium.configuration.WebDrivers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import models.User;
import ninja.NinjaFluentLeniumTest;
import ninja.utils.NinjaMode;
import ninja.utils.NinjaTestServer;

public class Auth0ControllerFluentLeniumTest extends NinjaFluentLeniumTest {

    public static final String AUTH0_USER = "auth0.test@zileo.net";
    
    public static final String AUTH0_ADMIN = "auth0.admin@zileo.net";

    public static final String AUTH0_NOT_VERIFIED = "auth0.not_verified@zileo.net";

    public static final String AUTH0_SIMULATED = "auth0.simulated@zileo.net";

    public static final String AUTH0_PASSWORD = "Auth0test";

    @Before
    @Override
    public void startupServer() {
        ninjaTestServer = new NinjaTestServer(NinjaMode.test, 8080);
    }

    @Override
    public String getWebDriver() {
        return "safari";
    }

    @Override
    public TriggerMode getScreenshotMode() {
        return TriggerMode.AUTOMATIC_ON_FAIL;
    }

    @Override
    public String getScreenshotPath() {
        return "./target/screenshots/";
    }

    @Override
    public WebDriver newWebDriver() {
        WebDriver webDriver = WebDrivers.INSTANCE.newWebDriver(getWebDriver(), getCapabilities(), this);
        if (Boolean.TRUE.equals(getEventsEnabled())) {
            webDriver = new EventFiringWebDriver(webDriver);
        }
        return webDriver;

    }

    @Test
    public void testPublicPages() {
        goTo(getBaseUrl() + "/helloPublic");
        assertTrue(window().title().contains("Test"));
        assertTrue($("h1").first().text().contains("Hello Public!"));

        goTo(getBaseUrl() + "/helloPublic.json");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloPublic.json"));
    }

    @Test
    public void testPrivatePages() {
        goTo(getBaseUrl() + "/helloPrivate");

        // Should have been redirected to Auth0 login page
        assertFalse(window().title().contains("Test"));
        assertTrue(window().title().contains("Sign In with Auth0"));
        login(AUTH0_USER);

        // Should have been redirected to the requested page
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertTrue($("h1").first().text().contains("Hello Private!"));

        goTo(getBaseUrl() + "/helloPrivate.json");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloPrivate.json"));

        goTo(getBaseUrl() + "/helloSubject");
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertEquals("Hello " + AUTH0_USER + "!", el("h1").text());
        assertEquals(User.class.getName(), el("h2").text());

        goTo(getBaseUrl() + "/helloSubject.json");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloSubject.json"));

        logout();
    }
    
    @Test
    public void testNotVerified() {
        goTo(getBaseUrl() + "/helloPrivate");

        // Should have been redirected to Auth0 login page
        assertFalse(window().title().contains("Test"));
        assertTrue(window().title().contains("Sign In with Auth0"));
        login(AUTH0_NOT_VERIFIED);

        // Should have been redirected to the requested page
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertTrue($("h1").first().text().contains("Hello Private!"));

        goTo(getBaseUrl() + "/helloPrivate.json");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloPrivate.json"));

        goTo(getBaseUrl() + "/helloSubject");
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertEquals("Hello " + AUTH0_USER + "!", el("h1").text());
        assertEquals(User.class.getName(), el("h2").text());

        goTo(getBaseUrl() + "/helloSubject.json");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloSubject.json"));

        logout();
    }

    @Test
    public void testSimulate() {
        goTo(getBaseUrl() + "/auth0/simulate/" + AUTH0_SIMULATED);

        goTo(getBaseUrl() + "/helloSubject");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloSubject"));
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertEquals("Hello " + AUTH0_SIMULATED + "!", el("h1").text());
        assertEquals(User.class.getName(), el("h2").text());

        logout();
    }
    
    @Test
    public void testAdminRole() {
        goTo(getBaseUrl() + "/helloPrivate");

        // Should have been redirected to Auth0 login page
        assertFalse(window().title().contains("Test"));
        assertTrue(window().title().contains("Sign In with Auth0"));
        login(AUTH0_ADMIN);

        // Should have been redirected to the requested page
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertTrue($("h1").first().text().contains("Hello Private!"));

        goTo(getBaseUrl() + "/helloPrivate.json");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloPrivate.json"));

        goTo(getBaseUrl() + "/helloSubject");
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertEquals("Hello " + AUTH0_ADMIN + "!", el("h1").text());
        assertEquals(User.class.getName(), el("h2").text());

        goTo(getBaseUrl() + "/helloSubject.json");
        assertTrue("Should have not been redirected to " + url(), url().contains("helloSubject.json"));

        logout();
    }

    private void login(String user) {
        await().atMost(30, TimeUnit.SECONDS).until(el("input.auth0-lock-input")).displayed();
        assertTrue($("input", withClass("auth0-lock-input")).present());
        assertTrue($("input", withName("email")).present());
        $("input", withName("email")).fill().with(user);
        assertTrue($("input", withName("password")).present());
        $("input", withName("password")).fill().with(AUTH0_PASSWORD);
        $("button", withClass("auth0-lock-submit")).click();
    }

    private void logout() {
        goTo(getBaseUrl() + "/auth0/logout");
        await().atMost(10, TimeUnit.SECONDS).until(el(".zileo")).present();
        assertTrue(window().title().contains("Test"));
        assertEquals("Index", el("h1").text());
    }
}