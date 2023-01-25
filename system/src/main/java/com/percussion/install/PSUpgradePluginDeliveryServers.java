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
