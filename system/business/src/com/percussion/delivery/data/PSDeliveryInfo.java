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

package com.percussion.delivery.data;

import java.util.ArrayList;

import com.percussion.share.data.PSAbstractDataObject;


/**
 * This class encapsulates information for a delivery tier. A delivery
 * tier represents the target location of publishing and/or various
 * processing services (forms, etc.).
 */
public class PSDeliveryInfo extends PSAbstractDataObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected String username;

    protected String password;

    protected String connectionUrl;
    
    protected String serverType = "PRODUCTION";

    protected String adminConnectionUrl;
    
    protected Boolean allowSelfSignedCertificate;
      
    protected ArrayList<String> availableServices;

    // Constants for Available Services
    public static final String SERVICE_COMMENTS    = "perc-comments-services";
    public static final String SERVICE_FORMS       = "perc-form-processor";
    public static final String SERVICE_FEEDS       = "feeds";
    public static final String SERVICE_INDEXER     = "perc-metadata-services";
    public static final String SERVICE_EXTRACTOR   = "perc-metadata-extractor";
    public static final String SERVICE_CACHING     = "perc-cache-manager";
    public static final String SERVICE_MEMBERSHIP  = "perc-membership-services";
    public static final String SERVICE_THIRDPARTY  = "perc-thirdparty-services";    
    public static final String SERVICE_INTEGRATIONS = "perc-integrations";
    
    public PSDeliveryInfo(String url)
    {
        this(url, null, null);
    }
    
    public PSDeliveryInfo(String url, String username, String password)
    {
        this.connectionUrl = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the value of the username property. Can be <code>null</code> or empty
     * 
     * @return username
     * 
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Sets the value of the username property. Can be <code>null</code> or empty
     * 
     * @param value username
     * 
     */
    public void setUsername(String value)
    {
        this.username = value;
    }

    /**
     * Gets the value of the password property. Can be <code>null</code> or empty
     * 
     * @return password
     * 
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the value of the password property. Can be <code>null</code> or empty
     * 
     * @param value password
     * 
     */
    public void setPassword(String value)
    {
        this.password = value;
    }

    /**
     * Gets the url of the delivery server. Can be neither <code>null</code> nor empty
     * 
     * @return Url of the delivery server
     * 
     */
    public String getUrl()
    {
        return connectionUrl;
    }

    /**
     * Sets the url of the delivery server.
     * 
     * @param value url of the delivery server. Can be neither <code>null</code> nor empty
     * 
     */
    public void setUrl(String value)
    {
        this.connectionUrl = value;
    }

    /**
     * Gets the url connection for admin of the delivery server. Can be neither <code>null</code> nor empty
     * 
     * @return the url connection for admin
     * 
     */
    public String getAdminUrl()
    {
        return adminConnectionUrl;
    }

    /**
     * Sets the admin connection url of the delivery server.
     * 
     * @param value admin connection url of the delivery server. Can be neither <code>null</code> nor empty
     * 
     */
    public void setAdminUrl(String adminConnectionUrl)
    {
        this.adminConnectionUrl = adminConnectionUrl;
    }
    
    /**
     * Gets the the value to know if it is allowed self signed certificates. Can be neither <code>null</code> nor empty
     * 
     * @return true if self-signed certificates are allowed
     * 
     */
    public Boolean getAllowSelfSignedCertificate()
    {
        return allowSelfSignedCertificate;
    }

    /**
     * Sets the value to set the property
     * 
     * @param value true if it is allowed self signed certificates. Can be neither <code>null</code> nor empty
     * 
     */
    public void setAllowSelfSignedCertificate(Boolean allowSelfSignedCertificate)
    {
        this.allowSelfSignedCertificate = allowSelfSignedCertificate;
    }
    
    /**
     * Gets the list of available services of the delivery server 
     * @return list of available services strings
     */
    public ArrayList<String> getAvailableServices()
    {
        return availableServices;
    }
    
    /**
     * Sets the list of available services of the delivery server
     * @param availableServices
     */
    public void setAvailableServices(ArrayList<String> availableServices)
    {
        this.availableServices = availableServices;
    }
    /**
     * Set the server Type of the Delivery Server (Staging | Production)
     * @param serverType
     */
   public void setServerType(String serverType)
   {
      this.serverType = serverType;
   }
   
   /**
    * Get the server Type of the Delivery Server (Staging | Production)
    * @return String server type
    */
   public String getServerType()
   {
      return this.serverType;
   }

   private String realm;
    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm=realm;
    }
}
