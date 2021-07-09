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
import com.percussion.services.content.data.PSContentTypeSummaryChild;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the {@link PSContentTypeSummaryChildConverter} class.
 */
@Category(IntegrationTest.class)
public class PSContentTypeSummaryChildConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object.
    *
    * @throws Exception If the test fails.
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSContentTypeSummaryChild src = createSummaryChild("ChildSummary");

      PSContentTypeSummaryChild target =
         (PSContentTypeSummaryChild) roundTripConversion(
               PSContentTypeSummaryChild.class,
            com.percussion.webservices.content.PSContentTypeSummaryChild.class,
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
      List<PSContentTypeSummaryChild> srcList = 
         new ArrayList<PSContentTypeSummaryChild>();
      srcList.add(createSummaryChild("ChildSummary"));
      srcList.add(createSummaryChild("ChildSummary2"));

      List<PSContentTypeSummaryChild> srcList2 = roundTripListConversion(
            com.percussion.webservices.content.PSContentTypeSummaryChild[].class,
            srcList);

      assertTrue(srcList.equals(srcList2));
   }

   /**
    * Creates a child summary field, which contains 2 fields.
    * 
    * @param name the name of the child summary; assumed not <code>null</code> 
    *    or empty.
    *    
    * @return the created child summary.
    */
   private PSContentTypeSummaryChild createSummaryChild(String name)
   {
      PSContentTypeSummaryChild src = new PSContentTypeSummaryChild();
      
      PSFieldDescription field = new PSFieldDescription(name + "_fld1",
         PSFieldDescription.PSFieldTypeEnum.TEXT.name());
      PSFieldDescription field2 = new PSFieldDescription(name + "_fld2",
            PSFieldDescription.PSFieldTypeEnum.BINARY.name());

      List<PSFieldDescription> fields = new ArrayList<PSFieldDescription>();
      fields.add(field);
      fields.add(field2);
      src.setChildFields(fields);
      src.setName(name);
      
      return src;
   }
   
}

