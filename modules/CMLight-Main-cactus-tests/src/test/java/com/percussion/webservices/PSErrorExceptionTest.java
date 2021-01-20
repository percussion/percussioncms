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
package com.percussion.webservices;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.faults.PSError;
import com.percussion.webservices.transformation.converter.PSConverterTestBase;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSErrorException} class.
 */
@Category(IntegrationTest.class)
public class PSErrorExceptionTest extends PSConverterTestBase
{
   /**
    * Test all contracts. 
    */
   public void testContracts() throws Exception
   {
      Exception exception = null;
      try
      {
         @SuppressWarnings("unused") PSErrorException source = 
            new PSErrorException(1, null, "stack");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         @SuppressWarnings("unused") PSErrorException source = 
            new PSErrorException(1, " ", "stack");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         @SuppressWarnings("unused") PSErrorException source = 
            new PSErrorException(1, "message", null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         @SuppressWarnings("unused") PSErrorException source = 
            new PSErrorException(1, "message", " ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      @SuppressWarnings("unused") PSErrorException source = 
         new PSErrorException(1, "message", "stack");
   }
   
   /**
    * Test conversion. 
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSErrorException source = new PSErrorException(1, "message", "stack");
      
      PSErrorException target = (PSErrorException) roundTripConversion(
         PSErrorException.class, PSError.class, source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
}

