package filters;

import org.slf4j.Logger;

import com.google.inject.Inject;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;

/**
 * Dummy filter.
 * 
 * @author jlannoy
 */
public class PathLoggingFilter implements Filter {

    @Inject
    private Logger logger;

    /**
     * @see ninja.Filter#filter(ninja.FilterChain, ninja.Context)
     */
    @Override
    public Result filter(FilterChain chain, Context context) {
        logger.info("Path : {}", context.getRequestPath());
        return chain.next(context);
    }

}