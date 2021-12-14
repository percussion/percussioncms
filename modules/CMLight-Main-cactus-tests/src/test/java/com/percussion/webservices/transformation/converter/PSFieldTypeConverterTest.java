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

import com.percussion.services.content.data.PSFieldDescription;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.PSFieldDescriptionDataType;
import org.junit.experimental.categories.Category;

/**
 * Tests the {@link PSFieldTypeConverter}
 */
@Category(IntegrationTest.class)
public class PSFieldTypeConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object.
    *  
    * @throws Exception If the test fails.
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSFieldDescription.PSFieldTypeEnum source = 
         PSFieldDescription.PSFieldTypeEnum.NUMBER; 
      
      PSFieldDescription.PSFieldTypeEnum target = 
         (PSFieldDescription.PSFieldTypeEnum) roundTripConversion(
            PSFieldDescription.PSFieldTypeEnum.class, 
            PSFieldDescriptionDataType.class, 
            source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
}

