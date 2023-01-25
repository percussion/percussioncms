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
package com.percussion.services.utils.xml;

import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSDateFormatISO8601;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.betwixt.expression.Context;
import org.apache.commons.betwixt.strategy.DefaultObjectStringConverter;

import java.text.ParseException;

/**
 * An extended converter to help treat more data types as primitives
 * 
 * @author dougrand
 */
public class PSBetwixtObjectConverter extends DefaultObjectStringConverter
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Default date pattern saves all information for the date in standard
    * format.
    */
   static public final PSDateFormatISO8601 ms_iso8601date = new PSDateFormatISO8601();

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.commons.betwixt.strategy.DefaultObjectStringConverter#objectToString(java.lang.Object,
    *      java.lang.Class, java.lang.String,
    *      org.apache.commons.betwixt.expression.Context)
    */
   @Override
   public String objectToString(Object object, Class type, String flavour,
         Context context)
   {
      if (object instanceof java.util.Date && isUtilDate(type))
      {
         return ms_iso8601date.format((java.util.Date) object);
      }
      else if (object instanceof Enum)
      {
         Enum value = (Enum) object;
         return value.name();
      }
      else if (object instanceof IPSGuid)
      {
         IPSGuid guid = (IPSGuid) object;
         return guid.toString();
      }
      else
      {
         return super.objectToString(object, type, flavour, context);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.commons.betwixt.strategy.DefaultObjectStringConverter#stringToObject(java.lang.String,
    *      java.lang.Class, java.lang.String,
    *      org.apache.commons.betwixt.expression.Context)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object stringToObject(String value, Class type, String flavour,
         Context context)
   {
      if (value.trim().length() == 0)
      {
         return null;
      }
      else if (isUtilDate(type))
      {
         try
         {
            return ms_iso8601date.parseObject(value);
         }
         catch (ParseException ex)
         {
            handleException(ex);
            return value;
         }
      }
      else if (Enum.class.isAssignableFrom(type))
      {
         return Enum.valueOf(type, value);
      }
      else if (IPSGuid.class.isAssignableFrom(type))
      {
         return new PSGuid(value);
      }
      else
      {
         return super.stringToObject(value, type, flavour, context);
      }
   }

   /**
    * Is the given type a java.util.Date but not a java.sql.Date?
    * 
    * @param type test this class type
    * @return true is this is a until date but not a sql one
    */
   private boolean isUtilDate(Class type)
   {
      return (java.util.Date.class.isAssignableFrom(type)
            && !java.sql.Date.class.isAssignableFrom(type)
            && !java.sql.Time.class.isAssignableFrom(type) && !java.sql.Timestamp.class
            .isAssignableFrom(type));
   }

}
