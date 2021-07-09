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
package com.percussion.utils.jsr170;

import com.percussion.utils.io.PSReaderInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

/**
 * Factory to create value objects
 * 
 * @author dougrand
 */
public class PSValueFactory implements ValueFactory
{
   /**
    * Used in the static method
    */
   private static final PSValueFactory ms_fact = new PSValueFactory();

   /**
    * Takes the input object and determines which concrete subclass should be
    * used to represent the data. If no subclass is appropriate then this throws
    * an exception.
    * 
    * @param data the data to encapsulate, never <code>null</code>
    * @return an appropriate value object that implements the value interface
    * @throws ValueFormatException if the object doesn't match a known type and
    *            cannot be coerced to a type
    */
   public static Value createValue(Object data) throws ValueFormatException
   {
      if (data == null)
      {
         return null;
      }
      if (data instanceof Value)
      {
         return (Value) data;
      }
      if (data instanceof Number)
      {
         if (data instanceof Double || data instanceof Float)
         {
            return ms_fact.createValue(((Number) data).doubleValue());
         }
         else
         {
            return ms_fact.createValue(((Number) data).longValue());
         }
      }
      else if (data instanceof String)
      {
         return new PSStringValue((String) data);
      }
      else if (data instanceof Blob)
      {
         try(InputStream io = ((Blob) data).getBinaryStream()) {

            return new PSInputStreamValue(io);
         }
         catch (SQLException | IOException e)
         {
            throw new ValueFormatException("Couldn't extract data", e);
         }
      }
      else if (data instanceof Clob)
      {
            try (InputStream io = new PSReaderInputStream(((Clob) data).getCharacterStream())) {
               return new PSInputStreamValue(io);
            } catch (SQLException | IOException e) {
               throw new ValueFormatException("Couldn't extract data", e);
            }

      }      
      else if (data instanceof Date)
      {
         Calendar cal = new GregorianCalendar();
         cal.setTime((Date) data);
         return ms_fact.createValue(cal);
      }
      else if (data instanceof Calendar)
      {
         return ms_fact.createValue((Calendar) data);
      }
      else if (data instanceof byte[])
      {
         try(InputStream stream = new ByteArrayInputStream((byte[]) data)) {
            return ms_fact.createValue(stream);
         } catch (IOException e) {
            throw new ValueFormatException("Couldn't extract data", e);
         }
      }
      else if (data instanceof Clob)
      {

         try(InputStream stream =
            new PSReaderInputStream(((Clob) data).getCharacterStream())) {
            return ms_fact.createValue(stream);
         }

         catch (SQLException | IOException e)
         {
            throw new ValueFormatException("Problem creating stream value", e);
         }
      }
      else if (data instanceof Blob)
      {

         try( InputStream stream = ((Blob) data).getBinaryStream()){
            return ms_fact.createValue(stream);
         }
         catch (SQLException | IOException e)
         {
            throw new ValueFormatException("Problem creating stream value", e);
         }
      }      
      else if (data instanceof Boolean)
      {
         return ms_fact.createValue(((Boolean) data).booleanValue());
      }
      else if (data instanceof Node)
      {
         try
         {
            return ms_fact.createValue((Node) data);
         }
         catch (RepositoryException e)
         {
            throw new ValueFormatException("Problem creating node value", e);
         }
      }
      throw new ValueFormatException("Cannot represent " + data.getClass()
            + " as a value");
   }

   public Value createValue(String arg0)
   {
      return new PSStringValue(arg0);
   }

   public Value createValue(String value, int type) throws ValueFormatException
   {
      switch (type)
      {
         case PropertyType.BINARY :
            return createValue(value.getBytes());
         case PropertyType.BOOLEAN :
            return new PSBooleanValue(value);
         case PropertyType.DATE :
            return new PSCalendarValue(PSValueConverter
                  .convertToCalendar(value));
         case PropertyType.DOUBLE :
            return new PSDoubleValue(value);
         case PropertyType.LONG :
            return new PSLongValue(value);
         case PropertyType.NAME :
         case PropertyType.STRING :
         case PropertyType.PATH :
            return new PSStringValue(value);
         case PropertyType.REFERENCE :
         case PropertyType.UNDEFINED :
         default :
            throw new ValueFormatException("Unimplemented type");
      }
   }

   public Value createValue(long arg0)
   {
      return new PSLongValue(arg0);
   }

   public Value createValue(double arg0)
   {
      return new PSDoubleValue(arg0);
   }

   public Value createValue(boolean arg0)
   {
      return new PSBooleanValue(arg0);
   }

   public Value createValue(Calendar arg0)
   {
      return new PSCalendarValue(arg0);
   }

   public Value createValue(InputStream arg0)
   {
      return new PSInputStreamValue(arg0);
   }

   public Value createValue(Node arg0) throws RepositoryException
   {
      return new PSReferenceValue(arg0);
   }

}
