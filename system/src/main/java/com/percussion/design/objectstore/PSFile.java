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

package com.percussion.design.objectstore;

import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSContentFactory;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.error.PSIllegalStateException;
import com.percussion.error.PSRuntimeException;
import com.percussion.util.PSBase64Decoder;
import com.percussion.util.PSBase64Encoder;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * The PSFile abstract class represents the file data of a request to load, save,
 * or remove a file.
 * It contains an input stream from which the raw file data can be read.
 * <P>
 * This object does <B>not</B> encapsulate a request to load, save, or
 * remove a file. It is the result of, or an argument to, such a request.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */

public abstract class PSFile extends PSComponent
{
   /**
    * No args constructor for use with fromXml()
    */
   protected PSFile()
   {
      super();
   }

   /**
    * Constructor with a file, for sending requests to load or remove a file.
    * All returned paths for this file will be normalized. 
    *
    * @param      fileName    the file name.
    *
    * @deprecated  Use {@link #PSFile(URL) PSFile(URL)} instead
    */
   protected PSFile(File fileName)
   {
      try
      {
         IllegalArgumentException ex = validateFileName(fileName);
         if (ex != null)
            throw ex;

         URL url = new URL("file", "", fixPathSeparator(fileName.getPath()));

         m_url = url;

      }
      catch(MalformedURLException e)
      {
         throw new IllegalArgumentException(
               "invalid file name: PSFile/constructor");
      }
   }


   /**
    * Construct a file object.  All returned paths for this file will be
    * normalized. 
    *
    * @param   stream   an input stream positioned at the start of the file
    * data.  If this object is to be converted to XML and/or serialized in the
    * form of a request, then this stream must remain open and unchanged until
    * the conversion process is complete. After the conversion, the input stream
    * will be closed by this object. So consider the input stream as belonging
    * only to this object.
    *
    * @param   fileName       the file name.
    *
    * @param   lastModified   the modification date of the file, or 0L if not
    * known or undefined.
    *
    * @deprecated Use {@link #PSFile(InputStream, URL, long) PSFile(InputSream,
    * URL, long)} instead
    */
   protected PSFile(InputStream stream, File fileName, long lastModified)
   {
      if (stream == null){
         throw new IllegalArgumentException("" +
               "no input stream: PSFile/constructor");
      }

      try
      {
         IllegalArgumentException ex = validateFileName(fileName);
         if (ex != null)
            throw ex;

         URL url = new URL("file", "", fixPathSeparator(fileName.getPath()));

         PSMimeContentAdapter content = new PSMimeContentAdapter(
            stream, // content stream
            PSContentFactory.guessMimeType(fileName), // MIME type
            null, // no transfer encoding
            null, // no char encoding
            -1);  // length unknown

         content.setName(url.getFile());

         m_content = content;
         m_url = url;
         m_lastModified = lastModified;

      }
      catch(MalformedURLException e)
      {
         throw new IllegalArgumentException(
               "invalid file name: PSFile/constructor");
      }

   }

   /**
    * Construct a file object from MIME content. Ususally one
    * does this in order to save this content to the server.
    *
    * @param   content MIME content whose stream is positioned at the start
    * of the data. If this object is to be converted to XML and/or serialized
    * in the form of a request, then this content must remain open and
    * unchanged until this conversion process is complete. After the conversion,
    * the content will be closed by this object. So consider the content as
    * belonging only to this object.
    *
    * @param   fileName       the file name.
    *
    * @param   lastModified   the modification date of the file, or 0L if not
    * known or undefined.
    *
    * @deprecated Use {@link #PSFile(IPSMimeContent, URL, long) PSFile(
    * IPSMimeContent, URL, long)} instead
    */
   protected PSFile(IPSMimeContent content, File fileName, long lastModified)
   {
      if (content == null){
         throw new IllegalArgumentException(
               "no input content: PSFile/constructor");
      }


      try
      {
         IllegalArgumentException ex = validateFileName(fileName);
         if (ex != null)
            throw ex;

         URL url = new URL("file", "", fixPathSeparator(fileName.getPath()));

         m_url = url;
         m_lastModified = lastModified;

         m_content = content;
      }
      catch(MalformedURLException e)
      {
         throw new IllegalArgumentException(
               "invalid file name: PSFile/constructor");
      }


   }

