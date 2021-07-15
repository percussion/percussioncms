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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore;

import com.percussion.security.PSBackEndTableDirectoryCataloger;
import com.percussion.security.PSJndiProvider;
import com.percussion.security.PSSecurityProvider;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Unit tests for the PSServerConfiguration class.
 */
public class PSServerConfigurationTest extends TestCase
{
   public PSServerConfigurationTest(String name)
   {
      super(name);
   }

   /**
    * Expose the protected empty ctor for use by other unit test classes.
    *
    * @return a newly constructed instance of an empty server configuration
    */
   public static PSServerConfiguration createServerConfig()
   {
      return new PSServerConfiguration();
   }


   public void testConstructors() throws Exception
   {
      PSServerConfiguration conf = new PSServerConfiguration();
      PSServerConfiguration otherConf = new PSServerConfiguration();
      assertEquals(conf, otherConf);
   }

   /**
    * Make sure that toXml() and fromXml() are consistent with equals.
    * This means that if you construct an object A, then call A.toXml
    * (producing a document ADoc), and then you create a new object B
    * and call B.fromXml(ADoc) on it with the document, A.equals(B)
    * should return true.
    * <p>
    * This means also that fromXml() should NOT create instances of
    * child objects unless their elements exist in the document. Likewise,
    * toXml() should NOT put elements in the document for objects that
    * are null.
    *
    * @author   chad loder
    *
    * @version 1.0 1999/8/25
    *
    *
    * @throws   Exception
    *
    */
   public void testToFromXml() throws Exception
   {
      PSServerConfiguration a = new PSServerConfiguration();
      PSServerConfiguration b = new PSServerConfiguration();

      a.setNotifier(new PSNotifier(PSNotifier.MP_TYPE_SMTP, "E2"));
      xmlEq(a, b);

      a.setRunningLogDays(101);
      xmlEq(a, b);

      a.setRequestRoot("/foobar");
      xmlEq(a, b);

      a.setUserSessionEnabled(!a.isUserSessionEnabled());
      xmlEq(a, b);

      a.setUserSessionTimeout(117);
      xmlEq(a, b);

      List<PSJdbcDriverConfig> driverConfigs = 
         new ArrayList<PSJdbcDriverConfig>();
      a.setJdbcDriverConfigs(driverConfigs);
      xmlEq(a, b);
      assertEquals(driverConfigs, a.getJdbcDriverConfigs());
      
      driverConfigs.add(new PSJdbcDriverConfig("driver1", "class1", 
         "typeMap1"));
      
      a.setJdbcDriverConfigs(driverConfigs);
      xmlEq(a, b);
      assertEquals(driverConfigs, a.getJdbcDriverConfigs());
      
      driverConfigs.add(new PSJdbcDriverConfig("driver2", "class2", 
         "typeMap2"));
      driverConfigs.add(new PSJdbcDriverConfig("driver3", "class3", 
         "typeMap3"));
      
      a.setJdbcDriverConfigs(driverConfigs);
      xmlEq(a, b);
      assertEquals(driverConfigs, a.getJdbcDriverConfigs());
   }

   /**
    * Tests to be sure security providers and group providers are acurately
    * stored and serialized to and from Xml.
    *
    * @throws Exception if there are any errors.
    */
   public void testSecurityProviders() throws Exception
   {
      PSServerConfiguration cfg1 = new PSServerConfiguration();
      
      PSCollection secProviders = new PSCollection(
         PSSecurityProviderInstance.class);
      PSSecurityProviderInstance secProv1 = new PSSecurityProviderInstance(
         "test1", PSSecurityProvider.SP_TYPE_DIRCONN);
      Properties props1 = new Properties();
      props1.setProperty(PSJndiProvider.PROPS_AUTH_PRINCIPAL, "cn");
      props1.setProperty(PSJndiProvider.PROPS_AUTH_SCHEME, "Simple");
      props1.setProperty(PSJndiProvider.PROPS_PROVIDER_CLASS_NAME,
         "pkg.myClass1");
      secProv1.setProperties(props1);
      PSReference ref1 = new PSReference("ref1", 
         PSDirectorySet.class.getName());
      PSProvider directoryProvider = new PSProvider(
         PSBackEndTableDirectoryCataloger.class.getName(), 
         PSProvider.TYPE_DIRECTORY, ref1);
      secProv1.setDirectoryProvider(directoryProvider);
      secProviders.add(secProv1);
      cfg1.setSecurityProviderInstances(secProviders);
   }

