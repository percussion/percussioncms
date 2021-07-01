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
package com.percussion.services.schedule.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.schedule.IPSTask;
import com.percussion.services.schedule.IPSTaskResult;
import com.percussion.services.schedule.data.PSTaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * This is used to purge log entries older than a specified date.
 *
 * @author Yu-Bing Chen
 */
public abstract class PSPurgeExpiredLog implements IPSTask
{
   /**
    * logger for this class.
    */
   private static final Logger m_log = LogManager.getLogger(PSPurgeExpiredLog.class);
   
   /**
    * Invokes the process of purging expired log entries. The returned task
    * result will contain the following variables, which can be used in
    * notification template. Note, the derived class may have additional 
    * parameters: <TABLEBORDER="1">
    * <TR>
    * <TH>Variable Name</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>$numberOfDays</TD>
    * <TD>The number of days of the expired (or to be purged) log entries.
    * Default to 30 days if not specified.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.executionDatetime</TD>
    * <TD>The starting date and time of the execution.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.executionElapsedTime</TD>
    * <TD>The duration of the execution.</TD>
    * </TR>
    * </TABLE>
    * <p>
    * The following context variables will be added by the framework: <TABLE
    * BORDER="1">
    * <TR>
    * <TH>Variable Name</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>$sys.taskName</TD>
    * <TD>The name of the scheduled task.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.completed</TD>
    * <TD>It is true if the command executed successfully; otherwise false.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.problemDesc</TD>
    * <TD>The problem description in case of execution failure.</TD>
    * </TR>
    * <TR>
    * <TD>$tools.*</TD>
    * <TD>The problem description in case of execution failure.</TD>
    * </TR>
    * </TABLE>
    * 
    * @param parameters It must contains a <code>command</code> parameter with
    * the native command and its associated arguments, never <code>null</code>
    * or empty.
    * 
    * @return the {@link IPSTaskResult#getNotificationVariables()} contains all
    * input parameters, never <code>null</code>.
    */
   public IPSTaskResult perform(Map<String,String> parameters)
   {
      IPSTaskResult result;
      boolean isSuccess = true;
      String errorCause = null;
      long startTime = System.currentTimeMillis();
      long endTime;
      Date beforeDate = getBeforeDate(parameters);
      try
      {
         m_log.info("Starting purging log entries...");
         purgeLogEntries(beforeDate, parameters);
         m_log.info("Completed purging log entries.");
         endTime = System.currentTimeMillis();
      }
      catch (Exception e)
      {
         isSuccess = false;
         endTime = System.currentTimeMillis();
         Throwable cause = e;
         if (e.getCause() != null)
            cause = e.getCause();
         m_log.error("Failed to purge log. Error: {}" + cause.getMessage());
         m_log.debug(cause);
         errorCause = cause.getLocalizedMessage();
      }
      
      result = new PSTaskResult(isSuccess, errorCause, 
            PSScheduleUtils.getContextVars(parameters, startTime, endTime));
      
      return result;
   }

   /**
    * Purges the log entries that are older than the specified date.
    * 
    * @param beforeDate the expiration date, never <code>null</code>.
    * @param parameters the parameters of the extension, never <code>null</code>.
    */
   protected abstract void purgeLogEntries(Date beforeDate,
         Map<String, String> parameters);
   
   /**
    * Gets the number of expiration days specified by the 
    * <code>numberDays</code> parameter. Defaults to 30 days if not specified
    * by the parameter.
    * 
    * @param parameters the parameters of the extension, assumed not 
    *    <code>null</code>.
    *    
    * @return the actual expiration date and time that is calculated from the
    *    current date and time back to the number of days described above. 
    *    Never <code>null</code>.
    */
   private Date getBeforeDate(Map<String,String> parameters)
   {
      String numParam = parameters.get("numberOfDays");
      int numberDays = 30;
      try
      {
         numberDays = Integer.parseInt(numParam);
      }
      catch (Exception e)
      {
      }

      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_MONTH, (numberDays * -1));

      return cal.getTime();
   }

   /*
    * //see base class method for details
    */
   @SuppressWarnings("unused")
   public void init(@SuppressWarnings("unused") IPSExtensionDef def, 
         @SuppressWarnings("unused") File codeRoot) throws PSExtensionException
   {
      // No initialization
   }
}
