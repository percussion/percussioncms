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
package com.percussion.services.security.impl;

import com.percussion.security.PSGroupEntry;
import com.percussion.security.PSRoleEntry;
import com.percussion.security.PSUserAttributes;
import com.percussion.security.PSUserEntry;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test the iterator used to determine a user's permissions in a given acl
 */
public class PSAclEntryImplIteratorTest extends TestCase
{
   /**
    * Test the iterator
    * 
    * @throws Exception if the test fails
    */
   public void testIterator() throws Exception
   {
      PSAclImpl testAcl = createTestAcl();
      
      PSUserEntry user;
      Set<String> comms = new HashSet<String>();
      user = createUser("admin1", new String[] {"Group1"}, null);
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.OWNER, 
         PSPermissions.READ, PSPermissions.RUNTIME_VISIBLE});
      
      user = createUser("admin1", new String[] {"Group2"}, 
         new String[] {"Admin"});
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.OWNER, PSPermissions.DELETE, PSPermissions.RUNTIME_VISIBLE});
      
      user = createUser("admin2", new String[] {"Group2"}, 
         new String[] {"Admin"});
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.UPDATE, 
         PSPermissions.READ, PSPermissions.DELETE, PSPermissions.RUNTIME_VISIBLE});
      
      user = createUser("admin3", new String[] {"Group2"}, 
         new String[] {"Admin"});
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.DELETE, PSPermissions.RUNTIME_VISIBLE});
      
      user = createUser("admin3", new String[] {"Group2"}, 
         new String[] {"Admin", "QA"});
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.UPDATE, PSPermissions.DELETE, PSPermissions.RUNTIME_VISIBLE});
      
      user = createUser("admin3", null, new String[] {"Editor"});
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.UPDATE, PSPermissions.RUNTIME_VISIBLE});

      user = createUser("admin3", null, new String[] {"Admin"});
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.DELETE, PSPermissions.RUNTIME_VISIBLE});

      comms.add("Test");
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.DELETE});
      
      user = createUser("admin3", null, new String[] {"Editor"});
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.UPDATE, PSPermissions.DELETE});
      
      comms.add("Test2");
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.UPDATE, PSPermissions.DELETE, PSPermissions.RUNTIME_VISIBLE});
      
      comms.clear();
      user = createUser("admin1", new String[] {"Group1", "Group2"}, null);
      checkPerms(testAcl, user, comms, new PSPermissions[] {PSPermissions.READ, 
         PSPermissions.DELETE, PSPermissions.OWNER, PSPermissions.RUNTIME_VISIBLE});
   }

   /**
    * Check that the supplied user has the expected permissions from the given
    * acl.
    * 
    * @param acl The acl specifying permissions for an object.
    * @param user The entry of the user to check.
    * @param comms The user's communities as a list of names.
    * @param permissions The expected permissions for the user.
    * 
    * @throws Exception if the test fails. 
    */
   private void checkPerms(PSAclImpl acl, PSUserEntry user, 
      Set<String> comms, PSPermissions[] permissions) throws Exception
   {
      Set<PSPermissions> expected = new HashSet<PSPermissions>();
      for (PSPermissions permission : permissions)
      {
         expected.add(permission);
      }
      
      Set<PSPermissions> perms = new HashSet<PSPermissions>();
      Iterator<IPSAclEntry> iterator = new PSAclEntryImplIterator(acl, 
         user, comms);
      while (iterator.hasNext())
      {
         PSAclEntryImpl entry = (PSAclEntryImpl) iterator.next();
         for (PSAccessLevelImpl level : entry.getPermissions())
         {
            perms.add(level.getPermission());
         }
      }
      
      assertEquals(expected, perms);
   }

   /**
    * Creates a user entry with the supplied info
    * 
    * @param name The user name, assumed not <code>null</code> or empty.
    * @param groupNames List of group names, may be <code>null</code> or empty.
    * @param roleNames List of role names, may be <code>null</code>.
    * 
    * @return The user entry, never <code>null</code>.
    */
   private PSUserEntry createUser(String name, String[] groupNames, String[] roleNames)
   {
      PSGroupEntry[] groups = null;
      if (groupNames != null)
      {
         groups = new PSGroupEntry[groupNames.length];
         for (int i = 0; i < groups.length; i++)
         {
            groups[i] = new PSGroupEntry(groupNames[i], 0);
         }
      }
      
      PSRoleEntry[] roles = null;
      if (roleNames != null)
      {
         roles = new PSRoleEntry[roleNames.length];
         for (int i = 0; i < roles.length; i++)
         {
            roles[i] = new PSRoleEntry(roleNames[i], 0);
         }
      }
      
      PSUserEntry user = new PSUserEntry(name, 0, groups, roles, 
         new PSUserAttributes(), "");
      
      
      return user;
   }

   /**
    * Create a test acl
    * 
    * @return The acl, never <code>null</code>.
    */
   private PSAclImpl createTestAcl()
   {
      PSAclEntryImpl owner = new PSAclEntryImpl(new PSTypedPrincipal("admin1",
         PrincipalTypes.USER));
      owner.addPermission(PSPermissions.OWNER);
     
      PSAclImpl acl = new PSAclImpl("testAcl1", owner);
      acl.setId(123);
      acl.setObjectId(456);
      acl.setObjectType(PSTypeEnum.TEMPLATE.getOrdinal());
      acl.setDescription("test");

      PSAclEntryImpl entry;
      entry = new PSAclEntryImpl();
      entry.setName("Default");
      entry.addPermission(PSPermissions.READ);
      entry.addPermission(PSPermissions.UPDATE);
      acl.addEntry(entry);
      
      entry = new PSAclEntryImpl();
      entry.setName("admin2");
      entry.addPermission(PSPermissions.UPDATE);
      acl.addEntry(entry);
      
      entry = new PSAclEntryImpl();
      entry.setName("Admin");
      entry.setType(PrincipalTypes.ROLE);
      entry.addPermission(PSPermissions.READ);
      entry.addPermission(PSPermissions.DELETE);
      acl.addEntry(entry);
      
      entry = new PSAclEntryImpl();
      entry.setName("QA");
      entry.setType(PrincipalTypes.ROLE);
      entry.addPermission(PSPermissions.UPDATE);
      acl.addEntry(entry);
      
      entry = new PSAclEntryImpl();
      entry.setName("Group1");
      entry.setType(PrincipalTypes.GROUP);
      entry.addPermission(PSPermissions.READ);
      acl.addEntry(entry);
      
      entry = new PSAclEntryImpl();
      entry.setName("Group2");
      entry.setType(PrincipalTypes.GROUP);
      entry.addPermission(PSPermissions.DELETE);
      acl.addEntry(entry);

      entry = new PSAclEntryImpl();
      entry.setName("AnyCommunity");
      entry.setType(PrincipalTypes.COMMUNITY);
      entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry(entry);
      
      entry = new PSAclEntryImpl();
      entry.setName("Test");
      entry.setType(PrincipalTypes.COMMUNITY);
      entry.addPermission(PSPermissions.DELETE);
      acl.addEntry(entry);
      
      entry = new PSAclEntryImpl();
      entry.setName("Test2");
      entry.setType(PrincipalTypes.COMMUNITY);
      entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry(entry);
      
      return acl;
   }
}

