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
