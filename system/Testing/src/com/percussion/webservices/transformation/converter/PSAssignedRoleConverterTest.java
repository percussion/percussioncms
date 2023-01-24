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
import com.percussion.services.workflow.data.PSAdhocTypeEnum;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;

/**
 * Test the {@link PSAssignedRoleConverter}
 */
public class PSAssignedRoleConverterTest extends TestCase
{
   /**
    * Test the converter
    * 
    * @throws Exception if there are any errors.
    */
   public void testConverter() throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(PSAssignedRole.class);

      PSAssignedRole src = new PSAssignedRole();
      src.setAdhocType(PSAdhocTypeEnum.ANONYMOUS);
      src.setAssignmentType(PSAssignmentTypeEnum.READER);
      src.setDoNotify(true);
      src.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_ROLE, 123));
      src.setShowInInbox(true);
      src.setStateId(234);
      src.setWorkflowId(567);
      
      com.percussion.webservices.system.PSAssignedRole tgt = 
         (com.percussion.webservices.system.PSAssignedRole) 
         converter.convert(
            com.percussion.webservices.system.PSAssignedRole.class, src);
      
      assertEquals(src.isDoNotify(), tgt.isDoNotify());
      assertEquals(src.isShowInInbox(), tgt.isShowInInbox());
      assertEquals(src.getAdhocType().name().toLowerCase(), 
         tgt.getAdhocType().toString());
      assertEquals(src.getAssignmentType().name().toLowerCase(), 
         tgt.getAssignmentType().toString());
      assertEquals(src.getGUID().longValue(), tgt.getId());
   }
}

