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
package com.percussion.utils;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * Implementation from article on jenkov.com
 * 
 * @author Jakob Jenkov
 * 
 * See http://tutorials.jenkov.com/java-howto/replace-strings-in-streams-arrays-files.html
 */
public class PSTokenReplacingReader extends Reader
{

    protected PushbackReader pushbackReader = null;

    protected IPSTokenResolver tokenResolver = null;

    protected StringBuilder tokenNameBuffer = new StringBuilder();

    protected String tokenValue = null;

    protected int tokenValueIndex = 0;

    public PSTokenReplacingReader(Reader source, IPSTokenResolver resolver)
    {
        this.pushbackReader = new PushbackReader(source, 2);
        this.tokenResolver = resolver;
    }

    @SuppressWarnings("unused")
    public int read(CharBuffer target) throws IOException
    {
        throw new RuntimeException("Operation Not Supported");
    }

    public int read() throws IOException
    {
        if (this.tokenValue != null)
        {
            if (this.tokenValueIndex < this.tokenValue.length())
            {
                return this.tokenValue.charAt(this.tokenValueIndex++);
            }
            if (this.tokenValueIndex == this.tokenValue.length())
            {
                this.tokenValue = null;
                this.tokenValueIndex = 0;
            }
        }

        int data = this.pushbackReader.read();
        if (data != '$') {
            return data;
        }
        
        boolean isXmlEncode = false;

        data = this.pushbackReader.read();
        if (data != '{')
        {
            if (data != 'X') {
                this.pushbackReader.unread(data);
                return '$';
            } 
            
            data = this.pushbackReader.read();
            if (data != 'M') {
                this.pushbackReader.unread(data);
                this.pushbackReader.unread('X');
                return '$';
            }
            
            data = this.pushbackReader.read();
            if (data != 'L') {
                this.pushbackReader.unread(data);
                this.pushbackReader.unread('M');
                this.pushbackReader.unread('X');
                return '$';
            }
            
            data = this.pushbackReader.read();
            if (data != '{') {
                this.pushbackReader.unread(data);
                this.pushbackReader.unread('L');
                this.pushbackReader.unread('M');
                this.pushbackReader.unread('X');
                return '$';
            } else {
                isXmlEncode = true;
            }
        }
        this.tokenNameBuffer.delete(0, this.tokenNameBuffer.length());

        data = this.pushbackReader.read();
        while (data != '}')
        {
            this.tokenNameBuffer.append((char) data);
            data = this.pushbackReader.read();
        }

        if (!isXmlEncode) {
            this.tokenValue = this.tokenResolver.resolveToken(this.tokenNameBuffer.toString());
        } else {
            this.tokenValue = StringEscapeUtils.escapeXml10(this.tokenResolver.resolveToken(this.tokenNameBuffer.toString()));
        }

        if (this.tokenValue == null)
        {
            this.tokenValue = "${" + this.tokenNameBuffer.toString() + "}";
        }
        return this.tokenValue.charAt(this.tokenValueIndex++);

    }

    public int read(char cbuf[]) throws IOException
    {
        return read(cbuf, 0, cbuf.length);
    }

    public int read(char cbuf[], int off, int len) throws IOException
    {
        int charsRead = 0;
        for (int i = 0; i < len; i++)
        {
            int nextChar = read();
            if (nextChar == -1)
            {
                if (charsRead == 0)
                {
                    charsRead = -1;
                }
                break;
            }
            charsRead = i + 1;
            cbuf[off + i] = (char) nextChar;
        }
        return charsRead;
    }

    public void close() throws IOException
    {
        this.pushbackReader.close();
    }

    @SuppressWarnings("unused")
    public long skip(long n) throws IOException
    {
        throw new RuntimeException("Operation Not Supported");
    }

    public boolean ready() throws IOException
    {
        return this.pushbackReader.ready();
    }

    public boolean markSupported()
    {
        return false;
    }

    @SuppressWarnings("unused")
    public void mark(int readAheadLimit) throws IOException
    {
        throw new RuntimeException("Operation Not Supported");
    }

    @SuppressWarnings("unused")
    public void reset() throws IOException
    {
        throw new RuntimeException("Operation Not Supported");
    }
}

