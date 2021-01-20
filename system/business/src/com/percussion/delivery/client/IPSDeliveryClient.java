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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * @author wesleyhirsch
 *
 */
public interface IPSDeliveryClient
{
    /**
     * Base request method to get something from the delivery tier. It does not
     * support sending an enclosed entity.
     * 
     * @param actionOptions An object specifying the delivery server, action url,
     * http method to use, etc. Must not be <code>null</code>.
     * @return Returns the plain string object sent by the delivery tier. Never
     * <code>null</code>, maybe empty.
     */
    public String getString(PSDeliveryActionOptions actionOptions);
    
    /**
     * Requests a JSON Object from a delivery server.
     * 
     * @param actionOptions An object specifying the delivery server, action url,
     * http method to use, etc. Must not be <code>null</code>.
     * @return A JSONObject of the response from the server. If there is no data
     * returned, returns an empty JSONObject. Will never be <code>null</code>,
     * may be empty.
     */
    public JSONObject getJsonObject(PSDeliveryActionOptions actionOptions);
    
    /**
     * Requests a JSON Object from a delivery server, with the possibility to
     * specify an enclosed entity to send to the server (needs a POST or PUT
     * method).
     * 
     * @param actionOptions An object specifying the delivery server, action url,
     * http method to use, etc. Must not be <code>null</code>.
     * @param requestMessageBody An object to be passed to the remote server. It
     * may be a String or a list of NameValuePair objects (in the case you want to
     * send data as an HTML form element). Must be pre-escaped, and in the correct
     * format. Maybe be <code>null</code>, in that case it runs in the same way as
     * if you were using getJsonObject(PSDeliveryActionOptions actionOptions).
     * @return A JSONObject of the response from the server. If there is no data
     * returned, returns an empty JSONObject. Will never be <code>null</code>,
     * may be empty.
     * 
     */
    public JSONObject getJsonObject(PSDeliveryActionOptions actionOptions, Object requestMessageBody);
    
    /**
     * Requests a JSON Array from a delivery server.
     * 
     * @param actionOptions An object specifying the delivery server, action url,
     * http method to use, etc. Must not be <code>null</code>.
     * @return A JSONArray of the response from the server. If there is no data
     * returned, returns an empty JSONArray. Will never be <code>null</code>,
     * may be empty.
     * 
     */
    public JSONArray getJsonArray(PSDeliveryActionOptions actionOptions);
    
    /**
     * Requests a JSON Array from a delivery server, with the possibility to
     * specify an enclosed entity to send to the server (needs a POST or PUT
     * method).
     * 
     * @param actionOptions An object specifying the delivery server, action url,
     * http method to use, etc. Must not be <code>null</code>.
     * @param requestMessageBody An object to be passed to the remote server. It
     * may be a String or a list of NameValuePair objects (in the case you want to
     * send data as an HTML form element). Must be pre-escaped, and in the correct
     * format. Maybe be <code>null</code>, in that case it runs in the same way as
     * if you were using getJsonObject(PSDeliveryActionOptions actionOptions).
     * @return A JSONArray of the response from the server. If there is no data
     * returned, returns an empty JSONArray. Will never be <code>null</code>,
     * may be empty.
     * 
     */
    public JSONArray getJsonArray(PSDeliveryActionOptions actionOptions, Object requestMessageBody);
    
    /**
     * Base method to send something to the delivery server, where you don't expect
     * nothing back.
     *  
     * @param actionOptions An object specifying the delivery server, action url,
     * http method to use, etc. Must not be <code>null</code>.
     * @param requestMessageBodyContentType the media type {@link MediaType} of the request
     * message body.  Defaults to {@link MediaType#APPLICATION_JSON} if blank.
     * @param requestMessageBody An object to be passed to the remote server. It
     * may be a String or a list of NameValuePair objects (in the case you want to
     * send data as an HTML form element). Must be pre-escaped, and in the correct
     * format, as specified in the request message type. Maybe be <code>null</code>,
     * in that case it runs in the same way as if you were using
     * getJsonObject(PSDeliveryActionOptions actionOptions).
     */
    public void push(PSDeliveryActionOptions actionOptions, String requestMessageBodyContentType,
            Object requestMessageBody);

    /**
     * Convenience method, call {@link #push(PSDeliveryActionOptions, String, Object) push(PSDeliveryActionOptions, null, Object)}
     * @param actionOptions An object specifying the delivery server, action url,
     * http method to use, etc. Must not be <code>null</code>.
     * @param requestMessageBody An object to be passed to the remote server. It
     * may be a String or a list of NameValuePair objects (in the case you want to
     * send data as an HTML form element). Must be pre-escaped, and in the correct
     * format, as specified in the request message type. Maybe be <code>null</code>,
     * in that case it runs in the same way as if you were using
     * getJsonObject(PSDeliveryActionOptions actionOptions).
     */
    public void push(PSDeliveryActionOptions actionOptions, Object requestMessageBody);

    public enum HttpMethodType
    {
    	// These methods are nullipotent, i.e., for N >= 0 requests, the result is always the same.  In other words, they don't change anything.
    	GET,
    	HEAD,

    	// These methods are idempotent, i.e., for N > 0 requests, the result is always the same.  In other words, they may change something, but what they change it to is always the same.
    	PUT,
    	DELETE,
    	
