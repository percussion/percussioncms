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

import com.percussion.rx.config.impl.PSConfigNormalizer;
import com.percussion.utils.types.PSPair;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Tests {@link PSConfigNormalizer}
 *
 * @author YuBingChen
 */
public class PSConfigNormalizerTest extends TestCase
{
   /**
    * Tests loading a local config file which contains (more advanced) 
    * example properties.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testLocalConfigTest() throws Exception
   {
      loadLocalConfigTest(true);
   }

   public void testLocalConfigTest_2() throws Exception
   {
      loadLocalConfigTest(false);
   }

   private void loadLocalConfigTest(boolean resolveFQName) throws Exception
   {
      Map<String, Object> props = loadLocalConfigFile(resolveFQName);
                 
      validateLocalConfig(props, resolveFQName);
   }

   public Map<String, Object> loadLocalConfigFile(boolean resolveFQName)
      throws Exception
   {
      String content = PSConfigFilesFactoryTest.loadFile(LOCAL_FILE_NAME);
      InputStream in = IOUtils.toInputStream(content);
      PSConfigNormalizer nm = new PSConfigNormalizer();
      Map<String, Object> props = nm.getNormalizedMap(in, resolveFQName);
         
      // for debugging
      //dumpNormalizedMap(props);
         
      return props;
   }
   

   @SuppressWarnings("unchecked")
   public void validateLocalConfig(Map<String, Object> props,
         boolean resolveFQName)
   {
      int count = resolveFQName ? 9 + 14 : 9; // == 9 top level + 14 sub-level
      assertTrue(props.size() == count); 

      // Simple Properties ----------------------
      //
      String KEY1 = "com.percussion.Smog.Boston";
      String VALUE1 = "Good"; 
      String KEY2 = "com.percussion.Smog.NewYork";
      String VALUE2 = "Fair"; 
      String KEY3 = "com.percussion.Smog.LosAngeles";
      String VALUE3 = "Bad"; 
      
      assertTrue(props.get(KEY1).equals(VALUE1));
      assertTrue(props.get(KEY2).equals(VALUE2));
      assertTrue(props.get(KEY3).equals(VALUE3));
      
      // Properties in 2nd solution ----------------------
      //
      String SOL_KEY1 = "com.percussion.Smog_2.Seattle";
      String SOL_VALUE1 = "Good"; 
      assertTrue(props.get(SOL_KEY1).equals(SOL_VALUE1));

      // Property Value List --------------------------------------------
      //
      String KEY4 = "com.percussion.Smog.ValueList";
      String VALUE4_0 = "value1"; 
      String VALUE4_1 = "value2"; 
      String VALUE4_2 = "value3"; 
     
      List<String> valueList = (List<String>) props.get(KEY4);
      assertTrue(valueList.get(0).equals(VALUE4_0));
      assertTrue(valueList.get(1).equals(VALUE4_1));
      assertTrue(valueList.get(2).equals(VALUE4_2));
      
      // Property Pair List --------------------------------------------
      //
      String KEY5 = "com.percussion.Smog.PairMap";
      String VALUE5_PAIR0_VALUE0 = "a"; 
      String VALUE5_PAIR0_VALUE1 = "1"; 
      String VALUE5_PAIR1_VALUE0 = "b"; 
      String VALUE5_PAIR1_VALUE1 = "2"; 
      String VALUE5_PAIR2_VALUE0 = "c"; 
      String VALUE5_PAIR2_VALUE1 = "3"; 
     
      List<PSPair> valueList2 = (List<PSPair>) props.get(KEY5);
      assertTrue(valueList2.get(0).getFirst().equals( VALUE5_PAIR0_VALUE0));
      assertTrue(valueList2.get(0).getSecond().equals(VALUE5_PAIR0_VALUE1));
      assertTrue(valueList2.get(1).getFirst().equals( VALUE5_PAIR1_VALUE0));
      assertTrue(valueList2.get(1).getSecond().equals(VALUE5_PAIR1_VALUE1));
      assertTrue(valueList2.get(2).getFirst().equals( VALUE5_PAIR2_VALUE0));
      assertTrue(valueList2.get(2).getSecond().equals(VALUE5_PAIR2_VALUE1));
      
      // Simple PropertySet containing Simple Properties ----------------------
      //
      String KEY6_0 = "com.percussion.Smog.propSet1";
      
      String KEY6 = "ps1p1Name";
      String VALUE6 = "ps1p1Value"; 
      String KEY7 = "ps1p2Name";
      String VALUE7 = "ps1p2Value"; 
      String KEY8 = "ps1p3Name";
      String VALUE8 = "ps1p3Value"; 
      
      // Access via Property map
      Map<String,Object> map1 = (Map<String,Object>) props.get(KEY6_0);
      assertTrue(map1.get(KEY6).equals(VALUE6));
      assertTrue(map1.get(KEY7).equals(VALUE7));
      assertTrue(map1.get(KEY8).equals(VALUE8));
      if (resolveFQName)
      {
         KEY6 = KEY6_0 + "." + KEY6;
         assertTrue(props.get(KEY6).equals(VALUE6));
         KEY7 = KEY6_0 + "." + KEY7;
         assertTrue(props.get(KEY7).equals(VALUE7));
         KEY8 = KEY6_0 + "." + KEY8;
         assertTrue(props.get(KEY8).equals(VALUE8));
      }
      
      /*      
      // Access via full Property name
      assertTrue(props.get(KEY6).equals(VALUE6));
      assertTrue(props.get(KEY7).equals(VALUE7));
      assertTrue(props.get(KEY8).equals(VALUE8));
      */
      
