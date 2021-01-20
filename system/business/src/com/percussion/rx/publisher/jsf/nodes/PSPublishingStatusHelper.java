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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.jsf.beans.PSRuntimeNavigation;
import com.percussion.rx.publisher.jsf.data.PSStatusLogEntry;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPubStatus.EndingState;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.impl.PSPublisherService;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Takes status from the publishing service and produces useful output for 
 * JSF.
 * 
 * @author dougrand
 *
 */
public class PSPublishingStatusHelper
{
   /**
    * Get the string representation of a given date.
    * @param date the Date object, never <code>null</code>.
    * @return the string representation of the date, never <code>null</code>.
    */
   public static String getDatetime(Date date)
   {
      if (date == null)
         throw new IllegalArgumentException("date may not be null.");
      
      DateFormat fmt = DateFormat.getDateTimeInstance();
      return fmt.format(date);
   }
   /**
    * Process status results from the publishing service into results that
    * can be displayed and used by JSF.
    * 
    * @param stati the status results, never <code>null</code>.
    * @param useAnimatedIcon <code>true</code> if use animated running icon.
    * @param navigation the runtime navigator, never <code>null</code>.
    * 
    * @return the processed results, never <code>null</code>.
    */
   public static List<Map<String, Object>> processStatus(
         List<IPSPubStatus> stati,
         boolean useAnimatedIcon,
         PSRuntimeNavigation navigation)
   {
      if (stati == null)
      {
         throw new IllegalArgumentException("stati may not be null");
      }
      if (navigation == null)
      {
         throw new IllegalArgumentException("navigation may not be null");
      }
      IPSRxPublisherServiceInternal rxpub = PSRxPubServiceInternalLocator
            .getRxPublisherService();
      IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
      Collection<Long> ids = rxpub.getActiveJobIds();
      List<Map<String,Object>> rval = new ArrayList<Map<String,Object>>();
      Map<Long, PSRuntimeNavigation.EditionSiteName> idmap = 
         navigation.getEditionIdNameMap();
      PSRuntimeNavigation.EditionSiteName names;
      for(IPSPubStatus status : stati)
      {
         Long jobId = status.getStatusId();
         Map<String,Object> data = new HashMap<String, Object>();
         data.put("statusid", jobId);
         
         // get the start time for display
         data.put("start", getDatetime(status.getStartDate()));
         // get the start time for sort
         data.put("startTime", status.getStartDate().getTime());
         
         data.put("elapsed", getElapseTime(status.getStartDate(),
               status.getEndDate()));
         data.put("delivered", status.getDeliveredCount());
         data.put("removed", status.getRemovedCount());
         data.put("failures", status.getFailedCount());
         Long editionId = new Long(status.getEditionId());
         
         boolean activeJob = ids.contains(jobId) || 
            ( ! pubService.getServerId().equals(status.getServer()) );
         
         String[] imgSrc = getStatusImage(status.getEndingState(), activeJob, useAnimatedIcon);
         data.put("statusImage", imgSrc[0]);
         data.put("statusDesc", imgSrc[1]);

         PSStatusLogEntry entry = new PSStatusLogEntry(jobId, navigation);
         entry.setTerminated(!IN_PROCESS.equals(imgSrc[1]));
         data.put("statusentry", entry);

         names = idmap.get(editionId);
         if (names != null)
         {
            data.put("edition", names.getEditionName());
            data.put("site", names.getSiteName());
         }
         else
         {
            data.put("edition", "Unknown Edition");
            data.put("site", "Unknown Site");
         }
         rval.add(data);
      }
      return rval;
   }

