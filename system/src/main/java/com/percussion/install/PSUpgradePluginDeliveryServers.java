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
package com.percussion.install;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Upgrades the delivery servers file.  This includes updating the user/password values as well as adding available
 * services.
 */
public class PSUpgradePluginDeliveryServers extends PSUpgradePluginDeliveryServersBase
{
   protected void upgradeDeliveryServer(Document doc, Element deliveryServer)
   {
      NodeList connUrls = deliveryServer.getElementsByTagName(CONNECTION_URL_TAGNAME);
      Element connUrl = (Element) connUrls.item(0);
      String connUrlStr = connUrl.getTextContent();
      
      logMsg("Updating delivery server configuration for connection url: " + connUrlStr + '.');
                  
      NodeList users = deliveryServer.getElementsByTagName(USER_TAGNAME);
      Element user = (Element) users.item(0);
                  
      NodeList passwords = deliveryServer.getElementsByTagName(PASSWORD_TAGNAME);
      Element password = (Element) passwords.item(0);
      
      if (user.getTextContent().equals(OLD_USER_VALUE) && password.getTextContent().equals(OLD_PASSWORD_VALUE))
      {
         logMsg("Updating user and password values.");
         
         user.setTextContent(NEW_USER_VALUE);
         password.setTextContent(NEW_PASSWORD_VALUE);
      }
        
      logMsg("Adding admin connection url element.");
      PSXmlDocumentBuilder.addElement(doc, deliveryServer, ADMIN_CONNECTION_URL_TAGNAME, 
            getAdminConnectionUrl(connUrlStr));
                            
      logMsg("Adding allow self signed certificate element.");
      PSXmlDocumentBuilder.addElement(doc, deliveryServer, ALLOW_SELF_SIGNED_CERT_TAGNAME, TRUE_VALUE);
      
      logMsg("Adding available services element.");
      Element availableServices = PSXmlDocumentBuilder.addEmptyElement(doc, deliveryServer,
            AVAILABLE_SERVICES_TAGNAME);
      PSXmlDocumentBuilder.addElement(doc, availableServices, SERVICE_TAGNAME,
            FORM_SERVICE_VALUE);
      PSXmlDocumentBuilder.addElement(doc, availableServices, SERVICE_TAGNAME,
            FEEDS_SERVICE_VALUE);
      PSXmlDocumentBuilder.addElement(doc, availableServices, SERVICE_TAGNAME,
            COMMENTS_SERVICE_VALUE);
      PSXmlDocumentBuilder.addElement(doc, availableServices, SERVICE_TAGNAME,
            METADATA_SERVICE_VALUE);
   }

   private String getAdminConnectionUrl(String connUrl)
   {
      String host;
      try
      {
         URL url = new URL(connUrl);
         host = url.getHost();
      }
      catch (MalformedURLException e)
      {
         logMsg(e.getLocalizedMessage());
         
         host = "localhost";
      }
      
      return "https://" + host + ":8443";                        
   }
   

   @Override
   protected String getDeliveryServersFilePath()
   {
      return DELIVERY_SERVERS_FILE;
   }
   
   private static final String OLD_USER_VALUE = "admin1";
   private static final String OLD_PASSWORD_VALUE = "7cf3be70d83a6948";
   private static final String NEW_USER_VALUE = "ps_manager";
   private static final String NEW_PASSWORD_VALUE = "-7a817995ebf59950e59a1641964fdd61";
   private static final String TRUE_VALUE = "true";
   private static final String FORM_SERVICE_VALUE = "perc-form-processor";
   private static final String FEEDS_SERVICE_VALUE = "feeds";
   private static final String COMMENTS_SERVICE_VALUE = "perc-comments-services";
   private static final String METADATA_SERVICE_VALUE = "perc-metadata-services";

   private static final String DELIVERY_SERVERS_FILE = "/config/delivery-servers.xml";

}
