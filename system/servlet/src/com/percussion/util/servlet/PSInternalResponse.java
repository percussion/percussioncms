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
package com.percussion.util.servlet;

import com.percussion.util.PSCharSetsConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *  Provides a way to capture the output from an
 * {@link PSInternalRequest}. This object holds the response from a
 * servlet called via {@link javax.servlet.RequestDispatcher}
 *
 * This implemenation provides
 * either a Writer or an OutputStream as requested.
 * We actually have a true Writer and an OutputStream backing
 * both, since we don't want to use a character encoding both
 * ways (Writer -> OutputStream -> Writer).  So we use no
 * encoding at all (as none is relevant) when the target resource
 * uses a Writer.
 *
 * Character encoding defaults to UTF8 (not the platform
 * default) and can be overriden by setContentType.  The MIME
 * content type itself is ignored, only the character encoding
 * is used.
 *
 * For servlets that return an XML document, see
 * PSInternalResponseXML.
 */
class PSInternalResponse
   extends HttpServletResponseWrapper
   implements HttpServletResponse
{
  /**
   * Creates a HttpServletResponse for internal requests
   *
   * @param response - the response from the calling servlet.
   */
   public PSInternalResponse(HttpServletResponse response)
   {
      super(response);
   }

   /**
    * Get a Writer designed to buffer the output.
    *
    * @return the writer, never <code>null</code>.
    *
    * @throws IllegalStateException if the caller has already
    *   reference the byte stream.
    */
   public PrintWriter getWriter()
   {
      if (m_isStreamUsed)
         throw new IllegalStateException(ILLEGAL_STREAM);
      m_isWriterUsed = true;
      if (m_initialSize > 0)
      {
         m_swriter = new StringWriter(m_initialSize);
      }
      else
      {
         m_swriter = new StringWriter();
      }
      return new PrintWriter(m_swriter);
   }

   /**
    * Gets a ServletOutputStream designed to buffer the output.
    *
    * @return the stream, never <code>null</code>.
    *
    * @throws IllegalStateException if the caller has already referenced
    *   the Writer
    */
   public ServletOutputStream getOutputStream()
   {
      if (m_isWriterUsed)
         throw new IllegalStateException(ILLEGAL_WRITER);
      m_isStreamUsed = true;
      if (m_initialSize > 0)
      {
         m_bos = new ByteArrayOutputStream(m_initialSize);
      }
      else
      {
         m_bos = new ByteArrayOutputStream();
      }
      m_sos = new InternalOutputStream(m_bos);
      return m_sos;
   }

   /**
    * Sets the MIME Content type. Only the character set setting has
    * any effect. If a charset is specified, it will override the
    * default encoding.
    *
    * @param cType the to be set content type. It may not be <code>null</code>
    *    or empty.
    */
   public void setContentType(String cType)
   {
      if (cType == null || cType.trim().length() == 0)
         throw new IllegalArgumentException("cType may not be null or empty");
      m_contentType = cType;
      if (m_contentType.contains("charset"))
      {
         boolean charsetnext = false;
         StringTokenizer st = new StringTokenizer(cType, " ;=");
         while (st.hasMoreTokens())
         {
            String next = st.nextToken();
            if (charsetnext)
            {
               m_charEncoding = next;
               return;
            }
            if (next.equalsIgnoreCase("charset"))
            {
               charsetnext = true;
            }
         }
      }
   }

   /** Has no effect. */
   public void setLocale(Locale x)
   {
      // ignore
   }

   // @see javax.servlet.http.HttpServletResponse#setStatus(int)
   public void setStatus(int status)
   {
      m_status = status;
   }

   // @see javax.servlet.http.HttpServletResponse#sendError(int)
   public void sendError(int status)
   {
      this.m_status = status;
   }

   // @see javax.servlet.http.HttpServletResponse#sendError(int, String)
   public void sendError(int status, String errorMessage)
   {
      this.m_errorMessage = errorMessage;
      sendError(status);
   }

   /**
    * gets the status code.
    *
    * @return the status code.
    */
   public int getStatus()
   {
      return m_status;
   }

   /**
    * Get the errormessage if one has been set by a call to
    * <code>sendError()<code>.
    *
    * @return the error message if one has been set. <code>null</code>
    *   otherwise.
    */
   public String getErrorMessage()
   {
      return m_errorMessage;
   }

   /**
    * Retrieves the buffered output, converting using the current
    * encoding if necessary.  not simply toString() because we need to throw
    * UnsupportedEncodingException.
    *
    * @return The buffered output String, never <code>null</code>, but may be
    *     empty.
    *
    * @throws UnsupportedEncodingException if the caller has set
    *     an unsupported encoding.
    */
   public String getString() throws UnsupportedEncodingException
   {
      if (m_isWriterUsed)
      {
         return m_swriter.toString();
      }
      else
      {
         if (m_isStreamUsed)
         {
            return m_bos.toString(m_charEncoding);
         }
         else
            return ""; // target didn't write anything
      }
   }

   /**
    * Retrieves the buffered output as a byte stream, converting
    * using the current encoding if necessary.
    *
    * @return the buffered output as a byte stream.
    *
    */
   public InputStream getInputStream()
   {
      InputStream is = null;
      if (m_isStreamUsed)
      {
         is = new ByteArrayInputStream(m_bos.toByteArray());
      }
      else
         if (m_isWriterUsed)
         {
            try
            {
               is =
                  new ByteArrayInputStream(
                     m_swriter.toString().getBytes(this.m_charEncoding));
            }
            catch (UnsupportedEncodingException e)
            {
               // this should never happen, as we specify the
               // default encoding (IS0-8859-1) which should never
               // fail.
               m_logger.error("unsupported encoding " + this.m_charEncoding);
               m_logger.error("unexpected exception ", e);
            }
         }
      return is;
   }

   /**
    * Indicates if the response is committed yet. See also
    * {@link #flushBuffer()}.
    *
    * @return <code>true</code> if the buffer has been committed.
    */
   public boolean isCommitted()
   {
      return m_isCommitted;
   }

   /**
    * Flush the output buffer. Also commits the response.
    *
    * @see javax.servlet.ServletResponse#flushBuffer()
    */
   public void flushBuffer() throws IOException
   {
      if (m_isStreamUsed)
      {
         m_bos.flush();
      }
      if (m_isWriterUsed)
      {
         m_swriter.flush();
      }
      m_isCommitted = true;
   }

   /**
    * Gets the actual size of the content in the buffer.
    *
    * @return the size of the content.
    *
    * @see javax.servlet.ServletResponse#getBufferSize()
    */
   public int getBufferSize()
   {
      if (m_isStreamUsed)
      {
         return m_bos.size();
      }
      if (m_isWriterUsed)
      {
         return m_swriter.getBuffer().length();
      }
      return 0;
   }

   /**
    * Resets the contents of the buffer. The buffer will be empty,
    * but any Stream or Writer object obtained from this response
    * object will still be valid.
    *
    * @throws IllegalStateException if the response has already
    * been committed.
    *
    * @see javax.servlet.ServletResponse#resetBuffer()
    *
    */
   public void resetBuffer()
   {
      if (m_isCommitted)
      {
         throw new IllegalStateException();
      }
      if (m_isWriterUsed)
      {
         m_swriter.getBuffer().setLength(0);
      }
      if (m_isStreamUsed)
      {
         m_bos.reset();
      }
   }

   /**
    * sets the initial buffer size for the response buffer.
    * This method cannot be called after getInputStream() or
    * getWriter().
    *
    * @see javax.servlet.ServletResponse#setBufferSize(int)
    */
   public void setBufferSize(int size)
   {
      m_initialSize = size;
   }

   /**
    * Sets the content length header.  This implementation ignores
    * this header, and this method does nothing.
    *
    * @see javax.servlet.ServletResponse#setContentLength(int)
    */
   public void setContentLength(int arg0)
   {
      //does nothing in this implementation
   }
   
   

   /**
    * Determines if the writer is used for this reponse.
    *
    * @return <code>true</code> if writer is used (or {@link #getWriter()
    *    was called); <code>false</code> otherwise.
    */
   protected boolean isWriterUsed()
   {
      return m_isWriterUsed;
   }

   /**
    * Determines if the stream is used for this reponse.
    *
    * @return <code>true</code> if stream is used (or {@link #getOutputStream()
    *    was called); <code>false</code> otherwise.
    */
   protected boolean isStreamUsed()
   {
      return m_isStreamUsed;
   }
   
   /**
    * Overrides setHeader so we can capture the headers and allow
    * us access to them via a {@link #getHeader(String)} method. 
    */
   public void setHeader(String name, String val)
   {
      m_headers.put(name, val);
      super.setHeader(name, val);
   }
   
   /**
    * Returns a specified response header
    * @param name, the name of the header to be returned, 
    * cannot be <code>null</code>.
    * @return the header value string or <code>null</code>
    * if not found.
    */
   public String getHeader(String name)
   {
      return (String)m_headers.get(name);
   }

   /**
    * This is used by {@link #getOutputStream()}, implemented
    * <code>ServletOutputStream</code> from a <code>ByteArrayOutputStream</code>
    * object.
    */
   private class InternalOutputStream extends ServletOutputStream
   {
      /**
       * Constructs an instance from a byte array stream.
       *
       * @param bos The byte array stream, may not be <code>null</code>.
       */
      private InternalOutputStream(ByteArrayOutputStream bos)
      {
         if (bos == null)
            throw new IllegalArgumentException("bos may not be null");

         m_bos = bos;
      }

      // see java.io.OutputStream#write(int)
      public void write(int b) throws IOException
      {
         m_bos.write(b);
      }

      /**
       * The buffer to hold the output content. Initialized by ctor,
       * never <code>null</code> after that.
       */
      private ByteArrayOutputStream m_bos;

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
           throw new RuntimeException("Not yet implemented");
       }
   }

   /**
    * The initial size for the buffer or writer. This is only recognized
    * if it is set before the buffer (or writer) is initially
    * referenced.
    */
   private int m_initialSize = 0;

   /**
    * The Writer we convey. Initialized by {@link #getWriter()}. It is
    * <code>null</code> if has not been initialized
    */
   private StringWriter m_swriter = null;

   /**
    * A buffer, alternatively, to accumulate bytes. This is used when
    * getting repose in stream mode. It is set by {@link #getOutputStream()}.
    * It may be <code>null</code> if has not set yet.
    */
   private ByteArrayOutputStream m_bos = null;

   /**
    * A ServletOutputStream we convey, tied to the m_bos.
    * It is set by {@link #getOutputStream()}.
    * It may be <code>null</code> if has not set yet.
    */
   private ServletOutputStream m_sos = null;

   /**
    * The character encoding that is set by {@link #setContentType(String)}
    * and is default to UTF8.
    */
   private String m_charEncoding = PSCharSetsConstants.rxJavaEnc();

   /**
    * MIME Content Type that is set by {@link #setContentType(String)}. It
    * may be <code>null</coce> if has not been set yet.
    */
   private String m_contentType = null;

   /**
    * The error message that is set by sendError(). It may be <code>null</code>
    * if has not been set yet.
    */
   private String m_errorMessage = null;

   /**
    * <code>true</code> if {@link #getWriter() was called; <code>false</code>
    * otherwise.
    */
   private boolean m_isWriterUsed = false;

   /**
    * <code>true</code> if {@link #getOutputStream() was called;
    * <code>false</code> otherwise.
    */
   private boolean m_isStreamUsed = false;

   /**
    * The HTTP status set by the target.
    */
   private int m_status = 200;

   /**
    * Indicates if this response been committed. <code>true</code> if it has
    * committed; <code>false</code> otherwise.
    */
   private boolean m_isCommitted = false;
   
   /**
    * Holds all response headers
    */
   private Map m_headers = new HashMap();

   /**
    * our private logger.
    */
   private Logger m_logger = LogManager.getLogger(this.getClass());

   private static final String ILLEGAL_STREAM =
      "Output stream cannot be used when Writer is already in use";
   private static final String ILLEGAL_WRITER =
      "Writer cannot be used when Output Stream is already in use";
}
