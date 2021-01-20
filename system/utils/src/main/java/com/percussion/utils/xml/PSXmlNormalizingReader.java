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

package com.percussion.utils.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;


public class PSXmlNormalizingReader extends Reader {

    private final BufferedReader underlyingReader;
    private static final int BUFFER_SIZE=33554432;


    public PSXmlNormalizingReader(final InputStream is) throws UnsupportedEncodingException {
       CharsetDecoder decoder = Charset.forName("UTF8").newDecoder();
       decoder.onMalformedInput(CodingErrorAction.IGNORE);
       decoder.onUnmappableCharacter(CodingErrorAction.IGNORE); 
       
       underlyingReader = new BufferedReader(new InputStreamReader(is, decoder),BUFFER_SIZE);
     
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
       
       char[] charBuffer = new char[cbuf.length];
       
       int ret  = underlyingReader.read(charBuffer, off, len);
       
       if(ret == -1 || charBuffer.length == 0 ) {
          return -1;
      } 
       
      int satisfied = 0;
      int currentOffset = off;
      for (char c:charBuffer) {
         if(isValid(c)){
            satisfied++;
            cbuf[currentOffset++] = c;
         }
      }    
      return satisfied;
    }

    private boolean isValid(char c) {
        return ((c == 0x9 || c == 0xA || c == 0xD) ||
                ((c >= 0x20) && (c <= 0xD7FF)) ||
                ((c >= 0xE000) && (c <= 0xFFFD)) ||
                ((c >= 0x10000) && (c <= 0x10FFFF)));
    }

    @Override
    public void close() throws IOException {
        underlyingReader.close();
    }

}