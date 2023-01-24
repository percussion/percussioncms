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

package com.percussion.server;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import junit.framework.TestCase;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;

public class PSPageCacheTest extends TestCase
{

   public void testPSPageCache()
   {
      PSPageCache cache = new PSPageCache();

      DocumentBuilderFactory factory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
              new PSXmlSecurityOptions(
                      true,
                      true,
                      true,
                      false,
                      true,
                      false
              )
      );

      DocumentBuilder builder;
      try
      {
         // cache timeout in miliseconds but we use Date to compare which uses seconds.
         cache.setCacheTimeout(2000);
         builder = factory.newDocumentBuilder();
         cache.addPage(builder.newDocument());
         assertEquals(1L, cache.getCacheSize());
         Thread.sleep(3000);
         cache.cleanCache();
         assertEquals(0L, cache.getCacheSize());

         // Test ceiling
         int i = 1;
         Document prototypeDoc = builder.newDocument();
         while (i < 2000)
         {
            cache.addPage(prototypeDoc);
            i++;
         }
         assertEquals(1000L, cache.getCacheSize());
         
         // wait 3 seconds cache items should expire.
         Thread.sleep(3000);

         cache.cleanCache();

         assertEquals(0L, cache.getCacheSize());

      }
      catch (Exception e)
      {
         assertEquals("Exception caught" + getFullStackTrace(e), 0, 1);
      }

   }
}
