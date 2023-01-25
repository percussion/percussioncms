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

