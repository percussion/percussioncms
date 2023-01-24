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
package com.percussion.services.schedule.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.purge.PSSqlPurgeHelperLocator;
import com.percussion.services.purge.data.RevisionData;
import com.percussion.services.schedule.IPSTask;
import com.percussion.services.schedule.IPSTaskResult;
import com.percussion.services.schedule.data.PSTaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * Purges revisions according to a criteria provided by the user.
 * 
 * The additional context variable can be used in notification template is :
 * <TABLEBORDER="1">
 * <TR>
 * <TH>Variable Name</TH>
 * <TH>Description</TH>
 * </TR>
 * <TR>
 * <TD>$FIXME</TD>
 * <TD>Enable archiving the to be purged logs before purging them. The logs will
 * be archived if this is true; otherwise the logs will be just purged from the
 * database. The location of the archived files is specified by the
 * archiveLocation property of sys_rxpublisherservice bean</TD>
 * </TR>
 * </TABLE>
 * 
 * @author Stas Burdan
 */
public class PSPurgeRevisions implements IPSTask
{

    public  PSPurgeRevisions(){
        //NOOP
    }

   /**
    * logger for this class.
    */
    private static final Logger m_log = LogManager.getLogger(PSPurgeRevisions.class);

   /**
    * * Revision purge logger
    */
    private static final Logger purge_logger = LogManager.getLogger("RevisionPurge");

   public IPSTaskResult perform(Map<String, String> params)
   {

      /*
       * ROUGH logic: FIXME min cond. # 1) ALWAYS keep minimum number X of
       * revisions to keep [number] min cond. date 2) ALWAYS keep all revisions
       * D younger than number of days [number]
       * 
       * if after that anything is left, than:
       * 
       * max cond # 3) delete all revisions above Y number (count) (delete
       * [Y+1,....max]) max cond date 4) delete all revisions above E days old
       * (delete [E+1,....max])
       * 
       * Y must be greater than X (if set). Error if not. E must be greater than
       * D (if set). Error if not.
       */

      String param1 = params.get("alwaysKeepMinNumberOfRevs");
      String param2 = params.get("alwaysKeepRevsYoungerThanDays");
      String param3 = params.get("deleteRevsAboveCount");
      String param4 = params.get("deleteRevsOlderThanDays");

      // FIXME: check params and various combinations, throw exceptions if wrong
      int keepMinNumberOfRevs, keepRevsYoungerThanDays, deleteRevsAboveCount, deleteRevsOlderThanDays;

      keepMinNumberOfRevs = parseInt(param1);
      keepRevsYoungerThanDays = parseInt(param2);
      deleteRevsAboveCount = parseInt(param3);
      deleteRevsOlderThanDays = parseInt(param4);

      RevisionData data = new RevisionData(keepMinNumberOfRevs, keepRevsYoungerThanDays, deleteRevsAboveCount,
            deleteRevsOlderThanDays);

      IPSTaskResult result;
      boolean isSuccess = true;
      String msg = "";
      long startTime = System.currentTimeMillis();
      long endTime;
      try
      {
         msg = "PurgeRevisions Params alwaysKeepMinNumberOfRevs:" + param1 + " alwaysKeepRevsYoungerThanDays:" + param2
               + " deleteRevsAboveCount:" + param3 + " deleteRevsOlderThanDays:" + param4;
         purge_logger.debug("Started " + msg);
         int revisionsRemoved = PSSqlPurgeHelperLocator.getPurgeHelper().purgeRevisions(data);
         purge_logger.debug("Finished Number Revisions purged = " + revisionsRemoved);
         msg += "\n Removed " + revisionsRemoved + " revisions";
         endTime = System.currentTimeMillis();
      }
      catch (Exception e)
      {
         isSuccess = false;
         endTime = System.currentTimeMillis();
         purge_logger.error("Failed to purge revisions.", e);
         msg += "\nERROR purging revisions, check server.log : " + e.getMessage();
      }

      result = new PSTaskResult(isSuccess, msg, PSScheduleUtils.getContextVars(params, startTime, endTime));

      return result;
   }

   /*
    * //see base class method for details
    */
   @SuppressWarnings("unused")
   public void init(@SuppressWarnings("unused")
   IPSExtensionDef def, @SuppressWarnings("unused")
   File codeRoot) throws PSExtensionException
   {
      // No initialization
   }

   private static int parseInt(String s)
   {
      int parsedResult = -1;
      try
      {
         parsedResult = Integer.parseInt(s);
      }
      catch (NumberFormatException x)
      {
         // ignore, return -1
      }
      return parsedResult;
   }

}
