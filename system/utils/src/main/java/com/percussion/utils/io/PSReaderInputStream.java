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
package com.percussion.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * This class is an adapter that converts a java reader to an input stream. It
 * does this by converting character by character from the reader to the 
 * equivalent UTF-8 bytes.
 * 
 * @author dougrand
 *
 */
public class PSReaderInputStream extends InputStream
{
   /**
    * Count of characters to be buffered. 
    */
   private static final int BUFFER_SIZE = 4096;
   
   /**
    * The reader wrapped by this input stream.
    */
   private Reader m_reader;
   
   /**
    * Byte array to buffer multi-byte characters from the reader.
    */
   private byte[] m_buffer = null;
   
   /**
    * Input buffer for characters from the reader.
    */
   private char[] m_input = new char[BUFFER_SIZE];
   
   /**
    * Position to return byte from in the output buffer.
    */
   private int m_pos = 0;
   
   /**
    * The count of characters in the input buffer.
    */
   private int m_count = 0;
   
   /**
    * Ctor
    * @param reader the reader, never <code>null</code>.
    */
   public PSReaderInputStream(Reader reader)
   {
      if (reader == null)
      {
         throw new IllegalArgumentException("reader may not be null");
      }
      m_reader = reader;
   }
   
   @Override
   public int read() throws IOException
   {
      if (m_pos >= m_count && m_count >= 0)
      {
         int count = m_reader.read(m_input);
         if (count > 0)
         {
            String input = new String(m_input, 0, count);
            m_buffer = input.getBytes(StandardCharsets.UTF_8);
            m_count = m_buffer.length;
            m_pos = 0;
         }
         else
         {
            m_count = -1;
         }
      }
      
      if (m_count < 0)
         return -1;
      else
         return m_buffer[m_pos++];         
   }

}
