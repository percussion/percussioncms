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

package com.percussion.sitemanage.importer.utils;

import java.util.HashSet;

public class PSLinkBadKeywords {
    private static final HashSet<String>  mFilterSet = new HashSet<String>() {
        {
            add("more");
            add("this");
            add("that");
            add("click here");
            add("there");
            add("here");
            add("over there");
        }
    };


    /**
     * Applies the filters to a given String
     * 
     * @param stringForFilter
     *            the String to be filtered
     * @return a filtered String
     */
    public static boolean isStringInFilterList(final String stringToFind) {
        return mFilterSet.contains(stringToFind.toLowerCase());        
    }
    
    public static String filterLinkTextString(String stringForFilter)
    {
        String returnString;
        returnString = stringForFilter.replace("Link to ", ""); // WordPress filter
        returnString = returnString.replace("link to ", ""); // WordPress filter
        returnString = returnString.replace("Browse to ", "");
        returnString = returnString.replace("browse to ", "");
        returnString = returnString.replace("Navigate to ", "");
        returnString = returnString.replace("navigate to ", "");
        returnString = returnString.replace("Click here for ", "");
        returnString = returnString.replace("click here for ", "");
        return returnString;
    }
}
