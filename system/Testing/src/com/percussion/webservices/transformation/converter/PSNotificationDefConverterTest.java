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
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;

/**
 * Test the {@link PSNotificationDefConverter}
 */
public class PSNotificationDefConverterTest extends TestCase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails
    */
   public void testConverter() throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(PSNotificationDef.class);
      PSNotificationDef srcDef = new PSNotificationDef();
      srcDef.setBody("body");
      srcDef.setDescription("desc");
      srcDef.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 123));
      srcDef.setSubject("sub");
      srcDef.setWorkflowId(456);
      
      com.percussion.webservices.system.PSNotificationDef tgtDef;
      tgtDef = (com.percussion.webservices.system.PSNotificationDef) 
         converter.convert(
         com.percussion.webservices.system.PSNotificationDef.class, 
         srcDef);

      assertNotNull(tgtDef);
      assertEquals(srcDef.getBody(), tgtDef.getBody());
      assertEquals(srcDef.getDescription(), tgtDef.getDescription());
      assertEquals(srcDef.getSubject(), tgtDef.getSubject());
      assertEquals(srcDef.getGUID().longValue(), tgtDef.getId());
   }
}

