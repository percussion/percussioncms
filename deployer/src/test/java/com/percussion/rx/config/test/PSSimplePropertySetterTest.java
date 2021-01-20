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
package com.percussion.rx.config.test;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.rx.config.impl.PSSimplePropertySetter;
import com.percussion.services.catalog.PSTypeEnum;
import junit.framework.TestCase;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test {@link PSSimplePropertySetter}
 *
 * @author YuBingChen
 */
public class PSSimplePropertySetterTest extends TestCase
{
   /**
    * Test properties in the super class
    * 
    * @throws Exception if an error occurs.
    */
   public void testSimplePropertiesWithDerivedClass() throws Exception
   {
      MyDesignObject dObj = new MyDesignObject();

      // create the setter
      PSSimplePropertySetter sSetter = new PSSimplePropertySetter();
      Map<String, Object> pmap = new HashMap<String, Object>();
      
      String MY_STR_VALUE = "My String Value";
      String STR_VALUE = "The String Value";

      pmap.put("myStringValue", MY_STR_VALUE);
      pmap.put("stringValue", STR_VALUE);
      sSetter.setProperties(pmap);

      // create the handler
      List<IPSPropertySetter> ss = new ArrayList<IPSPropertySetter>();
      ss.add(sSetter);
      PSObjectConfigHandler h = new PSObjectConfigHandler();
      h.setPropertySetters(ss);
      
      // perform the test
      assertTrue(dObj.getMyStringValue() == null);
      h.process(dObj, ObjectState.BOTH, null);
      assertTrue(dObj.getMyStringValue().equals(MY_STR_VALUE));
      assertTrue(dObj.getStringValue().equals(STR_VALUE));
   }

   public void testAddPropertyDefs() throws Exception
   {
      MyDesignObject dObj = new MyDesignObject();
      
      // create the setter
      PSSimplePropertySetter sSetter = new PSSimplePropertySetter();
      Map<String, Object> pmap = new HashMap<String, Object>();

      // constant properties without property definition
      pmap.put("myStringValue", "hello");
      pmap.put("stringValue", "constant");
      sSetter.setProperties(pmap);
      
      Map<String, Object> defs = new HashMap<String, Object>();
      sSetter.addPropertyDefs(dObj, defs);
      assertTrue(
            "Expect EMPTY defs since there is no place-holder defined in the properties",
            defs.isEmpty());
      
      // properties with one NULL property definition
      pmap.put("myStringValue", "${myPrefix.stringVal}");
      sSetter.setProperties(pmap);
      defs.clear();
      sSetter.addPropertyDefs(dObj, defs);
      assertTrue("Expect EMPTY 1 def", defs.size() == 1);
      Object nullObj = defs.get("myPrefix.stringVal");
      assertTrue("Expect null value", nullObj == null); 

      // properties with one none-NULL property definition
      dObj.setMyStringValue("MyValue");
      defs.clear();
      sSetter.addPropertyDefs(dObj, defs);
      String myvalue = (String) defs.get("myPrefix.stringVal");
      assertTrue("Expect MyValue", myvalue.equals("MyValue"));

      // properties with 1 FIX_ME property definition
      pmap.put("myStringValue", "${myPrefix.stringVal} abc");
      sSetter.setProperties(pmap);
      defs.clear();
      sSetter.addPropertyDefs(dObj, defs);
      assertTrue("Expect EMPTY 1 def", defs.size() == 1);
      myvalue = (String) defs.get("myPrefix.stringVal");
      assertTrue("Expect FIXME", myvalue.equals(PSSimplePropertySetter.FIX_ME));

      // properties with 2 property definitions
      pmap.put("myStringValue", "${myPrefix.stringVal} abc ${myPrefix.stringVal_2}");
      sSetter.setProperties(pmap);
      defs.clear();
      sSetter.addPropertyDefs(dObj, defs);
      
      // validate "defs"
      assertTrue("Expect EMPTY 2 def", defs.size() == 2);
      for (String k : defs.keySet())
      {
         assertTrue(k.equals("myPrefix.stringVal")
               || k.equals("myPrefix.stringVal_2"));
         
         myvalue = (String) defs.get(k);
         assertTrue("Expect value = \"FIXME\"", myvalue
               .equals(PSSimplePropertySetter.FIX_ME));
      }
      
      // properties with 1 inner property definitions
      pmap.clear();
      pmap.put("innerObject.innerObject2.stringValue", "${myPrefix.stringVal}");
      sSetter.setProperties(pmap);
      DesignObject dsObj = new DesignObject();
      dsObj.getInnerObject().getInnerObject2().setStringValue("MyInnerStringValue");
      defs.clear();
      sSetter.addPropertyDefs(dsObj, defs);
      
      // validate "defs"
      assertTrue("Expect EMPTY 1 def", defs.size() == 1);
      myvalue = (String) defs.get("myPrefix.stringVal");
      assertTrue("Expect value = \"MyInnerStringValue\"", myvalue.equals("MyInnerStringValue"));

      // properties with 1 string property & 2 inner property definitions
      pmap.clear();
      pmap.put("stringValue", "${myPrefix.stringVal_1}");
      pmap.put("innerObject.innerObject2.stringValue", "${myPrefix.stringVal} abc ${myPrefix.stringVal_2}");
      sSetter.setProperties(pmap);
      dsObj = new DesignObject();
      defs.clear();
      sSetter.addPropertyDefs(dsObj, defs);
      
      // validate "defs"
      assertTrue("Expect EMPTY 1 def", defs.size() == 3);

   }
   