   /**
    * Gets the source and (short) description of the status image.
    * 
    * @param status the status, assumed not <code>null</code>.
    * @param isActiveJob <code>true</code> if this is an active job.
    * @param useAnimated <code>true</code> if use animated icon to display
    *    the running job; otherwise use non-animated icon to display the 
    *    running job, which is used for displaying publishing logs.
    * 
    * @return source (1st element) and description (2nd element) of the image,
    *    never <code>null</code> or empty.
    */
   public static String[] getStatusImage(IPSPubStatus.EndingState endState,
         boolean isActiveJob, boolean useAnimated)
   {
      if (endState == null)
         throw new IllegalArgumentException("endState may not be null.");
      
      String[] imgSrc = new String[2];
      imgSrc[0] = "../../sys_resources/images/";
      imgSrc[1] = null;
      
      if (endState.equals(EndingState.STARTED) && isActiveJob)
      {
         if (useAnimated)
            imgSrc[0] += "running.gif";
         else
            imgSrc[0] += "running_non-animated.png";
         imgSrc[1] = IN_PROCESS;
      }
      else if (endState.equals(EndingState.CANCELED_BY_USER))
      {
         imgSrc[0] += "canceled.png";
      }
      else if (endState.equals(EndingState.COMPLETED_W_FAILURE))
      {
         imgSrc[0] += "warning.png";
      }
      else if (endState.equals(EndingState.COMPLETED))
      {
         imgSrc[0] += "completed.png";
      }
      else
      {
         endState = EndingState.ABORTED;
         imgSrc[0] += "aborted.png";
      }
      if (imgSrc[1] == null)
         imgSrc[1] = getJobState(endState).getDisplayName();
      
      return imgSrc;
   }

   /**
    * Converts a state to the ending state.
    * @param state the state to be converted, assumed not <code>null</code>.
    * @return the ending state, never <code>null</code>.
    */
   public static EndingState getEndingState(State state)
   {
      if (state == null)
         throw new IllegalArgumentException("state may not be null.");
      
      if (!state.isTerminal())
         return EndingState.STARTED;
      
      if (State.COMPLETED.ordinal() == state.ordinal())
         return EndingState.COMPLETED;
      
      if (State.COMPLETED_W_FAILURE.ordinal() == state.ordinal())
         return EndingState.COMPLETED_W_FAILURE;

      if (State.CANCELLED.ordinal() == state.ordinal())
         return EndingState.CANCELED_BY_USER;

      if (State.PUBSERVERNEWDBCONFIG.ordinal() == state.ordinal())
         return EndingState.RESTARTNEEDED;

      return EndingState.ABORTED;
   }
   
   /**
    * Converts the {@link IPSPubStatus} to {@link IPSPublisherJobStatus.State}
    * @param endState the source status, not <code>null</code>.
    * @return the target state, never <code>null</code>.
    */
   public static IPSPublisherJobStatus.State getJobState(IPSPubStatus.EndingState endState)
   {
      if (endState == null)
         throw new IllegalArgumentException("endState may not be null.");
      
      if (endState.equals(EndingState.CANCELED_BY_USER))
      {
         return IPSPublisherJobStatus.State.CANCELLED;
      }
      else if (endState.equals(EndingState.COMPLETED_W_FAILURE))
      {
         return IPSPublisherJobStatus.State.COMPLETED_W_FAILURE;
      }
      else if (endState.equals(EndingState.COMPLETED))
      {
         return IPSPublisherJobStatus.State.COMPLETED;
      }
      else if (endState.equals(EndingState.RESTARTNEEDED))
      {
         return State.PUBSERVERNEWDBCONFIG;
      }
      else
      {
         return IPSPublisherJobStatus.State.ABORTED;
      }
   }
   
   /**
    * Constant for the description of in process jobs 
    */
   private static String IN_PROCESS = "In processing";
   
   /**
    * Milliseconds in an hour
    */
   private static int ONE_HOUR = 3600000;
   /**
    * Milliseconds in an minute
    */
   private static int ONE_MINUTE = 60000;
   
   /**
    * Gets the elapse time in HH:MM:SS format for the given start/end time.
    * 
    * @param startTime the start time, never <code>null</code>.
    * @param endTime the end time, it may be <code>null</code>.
    * 
    * @return the formated elapse time, never <code>null</code>, but
    *    may be empty if the endTime is <code>null</code>.
    */
   public static String getElapseTime(Date startTime, Date endTime)
   {
      if (startTime == null)
         throw new IllegalArgumentException("startTime may not be null.");
      
      if (endTime == null)
         return "";
      
      return convertMilliSecondToHhMmSs(endTime.getTime()
            - startTime.getTime());
   }

