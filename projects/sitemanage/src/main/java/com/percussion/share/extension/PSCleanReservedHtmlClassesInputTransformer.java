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
package com.percussion.share.extension;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.utils.PSJsoupPreserver;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Removes reserved html class names from content in the specified html parameter value.
 * It also adds a place holder text for iframe elements if they are empty.
 * 
 * @author JaySeletz
 *
 */
public class PSCleanReservedHtmlClassesInputTransformer extends PSDefaultExtension implements IPSItemInputTransformer
{
    private static final String[] PERC_CLASSES = new String[] {"perc-widget", "perc-region", "perc-vertical", "perc-fixed", "perc-region-leaf", "perc-horizontal", "perc-itool-selectable-elem", "perc-itool-region-elem", "perc-zero-size-elem"};
    
    @Override
    public void preProcessRequest(Object[] params, IPSRequestContext request) throws PSAuthorizationException,
            PSRequestValidationException, PSParameterMismatchException, PSExtensionProcessingException
    {        
        try
        {
            PSExtensionParams ep = new PSExtensionParams(params);
            String fieldName = ep.getStringParam(0, null, true);
            if (StringUtils.isBlank(fieldName))
                throw new PSParameterMismatchException("No fieldName supplied");
            
            String value = request.getParameter(fieldName);
            if (StringUtils.isBlank(value))
                return;
            String newValue = processContent(value);
            request.setParameter(fieldName, newValue);
        }
        catch (PSConversionException e)
        {
            throw new PSParameterMismatchException(e.getLocalizedMessage());
        }

    }

    /**
     * Tries to parse the supplied content as HTML.  If fails, content is returned as is, otherwise
     * cleans reserved class names from all elements and returns the modified content. 
     * Adds place holder text for iframe elements if they are empty.
     * 
     * @param value The value to clean, assumed not <code>null<code/> or empty.
     * 
     * @return The value, cleaned if possible.
     */
    String processContent(String value)
    {
        Document doc = Jsoup.parseBodyFragment(PSJsoupPreserver.formatPreserveTagsForJSoupParse(value));
        
        boolean didChange = false;
        
        Elements elems = doc.getAllElements();
        for (Element elem : elems)
        {
            for (String className : PERC_CLASSES)
            {
                if (elem.hasClass(className))
                {
                    elem.removeClass(className);
                    didChange = true;
                }
            }
            if(elem.tagName().equalsIgnoreCase("iframe") && (elem.childNodes().isEmpty() || StringUtils.isBlank(elem.text())))
            {
                elem.text(EMPTY_IFRAME_TEXT);
                didChange = true;
            }
        }
        
        if (!didChange)
            return value;
        
        return PSJsoupPreserver.formatPreserveTagsForOutput(doc.body().html());
    }
    
    public static final String EMPTY_IFRAME_TEXT = "Alternate iframe text";
}
