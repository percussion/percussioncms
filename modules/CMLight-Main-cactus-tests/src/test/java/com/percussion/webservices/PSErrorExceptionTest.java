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

