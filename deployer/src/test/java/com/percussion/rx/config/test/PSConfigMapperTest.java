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

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.impl.PSConfigMapper;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.types.PSPair;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test {@link PSConfigMapper}
 * 
 * Note, the "Working directory" must be ${workspace_loc:Rhythmyx-Main} when run
 * this unit test.
 */
public class PSConfigMapperTest
{

    /**
     * Tests the PSConfigMapper where the configure definition file contains 
     * ONE handler (bean).
     * 
     * @throws Exception if an error occurs.
     */
    @SuppressWarnings("unchecked")
    @Test
   public void testConfigMapper() throws Exception
    {
       String prefix = "com.percussion.RSS.";
       String K1 = "label";
       String K2 = "description";
       String K3 = "bindingSet";
       String CTX_NAME = "contexts";
       
       // prepare test data
       String KEY1 = prefix + K1;
       String KEY2 = prefix + K2;
       String KEY3 = prefix + K3;

       // handler properties
       String CTX_KEY = prefix + CTX_NAME;
       
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       // Replaced all property values
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       Map<String, Object> props = new HashMap<String, Object>();
       props.put(KEY1, "localhost");
       props.put(KEY2, "Hello world");
       
       Map<String, String> map = new HashMap<String, String>();
       map.put("$backgroundColor", "red");
       map.put("$fontColor", "black");
       props.put(KEY3, map);
       props.put(CTX_KEY, "*Site*Folder*");

       // initialize previous properties
       Map<String, Object> prevProps = new HashMap<String, Object>();
       prevProps.putAll(props);
       prevProps.put(CTX_KEY, "publish");

       // initialize partial properties (handler's properties only)
       Map<String, Object> partialProps = new HashMap<String, Object>();
       partialProps.put(CTX_KEY, "*Site*Folder*");


       PSConfigMapper mapper = new PSConfigMapper();
       File f = PSResourceUtils.getFile(PSImplConfigLoaderTest.class,PSImplConfigLoaderTest.SIMPLE_CONFIG_DEF,null);
       List<IPSConfigHandler> handlers = mapper.getResolvedHandlers(
           f.getAbsolutePath(), partialProps, props,
            prevProps);
       assertTrue(handlers.size() == 1);
       
       IPSConfigHandler handler = handlers.get(0);
       
       // validate handler CURRENT properties
       String ctx = (String) handler.getExtraProperties().get(CTX_NAME);
       assertTrue(ctx.equals("*Site*Folder*"));
       assertTrue(handler.getName().equals("LocationScheme"));
       
       // validate handler PREVIOUS properties
       String prevCtx = (String) handler.getPrevExtraProperties().get(CTX_NAME);
       assertTrue(prevCtx.equals("publish"));
       
       // validate setter & its properties
       List<IPSPropertySetter> setters = handler.getPropertySetters();
       assertTrue(setters.size() == 1);
       
       IPSPropertySetter setter = setters.get(0);
       Map<String, Object> replacedProps = setter.getProperties();
       assertTrue(replacedProps.size() == 3);
       
       assertTrue(replacedProps.get(K1).equals(props.get(KEY1)));
       assertTrue(replacedProps.get(K2).equals(props.get(KEY2)));
       // test the replaced map
       assertTrue(replacedProps.get(K3) instanceof Map);
       Map rmap = (Map) replacedProps.get(K3);
       assertTrue(rmap.size() == 2);
       assertTrue(rmap.get("$backgroundColor").equals("red"));
       assertTrue(rmap.get("$fontColor").equals("black"));
       
       //\/\/\/\/\/\/\/\/\/\/\/\/\/
       // Partial and all properties are the SAME
       //\/\/\/\/\/\/\/\/\/\/\/\/\/
       
       partialProps.put(KEY1, "localhost-1");
       
       handlers = mapper.getResolvedHandlers(
            f.getAbsolutePath(), partialProps, props,
            props);
       setter = handlers.get(0).getPropertySetters().get(0);
       replacedProps = setter.getProperties();
       
       assertTrue(replacedProps.size() == 3);
       assertTrue(replacedProps.get(K1).equals(props.get(KEY1)));
       assertTrue(replacedProps.get(K2).equals(props.get(KEY2)));
       assertTrue(replacedProps.get(K3).equals(map));
    }

