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

