package conf;

import java.util.List;

import filters.PathLoggingFilter;
import ninja.Filter;
import ninja.application.ApplicationFilters;

public class Filters implements ApplicationFilters {

    @Override
    public void addFilters(List<Class<? extends Filter>> filters) {
        filters.add(PathLoggingFilter.class);
    }
}
