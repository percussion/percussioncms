/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.install;

import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;

/**
 * This upgrade plugin adds the feeds info post edition task to all editions that
 * don't currently have it.
 * @author erikserating
 *
 */
public class PSUpgradeAddFeedPostEditionTasks extends PSSpringUpgradePluginBase
{

   /* (non-Javadoc)
    * @see com.percussion.install.IPSUpgradePlugin#process(com.percussion.install.IPSUpgradeModule, org.w3c.dom.Element)
    */
   public PSPluginResponse process(@SuppressWarnings("unused") IPSUpgradeModule config, @SuppressWarnings("unused") Element elemData)
   {
      IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
      PSPluginResponse response = null;
      
      List<IPSEdition> editions = pubService.findAllEditions("");
      for(IPSEdition edition : editions)
      {
         List<IPSEditionTaskDef> tasks = pubService.loadEditionTasks(edition.getGUID());
         if(!hasFeedTask(tasks))
         {
            //Add and save feed task
            IPSEditionTaskDef feedTask = pubService.createEditionTask();
            feedTask.setContinueOnFailure(false);
            feedTask.setEditionId(edition.getGUID());
            feedTask.setSequence(2);
            feedTask.setExtensionName("Java/global/percussion/task/perc_PushFeedDescriptorTask");
            pubService.saveEditionTask(feedTask);
         }
         
      }
      return response;
   }
   
   private boolean hasFeedTask(Collection<IPSEditionTaskDef> tasks)
   {
      for(IPSEditionTaskDef task : tasks)
      {
         if(task.getExtensionName().equals(EXT_NAME))
            return true;
      }
      return false;
   }
   
   private static final String EXT_NAME = "Java/global/percussion/task/perc_PushFeedDescriptorTask";

}
