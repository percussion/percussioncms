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

package com.percussion.utils.servlet;


import org.springframework.util.FileCopyUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PSExplicitEtagFilter extends OncePerRequestFilter {

    private static final String HEADER_ETAG = "ETag";

    private static final String HEADER_IF_NONE_MATCH = "If-None-Match";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ShallowEtagResponseWrapper responseWrapper = new ShallowEtagResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        byte[] body = responseWrapper.toByteArray();
        int statusCode = responseWrapper.getStatusCode();

        if (isEligibleForEtag(request, responseWrapper, statusCode, body)) {
            String responseETag = generateETagHeaderValue(body);
            response.setHeader(HEADER_ETAG, responseETag);

            String requestETag = request.getHeader(HEADER_IF_NONE_MATCH);
            if (responseETag.equals(requestETag)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
            else {
                copyBodyToResponse(body, response);
            }
        }
        else {
            copyBodyToResponse(body, response);
        }
    }

    private void copyBodyToResponse(byte[] body, HttpServletResponse response) throws IOException {
        if (body.length > 0) {
            response.setContentLength(body.length);
            FileCopyUtils.copy(body, response.getOutputStream());
        }
    }

   
    protected boolean isEligibleForEtag(HttpServletRequest request, HttpServletResponse response,
            int responseStatusCode, byte[] responseBody) {
        return (responseStatusCode >= 200 && responseStatusCode < 300);
    }

  
    protected String generateETagHeaderValue(byte[] bytes) {
        StringBuilder builder = new StringBuilder("\"0");
        appendHashString(bytes, builder);
        builder.append('"');
        return builder.toString();
    }


    
    private static class ShallowEtagResponseWrapper extends HttpServletResponseWrapper {

        private final ByteArrayOutputStream content = new ByteArrayOutputStream();

        private final ServletOutputStream outputStream = new ResponseServletOutputStream();

        private PrintWriter writer;

        private int statusCode = HttpServletResponse.SC_OK;

        private ShallowEtagResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.statusCode = sc;
        }

        @Override
        public void setStatus(int sc, String sm) {
            super.setStatus(sc, sm);
            this.statusCode = sc;
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            this.statusCode = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            this.statusCode = sc;
        }

        @Override
        public void setContentLength(int len) {
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return this.outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (this.writer == null) {
                String characterEncoding = getCharacterEncoding();
                this.writer = (characterEncoding != null ? new ResponsePrintWriter(characterEncoding) :
                        new ResponsePrintWriter(WebUtils.DEFAULT_CHARACTER_ENCODING));
            }
            return this.writer;
        }

        @Override
        public void resetBuffer() {
            this.content.reset();
        }

        @Override
        public void reset() {
            super.reset();
            resetBuffer();
        }

        private int getStatusCode() {
            return statusCode;
        }

        private byte[] toByteArray() {
            return this.content.toByteArray();
        }

        private class ResponseServletOutputStream extends ServletOutputStream {

            private WriteListener writeListener = null;

            @Override
            public void write(int b) throws IOException {
                content.write(b);
                if (writeListener != null) {
                    writeListener.notify();
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                content.write(b, off, len);
                if (writeListener != null) {
                    writeListener.notify();
                }
            }

            /**
             * This method can be used to determine if data can be written without blocking.
             *
             * @return <code>true</code> if a write to this <code>ServletOutputStream</code>
             * will succeed, otherwise returns <code>false</code>.
             * @since Servlet 3.1
             */
            @Override
            public boolean isReady() {
                return false;
            }

            /**
             * Instructs the <code>ServletOutputStream</code> to invoke the provided
             * {@link WriteListener} when it is possible to write
             *
             * @param writeListener the {@link WriteListener} that should be notified
             *                      when it's possible to write
             * @throws IllegalStateException if one of the following conditions is true
             *                               <ul>
             *                               <li>the associated request is neither upgraded nor the async started
             *                               <li>setWriteListener is called more than once within the scope of the same request.
             *                               </ul>
             * @throws NullPointerException  if writeListener is null
             * @since Servlet 3.1
             */
            @Override
            public void setWriteListener(WriteListener writeListener) {
                //TODO: Implement me
                throw new RuntimeException("Not implemented");
            }
        }

        private class ResponsePrintWriter extends PrintWriter {

            private ResponsePrintWriter(String characterEncoding) throws UnsupportedEncodingException {
                super(new OutputStreamWriter(content, characterEncoding));
            }

            @Override
            public void write(char buf[], int off, int len) {
                super.write(buf, off, len);
                super.flush();
            }

            @Override
            public void write(String s, int off, int len) {
                super.write(s, off, len);
                super.flush();
            }

            @Override
            public void write(int c) {
                super.write(c);
                super.flush();
            }
        }
    }

    private static final char[] HEX_CHARS =
       {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

     
       public static byte[] getHash(byte[] bytes)
       {
          try
          {
             // MessageDigest is not thread-safe
             MessageDigest messageDigest = MessageDigest.getInstance("MD5");
             return messageDigest.digest(bytes);
          }
          catch (NoSuchAlgorithmException ex)
          {
             throw new IllegalStateException("Could not find MD5 MessageDigest instance", ex);
          }
       }

       private static char[] getHashChars(byte[] bytes)
       {
          byte[] hash = getHash(bytes);
          char chars[] = new char[32];
          for (int i = 0; i < chars.length; i = i + 2)
          {
             byte b = hash[i / 2];
             chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
             chars[i + 1] = HEX_CHARS[b & 0xf];
          }
          return chars;
       }

       public static String getHashString(byte[] bytes)
       {
          return new String(getHashChars(bytes));
       }

       
       public static StringBuilder appendHashString(byte[] bytes, StringBuilder builder)
       {
          builder.append(getHashChars(bytes));
          return builder;
       }
}
