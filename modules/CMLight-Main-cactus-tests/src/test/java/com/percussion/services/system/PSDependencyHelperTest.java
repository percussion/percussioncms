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
package com.percussion.services.system;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.system.impl.PSDependencyHelper;
import com.percussion.utils.guid.IPSGuid;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link PSDependencyHelper}.  This test depends on the  
 * FastForward implementation and sample content.
 */
@Category(IntegrationTest.class)
public class PSDependencyHelperTest extends ServletTestCase
{
   /**
    * Tests finding various dependencies.
    * 
    * @throws Exception if the test fails.
    */
   public void testFindDependents() throws Exception
   {
      PSDependencyHelper dh = new PSDependencyHelper();
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.WORKFLOW, 4)).isEmpty());
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.WORKFLOW, 5)).isEmpty());
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.LOCALE, 1)).isEmpty());
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.COMMUNITY_DEF, 1001)).isEmpty());
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.DISPLAY_FORMAT, 0)).isEmpty());
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.SLOT, 516)).isEmpty());
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.SITE, 303)).isEmpty());
      assertTrue(!dh.findDependents(new PSGuid(
         PSTypeEnum.TEMPLATE, 521)).isEmpty());
      IPSGuid[] guids = new IPSGuid[2];
      guids[0] = new PSGuid(PSTypeEnum.TEMPLATE, 521);
      guids[1] = new PSGuid(PSTypeEnum.NODEDEF, 307);
      assertTrue(!dh.findDependents(guids).isEmpty());
   }
}

