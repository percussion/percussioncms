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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.fastforward.calendar;

import com.percussion.util.PSDataTypeConverter;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.w3c.dom.Element;

/**
 * Represents an event that recurs for Mitre's Rhythmyx calendaring system.
 * 
 * @author James Schultz
 */
public class PSRecurringEvent
{

   /**
    * Constructs a <code>RecurringEvent</code> with the specified parameters.
    * 
    * @param startDate Date of the first occurrence of the event
    * @param endDate Date beyond which no occurrences occur
    * @param interval how many units of time pass between occurrences
    * @param intervalType Sets the interval type: {@link #DAILY_RECURRENCE},
    *           {@link #WEEKLY_RECURRENCE},{@link #MONTHLY_RECURRENCE_DAY},
    *           or {@link #MONTHLY_RECURRENCE_WEEK}
    * @param dayOfMonth The day of month (1-31) that a montly recurring event
    *           lands on. Must be assigned a value when using a monthly-by-day
    *           interval type.
    * @param dayOfWeek The day of week that a montly recurring event lands on.
    *           Must be assigned a value when using a monthly-by-week interval
    *           type.
    * @param dayOfWeekOccurrence The week of the month (1-4 or
    *           {@link #LAST_WEEK_OF_MONTH}) a montly recurring event lands on.
    *           Must be assigned a value when using a monthly-by-week interval
    *           type.
    */
   public PSRecurringEvent(Date startDate, Date endDate, int interval,
         int intervalType, int dayOfMonth, int dayOfWeek,
         int dayOfWeekOccurrence) {
      setStartDate(startDate);
      setEndDate(endDate);
      setInterval(interval);
      setDayOfMonth(dayOfMonth);
      setDayOfWeek(dayOfWeek);
      setDayOfWeekOccurrence(dayOfWeekOccurrence);
      // set interval type last, as it will perform parameter checking
      try
      {
         setIntervalType(intervalType);
      }
      catch (IllegalStateException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }

   }

