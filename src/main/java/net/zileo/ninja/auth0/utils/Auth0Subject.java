package net.zileo.ninja.auth0.utils;

import java.util.Date;

import net.zileo.ninja.auth0.subject.Subject;

/**
 * Default Subject implementation.
 * 
 * @author jlannoy
 */
public class Auth0Subject implements Subject {

    private final String email;

    private final Date createdAt;

    public Auth0Subject(String email) {
        this.email = email;
        this.createdAt = new Date();
    }

    public String getEmail() {
        return email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

}
