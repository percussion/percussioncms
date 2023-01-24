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

