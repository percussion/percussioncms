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

/**
 * Adds the membership service name to the list of available services for each delivery server.
 * 
 * @author JaySeletz
 *
 */
public class PSUpgradePluginAddMembershipDeliveryServer extends PSUpgradePluginDeliveryServersBase
{
   private static final String MEMBERSHIP_SERVICE_VALUE = "perc-membership-services";
   private static final String DELIVERY_SERVERS_FILE = "/rxconfig/DeliveryServer/delivery-servers.xml";
   

   @Override
   protected void upgradeDeliveryServer(Document doc, Element deliveryServer)
   {
      Element availableServices = (Element) deliveryServer.getElementsByTagName(AVAILABLE_SERVICES_TAGNAME).item(0);
      NodeList services = availableServices.getElementsByTagName(SERVICE_TAGNAME);
      int svcTot = services.getLength();
      boolean hasMembershipService = false;
      for (int i = 0; i < svcTot; i++)
      {
         Element service = (Element) services.item(i);
         String serviceName = service.getNodeValue();
         if (MEMBERSHIP_SERVICE_VALUE.equals(serviceName))
         {
            hasMembershipService = true;
            break;
         }
      }
      
      if (!hasMembershipService)
      {
         logMsg("Adding Membership service element");
         PSXmlDocumentBuilder.addElement(doc, availableServices, SERVICE_TAGNAME, MEMBERSHIP_SERVICE_VALUE);
      }
   }

   @Override
   protected String getDeliveryServersFilePath()
   {
      return DELIVERY_SERVERS_FILE;
   }
}
