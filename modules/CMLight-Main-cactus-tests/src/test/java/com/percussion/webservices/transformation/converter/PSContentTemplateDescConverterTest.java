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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for the {@link PSContentTemplateDescConverter}
 */
@Category(IntegrationTest.class)
public class PSContentTemplateDescConverterTest extends PSConverterTestBase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails
    */
   @SuppressWarnings(value={"unchecked"})
   public void testConverter() throws Exception
   {
      PSContentTemplateDesc ctd = new PSContentTemplateDesc();
      ctd.setId(new Long(123));
      ctd.setContentTypeId(new PSGuid(PSTypeEnum.NODEDEF, 456));
      ctd.setTemplateId(new PSGuid(PSTypeEnum.TEMPLATE, 789));
      
      PSContentTemplateDesc result = 
         (PSContentTemplateDesc) roundTripConversion(
            PSContentTemplateDesc.class, 
            com.percussion.webservices.content.PSContentTemplateDesc.class, 
            ctd);
      
      assertEquals(ctd, result);
      
      PSContentTemplateDesc ctd2 = new PSContentTemplateDesc();
      ctd2.setId(new Long(1232));
      ctd2.setContentTypeId(new PSGuid(PSTypeEnum.NODEDEF, 4562));
      ctd2.setTemplateId(new PSGuid(PSTypeEnum.TEMPLATE, 7892));      
      
      List<PSContentTemplateDesc> srcList = 
         new ArrayList<PSContentTemplateDesc>();
      srcList.add(ctd);
      srcList.add(ctd2);
      
      List<PSContentTemplateDesc> tgtList = roundTripListConversion(
         com.percussion.webservices.content.PSContentTemplateDesc[].class, 
         srcList);
      
      assertEquals(srcList, tgtList);
   }
}

