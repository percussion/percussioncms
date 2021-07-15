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
