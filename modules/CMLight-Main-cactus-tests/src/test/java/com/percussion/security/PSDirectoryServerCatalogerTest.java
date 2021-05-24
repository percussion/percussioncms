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
package com.percussion.security;

import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Directory object store class testing, including constructors,
 * <code>PSComponent</code> functionality, accessors and XML functionality.
 */
@Category(IntegrationTest.class)
public class PSDirectoryServerCatalogerTest extends ServletTestCase
{

   private static final Logger log = LogManager.getLogger(PSDirectoryServerCatalogerTest.class);

   public static final String DEFAULT_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

   /**
    * Constructor to call base class constructor.
    *
    * @see TestCase#TestCase(String) for more information.
    */
   public PSDirectoryServerCatalogerTest(String name)
   {
      super(name);
   }

   /**
    * Test component constructors and accessors.
    *
    * FIXME: Disabled as the AD SERVER is not responding failing the build.
    * 
    * @throws Exception if any exceptions occur or assertions fail.
    */
   public void fixmetestCataloging() throws Exception
   {
      PSServerConfiguration config = getServerConfig();
      
      String directorySetName = "e2srv - uid";
      
      Properties properties =  new Properties();
      properties.put(PSSecurityProvider.PROVIDER_NAME, 
         directorySetName);
         
      PSDirectoryServerCataloger cataloger = 
         new PSDirectoryServerCataloger(properties, config);
         
      String user = "admin1";
         
      // test getEmailAddress supported but not available
      boolean didThrow = false;
      String emailAddress = null;
      try
      {
         emailAddress = cataloger.getEmailAddress(user);
      }
      catch (UnsupportedOperationException e)
      {
         didThrow = true;
      }
      assertFalse(didThrow);
      assertTrue(emailAddress == null);
      
      // test getEmailAddress supported and available
      didThrow = false;
      emailAddress = null;
      try
      {
         emailAddress = cataloger.getEmailAddress("sshallow.1");
      }
      catch (UnsupportedOperationException e)
      {
         didThrow = true;
      }
      assertFalse(didThrow);
      assertTrue(emailAddress != null);
      
      // test get attributes
      List requestedAttributeNames = new ArrayList();
      requestedAttributeNames.add("cn");
      requestedAttributeNames.add("sn");
      requestedAttributeNames.add("givenname");
      requestedAttributeNames.add("mail");
      
      PSSubject subject = cataloger.getAttributes("sdeep.1", requestedAttributeNames);
      PSAttributeList attrs = subject.getAttributes();
      assertTrue(!attrs.getAttribute("cn").getValues().isEmpty());
      assertTrue(((String) attrs.getAttribute("cn").getValues().get(0)).equals("sun deep.1"));
      assertTrue(!attrs.getAttribute("sn").getValues().isEmpty());
      assertTrue(!attrs.getAttribute("givenname").getValues().isEmpty());
      assertTrue(!attrs.getAttribute("givenname").getValues().isEmpty());
      
      // test get users
      Collection users = cataloger.findUsers(null, null);
      assertTrue(!users.isEmpty());
      
      users = cataloger.findUsers(null, requestedAttributeNames);
      assertTrue(!users.isEmpty());
      
      PSTextLiteral name = new PSTextLiteral("cn");
      PSTextLiteral value = new PSTextLiteral("sun*.1");
      PSConditional[] criteria = new PSConditional[] {new PSConditional(name, 
         PSConditional.OPTYPE_EQUALS, value)};
      users = cataloger.findUsers(criteria, null);
      assertTrue(users.size() > 1);
      
      // test directory cataloger
      IPSDirectoryCataloger dirCataloger = 
         getDirectoryCataloger("e2srv - uid", config);
      users = dirCataloger.findUsers(criteria, null);
      assertTrue(users.size() > 1);
      
      assertEquals(dirCataloger.getAttribute("sshallow.1", "mail"), "sshallow.1@test.com");
      assertEquals(dirCataloger.getEmailAddress("sshallow.1"), "sshallow.1@test.com");
      
      // test multiples
      int count = 10000;
      criteria = new PSConditional[count];
      for (int i = 0; i < criteria.length; i++)
      {
         criteria[i] = new PSConditional(name, PSConditional.OPTYPE_EQUALS, 
            new PSTextLiteral("sun*." + i));
      }
      users = dirCataloger.findUsers(criteria, null);
      assertTrue(users.size() > 1);
   }
   
