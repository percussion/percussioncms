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
