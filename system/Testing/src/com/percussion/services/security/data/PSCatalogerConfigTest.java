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
package com.percussion.services.security.data;

import com.percussion.services.security.data.PSCatalogerConfig.ConfigTypes;
import com.percussion.utils.tools.PSTestUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.TestCase;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Test case for the {@link PSCatalogerConfig}.
 */
public class PSCatalogerConfigTest extends TestCase
{
   /**
    * Test the parameterized constructor.
    * 
    * @throws Exception If the test fails.
    */
   public void testCtor() throws Exception
   {
      Object[] args = ARGS.clone();
      PSCatalogerConfig config;
      
      config = (PSCatalogerConfig) PSTestUtils.testCtor(PSCatalogerConfig.class, 
         PARAMS, args, false);
      assertEquals(config.getName(), args[NAME]);
      assertEquals(config.getConfigType(), args[TYPE]);
      assertEquals(config.getClassName(), args[CLASS]);
      assertEquals(config.getDescription(), args[DESC]);
      assertEquals(config.getProperties(), args[PROPS]);
      
      args[NAME] = null;
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, true);
      args[NAME] = "";
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, true);

      args[NAME] = "name";
      args[TYPE] = null;
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, true);

      args[TYPE] = ConfigTypes.ROLE;
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, false);
      
      args[CLASS] = null;
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, true);
      args[CLASS] = "";
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, true);
      
      args[CLASS] = "com.test.PSCataloger";
      args[DESC] = null;
      config = (PSCatalogerConfig) PSTestUtils.testCtor(PSCatalogerConfig.class, 
         PARAMS, args, false);
      assertEquals(config.getDescription(), "");
      
      args[PROPS] = null;
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, true);
      args[PROPS] = new HashMap<String, String>();
      PSTestUtils.testCtor(PSCatalogerConfig.class, PARAMS, args, false);
   }
   
   /**
    * Test the get and set methods.
    * 
    * @throws Exception If the test fails.
    */
   public void testAccessors() throws Exception
   {
      PSCatalogerConfig config = (PSCatalogerConfig) PSTestUtils.testCtor(
         PSCatalogerConfig.class, PARAMS, ARGS, false);
      
      testSetter(config, NAME, ARGS[NAME], false);
      testSetter(config, NAME, null, true);
      testSetter(config, NAME, "", true);
      
      testSetter(config, DESC, ARGS[DESC], false);
      config.setDescription(null);
      assertEquals(config.getDescription(), "");
      testSetter(config, DESC, "", false);
      
      testSetter(config, CLASS, ARGS[CLASS], false);
      testSetter(config, CLASS, null, true);
      testSetter(config, CLASS, "", true);
      
      testSetter(config, PROPS, ARGS[PROPS], false);
      testSetter(config, PROPS, null, true);
      testSetter(config, PROPS, new HashMap<String, String>(), false);
   }
   
   /**
    * Test a set/get method.
    * 
    * @param config The config to use, assumed not <code>null</code>.
    * @param iProp The index into the arrays of the arg to test.
    * @param val The val to set/get, may be <code>null</code> or empty.
    * @param shouldThrow <code>true</code> if the setter should throw an 
    * excpetion, <code>false</code> if not.
    * 
    * @throws Exception If the test fails.
    */
   private void testSetter(PSCatalogerConfig config, int iProp, Object val, 
      boolean shouldThrow) throws Exception 
   {
      Object[] args = ARGS.clone();
      args[iProp] = val;
      PSTestUtils.testSetter(config, METHODS[iProp], args[iProp], PARAMS[iProp], 
         shouldThrow);
   }
   
   /**
    * Test to/fromXml.
    * 
    * @throws Exception If the test fails.
    */
   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      PSCatalogerConfig config1 = (PSCatalogerConfig) PSTestUtils.testCtor(
         PSCatalogerConfig.class, PARAMS, ARGS, false);
      assertEquals(config1, new PSCatalogerConfig(config1.toXml(doc), 
         config1.getConfigType()));
      
      Object[] args = ARGS.clone();
      args[TYPE] = ConfigTypes.ROLE;
      config1 = (PSCatalogerConfig) PSTestUtils.testCtor(
         PSCatalogerConfig.class, PARAMS, args, false);
      assertEquals(config1, new PSCatalogerConfig(config1.toXml(doc), 
         config1.getConfigType()));
      
      config1.setDescription(null);
      assertEquals(config1, new PSCatalogerConfig(config1.toXml(doc), 
         config1.getConfigType()));
      
      config1.setProperties(new HashMap<String, String>());
      assertEquals(config1, new PSCatalogerConfig(config1.toXml(doc), 
         config1.getConfigType()));
   }
   
   /**
    * Test the equals method.
    * 
    * @throws Exception If the test fails.
    */   
   public void testEquals() throws Exception
   {
      PSCatalogerConfig config1 = (PSCatalogerConfig) PSTestUtils.testCtor(
         PSCatalogerConfig.class, PARAMS, ARGS, false);
      PSCatalogerConfig config2 = (PSCatalogerConfig) PSTestUtils.testCtor(
         PSCatalogerConfig.class, PARAMS, ARGS, false);
      
      assertEquals(config1, config2);
      assertTrue(config1.hashCode() == config2.hashCode());
      assertTrue(!config1.equals(null));
      
      Object[] args = ARGS.clone();
      args[TYPE] = ConfigTypes.ROLE;
      PSCatalogerConfig config3 = (PSCatalogerConfig) PSTestUtils.testCtor(
         PSCatalogerConfig.class, PARAMS, args, false);
      assertTrue(!config1.equals(config3));
      assertTrue(config1.hashCode() != config3.hashCode());
      
      config2.setName(config2.getName() + "test");
      assertTrue(!config1.equals(config2));
      assertTrue(config1.hashCode() != config2.hashCode());
      config1.setName(config2.getName());
      assertEquals(config1, config2);
      assertTrue(config1.hashCode() == config2.hashCode());
      
      config2.setClassName(config2.getClassName() + "test");
      assertTrue(!config1.equals(config2));
      assertTrue(config1.hashCode() != config2.hashCode());
      config1.setClassName(config2.getClassName());
      assertEquals(config1, config2);
      assertTrue(config1.hashCode() == config2.hashCode());
      
      config2.setDescription(null);
      assertTrue(!config1.equals(config2));
      assertTrue(config1.hashCode() != config2.hashCode());
      config1.setDescription("");
      assertEquals(config1, config2);
      assertTrue(config1.hashCode() == config2.hashCode());
      
      config2.setProperties(new HashMap<String, String>());
      assertTrue(!config1.equals(config2));
      assertTrue(config1.hashCode() != config2.hashCode());
      config1.setProperties(config2.getProperties());
      assertEquals(config1, config2);
      assertTrue(config1.hashCode() == config2.hashCode());
      
      assertEquals(config1, config1.clone());
   }
   
   /**
    * Array of classes used to call the ctor by reflection.
    */
   private static final Class[] PARAMS = new Class[] {String.class, 
      ConfigTypes.class, String.class, String.class, Map.class};
   
   /**
    * Array of values used for the constructor and accessor args.
    */
   private static Object[] ARGS;
   static
   {
      Map<String, String> props = new HashMap<String, String>();
      props.put("prop1", "val1");
      ARGS = new Object[] {"name", ConfigTypes.SUBJECT, "com.test.PSCataloger", 
         "desc", props};      
   }
   
   /**
    * Array of bean property names used to call accessors by reflection.
    */
   private static final String[] METHODS = new String[] {"Name", "ConfigType", 
      "ClassName", "Description", "Properties"};
   
   // constants for indexes into the arrays by readable name
   private static final int NAME = 0;
   private static final int TYPE = 1;
   private static final int CLASS = 2;
   private static final int DESC = 3;
   private static final int PROPS = 4;
   
}

