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
package com.percussion.xml.serialization.junit;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Book)) return false;
      Book book = (Book) o;
      return Objects.equals(getTitle(), book.getTitle()) && Objects.equals(getPubDate(), book.getPubDate());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getTitle(), getPubDate());
   }
}
