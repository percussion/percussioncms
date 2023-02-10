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
