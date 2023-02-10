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

