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
package com.percussion.content;

import com.percussion.util.PSCharSets;

import java.io.IOException;
import java.io.InputStream;

/**
 * A very simple implementation of IPSMimeContent
 *
 */
public class PSMimeContentAdapter implements IPSMimeContent
{

   /**
    * Construct a new PSMimeContentAdapter.
    * 
    * @param   in The input stream. Cannot be <CODE>null</CODE>.
    *
    * @param   mimeType The MIME type. If this is <CODE>null</CODE> or
    * empty, will default to "application/octet-stream".
    *
    * @param   transferEncoding The transfer encoding. If <CODE>null</CODE>,
    * it means there is no transfer encoding.
    *
    * @param   charEncoding The character encoding. Can be <CODE>null</CODE>
    * if <CODE>in</CODE> is not a character stream. Can be either a standard
    * name from IANA or a Java name.
    *
    * @param   contentLength The approximate content length, or -1 if not
    * known. This is just a hint -- the true length can be longer or shorter.
    * 
    */
   public PSMimeContentAdapter(
      InputStream in,
      String mimeType,
      String transferEncoding,
      String charEncoding,
      long contentLength
      )
   {
      if (in == null)
         throw new IllegalArgumentException("Input stream cannot be null");

      m_in = in;
      if (mimeType == null || mimeType.length() == 0)
         mimeType = "application/octet-stream";

      m_mimeType = mimeType;
      m_xferEnc = transferEncoding;
      
      if (charEncoding != null)
      {
         m_charEnc = PSCharSets.getStdName(charEncoding);
      }
      
      m_cntLen = contentLength;
      if (m_cntLen < -1)
         m_cntLen = -1;
   }

   /**
    * Returns the byte stream for this content. Transfers responsibility
    * for closing/cleaning up the stream to the caller. It is up to
    * the particular implementation to decide whether it is valid to
    * call this method more than once (getting more than one input stream),
    * and implementations should document accordingly.
    * <P>
    * Any transfer decoding that needs to be done should be done to the
    * bytes returned from this stream.
    * 
    * This method must never return null. If the object is not properly
    * initialized, it must throw an IllegalStateException.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/11/8
    * 
    * @return   InputStream
    */
   public InputStream getContent() throws IllegalStateException
   {
      if (m_in == null)
         throw new IllegalStateException();
      InputStream in = m_in;
      m_in = null;
      return in;
   }

   /**
    * Sets the byte stream for this content. If an existing stream
    * is set, it will not be closed.
    *
    * @param in An InputStream. Cannot be null.
    */
   public void setContent(InputStream in) throws IllegalStateException
   {
      if (in == null)
         throw new IllegalArgumentException();

      m_in = in;
   }

   /**
    * Returns the approximate length of this content in bytes,
    * or -1 if not known.
    *
    * Note that this return value is merely a hint for performance
    * optimization reasons, and it is <B>not</B> an error to read
    * more bytes from the stream than this length, if the bytes are
    * available.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/11/8
    * 
    * @return   long The approximate content length if known, or -1
    * if not known.
    */
   public long getContentLength()
   {
      return m_cntLen;
   }

   @Override
   protected void finalize() throws Throwable
   {
      super.finalize();
      if (m_in != null)
      {
         try { m_in.close(); } catch (IOException e) { /* ignore */ }
      }
      m_in = null;
   }
   
   public void setContentLength(long len)
   {
      m_cntLen = len;
   }

   /**
    * Returns the name of the standard MIME type for this content (e.g., text/xml).
    * See http://www.isi.edu/in-notes/iana/assignments/media-types/media-types
    * for an official list of MIME types.
    *
    * This method must never return null. If the MIME type is not known,
    * it should return "application/octet-stream".
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/11/8
    * 
    * @return   String
    */
   public String getMimeType()
   {
      return m_mimeType;
   }

   /**
    * Sets the MIME type for this content.
    * 
    * @param   mimeType The MIME type name. Cannot be <CODE>null</CODE>
    * or empty.
    * 
    */
   public void setMimeType(String mimeType)
   {
      if (mimeType == null || mimeType.length() == 0)
         throw new IllegalArgumentException("MIME type must not be null");
      m_mimeType = mimeType;
   }

   /**
    * Returns the name of the transfer encoding applied to the content stream.
    * If the stream represents raw bytes (where any octet-sequence may occur),
    * the encoding will be "binary". If the stream represents base64 encoded
    * bytes, the encoding will be "base64" and so on. This may return
    * <CODE>null</CODE> if no encoding is defined.
    *
    * See IPSMimeContentTypes for some of the predefined encodings that
    * we support.
    *
    */
   public String getTransferEncoding()
   {
      return m_xferEnc;
   }

   /**
    * Sets the name of the transfer encoding of the content stream.
    * 
    * @param   transferEncoding
    * 
    */
   public void setTransferEncoding(String transferEncoding)
   {
      m_xferEnc = transferEncoding;
   }

   /**
    * Gets the standard IANA name for the character encoding of this character
    * data, or <CODE>null</CODE> if the character encoding is not applicable
    * (e.g., for binary content).
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/11/8
    * 
    * @return   String
    */
   public String getCharEncoding()
   {
      return m_charEnc;
   }

   /**
    * Sets the character encoding for the content stream. 
    * 
    * @param   charEncoding The standard IANA name for the character
    * encoding, or <CODE>null</CODE> if the character encoding is not
    * applicable.
    * 
    */
   public void setCharEncoding(String charEncoding)
   {
      m_charEnc = charEncoding;
   }

   /**
    * Convenience method.
    */
   @Override
   public String toString()
   {
      StringBuffer buf = new StringBuffer(50);
      buf.append("MIME Content\n");
      if (m_mimeType != null)
      {
         buf.append("Content Type: ");
         buf.append(m_mimeType);
         buf.append("\n");
      }

      if (m_charEnc != null)
      {
         buf.append("Character Encoding: ");
         buf.append(m_charEnc);
         buf.append("\n");
      }

      if (m_xferEnc != null)
      {
         buf.append("Transfer Encoding: ");
         buf.append(m_xferEnc);
         buf.append("\n");
      }
      
      if (m_cntLen > 0)
      {
         buf.append("Content Length: " + m_cntLen);
         buf.append("\n");
      }

      return buf.toString();
   }

   /**
    * Returns the name of this content, or <CODE>null</CODE> if the
    * content has no name.
    * <p>
    * Note that the interpretation of the name depends on the context
    * in which this content was created. It may be a file name, or
    * it may simply be an unstructured descriptive name (such as
    * "My Blob").
    *
    * @return The name of the content. May be <CODE>null</CODE>.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Sets the name of this content.
    *
    * @param name The content name, or <CODE>null</CODE> if the content
    * is to have no name.
    */
   public void setName(String name)
   {
      m_name = name;
   }

   protected InputStream m_in;
   protected String m_mimeType;
   protected String m_xferEnc;
   protected String m_charEnc;
   protected long m_cntLen;
   protected String m_name;

}
