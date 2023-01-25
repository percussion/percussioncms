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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adds another deliveryServer entry for cloud server, with third-party-service
 * in the list of available services
 * 
 * @author LucasPiccoli
 * 
 */
public class PSUpgradePluginAddThirdPartyDeliveryServer extends PSUpgradePluginDeliveryServersBase
{
   
   /**
    * Creates a new DeliveryServer entry for the cloud server, with all the 
    * configuration elements (URL / username, etc)
    * @param doc w3c.DOM parsed documment for delivery-servers.xml
    */
   @Override
   protected void upgradeDeliveryServers(Document doc)
   {
      Element cloudDeliveryServer = doc.createElement(DELIVERY_SERVER_TAGNAME);

      Element connectionUrl = doc.createElement(CONNECTION_URL_TAGNAME);
      connectionUrl.setTextContent(CLOUD_CONNECTION_URL_VALUE);
      cloudDeliveryServer.appendChild(connectionUrl);
      
      Element user = doc.createElement(USER_TAGNAME);
      user.setTextContent(CLOUD_USER_VALUE);
      cloudDeliveryServer.appendChild(user);      
      
      Element password = doc.createElement(PASSWORD_TAGNAME);
      password.setAttribute("encrypted", TRUE_VALUE);
      password.setTextContent(CLOUD_PASSWORD_VALUE);
      cloudDeliveryServer.appendChild(password);
      
      Element adminUrl = doc.createElement(ADMIN_CONNECTION_URL_TAGNAME);
      adminUrl.setTextContent(CLOUD_CONNECTION_ADMIN_URL_VALUE);
      cloudDeliveryServer.appendChild(adminUrl);      
      
      Element allowSelfSignedCert = doc.createElement(ALLOW_SELF_SIGNED_CERT_TAGNAME);
      allowSelfSignedCert.setTextContent(FALSE_VALUE);
      cloudDeliveryServer.appendChild(allowSelfSignedCert);
      
      doc.getDocumentElement().appendChild(cloudDeliveryServer);
      
      upgradeDeliveryServer(doc, cloudDeliveryServer);
   }
   
   /**
    * Adds available services to the deliveryServer.
    * @param doc w3c.DOM parsed documment for delivery-servers.xml
    * @param deliveryServer The cloud delivery server in which to add the services.
    */
   @Override
   protected void upgradeDeliveryServer(Document doc, Element deliveryServer)
   {
      Element services = doc.createElement(AVAILABLE_SERVICES_TAGNAME);
      Element thirdPartyService = doc.createElement(SERVICE_TAGNAME);
      thirdPartyService.setTextContent(THIRDPARTY_SERVICE_VALUE);
      services.appendChild(thirdPartyService);
      deliveryServer.appendChild(services);
   }

   @Override
   protected String getDeliveryServersFilePath()
   {
      return DELIVERY_SERVERS_FILE;
   }
   
   private static final String THIRDPARTY_SERVICE_VALUE = "perc-thirdparty-services";

   private static final String DELIVERY_SERVERS_FILE = "/rxconfig/DeliveryServer/delivery-servers.xml";

   private static final String CLOUD_CONNECTION_URL_VALUE = "https://delivery.percussion.com";

   private static final String CLOUD_CONNECTION_ADMIN_URL_VALUE = "https://delivery.percussion.com";

   private static final String CLOUD_USER_VALUE = "ps_manager";

   private static final String CLOUD_PASSWORD_VALUE = "-7cd50422f40e9ee79d8bd0e3478f4621e6e9018044a2518f";

   private static final String TRUE_VALUE = "true";

   private static final String FALSE_VALUE = "false";
   
}
