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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

/**
 * Unit test for the {@link PSAclImplConverter}
 */
@Category(IntegrationTest.class)
public class PSAclImplConverterTest extends PSConverterTestBase
{
   /**
    * Test the converter
    * 
    * @throws Exception if the test fails.
    */
   public void testConverter() throws Exception
   {
      PSAclEntryImpl owner = new PSAclEntryImpl(new PSTypedPrincipal("admin1",
         PrincipalTypes.USER));
      PSAccessLevelImpl perm = new PSAccessLevelImpl();
      owner.addPermission(PSPermissions.OWNER);
      perm.setPermission(PSPermissions.OWNER);
      perm.setId(345);
      owner.setId(234);
      PSAclImpl src = new PSAclImpl("testAcl1", owner);
      src.setId(123);
      src.setObjectId(456);
      src.setObjectType(PSTypeEnum.TEMPLATE.getOrdinal());
      src.setDescription("test");
      
      PSAclEntryImpl entry = new PSAclEntryImpl();
      src.addEntry(entry);
      entry.setId(123);
      entry.setName("admin2");
      int count = 0;
      for (PSPermissions permission : PSPermissions.values())
      {
         if (permission.equals(PSPermissions.OWNER))
            continue;
         perm = new PSAccessLevelImpl();
         perm.setId(400 + count++);
         perm.setPermission(permission);
         entry.addPermission(perm);
      }
      
     
      entry = new PSAclEntryImpl();
      src.addEntry(entry);
      entry.setId(456);
      entry.setName("reader1");
      perm = new PSAccessLevelImpl();
      perm.setPermission(PSPermissions.READ);
      perm.setId(567);
      entry.addPermission(perm);
      
      
      PSAclImpl tgt = (PSAclImpl) roundTripConversion(PSAclImpl.class, 
         com.percussion.webservices.system.PSAclImpl.class, src);
      
      assertEquals(src, tgt);
   }
}

