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
 
package com.percussion.services.publisher.data;


import com.percussion.services.guidmgr.data.PSGuid;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link PSEditionContentList} object.
 */
@Category(IntegrationTest.class)
public class PSEditionContentListTest
{
   /**
    * Test the xml serialization
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      PSEditionContentList ecl1 = new PSEditionContentList(
            new PSGuid("0-115-101"),
            new PSGuid("0-110-301"),
            new PSGuid("0-105-401"));
      ecl1.setAssemblyContextId(new PSGuid("0-100-501"));
      ecl1.setAuthtype(new Integer(1));
      ecl1.setDeliveryContextId(new PSGuid("0-100-601"));
      ecl1.setSequence(new Integer(2));
      
      String str = ecl1.toXML();
      PSEditionContentList ecl2 = new PSEditionContentList(
            new PSGuid("0-115-101"),
            new PSGuid("0-110-301"),
            new PSGuid("0-105-401"));
      assertTrue(!ecl1.equals(ecl2));
      ecl2.fromXML(str);
      
      assertTrue(ecl1.getAssemblyContextId().equals(
            ecl2.getAssemblyContextId()));
      assertTrue(ecl1.getAuthtype().equals(ecl2.getAuthtype()));
      assertTrue(ecl1.getContentListId().equals(ecl2.getContentListId()));
      assertTrue(ecl1.getDeliveryContextId().equals(
            ecl2.getDeliveryContextId()));
      PSEditionContentListPK ecl1Pk = ecl1.getEditionContentListPK();
      PSEditionContentListPK ecl2Pk = ecl2.getEditionContentListPK();
      assertTrue(ecl1Pk.getContentlistid() == ecl2Pk.getContentlistid());
      assertTrue(ecl1Pk.getEditionclistid() == ecl2Pk.getEditionclistid());
      assertTrue(ecl1Pk.getEditionid() == ecl2Pk.getEditionid());
      assertTrue(ecl1.getEditionId().equals(ecl2.getEditionId()));
      assertTrue(ecl1.getGUID().equals(ecl2.getGUID()));
      assertTrue(ecl1.getSequence().equals(ecl2.getSequence()));
   }
}