    /**
     * Tests the PSConfigMapper where the impl bean file contains TWO 
     * handler (bean) and each handler contains one setter.
     * 
     * @throws Exception if an error occurs.
     */
    @Test
    public void testConfigMapper2() throws Exception
    {
       // prepare test data
       String KEY1 = "com.percussion.RSS.label";
       String KEY2 = "com.percussion.RSS.description";
       String KEY3 = "com.percussion.RSS.label2";
       String KEY4 = "com.percussion.RSS.description2";
       
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
       // Replaced properties in all (2) handlers
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
       Map<String, Object> props = new HashMap<String, Object>();
       props.put(KEY1, "localhost");
       props.put(KEY2, "Hello world");
       props.put(KEY3, "localhost");
       props.put(KEY4, "Hello world");
       
       PSConfigMapper mapper = new PSConfigMapper();
       File f = PSResourceUtils.getFile(PSImplConfigLoaderTest.class,PSImplConfigLoaderTest.SIMPLE2_CONFIG_DEF,null );
       List<IPSConfigHandler> handlers = mapper.getResolvedHandlers(
             f.getAbsolutePath(), props, props, props);
       assertTrue(handlers.size() == 2);

       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       // Replaced properties in ONE handler with current & previous properties
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       props.clear();
       props.put(KEY1, "localhost");
       props.put(KEY2, "Hello world");
       
       Map<String, Object> prevProps = new HashMap<String, Object>();
       prevProps.put(KEY1, "Prev localhost");
       prevProps.put(KEY2, "Prev Hello world");
       
       handlers = mapper.getResolvedHandlers(
            f.getAbsolutePath(), props, props,
            prevProps);

       List<IPSPropertySetter> setters = handlers.get(0).getPropertySetters();
       assertTrue(setters.size() == 1);

       // validate the current and previous properties
       IPSPropertySetter setter = setters.get(0);
       validateMapValues(props, setter.getProperties());
       validateMapValues(prevProps, setter.getPrevProperties());
       
       
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       // Replaced properties in ONE handler with current & EMPTY previous properties
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       props.clear();
       props.put(KEY1, "localhost");
       props.put(KEY2, "Hello world");
       
       prevProps.clear();
       
       handlers = mapper.getResolvedHandlers(
            f.getAbsolutePath(), props, props,
            prevProps);

       IPSConfigHandler handler = handlers.get(0);
       
       // validate handler.getPrevProperties()
       assertTrue(handler.getPrevExtraProperties() == null);
       
       // validate setter
       setters = handler.getPropertySetters();
       assertTrue(setters.size() == 1);

       // validate the current and previous properties
       setter = setters.get(0);
       validateMapValues(props, setter.getProperties());
       assertTrue(setter.getPrevProperties() == null);
   }

