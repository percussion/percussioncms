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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

/**
 * Unit test for the {@link PSRelationship} class.
 */
public class PSRelationshipTest extends TestCase
{
   /**
    * The all public constructor contracts.
    * 
    * @throws Exception for any error.
    */
   public void testConstructors() throws Exception
   {
      PSRelationshipConfigSet configs = getConfigs();
      
      PSLocator owner = new PSLocator(300, 1);
      PSLocator dependent = new PSLocator(301, -1);
         
      // test constuctor 1: all valid parameters
      Exception exception = null;
      PSRelationship rs = null;
      // avoid eclipse warning
      if (rs == null);
      try
      {
         rs = new PSRelationship(1, owner, dependent, 
            configs.getConfig(PSRelationshipConfig.TYPE_TRANSLATION));
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);
         
      // test constuctor 1: invalid owner
      exception = null;
      rs = null;
      try
      {
         rs = new PSRelationship(1, null, dependent, 
            configs.getConfig(PSRelationshipConfig.TYPE_TRANSLATION));
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
         
      // test constuctor 1: invalid dependent
      exception = null;
      rs = null;
      try
      {
         rs = new PSRelationship(1, owner, null, 
            configs.getConfig(PSRelationshipConfig.TYPE_TRANSLATION));
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
         
      // test constuctor 1: invalid config
      exception = null;
      rs = null;
      try
      {
         rs = new PSRelationship(1, owner, dependent, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      PSRelationship test = new PSRelationship(1, owner, dependent, 
         configs.getConfig(PSRelationshipConfig.TYPE_NEW_COPY));
         
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element testXml = test.toXml(doc);

      // test constuctor 2: invalid source
      exception = null;
      rs = null;
      try
      {
         rs = new PSRelationship(null, null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof PSUnknownNodeTypeException);

      // test constuctor 3: all valid parameters
      exception = null;
      rs = null;
      try
      {
         rs = new PSRelationship(testXml, null, null, 
            configs.getConfig(PSRelationshipConfig.TYPE_NEW_COPY));
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);

      // test constuctor 3: invalid source
      exception = null;
      rs = null;
      try
      {
         rs = new PSRelationship(null, null, null, 
            configs.getConfig(PSRelationshipConfig.TYPE_NEW_COPY));
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof PSUnknownNodeTypeException);
   }
   
   /**
    * Test public API contracts.
    * 
    * @throws Exception for all errors.
    */
   public void testPublicAPI() throws Exception
   {
      PSRelationshipConfigSet configs = getConfigs();
         
      PSLocator owner = new PSLocator(300, 1);
      PSLocator dependent = new PSLocator(301, -1);
            
      PSRelationship rs = new PSRelationship(1, owner, dependent, 
         configs.getConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY));
      
      Exception exception = null;
      try
      {
         rs.copyFrom(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      exception = null;
      try
      {
         rs.getProperty(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      exception = null;
      try
      {
         rs.setDependent(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      exception = null;
      try
      {
         rs.setOwner(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      // Cannot set system properties through PSRelationship object
      exception = null;
      try
      {
         rs.setProperty(PSRelationshipConfig.RS_ALLOWCLONING, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertFalse(exception == null);
      
      exception = null;
      try
      {
         rs.setProperty(null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
   }
   
   /**
    * Get the relationship configuration set used for testing purposes.
    * 
    * @return the relationship configuration set, never <code>null</code> or
    *    empty.
    * @throws Exception for any error reading the relationship configuration
    *    set.
    */
   public static PSRelationshipConfigSet getConfigs() throws Exception
   {
      if (ms_configs == null)
      {
         ms_configs = PSRelationshipConfigTest.getConfigs();
      }
      
      return ms_configs;
   }
   
   /**
    * Test to & from XML API
    * 
    * @throws Exception if an error occurs.
    */
   public void testToFromXml() throws Exception
   {
      PSRelationshipConfigSet configs = getConfigs();
      
      // Testing relationships with EMPTY user properties
      PSLocator owner = new PSLocator(300, 1);
      PSLocator dependent = new PSLocator(301, -1);
         
      PSRelationship rs_1 = new PSRelationship(1, owner, dependent, 
            configs.getConfig(PSRelationshipConfig.TYPE_TRANSLATION));
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element rsEl = rs_1.toXml(doc);
      PSRelationship rs_2 = new PSRelationship(rsEl, null, null, configs
            .getConfig(PSRelationshipConfig.TYPE_TRANSLATION));
      
      assertTrue(rs_1.equals(rs_2));
      rs_1.resetId();
      assertFalse(rs_1.equals(rs_2));
      
      // Testing relationships with NONE-EMPTY user properties
      rs_1 = new PSRelationship(1, owner, dependent, 
            configs.getConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY));
      rs_1.setProperty(PSRelationshipConfig.PDU_FOLDERID, "111");
      rs_1.setProperty(PSRelationshipConfig.PDU_SITEID, "222");
      rs_1.setProperty(PSRelationshipConfig.PDU_VARIANTID, "333");
      rs_1.setProperty(PSRelationshipConfig.PDU_SLOTID, "444");
      rs_1.setProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP, "body");
      
      doc = PSXmlDocumentBuilder.createXmlDocument();
      rsEl = rs_1.toXml(doc);
      rs_2 = new PSRelationship(rsEl, null, null, 
            configs.getConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY));
      
      assertTrue(rs_1.equals(rs_2));
      rs_1.resetId();
      assertFalse(rs_1.equals(rs_2));
      
   }
   
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSLocator owner = new PSLocator();
      final PSLocator dependent = new PSLocator();
      final PSRelationshipConfig config =
         new PSRelationshipConfig("name", PSRelationshipConfig.RS_TYPE_SYSTEM);
      final PSRelationship relationship = new PSRelationship(1, owner, dependent, config);
      
      assertFalse(relationship.equals(new Object()));
      assertEqualsWithHash(relationship,
            new PSRelationship(1, owner, dependent, config));
      {
         final PSRelationshipConfig config2 =
            new PSRelationshipConfig("name2", PSRelationshipConfig.RS_TYPE_SYSTEM);
         assertFalse(relationship.equals(
               new PSRelationship(1, owner, dependent, config2)));
      }
   }

   /**
    * The relationship configuration set used for testing purposes. Initialized
    * in the first call to {@link getConfigs()}, never <code>null</code>, empty
    * or changed after that.
    */
   private static PSRelationshipConfigSet ms_configs = null;
}