   /**
    * Tests group provider cataloging
    * 
    *  FIXME: Disabled as the AD SERVER is not responding failing the build.
    * 
    * @throws Exception if the test fails
    */
   public void fixmetestGroupCataloger() throws Exception
   {
      PSServerConfiguration config = getServerConfig();
      IPSDirectoryCataloger dirCat = getDirectoryCataloger("e2srv - uid-groups",
         config);
      
      Iterator gps = dirCat.getGroupProviders();
      assertTrue(gps.hasNext());
      
      Collection<IPSTypedPrincipal> groups = new ArrayList<IPSTypedPrincipal>();
      groups.add(PSTypedPrincipal.createGroup("cn=static1,ou=groupstatic," +
            "ou=test,dc=percussion,dc=com"));
      Collection members = new ArrayList();
      
      gps = dirCat.getGroupProviders();
      while (gps.hasNext())
      {
         IPSGroupProvider gp = (IPSGroupProvider) gps.next();
         members.addAll(gp.getGroupMembers(groups));
      }
      assertTrue(!members.isEmpty());
      assertTrue(members.contains(PSTypedPrincipal.createSubject("sstatic_1")));
      assertTrue(members.contains(PSTypedPrincipal.createSubject("sstatic_3")));

/*      
 *    Commented out after moving to OpenLDAP which does not support dynamic groups.  Leaving code in case we need to 
 *    test again someday
 */
//      groups.clear();
//      groups.add(PSTypedPrincipal.createGroup("cn=dynamic1,ou=groupdynamic," +
//            "ou=test,dc=percussion,dc=com"));
//      members.clear();
//      gps = dirCat.getGroupProviders();
//      while (gps.hasNext())
//      {
//         IPSGroupProvider gp = (IPSGroupProvider) gps.next();
//         members.addAll(gp.getGroupMembers(groups));
//      }
//      assertTrue(!members.isEmpty());
//      assertTrue(members.contains(PSTypedPrincipal.createSubject(
//         "sdynamic_1")));

      
//    userGroups.clear();
//    gps = dirCat.getGroupProviders();
//    while (gps.hasNext())
//    {
//       IPSGroupProvider gp = (IPSGroupProvider) gps.next();
//       userGroups.addAll(gp.getUserGroups("sdynamic_1"));
//    }
//    assertTrue(userGroups.contains("cn=dynamic1,ou=groupdynamic," +
//          "ou=test,dc=percussion,dc=com"));
      
      groups.clear();
      groups.add(PSTypedPrincipal.createGroup("cn=nested1,ou=groupnested," +
            "ou=test,dc=percussion,dc=com"));
      members.clear();
      gps = dirCat.getGroupProviders();
      while (gps.hasNext())
      {
         IPSGroupProvider gp = (IPSGroupProvider) gps.next();
         members.addAll(gp.getGroupMembers(groups));
      }
      assertTrue(!members.isEmpty());
      assertTrue(members.contains(PSTypedPrincipal.createSubject(
         "snested_1")));
      
      
      Collection<String> userGroups = new ArrayList();
      gps = dirCat.getGroupProviders();
      while (gps.hasNext())
      {
         IPSGroupProvider gp = (IPSGroupProvider) gps.next();
         userGroups.addAll(gp.getUserGroups("sstatic_1"));
      }
      assertTrue(userGroups.contains("cn=static1,ou=groupstatic," +
         "ou=test,dc=percussion,dc=com"));

      
      userGroups.clear();
      gps = dirCat.getGroupProviders();
      while (gps.hasNext())
      {
         IPSGroupProvider gp = (IPSGroupProvider) gps.next();
         userGroups.addAll(gp.getUserGroups("snested_1"));
      }
      assertTrue(userGroups.contains("cn=nested1,ou=groupnested," +
            "ou=test,dc=percussion,dc=com"));
      
      Collection<String> groupNames = new ArrayList<String>();
      gps = dirCat.getGroupProviders();
      while (gps.hasNext())
      {
         IPSGroupProvider gp = (IPSGroupProvider) gps.next();
         groupNames.addAll(gp.getGroups("%1"));
      }
      assertTrue(groupNames.contains("cn=static1,ou=groupstatic," +
         "ou=test,dc=percussion,dc=com"));
//      assertTrue(groupNames.contains("cn=dynamic1,ou=groupdynamic," +
//         "ou=test,dc=percussion,dc=com"));
      assertTrue(groupNames.contains("cn=nested1,ou=groupnested," +
         "ou=test,dc=percussion,dc=com"));
   }