   /**
    * Test all public authentication container functionality.
    *
    * @throws Exception for any errors or failures during this test.
    */
   public void testAuthentications() throws Exception
   {
      PSServerConfiguration config = new PSServerConfiguration();

      PSAuthentication auth1 = new PSAuthentication("auth1",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw", "filter");

      PSAuthentication auth2 = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw", "filter");

      PSAuthentication auth3 = new PSAuthentication("auth3",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw", "filter");

      PSAuthentication auth2replacement = new PSAuthentication("auth2",
         PSAuthentication.SCHEME_SIMPLE, "user", "userAttr", "pw", null);

      // authentications must be empty
      assertTrue(!config.getAuthentications().hasNext());

      // test public interface
      boolean didThrow = false;
      try
      {
         config.getAuthentication(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.getAuthentication(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.addAuthentication(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeAuthentication(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeAuthentication(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // test add/replace
      PSAuthentication replaced = config.addAuthentication(auth1);
      assertTrue(replaced == null);
      replaced = config.addAuthentication(auth2);
      assertTrue(replaced == null);
      replaced = config.addAuthentication(auth3);
      assertTrue(replaced == null);
      replaced = config.addAuthentication(auth2replacement);
      assertTrue(replaced.equals(auth2));

      // test remove
      PSAuthentication removed = config.removeAuthentication("notKnown");
      assertTrue(removed == null);
      removed = config.removeAuthentication("auth2");
      assertTrue(removed.equals(auth2replacement));

      // test toXml/fromXml
      System.out.println("config:\n" +
         PSXmlDocumentBuilder.toString(config.toXml()));
      PSServerConfiguration config1 = new PSServerConfiguration(config.toXml());
      System.out.println("config1( config from XML ):\n" +
         PSXmlDocumentBuilder.toString(config1.toXml()));
      assertTrue(config.equals(config1));
   }

   /**
    * Test all public directory container functionality.
    *
    * @throws Exception for any errors or failures during this test.
    */
   public void testDirectories() throws Exception
   {
      PSServerConfiguration config = new PSServerConfiguration();

      PSDirectory dir1 = new PSDirectory("dir1", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PSDirectoryTest.PROVIDER_URL,
         PSDirectoryTest.ms_attributes);

      PSDirectory dir2 = new PSDirectory("dir2", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PSDirectoryTest.PROVIDER_URL,
         PSDirectoryTest.ms_attributes);

      PSDirectory dir3 = new PSDirectory("dir3", PSDirectory.CATALOG_SHALLOW,
         "factory", "auth", PSDirectoryTest.PROVIDER_URL,
         PSDirectoryTest.ms_attributes);

      PSDirectory dir2replacement = new PSDirectory("dir2",
         PSDirectory.CATALOG_SHALLOW, "factory", "auth",
         PSDirectoryTest.PROVIDER_URL, null);

      List<String> groups1 = new ArrayList<String>();
      groups1.add("Group1");
      groups1.add("Group2");

      // directories must be empty
      assertTrue(!config.getDirectories().hasNext());

      
      // test public interface
      boolean didThrow = false;
      try
      {
         config.getDirectory(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.getDirectory(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.addDirectory(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeDirectory(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeDirectory(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // test add/replace
      PSDirectory replaced = config.addDirectory(dir1);
      assertTrue(replaced == null);
      replaced = config.addDirectory(dir2);
      assertTrue(replaced == null);
      replaced = config.addDirectory(dir3);
      assertTrue(replaced == null);
      replaced = config.addDirectory(dir2replacement);
      assertTrue(replaced.equals(dir2));

      // test remove
      PSDirectory removed = config.removeDirectory("notKnown");
      assertTrue(removed == null);
      removed = config.removeDirectory("dir2");
      assertTrue(removed.equals(dir2replacement));

      // test toXml/fromXml
      System.out.println("config:\n" +
         PSXmlDocumentBuilder.toString(config.toXml()));
      PSServerConfiguration config1 = new PSServerConfiguration(config.toXml());
      System.out.println("config1( config from XML ):\n" +
         PSXmlDocumentBuilder.toString(config1.toXml()));
      assertTrue(config.equals(config1));
      
      // test group providers
      dir1.setGroupProviderNames(groups1.iterator());
      PSCollection grpProviders = config.getGroupProviderInstances();
      PSGroupProviderInstance group1 = new PSJndiGroupProviderInstance(
         "Group1", PSSecurityProvider.SP_TYPE_DIRCONN);
      PSGroupProviderInstance group2 = new PSJndiGroupProviderInstance(
         "Group2", PSSecurityProvider.SP_TYPE_DIRCONN);
      grpProviders.add(group1);
      grpProviders.add(group2);

      Document doc = config.toXml();
      System.out.println("config 1:\n" +
         PSXmlDocumentBuilder.toString(config.toXml()));
      config1 = new PSServerConfiguration(config.toXml());
      System.out.println("config 2:\n" +
         PSXmlDocumentBuilder.toString(config1.toXml()));
      assertEquals("to/fromXml not equal", config, config1);

      // now test extra group
      PSGroupProviderInstance group3 = new PSJndiGroupProviderInstance(
         "Group3", PSSecurityProvider.SP_TYPE_DIRCONN);
      config1.setGroupProviderInstance(group3);
      assertTrue("equals should return false if group providers different",
         !config.equals(config1));
      doc = config1.toXml();
      config1.fromXml(doc);
      assertEquals("unused group provider not removed", config, config1);      
   }

   /**
    * Test all public directory set container functionality.
    *
    * @throws Exception for any errors or failures during this test.
    */
   public void testDirectorySets() throws Exception
   {
      PSServerConfiguration config = new PSServerConfiguration();

      PSDirectorySet dirSet1 = new PSDirectorySet("dirSet1", "uid");
      dirSet1.add(PSDirectorySetTest.ms_ref1);

      PSDirectorySet dirSet2 = new PSDirectorySet("dirSet2", "uid");
      dirSet2.add(PSDirectorySetTest.ms_ref2);

      PSDirectorySet dirSet3 = new PSDirectorySet("dirSet3", "uid");
      dirSet3.add(PSDirectorySetTest.ms_ref1);
      dirSet3.add(PSDirectorySetTest.ms_ref2);

      PSDirectorySet dirSet2replacement = new PSDirectorySet("dirSet2", "uid");
      dirSet2replacement.add(PSDirectorySetTest.ms_ref1);
      dirSet2replacement.add(PSDirectorySetTest.ms_ref2);

      // directories must be empty
      assertTrue(!config.getDirectorySets().hasNext());

      // test public interface
      boolean didThrow = false;
      try
      {
         config.getDirectorySet(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.getDirectorySet(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.addDirectorySet(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeDirectorySet(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeDirectorySet(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // test add/replace
      PSDirectorySet replaced = config.addDirectorySet(dirSet1);
      assertTrue(replaced == null);
      replaced = config.addDirectorySet(dirSet2);
      assertTrue(replaced == null);
      replaced = config.addDirectorySet(dirSet3);
      assertTrue(replaced == null);
      replaced = config.addDirectorySet(dirSet2replacement);
      assertTrue(replaced.equals(dirSet2));

      // test remove
      PSDirectorySet removed = config.removeDirectorySet("notKnown");
      assertTrue(removed == null);
      removed = config.removeDirectorySet("dirSet2");
      assertTrue(removed.equals(dirSet2replacement));

      // test toXml/fromXml
      System.out.println("config:\n" +
         PSXmlDocumentBuilder.toString(config.toXml()));
      PSServerConfiguration config1 = new PSServerConfiguration(config.toXml());
      System.out.println("config1( config from XML ):\n" +
         PSXmlDocumentBuilder.toString(config1.toXml()));
      assertTrue(config.equals(config1));
   }

   /**
    * Test all public role provider container functionality.
    *
    * @throws Exception for any errors or failures during this test.
    */
   public void testRoleProviders() throws Exception
   {
      PSServerConfiguration config = new PSServerConfiguration();

      PSRoleProvider prov1 = new PSRoleProvider("prov1",
         PSRoleProvider.TYPE_DIRECTORY, "directoryRef");

      PSRoleProvider prov2 = new PSRoleProvider("prov2",
         PSRoleProvider.TYPE_DIRECTORY, "directoryRef");

      PSRoleProvider prov3 = new PSRoleProvider("prov3",
         PSRoleProvider.TYPE_DIRECTORY, "directoryRef");

      PSRoleProvider prov2replacement = new PSRoleProvider("prov2",
         PSRoleProvider.TYPE_BACKEND, (String) null);

      // directories must be empty
      assertTrue(!config.getRoleProviders().hasNext());

      // test public interface
      boolean didThrow = false;
      try
      {
         config.getRoleProvider(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.getRoleProvider(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.addRoleProvider(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeRoleProvider(null);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      didThrow = false;
      try
      {
         config.removeRoleProvider(" ");
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      // test add/replace
      PSRoleProvider replaced = config.addRoleProvider(prov1);
      assertTrue(replaced == null);
      replaced = config.addRoleProvider(prov2);
      assertTrue(replaced == null);
      replaced = config.addRoleProvider(prov3);
      assertTrue(replaced == null);
      replaced = config.addRoleProvider(prov2replacement);
      assertTrue(replaced.equals(prov2));

      // test remove
      PSRoleProvider removed = config.removeRoleProvider("notKnown");
      assertTrue(removed == null);
      removed = config.removeRoleProvider("prov2");
      assertTrue(removed.equals(prov2replacement));

      // test toXml/fromXml
      System.out.println("config:\n" +
         PSXmlDocumentBuilder.toString(config.toXml()));
      PSServerConfiguration config1 = new PSServerConfiguration(config.toXml());
      System.out.println("config1( config from XML ):\n" +
         PSXmlDocumentBuilder.toString(config1.toXml()));
      assertTrue(config.equals(config1));
   }

   protected void xmlEq(PSServerConfiguration a, PSServerConfiguration b)
      throws Exception
   {
      assertTrue(a.isModified());
      Document doc = a.toXml();
      b.fromXml(doc);
      assertTrue(b.isModified());
      assertEquals(a, b);
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSServerConfigurationTest("testConstructors"));
      suite.addTest(new PSServerConfigurationTest("testToFromXml"));
      suite.addTest(new PSServerConfigurationTest("testSecurityProviders"));
      suite.addTest(new PSServerConfigurationTest("testAuthentications"));
      suite.addTest(new PSServerConfigurationTest("testDirectories"));
      suite.addTest(new PSServerConfigurationTest("testDirectorySets"));
      suite.addTest(new PSServerConfigurationTest("testRoleProviders"));

      return suite;
   }
}
