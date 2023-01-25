/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
