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
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSCommunityVisibility;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link PSCommunityVisibilityConverter} class.
 */
@Category(IntegrationTest.class)
public class PSCommunityVisibilityConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object as well as a
    * server array of objects to a client array of objects and back.
    */
   public void testConversion() throws Exception
   {
      PSCommunityVisibility source = createCommunityVisiblity(
         getNextId(PSTypeEnum.COMMUNITY_DEF));
         
      PSCommunityVisibility target = (PSCommunityVisibility) roundTripConversion(
         PSCommunityVisibility.class, 
         com.percussion.webservices.security.data.PSCommunityVisibility.class, 
         source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSCommunityVisibility[] sourceArray = new PSCommunityVisibility[1];
      sourceArray[0] = source;
      
      PSCommunityVisibility[] targetArray = (PSCommunityVisibility[]) roundTripConversion(
         PSCommunityVisibility[].class, 
         com.percussion.webservices.security.data.PSCommunityVisibility[].class, 
         sourceArray);
      
      // verify the the round-trip array is equal to the source array
      assertTrue(sourceArray.length == targetArray.length);
      assertTrue(sourceArray[0].equals(targetArray[0]));
   }
   
   /**
    * Test a list of server object convert to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSCommunityVisibility> srcList = 
         new ArrayList<PSCommunityVisibility>();
      srcList.add(createCommunityVisiblity(
         getNextId(PSTypeEnum.COMMUNITY_DEF)));
      srcList.add(createCommunityVisiblity(
         getNextId(PSTypeEnum.COMMUNITY_DEF)));
      
      List<PSCommunityVisibility> srcList2 = roundTripListConversion(
         com.percussion.webservices.security.data.PSCommunityVisibility[].class, 
         srcList);

      assertTrue(srcList.equals(srcList2));
   }
   
   /**
    * Create a test community visiblity for the specified comunity.
    * 
    * @param id the community id, not <code>null</code>.
    * @return the test community visibility, never <code>null</code>.
    */
   public static PSCommunityVisibility createCommunityVisiblity(IPSGuid id) 
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      if (id.getType() != PSTypeEnum.COMMUNITY_DEF.getOrdinal())
         throw new IllegalArgumentException("id must be of type community");
      
      PSCommunityVisibility communityVisibility = new PSCommunityVisibility(id);
      communityVisibility.addVisibleObject(
         new PSObjectSummary(new PSGuid(PSTypeEnum.WORKFLOW, 1000), 
            "name_1", "label_1", "description_1"));
      communityVisibility.addVisibleObject(
         new PSObjectSummary(new PSGuid(PSTypeEnum.ACTION, 1001), 
            "name_2", "label_2", "description_2"));
      
      return communityVisibility;
   }
}

