package net.zileo.ninja.auth0.utils;

import java.util.Map;

import com.google.common.collect.Maps;

import net.zileo.ninja.auth0.subject.Subject;

/**
 * Default Subject implementation.
 * 
 * @author jlannoy
 */
public class Auth0Subject implements Subject {

    private final Map<String, Object> claims = Maps.newHashMap();

    private final String id;

    private final String email;

    public Auth0Subject(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void put(String claim, Object value) {
        claims.put(claim, value);
    }

    public boolean is(String claim) {
        if (claims.containsKey(claim)) {
            if (claims.get(claim) instanceof Boolean) {
                return (Boolean) claims.get(claim);
            }
            if (claims.get(claim) instanceof String) {
                return Boolean.valueOf((String) claims.get(claim));
            }
        }
        return false;
    }

    public Object get(String claim) {
        return claims.get(claim);
    }
}
