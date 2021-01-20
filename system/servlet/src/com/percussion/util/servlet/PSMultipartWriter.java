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

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Writes the postbody for a HTML form, using <code>multipart/form-data<code>
 * encoding.
 */
class PSMultipartWriter extends OutputStreamWriter
{
   /**
    * Default constructor.
    * @param instream      The stream that collects the post body output.
    * @param encoding      The encoding for this post body. Use the Java name.
    *                      must not be <code>null</code>
    * @throws UnsupportedEncodingException when the provided encoding is not
    *                      a recognized Java encoding name.
    */
   public PSMultipartWriter(ByteArrayOutputStream instream, String encoding)
      throws UnsupportedEncodingException
   {
      super(instream, encoding);
      m_bos = instream;
   }

   /**
    * Writes a string and appends a NEW_LINE marker.
    *
    * @param str the string to write, it may not be <code>null</code>.
    *
    * @throws IOException when the string cannot be written.
    */
   public void writeln(String str) throws IOException
   {
      if (str == null)
         throw new IllegalArgumentException("str may not be null");

      super.write(str);
      super.write(NEW_LINE);
   }

   /**
    * Gets the current contents of the post body as a byte array.
    *
    * @return the current contents of the post body, never <code>null</code>.
    */
   public byte[] toByteArray()
   {
      return m_bos.toByteArray();
   }

   /**
    * Gets the current contents of the post body as an output stream.
    *
    * @return the current contents of the post body, never <code>null</code>.
    */
   public OutputStream getOutputStream()
   {
      return m_bos;
   }

   /**
    * Gets the size of the post body.
    * @return the number of bytes in the post body.
    */
   public int getOutputStreamSize()
   {
      m_Logger.debug("Output Stream size: " + m_bos.size());
      return m_bos.size();
   }

   /**
    * Gets the current contents of the post body as a String. Uses the platform
    * default encoding. This method is intended primarily for debugging purposes
    *
    * @return the post body, converted to a string, never <code>null</code>.
    */
   public String getString() throws java.io.IOException
   {
      super.flush();
      m_bos.flush();
      return m_bos.toString();
   }

   /**
    * Sets the separator. This separator should be passed as it will appear in
    * the <code>boundary=</code>header, without the leading or trailing "--".
    * This method should only be called once, before the first field is added.
    * The default separator will be used if this is not been called.
    *
    * @param sep the separator. It may not be <code>null</code> or empty.
    */
   public void setSeparator(String sep)
   {
      if (sep == null || sep.trim().length() == 0)
         throw new IllegalArgumentException("sep may not be null or empty");

      m_Logger.debug("Separator is:" + sep);
      m_separator = sep;
   }

   /**
    * Get the separator, which may be set by {@link #setSeparator(String)} or
    * the default separator.
    * 
    * @return The separator, never <code>null</code> or empty.
    */
   public String getSeparator()
   {
      return m_separator;
   }
   
   /**
    * Adds a simple text field to the post body.
    *
    * @param fname the field name, may not be <code>null</code> or empty.
    *
    * @param fvalue the field value, may not be <code>null</code>.
    *
    * @throws IOException when an error occurs writing the byte array.  This
    *   should never happen.
    */
   public void addField(String fname, String fvalue) throws IOException
   {
      if (fname == null || fname.trim().length() == 0)
         throw new IllegalArgumentException("fname may not be null or empty.");
      if (fvalue == null)
         throw new IllegalArgumentException("fvalue may not be empty.");

      m_Logger.debug("writing field " + fname + " value " + fvalue);
      writeln("--" + m_separator);
      writeln("Content-Disposition: form-data; name=\"" + fname + "\"");
      writeln("");
      writeln(fvalue);
   }