   /**
    * Get the directory cataloger for the supplied refererence from the server 
    * configuration.
    * 
    * @param providerRef the directory cataloger reference name, assumed not 
    *    <code>null</code> or empty.
    * @param config the server cconfiguration from which to get the directory 
    *    cataloger, assumed not <code>null</code>.
    * @return the directory cataloger if found, <code>null</code> otherwise.
    */
   private IPSDirectoryCataloger getDirectoryCataloger(String providerRef, 
      PSServerConfiguration config)
   {
      PSReference ref = new PSReference(providerRef, 
         PSDirectorySet.class.getName());
      PSProvider provider = new PSProvider(
         PSDirectoryServerCataloger.class.getName(), 
         PSProvider.TYPE_DIRECTORY, ref);
         
      Properties properties = new Properties();
      properties.put(PSSecurityProvider.PROVIDER_NAME, 
         provider.getReference().getName());
         
      return (IPSDirectoryCataloger) PSSecurityProvider.instantiateProvider(
         provider, properties, config);
   }
   
   /**
    * Test directory server bindings.
    *
    * @throws Exception if any exceptions occur or assertions fail.
    */
   public void fixmetestBindings() throws Exception
   {
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CTX_FACTORY);
      env.put(Context.PROVIDER_URL, 
         "ldap://ad2008:390/ou=test,dc=percussion,dc=com");

      // Authenticate with the specified uid/pw
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, "cn=Manager");
      env.put(Context.SECURITY_CREDENTIALS, "P3rcuss1on");

      DirContext context = null;
      try
      {
         context = new InitialDirContext(env);
         
         NamingEnumeration bindings = context.listBindings("ou=subjects");
         while (bindings.hasMore())
         {
            Binding binding = (Binding) bindings.next();
            System.out.println(binding.getName() + ":" + binding.getClassName());
         }
      }
      catch (NamingException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
      }
      finally
      {
         if (context != null)
            try { context.close(); } catch (NamingException e) { /* noop */ };
      }
   }
   
   /**
    * Test directory server searches.
    *
    * @throws Exception if any exceptions occur or assertions fail.
    */
   public void fixmetestSearches() throws Exception
   {
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CTX_FACTORY);
      env.put(Context.PROVIDER_URL, 
         "ldap://e2srv:390/o=rhythmyx,dc=percussion,dc=com");

      // Authenticate with the specified uid/pw
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, "cn=Directory Manager");
      env.put(Context.SECURITY_CREDENTIALS, "ldapadmin");

      DirContext context = null;
      try
      {
         context = new InitialDirContext(env);
         
         SearchControls searchControls = new SearchControls();
         searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
         
         NamingEnumeration results = 
            context.search("", "(&(uid=*)(!(mail=*)))", searchControls);
         while (results.hasMore())
         {
            Object result = results.next();
            System.out.println(result.toString());
         }
      }
      catch (NamingException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
      }
      finally
      {
         if (context != null)
            try { context.close(); } catch (NamingException e) { /* noop */ };
      }
   }
   
   /**
    * Tests authentication.
    *
    * @throws Exception If any unexpected errors occur
    * 
    * @todo renaming to avoid running, cannot succeed as implemented
    */
   public void doNotTestAuthentication() throws Exception
   {
      PSServerConfiguration config = getServerConfig();

//      PSReference directorySetRef = new PSReference(
//         "E2 Server - Netscape/Rhythmyx - uid", PSDirectorySet.class.getName());
//      PSProvider directoryProvider = new PSProvider(
//         PSDirectoryServerCataloger.class.getName(), PSProvider.TYPE_DIRECTORY,
//         directorySetRef);
//            
//      Properties properties = new Properties();
//      properties.put(PSSecurityProvider.PROVIDER_NAME, directorySetRef.getName());
//
//      PSDirectoryConnProvider provider = new PSDirectoryConnProvider(
//         new Properties(), "testProvider");
//      provider.setDirectoryProvider(directoryProvider, properties, config);
//
      PSReference directorySetRef2 = new PSReference(
         "E2 Server - Netscape/Rhythmyx - cn", PSDirectorySet.class.getName());
      PSProvider directoryProvider2 = new PSProvider(
         PSDirectoryServerCataloger.class.getName(), PSProvider.TYPE_DIRECTORY,
         directorySetRef2);
            
      Properties properties2 = new Properties();
      properties2.put(PSSecurityProvider.PROVIDER_NAME, 
         "E2 Server - Netscape/Rhythmyx - cn");

      PSDirectoryConnProvider provider2 = new PSDirectoryConnProvider(
         new Properties(), "testProvider2");
      provider2.setDirectoryProvider(directoryProvider2, properties2, config);
      PSSecurityProviderPool.init(config);
      PSSecurityProvider provider = PSSecurityProviderPool.getProvider(
         "e2srv - uid");
      
      boolean didAuthenticate = true;
      try
      {
         provider.authenticate("admin1", "demo", null);
      }
      catch (PSAuthenticationFailedException e)
      {
         didAuthenticate = false;
      }
      assertTrue("didAuthenticate", didAuthenticate);

      // test bad password
      didAuthenticate = true;
      try
      {
         provider.authenticate("admin1", "badPass", null);
      }
      catch (PSAuthenticationFailedException e)
      {
         didAuthenticate = false;
      }
      assertTrue("!didAuthenticate: badPass", !didAuthenticate);

      // test using 'cn' instead of 'uid' as principal attribute
      didAuthenticate = true;
      try
      {
         provider2.authenticate("Admin 3", "demo", null);
      }
      catch (PSAuthenticationFailedException e)
      {
         didAuthenticate = false;
      }
      assertTrue("didAuthenticate: using cn=", didAuthenticate);
      
      didAuthenticate = true;
      try
      {
         provider2.authenticate("Admin 3", "badPass", null);
      }
      catch (PSAuthenticationFailedException e)
      {
         didAuthenticate = false;
      }
      assertTrue("!didAuthenticate: using cn= badPass", !didAuthenticate);
   }
   
   /**
    * Collect all tests into a TestSuite and return it.
    *
    * @return The suite of test methods for this class.  Not <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();

//      suite.addTest(new PSDirectoryServerCatalogerTest("testAuthentication"));
//      suite.addTest(new PSDirectoryServerCatalogerTest("testCataloging"));
//      suite.addTest(new PSDirectoryServerCatalogerTest("testBindings"));
//      suite.addTest(new PSDirectoryServerCatalogerTest("testGroupCataloger"));
//      suite.addTest(new PSDirectoryServerCatalogerTest("testSearches"));

      return suite;
   }
   
   /**
    * Reads the server configuration XML from the unit test resources and adds
    * all required directory server definitions.
    * 
    * @return a valid server configuration with all required directory server 
    *    definitions, never <code>null<code>.
    * @throws Exception for any errors.
    */
   public static PSServerConfiguration getServerConfig() throws Exception
   {
      // read test server configuration
      InputStream input = new FileInputStream(RESOURCE_PATH + "config.xml");
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
         new InputStreamReader(input), false);
      input.close();
      PSServerConfiguration config = new PSServerConfiguration(doc);

      // create all objects used for the directory cataloger
