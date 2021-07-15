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
