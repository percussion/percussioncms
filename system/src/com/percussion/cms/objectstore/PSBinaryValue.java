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
package com.percussion.cms.objectstore;

import com.percussion.util.IOTools;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSPurgableTempFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class holds the value as binary.  Once the value is set via constructor
 * or setData, the arguments are immediately converted to a <code>byte[]</code>.
 */
public class PSBinaryValue extends PSFieldValue
{
   /**
    * No-arg constructor used by derived classes.
    */
   protected PSBinaryValue()
   {      
   }
   
   /**
    * Creates an instance with the binary <code>content</code> as its value.
    *
    * @param content - the bytes to be the value.  This class takes ownership
    * of the array.  If <code>null</code>, a new empty array will be created.
    */
   public PSBinaryValue(byte[] content)
   {
      setData(content);
   }

   /**
    * Creates an instance with the <code>InputStream</code> <code>content</code>
    * as its value.
    *
    * @param content - the InputStream to be used as the value.  Must not be
    * <code>null</code>.  This method assumes ownership of the stream and is
    * responsible for closing it.
    * @throws IOException if there is a problem with the stream.
    */
   public PSBinaryValue(InputStream content) throws IOException
   {
      setData(content);
   }

   /**
    * Creates an instance that will use the supplied binary locator to retrieve
    * the data only if required.
    * 
    * @param locator The locator to use, may not be <code>null</code>.
    */
   public PSBinaryValue(IPSBinaryLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      m_locator = locator;
   }

   /**
    * Clones this object.  Makes a deep copy.
    *
    * @return deep copy of this object.
    */
   public Object clone()
   {
      PSBinaryValue copy = null;
      copy = (PSBinaryValue)super.clone();

      if(m_data != null)
      {
         copy.m_data = new byte[m_data.length];
         System.arraycopy(m_data, 0, copy.m_data, 0, m_data.length);
      }
      
      if (m_locator != null)
         copy.m_locator = (IPSBinaryLocator)m_locator.clone();

      return copy;
   }

   /** 
    * See {@link IPSFieldValue#equals(Object)} for details.  Two binary values 
    * with the same locator will not be considered equal if one has had its
    * data loaded and the other has not.
    */
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if(obj == null || !(getClass().isInstance(obj)))
         isEqual = false;
      else
      {
         PSBinaryValue comp = (PSBinaryValue) obj;      
         if (!compare(m_locator, comp.m_locator))
            isEqual = false;
         else if (m_isDataLoaded != comp.m_isDataLoaded)
            isEqual = false;
         else if (!compare(m_data, comp.m_data))
            isEqual = false;
      }
         
