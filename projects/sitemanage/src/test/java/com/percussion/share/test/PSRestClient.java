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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.test;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.delivery.client.EasySSLProtocolSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * A Wrapper around Commons HTTP client for REST services.
 * 
 * Most of the methods are protected as this classes should be
 * extended to provide more specific behavior.
 * 
 * @author adamgent
 * @see #GET(String)
 * @see #POST(String, String)
 * @see #DELETE(String)
 *
 */
public class PSRestClient {
    private String url;
    private Map<String, String> requestHeaders = new HashMap<String, String>();
    private String postContentType= "text/xml";
    
    /**
     * To determine if a protocol for SSL was already registered
     */
    private boolean sslSocketFactoryRegistered = false;
    
    private HttpClient client = new HttpClient();
    {
        client.getParams().setContentCharset("UTF-8");
    }
    
    public List<String> parseAcceptHeader(String acceptHeader) {
        return new ArrayList<String>(asList(acceptHeader.split(",")));
    }
    
    public String outputAcceptHeader(List<String> accepts) {
        return StringUtils.join(accepts, ",");
    }
    
    protected void addAccept(String mime) {
        List<String> accepts = parseAcceptHeader(getAcceptHeader());
        accepts.add(mime);
        setAcceptHeader(outputAcceptHeader(accepts));
    }
    protected String getAcceptHeader() {
        String accept = getRequestHeaders().get("Accept");
        if (accept == null)
            return "";
        return accept;
    }
    