   /**
    * Tests the properties in current class. Also test the dot notation.
    * 
    * @throws Exception if an error occurs.
    */
   public void testSimpleProperties() throws Exception
   {
      DesignObject dObj = new DesignObject();
      
      // create the setter
      PSSimplePropertySetter sSetter = new PSSimplePropertySetter();
      Map<String, Object> pmap = new HashMap<String, Object>();
      
      String STR_VALUE = "The String Value";
      String STR_IINER_VALUE = "The INNER String Value";
      String STR_IINER_VALUE2 = "The INNER INNER String Value";
      int INT_VALUE = 1234;
      Integer INTEGER_VALUE = 12345;
      Long LONG_VALUE = 123456L;
      Boolean BOOLEAN_VALUE = true;
      PSTypeEnum ENUM_VALUE = PSTypeEnum.TEMPLATE;
      String URL_VALUE = "http://localhost:9992/Rhythmyx";
      
      pmap.put("stringValue", STR_VALUE);
      pmap.put("intValue", String.valueOf(INT_VALUE));
      pmap.put("integerValue", String.valueOf(INTEGER_VALUE));
      pmap.put("longValue", String.valueOf(LONG_VALUE));
      pmap.put("booleanValue", "on"); //"yes"); //String.valueOf(BOOLEAN_VALUE));
      pmap.put("typeEnum", ENUM_VALUE.name());
      pmap.put("urlValue", URL_VALUE);
      
      pmap.put("innerObject.stringValue", STR_IINER_VALUE);
      pmap.put("innerObject.innerObject2.stringValue", STR_IINER_VALUE2);
      sSetter.setProperties(pmap);

      // create the handler
      List<IPSPropertySetter> ss = new ArrayList<IPSPropertySetter>();
      ss.add(sSetter);
      PSObjectConfigHandler h = new PSObjectConfigHandler();
      h.setPropertySetters(ss);
      
      // perform the test
      assertTrue(dObj.getStringValue() == null);
      h.process(dObj, ObjectState.BOTH, null);
      assertTrue(dObj.getStringValue().equals(STR_VALUE));
      assertTrue(dObj.getIntValue() == INT_VALUE);
      assertTrue(dObj.getIntegerValue().equals(INTEGER_VALUE));
      assertTrue(dObj.getLongValue().equals(LONG_VALUE));
      assertTrue(dObj.getBooleanValue().equals(BOOLEAN_VALUE));
      assertTrue(dObj.getTypeEnum().equals(ENUM_VALUE));
      assertTrue(dObj.getUrlValue().toString().equals(URL_VALUE));

      assertTrue(dObj.getInnerObject().getStringValue()
            .equals(STR_IINER_VALUE));
      assertTrue(dObj.getInnerObject().getInnerObject2().getStringValue()
            .equals(STR_IINER_VALUE2));
   }
   