   /**
    * Converts and format a given milli-seconds to hours, minutes and seconds. 
    * @param elapsed the milli-seconds in question.
    * @return the formated string, never <code>null</code> or empty.
    */
   public static String convertMilliSecondToHhMmSs(long elapsed)
   {
      int hours = (int)(elapsed / ONE_HOUR);
      int minutes = (int)((elapsed % ONE_HOUR) / ONE_MINUTE);
      int seconds = (int) ((elapsed % ONE_MINUTE) / 1000);
      
      String format = "{0,number,00}:{1,number,00}:{2,number,00}";
      String retval = MessageFormat.format(format, hours, minutes, seconds);
      return retval;      
   }
   
   /**
    * The 100 percent value.
    */
   private static final int PERCENT_100 = 100;


   /**
    * Calculates job completion percent.
    * @param stat the job status. Assumed not <code>null</code>.
    * @return number between 0 and 100 indicating percent of job completion.
    */
   public static int getJobCompletionPercent(IPSPublisherJobStatus stat)
   {
      if (stat == null)
         throw new IllegalArgumentException("stst must not be null.");
      
      if (stat.getState().isTerminal())
      {
         return PERCENT_100;
      }
      // weight of all the work for a single item
      final int ALL_WORK_WEIGHT = 10;
      
      // weight of an item work, until the item reaches
      // "Prepared for delivery" state
      final int PREPARED_FOR_DELIVERY_WORK_WEIGHT = 9;

      final int preparedWeight =
            stat.countItemsPreparedForDelivery()
            * PREPARED_FOR_DELIVERY_WORK_WEIGHT;
      final int deliveredWeight =
            stat.countItemsDelivered() * ALL_WORK_WEIGHT;
      final int failedWeight =
            stat.countFailedItems() * ALL_WORK_WEIGHT;

      final int totalWeight = stat.countTotalItems() * ALL_WORK_WEIGHT;
      final int completedWeight =
            failedWeight + deliveredWeight + preparedWeight;
      
      final int completedPercent = stat.countTotalItems() > 0
          ? completedWeight * PERCENT_100 / totalWeight 
          : 0;
      return Math.min(
            completedPercent,
            stat.getState().getMaxpercent());
   }

   /**
    * Split messages by the message separator.
    * @param msg the message, may be empty or <code>null</code>
    * @return the list, which may have no elements.
    */
   public static List<String> splitMessages(String msg)
   {
      if (StringUtils.isBlank(msg))
      {
         return Collections.emptyList();
      }
      List<String> rval = new ArrayList<String>();
      while (StringUtils.isNotBlank(msg))
      {
         MsgSpliter ms = splitIndex(msg);
         if (ms.index < 0)
         {
            rval.add(msg);
            msg = null;
         }
         else
         {
            String partial = msg.substring(0, ms.index);
            if (partial.length() != 0)
            {
               rval.add(partial);
            }
            msg = msg.substring(ms.index + ms.pattern.length());
         }
      }
      return rval;
   }

   /**
    * Helper class for splitting message.
    */
   private static class MsgSpliter
   {
      /**
       * The index of the to be split message.
       */
      int index = -1;
      
      /**
       * The message spliter, one of the element in {@link #ms_knownSpliters}
       */
      String pattern = null;
   }

   /**
    * Gets the index of the given message if it contains a known line breaker.
    * @param msg the message in question, assumed not <code>null</code>.
    * @return the index. It may be <code>-1</code> if there is no line breaker.
    */
   private static MsgSpliter splitIndex(String msg)
   {
      MsgSpliter lb = new MsgSpliter();
      lb.index = -1;
      for (String br : ms_knownSpliters)
      {
         int i = msg.indexOf(br);
         if ((i > 0 && lb.index == -1) || ((i != -1) && i < lb.index))
         {
            lb.index = i;
            lb.pattern = br;
         }
      }
      return lb;
   }  
   
   /**
    * A list of known message spliters.
    */
   private static String[] ms_knownSpliters = {
      PSPublisherService.MESSAGE_SEPARATOR, "\n" };
   
}
