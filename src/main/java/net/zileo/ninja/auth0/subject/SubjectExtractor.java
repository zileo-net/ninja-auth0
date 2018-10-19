package net.zileo.ninja.auth0.subject;

import com.google.inject.Inject;

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
        return this.tokenHandler.buildSubject(context);
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