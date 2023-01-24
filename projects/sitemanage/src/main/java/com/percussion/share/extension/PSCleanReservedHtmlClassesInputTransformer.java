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
