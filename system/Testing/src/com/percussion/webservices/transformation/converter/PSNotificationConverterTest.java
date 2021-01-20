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
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;

/**
 * Test case for {@link PSNotification}
 */
public class PSNotificationConverterTest extends TestCase
{
   /**
    * Test the converter
    * 
    * @throws Exception if there are any errors
    */
   public void testConverter() throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(PSNotification.class);

      PSNotification src = new PSNotification();
      src.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 123));
      src.setStateRoleRecipientType(
         PSNotification.PSStateRoleRecipientTypeEnum.FROM_STATE_RECIPIENTS);
      src.setTransitionId(123);
      src.setWorkflowId(456);
      
      List<String> ccRecipientList = new ArrayList<String>();
      ccRecipientList.add("abc@123.com");
      ccRecipientList.add("def@456.com");
      src.setCCRecipients(ccRecipientList);

      List<String> recipientList = new ArrayList<String>();
      recipientList.add("foo@bar.com");
      recipientList.add("bar@foo.com");
      src.setRecipients(recipientList);
      
      com.percussion.webservices.system.PSNotification tgt = 
         (com.percussion.webservices.system.PSNotification) 
         converter.convert(
            com.percussion.webservices.system.PSNotification.class, src);
      
      assertEquals(src.getCCRecipients(), Arrays.asList(tgt.getCCRecipients()));
      assertEquals(src.getGUID().longValue(), tgt.getId());
      assertEquals(src.getRecipients(), Arrays.asList(tgt.getRecipients()));
      assertEquals(PSStringUtils.toCamelCase(
         src.getStateRoleRecipientType().name()), 
         tgt.getStateRoleRecipientType().toString());
   }
}

