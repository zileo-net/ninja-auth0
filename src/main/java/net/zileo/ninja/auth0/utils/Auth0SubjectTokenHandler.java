package net.zileo.ninja.auth0.utils;

import com.auth0.jwt.interfaces.DecodedJWT;

import net.zileo.ninja.auth0.handlers.Auth0EmailHandler;

public class Auth0SubjectTokenHandler extends Auth0EmailHandler<Auth0Subject> {

    @Override
    public Auth0Subject buildSubjectFromEmail(DecodedJWT jwt, String userId, String email) {
        return new Auth0Subject(email);
    }

}