   /**
    * Adds a binary file attachment to the post body.
    *
    * @param fldname  the HTML parameter name for this attachment, it may not
    *    be <code>null</code> or empty.
    *
    * @param filename the absolute path name of the file to attach. It may be
    *    <code>null</code> or empty.
    *
    * @param mimeType the MIME content type / subtype of this file attachment.
    *    It may be <code>null</code> or empty.
    *
    * @param encoding the character encoding of this file. For true binary files
    *                 (for example: images) this parameter should be
    *                  <code>null</code>.
    **/
   public void addFile(
      String fldname,
      String filename,
      String mimeType,
      String encoding)
      throws java.io.IOException
   {
      if (fldname == null || fldname.trim().length() == 0)
         throw new IllegalArgumentException("fldname may not be null or empty");
      if (filename == null || filename.trim().length() == 0)
         throw new IllegalArgumentException("filename may not be null or empty");
      if (mimeType == null || mimeType.trim().length() == 0)
         throw new IllegalArgumentException("mimeType may not be null or empty");

      m_Logger.debug("writing field " + fldname + " file " + filename);

      File f = new File(filename);
      if (f == null)
      {
         return;
      }
      String ctype;
      if (encoding != null && encoding.length() > 0)
      {
         ctype = mimeType + "; charset=" + encoding;
      }
      else
      {
         ctype = mimeType;
      }
      writeln("--" + m_separator);
      writeln(
         "Content-Disposition: form-data; name=\""
            + fldname
            + "\"; "
            + "filename=\""
            + f.getName()
            + "\"");
      writeln("Content-Type: " + ctype);
      writeln("");
      super.flush();
      // now copy the file
      FileInputStream fis = new FileInputStream(f);
      PSHttpUtils.copyStream(fis, m_bos);
      m_bos.flush();
      writeln("");
      super.flush();
   }

   /**
    * Adds a binary data to the post body.
    *
    * @param fldname  the HTML parameter name for this attachment, it may not
    *    be <code>null</code> or empty.
    * @param filename the path name of the file to attach. It may be
    *    <code>null</code> or empty.
    * @param mimeType the MIME content type / subtype of this file attachment.
    *    It may be <code>null</code> or empty.
    *
    * @param encoding the character encoding of this file. For true binary files
    *                 (for example: images) this parameter should be
    *                  <code>null</code>.
    *
    * @param binaryData the binary data for the post body. It may be
    *    <code>null</code>.
    *
    * @throws java.io.IOException if I/O error occurs.
    **/
   public void addBytes(
      String fldname,
      String filename,
      String mimeType,
      String encoding,
      byte[] binaryData)
      throws java.io.IOException
   {
      if (fldname == null || fldname.trim().length() == 0)
         throw new IllegalArgumentException("fldname may not be null or empty");
      if (mimeType == null || mimeType.trim().length() == 0)
         throw new IllegalArgumentException("mimeType may not be null or empty");
      if (binaryData == null)
         throw new IllegalArgumentException("binaryData may not be null");

      m_Logger.debug("writing field " + fldname);
      String ctype;
      if (encoding != null && encoding.length() > 0)
      {
         ctype = mimeType + "; charset=" + encoding;
      }
      else
      {
         ctype = mimeType;
      }
      writeln("--" + m_separator);
      writeln(
         "Content-Disposition: form-data; name=\""
            + fldname
            + "\"; "
            + "filename=\""
            + filename
            + "\"");
      writeln("Content-Type: " + ctype);
      writeln("");
      super.flush();
      m_bos.write(binaryData);
      writeln("");
      m_bos.flush();
   }

   /**
    * Adds the ending separator to the post body.  The ending separator has
    * an extra trailing "--" that indicates that this is the end of the post
    * body.
    *
    * @throws IOException when an error occurs writing to the output stream.
    *                     This should never happen.
    **/
   public void addEndMarker() throws IOException
   {
      m_Logger.debug("writing end marker");
      writeln("--" + m_separator + "--");
      super.flush();
      m_bos.flush();
   }

   /**
    * The NEW_LINE marker for HTTP is always <code>CR-LFM</code>, even if the platform
    * default is <code>LF</code> only, as it is on most Unix platforms.
    **/
   public final static String NEW_LINE = "\r\n";

   /**
    * Collects the output as bytes, initialized by ctor, never <code>null</code>
    * after that.
    */
   private ByteArrayOutputStream m_bos;

   /**
    * Holds the separator, withot the leading or trailing "--".
    */
   private String m_separator = MF_SEPARATOR;

   /**
    * The logger for this class.
    */
   private Logger m_Logger = Logger.getLogger(this.getClass());;

   /**
    * The multi-part form separator.
    */
   public final static String MF_SEPARATOR = "||------------A2YTKXU3M546XMNB";

}