   @Test
   public void testGetPlaceholders() throws Exception
   {
      PSPair<List<String>, Boolean> result;
      String PLACEHOLDER_1 = "One";
      String PLACEHOLDER_2 = "Two";
      String PREFIX = PSConfigMapper.PREFIX;
      String SUFFIX = PSConfigMapper.SUFFIX;

      // handle ONE place-holder
      String text = PREFIX + PLACEHOLDER_1 + SUFFIX;
      result = PSConfigMapper.getPlaceholders(text);
      assertTrue("Should have only one placeholder", result.getSecond());
      List<String> holders = result.getFirst();
      assertTrue("Should have only one placeholder", holders.size() == 1);
      String holder = result.getFirst().get(0);
      assertTrue("The placeholder is One", holder.equals(PLACEHOLDER_1));

      // handle ONE place-holder with blank characters
      text = " " + PREFIX + " " + PLACEHOLDER_1 + "\t " + SUFFIX + " \t ";
      result = PSConfigMapper.getPlaceholders(text);
      assertTrue("Should have only one placeholder", result.getSecond());
      holder = result.getFirst().get(0);
      assertTrue("The placeholder is One", holder.equals(PLACEHOLDER_1));

      // handle ONE place-holder with OTHER characters
      text = PREFIX + " " + PLACEHOLDER_1 + "\t " + SUFFIX + "abc";
      result = PSConfigMapper.getPlaceholders(text);
      assertTrue(
            "Should have only one placeholder, but with other characters",
            !result.getSecond());
      holder = result.getFirst().get(0);
      assertTrue("The placeholder is One", holder.equals(PLACEHOLDER_1));
      
      // handle more than one place-holder(s)
      text = PREFIX + PLACEHOLDER_1 + SUFFIX + PREFIX + PLACEHOLDER_2 + SUFFIX;
      result = PSConfigMapper.getPlaceholders(text);
      assertTrue("Should have 2 placeholders", !result.getSecond());
      assertTrue("Should have 2 placeholders", result.getFirst().size() == 2);

      // handle more than one place-holder(s), and other characters
      text = PREFIX + " " + PLACEHOLDER_1 + SUFFIX + "abc " + PREFIX
            + PLACEHOLDER_2 + " " + SUFFIX + "advc";
      result = PSConfigMapper.getPlaceholders(text);
      assertTrue("Should have 2 placeholders", !result.getSecond());
      holders = result.getFirst();
      assertTrue("Should have 2 placeholders", holders.size() == 2);
      assertTrue("The 1st placeholder is One", holders.get(0).equals(PLACEHOLDER_1));
      assertTrue("The 2nd placeholder is Two", holders.get(1).equals(PLACEHOLDER_2));
   }
    
    
    /**
       * Validates the specified maps.
       * 
       * @param m1 the 1st map to be compared with, assumed not
       * <code>null</code>.
       * @param m2 the 2nd map to be compared with, assumed not
       * <code>null</code>.
       */
    @SuppressWarnings("unchecked")
    private void validateMapValues(Map<String, Object> m1,
         Map<String, Object> m2)
    {
       Set values1 = new HashSet<String>();
       values1.addAll(m1.values());
       Set values2 = new HashSet<String>();
       values2.addAll(m2.values());

       assertTrue(values1.equals(values2));
    }
    
    
    /**
     * Tests replacing part of the property values. 
     * For example, a value may be "ABC ${...} XYZ". The replacement should not
     * replace "ABC" or "XYZ". 
     * 
     * @throws Exception if an error occurs.
     */
    @SuppressWarnings("unchecked")
    @Test
   public void testPartialReplacement() throws Exception
    {
       // prepare test data
       String PREFIX = "com.percussion.RSS.";
       String K1 = "label";
       String K2 = "description";
       String K3 = "label_desc";
       String K4 = "listEntry";
       String K5 = "mapEntry";
       String K6 = "constantEntry";
       String K7 = "noReplace";

       String KEY1 = PREFIX + K1;
       String KEY2 = PREFIX + K2;
              
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       // Replaced all property values
       //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
       Map<String, Object> props = new HashMap<String, Object>();
       props.put(KEY1, "localhost");
       props.put(KEY2, "Hello world");
       
       PSConfigMapper mapper = new PSConfigMapper();
       File f = PSResourceUtils.getFile(PSConfigMapperTest.class,PARTIAL_IMPL_CONFIG,null);

       List<IPSConfigHandler> handlers = mapper.getResolvedHandlers(
             f.getAbsolutePath(), props, props, props);
       assertTrue(handlers.size() == 1);
       
       List<IPSPropertySetter> setters = handlers.get(0).getPropertySetters();
       assertTrue(setters.size() == 1);
       
       IPSPropertySetter setter = setters.get(0);
       Map<String, Object> replacedProps = setter.getProperties();
       assertTrue(replacedProps.size() == 7);
       
       // "Label_Begin_ ${com.percussion.RSS.label}"
       String replacedValue = "Label_Begin_ " + props.get(KEY1); 
       assertTrue(replacedProps.get(K1).equals(replacedValue));
       
       // "Begin_ ${com.percussion.RSS.description} _End"
       replacedValue = "Begin_ " + props.get(KEY2) + " _End"; 
       assertTrue(replacedProps.get(K2).equals(replacedValue));

       // "Label_Begin_ ${com.percussion.RSS.label} MIDDLE ${com.percussion.RSS.description} _End"
       replacedValue = "Label_Begin_ " + props.get(KEY1) + " MIDDLE " + props.get(KEY2) + " _End";
       assertTrue(replacedProps.get(K3).equals(replacedValue));    
       
       //\/\/\/\/\/\/\/
       // validate List
       List listEntry = (List)replacedProps.get(K4);
       assertTrue(listEntry.size() == 2);
       assertTrue(listEntry.get(0).equals(props.get(KEY1)));
       assertTrue(listEntry.get(1).equals(props.get(KEY2)));

       //\/\/\/\/\/\/\/
       // validate Map
       Map mapEntry = (Map)replacedProps.get(K5);
       assertTrue(mapEntry.size() == 2);
       
       // "Label_Begin_ ${com.percussion.RSS.label}"
       replacedValue = "Label_Begin_ " + props.get(KEY1); 
       assertTrue(mapEntry.get(K1).equals(replacedValue));
       
       // "Begin_ ${com.percussion.RSS.description} _End"
       replacedValue = "Begin_ " + props.get(KEY2) + " _End"; 
       assertTrue(mapEntry.get(K2).equals(replacedValue));
       
       //\/\/\/\/\/\/\/
       // validate CONSTANT and No Replacement
       String constantEntry = (String)replacedProps.get(K6);
       assertTrue(constantEntry.equals("constant value"));

       String noReplace = (String)replacedProps.get(K7);
       assertTrue(noReplace.equals("${com.percussion.RSS.noReplace}"));
    }
    
    /**
     * the configure file contains property value with partial replacement.  
     */
    public static final String PARTIAL_IMPL_CONFIG = "/com/percussion/config/ImplConfigBean_PartialReplace.xml";
}
