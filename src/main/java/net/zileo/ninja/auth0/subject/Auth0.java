package net.zileo.ninja.auth0.subject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ninja.params.WithArgumentExtractor;

/**
 * Annotation allwoing extraction of the authenticated Subject in a controller's method parameter.
 * 
 * @author jlannoy
 */
@WithArgumentExtractor(SubjectExtractor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.PARAMETER
})
public @interface Auth0 {}