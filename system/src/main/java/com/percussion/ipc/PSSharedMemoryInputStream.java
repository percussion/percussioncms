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

package com.percussion.ipc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates a stream that allows the user to read data from a block of shared
 * memory. Marking and resetting are supported.
**/
public class PSSharedMemoryInputStream extends InputStream 
{
   /**
    * Creates a stream to read data from a shared memory block.
   **/
   public PSSharedMemoryInputStream( PSSharedMemory shMem )
   {
      m_shMem = shMem;
      m_sharedMemView = mapSharedMemoryView( shMem.getMemoryId());
      m_bOpen = true;
   }
   
   public void close() throws IOException 
   {
      if ( 0 != m_sharedMemView )
      {
         unmapSharedMemoryView( m_sharedMemView );
         m_sharedMemView = 0;
      }
      m_bOpen = false;
//      try
      {
         m_shMem.dispose();   
      }
//      catch ( PSIpcOSException e )
      {
         // nothing we can do, so ignore it (log it?)
      }
      
   }
   
   public int read() 
      throws IOException 
   {
      if ( !m_bOpen )
         throw new IOException();

      if ( 0 == available())
         throw new EOFException();

      byte [] buf = readBytes( m_sharedMemView, m_offset++, 1 );

      return (buf[0] & 0xff);
   }   
   
   public int available()
         throws IOException 
   {
      return ( m_shMem.getSize() - m_offset );
      
   }
   
   
   private native int mapSharedMemoryView( int shMemHandle );
   private native void unmapSharedMemoryView( int shMemView );
   private native byte [] readBytes( int shMemView, int offset, int bytes );
   
   private boolean m_bOpen = false;
   private int m_offset = 0;
   private PSSharedMemory m_shMem;
   private int m_sharedMemView;
}
