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

