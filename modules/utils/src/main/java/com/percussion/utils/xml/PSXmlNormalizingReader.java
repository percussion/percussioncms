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