//      String authenticationName = "E2 Server - Netscape";
//      PSAuthentication authentication = new PSAuthentication(
//         authenticationName, PSAuthentication.SCHEME_SIMPLE, 
//         "Directory Manager", "cn", "ldapadmin", null);
//
//      String directoryName = "E2 Server - Netscape/Rhythmyx";
//      PSDirectory directory = new PSDirectory(directoryName, 
//         PSDirectory.CATALOG_DEEP, DEFAULT_CTX_FACTORY, 
//         authenticationName, "ldap://e2srv:390/o=rhythmyx,dc=Percussion,dc=com", 
//         null);
      PSReference directoryRef = new PSReference("deep",
         PSDirectory.class.getName());

//      String directorySetName = "E2 Server - Netscape/Rhythmyx - uid";
//      PSDirectorySet directorySet = new PSDirectorySet(directorySetName, "uid");
//      directorySet.addDirectoryRef(directoryRef);

      String directorySetName2 = "E2 Server - Netscape/Rhythmyx - cn";
      PSDirectorySet directorySet2 = new PSDirectorySet(directorySetName2, "cn");
      directorySet2.addDirectoryRef(directoryRef);
      
      // and add them to the server configuration
//      config.addAuthentication(authentication);
//      config.addDirectory(directory);
//      config.addDirectorySet(directorySet);
      config.addDirectorySet(directorySet2);
      
      // create role providers
//      String roleProviderName = "E2 Server - Netscape/Rhythmyx - uid";
//      PSReference directorySetRef = new PSReference(directorySetName,
//         PSDirectorySet.class.getName());
//      PSRoleProvider roleProvider = new PSRoleProvider(roleProviderName, 
//         PSRoleProvider.TYPE_DIRECTORY, directorySetRef);
      
      String roleProviderName2 = "E2 Server - Netscape/Rhythmyx - cn";
      PSReference directorySetRef2 = new PSReference(directorySetName2,
         PSDirectorySet.class.getName());
      PSRoleProvider roleProvider2 = new PSRoleProvider(roleProviderName2, 
         PSRoleProvider.TYPE_DIRECTORY, directorySetRef2);
         
      // and add them to the server configuration
//      config.addRoleProvider(roleProvider);
      config.addRoleProvider(roleProvider2);

      return config;
   }

   /**
    * Defines the path to the files used by this unit test, relative from the
    * rhythmyx root.
    */
   private static final String RESOURCE_PATH =
      "UnitTestResources/com/percussion/security/";
}
