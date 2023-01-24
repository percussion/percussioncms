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
package com.percussion.pso.legacy;

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.jexl.PSDocumentUtils;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.percussion.xml.PSXmlDocumentBuilder.createXmlDocument;
import static java.util.Arrays.asList;

/**
 * This exit allows you to make an internal request to an external resource.
 * <br/>
 * The request parameters that are non-null are copied to the provided url 
 * that is to be called using HTTP GET.
 * 
 * The results of the request are then converted to W3C DOM Document.
 * <em>If the results are not XML you will get an XML error.</em>
 *  
 * This is useful when you need to make a sys_Lookup XML document for a control
 * and would like to use JSP instead of a full blown legacy XML query resource.
 * 
 * <br/>
 * See Extensions.xml for the parameters that you can pass to this exit.
 * 
 * @author adamgent
 *
 */
public class PSOProxyQueryResource extends PSDefaultExtension
    implements IPSResultDocumentProcessor {
    
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_USER = "user";
    private static final String PARAM_URL = "url";
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */



    private static final Logger log = LogManager.getLogger(PSOProxyQueryResource.class);

    public boolean canModifyStyleSheet() {
        return false;
    }

    public Document processResultDocument(Object[] params,
            IPSRequestContext request, Document resultDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException {
        Map<String, String> p = getParameters(params);
        PSOExtensionParamsHelper helper = new PSOExtensionParamsHelper(p, request, log);
        String url = helper.getRequiredParameter(PARAM_URL);
        String user = helper.getOptionalParameter(PARAM_USER, null);
        String password = helper.getOptionalParameter(PARAM_PASSWORD, null);
        PSDocumentUtils utils = new PSDocumentUtils();
        
        String prepend = null;
        String queryString = buildUrlQueryString(request, asList(PARAM_URL));
        if (StringUtils.isBlank(queryString)) {
            prepend = "";   
        }
        else if (url.contains("?")) {
            prepend = "&amp;";
        }
        else {
            prepend = "?";
        }
        
        url = url + prepend + queryString;
        String repr = "url = " + url + " user = " + user + " password = " + password;
        log.debug("Trying to get document with: {}", repr);
        
        try {

            String results = utils.getDocument(url,user,password);
            results = results.replaceFirst("<\\?xml.*\\?>", "");
            try {
                return createXmlDocument(new StringReader(results), false);
            } catch (SAXException e) {
                String message = "XML Error with " + repr;
                log.error("{}, Error: {}", message,PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                throw new RuntimeException(message,e);
            }
        } catch (HttpException e) {
            String message = "Http Error with " + repr;
            log.error("{}, Error: {}", message,PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new RuntimeException(message, e);
        } catch (IOException e) {
            String message = "IO Error with " + repr;
            log.error("{}, Error: {}", message,PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new RuntimeException(message, e);
        } catch (ServletException e) {
            String message = "Servlet error with " + repr;
            log.error("{}, Error: {}", message, PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new RuntimeException(message, e);
        }
    }

    @SuppressWarnings({ "unused", "unchecked" })
    private String buildUrlQueryString(IPSRequestContext request, List<String> ignore) {
        Iterator it = request.getParametersIterator();
        List<String> params = new ArrayList<String>();
        while (it.hasNext()) {
            Entry<String, Object> element = (Entry<String, Object>) it.next();
            String name = element.getKey();
            if (ignore != null && ignore.contains(name)) continue;
            Object value = element.getValue();
            if (value == null) continue;
            String valueString = null;
            if (value instanceof String) {
                valueString = (String) value;
            }
            else if ( value instanceof Number) {
                valueString = value.toString();
            }
            if (valueString != null)
                params.add(name + "=" + valueString);
        }
        return StringUtils.join(params.iterator(), "&amp;");
    }

}
