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

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.IPSDirectoryCataloger;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.server.PSServer;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.services.security.data.PSCatalogerConfig.ConfigTypes;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

/**
 * Test case for the {@link IPSRoleMgr}.
 */
@Category(IntegrationTest.class)
public class PSRoleMgrTest extends ServletTestCase
{
   /**
    * Tests getting and setting cataloger configs.
    * 
    * @throws Exception If the test fails.
    */
   public void testConfigs() throws Exception
   {
      List<PSCatalogerConfig> curConfigs = null;
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      try
      {  
         curConfigs = roleMgr.getCatalogerConfigs();
         
         List<PSCatalogerConfig> configs = new ArrayList<PSCatalogerConfig>();
         roleMgr.saveCatalogerConfigs(configs);
         assertTrue(roleMgr.getCatalogerConfigs().isEmpty());
         
         PSCatalogerConfig config1 = new PSCatalogerConfig("Config1",
            ConfigTypes.SUBJECT, "com.percussion.test.PSTestConfig", "desc1",
            new HashMap<String, String>()); 
         configs.add(config1);
         
         roleMgr.saveCatalogerConfigs(configs);
         assertEquals(configs, roleMgr.getCatalogerConfigs());
         
         Map<String, String> props = new HashMap<String, String>();
         props.put("prop1", "val1");
         props.put("prop2", "val2");
         
         PSCatalogerConfig config2 = new PSCatalogerConfig("Config2",
            ConfigTypes.SUBJECT, "com.percussion.test.PSTestConfig", "desc2",
            props); 
         configs.add(config2);
         
         PSCatalogerConfig config3 = new PSCatalogerConfig("Config3",
            ConfigTypes.ROLE, "com.percussion.test.PSTestConfig", "desc3",
            props); 
         configs.add(config3);
         
         PSCatalogerConfig config4 = new PSCatalogerConfig("Config4",
            ConfigTypes.ROLE, "com.percussion.test.PSTestConfig", "desc4",
            new HashMap<String, String>()); 
         configs.add(config4);

         roleMgr.saveCatalogerConfigs(configs);
         assertEquals(configs, roleMgr.getCatalogerConfigs());
      }
      finally
      {
         if (curConfigs != null)
            roleMgr.saveCatalogerConfigs(curConfigs);
      }
   }
   
   /**
    * Test the fineUsers method.
    * 
    * @throws Exception if there are any errors
    */
   public void testFindUsers() throws Exception
   {
      IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
      
      List<String> names = new ArrayList<String>(); 
      List<Subject> users;
      
      // test all
      users = rolemgr.findUsers(null);
      assertTrue(!users.isEmpty());
      Set<Subject> allUsers = new HashSet<Subject>(users);
      
      // test name
      names.add("admin_");
      users = rolemgr.findUsers(names);
      assertTrue(users.size() == 2);
      for (Subject subject : users)
      {
         IPSTypedPrincipal p = PSJaasUtils.subjectToPrincipal(subject);
         assertNotNull(p);
         assertTrue(p.getName().startsWith("admin"));
         assertTrue(allUsers.contains(subject));
      }
      
      names.add("%1");
      users = rolemgr.findUsers(names);
      assertTrue(!users.isEmpty());
      for (Subject subject : users)
      {
         IPSTypedPrincipal p = PSJaasUtils.subjectToPrincipal(subject);
         assertNotNull(p);
         assertTrue(p.getName().startsWith("admin") || p.getName().endsWith(
            "1"));
         assertTrue(allUsers.contains(subject));
      }
      
      Set<Subject> testUserSet = new HashSet<Subject>();
      List<IPSDirectoryCataloger> dirCats = rolemgr.getDirectoryCatalogers();
      for (IPSDirectoryCataloger dirCat : dirCats)
      {
         testUserSet.addAll(rolemgr.findUsers(null, dirCat.getName(), 
            dirCat.getCatalogerType()));
      }
      assertEquals(allUsers, testUserSet);
   }

   /**
    * Class under test for Set<IPSTypedPrincipal> getRoleMembers(String)
    * 
    * @throws Exception If the test fails
    */
   public void testGetRoleMembersString() throws Exception
   {
      IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
      
      Set<IPSTypedPrincipal> members;
      members = rolemgr.getRoleMembers("Admin");
      assertTrue(!members.isEmpty());
      assertTrue(members.contains(PSTypedPrincipal.createSubject("admin1")));
      assertTrue(members.contains(PSTypedPrincipal.createSubject("admin2")));
      assertTrue(members.contains(PSTypedPrincipal.createSubject("rxserver")));
      assertTrue(!members.contains(PSTypedPrincipal.createSubject("author1")));
   }

   /**
    * Class under test for Set<IPSTypedPrincipal> getRoleMembers(String, 
    * IPSTypedPrincipal.PrincipalTypes)
    * 
    * @throws Exception if the test fails
    */
   public void testGetRoleMembersStringPrincipalTypes() throws Exception
   {
      IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
      
      Set<IPSTypedPrincipal> members;
      members = rolemgr.getRoleMembers("Author", 
         IPSTypedPrincipal.PrincipalTypes.SUBJECT);
      assertTrue(!members.isEmpty());
      assertTrue(members.contains(PSTypedPrincipal.createSubject("author1")));
      assertTrue(members.contains(PSTypedPrincipal.createSubject("author2")));
      assertTrue(!members.contains(PSTypedPrincipal.createSubject("admin1")));
      
      members = rolemgr.getRoleMembers("Author", 
         IPSTypedPrincipal.PrincipalTypes.GROUP);
      assertTrue(members.isEmpty());
   }

