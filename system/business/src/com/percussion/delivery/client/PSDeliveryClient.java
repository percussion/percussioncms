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
package com.percussion.delivery.client;

import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.proxyconfig.data.PSProxyConfig;
import com.percussion.proxyconfig.service.IPSProxyConfigService;
import com.percussion.proxyconfig.service.PSProxyConfigServiceLocator;
import com.percussion.server.PSServer;
import com.percussion.services.PSMissingBeanConfigurationException;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author wesleyhirsch
 *
 */
public class PSDeliveryClient extends HttpClient implements IPSDeliveryClient
{

   public static final String PERC_TID_HEADER="perc-tid";
   public static final String PERC_VERSION_HEADER="perc-version";
   public static final String TOMCAT_USER="tomcat-user";
   public static final String TOMCAT_PASSWORD="tomcat-password";

   /**
    * License Override
    */
    private String licenseOverride="";

    private static final String ENCODING_UTF8 = "UTF-8";

    /**
     * Logger for this service.
     */
    public static Log log = LogFactory.getLog(PSDeliveryClient.class);

    /**
     * The number of times a method will be retried.
     *
     * @see org.apache.commons.httpclient.DefaultHttpMethodRetryHandler
     */
    private int retryCount = 3;

    /**
     * Offline Detection
     *
     * Stop pinging the services if it appears offline.
     */
    private boolean offline; //indicates if the service is considered to be offline or not.
    /**
    * @return the offline
    */
   public boolean isOffline()
   {
      return offline;
   }

   /**
    * @param offline the offline to set
    */
   public void setOffline(boolean offline)
   {
      this.offline = offline;
   }

   /**
    * @return the failureCount
    */
   public int getFailureCount()
   {
      return failureCount;
   }

   /**
    * @param failureCount the failureCount to set
    */
   public void setFailureCount(int failureCount)
   {
      this.failureCount = failureCount;
   }

   private int failureCount;  //the number of failures reported.
    private static final int MAX_FAILURES=30; //The maximum number of failures before we assume the DTS is offline.

    /**
     * Sets the timeout until a connection is established in milli-seconds.
     *
     * @see org.apache.commons.httpclient.params.HttpConnectionParams#setConnectionTimeout(int)
     */
    private int connectionTimeout = 300000;

    /**
     * Sets the socket timeout (for each HTTP method called) in milli-seconds.
     *
     * @see org.apache.commons.httpclient.params.HttpConnectionParams#setSoTimeout(int)
     */
    private int operationTimeout = 300000;

