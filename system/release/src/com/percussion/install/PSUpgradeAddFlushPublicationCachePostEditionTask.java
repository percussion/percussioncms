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

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

/**
 * This upgrade plugin adds the flush publication cache post edition task to all editions that
 * don't currently have it.
 */

public class PSUpgradeAddFlushPublicationCachePostEditionTask extends PSSpringUpgradePluginBase
{

   /* (non-Javadoc)
    * @see com.percussion.install.IPSUpgradePlugin#process(com.percussion.install.IPSUpgradeModule, org.w3c.dom.Element)
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      PSPluginResponse response = null;
      try
      {
         logger = config.getLogStream();
         IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
         List<IPSEdition> editions = pubService.findAllEditions("");
         for(IPSEdition edition : editions)
         {
            List<IPSEditionTaskDef> tasks = pubService.loadEditionTasks(edition.getGUID());
            if(!hasFlushPublicationCacheTask(tasks))
            {
               /*
                * Add the flush publication cache post edition task on the editions if they don't have that already
                */
               logger.println("Adding the sys_flushPublicationCache post task on the edition " +
                     "that doesn't have it already");
               IPSEditionTaskDef flushPublicationCacheTask = pubService.createEditionTask();
               flushPublicationCacheTask.setContinueOnFailure(false);
               flushPublicationCacheTask.setEditionId(edition.getGUID());
               flushPublicationCacheTask.setSequence(3);
               flushPublicationCacheTask.setExtensionName(EXT_NAME);
               pubService.saveEditionTask(flushPublicationCacheTask);
               logger.println("Finished adding the sys_flushPublicationCache post task on the edition");
            }

         }
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e
               .getLocalizedMessage());
      }
      return response;
   }
   
   private boolean hasFlushPublicationCacheTask(Collection<IPSEditionTaskDef> tasks)
   {
      for(IPSEditionTaskDef task : tasks)
      {
         if(task.getExtensionName().equals(EXT_NAME))
            return true;
      }
      return false;
   }
   
   private static final String EXT_NAME = "Java/global/percussion/task/sys_flushPublicationCache";
   
   /**
    * Used for logging output to the plugin log file, initialized in
    * {@link #process(IPSUpgradeModule, Element)}. 
    */
   private static PrintStream logger;

}
