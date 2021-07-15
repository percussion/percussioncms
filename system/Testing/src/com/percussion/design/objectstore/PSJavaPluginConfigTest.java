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

public class PSJavaPluginConfigTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSJavaPluginConfig config1 = new PSJavaPluginConfig();
      final PSJavaPluginConfig config2 = new PSJavaPluginConfig();
      
      assertFalse(config1.equals(new Object()));
      assertEqualsWithHash(config1, config2);
      
      config1.addPlugin(
            new PSJavaPlugin("Anything", VERSION, true, DOWLOAD_LOCATION));
      assertFalse(config1.equals(config2));

      config2.addPlugin(
            new PSJavaPlugin("Anything", VERSION, true, DOWLOAD_LOCATION));
      assertEqualsWithHash(config1, config2);
      
      config1.addPlugin(
            new PSJavaPlugin("Anything", VERSION, true, DOWLOAD_LOCATION));
      config2.addPlugin(
            new PSJavaPlugin("Anything", VERSION, true, OTHER_STR));
      assertFalse(config1.equals(config2));
   }
   
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
