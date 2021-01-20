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

package com.percussion.deployer.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps an input stream and at any time can return the number of bytes read 
 * from the stream.
 */
public class PSInputStreamCounter extends FilterInputStream 
   implements IPSStreamCounter
{

   /**
    * Construct this class from an InputStream
    * 
    * @param in The input stream on which to track bytes read, may not be
    * <code>null</code>.
    */
   public PSInputStreamCounter(InputStream in)
   {
      super(in);
   }
   
   
   // see super class
   public int read() throws IOException 
   {
      int result = super.read();
      m_count++; 
      
      return result;
   }

   // see super class
   public int read(byte b[]) throws IOException
   {
      return read(b, 0, b.length);
   }
   
   // see super class
   public int read(byte b[], int off, int len) throws IOException
   {
      int count = super.read(b, off, len);
      m_count += count; 
      
      return count;
   }
   
   
   // see IPSStreamCounter
   public int getByteCount()
   {
      return m_count;
   }

   // see IPSStreamCounter
   public void closeStream()
   {
      try
      {
         super.close();
      }
      catch (IOException e)
      {
         // we expect this, someone else has the stream and will handle any
         // exceptions they encounter.
      }
   
   }

   /**
    * Count of bytes read so far, incremented each time a <code>read()</code>
    * method is called, initially zero.
    */
   private int m_count = 0;

}
