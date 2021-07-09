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