    protected void setAcceptHeader(String header) {
        notNull(header, "header");
        getRequestHeaders().put("Accept", header);
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    

    protected HttpClient getClient() {
        return client;
    }

    /**
     * Performs an HTTP GET.
     * @param path relative or fully qualified.
     * @param params never <code>null</code>.
     * @return never <code>null</code>.
     */
    protected String GET(String path, Collection<Entry<String, String>> params) {
        GetMethod method = getMethod();
        setPathMethod(path, method);
        NameValuePair[] nvps = nameValuePairs(params);
        method.setQueryString(nvps);
        return executeMethod(method);
    }
    
    protected String GET(String path) {
        GetMethod method = getMethod();
        setPathMethod(path, method);
        return executeMethod(method);
    }
    
    protected InputStream GET_BINARY(String path) {
        GetMethod method = getMethod();
        setPathMethod(path, method);
        return executeBinaryMethod(method);
    }
    
    private InputStream executeBinaryMethod(HttpMethod method) throws RestClientException {
        
        registerSslProtocol();
        
        try {
            int stat = client.executeMethod(method);
            InputStream body = method.getResponseBodyAsStream();
            log.trace("HTTP return code: " + stat);
            String uri = method.getURI().getURI();
            String name = method.getName();
            if (log.isDebugEnabled())
                log.debug(format("{0} {1}  HTTP Stat:{2}", name, uri, "" + stat));
            if (log.isTraceEnabled())
                log.trace("Response: " + body);
            if (/*Not Success*/ ! (200 <= stat && stat < 305) ) {
                String error = "URI: " + uri + " HTTP Error: " + stat + " Response: \n" + body;
                log.error(error);
                throw new RestClientException(stat, uri, body);
            }
            return body;
        } catch (HttpException e) {
            throw new RestClientException(e);
        } catch (IOException e) {
            throw new RestClientException(e);
        }

    }
    
    protected String POST(String path, String body) {
        return POST(path, body, getPostContentType());
    }
    
    protected String POST(String path, String body, String contentType) {
        PostMethod method = postMethod();
        String ct = contentType + "; charset=UTF-8";
        setPathMethod(path, method);
        try {
            if ( log.isTraceEnabled() && body != null) {
                log.trace("POST Body: " + body);
            }
            if (body != null)
            {
                StringRequestEntity sre = new StringRequestEntity(body, ct, "UTF-8");
                method.setRequestEntity(sre);
            }
            return executeMethod(method);
        } catch (UnsupportedEncodingException e) {
            throw new RestClientException(e);
        }
    }
    
    protected String PUT(String path, String body) {
        return PUT(path, body, getPostContentType());
    }
    
    protected String PUT(String path, String body, String contentType) {
        PutMethod method = putMethod();
        String ct = contentType + "; charset=UTF-8";
        setPathMethod(path, method);
        try {
            if ( log.isTraceEnabled() ) {
                log.trace("PUT Body: " + body);
            }
            StringRequestEntity sre = new StringRequestEntity(body, ct, "UTF-8");
            method.setRequestEntity(sre);
            return executeMethod(method);
        } catch (UnsupportedEncodingException e) {
            throw new RestClientException(e);
        }
    }
    
    
    protected String POST(String path, Collection<Entry<String, String>> params) {
        PostMethod method = postMethod();
        setPathMethod(path, method);
        NameValuePair[] nvps = nameValuePairs(params);
        method.setRequestBody(nvps);
        return executeMethod(method);
    }
    
    protected String DELETE(String path) {
        DeleteMethod method = deleteMethod();
        setPathMethod(path, method);
        return executeMethod(method);
    }
    
    protected void setPathMethod(String path, HttpMethod method) {
        notNull(path);
        notNull(method);
        URI uri = getUri(path);
        try {
            method.setURI(uri);
        } catch (URIException e) {
            throw new RestClientException("Bad url " + getUrl() + path, e);
        }
    }
    
    protected URI getUri(String relativePath) {
        try {
            return new URI(new URI(getUrl(),false), new URI(relativePath,false));
        } catch (URIException e) {
            throw new RestClientException("Bad url " + getUrl() + relativePath, e);
        } catch (NullPointerException e) {
            throw new RestClientException("Bad url " + getUrl() + relativePath, e);
        }
        
    }
 
    private NameValuePair[] nameValuePairs(Collection<Entry<String, String>> params) {
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        for(Entry<String,String> e : params) {
            NameValuePair nvp = new NameValuePair(e.getKey(),e.getValue());
            list.add(nvp);
        }
        return list.toArray(new NameValuePair[] { });
    }
    
    private void setRequestHeaders(HttpMethod method) {
        for(Entry<String, String> e : getRequestHeaders().entrySet()) {
            method.setRequestHeader(e.getKey(), e.getValue());
        }
    }
    
    public String concatPath(String start, String ... end) {
        isTrue(isNotBlank(start), "start cannot be blank");
        notEmpty(end, "Must have end paths.");
        String path = start;
        for (String p : end ) {
            path = removeEnd(path, "/") + "/" + removeStart(p, "/");
        }
        return path;
    }
    
    public String escapePath(String path) {
        try
        {
            return URLEncoder.encode(path, CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Should never happen", e);
        }
    }
    
    public String getRequestContentType() {
        return getRequestHeaders().get("Content-Type");
    }
    public void setRequestContentType(String contentType) {
        getRequestHeaders().put("Content-Type", contentType);
    }
    
    private GetMethod getMethod() {
        GetMethod method = new GetMethod(getUrl());
        setRequestHeaders(method);
        return method;
    }
    
    private PostMethod postMethod() {
        PostMethod method = new PostMethod(getUrl());
        //method.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
        setRequestHeaders(method);
        return method;
    }
    
    private PutMethod putMethod() {
        PutMethod method = new PutMethod(getUrl());
        //method.setRequestHeader("Content-Type", "text/xml; charset=UTF-8");
        setRequestHeaders(method);
        return method;
    }
    
    private DeleteMethod deleteMethod() {
        DeleteMethod method = new DeleteMethod(getUrl());
        setRequestHeaders(method);
        return method;
    }
    
    private String executeMethod(HttpMethod method) throws RestClientException {
        
        registerSslProtocol();
        
        try {
            int stat = client.executeMethod(method);
            log.trace("HTTP return code: " + stat);
            String body = method.getResponseBodyAsString();
            String uri = method.getURI().getURI();
            String name = method.getName();
            if (log.isDebugEnabled())
                log.debug(format("{0} {1}  HTTP Stat:{2}", name, uri, "" + stat));
            if (log.isTraceEnabled())
                log.trace("Response: " + body);
            if (/*Not Success*/ ! (200 <= stat && stat < 305) ) {
                String error = "URI: " + uri + " HTTP Error: " + stat + " Response: \n" + body;
                log.error(error);
                throw new RestClientException(stat, uri, body);
            }
            return body;
        } catch (HttpException e) {
            throw new RestClientException(e);
        } catch (IOException e) {
            throw new RestClientException(e);
        }

    }
    
    private void registerSslProtocol()
    {
        if (sslSocketFactoryRegistered)
            return;
        
        ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();   
        Protocol.registerProtocol("https", new Protocol("https", socketFactory, 443));
        
        sslSocketFactoryRegistered = true;
    }

    /**
     * 
     * Base exception for a REST failure.
     * 
     * @author adamgent
     *
     */
    public static class RestClientException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        private int status = 0;
        private String uri = "";
        private String responseBody;
        private String message = null;

        public RestClientException(String message) {
            super(message);
        }

        public RestClientException(String message, Throwable cause) {
            super(message, cause);
        }

        public RestClientException(Throwable cause) {
            super(cause);
        }
        
        public RestClientException() {
        }

        public RestClientException(RestClientException cause) {
            setStatus(cause.getStatus());
            setUri(cause.getUri());
            setResponseBody(cause.getResponseBody());
            setMessage(cause.getMessage());
        }
        
        public RestClientException(int status, String uri, InputStream responseBody) {
            init(status, uri, null);
            setMessage(getRestErrorMessage());
            fillInStackTrace();
        }
        
        public RestClientException(int status, String uri, String responseBody) {
            init(status, uri, responseBody);
            setMessage(getRestErrorMessage());
            fillInStackTrace();
        }
        
        
        @Override
        public String getMessage()
        {
            if (message != null) {
                return this.message;
            }
            return super.getMessage();
        }
        
        protected void setMessage(String message) 
        {
            this.message = message;
        }

        protected String getRestErrorMessage() {
            return format("HTTP Error code: {0}\nURI: {1}\nResponse: {2}", new Integer(getStatus()),getUri(),getResponseBody());
        }
        
        protected void init(int status, String uri, String responseBody) {
            this.status = status;
            this.uri = uri;
            this.responseBody = responseBody;
        }
        
        
        public String getUri() {
            return uri;
        }

        
        public void setUri(String uri) {
            this.uri = uri;
        }

        public int getStatus() {
            return status;
        }

        
        public void setStatus(int status) {
            this.status = status;
        }

        
        public String getResponseBody() {
            return responseBody;
        }

        
        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }
        
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected static final Logger log = LogManager.getLogger(PSRestClient.class);

    
    
    
    
    public String getPostContentType() {
        return postContentType;
    }

    
    public void setPostContentType(String postContentType) {
        this.postContentType = postContentType;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    
    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