    	// This method is volatile, i.e., for N requests, the result may be different every time, and/or, they may change things every time.  Be careful when using this.
    	POST,
    	
    	// These methods are little known and not often used.  Look up how HTTP works to understand these.
    	OPTIONS,
    	TRACE,
    	CONNECT
    }
    
    /**
     * Represent a set of settings when executing the remote method in the
     * delivery tier. You can specify the delivery server, action URL, http
     * method to use, and whether the service is and "admin" one or not.
     * 
     * @author miltonpividori
     *
     */
    public class PSDeliveryActionOptions
    {
        /**
         * The delivery server information object. Should not be <code>null</code>,
         * and it should have valid delivery server information.
         */
        private PSDeliveryInfo deliveryInfo;
        
        /**
         * Action URL of the remove service. For example "/perc-comments-services/comment".
         * It will be concatenated to the delivery host URL. Should not be <code>null</code>.
         */
        private String actionUrl;
        
        /**
         * Http method to use (i.e. GET, POST, PUT, etc).
         */
        private HttpMethodType httpMethod;
        
        /**
         * An "admin" call means that the operation is intented for admin users,
         * and it's secured in the delivery tier.
         */
        private boolean isAdminOperation;
        
        /**
         * Stores a list of "successful" HTTP status codes. If the delivery
         * server returns one of these, the delivery client will accept it
         * as a correct status code.
         */
        private Set<Integer> successfullHttpStatusCodes;
        
        public PSDeliveryActionOptions()
        {
            successfullHttpStatusCodes = new HashSet<Integer>();
        }
        
       /**
        * Setup the options to use for the request to the DTS.
        * 
        * @param deliveryInfo The DTS info, not <code>null</code>.
        * @param actionUrl The url to request
        * @param httpMethod The HTTP method to use, not <code>null</code>
        * @param isAdminOperation <code>true</code> to use HTTPS, <code>false</code> otherwise, as a rule <code>true</code> should always
        * be passed to allow for HTTPS-only communications from CM1.
        */
        public PSDeliveryActionOptions(PSDeliveryInfo deliveryInfo, String actionUrl, HttpMethodType httpMethod, boolean isAdminOperation)
        {
            this();
            
            this.deliveryInfo = deliveryInfo;
            this.actionUrl = actionUrl;
            this.httpMethod = httpMethod;
            this.isAdminOperation = isAdminOperation;
        }
        
        public PSDeliveryActionOptions(PSDeliveryInfo deliveryInfo, String actionUrl, HttpMethodType httpMethod)
        {
            this(deliveryInfo, actionUrl, httpMethod, true);
        }
        
        public PSDeliveryActionOptions(PSDeliveryInfo deliveryInfo, String actionUrl, boolean isAdminOperation)
        {
            this(deliveryInfo, actionUrl, HttpMethodType.GET, isAdminOperation);
        }
        
        public PSDeliveryActionOptions(PSDeliveryInfo deliveryInfo, String actionUrl)
        {
            this(deliveryInfo, actionUrl, HttpMethodType.GET);
        }

        public PSDeliveryInfo getDeliveryInfo()
        {
            return deliveryInfo;
        }

        public PSDeliveryActionOptions setDeliveryInfo(PSDeliveryInfo deliveryInfo)
        {
            this.deliveryInfo = deliveryInfo;
            return this;
        }

        public String getActionUrl()
        {
            return actionUrl;
        }

        public PSDeliveryActionOptions setActionUrl(String actionUrl)
        {
            this.actionUrl = actionUrl;
            return this;
        }

        public HttpMethodType getHttpMethod()
        {
            return httpMethod;
        }

        public PSDeliveryActionOptions setHttpMethod(HttpMethodType httpMethod)
        {
            this.httpMethod = httpMethod;
            return this;
        }

        public boolean isAdminOperation()
        {
            return isAdminOperation;
        }

        public PSDeliveryActionOptions setAdminOperation(boolean isAdminOperation)
        {
            this.isAdminOperation = isAdminOperation;
            return this;
        }

        public Set<Integer> getSuccessfullHttpStatusCodes()
        {
            return successfullHttpStatusCodes;
        }

        public PSDeliveryActionOptions setSuccessfullHttpStatusCodes(Set<Integer> successfullHttpStatusCodes)
        {
            this.successfullHttpStatusCodes = successfullHttpStatusCodes;
            return this;
        }

        public PSDeliveryActionOptions addSuccessfullHttpStatusCode(Integer successfullHttpStatusCode)
        {
            this.successfullHttpStatusCodes.add(successfullHttpStatusCode);
            return this;
        }
    }
    
    public static class PSDeliveryClientException extends RuntimeException
    {

        /**
         * 
         */
        private static final long serialVersionUID = -7552921159505062022L;

        /**
         * 
         */
        public PSDeliveryClientException() {
            super();
            // TODO Auto-generated constructor stub
        }

        /**
         * @param message
         * @param cause
         */
        public PSDeliveryClientException(String message, Throwable cause) {
            super(message, cause);
            // TODO Auto-generated constructor stub
        }

        /**
         * @param message
         */
        public PSDeliveryClientException(String message) {
            super(message);
            // TODO Auto-generated constructor stub
        }

        /**
         * @param cause
         */
        public PSDeliveryClientException(Throwable cause) {
            super(cause);
            // TODO Auto-generated constructor stub
        }
    }
}
