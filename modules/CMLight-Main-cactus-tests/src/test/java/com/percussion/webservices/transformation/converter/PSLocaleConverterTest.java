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

import com.percussion.i18n.PSLocale;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link PSLocaleConverter} class.
 */
@Category(IntegrationTest.class)
public class PSLocaleConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testConversion() throws Exception
   {
      // create the source object
      PSLocale source = new PSLocale("de-ch", "Swiss German", 
         "German language used in Switzerland", PSLocale.STATUS_ACTIVE);
      
      PSLocale target = (PSLocale) roundTripConversion(PSLocale.class, 
         com.percussion.webservices.content.PSLocale.class, source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
      
      List<PSLocale> srcList = new ArrayList<PSLocale>(2);
      srcList.add(source);
      srcList.add(new PSLocale("fr-ca", "Canadian French", 
         "French language used in Canada", PSLocale.STATUS_INACTIVE));
      List tgtList = roundTripListConversion(
         com.percussion.webservices.content.PSLocale[].class, srcList);
      assertEquals(srcList, tgtList);
      
      // test com.percussion.webservices.security.data.PSLocale
      target = (PSLocale) roundTripConversion(PSLocale.class, 
         com.percussion.webservices.security.data.PSLocale.class, source);
      assertTrue(source.equals(target));
      
      // test com.percussion.webservices.security.data.PSLocale[]
      tgtList = roundTripListConversion(
         com.percussion.webservices.security.data.PSLocale[].class, srcList);
      assertEquals(srcList, tgtList);
   }
   
   /**
    * Test a list of server object convert to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      List<PSLocale> srcList = new ArrayList<PSLocale>();
      srcList.add(new PSLocale("en-us", "English US", 
            "English language used in United States", PSLocale.STATUS_ACTIVE));
      srcList.add(new PSLocale("de-ch", "Swiss German", 
            "German language used in Switzerland", PSLocale.STATUS_ACTIVE));
      
      List<PSLocale> srcList2 = roundTripListConversion(
            com.percussion.webservices.content.PSLocale[].class, srcList);
      assertTrue(srcList.equals(srcList2));
      
      // test com.percussion.webservices.security.data.PSLocale[]
      srcList2 = roundTripListConversion(
            com.percussion.webservices.security.data.PSLocale[].class, srcList);
      assertTrue(srcList.equals(srcList2));
   }
}

