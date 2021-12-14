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
package com.percussion.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Handle encoding and preserving html content that JSoup may strip out, primarily to support
 * server-side scripting.  Content must be surrounded by &lt;PRESERVE&gt;&lt;/PRESERVE&gt; tags (case-sensitive).
 * 
 * @author JaySeletz
 *
 */
public class PSJsoupPreserver
{

    private static final String PRESERVATION_BEGIN_MARKER = "<PRESERVE>";
    private static final String PRESERVATION_END_MARKER = "</PRESERVE>";
    private static final String PRESERVATION_PARSE_START = "<!--PRESERVE";
    private static final String PRESERVATION_PARSE_END = "PRESERVE_END-->";

    /**
     * This method should be called using content returned from a Jsoup document html() call to 
     * revert the preservation manipulation done by {@link #formatPreserveTagsForJSoupParse(String)}.
     * @param source The content to restore
     * 
     * @return The content with the preserve tags restored.
     */
    public static String formatPreserveTagsForOutput(String source){
        String returnHTML = source;
        returnHTML = returnHTML.replace(PRESERVATION_PARSE_START, PRESERVATION_BEGIN_MARKER);
        returnHTML = returnHTML.replace(PRESERVATION_PARSE_END, PRESERVATION_END_MARKER);
        returnHTML =  decodeBetweenPreserveMarkers(returnHTML);
        return returnHTML;
    }

    /**
     * This method should be called before parsing content with Jsoup, will turn the preserve tag content into
     * an html comment which Jsoup will preserve, and html encodes the content between the tags.
     * 
     * @param source The raw src html
     * 
     * @return The preserved html
     */
    public static String formatPreserveTagsForJSoupParse(String source){
        String returnHTML = source;
        returnHTML =  encodeBetweenPreserveMarkers(returnHTML);
        returnHTML = returnHTML.replace(PRESERVATION_BEGIN_MARKER, PRESERVATION_PARSE_START);
        returnHTML = returnHTML.replace(PRESERVATION_END_MARKER, PRESERVATION_PARSE_END);
        return returnHTML;
    }

    private static String encodeBetweenPreserveMarkers(String source)
    {
        String returnHTML = source;
        String[] strings = StringUtils.substringsBetween(returnHTML,PRESERVATION_BEGIN_MARKER,PRESERVATION_END_MARKER);
        
        if (strings != null)
        {
            for (String string : strings)
            {
                returnHTML = returnHTML.replace(string, StringEscapeUtils.escapeHtml(string));
            }
        }
        return returnHTML;
    }

    private static String decodeBetweenPreserveMarkers(String source)
    {
        String returnHTML = source;
        String[] strings = StringUtils.substringsBetween(returnHTML,PRESERVATION_BEGIN_MARKER,PRESERVATION_END_MARKER);
        if (strings != null)
        {
            for (String string : strings)
            {
                returnHTML = returnHTML.replace(string, StringEscapeUtils.unescapeHtml(string));
            }
        }
        
        return returnHTML;
    }

}
