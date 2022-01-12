package com.percussion.rest.sites;

public enum SiteMapDateFormat {
    /** "yyyy-MM-dd'T'HH:mm:ss.SSSZ" */
    MILLISECOND("yyyy-MM-dd'T'HH:mm:ss.SSSZ", true),
    /** "yyyy-MM-dd'T'HH:mm:ssZ" */
    SECOND("yyyy-MM-dd'T'HH:mm:ssZ", true),
    /** "yyyy-MM-dd'T'HH:mmZ" */
    MINUTE("yyyy-MM-dd'T'HH:mmZ", true),
    /** "yyyy-MM-dd" */
    DAY("yyyy-MM-dd", false),
    /** "yyyy-MM" */
    MONTH("yyyy-MM", false),
    /** "yyyy" */
    YEAR("yyyy", false),
    /** Automatically compute the right pattern to use */
    AUTO("", true);

    private final String pattern;
    private final boolean includeTimeZone;

    SiteMapDateFormat(String pattern, boolean includeTimeZone) {
        this.pattern = pattern;
        this.includeTimeZone = includeTimeZone;
    }
}
