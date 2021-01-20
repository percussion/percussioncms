/******************************************************************************
*
* [ PSExpandRecurringEvents ]
* 
* COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
******************************************************************************/
package com.percussion.fastforward.calendar;

import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Java post-exit used to expand recurring events into a format consumable by
 * auto indexes and Mitre's Rhythmyx calendaring system.
 * 
 * @author James Schultz
 */
public class PSExpandRecurringEvents extends PSDefaultExtension 
      implements
         IPSResultDocumentProcessor
{
   /**
    * Always <code>false</code>, as the stylesheet is never modified.
    * 
    * @return always <code>false</code>
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Parses the required parameters. Expects the parameter object (array) to
    * contain two memebrs. The first one is treated as the calendar start date
    * string and the second one as the calendar end date string. Both will be
    * parsed as Date objects and return as a new object array.
    * 
    * @param params parameter array as explained above, must not be
    *           <code>null</code> or empty.
    * @return @throws PSParameterMismatchException
    * @throws PSParameterMismatchException
    * @throws PSExtensionProcessingException
    */
   private static Object[] parseParameters(Object[] params)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if(params==null || params.length < 2)
      {
         throw new PSExtensionProcessingException(0,
               "params must not be null and must have at least two entries."); 
      }
      /*
       * look for two input parameters: calendarStart and calendarEnd. these
       * parameters determine the bounds for including recurring event instances
       */
      String calendarStartString = PSUtils.getParameter(params, 0);
      String calendarEndString = PSUtils.getParameter(params, 1);

      // it is an error if either parameter is missing
      if (calendarStartString == null)
         throw new PSParameterMismatchException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
               new Object[]
               {"calendarStart", "is a required parameter"});
      if (calendarEndString == null)
         throw new PSParameterMismatchException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
               new Object[]
               {"calendarEnd", "is a required parameter"});

      // try to parse the parameters into Dates
      Date calendarStart = PSDataTypeConverter
            .parseStringToDate(calendarStartString);
      Date calendarEnd = PSDataTypeConverter
            .parseStringToDate(calendarEndString);
      if (calendarStart == null)
         throw new PSParameterMismatchException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
               new Object[]
               {"calendarStart", "could not be parsed as a Date"});
      if (calendarEnd == null)
         throw new PSParameterMismatchException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
               new Object[]
               {"calendarEnd", "could not be parsed as a Date"});

      if (calendarEnd.before(calendarStart))
         throw new PSExtensionProcessingException(
               IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, new Object[]
               {"ExpandRecurringEvents",
                     "Calendar end must be greater than than calendar start"});

      return new Object[]
      {calendarStart, calendarEnd};
   }

   /**
    * Implementation of the interface method.
    * @param params parameters for the exit, consisting of:
    *           <ol>
    *           <li><b>calendarStart </b>: The start datetime of the calendar
    *           being generated. No recurring event instances before this
    *           datetime will be returned. This parameter is required.
    *           <li><b>calendarEnd </b>: The end datetime of the calendar being
    *           generated. No recurring event instances on or after this
    *           datetime will be returned. This parameter is required.
    *           </ol>
    * @param request the current request context, never <code>null</code>.
    * @param resultDoc
    * @return @throws com.percussion.extension.PSParameterMismatchException if
    *         either required parameter is not supplied, or cannot be parsed as
    *         a Date.
    * @throws PSParameterMismatchException
    * @throws com.percussion.extension.PSExtensionProcessingException
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {

      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      if (resultDoc == null)
         throw new IllegalArgumentException("resultDoc may not be null");

      Document newResultDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element sysAssemblerInfo = newResultDoc
            .createElement("sys_AssemblerInfo");
      Element relatedContent = newResultDoc.createElement("RelatedContent");
      sysAssemblerInfo.appendChild(relatedContent);

      Object[] calendarBounds = PSExpandRecurringEvents.parseParameters(params);
      Date calendarStart = (Date) calendarBounds[0];
      Date calendarEnd = (Date) calendarBounds[1];
      request.printTraceMessage("Expanding recurring events between "
            + calendarStart + " and " + calendarEnd);

      Element root = resultDoc.getDocumentElement();
      if (root == null)
         return resultDoc;
      DateFormat dayFormat = new SimpleDateFormat("d");
      DateFormat monthFormat = new SimpleDateFormat("MM");
      DateFormat yearFormat = new SimpleDateFormat("yyyy");
      DateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd");

      // load the holidays
      PSHolidays holidays = loadHolidays(request);

      // grab all the <event> element children and convert to objects
      NodeList eventElems = root.getElementsByTagName(PSRecurringEvent.EVENT);
      for (int i = 0; i < eventElems.getLength(); i++)
      {
         Element eventElem = (Element) eventElems.item(i);
         request.printTraceMessage("Processing sys_contentid = "
               + eventElem.getAttribute("sys_contentid"));
         NodeList nlLinkurl = eventElem.getElementsByTagName("linkurl");
         if (nlLinkurl.getLength() > 0)
         {
            Element linkUrl = (Element) nlLinkurl.item(0);
            try
            {
               PSRecurringEvent event = new PSRecurringEvent(eventElem);

               // extract all iterations of the RecurringEvent and add to XML
               Calendar c;
               Iterator eventIterator = event.getRecurrenceIterator();

               while ((c = (Calendar) eventIterator.next()) != null)
               {
                  Date d = c.getTime();

                  // make sure the date is in bounds for the calendar
                  if (d.compareTo(calendarEnd) >= 0)
                  {
                     /*
                      * once the calendar end date is reached, there is no
                      * reason to resolve further instances, so exit the while
                      * loop
                      */
                     break;
                  }
                  if (d.compareTo(calendarStart) >= 0 && !holidays.isHoliday(d))
                  {
                     /*
                      * only add the instance if it occurs within the calendar
                      * being created and isn't on a holiday
                      */
                     Element linkUrlClone = (Element) linkUrl.cloneNode(true);
                     // add the attributes used by the MakeCalendar exit
                     linkUrlClone.setAttribute(DAY_ATTR, dayFormat.format(d));
                     linkUrlClone.setAttribute(MONTH_ATTR, monthFormat
                           .format(d));
                     linkUrlClone.setAttribute(YEAR_ATTR, yearFormat.format(d));
                     // add the recurrence date to the URL so it can be used
                     // by snippet
                     NodeList nlValue = linkUrlClone
                           .getElementsByTagName("Value");
                     if (nlValue.getLength() > 0)
                     {
                        Element value = (Element) nlValue.item(0);
                        String currentUrl = value.getAttribute("current");
                        if (currentUrl != null)
                        {
                           value.setAttribute("current", currentUrl
                                 + "&recurrenceDate=" + fullFormat.format(d));
                        }
                     }
                     relatedContent.appendChild(newResultDoc.importNode(
                           linkUrlClone, true));
                  }
               }
            }
            catch (PSRecurringEvent.IllegalValueException e)
            {
               /*
                * we shouldn't have problems with our XML, but if we do, log the
                * error and skip to the next event
                */
               request.printTraceMessage("error with recurring event values: "
                     + e);
            }
            catch (PSRecurringEvent.UnknownNodeTypeException e)
            {
               /*
                * we shouldn't have problems with our XML, but if we do, print
                * the error and skip to the next event
                */
               e.printStackTrace();
               request.printTraceMessage("error parsing recurring event: " + e);
            }
         }
      }
      return newResultDoc;
   }

   /**
    * Loads the collection of calendar holidays. Recurring events should never
    * occur on a holiday.
    * <p>
    * This method is protected so it can be overridden by the test framework, to
    * allow testing the rest of the class without a running Rhythmyx server.
    * 
    * @param request
    * @return a newly constructed <code>Holidays</code> object, never
    *         <code>null</code>
    */
   protected PSHolidays loadHolidays(IPSRequestContext request)
   {
      return new PSHolidays(request);
   }

   /**
    * String constant for the attribute "year".
    */
   private static final String YEAR_ATTR = "year";

   /**
    * String constant for the attribute "month".
    */
   private static final String MONTH_ATTR = "month";

   /**
    * String constant for the attribute "day".
    */
   private static final String DAY_ATTR = "day";
}