      // Complex Property with Simple PropertySet ------------------------------------
      //
      String KEY9_0 = "com.percussion.Smog.propertyWithPropSet";
      String KEY9 = "pwpsP1Name";
      String VALUE9 = "pwpsP1Value"; 
      String KEY10 = "pwpsP2Name";
      String VALUE10 = "pwpsP2Value"; 
      String KEY11 = "pwpsP3Name";
      String VALUE11 = "pwpsP3Value"; 
      String KEY12 = "pwpsP4Name";
      String VALUE12 = "pwpsP4Value"; 
      
      List<Object> list2 = (List<Object>) props.get(KEY9_0);
      Map<String,Object> map2 = (Map<String,Object>) list2.get(0);      
      assertTrue(map2.get(KEY9).equals(VALUE9));
      assertTrue(map2.get(KEY10).equals(VALUE10));
      Map<String,Object> map3 = (Map<String,Object>) list2.get(1);      
      assertTrue(map3.get(KEY11).equals(VALUE11));
      assertTrue(map3.get(KEY12).equals(VALUE12));

      // No access via full Property name, only through List (as above)
      assertNull(props.get(KEY9));
      assertNull(props.get(KEY10));
      assertNull(props.get(KEY11));
      assertNull(props.get(KEY12));
      
      // Complex PropertySet with Semi-Complex Children  -----------------------
      //   (Properties & PropertySets - List & Map)
      String KEY13_0 = "com.percussion.Smog.propSet2";
      String KEY13_1 = "propSet2L2";
      
      String KEY13   = "ps2p1Name";
      String VALUE13 = "ps2p1Value"; 
      String KEY14   = "ps2p2Name";
      String VALUE14 = "ps2p2Value"; 
      String KEY15   = "ps2p3Name";
      String VALUE15 = "ps2p3Value"; 
      
      String KEY16   = "ps2L3p1Name";
      String VALUE16 = "ps2L3p1Value"; 
      String KEY17   = "ps2L3p2Name";
      String VALUE17 = "ps2L3p2Value"; 
      String KEY18   = "ps2L3p3Name";
      String VALUE18 = "ps2L3p3Value"; 
      String KEY19   = "ps2L3p4Name";
      String VALUE19 = "ps2L3p4Value"; 
      
      // properties within propertySet
      Map<String,Object> map4 = (Map<String,Object>) props.get(KEY13_0);
      assertTrue(map4.get(KEY13).equals(VALUE13));
      assertTrue(map4.get(KEY14).equals(VALUE14));
      assertTrue(map4.get(KEY15).equals(VALUE15));
      
      // a propertySet with List within propertySet
      List<Object> list3 = (List<Object>) map4.get(KEY13_1);
      Map<String,Object> map5 = (Map<String,Object>) list3.get(0);      
      assertTrue(map5.get(KEY16).equals(VALUE16));
      assertTrue(map5.get(KEY17).equals(VALUE17));
      Map<String,Object> map6 = (Map<String,Object>) list3.get(1);      
      assertTrue(map6.get(KEY18).equals(VALUE18));
      assertTrue(map6.get(KEY19).equals(VALUE19));
      
      if (resolveFQName)
      {
         KEY13_1 = KEY13_0 + "." + KEY13_1;
         assertTrue(list3 == props.get(KEY13_1));
      }
      

      // a propertySet with Map within propertySet

      String KEY13_2 = "propSet2L2-2";
      
      String KEY16_2   = "ps2L3p1Name-2";
      
      String KEY16_2_1 = "ps2L4p1Name-2";
      String VAL16_2_1 = "ps2L4p1Value-2"; 
      String KEY16_2_2 = "ps2L4p2Name-2";
      String VAL16_2_2 = "ps2L4p2Value-2"; 
      
      
      String KEY17_2   = "ps2L3p2Name-2";

      String KEY17_2_1 = "ps2L4p3Name-2";
      String VAL17_2_1 = "ps2L4p3Value-2"; 
      String KEY17_2_2 = "ps2L4p4Name-2";
      String VAL17_2_2 = "ps2L4p4Value-2"; 

      Map<String, Object> map7 = (Map<String, Object>) map4.get(KEY13_2);
      assertTrue(map7.size() == 2);
      
