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
package com.percussion.services.touchitem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link PSTouchItemConfiguration} class.
 */
@Category(IntegrationTest.class)
public class PSTouchItemConfigurationTest extends ServletTestCase
{
   private PSTouchItemConfiguration configuration;
      
   public void setUp()
   {
      configuration = new PSTouchItemConfiguration();
      
      PSTouchItemConfigBean bean1 = new PSTouchItemConfigBean();
      Set<String> srcTypes = new HashSet<String>();
      srcTypes.add("rffFile");
      Set<String> tgtTypes = new HashSet<String>();
      tgtTypes.add("rffGeneric");
      bean1.setSourceTypes(srcTypes);
      bean1.setTargetTypes(tgtTypes);
      bean1.setLevel(0);
      bean1.setTouchAAParents(false);
      
      PSTouchItemConfigBean bean2 = new PSTouchItemConfigBean();
      srcTypes = new HashSet<String>();
      srcTypes.add("rffFile");
      srcTypes.add("rffBrief");
      tgtTypes = new HashSet<String>();
      tgtTypes.add("rffGeneric");
      tgtTypes.add("rffImage");
      bean2.setSourceTypes(srcTypes);
      bean2.setTargetTypes(tgtTypes);
      bean2.setLevel(-1);
      bean2.setTouchAAParents(true);
      
      PSTouchItemConfigBean bean3 = new PSTouchItemConfigBean();
      srcTypes = new HashSet<String>();
      srcTypes.add("rffFile");
      srcTypes.add("rffBrief");
      srcTypes.add("rffContacts");
      
      tgtTypes = new HashSet<String>();
      tgtTypes.add("rffGeneric");
      tgtTypes.add("rffImage");
      tgtTypes.add("rffNavTree");
      tgtTypes.add("rffCalendar");
      bean3.setSourceTypes(srcTypes);
      bean3.setTargetTypes(tgtTypes);
      bean3.setLevel(-2);
      bean3.setTouchAAParents(false);
      
      Set<PSTouchItemConfigBean> configBeans =
         new HashSet<PSTouchItemConfigBean>();
      configBeans.add(bean1);
      configBeans.add(bean2);
      configBeans.add(bean3);
      
      configuration.setTouchItemConfig(configBeans);
   }
   
   public void testGetTouchItemConfigMap() throws Exception
   {
      PSTouchItemConfiguration itemConfig =
         new PSTouchItemConfiguration();
      assertTrue(itemConfig.getTouchItemConfig().isEmpty());
      assertTrue(itemConfig.getTouchItemConfigMap().isEmpty());
      
      Map<Long, Set<PSTouchItemConfigBean>> configMap =
         configuration.getTouchItemConfigMap();
      assertEquals(3, configMap.size());
      
      Set<PSTouchItemConfigBean> configBeans = 
         configMap.get(Long.valueOf(309));
      assertEquals(3, configBeans.size());
      
      configBeans = configMap.get(Long.valueOf(302));
      assertEquals(2, configBeans.size());
      
      configBeans = configMap.get(Long.valueOf(305));
      assertEquals(1, configBeans.size());
   }
   
   public void testGetLevelTargetTypes() throws Exception
   {
      Map<Integer, Set<String>> levelTargetTypes = 
         configuration.getLevelTargetTypes(Long.valueOf(309));
      
      Set<String> targetTypes = levelTargetTypes.get(0);
      assertEquals(1, targetTypes.size());
      assertTrue(targetTypes.contains("rffGeneric"));
      
      targetTypes = levelTargetTypes.get(-1);
      assertEquals(2, targetTypes.size());
      assertTrue(targetTypes.contains("rffGeneric"));
      assertTrue(targetTypes.contains("rffImage"));
      
      targetTypes = levelTargetTypes.get(-2);
      assertEquals(4, targetTypes.size());
      assertTrue(targetTypes.contains("rffGeneric"));
      assertTrue(targetTypes.contains("rffImage"));
      assertTrue(targetTypes.contains("rffNavTree"));
      assertTrue(targetTypes.contains("rffCalendar"));
      
      levelTargetTypes = 
         configuration.getLevelTargetTypes(Long.valueOf(302));
      
      targetTypes = levelTargetTypes.get(0);
      assertNull(targetTypes);
      
      targetTypes = levelTargetTypes.get(-1);
      assertEquals(2, targetTypes.size());
      assertTrue(targetTypes.contains("rffGeneric"));
      assertTrue(targetTypes.contains("rffImage"));
      
      targetTypes = levelTargetTypes.get(-2);
      assertEquals(4, targetTypes.size());
      assertTrue(targetTypes.contains("rffGeneric"));
      assertTrue(targetTypes.contains("rffImage"));
      assertTrue(targetTypes.contains("rffNavTree"));
      assertTrue(targetTypes.contains("rffCalendar"));
      
      levelTargetTypes = 
         configuration.getLevelTargetTypes(Long.valueOf(305));
      
      targetTypes = levelTargetTypes.get(0);
      assertNull(targetTypes);
      
      targetTypes = levelTargetTypes.get(-1);
      assertNull(targetTypes);
      
      targetTypes = levelTargetTypes.get(-2);
      assertEquals(4, targetTypes.size());
      assertTrue(targetTypes.contains("rffGeneric"));
      assertTrue(targetTypes.contains("rffImage"));
      assertTrue(targetTypes.contains("rffNavTree"));
      assertTrue(targetTypes.contains("rffCalendar"));           
   }
   
   public void testShouldTouchAAParents() throws Exception
   {
      Set<String> tgtTypes = new HashSet<String>();
      tgtTypes.add("rffGeneric");
      assertFalse(configuration.shouldTouchAAParents(Long.valueOf(309),
            0, tgtTypes));
      assertFalse(configuration.shouldTouchAAParents(Long.valueOf(309),
            -1, tgtTypes));
     
      tgtTypes.add("rffImage");
      assertTrue(configuration.shouldTouchAAParents(Long.valueOf(309),
            -1, tgtTypes));
      assertTrue(configuration.shouldTouchAAParents(Long.valueOf(302),
            -1, tgtTypes));
      
      tgtTypes.add("rffNavTree");
      tgtTypes.add("rffCalendar");
      assertFalse(configuration.shouldTouchAAParents(Long.valueOf(309),
            -2, tgtTypes));
      assertFalse(configuration.shouldTouchAAParents(Long.valueOf(302),
            -2, tgtTypes));
      assertFalse(configuration.shouldTouchAAParents(Long.valueOf(305),
            -2, tgtTypes));
   }
   
   public void testGetMinimumLevel() throws Exception
   {
      PSTouchItemConfiguration itemConfig =
         new PSTouchItemConfiguration();
      assertNull(itemConfig.getMinimumLevel());
      
      assertTrue(configuration.getMinimumLevel() == -2);
   }
   
}


