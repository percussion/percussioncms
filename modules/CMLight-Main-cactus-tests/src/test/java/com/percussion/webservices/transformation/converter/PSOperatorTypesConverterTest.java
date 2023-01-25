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

import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.common.OperatorTypes;
import com.percussion.webservices.transformation.converter.PSConverterTestBase;
import com.percussion.webservices.transformation.converter.PSOperatorTypesConverter;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSOperatorTypesConverter} class.
 */
@Category(IntegrationTest.class)
public class PSOperatorTypesConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSWSSearchField.PSOperatorEnum source = 
         PSWSSearchField.PSOperatorEnum.GREATERTHAN;
      
      PSWSSearchField.PSOperatorEnum target = 
         (PSWSSearchField.PSOperatorEnum) roundTripConversion(
            PSWSSearchField.PSOperatorEnum.class, 
            OperatorTypes.class, 
            source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
}
