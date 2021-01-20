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
import com.percussion.rx.config.impl.PSEditionSetter;
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.rx.design.impl.PSEditionWrapper;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Class description
 *
 * @author YuBingChen
 */
@SuppressWarnings("unchecked")
@Category(IntegrationTest.class)
public class PSEditionSetterTest extends PSConfigurationTest
{
   /**
    * Creates an Edition Wrapper with 1 pre-task and 2 post-tasks
    * 
    * @return the created edition wrapper object, never <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private PSEditionWrapper createEdition() throws Exception
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = factory.getDesignModel(PSTypeEnum.EDITION);
      PSEditionWrapper wrapper = (PSEditionWrapper) model
            .loadModifiable(EDITION_NAME);

      // create the setter
      PSEditionSetter setter = new PSEditionSetter();

      // set Content Type setter
      Map<String, Object> props = new HashMap<String, Object>();

      String MY_PRIORITY = "HIGHEST";

      props.put("priority", MY_PRIORITY);
      props.put("preTasks", ms_preTasks);
      props.put("postTasks", ms_postTasks);

      setter.setProperties(props);

      // perform the test
      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(wrapper, ObjectState.BOTH, null);

      model.save(wrapper, null);
      wrapper = (PSEditionWrapper) model.loadModifiable(EDITION_NAME);

      return wrapper;
   }

   public void testAddPropertyDefs() throws Exception
   {
      PSEditionWrapper wrapper = createEdition();
      PSEditionSetter setter = new PSEditionSetter();
      
      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      
      props.put(PSEditionSetter.PRE_TASKS, "${perc.prefix.pre_tasks}");
      props.put(PSEditionSetter.POST_TASKS, "${perc.prefix.post_tasks}");
      
      setter.setProperties(props);
      setter.addPropertyDefs(wrapper, defs);
      
      // validate
      assertTrue("Expect 2 defs", defs.size() == 2);
      List<Map<String, Object>> preTask = (List<Map<String, Object>>)defs.get("perc.prefix.pre_tasks");
      List<Map<String, Object>> postTask = (List<Map<String, Object>>)defs.get("perc.prefix.post_tasks");
      
      assertTrue("Expect 2 pre-tasks", preTask.size() == 2);
      assertTrue("Expect 1 post-tasks", postTask.size() == 1);
      Map<String, Object> pstTask = postTask.get(0);
      assertTrue("Expect 2 element in post-task", pstTask.size() == 2);
      assertTrue(
            "Expect \"Java/global/percussion/task/sys_editionCommandTask\"",
            pstTask.get("extensionName").equals(
                  "Java/global/percussion/task/sys_editionCommandTask"));
   }
   
   public void testEditionProperties() throws Exception
   {
      PSEditionWrapper wrapper = createEdition();
      
      wrapper.getEdition().getPriority().equals(IPSEdition.Priority.HIGHEST);
      assertTrue(wrapper.getPreTasks().size() == 2);
      assertTrue(wrapper.getPostTasks().size() == 1);

      // validate the sequence of the pre-tasks.
      // "continueOnFailure" == false is the 1st task
      // "continueOnFailure" == true is the 2nd task
      List<IPSEditionTaskDef> preTasks = wrapper.getPreTasks();
      if (!preTasks.get(0).getContinueOnFailure())
      {
         // 1st element is the 1st task
         assertTrue(preTasks.get(0).getSequence() < preTasks.get(1)
               .getSequence());         
      }
      else
      {
         // 1st element is the 2nd task
         assertTrue(preTasks.get(0).getSequence() > preTasks.get(1)
               .getSequence());
      }
      
      //\/\/\/\/\/\/\/\
      // cleanup
      cleanup();
   }

   private void cleanup() throws Exception
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = factory.getDesignModel(PSTypeEnum.EDITION);
      PSEditionWrapper wrapper = (PSEditionWrapper) model
            .loadModifiable(EDITION_NAME);

      Map<String, Object> props = new HashMap<String, Object>();
      props.clear();
      props.put("priority", "LOWEST");
      // a list with an empty map is the same as empty list/map
      props.put("preTasks", Collections.singletonList(Collections.emptyMap()));
      // empty map is the same as empty list
      props.put("postTasks", Collections.emptyMap()); 
      
      PSEditionSetter setter = new PSEditionSetter();
      setter.setProperties(props);

      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(wrapper, ObjectState.BOTH, null);
      h.saveResult(model, wrapper, ObjectState.BOTH, null);
      
      wrapper = (PSEditionWrapper) model.loadModifiable(EDITION_NAME);

      wrapper.getEdition().getPriority().equals(IPSEdition.Priority.LOWEST);
      assertTrue(wrapper.getPreTasks() == null);
      assertTrue(wrapper.getPostTasks() == null);
   }
   
   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);

      //\/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
   }
   
   private static List<Map<String, Object>> ms_preTasks = new ArrayList<Map<String, Object>>();
   private static List<Map<String, Object>> ms_postTasks = new ArrayList<Map<String, Object>>();
   
   /**
    * The pre-tasks properties
    */
   private static Map<String, Object> ms_preTaskProps = new HashMap<String, Object>();
   private static Map<String, Object> ms_preTaskProps2 = new HashMap<String, Object>();
   
   /**
    * The post-tasks properties
    */
   private static Map<String, Object> ms_postTaskProps = new HashMap<String, Object>();
   
   private static List<PSPair<String, String>> ms_params = new ArrayList<PSPair<String, String>>();
   static
   {
      ms_params.add(new PSPair("command", "someCooamd ${edition_name}"));
      
      // set the pre task properties.
      ms_preTaskProps.put("extensionName", "Java/global/percussion/task/sys_editionCommandTask");
      ms_preTaskProps.put("continueOnFailure", "false");
      ms_preTaskProps.put("extensionParams", ms_params);

      ms_preTaskProps2.put("extensionName", "Java/global/percussion/task/sys_editionCommandTask");
      ms_preTaskProps2.put("continueOnFailure", "true");
      ms_preTaskProps2.put("extensionParams", ms_params);

      
      ms_postTaskProps.put("extensionName", "Java/global/percussion/task/sys_editionCommandTask");
      ms_postTaskProps.put("continueOnFailure", "false");
      ms_postTaskProps.put("extensionParams", ms_params);
      
      ms_preTasks.add(ms_preTaskProps);
      ms_preTasks.add(ms_preTaskProps2);

      ms_postTasks.add(ms_postTaskProps);
   }

   private static final String EDITION_NAME = "CI_Full";

   public static final String PKG_NAME = "PSEditionSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_ConfigDefs.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_LocalConfigs.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_DefaultConfigs.xml";

}
