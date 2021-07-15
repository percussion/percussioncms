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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;

/**
 * Test the {@link PSWorkflowRoleConverter}
 */
public class PSWorkflowRoleConverterTest extends TestCase
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
      Converter converter = factory.getConverter(PSWorkflowRole.class);

      PSWorkflowRole src = new PSWorkflowRole();
      src.setDescription("desc");
      src.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_ROLE, 123));
      src.setName("role");
      src.setWorkflowId(456);
      
      com.percussion.webservices.system.PSWorkflowRole tgt = 
         (com.percussion.webservices.system.PSWorkflowRole) 
         converter.convert(
            com.percussion.webservices.system.PSWorkflowRole.class, src);
      
      assertEquals(src.getDescription(), tgt.getDescription());
      assertEquals(src.getName(), tgt.getName());
      assertEquals(src.getGUID().longValue(), tgt.getId());
   }
}