   /**
    * Constructor with a file, for sending requests to load or remove a file.
    * All returned paths for this file will be normalized. 
    *
    * @param      fileName    the file name.
    */
   protected PSFile(URL fileName)
   {
      IllegalArgumentException ex = validateUrl(fileName);
      if (ex != null)
         throw ex;
      try
      {
         m_url = new URL("file", "", fixPathSeparator(fileName.getFile()));
      }
      catch(MalformedURLException e)
      {
         throw new IllegalArgumentException(
               "invalid file name: PSFile/constructor");
      }
   }


   /**
    * Construct a file object.  All returned paths for this file will be
    * normalized. 
    *
    * @param   stream   an input stream positioned at the start of the file
    * data.  If this object is to be converted to XML and/or serialized in the 
    * form of a request, then this stream must remain open and unchanged until
    * the conversion process is complete. After the conversion, the input stream
    * will be closed by this object. So consider the input stream as belonging
    * only to this object.
    *
    * @param   fileName       the file name as a URL.
    *
    * @param   lastModified   the modification date of the file, or 0L if not
    * known or undefined.
    */
   protected PSFile(InputStream stream, URL fileName, long lastModified)
   {
      if (stream == null){
         throw new IllegalArgumentException(
               "no input stream: PSFile/constructor");
      }

      IllegalArgumentException ex = validateUrl(fileName);
      if (ex != null)
         throw ex;

      PSMimeContentAdapter content = new PSMimeContentAdapter(
         stream, // content stream
         PSContentFactory.guessMimeType(new File(fileName.getFile())), // MIME type
         null, // no transfer encoding
         null, // no char encoding
         -1);  // length unknown


      try
      {
         m_url = new URL("file", "", fixPathSeparator(fileName.getFile()));
         content.setName(m_url.getFile());

         m_content = content;
         m_lastModified = lastModified;
      }
      catch(MalformedURLException e)
      {
         throw new IllegalArgumentException(
               "invalid file name: PSFile/constructor");
      }

   }

   /**
    * Construct a file object from MIME content. Ususally one does this in order
    * to save this content to the server.  All returned paths for this file will
    * be normalized. 
    *
    * @param   content MIME content whose stream is positioned at the start
    * of the data. If this object is to be converted to XML and/or serialized
    * in the form of a request, then this content must remain open and
    * unchanged until this conversion process is complete. After the conversion,
    * the content will be closed by this object. So consider the content as
    * belonging only to this object.
    *
    * @param   fileName       the file name.
    *
    * @param   lastModified   the modification date of the file, or 0L if not
    * known or undefined.
    */
   protected PSFile(IPSMimeContent content, URL fileName, long lastModified)
   {
      if (content == null){
         throw new IllegalArgumentException(
               "no input content: PSFile/constructor");
      }


      IllegalArgumentException ex = validateUrl(fileName);
      if (ex != null)
         throw ex;

      try
      {
         m_url = new URL("file", "", fixPathSeparator(fileName.getFile()));
         m_lastModified = lastModified;
         m_content = content;
      }
      catch(MalformedURLException e)
      {
         throw new IllegalArgumentException(
               "invalid file name: PSFile/constructor");
      }

   }

   /**
    * Validate a file's name.
    *
    * @param   fileName    the file name as a File
    */
   private static IllegalArgumentException validateFileName(File fileName)
   {
      if (fileName == null || fileName.getPath().trim().length() == 0){
         throw new IllegalArgumentException(
               "file name does not exist: validateFileName");
      }

      return null;
   }

   /**
    * Validate a URL.
    *
    * @param   url    the URL
    */
   private static IllegalArgumentException validateUrl(URL url)
   {
      if (url == null || url.getFile().trim().length() == 0){
         throw new IllegalArgumentException("url does not exist: " + url);
      }

      return null;
   }

   /**
    * Gets the last modification date of the underlying file.
    *
    * @return   long value representing the time the file was last modified,
    * measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
    * or 0L if the last modified time is not defined or not available. This time
    * will reflect the clock of the Java VM on the server if this file is the
    * result of a load file request.
    */
   public long getLastModified()
   {
      return m_lastModified;
   }

   /**
    * Gets the file name of this object, understood to be relative to the
    * extension on the server. The extension is not known to this object -- it
    * is supplied by other means when a request is made.
    *
    * @return   a java.io.File object
    */
   public File getFileName()
   {
      return new File(m_url.getFile());
   }

