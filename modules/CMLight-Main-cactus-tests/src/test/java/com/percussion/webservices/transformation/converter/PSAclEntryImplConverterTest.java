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

import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
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

