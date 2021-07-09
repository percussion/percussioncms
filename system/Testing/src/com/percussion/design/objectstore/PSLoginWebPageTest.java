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
package com.percussion.design.objectstore;

import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class PSLoginWebPageTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode() throws MalformedURLException
   {
      final PSLoginWebPage webPage1 = new PSLoginWebPage(getUrl1(), true);
      final PSLoginWebPage webPage2 = new PSLoginWebPage(getUrl1(), true);
      
      assertFalse(webPage1.equals(new Object()));
      assertEqualsWithHash(webPage1, webPage2);
      
      webPage1.setSecure(false);
      assertFalse(webPage1.equals(webPage2));
      webPage1.setSecure(true);
      assertEqualsWithHash(webPage1, webPage2);
      
      webPage1.setUrl(getUrl2());
      assertFalse(webPage1.equals(webPage2));
      webPage1.setUrl(getUrl1());
      assertEqualsWithHash(webPage1, webPage2);
   }

   /**
    * Creates sample url.
    */
   private URL getUrl1() throws MalformedURLException
   {
      return new URL("http://www.yahoo.com");
   }
   
   /**
    * Creates another sample url.
    */
   private URL getUrl2() throws MalformedURLException
   {
      return new URL("http://www.google.com");
   }
}