   /**
    * Constructs a <code>RecurringEvent</code> from its XML representation:
    * <code><pre>
    * 
    *  &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
    *  &lt;!ELEMENT event (interval, dayOfWeek?, dayOfMonth?)&gt;
    *  &lt;!ATTLIST event
    *     sys_contentid CDATA #REQUIRED
    *     startDate CDATA #REQUIRED
    *     endDate CDATA #REQUIRED
    *  &gt;
    *  &lt;!ELEMENT interval (#PCDATA)&gt;
    *  &lt;!ATTLIST interval
    *     type (d|w|md|mw)
    *  &gt;
    *  &lt;!ELEMENT dayOfWeek (#PCDATA)&gt;
    *  &lt;!ELEMENT dayOfMonth (#PCDATA)&gt;
    *  &lt;!ATTLIST dayOfWeek
    *     occurrence CDATA #REQUIRED
    *  &gt;
    *  
    * </pre></code>
    * 
    * @param root an XML tree that must conform to the specified representation
    * @throws UnknownNodeTypeException if the XML tree does not conform to the
    *            specified representation
    * @throws IllegalValueException if the XML tree contains illegal values for
    *            construction of a recurrent event
    */
   public PSRecurringEvent(Element root) throws UnknownNodeTypeException,
         IllegalValueException {
      if (!(root.getTagName().equals(EVENT)))
         throw new UnknownNodeTypeException("Root element must be named: "
               + EVENT);

      /* parse the attributes of the root element */
      String intervalType = null;
      try
      {
         String startDateString = root.getAttribute("startDate");
         if (startDateString == null)
            throw new UnknownNodeTypeException(
                  "Missing required attribute: startDate");
         String endDateString = root.getAttribute("endDate");
         if (endDateString == null)
            throw new UnknownNodeTypeException(
                  "Missing required attribute: endDate");

         // throw an explicit exception if the XML contains illegal values
         try
         {
            setStartDate(PSDataTypeConverter.parseStringToDate(startDateString));
            setEndDate(PSDataTypeConverter.parseStringToDate(endDateString));
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalValueException("Illegal start or end date value.");
         }
         catch (IllegalStateException e)
         {
            throw new IllegalValueException("Illegal start or end date value.");
         }
         
         PSXmlTreeWalker walker = new PSXmlTreeWalker(root);
         /* process the child elements of the root element */
         Element intervalElem = walker.getNextElement(INTERVAL_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (intervalElem == null)
            throw new UnknownNodeTypeException("Missing required element: "
                  + INTERVAL_ELEM);
         try
         {
            setInterval(Integer.parseInt(PSXmlTreeWalker.getElementData(intervalElem)));
         }
         catch (NumberFormatException e)
         {
            throw new UnknownNodeTypeException("Could not parse "
                  + INTERVAL_ELEM);
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalValueException("Illegal interval value.");
         }

         /*
          * dayOfWeek is an optional element (not needed for daily or weekly
          * recurrences) so it is not an error if it is missing
          */
         Element dayOfWeek = walker.getNextElement(DAYOFWEEK_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (dayOfWeek != null)
         {
            String data = walker.getElementData(dayOfWeek);
            try
            {
               setDayOfWeek(Integer.parseInt(data));
            }
            catch (NumberFormatException e)
            {
               throw new UnknownNodeTypeException("Could not parse "
                     + DAYOFWEEK_ELEM + ": " + data);
            }
            String occurrence = dayOfWeek.getAttribute(OCCURRENCE_ATTR);
            if (occurrence == null)
               throw new UnknownNodeTypeException(
                     "Missing required attribute: " + OCCURRENCE_ATTR);
            try
            {
               setDayOfWeekOccurrence(Integer.parseInt(occurrence));
            }
            catch (NumberFormatException e)
            {
               throw new UnknownNodeTypeException("Could not parse "
                     + OCCURRENCE_ATTR);
            }
         }

         /*
          * dayOfMonth is an optional element (not needed for daily or weekly
          * recurrences) so it is not an error if it is missing
          */
         Element dayOfMonth = walker.getNextElement(DAYOFMONTH_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (dayOfMonth != null)
         {
            String data = walker.getElementData(dayOfMonth);
            try
            {
               setDayOfMonth(Integer.parseInt(data));
            }
            catch (NumberFormatException e)
            {
               throw new UnknownNodeTypeException("Could not parse "
                     + DAYOFMONTH_ELEM + ": " + data);
            }

         }

         // set interval type last, as it will perform parameter checking
         try
         {
            intervalType = intervalElem.getAttribute(TYPE_ATTR);
            if (intervalType == null)
               throw new UnknownNodeTypeException(
                     "Missing required attribute: " + TYPE_ATTR);
            setIntervalType(intervalType);
         }
         catch (IllegalStateException e)
         {
            throw new UnknownNodeTypeException(e.getMessage());
         }

      }
      catch (IllegalArgumentException e)
      {
         throw new UnknownNodeTypeException(e.getMessage());
      }

   }

   private void setStartDate(Date startDate)
   {
      if (startDate == null)
         throw new IllegalArgumentException(
               "Illegal startDate (must not be null).");

      if (m_endDate != null && m_endDate.compareTo(startDate) > 0)
         throw new IllegalStateException("startDate may not come before enDate");

      m_startDate = startDate;
   }

   private void setEndDate(Date endDate)
   {
      if (endDate == null)
         throw new IllegalArgumentException(
               "Illegal endDate (must not be null).");

      if (m_startDate != null && m_startDate.compareTo(endDate) > 0)
         throw new IllegalStateException("endDate " + endDate
               + "may not come before startDate " + m_startDate);

      m_endDate = endDate;
   }

   /**
    * Sets the interval of recurrence (how many units of time pass between
    * occurrences).
    * 
    * @param interval the interval of recurrence, must be greater than zero.
    */
   private void setInterval(int interval)
   {
      if (interval < 1)
         throw new IllegalArgumentException("Illegal interval (must be > 0).");
      m_interval = interval;
   }

   /**
    * Sets the interval type (daily, weekly, or monthly)
    * 
    * @param intervalType must be one of the following codes:
    *           <ul>
    *           <li>{@link #DAILY_RECURRENCE}
    *           <li>{@link #WEEKLY_RECURRENCE}
    *           <li>{@link #MONTHLY_RECURRENCE_DAY}
    *           <li>{@link #MONTHLY_RECURRENCE_WEEK}
    *           </ul>
    * 
    * @throws IllegalStateException if the fields needed to resolve recurrences
    *            for the desired interval type have not been specified.
    */
   private void setIntervalType(int intervalType)
   {
      if (intervalType != DAILY_RECURRENCE
            && intervalType != WEEKDAILY_RECURRENCE
            && intervalType != WEEKLY_RECURRENCE
            && intervalType != MONTHLY_RECURRENCE_DAY
            && intervalType != MONTHLY_RECURRENCE_WEEK)
         throw new IllegalArgumentException("Illegal intervalType: "
               + intervalType);
      m_intervalType = intervalType;

      /* validate the other parameters based on the interval type */
      if (intervalType == MONTHLY_RECURRENCE_DAY
            && (m_dayOfMonth < 1 || m_dayOfMonth > 31))
         throw new IllegalStateException(
               "When recurring by day of month, must provide a valid day.");
      if (intervalType == MONTHLY_RECURRENCE_WEEK
            && (m_dayOfWeek < 1 || m_dayOfWeekOccurrence == 0 || m_dayOfWeekOccurrence > 4))
         throw new IllegalStateException(
               "When recurring by day of week of month, must provide a valid day of week.");

   }

   /**
    * Sets the interval type (daily, weekly, or monthly)
    * 
    * @param intervalType must be one of the following codes:
    *           <ul>
    *           <li><b>d </b>: DAILY_RECURRENCE
    *           <li><b>dd </b>: WEEKDAILY_RECURRENCE
    *           <li><b>w </b>: WEEKLY_RECURRENCE
    *           <li><b>md </b>: MONTHLY_RECURRENCE_DAY
    *           <li><b>mw </b>: MONTHLY_RECURRENCE_WEEK
    *           </ul>
    * 
    * @throws IllegalStateException if the fields needed to resolve recurrences
    *            for the desired interval type have not been specified.
    */
   private void setIntervalType(String intervalType)
   {
      if (intervalType == null)
         throw new IllegalArgumentException("Illegal intervalType: "
               + intervalType);

      intervalType = intervalType.trim();
      if (intervalType.equals("d"))
         setIntervalType(DAILY_RECURRENCE);
      else if (intervalType.equals("dd"))
         setIntervalType(WEEKDAILY_RECURRENCE);
      else if (intervalType.equals("w"))
         setIntervalType(WEEKLY_RECURRENCE);
      else if (intervalType.equals("md"))
         setIntervalType(MONTHLY_RECURRENCE_DAY);
      else if (intervalType.equals("mw"))
         setIntervalType(MONTHLY_RECURRENCE_WEEK);
      else
         throw new IllegalArgumentException("Illegal intervalType: "
               + intervalType);
   }

   /**
    * Set day of the week.
    * @param dayOfWeek must be a valid number.
    */
   public void setDayOfWeek(int dayOfWeek)
   {
      // todo validation of dayOfWeek
      m_dayOfWeek = dayOfWeek;
   }

   /**
    * Set the day of wee occurrence.
    * @param dayOfWeekOccurrence
    */
   public void setDayOfWeekOccurrence(int dayOfWeekOccurrence)
   {
      // todo validation of dayOfWeekOccurrence
      m_dayOfWeekOccurrence = dayOfWeekOccurrence;
   }

   /**
    * Set day of month.
    * @param dayOfMonth
    */
   public void setDayOfMonth(int dayOfMonth)
   {
      // todo validation of dayOfMonth
      m_dayOfMonth = dayOfMonth;
   }

   /**
    * Gets the date of the specified recurrence of this event.
    * 
    * @param recurrence
    * @return a calendar (with the default time zone and locale) set to the date
    *         of the specified recurrence of this event, or <code>null</code>
    *         if the date of the recurrence is greater than the end date of this
    *         event.
    */
   public Calendar getRecurrence(int recurrence)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(m_startDate);

      if (recurrence > 0)
      {
         if (m_intervalType == DAILY_RECURRENCE)
         {
            calendar.add(Calendar.DATE, recurrence * m_interval);
         }
         else if (m_intervalType == WEEKDAILY_RECURRENCE)
         {
            for (int i = 0; i < recurrence; i++)
            {
               calendar.add(Calendar.DATE, 1);
               // skip the weekends
               while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                     || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                  calendar.add(Calendar.DATE, 1);
            }
         }
         else if (m_intervalType == WEEKLY_RECURRENCE)
         {
            calendar.add(Calendar.WEEK_OF_YEAR, recurrence * m_interval);
         }
         else if (m_intervalType == MONTHLY_RECURRENCE_DAY)
         {
            // set the correct date of the month
            calendar.set(Calendar.DATE, m_dayOfMonth);
            // advance to the correct month
            calendar.add(Calendar.MONTH, recurrence * m_interval);
         }
         else if (m_intervalType == MONTHLY_RECURRENCE_WEEK)
         {
            assignMonthlyByWeekRecurrence(calendar, recurrence);
         }
      }

      // do not recur past the end date of the event
      if (calendar.getTime().compareTo(m_endDate) <= 0)
         return calendar;
      else
         return null;
   }

   private void assignMonthlyByWeekRecurrence(Calendar calendar, int recurrence)
   {
      // advance to the correct month
      calendar.add(Calendar.MONTH, recurrence * m_interval);

      if (m_dayOfWeekOccurrence == LAST_WEEK_OF_MONTH)
      {
         /*
          * start at the last day of the month and step backwards day by day
          * until we land on the day we want
          */
         calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
         while (calendar.get(Calendar.DAY_OF_WEEK) != m_dayOfWeek)
            calendar.add(Calendar.DATE, -1);
      }
      else
      {
         /*
          * start at the first day of the month and advance day by day until we
          * land on the day we want, then jump the desired number of weeks
          */
         calendar.set(Calendar.DATE, calendar.getMinimum(Calendar.DATE));
         while (calendar.get(Calendar.DAY_OF_WEEK) != m_dayOfWeek)
            calendar.add(Calendar.DATE, 1);

         calendar.add(Calendar.DATE, (m_dayOfWeekOccurrence - 1) * 7);
      }
   }

   /**
    * Gets an iterator that can be used to step through each recurrence of this
    * event.
    * 
    * @return an iterator that can be used to step through each recurrence of
    *         this event, never <code>null</code>.
    */
   public Iterator getRecurrenceIterator()
   {
      return new PSRecurrenceIterator(this);
   }

   /**
    * @return start date of the event.
    */
   public Date getStartDate()
   {
      return m_startDate;
   }

   /**
    * Value of the interval type field indicating daily recurrences.
    */
   public static final int DAILY_RECURRENCE = 0;

   /**
    * Value of the interval type field indicating week-daily (no weekend)
    * recurrences.
    */
   public static final int WEEKDAILY_RECURRENCE = 1;

   /**
    * Value of the interval type field indicating weekly recurrences.
    */
   public static final int WEEKLY_RECURRENCE = 2;

   /**
    * Value of the interval type field indicating monthly recurrences on a
    * specific date.
    */
   public static final int MONTHLY_RECURRENCE_DAY = 3;

   /**
    * Value of the interval type field indicating monthly recurrences on a
    * specific week day and week number.
    */
   public static final int MONTHLY_RECURRENCE_WEEK = 4;

   /**
    * Value of the day of week occurrence field indicating the last week of the
    * month.
    */
   public static final int LAST_WEEK_OF_MONTH = -1;

   private Date m_startDate;

   private Date m_endDate;

   /**
    * The interval of recurrence.
    */
   private int m_interval;

   private int m_intervalType;

   /**
    * The day of week that a montly recurring event lands on. Must be assigned a
    * value when using a monthly-by-week interval type.
    */
   private int m_dayOfWeek;

   /**
    * The week of the month (1-4 or {@link #LAST_WEEK_OF_MONTH}) a montly
    * recurring event lands on. Must be assigned a value when using a
    * monthly-by-week interval type.
    */
   private int m_dayOfWeekOccurrence;

   /**
    * The day of month (1-31) that a montly recurring event lands on. Must be
    * assigned a value when using a monthly-by-day interval type.
    */
   private int m_dayOfMonth;

   /**
    * Name of the XML element used to represent a recurring event.
    */
   public static final String EVENT = "event";

   /**
    * Name of the XML attribute used to represent a recurring event's interval
    * type
    */
   public static final String TYPE_ATTR = "type";

   /**
    * Name of the XML element used to represent a recurring event's interval
    */
   public static final String INTERVAL_ELEM = "interval";

   /**
    * Name of the XML element used to represent the day of week that a montly
    * recurring event lands on.
    */
   public static final String DAYOFWEEK_ELEM = "dayOfWeek";

   /**
    * Name of the XML atribute used to represent which week of the month a
    * montly recurring event lands on.
    */
   public static final String OCCURRENCE_ATTR = "occurrence";

   /**
    * Name of the XML element used to represent the day of month that a montly
    * recurring event lands on.
    */
   public static final String DAYOFMONTH_ELEM = "dayOfMonth";

   /**
    * An internal exception to indicate that the supplied XML representation
    * does not match the required format.
    */
   public class UnknownNodeTypeException extends Exception
   {
      /**
       * Just calls the super class version.
       * @param s
       */
      public UnknownNodeTypeException(String s) {
         super(s);
      }
   };

   /**
    * An internal exception to indicate that the supplied XML representation
    * contains illegal values.
    */
   public class IllegalValueException extends Exception
   {
      /**
       * Just calls the super class version.
       * @param s
       */
      public IllegalValueException(String s) {
         super(s);
      }
   };

}
