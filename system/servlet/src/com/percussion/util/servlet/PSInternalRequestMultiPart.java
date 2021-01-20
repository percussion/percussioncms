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

import com.percussion.util.PSCharSets;
import com.percussion.util.PSStringOperation;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to POST parameters in the multi-part form.
 *
 * @author DavidBenua
 */
class PSInternalRequestMultiPart
   extends PSInternalRequest
   implements HttpServletRequest
{
   /**
    * Constructs an instance from a given servlet request.
    *
    * @param req The original servlet request object.
    */
   public PSInternalRequestMultiPart(HttpServletRequest req)
   {
      super(req);
      this.setMethod("POST");
   }

   /**
    * Empty the body. It is used when forwarding the request instance to
    * another servlet within the same container where we don't have to
    * re-package the same set of parameters into the body, and to avoid the 
    * target servlet receiving the same set of parameters twice.
    */
   public void emptyBody()
   {
      m_bos = new ByteArrayOutputStream();
      m_prepared = true;
      setContentLength(); // has to call this last
   }
   
   /**
    * Prepare the body and/or header of the current parameters. This method
    * must be called before pass this object to another servlet, for example
    * through {@link javax.servlet.RequestDispatcher}.
    */
   public void prepareBody()
   {
      PSMultipartWriter httpWriter = null;
      try
      {
         boolean hasContent = false;
         m_bos = new ByteArrayOutputStream();
         httpWriter = new PSMultipartWriter(m_bos, getBodyEncoding());

         Enumeration pNames = this.getParameterNames();
         if (pNames.hasMoreElements())
            hasContent = true;
         while (pNames.hasMoreElements())
         {
            String pName = (String) pNames.nextElement();
            ArrayList pValues =
               new ArrayList(Arrays.asList(this.getParameterValues(pName)));
            Iterator pIter = pValues.iterator();
            while (pIter.hasNext())
            {
               String pValue = (String) pIter.next();
               httpWriter.addField(pName, pValue);
            }
         }
         Iterator bodyParts = m_bodyParts.iterator();
         if (bodyParts.hasNext())
            hasContent = true;
         while (bodyParts.hasNext())
         {
            PSHttpBodyPart bPart = (PSHttpBodyPart) bodyParts.next();
            httpWriter.addBytes(
               bPart.getFieldName(),
               bPart.getFileName(),
               bPart.getMimeType(),
               bPart.getEncoding(),
               bPart.getBytes());
         }
         if (hasContent)
            httpWriter.addEndMarker();
      }
      catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
      m_prepared = true;
      // set header for "Content-Type" and "content-length"
      // this has to be called after set "m_prepared"
      setContentLength();
      String contentType =
         "multipart/form-data; charset="
            + PSCharSets.getStdName(getBodyEncoding())
            + "; boundary="
            + httpWriter.getSeparator();
      setContentType(contentType);
   }

   // see javax.servlet.ServletRequest#getInputStream()
   public ServletInputStream getInputStream()
   {
      if (!m_prepared)
      {
         prepareBody();
      }
      if (m_reader)
      {
         throw new IllegalStateException();
      }
      m_stream = true;
      ServletInputStream res =
         (ServletInputStream) new InternalInputStream(new ByteArrayInputStream(m_bos
            .toByteArray()));
      return res;
   }

   /**
    * Get the encoding of the body.
    *
    * @return character encoding, it is default to UTF8 if the encoding
    *    is not defined.
    */
   private String getBodyEncoding()
   {
      String encoding = getCharacterEncoding();
      if (encoding == null)
      {
         encoding = PSCharSets.rxJavaEnc();
      }
      else
      {
         // strip double quote if exist. Some app server such as
         // WebSphere 5.1 will have "\"" at the beginning and end.
         encoding = PSStringOperation.replace(encoding, "\"", "");
         encoding = PSCharSets.getJavaName(encoding);
      }
      return encoding;
   }

   /**
    * Set the "content-type" header to the given value.
    *
    * @param contentType The to be set value of the content type, assume
    *    it is not <code>null</code> or empty.
    */
   private void setContentType(String contentType)
   {
      m_contentType = contentType;
      // has to set with lower case, CANNOT BE "Content-Type";
      // otherwise, RhythmyxServlet will send out both headers below, which
      // will confuse Rhythmyx Server:
      //
      //       content-type: text/xml
      //       Content-Type: XXXX
      //
      setHeader("content-type", contentType);
   }

   /**
    * Set the "content-length" header to be the length of the "prepared" body.
    */
   private void setContentLength()
   {
      // has to set with lower case, CANNOT BE "Content-Length";
      // otherwise, RhythmyxServlet will send out both headers below, which
      // will confuse Rhythmyx Server:
      //
      //       content-length: 0
      //       Content-Length: XXXX
      //
      setHeader("content-length", String.valueOf(getContentLength()));
   }

   // see javax.servlet.ServletRequest#getContentType()
   public String getContentType()
   {
      if (m_contentType == null)
         return super.getContentType();
      else
         return m_contentType;
   }

   // see javax.servlet.ServletRequest#getContentLength()
   public int getContentLength()
   {
      if (!m_prepared)
      {
         prepareBody();
      }
      return m_bos.size();
   }

   // see javax.servlet.ServletRequest#getReader()
   public BufferedReader getReader() throws UnsupportedEncodingException
   {
      if (!m_prepared)
      {
         prepareBody();
      }
      if (m_stream)
      {
         throw new IllegalStateException("It is operated in stream mode, not in text mode");
      }
      m_reader = true;
      InputStream is = new ByteArrayInputStream(m_bos.toByteArray());
      InputStreamReader ir =
         new InputStreamReader(is, getBodyEncoding());
      BufferedReader br = new BufferedReader(ir);
      return br;
   }

   /**
    * Add the given body part.
    *
    * @param bPart the body part, it may not be <code>null</code>.
    */
   public void addBodyPart(PSHttpBodyPart bPart)
   {
      if (bPart == null)
         throw new IllegalArgumentException("bPart may not be null");
      m_bodyParts.add(bPart);
   }

   /**
    * The output byte array which contains the POST content. It is initialized
    * by {@link #prepareBody()}. It is <code>null</code> if not initialized
    * yet.
    */
   private ByteArrayOutputStream m_bos = null;

   /**
    * The content type is set by {@link #setContentType(String)}. It is
    * <code>null</code> if has not set yet.
    */
   private String m_contentType = null;

   /**
    * <code>true</code> if getting the response info it operated in stream mode.
    * It is set by the {@link #getInputStream()}.
    */
   private boolean m_stream = false;

   /**
    * <code>true</code> if getting the response info it operated in text mode.
    * It is set by the {@link @getReader()}.
    */
   private boolean m_reader = false;

   /**
    * <code>true</code> if the POST body has been prepared for send. It is
    * set by {@link #prepareBody()}.
    */
   private boolean m_prepared = false;

   /**
    * Holds a list of <code>PSHttpBodyPart</code> objects, never
    * <code>null</code>, but may be empty.
    */
   private List m_bodyParts = new ArrayList();


   /**
    * A concrete instance of ServletInputStream. This class is need to
    * fully implement the HttpServletResponse interface.
    *
    * @author DavidBenua
    */
private class InternalInputStream extends ServletInputStream
   {
      /**
      * create our stream from an existing byte array
      *
      * @param bis The byte array, may not be <code>null</code>.
      */
      public InternalInputStream(ByteArrayInputStream bis)
      {
        super();
        if (bis == null)
          throw new IllegalArgumentException("bis may not be null");
        m_bis = bis;
      }

      @Override
      public int available() {
         return m_bis.available();
      }

      /**
      * read the input stream
      *
      * @see java.io.InputStream#read()
      */
      public int read() throws IOException
      {
        return m_bis.read();
      }

      /**
      * The stream that backs our stream.
      */
      private ByteArrayInputStream m_bis;

   }


}