   /**
    * Gets the MIME content from the object and transfers to the caller the
    * responsibility for releasing the stream. This method can only be
    * called once because this object releases all references to the
    * content.
    *
    * @return   an IPSMimeContent object
    *
    * @exception   PSIllegalStateException if the input stream has already been
    * retrieved from this object (the ownership of the stream is transferred
    * to the first caller of this method). This is to avoid situations where
    * multiple objects are reading from the stream simultaneously.
    * This exception can also be thrown if the {@link #toXml(Document) toXml}
    * method has previously been called on this object.
    */
   public IPSMimeContent getContent() throws PSIllegalStateException
   {
      if (m_content == null){
         int errCode = IPSObjectStoreErrors.APP_FILE_STREAM_EXHAUSTED;
         throw new PSIllegalStateException(errCode);
      }

      IPSMimeContent ret = m_content;
      m_content = null;
      return ret;
   }
   
   /**
    * Gets the file path of this object.
    * 
    * @return a java.lang.String object
    */
   public String getPath()
   {
      return m_url.getPath();
   }

   // **************  IPSComponent Interface Implementation **************

   /**
    * Create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @param   doc   the document
    *
    * @return    the newly created XML element node
    */
   public abstract Element toXml(Document doc);

   /**
    * This method is called by subclasses' <code>toXml(Document)</code> method.
    *
    * @param   doc   the input XNL document
    * @param   root  the root of <code>doc</code>
    *
    * @return    the newly created XML element node
    */
   protected Element toXml(Document doc, Element root)
   {
      root.setAttribute(FILE_NAME, m_url.getFile());
      root.setAttribute(LAST_MOD, Long.toString(m_lastModified, ms_radix));

      String content = null;
      long bytesEnc = 0L;

      if (m_content != null)
      {
         InputStream in = null;
         try
         {
            ByteArrayOutputStream out;
            long len = m_content.getContentLength();
            if (len != -1L){
               out = new ByteArrayOutputStream((int)len);
            }
            else{
               out = new ByteArrayOutputStream(8192);
            }

            in = m_content.getContent();
            bytesEnc = PSBase64Encoder.encode(in, out);

            // base64 is US-ASCII representable
            content = out.toString("US-ASCII");
         }
         catch (IOException e)
         {
            Object[] args = new Object[] { m_url, e.getMessage() };
            throw new PSRuntimeException(IPSObjectStoreErrors.APP_FILE_IO_ERROR,
                  args);
         }
         finally
         {
            try{
               if (in != null)
                  in.close();
            } catch (IOException e) { /* ignore */ }
         }

         if (content == null){
            content = "";
         }

         Element contentEl = PSXmlDocumentBuilder.addElement(doc, root, CONTENT,
               content);

         contentEl.setAttribute(XFER_ENC, "base64");
         contentEl.setAttribute(LENGTH, Long.toString(bytesEnc, ms_radix));
         contentEl.setAttribute(MIME_TYPE, m_content.getMimeType());
         contentEl.setAttribute(CHAR_ENC, m_content.getCharEncoding());
         // TODO: set this when we support auto-decode of transfer encoding
         // contentEl.setAttribute("xferEnc", m_content.getTransferEncoding());

         // release all references to this puppy
         m_content = null;
      }

      return root;
   }

   /**
    * Populate an object from an XML element node. An element node may contain
    * a hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @param      sourceNode         the XML element node to construct this
    *                                object from, (may not be <code>null</code>)
    *
    * @param      parentDoc         the Java object which is the parent of this
    *                               object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception PSUnknownNodeTypeException  if the XML element node does not
    *                                        represent a type supported by the 
    *                                        class.
    */
   public abstract void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
      throws PSUnknownNodeTypeException;

