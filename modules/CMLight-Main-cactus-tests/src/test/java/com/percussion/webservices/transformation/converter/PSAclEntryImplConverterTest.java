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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link PSAclEntryImplConverter}.
 */
@Category(IntegrationTest.class)
public class PSAclEntryImplConverterTest extends PSConverterTestBase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails.
    */
   public void testConverter() throws Exception
   {
      PSAclEntryImpl src = new PSAclEntryImpl(new PSTypedPrincipal("admin1",
         PrincipalTypes.ROLE));
      src.setAclId(123);
      src.setId(456);
      
      PSAccessLevelImpl level;
      
      int count = 0;
      for (PSPermissions perm : PSPermissions.values())
      {
         level = new PSAccessLevelImpl();
         level.setPermission(perm);
         level.setId(700 + count++);
         src.addPermission(level);
      }
      
      PSAclEntryImpl tgt = (PSAclEntryImpl) roundTripConversion(
         PSAclEntryImpl.class, 
         com.percussion.webservices.system.PSAclEntryImpl.class, src);
      
      assertEquals(src, tgt);
   }
}

