/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