   /**
    * Negative test for the simple setter.
    * 
    * @throws Exception if an error occurs.
    */
   public void testSimplePropertyNegative() throws Exception
   {
      DesignObject dObj = new DesignObject();
      
      // create the setter
      PSSimplePropertySetter sSetter = new PSSimplePropertySetter();
      Map<String, Object> pmap = new HashMap<String, Object>();
      pmap.put("nullInnerObject.stringValue", "blah");
      sSetter.setProperties(pmap);

      // create the handler
      List<IPSPropertySetter> ss = new ArrayList<IPSPropertySetter>();
      ss.add(sSetter);
      PSObjectConfigHandler h = new PSObjectConfigHandler();
      h.setPropertySetters(ss);
      
      // perform the test
      try
      {
         h.process(dObj, ObjectState.BOTH, null);
         assertTrue("Should fail on null child object", false);
      }
      catch (Exception e)
      {
         assertTrue("Caught the null child object", true);         
      }
   }
   
   public static class InnerObject
   {
      /**
       * The String type property
       */
      private String m_stringValue;
      
      public String getStringValue()
      {
         return m_stringValue;
      }
      
      public void setStringValue(String value)
      {
         m_stringValue = value;
      }
      
      private InnerObject2 inObj = new InnerObject2();
      
      public InnerObject2 getInnerObject2()
      {
         return inObj;
      }
   }

   public static class InnerObject2
   {
      /**
       * The String type property
       */
      private String m_stringValue;
      
      public String getStringValue()
      {
         return m_stringValue;
      }
      
      public void setStringValue(String value)
      {
         m_stringValue = value;
      }      
   }
   

   /**
    * This class is used as a mock object for the design objects.
    */
   public static class DesignObject
   {
      private InnerObject inObj = new InnerObject();
      
      public InnerObject getInnerObject()
      {
         return inObj;
      }
      
      private InnerObject inObj2 = null;
      
      public InnerObject getNullInnerObject()
      {
         return inObj2;
      }
      
      /**
       * The String type property
       */
      private String m_stringValue;
      
      public String getStringValue()
      {
         return m_stringValue;
      }
      
      public void setStringValue(String value)
      {
         m_stringValue = value;
      }
      
      /**
       * The integer primitive type property
       */
      private int m_intValue;
      
      public int getIntValue()
      {
         return m_intValue;
      }
      
      public void setIntValue(int value)
      {
         m_intValue = value;
      }
      
      /**
       * The integer type property
       */
      private Integer m_integerValue;
      
      public Integer getIntegerValue()
      {
         return m_integerValue;
      }
      
      public void setIntegerValue(Integer value)
      {
         m_integerValue = value;
      }
      
      /**
       * The integer type property
       */
      private Long m_longValue;
      
      public Long getLongValue()
      {
         return m_longValue;
      }
      
      public void setLongValue(Long value)
      {
         m_longValue = value;
      }

      /**
       * The boolean type property
       */
      private Boolean m_booleanValue;
      
      public Boolean getBooleanValue()
      {
         return m_booleanValue;
      }
      
      public void setBooleanValue(Boolean value)
      {
         m_booleanValue = value;
      }
      
      private PSTypeEnum m_enum;
      
      public PSTypeEnum getTypeEnum()
      {
         return m_enum;
      }
      
      public void setTypeEnum(PSTypeEnum e)
      {
         m_enum = e;
      }
      
      /**
       * The URL type property
       */
      private URL m_url;
      
      public URL getUrlValue()
      {
         return m_url;
      }
      
      public void setUrlValue(URL url)
      {
         m_url = url;
      }
   }
   
   /**
    * Used to test the methods in derived classes
    */
   public static class MyDesignObject extends DesignObject
   {
      /**
       * The String type property
       */
      private String m_myStringValue;
      
      public String getMyStringValue()
      {
         return m_myStringValue;
      }
      
      public void setMyStringValue(String value)
      {
         m_myStringValue = value;
      }      
   }
}