      Map<String, Object> map8 = (Map<String, Object>) map7.get(KEY16_2);
      assertTrue(map8.size() == 2);
      assertTrue(map8.get(KEY16_2_1).equals(VAL16_2_1));
      assertTrue(map8.get(KEY16_2_2).equals(VAL16_2_2));
      
      Map<String, Object> map9 = (Map<String, Object>) map7.get(KEY17_2);
      assertTrue(map9.size() == 2);
      assertTrue(map9.get(KEY17_2_1).equals(VAL17_2_1));
      assertTrue(map9.get(KEY17_2_2).equals(VAL17_2_2));
      
      if (resolveFQName)
      {
         KEY13_2 = KEY13_0 + "." + KEY13_2;
         assertTrue(map7 == props.get(KEY13_2));
         
         KEY16_2 = KEY13_2 + "." + KEY16_2;
         assertTrue(map8 == props.get(KEY16_2));

         KEY16_2_1 = KEY16_2 + "." + KEY16_2_1;
         assertTrue(props.get(KEY16_2_1).equals(VAL16_2_1));
         KEY16_2_2 = KEY16_2 + "." + KEY16_2_2;
         assertTrue(props.get(KEY16_2_2).equals(VAL16_2_2));

         KEY17_2 = KEY13_2 + "." + KEY17_2;
         assertTrue(map9 == props.get(KEY17_2));

         KEY17_2_1 = KEY17_2 + "." + KEY17_2_1;
         assertTrue(props.get(KEY17_2_1).equals(VAL17_2_1));
         KEY17_2_2 = KEY17_2 + "." + KEY17_2_2;
         assertTrue(props.get(KEY17_2_2).equals(VAL17_2_2));
      }
   }

   @SuppressWarnings("unchecked")
   private void dumpNormalizedMap(Map<String, Object> nMap)
   {
      SortedMap<String,Object> sortedMap = new TreeMap<String,Object>(nMap);
      Set<Map.Entry<String,Object>> sortedSet = sortedMap.entrySet();
      for (Map.Entry<String, Object> mapEntry : sortedSet)
      {
         System.out.println("Key: " + mapEntry.getKey());
         System.out.println("Value: " + mapEntry.getValue());
         if (mapEntry.getValue() instanceof Map)
         {
            System.out.println("Map Size: " + ((Map)mapEntry.getValue()).size());
         }
         else if ((mapEntry.getValue() instanceof Collection))
         {
            System.out.println("List Size: "
                  + ((Collection) mapEntry.getValue()).size());
         }
         System.out.println("----------");
      }
   }
   
   /**
    * Testing empty map and list.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testEmptyLocalConfig() throws Exception
   {
      LoadEmptyLocalConfig(false);
   }

   public void testEmptyLocalConfig_2() throws Exception
   {
      LoadEmptyLocalConfig(true);
   }

   public void LoadEmptyLocalConfig(boolean resolveFQName) throws Exception
   {
      String content = PSConfigFilesFactoryTest.loadFile(LOCAL_EMPTY_FILE_NAME);
      InputStream in = IOUtils.toInputStream(content);

      PSConfigNormalizer nm = new PSConfigNormalizer();
      Map<String, Object> props = nm.getNormalizedMap(in, resolveFQName);
      
      validateEmptyProperties(props, resolveFQName ? 5 : 4);
   }
   
   private void validateEmptyProperties(Map<String, Object> props, int count)
   {
      // for debugging
      //dumpNormalizedMap(props);

      assertTrue(props.size() == count);
      
      // empty map (propertySet)
      Object set1Value = props.get("com.percussion.Smog.emptySet1"); 
      assertTrue(set1Value instanceof Map);
      assertTrue(((Map)set1Value).size() == 0);
      
      // property value is a map, which contains an empty Map
      Object paretValue = props.get("com.percussion.Smog.parent");
      assertTrue(paretValue instanceof Map);
      assertTrue(((Map)paretValue).size() == 1);
      
      Object childObj = ((Map) paretValue).get("emptySet2");
      assertTrue(childObj instanceof Map);
      assertTrue(((Map)childObj).size() == 0);
      
      if (count == 5)
      {
         Object sub_childObj = props
               .get("com.percussion.Smog.parent.emptySet2");

         assertTrue(sub_childObj == childObj);         
      }
      
      // empty values
      Object values = props.get("com.percussion.Smog.emptyValues");
      assertTrue(values instanceof List);
      assertTrue(((List)values).size() == 0);
      
      values = props.get("com.percussion.Smog.nullProperty");
      assertTrue(values == null);
   }
   
   /**
    * The local configuration file (for web-master) contains more advanced properties
    */
   private static final String LOCAL_FILE_NAME = "PSConfigNormalizerTest.xml";
   
   /**
    * The local configuration file contains empty map or list
    */
   private static final String LOCAL_EMPTY_FILE_NAME = "PSConfigNormalizerTest_Empty.xml";
   
}
