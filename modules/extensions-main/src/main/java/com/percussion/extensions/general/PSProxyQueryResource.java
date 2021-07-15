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

package com.percussion.extensions.general;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extensions.utils.PSExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.percussion.utils.request.PSRequestInfoBase.KEY_JSESSIONID;
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
public class PSProxyQueryResource extends PSDefaultExtension
    implements IPSResultDocumentProcessor {
    
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_USER = "user";
    private static final String PARAM_URL = "url";
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSProxyQueryResource.class);

    public boolean canModifyStyleSheet() {
        return false;
    }

    public Document processResultDocument(Object[] params,
            IPSRequestContext request, Document resultDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException {
       try{
       Map<String, String> p = getParameters(params);
        PSExtensionParamsHelper helper = new PSExtensionParamsHelper(p, request, log);
        String url = helper.getRequiredParameter(PARAM_URL);
        String user = helper.getOptionalParameter(PARAM_USER, null);
        String password = helper.getOptionalParameter(PARAM_PASSWORD, null);
        String host = "";
        int port = -1;
        String scheme = "http";
        URI uri=null;
        
        boolean internalRequest = false;
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
        if (url.startsWith("../"))
        {
           // Rewrite as absolute to the server
           url = PSServer.getRequestRoot() + url.substring(2);
        }
        
        if (url.startsWith(PSServer.getRequestRoot()))
        {
           internalRequest = true;
           
           host = "127.0.0.1";
           port = PSServer.getListenerPort();
           
           try
            {
               uri = new URI(scheme, null, host, port, url);
               
               //This is an internal request so pass the jsessionid
               String sessionid = (String) PSRequestInfo
                     .getRequestInfo(KEY_JSESSIONID);
               
               uri.setPath(uri.getPath() + ";jsessionid=" + sessionid);
               
            }
            catch (URIException e)
            {
               log.error("Error parsing supplied url: {} Error: {}" ,url, e.getMessage());
               throw new RuntimeException("Error parsing supplied url:" + url,e);
            }
       }else
       {
          try
          {
            uri = new URI(url,true);
            
            
          }
          catch (URIException e)
          {
             log.error("Error parsing supplied url: {} Error: {}" , url, e.getMessage());
             throw new RuntimeException("Error parsing supplied url:" + url,e);
          }
       }
       
        
        String repr = "url = " + url + " user = " + user + " password = " + password;
        log.debug("Trying to get document with: {}" , repr);
        
        HttpClient client = new HttpClient();
        
        HttpMethod method = new GetMethod(url);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
              new DefaultHttpMethodRetryHandler(3, false));
        
        if(!internalRequest && !StringUtils.isEmpty(user)){
           //Enable authentication for the request
           method.setDoAuthentication(true);
           UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
           client.getState().setCredentials(AuthScope.ANY, credentials);
           List<String> authPrefs = new ArrayList<>(1);
           authPrefs.add(AuthPolicy.BASIC);
           client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);              
        }
       
         try
         {
            method.setURI(uri);
         }
         catch (URIException e1)
         {
            log.error("Failed to parse url as a valid URI: {}" , url);
            throw new RuntimeException(e1);
         }
        try {
           // Execute the method.
           int statusCode = client.executeMethod(method);

           if (statusCode != HttpStatus.SC_OK) {
             log.error("Remote request to url: {} failed with status code: {}" ,url, statusCode);
             throw new RuntimeException("Remote request to url: " + url + " failed with status code: " + statusCode);
           }

           // Read the response body.
           String results = method.getResponseBodyAsString();
           results = results.replaceFirst("<\\?xml.*\\?>", "");
        
           try
           {
              return createXmlDocument(new StringReader(results), false);
           }catch (SAXException e)
           {
              String message = "XML Error with " + repr;
              log.error(message,e);
              throw new Exception(message,e);
          }
        }catch (HttpException e)
        {
           log.error("Fatal protocol violation: {}" , e.getMessage());
           throw new Exception("Fatal protocol violation: " + e.getMessage(),e);
        }
        catch (IOException e)
        {
          log.error("Fatal transport error: {}" , e.getMessage());
          throw new Exception("Fatal transport error: " + e.getMessage(),e);
        }
        finally
        {
           // Release the connection.
           method.releaseConnection();
        }
       }catch(Exception e){
          log.debug("PSProxyQueryResource attempt failed. Returning null to caller.", e);
           return null;
        }
    }
         
    @SuppressWarnings({ "unused", "unchecked" })
    private String buildUrlQueryString(IPSRequestContext request, List<String> ignore) {
        Iterator it = request.getParametersIterator();
        List<String> params = new ArrayList<>();
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
