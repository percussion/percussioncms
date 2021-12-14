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

import com.percussion.services.content.data.PSItemStatus;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.PSTransformationException;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link PSItemStatusConverter} class.
 */
@Category(IntegrationTest.class)
public class PSItemStatusConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object and vice versa.
    */
   public void testConversion() throws Exception
   {
      PSItemStatus is;
      is = createItemStatus(103, true, true, 7L, "Public", 6L, "Quick-Edit");
      roundTripConvertion(is);
      
      is = createItemStatus(100, false, false, 1L, "Draft", 1L, "Draft");
      roundTripConvertion(is);
      
   }

   /**
    * Test a list of server object convert to client array, and vice versa.
    *
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSItemStatus> srcList = new ArrayList<PSItemStatus>();
      srcList.add(createItemStatus(100, false, false, null, null, null, null));
      srcList.add(createItemStatus(101, false, false, 1L, "Quick-Edit", 1L, "Quick-Edit"));
      srcList.add(createItemStatus(102, true, false, null, null, null, null));
      srcList.add(createItemStatus(103, true, true, 7L, "Public", 6L, "Quick-Edit"));

      List<PSItemStatus> tgtList = roundTripListConversion(
            com.percussion.webservices.content.PSItemStatus[].class, srcList);
      
      assertTrue(srcList.equals(tgtList));
   }

   @SuppressWarnings("unused")
   private void roundTripConvertion(PSItemStatus source) throws PSTransformationException
   {
      PSItemStatus target = (PSItemStatus) roundTripConversion(PSItemStatus.class,
            com.percussion.webservices.content.PSItemStatus.class, source);

      assertTrue(source.equals(target));
   }

   private PSItemStatus createItemStatus(int id, boolean isChkout,
         boolean isTransition, Long fromStateId, String fromState,
         Long toStateId, String toState)
   {
      PSItemStatus is = new PSItemStatus(id, isChkout, isTransition,
            fromStateId, fromState, toStateId, toState);
      
      return is;
      
   }
}

