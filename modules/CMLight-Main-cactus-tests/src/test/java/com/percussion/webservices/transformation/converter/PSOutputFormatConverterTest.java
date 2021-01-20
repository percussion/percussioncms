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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.assembly.data.OutputFormatType;
import com.percussion.webservices.transformation.converter.PSConverterTestBase;
import com.percussion.webservices.transformation.converter.PSOutputFormatConverter;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSOutputFormatConverter} class.
 */
@Category(IntegrationTest.class)
public class PSOutputFormatConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testConversion() throws Exception
   {
      // create the source object
      IPSAssemblyTemplate.OutputFormat source = 
         IPSAssemblyTemplate.OutputFormat.Database;
      
      IPSAssemblyTemplate.OutputFormat target = 
         (IPSAssemblyTemplate.OutputFormat) roundTripConversion(
            IPSAssemblyTemplate.OutputFormat.class, 
            OutputFormatType.class, 
            source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
}
