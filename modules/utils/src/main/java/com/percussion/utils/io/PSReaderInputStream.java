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
