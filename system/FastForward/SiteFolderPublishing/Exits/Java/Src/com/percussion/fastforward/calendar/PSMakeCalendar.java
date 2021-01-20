/******************************************************************************
 *
 * [ PSMakeCalendar.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.fastforward.calendar;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.server.IPSRequestContext;
//import org.cms.calendar.exits.Holidays;
//import com.percussion.consulting.mii.calendar.Holidays;
import com.percussion.util.PSDataTypeConverter;

//import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This exit takes the result document and replaces the RelatedContent portion
 * of the sys_AssemblerInfo node in the document with another document fragment
 * (based on calendar.dtd) that is used by the stylesheet to generate the
 * graphical representation of a monthly calendar.
 * 
 * @author Roy Kiesler
 * @version 1.0
 * @deprecated Use a Velocity template paired with 
 * {@link com.percussion.fastforward.calendar.PSCalendarMonthModel 
 * PSCalendarMonthModel} instead.
 */
public class PSMakeCalendar implements IPSResultDocumentProcessor
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   public void init(IPSExtensionDef extensionDef, java.io.File codeRoot)
         throws PSExtensionException
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object params[],
         IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // get the calendar start date
      if (params[0] == null)
         throw new PSParameterMismatchException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
               new Object[]
               {"date", "is a required parameter"});
      Date date = PSDataTypeConverter.parseStringToDate(params[0].toString());
      if (date == null)
         throw new PSExtensionProcessingException(0, new ParseException(
               "Could not parse date: " + params[0], 0));

      // calculate number of weeks for this month
      int weeksInMonth = getWeeksInMonth(date);

      // set up the holidays collection
      PSHolidays holidays = new PSHolidays(request);

      /*
       * create the root element for the calendar XML that will replace the
       * RelatedContent element
       */
      Element calRoot = doc.createElement("Calendar");

      // create the Today element
      Element todayElem = doc.createElement("Today");
      Calendar cal = Calendar.getInstance();
      todayElem.setAttribute("day", Integer.toString(cal
            .get(Calendar.DAY_OF_MONTH)));
      todayElem.setAttribute("month", new SimpleDateFormat("MMMM").format(cal
            .getTime()));
      todayElem.setAttribute("year", Integer.toString(cal.get(Calendar.YEAR)));
      calRoot.appendChild(todayElem);

      // create the Month element
      Element monthElem = doc.createElement("Month");
      cal.setTime(date);

      monthElem.setAttribute("name", new SimpleDateFormat("MMMM").format(cal
            .getTime()));
      monthElem.setAttribute("year", Integer.toString(cal.get(Calendar.YEAR)));
      monthElem.setAttribute("numOfWeeks", Integer.toString(weeksInMonth));

      // add Week elements to Month
      for (int i = 0; i < weeksInMonth; i++)
      {
         Element weekElem = doc.createElement("Week");
         weekElem.setAttribute("number", Integer.toString(i + 1));
         monthElem.appendChild(weekElem);
      }
      calRoot.appendChild(monthElem);

      // get a list of all <linkurl> elements (used later)
      NodeList linkUrls = doc.getElementsByTagName("linkurl");
      int numberOfLinkurls = linkUrls.getLength();
      // get a list of all <linkurl> into Array
      Element linkUrlsArray[] = new Element[numberOfLinkurls];
      for (int i = 0; i < numberOfLinkurls; i++)
      {
         linkUrlsArray[i] = (Element) linkUrls.item(i);
      }
      // append to the current document contents
      NodeList nlRoot = doc.getElementsByTagName("RelatedContent");
      Node relatedContentNode = nlRoot.item(0);
      Node assemblerInfoNode = relatedContentNode.getParentNode();
      // Line below removed by Jeff Larimer, 5/25/04
      // assemblerInfoNode.removeChild(relatedContentNode);

      assemblerInfoNode.appendChild(calRoot);

      // add Day elements to each Week element
      int daysInCalendar = weeksInMonth * 7;
      Element days[] = new Element[daysInCalendar];
      String daysOfWeek[] =
      {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday"};

      for (int i = 0; i < daysInCalendar; i++)
      {
         days[i] = doc.createElement("Day");
         days[i].setAttribute("day", Integer.toString(0));
      }

      NodeList nlWeeks = doc.getElementsByTagName("Week");
      for (int i = 0; nlWeeks != null && i < weeksInMonth; i++)
      {
         Element weekElem = (Element) nlWeeks.item(i);
         for (int j = 0; j < 7; j++)
         {
            days[(i * 7) + j].setAttribute("name", daysOfWeek[j]);
            days[(i * 7) + j]
                  .setAttribute("dayOfWeek", Integer.toString(j + 1));
            weekElem.appendChild(days[(i * 7) + j]);
         }
      }

      // find what day of the week the first of the month is on
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.getTime();

      int firstDayOfWeekInMonth = cal.get(Calendar.DAY_OF_WEEK);
      int daysInMonth = getDaysInMonth(date);

      NodeList nlDays = doc.getElementsByTagName("Day");
      int j = 0;
      for (int i = 0; nlDays != null && i < daysInCalendar; i++)
      {
         Element dayElem = (Element) nlDays.item(i);
         if (i + 1 >= firstDayOfWeekInMonth && j < daysInMonth)
         {
            dayElem.setAttribute("day", Integer.toString(++j));

            // add a <holiday> element if one exists for this <Day>
            cal.set(Calendar.DAY_OF_MONTH, j);
            String holidayName = holidays.getHoliday(cal.getTime());
            if (holidayName != null)
            {
               Element h = doc.createElement("Holiday");
               h.setNodeValue(holidayName);
               h.appendChild(doc.createTextNode(holidayName));
               dayElem.appendChild(h);
            }
         }
      }

      // add all <linkurl> nodes under their respective <Day>

      for (int i = 0; i < numberOfLinkurls; i++)
      {
         Element link = linkUrlsArray[i];
         try
         {
            int linkDay = Integer.parseInt(link.getAttribute("day"));
            try
            {
               int numberOfDays = nlDays.getLength();
               for (j = 0; j < numberOfDays; j++)
               {
                  Element dayElem = (Element) nlDays.item(j);
                  int calendarDay = Integer.parseInt(dayElem
                        .getAttribute("day"));
                  if (calendarDay == linkDay)
                     dayElem.appendChild(link);
               }
            }
            catch (NumberFormatException nfe)
            {
               throw new PSExtensionProcessingException(0, nfe);
            }
         }
         catch (NumberFormatException nfe)
         {
            // this should only happen because of the default empty <linkurl>
            // element that automated index assemblers carry
         }
      }
      return doc;
   }

   /**
    * Get days in the month of the specified date. 
    * 
    * @param date date object must not be <code>null</code>.
    * @return days in the month.
    */
   private int getDaysInMonth(Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);

      return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
   }

   /**
    * Get weeks in the month of the specified date. 
    * 
    * @param date date object must not be <code>null</code>.
    * @return weeks in the month.
    */
   private int getWeeksInMonth(Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);

      int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      cal.set(Calendar.DAY_OF_MONTH, daysInMonth);

      return cal.get(Calendar.WEEK_OF_MONTH);
   }
}
