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
package com.percussion.services.utils.xml;

import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSDateFormatISO8601;
import com.percussion.utils.guid.IPSGuid;

import java.text.ParseException;

import org.apache.commons.betwixt.expression.Context;
import org.apache.commons.betwixt.strategy.DefaultObjectStringConverter;

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
