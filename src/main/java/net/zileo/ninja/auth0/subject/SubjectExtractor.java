package net.zileo.ninja.auth0.subject;

import com.google.inject.Inject;

import net.zileo.ninja.auth0.filters.AuthenticateFilter;
import net.zileo.ninja.auth0.handlers.Auth0TokenHandler;
import ninja.Context;
import ninja.params.ArgumentExtractor;

/**
 * Extractor used by the {@link Auth0} annotation.
 * 
 * @author jlannoy
 */
public class SubjectExtractor implements ArgumentExtractor<Subject> {

    private final Auth0TokenHandler<? extends Subject> tokenHandler;

    /**
     * Constructor.
     * 
     * @param tokenHandler
     *            client implementation of a token handler
     */
    @Inject
    public SubjectExtractor(Auth0TokenHandler<? extends Subject> tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    /**
     * @see ninja.params.ArgumentExtractor#extract(ninja.Context)
     */
    @Override
    public Subject extract(Context context) {

        // Check first if the job has already been done by an AuthenticateFilter
        if (context.getAttribute(AuthenticateFilter.SUBJECT_CTX_KEY) != null) {
            return context.getAttribute(AuthenticateFilter.SUBJECT_CTX_KEY, Subject.class);
        }

        try {
            return this.tokenHandler.buildSubject(context);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    /**
     * @see ninja.params.ArgumentExtractor#getExtractedType()
     */
    @Override
    public Class<Subject> getExtractedType() {
        return Subject.class;
    }

    /**
     * @see ninja.params.ArgumentExtractor#getFieldName()
     */
    @Override
    public String getFieldName() {
        return null;
    }
}