    /**
     * This HTTP codes are treated as successful when returned by the delivery
     * server.
     */
    private static final List<Integer> successfullHttpStatusCodes = new ArrayList<Integer>()
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            add(HttpStatus.SC_OK);
            add(HttpStatus.SC_NO_CONTENT);
        }
    };

    private HttpMethodType requestType;

    private String userName ;

    private String password ;

    private Object requestMessageBody;

    private String requestUrl;

    private String responseMessageBodyContentType = MediaType.APPLICATION_JSON;

    // Make these set-able
    private String requestMessageBodyEncoding = ENCODING_UTF8;

    /**
     * The request message body content type is only used when the request body
     * is a String.
     */
    private String requestMessageBodyContentType = MediaType.APPLICATION_JSON;

    private IPSProxyConfigService proxyConfigService;

    private PSProxyConfig proxyConfig = null;

    /**
     * Creates an instance with default options for connection and operation
     * timeouts.
     * <p>
     * The instance has to be closed when it's no longer necessary. See {@link
     * PSDeliveryClient#close} for more information.
     */
    public PSDeliveryClient()
    {
        super(new MultiThreadedHttpConnectionManager());

        setupParams();
    }

    /**
     * Creates an instance with the specified {@link HttpConnectionManager} object
     * and default options for connection and operation timeouts.
     * <p>
     * The instance has to be closed when it's no longer necessary. See {@link
     * PSDeliveryClient#close} for more information.
     *
     * @param httpConnectionManager the connection manager, not
     *            <code>null</code>.
     */
    public PSDeliveryClient(HttpConnectionManager httpConnectionManager)
    {
       super(httpConnectionManager);

       setupParams();
    }

    /**
     * Creates an instance with the specified {@link HttpConnectionManager}
     * object, retry count, connection and operation timeout parameters.
     * <p>
     * The instance has to be closed when it's no longer necessary. See {@link
     * PSDeliveryClient#close} for more information.
     *
     * @param httpConnectionManager the connection manager, not
     *            <code>null</code>.
     * @param retryCount the number of retry if failure, must not be less than
     *            zero.
     * @param connectionTimeout the timeout until a connection is established in
     *            milli-seconds. Must not be less than zero.
     * @param operationTimeout the socket timeout for each operation in
     *            milli-seconds. Must not be less than zero.
     */
    public PSDeliveryClient(HttpConnectionManager httpConnectionManager, int retryCount,
            int connectionTimeout, int operationTimeout)
    {
       super(httpConnectionManager);

       if (retryCount < 0)
          throw new IllegalArgumentException("retryCount must not be < 0.");
       if (connectionTimeout < 0)
          throw new IllegalArgumentException("connectionTimeout must not be < 0.");
       if (operationTimeout < 0)
          throw new IllegalArgumentException("operationTimeout must not be < 0.");

       this.retryCount = retryCount;
       this.connectionTimeout = connectionTimeout;
       this.operationTimeout = operationTimeout;

       setupParams();
    }

    /**
     * Setups parameters for this PSDeliveryClient instance.
     */
    private void setupParams()
    {
        this.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);
        this.getHttpConnectionManager().getParams().setSoTimeout(operationTimeout);
    }

    /**
     * Shuts down the {@link HttpConnectionManager} instance configured for this
     * <code>PSDeliveryClient</code> object. This method should be called when this PSDeliveryClient
     * instance is not used anymore.
     * <p>
     * HTTP connection managers supported by this close method are
     * {@link SimpleHttpConnectionManager} and
     * {@link MultiThreadedHttpConnectionManager}. If the client provides a
     * custom HttpConnectionManager implementation, it is the caller's
     * responsibility to properly close it.
     * <p>
     * Calling this method more than once will have no effect.
     *
     * @throws IllegalStateException if a not supported HttpConnectionManager
     *             was set to the PSDeliveryClient object.
     */
    public void close()
    {
        HttpConnectionManager connectionManager = this.getHttpConnectionManager();

        if (connectionManager instanceof SimpleHttpConnectionManager)
        {
            ((SimpleHttpConnectionManager) connectionManager).shutdown();
        }
        else if (connectionManager instanceof MultiThreadedHttpConnectionManager)
        {
            ((MultiThreadedHttpConnectionManager) connectionManager).shutdown();
        }
        else
            throw new IllegalStateException("The provided HttpConnectionManager is not supported. "
                    + "Close method only support SimpleHttpConnectionManager and MultiThreadedHttpConnectionManager "
                    + "implementations. For other ones, it's the client responsibility to close the connection "
                    + "manager.");
    }

    /**
     * @return A JSON of the received data.
     * @throws PSDeliveryClientException
     */
    private JSON getJson() throws PSDeliveryClientException {
        String response = pushOrGet(MediaType.APPLICATION_JSON);
        JSON obj = net.sf.json.JSONSerializer.toJSON(response);

        return obj;
    }

    /**
     * A low level function that simply sends some content using the corresponding
     * HTTP method (according to <code>requestType</code> value), gets the response
     * and returns it.
     *
     * @param requestMessageBodyContentType
     * @return A string returned when the HTTP method is executed.
     */
    private String pushOrGet(String requestMessageBodyContentType) throws PSDeliveryClientException {
        String response = null;
        this.responseMessageBodyContentType = MediaType.APPLICATION_JSON;

        switch (this.requestType)
        {
        case GET:
            response = executeGetMethod();
            break;
        case DELETE:
            response = executeDeleteMethod();
            break;
        case POST:
            if (this.requestMessageBody == null)
                log.warn("Executing Post Method with null body.  This is probably not what you intended.");
            response = executePostMethod(requestMessageBodyContentType);
            break;
        case PUT:
            if (this.requestMessageBody == null)
                log.warn("Executing Put Method with null body.  This is probably not what you intended.");
            response = executePutMethod(requestMessageBodyContentType);
            break;
        default:
            throw new PSDeliveryClientException("Method " + this.requestType + " not implemented.");
        }

        if (isBlank(response))
            response = StringUtils.EMPTY;

        return response;
    }

    /**
     * Requests a JSON Object from a delivery server. Requires that
     * <code>this.url</code> is already set.
     *
     * @return A JSONObject of the response from the server. If there is no data
     *         returned, returns an empty JSONObject. Will never be
     *         <code>null</code>, may be empty.
     * @throws URIException
     * @throws PSDeliveryClientException if the remote server did not return
     *             JSON of the expected type.
     *
     */
    private JSONObject getJsonObject() throws PSDeliveryClientException {
        JSON obj;
        try
        {
            obj = getJson();
        }
        catch (Exception ex)
        {
            throw new PSDeliveryClientException(ex);
        }

        if (obj instanceof JSONNull)
            obj = new JSONObject();
        else if (!(obj instanceof JSONObject))
            throw new PSDeliveryClientException("Expected JSONObject, got ".concat(obj.getClass().toString()));

        return (JSONObject) obj;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.client.IPSDeliveryClient#getJsonObject(com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions)
     */
    public JSONObject getJsonObject(PSDeliveryActionOptions actionOptions) throws PSDeliveryClientException {
        prepare(actionOptions, null);
        return getJsonObject();
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.client.IPSDeliveryClient#getJsonObject(com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions, java.lang.Object)
     */
    public JSONObject getJsonObject(PSDeliveryActionOptions actionOptions, Object requestMessageBody) throws PSDeliveryClientException {
        prepare(actionOptions, requestMessageBody);
        return getJsonObject();
    }

    /**
     * Requests a JSON Array from a delivery server. Requires that
     * <code>this.url</code> is already set.
     *
     * @return A JSONArray of the response from the server. If there is no data
     *         returned, returns an empty JSONArray. Will never be
     *         <code>null</code>, may be empty.
     * @throws URIException
     * @throws PSDeliveryClientException if the remote server did not return
     *             JSON of the expected type.
     *
     */
    private JSONArray getJsonArray() throws PSDeliveryClientException {
        JSON obj;
        try
        {
            obj = getJson();
        }
        catch (Exception ex)
        {
            throw new PSDeliveryClientException(ex);
        }

        if (obj instanceof JSONNull)
            obj = new JSONArray();
        else if (!(obj instanceof JSONArray))
            throw new PSDeliveryClientException("Expected JSONArray, got ".concat(obj.getClass().toString()));

        return (JSONArray) obj;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.client.IPSDeliveryClient#getJsonArray(com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions)
     */
    public JSONArray getJsonArray(PSDeliveryActionOptions actionOptions) throws PSDeliveryClientException {
        prepare(actionOptions, null);
        return getJsonArray();
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.client.IPSDeliveryClient#getJsonArray(com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions, java.lang.Object)
     */
    public JSONArray getJsonArray(PSDeliveryActionOptions actionOptions, Object requestMessageBody) throws PSDeliveryClientException {
        prepare(actionOptions, requestMessageBody);
        return getJsonArray();
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.client.IPSDeliveryClient#push(com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions, java.lang.String, java.lang.Object)
     */
    public void push(PSDeliveryActionOptions actionOptions, String requestMessageBodyContentType,
            Object requestMessageBody) throws PSDeliveryClientException {
        prepare(actionOptions, requestMessageBody);

        String mediaType = StringUtils.isNotBlank(requestMessageBodyContentType) ? requestMessageBodyContentType :
            MediaType.APPLICATION_JSON;

        pushOrGet(mediaType);
    }

    public void push(PSDeliveryActionOptions actionOptions, Object requestMessageBody) throws PSDeliveryClientException {
       push(actionOptions, null, requestMessageBody);
    }


    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.client.IPSDeliveryClient#getString(com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions)
     */
	public String getString(PSDeliveryActionOptions actionOptions) throws PSDeliveryClientException {
        prepare(actionOptions, null);
        String response = pushOrGet(MediaType.APPLICATION_JSON);
        return response;
	}

    /**
     * Prepares everything to run the query against the remote delivery server
     * service. It extracts data from the PSDeliveryActionOptions object and
     * saves the request message body value to use it when executing an entity
     * enclosing method.
     *
     * @param actionOptions The PSDeliveryActionOptions object.
     * @param requestMessageBody The request message body
     */
	private void prepare(PSDeliveryActionOptions actionOptions, Object requestMessageBody) throws PSDeliveryClientException {
       if(actionOptions.getDeliveryInfo() == null)
       {
          log.error("Error getting info from delivery config file");
          throw new PSDeliveryClientException("Error getting info from delivery config file");
       }

        ProtocolSocketFactory socketFactory = null;
        boolean sslEnabled = isSslEnabled(actionOptions);

        if(sslEnabled){
           if (actionOptions.getDeliveryInfo().getAllowSelfSignedCertificate() != null &&
                   actionOptions.getDeliveryInfo().getAllowSelfSignedCertificate())
           {
               socketFactory = new EasySSLProtocolSocketFactory();
           }
           else //Not using self signed so setup SSL accordingly
           {
              socketFactory = new TLSProtocolSocketFactory();
           }
        }
        PSDeliveryInfo server = actionOptions.getDeliveryInfo();
        URI uri;
        String protocol, port;

        try
        {
            uri = new URI(server.getAdminUrl(), false);
            if (!sslEnabled)
            {
                // Parse delivery server url to get the protocol and port
                uri = new URI(server.getUrl(), false);
            }
        }
        catch (URIException e)
        {
            log.error("Error getting info from delivery config file");
            throw new PSDeliveryClientException(e);
        }

        protocol = uri.getScheme();

        if (sslEnabled)
        {
           port = (uri.getPort() <= 0) ? "443" : Integer.toString(uri.getPort());
           Protocol.registerProtocol(protocol, new Protocol(protocol, socketFactory, Integer.parseInt(port)));
        }else{
           port = (uri.getPort() <= 0) ? "80" : Integer.toString(uri.getPort());
        }

        this.requestType = actionOptions.getHttpMethod();
        this.requestMessageBody = requestMessageBody;

        if (actionOptions.getSuccessfullHttpStatusCodes() != null &&
                actionOptions.getSuccessfullHttpStatusCodes().size() > 0)
            successfullHttpStatusCodes.addAll(actionOptions.getSuccessfullHttpStatusCodes());

        // Request information
        if (this.requestType.equals(HttpMethodType.GET) && requestMessageBody != null)
            throw new IllegalArgumentException("Attempting to execute GET method with message body.  Body is: " + requestMessageBody);

        this.requestUrl = processUrl(actionOptions);

        // Authentication information
        String userName = actionOptions.getDeliveryInfo().getUsername();
        String password = actionOptions.getDeliveryInfo().getPassword();
        this.userName = userName;
        this.password = password;

        if (isNotBlank(userName) && isBlank(password))
            log.warn("Executing HTTP request with username but blank password.  This is probably not what you intended.");

        if (StringUtils.isNotEmpty(userName)) {
            AuthScope authScope = AuthScope.ANY;
            try {
                authScope = new AuthScope(uri.getHost(), uri.getPort(),server.getRealm());
            } catch (URIException e) {
                e.printStackTrace();
            }
            this.getState().setCredentials(authScope, new UsernamePasswordCredentials(userName, password));
        }
        
        if (proxyConfig == null)
        {
           if (this.proxyConfigService == null)
              this.proxyConfigService = getProxyConfigService();
           if (this.proxyConfigService != null)
              this.proxyConfig = proxyConfigService
                 .findByProtocol(protocol);
           else
              this.proxyConfig = new PSProxyConfig();
        }

    }
    
	/**
	 * Process the delivery server host URL, and returns the appropriate
	 * one according to the type of service (admin or non-admin) in the
	 * delivery server.
	 * 
	 * @param actionOptions The PSDeliveryActionOptions object
	 * @return The processed URL of the delivery service.
	 */
    private String processUrl(PSDeliveryActionOptions actionOptions) throws PSDeliveryClientException {
        PSDeliveryInfo server = actionOptions.getDeliveryInfo();
        
        String actionUrl = actionOptions.getActionUrl();
        String finalUrl = "";
        
        URI uri;
        
        try
        {
            String protocol, deliveryHost, port;            
            
            if (actionOptions.isAdminOperation())
            {
                // Parse delivery server url to get the host
                uri = new URI(server.getAdminUrl(), false);
            }
            else
            {
                // Parse delivery server url to get the host
                uri = new URI(server.getUrl(), false);
            }
            deliveryHost = uri.getHost();
            protocol = uri.getScheme();
            port = uri.getPort() <= 0 ? "" : Integer.toString(uri.getPort());
                      
            //Add the slash for the port
            port = (port == null || port.length() == 0) ? "" : ":" + port;
            
            // Make final URL
            finalUrl =
                protocol + "://" +
                deliveryHost + port +
                // Add slash to the url if necessary
                (actionUrl.startsWith("/") ? StringUtils.EMPTY : "/") + actionUrl;
            
            uri = new URI(finalUrl, false);
        }
        catch (URIException e)
        {
            log.error("Error parsing URL: " + finalUrl);
            throw new PSDeliveryClientException(e);
        }
        
        return uri.getEscapedURI();
    }
	
    /**
     * Executes a GET request against the given URL using authentication.
     * 
     * @return A string containing the entire contents of the body of the
     *         response. May return <code>null</code> if there is an error
     *         processing the given url.
     * @throws URIException 
     */
    private String executeGetMethod()
    {
        if (isBlank(this.responseMessageBodyContentType))
            this.responseMessageBodyContentType = MediaType.APPLICATION_JSON;
        
        GetMethod getMethod = new GetMethod(this.requestUrl);
        getMethod.setRequestHeader(HttpHeaders.CONTENT_TYPE, this.responseMessageBodyContentType);

        return this.executeHttpMethod(getMethod);
    }
    
    private String executeDeleteMethod()
    {
        if (isBlank(this.responseMessageBodyContentType))
            this.responseMessageBodyContentType = MediaType.APPLICATION_JSON;
        
        DeleteMethod deleteMethod = new DeleteMethod(this.requestUrl);
        
        if (requestMessageBody != null && requestMessageBody instanceof NameValuePair[])
            deleteMethod.setQueryString((NameValuePair[]) requestMessageBody);

        return this.executeHttpMethod(deleteMethod);
    }

    /**
     * Executes a PUT method against the given URL using authentication.
     * 
     * @param requestMessageBodyContentType
     * @return A string containing the entire contents of the body of the
     *         response. May return <code>null</code> if there is an error
     *         processing the given url.
     * @throws IOException 
     */
    private String executePutMethod(String requestMessageBodyContentType)
    {
        PutMethod putMethod = new PutMethod(this.requestUrl);
        return this.executeEntityEnclosingMethod(putMethod, requestMessageBodyContentType);
    }
    
    /**
     * Executes a POST request against the given URL using authentication.
     * 
     * @param requestMessageBodyContentType
     * @return A string containing the entire contents of the body of the
     *         response. May return <code>null</code> if there is an error
     *         processing the given url.
     * @throws URIException 
     */
    @SuppressWarnings("unchecked")
    private String executePostMethod(String requestMessageBodyContentType)
    {
         PostMethod postMethod = new PostMethod(this.requestUrl);
        
        // FIXME This code should not be here, but in executeEntityEnclosingMethod.
        // The current version of Apache HttpClient used has some limitations that
        // I couldn't workaround.
        if (this.requestMessageBody instanceof Collection<?>)
        {
            try
            {
                @SuppressWarnings("rawtypes")
               NameValuePair[] parts = (NameValuePair[]) ((Collection) this.requestMessageBody).toArray(
                        new NameValuePair[0]);
                postMethod.setRequestBody(parts);
            }
            catch (Exception ex)
            {
                String errorMessage = "Error in trying to set the request body for the POST method";
                
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } else {
           try
         {
            postMethod.setRequestEntity(new StringRequestEntity(this.requestMessageBody.toString(),"application/json","UTF-8"));
         }
         catch (UnsupportedEncodingException e)
         {
             // UTF-8 allways supported
              log.error("Unsupported Exception for UTF-8?");
        
         }
        }
        
        return this.executeEntityEnclosingMethod(postMethod, requestMessageBodyContentType);
    }
    
    /**
     * Executes an entity enclosing method (POST or PUT). This methods contains
     * the shared logic between POST and PUT.
     * 
     * @param httpMethod The HTTP method object to execute.
     * @param requestMessageBodyContentType
     * @return A string containing the entire contents of the body of the
     *         response. May return <code>null</code> if there is an error
     *         processing the given url.
     */
    private String executeEntityEnclosingMethod(EntityEnclosingMethod httpMethod, String requestMessageBodyContentType)
    {
        try
        {
            if (this.requestMessageBody != null)
            {
                if (this.requestMessageBody instanceof String)
                {
                    StringRequestEntity requestEntity = new StringRequestEntity(
                            this.requestMessageBody.toString(),
                            requestMessageBodyContentType,
                            this.requestMessageBodyEncoding);
                    
                    httpMethod.setRequestEntity(requestEntity);
                }
                else if (this.requestMessageBody instanceof Part[])
                {
                    httpMethod.setRequestEntity(new MultipartRequestEntity((Part[]) this.requestMessageBody,
                            httpMethod.getParams()));
                }
                else if (this.requestMessageBody instanceof InputStream)
                {
                    httpMethod.setRequestEntity(new InputStreamRequestEntity((InputStream) this.requestMessageBody,
                            requestMessageBodyContentType));
                }
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Error in trying to set the request body for the HTTP method";
            
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        return this.executeHttpMethod(httpMethod);
    }

    /**
     * Low level HTTP request function.
     * 
     * @param httpMethod An extended HttpMethodBase class (e.g., GetMethod,
     *            PostMethod) which contains all the configuration necessary to
     *            make a request. <strong>Note:</strong> May attempt the same
     *            request multiple times. Make sure the request passed into here
     *            is idempotent.
     * 
     * @return A string containing the body of the returned page. May be
     *         <code>null</code> if something went wrong.
     */
    private String executeHttpMethod(HttpMethodBase httpMethod)
    {
 
        // By default, the content type is APPLICATION_JSON.
        if (httpMethod.getRequestHeader(HttpHeaders.CONTENT_TYPE) != null &&
                isBlank(httpMethod.getRequestHeader(HttpHeaders.CONTENT_TYPE).getValue()))
            httpMethod.setRequestHeader(HttpHeaders.CONTENT_TYPE, this.requestMessageBodyContentType);

        if (proxyConfig != null)
        {
           if (proxyConfig.getHost() != null && proxyConfig.getPort() != null)
           {
              HostConfiguration config = this.getHostConfiguration();
              config.setProxy(proxyConfig.getHost(),
                      Integer.parseInt(proxyConfig.getPort()));
              
              if (proxyConfig.getUser() != null && proxyConfig.getPassword() != null)
              {
                 String proxyUser = proxyConfig.getUser();
                 String proxyPassword = proxyConfig.getPassword();
                 Credentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
                 AuthScope authScope = new AuthScope(proxyConfig.getHost(),
                         Integer.parseInt(proxyConfig.getPort()));
                 
                 this.getState().setProxyCredentials(authScope, credentials);              
              }
           }
        }

        httpMethod.setRequestHeader(PERC_VERSION_HEADER, PSServer.getVersion());
        httpMethod.setRequestHeader(TOMCAT_USER, this.userName);
        httpMethod.setRequestHeader(TOMCAT_PASSWORD, this.password);
        httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(retryCount, true));

        try
        {
            int statusCode = this.executeMethod(httpMethod);

            InputStream responseDataStream = httpMethod.getResponseBodyAsStream();

            String responseData = responseDataStream==null ? "" : IOUtils.toString(responseDataStream);
            if (!successfullHttpStatusCodes.contains(statusCode))
            {
               failureCount = 0;
               offline=false;
               if (statusCode == HttpStatus.SC_UNAUTHORIZED)
                {
                    log.error("Authentication error. Check user and " +
                            "password for this delivery server: "
                            + httpMethod.getStatusLine());
                    throw new RuntimeException("Authentication error. Check user and " +
                            "password for this delivery server: " +
                            httpMethod.getStatusLine());
                }
                else
                {
                    log.error("Error when executing method : " + httpMethod.getStatusLine() + " : " + responseData);
                    throw new RuntimeException("Error when executing method: " + httpMethod.getStatusLine());
                }

            }
            else
            {
                // TODO ensure correct character encoding
            	if (statusCode == HttpStatus.SC_NO_CONTENT)
            		return "";
            	else
            		return responseData;
            }

        }
        catch (IOException ex)
        {
           failureCount++;
           if(failureCount > MAX_FAILURES){
              offline = true;
              log.info("Delivery Services are unavailble.  Supressing further messages.");
           }
           if(!offline)
              log.error("Fatal transport error: " + ex.getLocalizedMessage());
            
           String reqUrl = this.requestUrl;
           try
           {
              URL url = new URL(reqUrl);
              reqUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
           }
           catch (MalformedURLException e)
           {
              if(!offline)                 
                 log.error(e.getLocalizedMessage());
           }
           if(!offline)
              throw new RuntimeException("Unable to connect to delivery server at: " + reqUrl + ".");
         return null;
        }
        finally
        {
            httpMethod.releaseConnection();
        }
    }
    
    private boolean isSslEnabled(PSDeliveryActionOptions actionOptions) throws PSDeliveryClientException {
        boolean sslEnabled = false;
        
        PSDeliveryInfo server = actionOptions.getDeliveryInfo();
        
        URI uri;
        String protocol;            
        
        try
        {
            uri = new URI(server.getAdminUrl(), false);
            protocol = uri.getScheme();
            if (protocol.equals("https"))
            {
                sslEnabled = true;
            }
            else
            {
                sslEnabled = false;
            }

        }
        catch (URIException e)
        {
            log.error("Error getting info from delivery config file");
            throw new PSDeliveryClientException(e);
        }     
        return sslEnabled;
    }


   /**
    * When set, requests send to the delivery tier will use
    * the supplied license number instead of the primary 
    * instance license id.  This is for remote calls that 
    * use a dedicated tenant id, such as the remote authorization
    * configuration service. 
    * 
    * This property will be automatically cleared after method
    * execution to prevent accidental override. 
    * 
    * @param licenseOverride the licenseOverride to set
    */
   public void setLicenseOverride(String licenseOverride)
   {
      this.licenseOverride = licenseOverride;
   }

   
   /**
    * Gets the corresponding proxy config bean for the service
    * @return ProxyConfigService bean. May be <code>null</code> if bean is not found
    */
   private IPSProxyConfigService getProxyConfigService()
   {
      try
      {
         return PSProxyConfigServiceLocator.getProxyConfigService();
      }
      catch (PSMissingBeanConfigurationException e)
      {
         return null;         
      }
   }
   
   public void setProxyConfig (PSProxyConfig proxyConfig)
   {
      this.proxyConfig = proxyConfig;
   }
   
   public PSProxyConfig getProxyConfig ()
   {
      return this.proxyConfig;
   }   
}
