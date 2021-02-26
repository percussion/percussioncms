/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.fastforward.calendar;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.assembly.IPSAssemblyResult;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Supports templates rendering a monthly calendar by providing details about the
 * month and its events.
 * 
 * @author James Schultz
 * @since 6.0
 */
public class PSCalendarMonthModel extends PSJexlUtilBase
{

   @IPSJexlMethod(description = "Assigns the calendar to the month containing the specified date.", params =
   {@IPSJexlParam(name = "date", type = "Date", description = "date within month to assign")})
   public PSCalendarMonthModel assign(Date date)
   {
      if (date == null)
         throw new IllegalArgumentException("date may not be null");

      PSCalendarMonthModel model = getModel();
      model.m_eventsByDay = null;
      model.m_cal.setTime(date);
      return model;
   }

   @IPSJexlMethod(description = "Assigns the specified calendar for computing month information.", params =
   {@IPSJexlParam(name = "cal", type = "Calendar", description = "calendar to use for computing month information")})
   public PSCalendarMonthModel assign(Calendar cal)
   {
      if (cal == null)
         throw new IllegalArgumentException("cal may not be null");
      
      PSCalendarMonthModel model = getModel();
      model.m_eventsByDay = null;
      model.m_cal = (Calendar) cal.clone();
      return model;
   }

   @IPSJexlMethod(description = "Assigns the calendar to the month containing the specified date.", params =
   {
         @IPSJexlParam(name = "format", type = "String", description = "format of date to assign"),
         @IPSJexlParam(name = "date", type = "String", description = "date within month to assign")})
   public PSCalendarMonthModel assign(String format, String date)
   {
      if (StringUtils.isBlank(format))
         throw new IllegalArgumentException("format may not be null or empty");
      if (StringUtils.isBlank(date))
         throw new IllegalArgumentException("date may not be null or empty");

      PSCalendarMonthModel model = getModel();
      FastDateFormat df = FastDateFormat.getInstance(format);
      try
      {
         model.m_eventsByDay = null;
         model.m_cal.setTime(df.parse(date));
      }
      catch (ParseException e)
      {
         ms_log.error(e);
      }
      return model;
   }

   /**
    * @return the number of weeks in the assigned month
    */
   public int getWeeks()
   {
      getModel().m_cal.set(Calendar.DAY_OF_MONTH, getLastDay());
      return getModel().m_cal.get(Calendar.WEEK_OF_MONTH);
   }

   /**
    * @return the last day of the assigned month
    */
   public int getLastDay()
   {
      return getModel().m_cal.getActualMaximum(Calendar.DAY_OF_MONTH);
   }

   /**
    * @return the day of the week for the first day of the assigned month
    */
   public int getFirstDayOfWeek()
   {
      getModel().m_cal.set(Calendar.DAY_OF_MONTH, 1);
      return getModel().m_cal.get(Calendar.DAY_OF_WEEK);
   }

   /**
    * @return the day of the week for the last day of the assigned month
    */
   public int getLastDayOfWeek()
   {
      getModel().m_cal.set(Calendar.DAY_OF_MONTH, getLastDay());
      return getModel().m_cal.get(Calendar.DAY_OF_WEEK);
   }

   /**
    * @return a calendar set to midnight on the first day of the assigned month,
    *         never <code>null</code>
    */
   public Calendar getStartDate()
   {
      PSCalendarMonthModel model = getModel();
      model.m_cal.set(Calendar.DAY_OF_MONTH, 1);
      model.m_cal.set(Calendar.HOUR_OF_DAY, 0);
      model.m_cal.set(Calendar.MINUTE, 0);
      model.m_cal.set(Calendar.SECOND, 0);
      model.m_cal.set(Calendar.MILLISECOND, 0);
      return (Calendar) model.m_cal.clone();
   }

   /**
    * @return the date of the first day of the assigned month, formatted as
    *         <code>yyyy-MM-dd</code>
    */
   public String getStart()
   {
      return getModel().m_formatter.format(getStartDate().getTime());
   }

