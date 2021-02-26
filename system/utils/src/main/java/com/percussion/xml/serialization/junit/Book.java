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
package com.percussion.xml.serialization.junit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;

/**
 * A sample Book class used in unit test of the
 * {@link com.percussion.xml.serialization.PSObjectSerializer} class. As can be
 * seen it is a simple java bean with a default ctor (required) and setXxx() and
 * getXxx() methods.
 */
public class Book
{
   String title;

   Date pubDate;

   /**
    * Default ctor. Required by serializer.
    */
   public Book()
   {

   }

   /**
    * @throws ParseException
    */
   public Book(String title, String pubDate) throws ParseException
   {
      this.title = title;
      FastDateFormat df = FastDateFormat.getInstance("MMddyyyy");
      this.pubDate = df.parse(pubDate);
   }

   public Date getPubDate()
   {
      return pubDate;
   }

   public void setPubDate(Date pubDate)
   {
      this.pubDate = pubDate;
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

}
