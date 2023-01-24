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

import junit.framework.TestCase;
import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

public class PSJavaPluginTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSJavaPlugin plugin1 = createPlugin();
      final PSJavaPlugin plugin2 = createPlugin();
      assertEqualsWithHash(plugin1, plugin2);

      plugin2.setOsKey(OTHER_STR);
      assertFalse(plugin1.equals(plugin2));
      plugin2.setOsKey(OS_KEY);
      assertEqualsWithHash(plugin1, plugin2);

      plugin2.setBrowserKey(OTHER_STR);
      assertFalse(plugin1.equals(plugin2));
      plugin2.setBrowserKey(BROWSER_KEY);
      assertEqualsWithHash(plugin1, plugin2);

      plugin2.setVersionToUse(OTHER_STR);
      assertFalse(plugin1.equals(plugin2));
      plugin2.setVersionToUse(VERSION);
      assertEqualsWithHash(plugin1, plugin2);

      plugin2.setStaticVersioningType(false);
      assertFalse(plugin1.equals(plugin2));
      plugin2.setStaticVersioningType(true);
      assertEqualsWithHash(plugin1, plugin2);

      plugin2.setDownloadLocation(OTHER_STR);
      assertFalse(plugin1.equals(plugin2));
      plugin2.setDownloadLocation(DOWLOAD_LOCATION);
      assertEqualsWithHash(plugin1, plugin2);
}
   
   /**
    * Creates and initializes java plugin.
    * @return
    */
   private PSJavaPlugin createPlugin()
   {
      return new PSJavaPlugin(OS_KEY, BROWSER_KEY, VERSION,
            true, DOWLOAD_LOCATION);
   }


   /**
    * Sample OS key.
    */
   private static final String OS_KEY = "OS key";
   
   /**
    * Sample browser key.
    */
   private static final String BROWSER_KEY = "Browser Key";
   
   /**
    * Sample version.
    */
   private static final String VERSION = "Version";
   
   /**
    * Sample download location.
    */
   private static final String DOWLOAD_LOCATION = "Download Location";

   /**
    * Sample string.
    */
   private static final String OTHER_STR = "Other String";
}
