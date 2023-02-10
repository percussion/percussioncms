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
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;
/**
 * Test the {@link PSTransitionConverter}
 */
public class PSTransitionConverterTest extends TestCase
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
      Converter converter = factory.getConverter(PSTransition.class);
      
      PSTransition src = new PSTransition();
      src.setAllowAllRoles(true);
      src.setApprovals(3);
      src.setDefaultTransition(true);
      src.setDescription("desc");
      src.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, 123));
      src.setLabel("label");
      src.setRequiresComment(PSTransition.PSWorkflowCommentEnum.DO_NOT_SHOW);
      src.setTransitionAction("transAction");
      src.setTrigger("trigger");
      List<PSNotification> notificationList = new ArrayList<PSNotification>();
      notificationList.add(new PSNotification());
      notificationList.add(new PSNotification());
      src.setNotifications(notificationList);
      
      com.percussion.webservices.system.PSTransition tgt = 
         (com.percussion.webservices.system.PSTransition) 
         converter.convert(
            com.percussion.webservices.system.PSTransition.class, src);
      
      assertEquals(src.getDescription(), tgt.getDescription());
      assertEquals(src.getLabel(), tgt.getLabel());
      assertEquals(src.getTransitionAction(), tgt.getTransitionAction());
      assertEquals(src.getTrigger(), tgt.getTrigger());
      assertEquals(src.getApprovals(), tgt.getApprovals());
      assertEquals(src.getGUID().longValue(), tgt.getId().longValue());
      assertEquals(src.getNotifications().size(), 
         tgt.getNotifications().length);
      assertEquals(PSStringUtils.toCamelCase(src.getRequiresComment().name()), 
         tgt.getComment().toString());
      assertEquals(src.isAllowAllRoles(), tgt.isAllowAllRoles());
      assertEquals(src.isDefaultTransition(), tgt.isDefaultTransition());
   }
}