   /**
    * This method is called by subclasses'
    * <code>fromXml(Element, IPSDocument, ArrayList)</code> method.
    *
    * @param      sourceNode         the XML element node to construct this
    *                                object from, (may not be <code>null</code>)
    *
    * @param      parentDoc         the Java object which is the parent of this
    *                               object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @param      nodeType          the element tag name representing a
    *                               subclass' object, such as
    *                               <PSXApplicationFile>
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not of
    *                                         the appropriate type
    *
    */
   protected void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents, String nodeType)
      throws PSUnknownNodeTypeException
   {
      String lastModified = null;
      try
      {
         // do the metadata
         if ((nodeType == null) || 
               (!nodeType.equals(sourceNode.getNodeName())))
         {
            Object[] args = { nodeType, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);

         m_url = new URL("file", "", getRequiredElementValue(walker,
                                       FILE_NAME, nodeType));

         lastModified = walker.getElementData(LAST_MOD);
         if (lastModified == null || lastModified.length() == 0)
         {
            lastModified = "0";
         }

         m_lastModified = Long.parseLong(lastModified, ms_radix);

         // do the content
         Element contentEl = walker.getNextElement(CONTENT);
         if (contentEl != null)
         {
            String xferEnc = null;
            // TODO: support base64 streams
            // String xferEnc = contentEl.getAttribute(XFER_ENC);
            // if (xferEnc == null || !(encoding.equals("base64")))
            // {
            //    Object[] args = { CONTENT, XFER_ENC, xferEnc };
            //    throw new PSUnknownNodeTypeException(
            //       IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            // }

            String mimeType = contentEl.getAttribute(MIME_TYPE);
            String charEnc  = contentEl.getAttribute(CHAR_ENC);
            long len = -1L;
            {
               String length   = contentEl.getAttribute(LENGTH);
               if (length != null && length.length() > 0)
                  len = Long.parseLong(length, ms_radix);
            }

            InputStream in = null;

            // TODO: this is extremely indirect...maybe use Piped streams ?
            String content = walker.getElementData(CONTENT);
            in = new ByteArrayInputStream(content.getBytes("US-ASCII"));
            ByteArrayOutputStream out = new ByteArrayOutputStream(
                  content.length());
            PSBase64Decoder.decode(in, out);
            in = new ByteArrayInputStream(out.toByteArray());

            PSMimeContentAdapter mimeContent = new PSMimeContentAdapter(
               in, mimeType, xferEnc, charEnc, len);

            mimeContent.setName(m_url.getFile());
            m_content = mimeContent;
         }
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(
            "Unrecoverable error in PSFile.fromXML(): " + e.toString());
      }
      catch (NumberFormatException e)
      {
         Object[] args = { nodeType, LAST_MOD, lastModified };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      catch (IOException e)
      {
         Object[] args = new Object[] { m_url, e.getMessage() };
         throw new PSRuntimeException(IPSObjectStoreErrors.APP_FILE_IO_ERROR,
               args);
      }

   }

   private static String getRequiredElementValue(PSXmlTreeWalker walker,
      String elementName, String nodeType) throws PSUnknownNodeTypeException
   {
      String val = walker.getElementData(elementName);
      if (val == null || val.length() == 0)
      {
         Object[] args = { nodeType, elementName, "" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      return val;
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * re-thrown).
    *
    * @param   cxt the validation context.
    *
    * @exception PSSystemValidationException depends on the implementation of the
    * validation context (on warnings and/or errors).
    */
   @Override
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateUrl(m_url);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());
   }

   /**
    * Determine whether the input object is the same as this object.
    *
    * @param   o   the input object
    *
    * @return   <CODE>true</CODE> if the given file object represents
    *          the same abstract filename
    */
   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSFile))
         return false;

      PSFile other = (PSFile)o;

      if (!compare(m_url, other.m_url))
         return false;

      return true;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .append(m_url)
            .toHashCode();
   }

   /**
    * Fixes up a filename's separators to be safe on Unix systems. If
    * the current system path separator is a back-slash then any back-
    * slashes found in the filename will be converted to forward-slashes.
    * If the current system path separator is a forward-slash, the filename
    * is returned without conversion.
    *
    * @param   fileName    the file name
    *
    * @return  the fixed up filename
    */
   private String fixPathSeparator(String fileName)
   {
      String newFile;

      if (!File.separator.equals("/"))
      {
         newFile = fileName.replace('\\', '/');
      }
      else
         newFile = fileName;


      return newFile;
   }



   /** The file name, relative to the file root as a URL.
       May be null if constructed with no arguments.  */
   URL m_url = null;

   /** The time (including date) the file was last modified. */
   protected long m_lastModified = -1L;

   /** The MIME content with data and type information. */
   protected IPSMimeContent m_content = null;

   private static final String XFER_ENC  = "xferEnc";
   private static final String CHAR_ENC  = "charEnc";
   private static final String FILE_NAME = "fileName";
   private static final String LENGTH    = "length";
   private static final String MIME_TYPE = "mimeType";
   private static final String CONTENT   = "content";
   private static final String LAST_MOD  = "lastModified";

   private static final int ms_radix = 16;
}

