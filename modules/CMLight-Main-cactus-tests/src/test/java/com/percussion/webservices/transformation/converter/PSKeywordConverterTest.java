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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link PSKeywordConverter} class.
 */
@Category(IntegrationTest.class)
public class PSKeywordConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object as well as a
    * server array of objects to a client array of objects and back.
    * 
    * @throws Exception if an error occurs.
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSKeyword source = createKeyword("1");
      
      PSKeyword target = (PSKeyword) roundTripConversion(
         PSKeyword.class, 
         com.percussion.webservices.content.PSKeyword.class, 
         source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      // create the source array
      PSKeyword[] sourceArray = new PSKeyword[1];
      sourceArray[0] = source;
      
      PSKeyword[] targetArray = (PSKeyword[]) roundTripConversion(
         PSKeyword[].class, 
         com.percussion.webservices.content.PSKeyword[].class, 
         sourceArray);
      
      // verify the the round-trip array is equal to the source array
      assertTrue(sourceArray.length == targetArray.length);
      assertTrue(sourceArray[0].equals(targetArray[0]));
   }
   
   /**
    * Test a list of server object conversion to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSKeyword> sourceList = new ArrayList<PSKeyword>();
      sourceList.add(createKeyword("1"));
      sourceList.add(createKeyword("2"));
      
      List<PSKeyword> targetList = roundTripListConversion(
            com.percussion.webservices.content.PSKeyword[].class, 
            sourceList);

      assertTrue(sourceList.equals(targetList));
   }

   /**
    * Create a keyword for testing.
    * 
    * @param index the index used for all keyword members, assumed not 
    *    <code>null</code> or empty.
    * @return the new keyword, never <code>null</code>.
    */
   private PSKeyword createKeyword(String index)
   {
      PSKeyword keyword = new PSKeyword("label_" + index, 
         "description_" + index, index);
      keyword.setGUID(new PSGuid(PSTypeEnum.KEYWORD_DEF, 1001));
      List<PSKeywordChoice> choices = new ArrayList<PSKeywordChoice>();
      for (int i=0; i<3; i++)
      {
         PSKeywordChoice choice = new PSKeywordChoice();
         choice.setLabel("choice_" + index + "." + i);
         choice.setDescription("description_" + index + "." + i);
         choice.setValue("1." + i);
         choice.setSequence(i);
         
         choices.add(choice);
      }
      keyword.setChoices(choices);
      
      return keyword;
   }
}

