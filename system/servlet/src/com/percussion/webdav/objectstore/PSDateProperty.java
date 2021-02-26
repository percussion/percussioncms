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
package com.percussion.webdav.objectstore;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A utility class to handle the date format for the properties such as
 * <code>creationdate</code> and <code>getlastmodifieddate</code>. The
 * input of the date data is from the search result of Rhythmyx Server,
 * the format of that is always yyyy-MM-dd HH:mm:ss.SSS. However, the
 * date format for <code>creationdate</code> is yyyy-MM-ddTHH:mm:ssZ (where
 * 'T' and 'Z' are letters, see RFC2518 23.2 Appendix 2); the date format
 * for <code>getlastmodifieddate</code> is EEE, dd MMM yyyy HH:mm:ss GMT (
 * see RFC2068 3.3.1 Full Date).
 */
public class PSDateProperty
{

   /**
    * Constructs an instance from a string date.
    *
    * @param rxDate   The date from the search result of Rhythmyx Server.
    *    Assume its format is yyyy-MM-dd HH:mm:ss.SSS. It may not be
    *    <code>null</code> or empty.
    */
   public PSDateProperty(String rxDate)
   {
      if (rxDate == null || rxDate.trim().length() ==0)
         throw new IllegalArgumentException("rxDate may not be null or empty.");
      
      m_date = ms_rxDatetimeFormat.parse(rxDate,  new ParsePosition(0));
      
      // If failed to parse both date and time, then try just the date; 
      // otherwise, throw IllegalArgumentException
      if (m_date == null)
      {
         m_date = ms_rxDateFormat.parse(rxDate,  new ParsePosition(0));
         if (m_date == null)
         {
            throw new IllegalArgumentException(
                  "Failed to parse date string '"
                        + rxDate
                        + "'. It is expected to be in the format of 'yyyy-MM-dd HH:mm:ss.SSS'");
         }
      }
   }

   /**
    * Constructs an instance from a date.
    *
    * @param date   The date, may not be <code>null</code>.
    */
   public PSDateProperty(Date date)
   {
      if (date == null)
         throw new IllegalArgumentException("date may not be null");
        
      m_date = date;
   }


   /**
    * Get the date in http date format, which is in the format of
    * EEE, dd MMM yyyy HH:mm:ss GMT (see RFC2068 3.3.1 Full Date).
    * This is typically used for the value of <code>getlastmodifieddate</code>
    * property.
    */
   public String getHttpDate()
   {
      return ms_httpDateformat.format(m_date);
   }

   /**
    * Get the date in the format of yyyy-MM-ddTHH:mm:ssZ (where 'T' and 'Z'
    * are letters, see RFC2518 23.2 Appendix 2). This is typically used for
    * the value of <code>creationdate</code> property.
    */
   public String getWebdavDate()
   {
      return ms_wdDateformat.format(m_date);
   }

   /**
    * Unit test
    */
   public static void main(String[] args)
   {
      String date1 = "2003-10-01 02:03:10.5";
      String date2 = "2003-11-12 22:13:12.10";
      
      PSDateProperty dprop1 = new PSDateProperty(date1);
      PSDateProperty dprop2 = new PSDateProperty(date2);
      
      System.out.println("date1: " + date1);
      System.out.println("date1 http format: " + dprop1.getHttpDate());
      System.out.println("date1 webdav format: " + dprop1.getWebdavDate());

      System.out.println("\ndate2: " + date2);
      System.out.println("date2 http format: " + dprop2.getHttpDate());
      System.out.println("date2 webdav format: " + dprop2.getWebdavDate());      
   }

   /**
    * The date that is initialized by ctor, never <code>null</code> after that.
    */
   private Date m_date;

   /**
    * The date-time format of the search result of Rhythmyx server.
    */
   private final static String RX_SEARCH_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

   /**
    * The date format of the search result of Rhythmyx server.
    */
   private final static String RX_SEARCH_DATE_FORMAT = "yyyy-MM-dd";

   /**
    * The HTTP date format.
    */   
   private final static String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

  /**
   * The WebDAV date format for <code>creationdate</code> property
   */
   private final static String WEBDAV_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'-00:00'";
   
    /**
    * The date format object for webdav
    */
   private final static FastDateFormat ms_wdDateformat =
            FastDateFormat.getInstance(WEBDAV_DATE_FORMAT,TimeZone.getTimeZone("GMT"));
   
   /**
    * The date format object for the search result of Rhythmyx server.
    */
   private final static FastDateFormat ms_rxDatetimeFormat =
           FastDateFormat.getInstance(RX_SEARCH_DATETIME_FORMAT);
      
   /**
    * The date format object for the search result of Rhythmyx server.
    */
   private final static FastDateFormat ms_rxDateFormat =
           FastDateFormat.getInstance(RX_SEARCH_DATE_FORMAT);
      
   /**
    * The date format object for HTTP date. The date formate has to be in 
    * English locale, otherwise it may not recognized by non-English WebDAV
    * client, such as the "Web Folder" in French version of Windows XP.
    */
   private final static FastDateFormat ms_httpDateformat =
           FastDateFormat.getInstance(HTTP_DATE_FORMAT, TimeZone.getTimeZone("GMT"), new Locale("en"));

}



