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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps an output stream and at any time can return the number of bytes written 
 * to the stream.
 */
public class PSOutputStreamCounter extends FilterOutputStream 
   implements IPSStreamCounter
{

   /**
    * Construct this class from an <code>OutputStream</code>
    * 
    * @param out The stream, may not be <code>null</code>.
    */
   public PSOutputStreamCounter(OutputStream out)
   {
      super(out);
   }

   // see super class
   public void write(int b) throws IOException
   {
      super.write(b);
      m_count++;
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
    * Count of bytes written so far, incremented each time a <code>write()</code>
    * method is called, initially zero.
    */
   private int m_count = 0;

}
