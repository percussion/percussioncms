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

import com.percussion.services.content.data.PSFieldDescription;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the {@link PSFieldDescriptionConverter} class.
 */
@Category(IntegrationTest.class)
public class PSFieldDescriptionConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object.
    *  
    * @throws Exception If the test fails.
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSFieldDescription src = new PSFieldDescription("fld1", 
         PSFieldDescription.PSFieldTypeEnum.TEXT.name()); 
      
      PSFieldDescription target = 
         (PSFieldDescription) roundTripConversion(
            PSFieldDescription.class, 
            com.percussion.webservices.content.PSFieldDescription.class, 
            src);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(src.equals(target));
   }

   /**
    * Test a list of server object convert to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSFieldDescription> srcList = new ArrayList<PSFieldDescription>();
      srcList.add(new PSFieldDescription("fld1", 
            PSFieldDescription.PSFieldTypeEnum.TEXT.name()));
      srcList.add(new PSFieldDescription("fld2", 
            PSFieldDescription.PSFieldTypeEnum.TEXT.name()));
      
      List<PSFieldDescription> srcList2 = roundTripListConversion(
            com.percussion.webservices.content.PSFieldDescription[].class, 
            srcList);

      assertTrue(srcList.equals(srcList2));
   }
   
   
}