      return isEqual;
   }

   /** @see IPSFieldValue */
   public int hashCode()
   {
      int hash = 0;

      // super is abtract, don't call      
      hash += m_isDataLoaded ? 1 : 0;
      hash += hashBuilder(m_data);
      hash += hashBuilder(m_locator);

      return hash;
   }

   /**
    * Sets the data for this value using the provided <code>byte[]</code>.  This
    * class will take ownership of the array.
    *
    * @param content - The data to set, may be <code>null</code> to clear the
    * value and create a new empty array.
    */
   public void setData(byte[] content)
   {
      if(content == null)
         m_data = new byte[0];

      m_data = content;
      m_isDataLoaded = true;
   }

   /**
    * Sets the data for this value using the provided <code>InputStream</code>.
    * This will close the stream, after reading its bytes into the data array.
    *
    * @param content - the InputStream to be used as the value.  Must not be
    * <code>null</code>.  This method assumes ownership of the stream and is
    * responsible for closing it.
    */
   public void setData(InputStream content) throws IOException
   {
      if(content == null)
         throw new IllegalArgumentException("content must not be null");

      try
      {
         ByteArrayOutputStream out = new ByteArrayOutputStream(
            content.available());
         IOTools.copyStream(content, out);
         
         byte[] data = out.toByteArray();
         setData(data);
      }
      finally
      {
         content.close();
      }
   }

   /**
    * This base 64 encodes the value and returns the value as a
    * <code>String</code>.  If the value is empty an empty <code>String</code>
    *  will be returned.
    */
   public String getBase64Encode()
   {
      byte[] data = getData();
      
      if(data == null || data.length  == 0)
         return "";

      // going to write it to the field value as this for getString.
      InputStream inStream = null;
      ByteArrayOutputStream outStream = null;
      String encString = null;
      try
      {
         inStream = new ByteArrayInputStream(data);
         outStream = new ByteArrayOutputStream();

         // encode it
         PSBase64Encoder.encode(inStream, outStream);

         // get every last byte:
         outStream.flush();
         encString = outStream.toString();

         return encString;
      }
      catch(IOException e)
      {
         throw new RuntimeException(e.getLocalizedMessage());
      }
      finally
      {
         try
         {
            if(inStream != null)
               inStream.close();

            if(outStream != null)
               outStream.close();
         }
         catch(IOException e)
         {
            throw new RuntimeException(e.getLocalizedMessage());
         }
      }
   }

   /**
    * Gets the length in bytes of the data provided by this value.
    *
    * @return The length, <code>0</code> if there is no data set.
    */
   public int getLength()
   {
      byte[] data = getData();
      return data == null ? 0 : data.length;
   }

   /**
    * Returns the value as a byte[] of the value.  This is the actual
    * storage array and modifications to the returned array affect this
    * instance.
    *
    * @return byte[] - may be empty, never <code>null</code>.
    */
   public Object getValue()
   {
      return getData();
   }

   /**
    * Get the temporary file holding the binary value.
    * 
    * @return the temporary file holding the binary value, may be 
    *    <code>null</code> if no value is assigned yet.
    */
   public PSPurgableTempFile getValueFile()
   {
      if (m_locator != null)
      {
         try
         {
            return m_locator.getDataFile();
         }
         catch (Exception e)
         {
            throw new RuntimeException(
               "Failed to load binary data from the server: " + 
               e.getLocalizedMessage());
         }
      }
      
      return null;
   }
   
   /**
    * Convenience method for use with <code>toXml</code>'s.  Calls <code>
    * getBase64Encode()</code>.
    *
    * @return never <code>null</code>, may be empty.
    */
   public String getValueAsString()
   {
      return getBase64Encode();
   }
   
   /**
    * Determines if this value has data loaded. Since data may be loaded upon
    * request, it may never have been loaded.  If data is supplied to the ctor,
    * then it is considered to be loaded.
    * 
    * @return <code>true</code> if data is loaded, <code>false</code> otherwise.
    */
   public boolean isDataLoaded()
   {
      return m_isDataLoaded;
   }
   
   /**
    * Gets the data represented by this value.  If not already loaded, then the
    * binary locator is used to retrieve the data at this time.
    * 
    * @return The data, may be <code>null</code> if the data is 
    * <code>null</code>.
    */
   protected byte[] getData()
   {
      if (!m_isDataLoaded)
      {
         try
         {
            byte[] data = (byte[])m_locator.getData();
            setData(data);
         }
         catch (Exception e)
         {
            throw new RuntimeException(
               "Failed to load binary data from the server: " + 
               e.getLocalizedMessage());
         }         
      }      
      
      return m_data;
   }

   /**
    * The content of this value, modified by <code>setData()</code> and possibly
    * <code>getData()</code>, may be empty or <code>null</code>.
    */
   private byte[] m_data;
   
   /**
    * The locator to use to retrieve this value's data, initialized during 
    * construction then never changed, may be <code>null</code>.
    */
   private IPSBinaryLocator m_locator;
   
   /**
    * Determines if data has been supplied.  If a binary locator is supplied
    * at construction, this value is <code>false</code> until a call is made
    * to the <code>setData</code> or {@link #getData()} methods.
    */
   private boolean m_isDataLoaded = false;
}