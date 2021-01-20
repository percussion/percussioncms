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
package com.percussion.install;

import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

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