   /**
    * @return a calendar set to the final millisecond of the last day of the
    *         assigned month, never <code>null</code>
    */
   public Calendar getEndDate()
   {
      PSCalendarMonthModel model = getModel();
      int lastDay = model.getLastDay();
      model.m_cal.set(Calendar.DAY_OF_MONTH, lastDay);
      model.m_cal.add(Calendar.DAY_OF_YEAR, 1);
      model.m_cal.add(Calendar.MILLISECOND, -1);
      return (Calendar) model.m_cal.clone();
   }

   /**
    * @return the date of the last day of the assigned month, formatted as
    *         <code>yyyy-MM-dd</code>
    */
   public String getEnd()
   {
      return getModel().m_formatter.format(getEndDate().getTime());
   }

   /**
    * Gets the events that occur on the specified day of the month.
    * 
    * @param day day of the month whose events will be returned
    * @return a collection of assembled events for the specified day. will be
    *         <code>null</code> if no events have been set or events occur on
    *         the specified day
    */
   public Collection<IPSAssemblyResult> getEvents(int day)
   {
      if (getModel().m_eventsByDay == null)
      {
         return null;
      }
      else
      {
         return (Collection<IPSAssemblyResult>) getModel().m_eventsByDay.get(day);
      }
   }

   /**
    * Sets the events that occur in the month. Any events whose start date does
    * not fall within the current month are ignored.  The event start date must
    * be available as a node property named {@link #EVENT_START_PROP_NAME 
    * rx:event_start}.
    * 
    * @param events list of event snippets to assign to the calendar, may be
    * <code>null</code> to clear events
    */
   public void setEvents(List<IPSAssemblyResult> events)
   {
      if (events == null)
      {
         getModel().m_eventsByDay = null;
      }
      else
      {
         PSCalendarMonthModel model = getModel();
         model.m_eventsByDay = new MultiHashMap();
         for (IPSAssemblyResult event : events)
         {
            Node event_node = event.getNode();
            if (event_node != null)
            {
               try
               {
                  Property event_start_prop = event_node
                        .getProperty(EVENT_START_PROP_NAME);
                  if (event_start_prop != null)
                  {
                     Calendar event_start = event_start_prop.getDate();
                     if (!(event_start.before(getStartDate()) || event_start
                           .after(getEndDate())))
                     {
                        Integer event_day = event_start
                              .get(Calendar.DAY_OF_MONTH);
                        model.m_eventsByDay.put(event_day, event);
                     }
                     else
                     {
                        ms_log.warn("event " + event.getId()
                              + " does not fall within current month");
                     }
                  }
                  else
                  {
                     ms_log.error("event " + event.getId()
                           + " does not have rx:event_start property");
                  }
               }
               catch (RepositoryException e)
               {
                  ms_log.error(e);
               }
            }
            else
            {
               ms_log.error("event " + event.getId() + " does not have a Node"
                     + " (may have been assembled by a legacy template)");
            }
         }
      }
   }

   /**
    * Get thread safe instance.
    * @return the instance, never <code>null</code>.
    */
   private PSCalendarMonthModel getModel()
   {
      if (m_model.get() == null)
      {
         m_model.set(new PSCalendarMonthModel());
      }
      return m_model.get();
   }
   
   /**
    * The name of the event field (jsr-170 property) that contains the event
    * start date.
    */
   public static final String EVENT_START_PROP_NAME = "rx:event_start";

   /**
    * Need to use a model setup for the specific thread to keep this thread safe
    */
   private static ThreadLocal<PSCalendarMonthModel> m_model = 
      new ThreadLocal<PSCalendarMonthModel>();
   
   /**
    * Used for calculating month details, never <code>null</code>.
    */
   private Calendar m_cal = Calendar.getInstance();

   /**
    * Used for formatting the start and end date strings, never
    * <code>null</code>
    */
   private FastDateFormat m_formatter = FastDateFormat.getInstance("yyyy-MM-dd");

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static Log ms_log = LogFactory.getLog(PSCalendarMonthModel.class);

   /**
    * A mapping of events, keyed by day of the month (Integer), returning a
    * list of IPSAssemblyResult.  Will be <code>null</code> until setEvents is 
    * called.
    */
   private MultiMap m_eventsByDay;

}
