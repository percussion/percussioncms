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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.data.PSAgingTransition;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.Converter;

import junit.framework.TestCase;

/**
 * Test the {@link PSAgingTransitionConverter}
 */
public class PSAgingTransitionConverterTest extends TestCase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails.
    */
   public void testConverter() throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(PSAgingTransition.class);
      
      PSAgingTransition src = new PSAgingTransition();
      src.setDescription("desc");
      src.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, 123));
      src.setLabel("label");
      src.setTransitionAction("transAction");
      src.setTrigger("trigger");
      List<PSNotification> notificationList = new ArrayList<PSNotification>();
      notificationList.add(new PSNotification());
      notificationList.add(new PSNotification());
      src.setNotifications(notificationList);
      
      src.setInterval(5);
      src.setSystemField("field1");
      src.setType(PSAgingTransition.PSAgingTypeEnum.REPEATED);
      
      com.percussion.webservices.system.PSAgingTransition tgt = 
         (com.percussion.webservices.system.PSAgingTransition) 
         converter.convert(
            com.percussion.webservices.system.PSAgingTransition.class, 
            src);
      
      assertEquals(src.getDescription(), tgt.getDescription());
      assertEquals(src.getLabel(), tgt.getLabel());
      assertEquals(src.getTransitionAction(), tgt.getTransitionAction());
      assertEquals(src.getTrigger(), tgt.getTrigger());
      assertEquals(src.getGUID().longValue(), tgt.getId().longValue());
      assertEquals(src.getNotifications().size(), 
         tgt.getNotifications().length);
      
      assertEquals(src.getInterval(), tgt.getInterval());
      assertEquals(src.getSystemField(), tgt.getSystemField());
      assertEquals(PSStringUtils.toCamelCase(src.getType().name()), 
         tgt.getType().toString());
   }

}