   /**
    * Test the getDefaultUserRoles method.
    * 
    * @throws Exception if there are any errors
    */
   public void testGetDefaultUserRoles() throws Exception
   {
      IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
      Set<String> roles;
      
      roles = rolemgr.getDefaultUserRoles(
         PSTypedPrincipal.createSubject("admin1"));
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Admin"));
   }

   /**
    * Test the getUserRoles method.
    * 
    * @throws Exception if there are any errors
    */
   public void testGetUserRoles() throws Exception
   {
      IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
      Set<String> roles;
      
      roles = rolemgr.getUserRoles(
         PSTypedPrincipal.createSubject("admin1"));
      assertTrue(!roles.isEmpty());
      assertTrue(roles.contains("Admin"));      
   }

   /**
    * Test the getUserGroups method.  
    * 
    *  FIXME: Disabled as the AD SERVER is not responding failing the build.
    * 
    * @throws Exception if there are any errors
    */
   public void fixmetestGetUserGroups() throws Exception
   {
      try
      {
         setUpDirSvcs();
         
         IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
         Set<Principal> groups = rolemgr.getUserGroups(
            PSTypedPrincipal.createGroup("sstatic_1"));
         assertTrue(!groups.isEmpty());
      }
      finally
      {
         tearDownDirSvcs();
      }
   }

   /**
    * Test the findGroups method.
    * 
    *  FIXME: Disabled as the AD SERVER is not responding failing the build.
    * 
    * @throws Exception if there are any errors
    */
   public void fixmetestFindGroups() throws Exception
   {
      try
      {
         setUpDirSvcs();
         
         IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
         List<Principal> groups = rolemgr.findGroups("static1", null, null);
         assertTrue(!groups.isEmpty());
         
         Set<Principal> testGroups = new HashSet<Principal>();
         List<IPSDirectoryCataloger> dirCats = rolemgr.getDirectoryCatalogers();         
         for (IPSDirectoryCataloger dirCat : dirCats)
         {
            testGroups.addAll(rolemgr.findGroups("static1", dirCat.getName(), 
               dirCat.getCatalogerType()));
         }     
         assertEquals(new HashSet<Principal>(groups), testGroups);
      }
      finally
      {
         tearDownDirSvcs();
      }      
   }

   /**
    * Test the getGroupMembers method.
    * 
    *  FIXME: Disabled as the AD SERVER is not responding failing the build.
    * 
    * @throws Exception if there are any errors
    */   
   public void fixmetestGetGroupMembers() throws Exception
   {
     try
      {
         setUpDirSvcs();
         
         Collection<IPSTypedPrincipal> groups = 
            new ArrayList<IPSTypedPrincipal>();
         groups.add(PSTypedPrincipal.createGroup("cn=static1,ou=groupstatic," +
               "ou=test,dc=percussion,dc=com"));
         
         IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
         List<IPSTypedPrincipal> members = rolemgr.getGroupMembers(groups);
         assertTrue(!members.isEmpty());
         
         assertTrue(members.contains(PSTypedPrincipal.createSubject(
            "sstatic_1")));
         assertTrue(members.contains(PSTypedPrincipal.createSubject(
            "sstatic_3")));
      }
      finally
      {
         tearDownDirSvcs();
      }      
   }

   /**
    * Test the supportsGroups method.
    * 
    * @throws Exception if there are any errors
    */
   public void testSupportsGroups() throws Exception
   {
      IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
      
      List<IPSDirectoryCataloger> dirCats = rolemgr.getDirectoryCatalogers();
      for (IPSDirectoryCataloger dirCat : dirCats)
      {
         assertFalse(rolemgr.supportsGroups(dirCat.getName(), 
            dirCat.getCatalogerType()));
      }
   }

   /**
    * Test the getDefinedRoles method.
    * 
    * @throws Exception if there are any errors
    */
   public void testGetDefinedRoles() throws Exception
   {
      IPSRoleMgr rolemgr = PSRoleMgrLocator.getRoleManager();
      List<String> roles = rolemgr.getDefinedRoles();
      assertTrue(roles.contains("Admin"));
      assertTrue(roles.contains("Author"));
   }

   /**
    * Re-initializes the security provider pool with a configuration that 
    * includes the test directory services setup.  Original configuration is
    * saved and may be restored using {@link #tearDownDirSvcs()}.
    * 
    * @throws Exception If there are any errors.
    */
   private void setUpDirSvcs() throws Exception
   {
      if (m_oldConfig != null)
         throw new IllegalStateException("must call tearDownDirSvcs first");
      
      Document doc;
      InputStream in = getClass().getResourceAsStream("test/config.xml");
      if (in == null)
         throw new RuntimeException("Failed to load test config");
      try
      {
         doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      finally
      {
         try
         {
            in.close();
         }
         catch (IOException e)
         {
         }
      }
      
      PSServerConfiguration newConfig = new PSServerConfiguration(doc);
      m_oldConfig = PSServer.getServerConfiguration();
      PSSecurityProviderPool.init(newConfig);
   }

   /**
    * Restores the configuration replaced by {@link #setUpDirSvcs()}.
    * 
    * @throws Exception if there are any errors.
    */
   private void tearDownDirSvcs() throws Exception
   {
      if (m_oldConfig != null)
      {
         PSSecurityProviderPool.init(m_oldConfig);
         m_oldConfig = null;
      }
   }

   /**
    * Stores the original config loaded during {@link #setUpDirSvcs()}
    */
   PSServerConfiguration m_oldConfig = null;
}

