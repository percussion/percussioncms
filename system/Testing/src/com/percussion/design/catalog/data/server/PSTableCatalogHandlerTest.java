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

package com.percussion.design.catalog.data.server;

import junit.framework.TestCase;

public class PSTableCatalogHandlerTest extends TestCase
{
   public void testIsOracleRecycleBinObject()
   {
      assertTrue(isOracleRecycleBinObject("BIN$KGHNSSICRgW807Nul9jzZA==$0"));
      assertTrue(isOracleRecycleBinObject("BIN$/f2GYNKuTTadpSywb4pxaw==$0"));
      
      //lesser length
      assertFalse(isOracleRecycleBinObject("BIN$/f2GYNKuTTadpSywb4pxaw==$"));
      
      //no last $
      assertFalse(isOracleRecycleBinObject("BIN$/f2GYNKuTTadpSywb4pxaw==00"));
      
      //no first $
      assertFalse(isOracleRecycleBinObject("BIN0/f2GYNKuTTadpSywb4pxaw==$0"));
   }
   
   /**
    * Convenience method to access
    * {@link PSTableCatalogHandler#isOracleRecycleBinObject(String)}.
    */
   private boolean isOracleRecycleBinObject(String name)
   {
      return new PSTableCatalogHandler().isOracleRecycleBinObject(name);
   }
}
