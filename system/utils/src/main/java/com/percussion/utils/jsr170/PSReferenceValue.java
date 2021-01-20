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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.utils.jsr170;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * Value that holds a reference
 * 
 * @author dougrand
 */
public class PSReferenceValue extends PSBaseValue<Node>
{   
   /**
    * Constructor
    * @param arg0 the node value, may be <code>null</code>
    */
   public PSReferenceValue(Node arg0) {
      m_value = arg0;
   }

   public String getString() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      if (m_value != null)
         return m_value.getUUID();
      else
         return null;
   }

   public InputStream getStream() throws IllegalStateException,
         RepositoryException
   {
      String stringrep = getString();
      if (stringrep != null)
         try
         {
            return new ByteArrayInputStream(stringrep.getBytes("UTF8"));
         }
         catch (UnsupportedEncodingException e)
         {
            throw new ValueFormatException("Problem encoding", e);
         }
      else
         return null;
   }

   public long getLong() throws ValueFormatException, IllegalStateException,
         RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to long");
   }

   public double getDouble() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to double");
   }

   public Calendar getDate() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to date");
   }

   public boolean getBoolean() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to boolean");
   }

   public int getType()
   {
      return PropertyType.REFERENCE;
   }

   @Override
   public long getSizeInBytes()
   {
      return ((IPSJcrCacheItem) m_value).getSizeInBytes() + 4;
   }

}
