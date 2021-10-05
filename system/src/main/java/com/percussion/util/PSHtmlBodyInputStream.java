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

package com.percussion.util;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Filters out BODY portion from a given HTML input stream. Letter case
 * of the <code>BODY</code> tag is NOT important.
 * For more information see base class: {@link FilterInputStream}.
 */
public class PSHtmlBodyInputStream extends FilterInputStream
{
   /**
    * Initilizes the base class by passing it a given input stream wrapped
    * into the new instance of the {@link java.io.BufferedInputStream}.
    * @param in, may be <code>null</code>.
    */
   public PSHtmlBodyInputStream(InputStream in)
   {
     super(new BufferedInputStream(in));
   }


   /**
    * Reads <code>size</code> bytes at a time and puts them into a given
    * <code>buf</code> begining from a given <code>offset</code>; ensures that
    * buffer overflow never happens; also see {@link FilterInputStream}.
    * @param buf dest buffer, never <code>null</code>.
    * @param offset dest buffer offset, must be in the buffer range.
    * @param size number of bytes to read
    *
    * @return number of bytes read.
    * @throws IOException see base class.
    */
   public int read(byte[] buf, int offset, int size)
     throws IOException
   {
      if (buf==null)
         throw new IllegalArgumentException("buf may not be null");
      if (offset < 0 || offset >= buf.length)
         throw new IllegalArgumentException("buf offset out of range");

      int bytesRead = 0;

      for(int i = 0; i < size; i++)
      {
         int bufInd = offset + i;

         if (bufInd >= buf.length)
            break; //make sure not to overflow the buffer

         //get next byte
         byte next = (byte)this.read();

         //are we at the end?
         if(next == EOF)
         {
            if (bytesRead == 0)
            {
               bytesRead = EOF;
            }
            break;
         }

         buf[bufInd] = next;

         bytesRead++;
      }

      return bytesRead;
   }

   /**
    * Reads one byte at a time, <body>only bytes that are between 'body' tags
    * </body> are actually read.
    * @return byte read.
    * @throws IOException if any network error happens during the read.
    */
   public int read() throws IOException
   {
      int nextByte;

      switch(m_state)
      {
      case STATE_INITIAL:
        nextByte = readFirst();
        if(nextByte == EOF)
           m_state = STATE_EOF; // we got an EOF without finding the start of body
        else
           m_state = STATE_ECHO; // we are echoing all subsequent characters
        break;

      case STATE_ECHO:
        nextByte = super.read();

        if(nextByte == EOF)
           return nextByte;

        if(nextByte == LT && checkMarkerIgnoreCase(END_MARKER))
        {
           // we found the end of the body
           m_state = STATE_EOF;
           return EOF;
        }
        break;

      case STATE_EOF:
        nextByte = EOF;
        break;

      default: //should never happen: keeps the compiler happy.
        m_state = STATE_EOF;
        nextByte = EOF;
      }

      return nextByte;
   }

   /**
    * Looks for a given marker starting from a current stream position;
    * comparison is case-insensitive; when done, resets the stream back
    * to the original position.
    * @param marker marker to watch for, never <code>null</code>.
    * @return <code>true</code> is a given marker is found, <code>false</code>
    * otherwise.
    * @throws IOException if any network error happens during the read.
    */
   private boolean checkMarkerIgnoreCase(String marker) throws IOException
   {
      if (marker==null)
         throw new IllegalArgumentException("marker may not be null");

      boolean retval = false;
      int markerLen = marker.length();

      this.mark(markerLen+10);

      byte ibuf[] = new byte[markerLen];

      int readLen = super.read(ibuf, 0, markerLen);

      if(readLen == markerLen)
      { // we got the whole marker (no EOF), compare it.
         String nextPart = new String(ibuf);
         retval = marker.equalsIgnoreCase(nextPart);
      }

      this.reset();

      return retval;
   }

   /**
    * Reads input stream until an opening <code>body</code> tag is found.
    * @return first byte after the opening <code>body</code> tag, or
    * <code>EOF</code> if there is no data in the body.
    * @throws IOException
    */
   private int readFirst()
     throws IOException
   {
     int next = 0;

     //look for the opening 'body' tag
     for(;;)
     {
        next = super.read();

        if(next == EOF)
           return EOF;

        if(next == LT && checkMarkerIgnoreCase(START_MARKER))
           break;
      }

      //look for the GT of the opening 'body' tag
      while(next != GT)
      {
         next = super.read();

         if(next == EOF) return EOF;
      }

      //return the first byte after an opening 'body' element
      return super.read();
   }


   /**
    * Keeps track of the current stream state.
    */
   private int m_state = STATE_INITIAL;

   /**
    * Initial state.
    */
   private static final int STATE_INITIAL = 0;

   /**
    * Echoing state.
    */
   private static final int STATE_ECHO = 1;

   /**
    * State EOF.
    */
   private static final int STATE_EOF = 2;

   /**
    * 'body' constant.
    */
   private static final String START_MARKER = "body";

   /**
    * '/body' constant.
    */
   private static final String END_MARKER = "/body";

   /**
    * The '&lt;' char as bytes.
    */
   private static final byte LT = String.valueOf('<').getBytes()[0];

   /**
    * The '&gt;' char as bytes.
    */
   private static final byte GT = String.valueOf('>').getBytes()[0];

   /**
    * Value returned by the read methods to indicate that an EOF
    * has been encountered.
    */
   private static final byte EOF = -1;
}
