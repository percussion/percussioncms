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
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Unit tests for the {@link PSCommunityConverter} class.
 */
@Category(IntegrationTest.class)
public class PSCommunityConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSCommunity source = new PSCommunity();
      source.setGUID(new PSGuid(PSTypeEnum.COMMUNITY_DEF, 1001));
      source.setName("Name");
      source.setDescription("Description");
      Collection<IPSGuid> roles = new ArrayList<IPSGuid>();
      roles.add(new PSGuid(PSTypeEnum.ROLE, 1));
      source.setRoleAssociations(roles);
      
      PSCommunity target = (PSCommunity) roundTripConversion(PSCommunity.class, 
         com.percussion.webservices.security.data.PSCommunity.class, source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
}

