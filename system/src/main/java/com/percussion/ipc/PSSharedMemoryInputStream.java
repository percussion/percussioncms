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
