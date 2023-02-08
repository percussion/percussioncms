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
package com.percussion.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

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
                returnHTML = returnHTML.replace(string, StringEscapeUtils.escapeHtml4(string));
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
                returnHTML = returnHTML.replace(string, StringEscapeUtils.unescapeHtml4(string));
            }
        }
        
        return returnHTML;
    }

}
