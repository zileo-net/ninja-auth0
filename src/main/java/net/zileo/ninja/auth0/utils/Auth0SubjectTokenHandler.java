package net.zileo.ninja.auth0.utils;

import javax.inject.Named;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;

import net.zileo.ninja.auth0.handlers.Auth0EmailHandler;
import ninja.Context;

public class Auth0SubjectTokenHandler extends Auth0EmailHandler<Auth0Subject> {

    @Inject(optional = true)
    @Named("auth0.claimsNamespace")
    private String claimsNamespace;

    @Override
    public Auth0Subject buildSubjectFromEmail(Context context, DecodedJWT jwt, String userId, String email) {
        Auth0Subject subject = new Auth0Subject(userId, email);

        if (claimsNamespace != null) {
            autoExtractClaims(jwt, subject);
        }

        return subject;
    }

    protected void autoExtractClaims(DecodedJWT jwt, Auth0Subject subject) {

        for (String key : jwt.getClaims().keySet()) {

            if (key.startsWith(claimsNamespace)) {
                Claim claim = jwt.getClaim(key);

                if (claim.asBoolean() != null) {
                    subject.put(key.substring(claimsNamespace.length()), claim.asBoolean());
                } else if (claim.asLong() != null) {
                    subject.put(key.substring(claimsNamespace.length()), claim.asLong());
                } else if (claim.asInt() != null) {
                    subject.put(key.substring(claimsNamespace.length()), claim.asInt());
                } else if (claim.asDate() != null) {
                    subject.put(key.substring(claimsNamespace.length()), claim.asDate());
                } else if (claim.asString() != null) {
                    subject.put(key.substring(claimsNamespace.length()), claim.asString());
                }
            }

        }

    }
}