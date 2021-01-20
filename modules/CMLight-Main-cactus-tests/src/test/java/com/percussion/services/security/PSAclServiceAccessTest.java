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
package com.percussion.services.security;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.servlets.PSSecurityFilter;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link IPSAclService} access level operations.  See
 * {@link PSAclServiceTest} for other test cases. 
 */
@Category(IntegrationTest.class)
public class PSAclServiceAccessTest extends ServletTestCase
{
   
   /**
    * Test that the user access service call works correctly.  Note that this
    * does not exhaustively test the combinations and permutiations of acl
    * access - that is done by the
    * {@link com.percussion.services.security.impl.PSAclEntryImplIteratorTest}.
    * 
    * @throws Exception if the test fails or there are any errors
    */
   public void testUserAccessLevel() throws Exception
   {
      PSSecurityFilter.authenticate(request, response, "admin1", "demo");
      IPSAclService svc = PSAclServiceLocator.getAclService();
      List<IPSAcl> aclList = null;
      boolean success = false;
      try
      {
         aclList = PSAclServiceTest.createTestAcls();
         PSAclImpl acl = (PSAclImpl) aclList.get(0);
         
         // test no acl
         PSUserAccessLevel al;
         al = svc.getUserAccessLevel(new PSGuid(PSTypeEnum.EXTENSION, 9999));
         assertTrue(al.hasDeleteAccess());
         assertTrue(al.hasReadAccess());
         assertFalse(al.hasRuntimeAccess());
         assertTrue(al.hasUpdateAccess());
         assertTrue(al.hasOwnerAccess());
         
         
         // test owner
         al = svc.getUserAccessLevel(
            new PSGuid(PSTypeEnum.valueOf(acl.getObjectType()), 
               acl.getObjectId()));
         assertFalse(al.hasDeleteAccess());
         assertTrue(al.hasReadAccess());
         assertTrue(al.hasRuntimeAccess());
         assertFalse(al.hasUpdateAccess());
         assertTrue(al.hasOwnerAccess());

         
         // no perms
         acl = (PSAclImpl) aclList.get(1);
         al = svc.getUserAccessLevel(
            new PSGuid(PSTypeEnum.valueOf(acl.getObjectType()), 
               acl.getObjectId()));
         assertFalse(al.hasDeleteAccess());
         assertTrue(al.hasReadAccess());
         assertTrue(al.hasRuntimeAccess());
         assertTrue(al.hasUpdateAccess());
         assertFalse(al.hasOwnerAccess());
         
         success = true;
      }
      finally
      {
         if (aclList != null)
         {
            try
            {
               PSAclServiceTest.deleteAcls(aclList);
            }
            catch (Exception e)
            {
               if (success)
                  throw (Exception)e.fillInStackTrace();
               else
               {
                  // just log so we don't mask real error
                  System.out.println("Error deleting test acls: " + 
                     e.getLocalizedMessage());
               }
            }
         }
      }
   }
}

