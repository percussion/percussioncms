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

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents the collection of holidays entered into Mitre's Rhythmyx
 * calendaring system.
 * 
 * @author James Schultz
 */
public class PSHolidays
{

   /**
    * Constructs the holidays object, loading all holidays currently registered
    * with the system.
    * 
    * @param request used to make internal request to fetch holidays
    */
   public PSHolidays(IPSRequestContext request)
   {
      m_holidays = loadHolidays(request);
   }

   /**
    * Checks each holiday against the specified date, returning true if the
    * specified date matches a known holiday. Only year, month, and date are
    * compared, any time component (hour, minute, second, millisecond) is
    * ignored.
    * 
    * @param d the date to be tested, not <code>null</code>.
    * @return <code>true</code> if the specified m_date matches a known
    *         holiday, <code>false</code> otherwise.
    */
   public boolean isHoliday(Date d)
   {
      if (d == null)
         throw new IllegalArgumentException("m_date may not be null");

      for (Iterator i = m_holidays.iterator(); i.hasNext();)
      {
         Holiday holiday = (Holiday) i.next();
         if (holiday.isSameDate(d))
            return true;
      }
      return false;
   }

   /**
    * Gets the name of the holiday that matches the specified date, if any.
    * 
    * @param d the date to be tested, not <code>null</code>.
    * @return the name of the holiday scheduled for the specified date, or
    *         <code>null</code> if not holiday is scheduled.
    */
   public String getHoliday(Date d)
   {
      if (d == null)
         throw new IllegalArgumentException("date may not be null");

      for (Iterator i = m_holidays.iterator(); i.hasNext();)
      {
         Holiday holiday = (Holiday) i.next();
         if (holiday.isSameDate(d))
            return holiday.getName();
      }
      return null;
   }

   /**
    * Makes an internal request to {@link #HOLIDAY_CATALOGER}to retrieve and
    * XML document that contains the name and date of all holidays. The XML
    * document will conform to the following structure: <code><pre>
    * 
    *  &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
    *  &lt;!ELEMENT holidays (holiday*)&gt;
    *  &lt;!ELEMENT holiday (#PCDATA)&gt;
    *  &lt;!ATTLIST holiday
    *     day CDATA #REQUIRED
    *  &gt;
    *  
    * </pre></code>
    * <p>
    * This method is protected so it can be overridden by the test framework, to
    * allow testing the rest of the class without a running Rhythmyx server.
    * 
    * @param request the current request object, used for logging and resolving
    *           the internal request, must not be <code>null</code>.
    * @return An XML document containing the name and date of all holidays. Will
    *         be <code>null</code> if the internal request fails. Will be
    *         empty if no holidays have been registered.
    */
   protected Document executeCatalogerQuery(IPSRequestContext request)
   {
      if (request == null)
         throw new IllegalArgumentException("Must provide the request context");

      Document resultDoc = null;
      IPSInternalRequest ir = request.getInternalRequest(HOLIDAY_CATALOGER,
            null, false);
      if (ir == null)
      {
         StringBuffer error = new StringBuffer();
         error
               .append("Catalog of holidays failed, beacause the query resource '");
         error.append(HOLIDAY_CATALOGER);
         error.append("' was not found.");
         PSUtils.printTraceMessage(request, error.toString());
      }
      else
      {
         try
         {
            resultDoc = ir.getResultDoc();
         }
         catch (PSInternalRequestCallException e)
         {
            StringBuffer error = new StringBuffer();
            error.append("Catalog of holidays failed ");
            error
                  .append("because an error occurred during the internal request: ");
            error.append(e.toString());
            PSUtils.printTraceMessage(request, error.toString());
         }
      }
      return resultDoc;
   }

   /**
    * Loads the holidays.
    * <p>
    * This method is protected so it can be overridden by the test framework, to
    * allow testing the rest of the class without a running Rhythmyx server.
    * 
    * @param request request the current request object, used for logging and
    *           resolving the internal request, must not be <code>null</code>.
    * @return a set of <code>Holiday</code> objects, never <code>null</code>
    *         but may be empty.
    */
   protected Set loadHolidays(IPSRequestContext request)
   {
      Set holidays = new HashSet();
      Document results = executeCatalogerQuery(request);
      if (results != null)
      {
         NodeList nodes = results.getElementsByTagName("holiday");
         /*
          * NodeList loops should be structured with getLength() called outside
          * of the loop for performance and compatiblity with both Rhythmyx 4.5
          * and Rhythmyx 5
          */
         int numNodes = nodes.getLength();
         for (int i = 0; i < numNodes; i++)
         {
            Element holidayElem = (Element) nodes.item(i);
            String name = PSXmlTreeWalker.getElementData(holidayElem);
            String dayAttr = holidayElem.getAttribute("day");
            Date day = PSDataTypeConverter.parseStringToDate(dayAttr);
            if (day == null)
            {
               PSUtils.printTraceMessage(request,
                     "Could not parse day attribute: " + dayAttr);
            }
            else
            {
               Holiday h = new Holiday(day, name);
               holidays.add(h);
            }
         }
      }
      return holidays;
   }

   /**
    * Path to the Rhythmyx query resource used for cataloging holidays.
    */
   private static final String HOLIDAY_CATALOGER = "mii_autoCalendar/holidays.xml";

   /**
    * Stores <code>Holiday</code> objects. Never <code>null</code> after
    * construction, but may be empty.
    */
   private Set m_holidays;

   /**
    * Inner class to represent a single holiday object. 
    */
   class Holiday
   {
      /**
       * Ctor. Takes the date and name of the holiday.
       * 
       * @param date date, must not eb <code>null</code>.
       * @param name name of the holiday, must not be <code>null</code> or
       *           empty.
       */
      public Holiday(Date date, String name)
      {
         if (date == null)
            throw new IllegalArgumentException("date may not be null");
         if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name may not be null or empty");

         this.m_date = date;
         this.m_name = name;
      }

      /**
       * Compares the date of this holiday against the supplied date, ignoring
       * any time setting (hour/minute/second/millisecond).
       * 
       * @param d the date to be tested, not <code>null</code>.
       * @return <code>true</code> if the two dates have the same year, month,
       *         and date, <code>false</code> otherwise.
       */
      public boolean isSameDate(Date d)
      {
         if (d == null)
            throw new IllegalArgumentException("date may not be null");

         Calendar holiday = Calendar.getInstance();
         holiday.setTime(m_date);
         Calendar supplied = Calendar.getInstance();
         supplied.setTime(d);

         return holiday.get(Calendar.YEAR) == supplied.get(Calendar.YEAR)
               && holiday.get(Calendar.MONTH) == supplied.get(Calendar.MONTH)
               && holiday.get(Calendar.DATE) == supplied.get(Calendar.DATE);
      }

      /**
       * Gets the name of this holiday.
       * 
       * @return the name of this holiday, never <code>null</code> or empty.
       */
      public String getName()
      {
         return m_name;
      }

      /**
       * The date when this holiday occurs. Never <code>null</code> after
       * construction.
       */
      private Date m_date;

      /**
       * The name of this holiday. Never <code>null</code> or empty after
       * construction.
       */
      private String m_name;
   }
}
