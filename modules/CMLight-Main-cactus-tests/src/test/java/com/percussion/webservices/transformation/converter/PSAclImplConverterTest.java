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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
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

