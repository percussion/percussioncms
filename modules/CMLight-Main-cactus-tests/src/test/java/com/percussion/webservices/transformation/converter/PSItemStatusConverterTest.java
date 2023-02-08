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

