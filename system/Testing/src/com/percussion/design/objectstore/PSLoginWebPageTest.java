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
