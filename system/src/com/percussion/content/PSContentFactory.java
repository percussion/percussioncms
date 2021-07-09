/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.content;

import com.percussion.server.PSServer;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSProperties;
import com.percussion.util.PSPurgableFileInputStream;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * PSContentFactory provides a convenient set of methods to load
 * HTML, XML, XSL, and DTD files from disk in particular encodings
 * and automatically convert them to UTF-8.
 * <P>
 * Sometimes loading and converting requires us to make a distinct,
 * converted copy of the file in another location on disk. This is
 * the case, for example, with HTML files. Many HTML files do not
 * conform to the standard, so we try to fix them and update the
 * contents of the &lt;META HTTP-EQUIV="Content-Type" ...&gt; tag
 * to reflect the new UTF-8 encoding.
 * <P>
 * A similar situation exists with XML, XSL, or DTD files that
 * have a &lt;?xml ... encoding='whatever' ?&gt; header in them.
 * This is updated in the corrected version of the file to reflect
 * the new UTF-8 encoding.
 * <P>
 * We use UTF-8 because it is basically guaranteed that any character
 * from any language is representable in UTF-8 (or soon will be). This
 * guarantee cannot be made for any non-Unicode encodings.
 *
 */
public abstract class PSContentFactory
{
   /**
    * Logger to use.
    */
   private static final Logger log = LogManager.getLogger(PSContentFactory.class);
   
   /**
    * Interactive test. Command line arguments are filenames, which will
    * be loaded by the content factory and processed accordingly.
    */
   public static void main(String[] args)
   {
      try
      {
         for (int i = 0; i < args.length; i++)
         {
            File f = new File(args[i]);
            log.info("{} :", f.toString());
            IPSMimeContent cnt = loadFile(f);
            log.info(cnt);
            log.info("------------------------------------------------");
            writeContent(cnt);
            log.info("------------------------------------------------");
         }
      }
      catch (Throwable t)
      {
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
      }
   }

   /**
    * TESTING METHOD. Writes the content to System.out
    */
   private static void writeContent(IPSMimeContent cnt)
      throws IOException
   {
      String cs = PSCharSets.getJavaName(cnt.getCharEncoding());
      InputStream in = cnt.getContent();
      writeCharStream(in, cs);
   }

   /**
    * TESTING METHOD. Writes the content to System.out
    */
   private static void writeCharStream(InputStream in, String enc)
      throws IOException
   {
      BufferedReader r = new BufferedReader(new InputStreamReader(in, enc));
      String line = r.readLine();
      while (line != null)
      {
         log.info(line);
         line = r.readLine();
      }
   }

   /**
    * Returns a buffered stream around the given stream, unless the
    * given stream is already buffered.
    *
    * @param in The stream to be buffered. Must not be
    * <CODE>null</CODE>.
    *
    * @return A buffered stream, possibly the original stream that
    * was passed in. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    */
   @SuppressWarnings("deprecation")
   public static InputStream bufferStream(InputStream in)
      throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in cannot be null");

      if (in instanceof BufferedInputStream || in instanceof ByteArrayInputStream
         || in instanceof java.io.StringBufferInputStream)
         return in;
      else
         return new BufferedInputStream(in);
   }

   /**
    * Returns a buffered reader around the given reader, unless the
    * given reader is already buffered.
    *
    * @param in The reader to be buffered. Must not be <CODE>null</CODE>.
    *
    * @return A buffered reader, possibly the original reader that
    * was passed in. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    */
   public static Reader bufferReader(Reader in)
      throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in cannot be null");

      if (in instanceof BufferedReader || in instanceof java.io.StringReader
         || in instanceof java.io.CharArrayReader )
         return in;
      else
         return new BufferedReader(in);
   }

   /**
    * Returns a reader around a buffered version of the given stream,
    * unless the given stream is already buffered.
    *
    * @param in The stream to be buffered. Must not be <CODE>null</CODE>.
    * @param enc The character encoding to use. Must not be
    * <CODE>null</CODE>.
    *
    * @return A reader around a buffered stream. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    */
   public static Reader bufferReader(InputStream in, String enc)
      throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in cannot be null");

      if (enc == null)
         throw new IllegalArgumentException("enc cannot be null");

      in = bufferStream(in);
      return new InputStreamReader(in, enc);
   }

   /**
    * Returns a buffered stream around the given output stream, unless
    * the stream is already buffered, in which case the stream itself
    * will be returned.
    *
    * @param out The output stream to be buffered. Must not be
    * <CODE>null</CODE>.
    *
    * @return The buffered output stream, possibly the same stream
    * that was passed in. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    */
   public static OutputStream bufferStream(OutputStream out)
      throws IOException
   {
      if (out == null)
         throw new IllegalArgumentException("out cannot be null");

      if (out instanceof BufferedOutputStream
         || out instanceof java.io.ByteArrayOutputStream)
         return out;
      else
         return new BufferedOutputStream(out);
   }

   /**
    * Returns a buffered writer around the given writer, unless
    * the writer is already buffered, in which case the writer itself
    * will be returned.
    *
    * @param out The writer to be buffered. Must not be
    * <CODE>null</CODE>.
    *
    * @return The buffered writer, possibly the same writer
    * that was passed in. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    */
   public static Writer bufferWriter(Writer out)
      throws IOException
   {
      if (out == null)
         throw new IllegalArgumentException("out cannot be null");

      if (out instanceof java.io.BufferedWriter || out instanceof java.io.StringWriter
         || out instanceof java.io.CharArrayWriter)
         return out;
      else
         return new java.io.BufferedWriter(out);
   }

   /**
    * Returns a writer around a buffered version of the given output
    * stream using the given character encoding. If the stream is
    * already buffered, it will not be buffered again.
    *
    * @param out The writer to be buffered. Must not be
    * <CODE>null</CODE>.
    *
    * @return The buffered writer, possibly the same writer
    * that was passed in. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    */
   public static Writer bufferedWriter(OutputStream out, String enc)
      throws IOException
   {
      if (out == null)
         throw new IllegalArgumentException("out cannot be null");

      if (enc == null)
         throw new IllegalArgumentException("enc cannot be null");

      out = bufferStream(out);
      return new OutputStreamWriter(out, enc);
   }

   /**
    * Tries to guess the MIME type based on the file extension.
    *
    * @author   chad loder
    *
    * @version 1.0 1999/11/12
    *
    * @param filename A filename with an extension. Must not be
    * <CODE>null</CODE>.
    *
    * @return   String The guessed MIME type. Will return
    * "application/octet-stream" if it cannot guess. Never <CODE>null</CODE>.
    */
   public static String guessMimeType(File filename)
   {
      if (filename == null)
         throw new IllegalArgumentException("filename cannot be null");

      return guessMimeType(filename, ms_defaultMimeType);
   }

   /**
    * Tries to guess the MIME type based on the file extension.
    *
    * @author   chad loder
    *
    * @version 1.0 1999/11/12
    *
    * @param filename A filename with an extension. Must not be
    * <CODE>null</CODE>.
    *
    * @param defaultType The default type if we can't guess. Can be
    * <CODE>null</CODE> in which case <CODE>null</CODE> will be
    * returned if we can't guess.
    *
    * @return   String The guessed MIME type. Will return
    * <CODE>defaultType</CODE> if it cannot guess. If defaultType is
    * <CODE>null</CODE>, will return <CODE>null</CODE> if it cannot guess.
    */
   public static String guessMimeType(File filename, String defaultType)
   {
      if (filename == null)
         throw new IllegalArgumentException("filename cannot be null");

      String ret = null;
      String ext = filename.getName();
      int dotPos = ext.lastIndexOf('.');
      if (dotPos > 0 && dotPos < (ext.length() - 1))
      {
         ext = ext.substring(dotPos + 1);
         ret = (String)ms_mimeTypes.get(ext.toLowerCase());
      }

      if (ret == null)
         ret = defaultType;

      return ret;
   }
   
   /**
    * Returns the mimetype associated with the supplied file extension.
    * 
    * @param ext The file extension, if <code>null</code> or empty returns
    * <code>null</code> for mimetype. Leading dots are stripped.
    * @return mimetype may be <code>null</code>. If the extension is not part
    * of the mimemap.properties file.
    */
   public static String guessMimeType(String ext)
   {
      if(StringUtils.isBlank(ext))
         return null;
      if(ext.startsWith("."))
         ext = ext.substring(1);
      return (String)ms_mimeTypes.get(ext.toLowerCase());
   }
   
   /**
    * Returns the list of supported mime types in ascending order.
    * @return String array of supported mime types
    */
   public static String[] getSupportedMimeTypes()
   {
      Set<String> temp = new HashSet<>();
      Iterator iter = ms_mimeTypes.keySet().iterator();
      while(iter.hasNext())
      {
         String mt = (String) iter.next();
         temp.add(ms_mimeTypes.getProperty(mt));
      }
      List<String> mtypes = new ArrayList<>(temp);
      Collections.sort(mtypes);
      return mtypes.toArray(new String[mtypes.size()]);
   }
   
   /**
    * <B>Not implemented</B>: Returns an input stream that will properly
    * decode the transfer encoding of the MIME content. If there is no
    * transfer encoding (if the encoding is raw binary), it will return
    * the a stream to the raw data, untransformed.
    *
    * @param mime The MIME content to be decoded. Must not be <CODE>null</CODE>.
    *
    * @return An input stream which presents a decoded version of the
    * content. Never <CODE>null</CODE>.
    *
    * @throws IOException If the content transfer encoding specified in
    * the MIME content is not supported.
    *
    * @todo Implement at least base64 decoding using filter streams
    */
   public static InputStream getDecodedInput(IPSMimeContent mime)
      throws IOException
   {
      if (mime == null)
         throw new IllegalArgumentException("mime cannot be null");

      if (mime.getTransferEncoding() != null)
      {
         throw new IOException(mime.getTransferEncoding()
            + " content transfer decoding not supported");
      }
      return mime.getContent();
   }

   /**
    * Gets a reader for the given content. If the content does not
    * represent character data, an IllegalArgumentException will
    * be thrown.
    *
    * @param mime The content to be read. The character encoding
    * specified in this content will be used to construct the
    * reader. Must not be <CODE>null</CODE>.
    *
    * @return A reader that properly converts the content bytes
    * into characters. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    */
   public static Reader getReader(IPSMimeContent mime)
      throws IOException
   {
      if (mime == null)
         throw new IllegalArgumentException("mime cannot be null");

      return new InputStreamReader(getDecodedInput(mime), mime.getCharEncoding());
   }

   /**
    * Parses an XML document from the given content.
    *
    * @param mime The content representing an XML document. Must not be
    * <CODE>null</CODE>.
    *
    * @return A parsed XML document. Never <CODE>null</CODE>.
    *
    * @throws IOException If an error occurred related to the content
    * stream.
    *
    * @throws SAXException If a parse error occurred.
    *
    */
   public static Document getDocument(IPSMimeContent mime)
      throws IOException, SAXException
   {
      return PSXmlDocumentBuilder.createXmlDocument(getReader(mime), false);
   }

   /**
    * Loads the given file from disk. If the file extension reflects
    * an HTML or XML type that we understand, it corrects the file and
    * puts it in UTF-8 format, and returns the corrected version
    * (which may or may not point to another, distinct, disk file).
    * The contents of the original file will not be modified in
    * any way.
    * <P>
    * If the file extension reflects any other type, we try to guess
    * the MIME type based on common file extensions, but we do no
    * processing to the file's contents.
    * <P>
    * It is the caller's responsibility to close the returned
    * content stream when he is finished.
    *
    * @param f The filename. Must not be <CODE>null</CODE>.
    *
    * @return   IPSMimeContent The correctly typed MIME input. Never
    * <CODE>null</CODE>.
    *
    * @throws   IOException If the type of file cannot be determined,
    * if the given file cannot be found, or any other kind of I/O
    * error occurred.
    */
   public static IPSMimeContent loadFile(File f)
      throws IOException
   {
      if (f == null)
      {
         throw new IllegalArgumentException("File cannot be null");
      }

      IPSMimeContent ret = null;
      String name = f.getName().toLowerCase();
      int dotPos = name.lastIndexOf(".");
      if (dotPos >= 0 && (dotPos <= name.length() - 1))
      {
         String ext = name.substring(dotPos + 1);
         if (ext.equals("xml"))
            ret = loadXmlFile(f);
         else if (ext.equals("xsl"))
            ret = loadXslFile(f);
         else if (ext.equals("dtd"))
            ret = loadDtdFile(f);
         else if (ext.equals("html"))
            ret = loadHtmlFile(f);
         else if (ext.equals("htm"))
            ret = loadHtmlFile(f);
      }

      if (ret == null)
         throw new IOException("Cannot determine type of file " + f.toString());

      return ret;
   }

   /**
    * Loads the given XML file from disk, corrects it and puts it in
    * UTF-8 format, and returns the corrected version (which may or
    * may not point to another, distinct, disk file). The contents
    * of the original file will not be modified in any way.
    * <P>
    * It is the caller's responsibility to close the returned
    * content stream when he is finished.
    *
    * @param f The filename. Must not be <CODE>null</CODE>.
    *
    * @return   IPSMimeContent The (possibly corrected) content.
    * Never <CODE>null</CODE>.
    *
    * @throws   IOException If an I/O error occurs.
    */
   public static IPSMimeContent loadXmlFile(File f)
      throws IOException
   {
      if (f == null)
         throw new IllegalArgumentException("f cannot be null");

      PSPurgableTempFile corrected = loadXmlFile(f,
         PSCharSets.getLocalJavaName(), "dtd");

      InputStream in = new BufferedInputStream(
         new PSPurgableFileInputStream(corrected));

      return new PSMimeContentAdapter(in,
         IPSMimeContentTypes.MIME_TYPE_TEXT_XML, null,
         PSCharSets.rxStdEnc(), corrected.length());
   }

   /**
    * Loads the given XSL file from disk, corrects it and puts it in
    * UTF-8 format, and returns the corrected version (which may or
    * may not point to another, distinct, disk file). The contents
    * of the original file will not be modified in any way.
    * <P>
    * It is the caller's responsibility to close the returned
    * content stream when he is finished.
    *
    * @param f The filename. Must not be <CODE>null</CODE>.
    *
    * @return   IPSMimeContent The (possibly corrected) content.
    * Never <CODE>null</CODE>.
    *
    * @throws   IOException If an I/O error occurs.
    */
   public static IPSMimeContent loadDtdFile(File f)
      throws IOException
   {
      if (f == null)
         throw new IllegalArgumentException("f cannot be null");

      PSPurgableTempFile corrected = loadXmlFile(f,
         PSCharSets.getLocalJavaName(), "dtd");

      InputStream in = new BufferedInputStream(
         new PSPurgableFileInputStream(corrected));

      return new PSMimeContentAdapter(in,
         IPSMimeContentTypes.MIME_TYPE_APPLICATION_DTD, null,
         PSCharSets.rxStdEnc(), corrected.length());
   }

   /**
    * Loads the given XSL file from disk, corrects it and puts it in
    * UTF-8 format, and returns the corrected version (which may or
    * may not point to another, distinct, disk file). The contents
    * of the original file will not be modified in any way.
    * <P>
    * It is the caller's responsibility to close the returned
    * content stream when he is finished.
    *
    * @param f The filename. Must not be <CODE>null</CODE>.
    *
    * @return   IPSMimeContent The (possibly corrected) content.
    * Never <CODE>null</CODE>.
    *
    * @throws   IOException If an I/O error occurs.
    */
   public static IPSMimeContent loadXslFile(File f)
      throws IOException
   {
      if (f == null)
         throw new IllegalArgumentException("f cannot be null");

      PSPurgableTempFile corrected =
         loadXmlFile(f, PSCharSets.getLocalJavaName(), "xsl");

      InputStream in = new BufferedInputStream(
         new PSPurgableFileInputStream(corrected));

      return new PSMimeContentAdapter(in,
         IPSMimeContentTypes.MIME_TYPE_APPLICATION_XSL, null,
         PSCharSets.rxStdEnc(), corrected.length());
   }

   /**
    * Loads an XML, XSL, or DTD file from disk, corrects it, and returns
    * the purgable temp file which contains the corrected version.
    *
    * @param f The original filename. Must not be null.
    *
    * @param initialEnc The initial encoding to use when reading the file.
    * Must not be <CODE>null</CODE>.
    *
    * @param fileType The filetype, "XSL", "DTD", or "XSL" to use as the
    * extension when creating the corrected file. Must not be
    * <CODE>null</CODE>, and must reflect the actual type of the file.
    *
    * @return The purgable temp file of the corrected version. Never
    *    <code>null</code>. It is the caller's responsibility to delete the
    *    temp file.
    *
    * @throws IOException If file is too large, empty, unreadable or any I/O
    *    failures.
    */
   public static PSPurgableTempFile loadXmlFile(File f, String initialEnc,
      String fileType)
      throws IOException
   {
      if (f == null)
         throw new IllegalArgumentException("f cannot be null");
      if (initialEnc == null)
         throw new IllegalArgumentException("initialEnc cannot be null");
      if (fileType == null)
         throw new IllegalArgumentException("fileType cannot be null");

      if (!f.canRead())
      {
         throw new IOException("Cannot read " + fileType + " file "
               + f.toString());
      }

      // make sure the length of the file is big enough to fit into a
      // byte array (it would have to be > 4GB or so!!!)
      long fLen = f.length();
      if (fLen > Integer.MAX_VALUE)
      {
         // TODO: use error codes for internationalization
         throw new IOException(fileType + " file too large ("
               + fLen + " bytes): " + f.toString());
      }
      else if ( fLen == 0 )
         throw new IOException( "File is empty" );

      // we do not buffer the input stream because we are reading it
      // using the largest chunks possible, instead of one char at a time
      // so in-memory buffering would actually slow us down
      FileInputStream fIn = new FileInputStream(f);
      ByteArrayInputStream bIn = null;
      try
      {
         bIn = readIntoMem(fIn, (int)fLen);
      }
      finally
      {
         try
         { fIn.close(); }
         catch (IOException e)
         { /* ignore */ }
      }

      PSPurgableTempFile corrected = correctXmlFile(bIn, initialEnc, "xml");
      return corrected;
   }

   /**
    * Corrects an XML, DTD, or XSL file, puts it in the correct encoding,
    * then returns the purgable temp file which contains the corrected version.
    *
    * @param in The in-memory version of the document. Must not be
    * <CODE>null</CODE>.
    *
    * @param encoding The initial character encoding to use when reading the
    * document. This will be superceded by any encoding specified in the
    * document itself. Must not be <CODE>null</CODE>.
    *
    * @param fileType The filetype (must be one of "XML", "DTD", or "XSL",
    * and must represent the type of the file. Must not be <CODE>null</CODE>.
    *
    * @return The purgable temp file of the corrected version.
    *    Never <code>null</code>. It is the caller's responsibility to delete
    *    the temp file.
    *
    * @throws IOException If an IO error occurred.
    */
   private static PSPurgableTempFile correctXmlFile(
      ByteArrayInputStream in,
      String encoding,
      String fileType
      )
      throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in cannot be null");
      if (encoding == null)
         throw new IllegalArgumentException("encoding cannot be null");
      if (fileType == null)
         throw new IllegalArgumentException("fileType cannot be null");

      boolean read = true;
      String line = null;
      BufferedReader rdr = null;

      while (read)
      {
         in.reset();

         rdr = new BufferedReader(
            new InputStreamReader(in, PSCharSets.getJavaName(encoding)));

         line = rdr.readLine();

         // suck up all blank lines
         while (line != null)
         {
            line = line.trim();
            if (line.length() == 0)
            {
               line = rdr.readLine();
            }
            else
            {
               break;
            }
         }

         if (line == null)
         {
            throw new IOException(fileType + " file is empty.");
         }

         read = false;

         String xmlEnc = extractXmlEncoding(line);
         if (xmlEnc != null &&
            !PSCharSets.getStdName(xmlEnc).equals(PSCharSets.getStdName(encoding)))
         {
            encoding = xmlEnc;
            read = true;
         }
      }

      // write the corrected document to the XML file, along with the
      // rest of the chars from the reader
      PSPurgableTempFile corr =
         new PSPurgableTempFile("ps_", "." + fileType, null);
      Writer out = new OutputStreamWriter(
         new BufferedOutputStream(new FileOutputStream(corr)),
         PSCharSets.rxJavaEnc());

      try
      {
         out.write("<?xml version='1.0' encoding='");
         out.write(PSCharSets.rxStdEnc());
         out.write("'?>");
         out.write(System.getProperty("line.separator"));

         if (!line.startsWith("<?xml"))
         {
            out.write(line);
            out.write(System.getProperty("line.separator"));
         }
         //even if it is started with that, we might have some more characters
         //appended after the header, so we have to get them and append.
         //The possible case is
         //<?xml version='1.0' encoding='UTF-8' ?> <!DOCTYPE PSXContentEditor
         else
         {
            int index = line.indexOf(">");
            if(index != -1)
            out.write( line.substring(index+1) );
         }

         dumpReader(rdr, out);
      }
      finally
      {
         try
         { out.flush(); out.close(); }
         catch (IOException e)
         { /* ignore */ }
      }

      return corr;
   }

   /**
    * Extracts the XML character encoding from a line that looks like
    * <CODE>&lt;?xml ... encoding='xxx'?&gt;</CODE> or
    * <CODE>&lt;?xml ... encoding="xxx"?&gt;</CODE>
    *
    * @param line The XML header line. Must not be <CODE>null</CODE>.
    *
    * @return The encoding specified in the line, or <CODE>null</CODE>
    * if it could not be found.
    */
   private static String extractXmlEncoding(String line)
   {
      if (line == null)
         throw new IllegalArgumentException("line cannot be null");

      String ret = null;
      if (line.startsWith("<?xml"))
      {
         final String param = "encoding";
         int encIdx = line.indexOf(param);
         if (encIdx >= 0 && encIdx < (line.length() - 1))
         {
            String sub = line.substring(
               encIdx + param.length());

            sub = sub.trim();
            if (sub.startsWith("="))
               sub = sub.substring(1);

            sub = sub.trim();
            char quoteChar = 0;
            if (sub.startsWith("\'"))
               quoteChar = '\'';
            else if (sub.startsWith("\""))
               quoteChar = '"';

            if (quoteChar != 0)
            {
               sub = sub.substring(1);
               sub = sub.trim();
               int quoteIdx = sub.indexOf(quoteChar);
               if (quoteIdx >= 0)
               {
                  sub = sub.substring(0, quoteIdx);
                  sub = sub.trim();

                  // now, see if we need to reparse
                  sub = PSCharSets.getStdName(sub);
                  ret = sub;
               }
            }
         }
      }

      return ret;
   }

   /**
    * Save the given content to the given disk file, overwriting
    * any existing file by that name. The content stream will be
    * closed after reading.
    *
    * @param fileName The named file to be saved. Must not be
    * <CODE>null</CODE>.
    *
    * @param content The content of the file. Must not be
    * <CODE>null</CODE>.
    *
    * @throws IOException If an IO error occurred.
    */
   public static void saveFile(File fileName, IPSMimeContent content)
      throws IOException
   {
      if (fileName == null)
         throw new IllegalArgumentException("fileName cannot be null");
      if (content == null)
         throw new IllegalArgumentException("content cannot be null");

      InputStream in = bufferStream(content.getContent());
      try
      {
         OutputStream out = bufferStream(new FileOutputStream(fileName));
         try
         {
            copyStream(in, out, 8192);
         }
         finally
         {
            out.close();
         }
      }
      finally
      {
         in.close();
      }
   }

   /**
    * Copies all of the bytes from the InputStream to the
    * OutputStream. Neither stream will be closed.
    *
    * @author   chad loder
    *
    * @param in The input stream from which all bytes will be read. Must
    * not be <CODE>null</CODE>.
    *
    * @param out The output stream to which all bytes will be written. Must
    * not be <CODE>null</CODE>.
    *
    * @param   bufSize The read/write increment buffer size to use. Must
    * be greater than 0.
    *
    * @return long The number of bytes written. Never < 0.
    *
    * @throws IOException If an IO error occurred.
    */
   public static long copyStream(InputStream in, OutputStream out, int bufSize)
      throws IOException
   {
      if (bufSize <= 0)
         throw new IllegalArgumentException("bufSize cannot equal " + bufSize);
      if (in == null)
         throw new IllegalArgumentException("input stream cannot be null");
      if (out == null)
         throw new IllegalArgumentException("output stream cannot be null");

      byte[] buf = new byte[bufSize];
      long bytesWritten = 0L;
      while (true)
      {
         int read = in.read(buf);

         if (read < 0)
            break; // end of input stream reached

         out.write(buf, 0, read);

         bytesWritten += read;
      }

      out.flush();
      return bytesWritten;
   }

   /**
    * Loads the given disk file, without translating using UTF-8
    * format, since static files are not converted to UTF-8 when stored.
    * This file may or may not point to another, distinct, disk file.
    * The contents of the original file will not be modified in any way.
    *
    * @param f The filename. Must not be <CODE>null</CODE>.
    *
    * @return The (possibly corrected) content. Never <CODE>null</CODE>.
    *
    * @throws IOException If an IO error occurred.
    *
    * @throws HTMLException If a parse error occurred.
    */
   public static IPSMimeContent loadHtmlFile(File f)
      throws IOException, HTMLException
   {
      if (f == null)
         throw new IllegalArgumentException("f cannot be null");

      if (!f.canRead())
      {
         // TODO: error use codes for internationalization
         throw new IOException("Cannot read HTML file " + f.toString());
      }

      // make sure the length of the file is big enough to fit into a
      // byte array (it would have to be > 4GB or so!!!)
      long fLen = f.length();
      if (fLen > Integer.MAX_VALUE)
      {
         // TODO: use error codes for internationalization
         throw new IOException("HTML file too large ("
            + fLen + " bytes): " + f.toString());
      }

      InputStream in = new BufferedInputStream(
         new FileInputStream(f));

      // JS Fix bug Rx-00-09-0037 - pass null encoding type
      return new PSMimeContentAdapter(in,
         IPSMimeContentTypes.MIME_TYPE_TEXT_HTML, null,
         null, f.length());
   }


   /**
    * Parses and corrects the HTML file, returning the filename of a
    * temporary file which contains a corrected, UTF-8 version of the file.
    * The algorithm follows this flow:
    *
    * <DIV ALIGN="CENTER">
    * <IMG SRC="doc-files/PSContentFactory-1.gif">
    * </DIV>
    *
    * @param in The in-memory version of the document. Must not be
    * <CODE>null</CODE>.
    *
    * @param encoding The initial character encoding to use when reading the
    * document. This will be superceded by any encoding specified in the
    * document itself. Must not be <CODE>null</CODE>.
    *
    * @return The filename of the corrected version. Never <CODE>null</CODE>.
    * This file is safe to delete (use
    * {@link java.io.File#deleteOnExit deleteOnExit} ), but
    * it is the caller's responsibility to delete it.
    *
    * @throws IOException If an IO error occurred.
    *
    * @throws HTMLException If a parse error occurred.
    */
   /*
   private static File correctHtmlFile(ByteArrayInputStream in, String encoding)
      throws IOException, HTMLException
   {
      if (in == null)
         throw new IllegalArgumentException("in cannot be null");

      if (encoding == null)
         throw new IllegalArgumentException("encoding cannot be null");

      boolean parse = true;
      HTMLDocumentFragment doc = null;
      HTMLElement stop = null;
      Reader rdr = null;

      while (parse)
      {
         doc = null;
         stop = null;

         in.reset();
         rdr = new InputStreamReader(in, PSCharSets.getJavaName(encoding));

         // by default, the parser automatically converts tags and
         // attribute names to uppercase, so it is safe and necessary
         // for us to do all comparisons and attribute-gets in uppercase
         PSHtmlParser p = createPartialParser();
         p.parse(rdr);
         parse = false;

         doc = p.getDocFragment();
         stop = p.getStoppedElement();
         if (stop == null)
         {
            throw new IOException("Could not parse HTML document");
         }

         if (doc == null)
         {
            throw new IOException("Could not parse HTML document");
         }

         // get the charset according to the document itself. If this
         // is set to something other than the initial encoding, then we
         // will re-parse the document up to this point because we may
         // already have misinterpreted some chars
         if (stop.getTagName().equals("META"))
         {
            String contentType = stop.getAttribute("CONTENT");
            HashMap contentParams = new HashMap();
            try
            {
               String mediaType =
                  PSBaseHttpUtils.parseContentType(contentType, contentParams);

               String charset = (String)contentParams.get("charset");
               if (charset != null)
               {
                  charset = PSCharSets.getStdName(charset);
                  if (!charset.equals(PSCharSets.getStdName(encoding)))
                  {
                     encoding = charset;
                     parse = true; // set up for re-parse
                  }
               }
            }
            catch (IllegalArgumentException e)
            {
               // malformed content type -- ignore and let us write it out
               // ourselves
            }
         }
      }

      // correct the HTML document
      HTMLElement meta = correctHtmlDoc(doc, stop);

      // write the corrected document to the HTML file, along with the
      // rest of the chars from the reader
      File corr = File.createTempFile("ps_", ".html");

      // DBG>
      // System.out.println("Writing to " + corr.toString());
      // <DBG

      OutputStream out = new BufferedOutputStream(
         new FileOutputStream(corr));

      try
      {
         joinHtml(out, rdr, doc.getFirstElementChild(), stop);
      }
      finally
      {
         try
         { out.flush(); out.close(); }
         catch (IOException e)
         { / * ignore * / }
      }

      return corr;
   }
   */

   /**
    * Seamlessly joins a parsed partial tree with the rest of the unparsed
    * characters and writes the output in UTF-8 format to the given output
    * stream.
    * <P>
    * This is somewhat complicated, due to a chicken-and-egg problem with
    * HTML file encoding. You have to read the HTML file in order to determine
    * its encoding, but you cannot read anything correctly without knowing
    * the encoding. So what we do is a partial parse up to the encoding
    * specification (tentatively using one encoding), then if we guessed
    * the encoding incorrectly, we close and re-read the file in that
    * encoding. If we guessed correctly, then we don't bother re-reading,
    * we simply prettyprint our partial parse tree up until where we stopped
    * parsing, then print the rest of the unparsed characters with no
    * modification. This requires a very exact behavior on the parser's
    * part -- every character it reads must go into the parse tree
    * immediately. It cannot buffer ahead, otherwise the buffered characters
    * would get lost in the resulting version (because they would not be
    * in the parse tree nor would they be in the unparsed output). Our
    * HTML parser is designed to meet these requirements.
    *
    * @param out The output stream to which the joined HTML will be
    * written. Must not be <CODE>null</CODE>.
    *
    * @param remainder The remainder of the unparsed output. Must not
    * be <CODE>null</CODE>.
    *
    * @param start The start of our pretty printing. To recreate the input
    * document exactly, this should be the root of the parse tree, but
    * it does not have to be the root. This node and all its children
    * up to and including <CODE>stop</CODE> will be written, but none of
    * this node's siblings will be printed. Must not be <CODE>null</CODE>.
    *
    * @param stop The stop of our pretty printing. This must be a child of
    * <CODE>start</CODE>, otherwise results are undefined. This
    * will be the last node written. Must not be <CODE>null</CODE>.
    *
    * @throws IOException If an I/O error occurs.
    *
    * @see PSHtmlParser#writeHtmlTree
    */
   /*
   private static void joinHtml(
      OutputStream out,
      Reader remainder,
      HTMLNode start,
      HTMLNode stop
      )
      throws IOException
   {
      if (out == null)
         throw new IllegalArgumentException("out cannot be null");
      if (remainder == null)
         throw new IllegalArgumentException("remainder cannot be null");
      if (start == null)
         throw new IllegalArgumentException("start cannot be null");
      if (stop == null)
         throw new IllegalArgumentException("stop cannot be null");

      // write the partial tree
      OutputStreamWriter w = new OutputStreamWriter(out, PSCharSets.rxJavaEnc());
      PSHtmlParser.writeHtmlTree(start, stop, w);
      dumpReader(remainder, w);
      w.flush();
   }
   */

   /**
    * Dumps all characters from the given reader to the given
    * writer.
    *
    * @param  in The reader to read from. Must not be <CODE>null</CODE>.
    * @param  out The writer to write to. Must not be <CODE>null</CODE>.
    *
    * @return The number of characters read/written. Always >= 0.
    *
    * @throws IOException If an I/O error occurs.
    *
    */
   private static long dumpReader(Reader in, Writer out)
      throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in cannot be null");
      if (out == null)
         throw new IllegalArgumentException("out cannot be null");

      // write all of the chars from the reader
      char[] buf = new char[8192];
      long numChars = 0L;
      while (true)
      {
         int charsRead = in.read(buf);
         if (charsRead == -1)
            break;
         numChars += charsRead;
         out.write(buf, 0, charsRead);
      }

      out.flush();
      return numChars;
   }


   /**
    * Correct the structure of an HTML document so that it has an HTML element,
    * a HEAD element, and a META HTTP-EQUIV="Content-Type" within the HEAD that
    * reflects UTF-8 encoding. If the document is already correct, it will not
    * be changed.
    *
    * @param doc The document fragment which may represent a partial parse.
    * Must not be <CODE>null</CODE>.
    *
    * @param lastEl the stopped element from the parser, which should not have
    * advanced any further than the first of
    * (<CODE>&lt;META HTTP-EQUIV="Content-type"&gt;, &lt;BODY>, or &lt;FRAMESET&gt;
    * </CODE>). Must not be <CODE>null</CODE>.
    *
    * @return The <CODE>META</CODE> tag that was added to (or already present in)
    * the HEAD. Never <CODE>null</CODE>.
    *
    * @throws HTMLException If a parse error occurs.
    */
   /*
   private static HTMLElement correctHtmlDoc(HTMLDocumentFragment doc, HTMLElement lastEl)
      throws HTMLException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc cannot be null");
      if (lastEl == null)
         throw new IllegalArgumentException("lastEl cannot be null");

      HTMLElement meta = lastEl;
      if (meta.getTagName().equals("META"))
      {
         Attr attr = meta.getAttributeNode("CONTENT");
         attr.setValue(IPSMimeContentTypes.MIME_TYPE_TEXT_HTML
            + "; charset=" + PSCharSets.rxStdEnc());
      }
      else // we did not find the tag itself, so we will add it
      {
         HTMLElement html = (HTMLElement)doc.getFirstElementChild();

         // get the HTML element, adding it if it doesn't exist
         if (!html.getNodeName().equals("HTML"))
         {
            HTMLElement newElement = new HTMLElement("HTML");
            newElement.appendChild(new HTMLText(System.getProperty("line.separator")));
            doc.insertBefore(newElement, html);
            newElement.appendChild(html);
            html = newElement;
         }

         // get the HEAD element from the HTML, adding it if it doesn't exist
         HTMLElement head = html.getFirstElementChild();
         if (head == null)
         {
            head = new HTMLElement("HEAD");
            head.appendChild(new HTMLText(System.getProperty("line.separator")));
            html.appendChild(html);
         }
         else if (!head.getNodeName().equals("HEAD"))
         {
            // we need to add the HEAD ourselves

            // Check if this is something that belongs in the head.
            // TODO: instead of complaining, we should pull all the HEAD-belonging
            // elements up into the HEAD which we create.
            // That will be a little complicated because
            // we will have to re-stitch the prev/next-sibling and parent
            // relationships
            if (
               head.getNodeName().equals("TITLE")
               || head.getNodeName().equals("BASE")
               || head.getNodeName().equals("META")
               || head.getNodeName().equals("ISINDEX")
               || head.getNodeName().equals("LINK")
               || head.getNodeName().equals("STYLE")
               )
            {
               throw new HTMLException((short)0, "The " + head.getNodeName()
                  + " element belongs in the HEAD element, which was not found.");
            }

            // create a new HEAD element
            HTMLElement newHead = new HTMLElement("HEAD");
            newHead.appendChild(new HTMLText(System.getProperty("line.separator")));
            html.insertBefore(newHead, head);
            head = newHead;
         }

         // add the META element to the HEAD
         meta = new HTMLElement("META");
         meta.appendChild(new HTMLText(System.getProperty("line.separator")));
         meta.setAttribute("HTTP-EQUIV", "Content-Type");
         meta.setAttribute("CONTENT", IPSMimeContentTypes.MIME_TYPE_TEXT_HTML
            + "; charset=" + PSCharSets.rxStdEnc());
         HTMLElement firstHeadEl = head.getFirstElementChild();
         if (firstHeadEl == null)
         {
            head.appendChild(meta);
         }
         else
         {
            head.insertBefore(meta, firstHeadEl);
         }
      }

      return meta;
   }
   */

   /**
   * Creates a parser that will stop at the end of the
   * <CODE>&lt;HEAD&gt;</CODE>, the beginning of the
   * <CODE>&lt;BODY&gt;</CODE>, or the
   * <CODE>&lt;META HTTP-EQUIV="Content-Type"&gt;</CODE> tag/attribute,
   * whichever comes first.
   *
   * @return A partial parser. Never <CODE>null</CODE>.
   */
   /*
   private static PSHtmlParser createPartialParser()
   {
      PSHtmlParser p = new PSHtmlParser();
      HTMLElement contentSearch = new HTMLElement("META");
      contentSearch.setAttribute("HTTP-EQUIV", "Content-Type");
      p.addStopElement(contentSearch, true);

      p.addStopElement(new HTMLElement("BODY"), true);
      p.addStopElement(new HTMLElement("FRAMESET"), true);

      return p;
   }
   */

   /**
    * Reads at most <CODE>len</CODE> bytes from <CODE>in</CODE> into memory
    * and returns a ByteArrayInputStream to the in-memory bytes. If at
    * least <CODE>len</CODE> bytes can be read from <CODE>in</CODE>, then
    * exactly <CODE>len</CODE> bytes <I>will</I> be read (even if multiple
    * reads are necessary).
    *
    * @param in The input stream from which bytes are to be read. No more
    * than <CODE>len</CODE> bytes will be read from this stream, and it
    * will not be closed. Must not be <CODE>null</CODE>.
    *
    * @param len The maximum number of bytes that should be read. If
    * there are at least <CODE>len</CODE> bytes available to be read,
    * then exactly <CODE>len</CODE> bytes will be read. Must be > 0.
    *
    * @return An in-memory input stream containing at most <CODE>len</CODE>
    * bytes from the given input stream. Never <CODE>null</CODE>.
    *
    * @throws IOException If an I/O error occurs.
    */
   private static ByteArrayInputStream readIntoMem(InputStream in, int len)
      throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in cannot be null");
      if (len < 1L)
         throw new IllegalArgumentException("len cannot be " + len);

      byte[] bytes = new byte[len];
      int bytesRead = 0;
      int bytesRemaining = len;

      while (bytesRemaining > 0)
      {
         int read = in.read(bytes, bytesRead, bytesRemaining);

         if (read < 0)
            break; // end of input stream reached

         bytesRead += read;
         bytesRemaining -= read;
      }

      return new ByteArrayInputStream(bytes);
   }

   /** The default MIME type for streams. Specifies a "raw" type. */
   private static final String ms_defaultMimeType = "application/octet-stream";

   /** The name of the properties file to load */
   private static final String ms_mimePropsFileName = "mimemap.properties";

   /** A map from file types (extensions) to MIME types. */
   private static Properties ms_mimeTypes = new Properties();

   static
   {
      boolean mimesObtainedFromFile = false;

      File f = PSProperties.getConfig(PSServer.ENTRY_NAME, ms_mimePropsFileName,
            PSServer.SERVER_DIR);
      if (!canBeRead(f))
      {
         try
         {
            f = new File(
                  new File(getRootDirFromLocator(), PSServer.SERVER_DIR),
                  ms_mimePropsFileName);
         }
         catch (Exception e)
         {
            log.info("Was not able to get server directory from locator (normal for client) Error {}", e.getMessage());
            log.debug(e.getMessage(),e);
         }
      }
      if (canBeRead(f))
      {
         FileInputStream fInput = null;
         try {
            fInput = new FileInputStream(f);
            ms_mimeTypes.load(fInput);
            mimesObtainedFromFile = true;
         } catch (IOException e)
         {
            log.warn("Could not load mime props from {}, . Using hardcoded defaults. Error: {} " + f, e.getMessage());
         } finally {
            if (fInput!=null) 
               try { fInput.close();} catch (Exception e) {/*ignore*/ }
         }
      }

      if (!mimesObtainedFromFile)
      {
         ms_mimeTypes.setProperty("xsl", IPSMimeContentTypes.MIME_TYPE_APPLICATION_XSL);
         ms_mimeTypes.setProperty("xml", IPSMimeContentTypes.MIME_TYPE_TEXT_XML);
         ms_mimeTypes.setProperty("dtd", IPSMimeContentTypes.MIME_TYPE_APPLICATION_DTD);

         ms_mimeTypes.setProperty("a", "application/octet-stream");
         ms_mimeTypes.setProperty("ai", "application/postscript");
         ms_mimeTypes.setProperty("aif", "audio/x-aiff");
         ms_mimeTypes.setProperty("aifc", "audio/x-aiff");
         ms_mimeTypes.setProperty("aiff", "audio/x-aiff");
         ms_mimeTypes.setProperty("arc", "application/octet-stream");
         ms_mimeTypes.setProperty("au", "audio/basic");
         ms_mimeTypes.setProperty("avi", "application/x-troff-msvideo");
         ms_mimeTypes.setProperty("bcpio", "application/x-bcpio");
         ms_mimeTypes.setProperty("bin", "application/octet-stream");
         ms_mimeTypes.setProperty("c", "text/plain");
         ms_mimeTypes.setProperty("c++", "text/plain");
         ms_mimeTypes.setProperty("cc", "text/plain");
         ms_mimeTypes.setProperty("cdf", "application/x-netcdf");
         ms_mimeTypes.setProperty("cpio", "application/x-cpio");
         ms_mimeTypes.setProperty("cpp", "text/plain");
         ms_mimeTypes.setProperty("dump", "application/octet-stream");
         ms_mimeTypes.setProperty("dvi", "application/x-dvi");
         ms_mimeTypes.setProperty("eps", "application/postscript");
         ms_mimeTypes.setProperty("etx", "text/x-setext");
         ms_mimeTypes.setProperty("exe", "application/octet-stream");
         ms_mimeTypes.setProperty("gif", "image/gif");
         ms_mimeTypes.setProperty("gtar", "application/x-gtar");
         ms_mimeTypes.setProperty("gz", "application/octet-stream");
         ms_mimeTypes.setProperty("h", "text/plain");
         ms_mimeTypes.setProperty("hdf", "application/x-hdf");
         ms_mimeTypes.setProperty("hpp", "text/plain");
         ms_mimeTypes.setProperty("hqx", "application/octet-stream");
         ms_mimeTypes.setProperty("htm", "text/html");
         ms_mimeTypes.setProperty("html", "text/html");
         ms_mimeTypes.setProperty("ief", "image/ief");
         ms_mimeTypes.setProperty("java", "text/plain");
         ms_mimeTypes.setProperty("jfif", "image/jpeg");
         ms_mimeTypes.setProperty("tbnl", "image/jpeg");
         ms_mimeTypes.setProperty("jpe", "image/jpeg");
         ms_mimeTypes.setProperty("jpeg", "image/jpeg");
         ms_mimeTypes.setProperty("jpg", "image/jpeg");
         ms_mimeTypes.setProperty("latex", "application/x-latex");
         ms_mimeTypes.setProperty("man", "application/x-troff-man");
         ms_mimeTypes.setProperty("me", "application/x-troff-me");
         ms_mimeTypes.setProperty("mime", "message/rfc822");
         ms_mimeTypes.setProperty("mov", "video/quicktime");
         ms_mimeTypes.setProperty("movie", "video/x-sgi-movie");
         ms_mimeTypes.setProperty("mpe", "video/mpeg");
         ms_mimeTypes.setProperty("mpeg", "video/mpeg");
         ms_mimeTypes.setProperty("mpg", "video/mpeg");
         ms_mimeTypes.setProperty("ms", "application/x-troff-ms");
         ms_mimeTypes.setProperty("mv", "video/x-sgi-movie");
         ms_mimeTypes.setProperty("nc", "application/x-netcdf");
         ms_mimeTypes.setProperty("o", "application/octet-stream");
         ms_mimeTypes.setProperty("oda", "application/oda");
         ms_mimeTypes.setProperty("pbm", "image/x-portable-bitmap");
         ms_mimeTypes.setProperty("pdf", "application/pdf");
         ms_mimeTypes.setProperty("pgm", "image/x-portable-graymap");
         ms_mimeTypes.setProperty("pl", "text/plain");
         ms_mimeTypes.setProperty("pnm", "image/x-portable-anymap");
         ms_mimeTypes.setProperty("ppm", "image/x-portable-pixmap");
         ms_mimeTypes.setProperty("ps", "application/postscript");
         ms_mimeTypes.setProperty("qt", "video/quicktime");
         ms_mimeTypes.setProperty("ras", "image/x-cmu-rast");
         ms_mimeTypes.setProperty("rgb", "image/x-rgb");
         ms_mimeTypes.setProperty("roff", "application/x-troff");
         ms_mimeTypes.setProperty("rtf", "application/rtf");
         ms_mimeTypes.setProperty("rtx", "application/rtf");
         ms_mimeTypes.setProperty("saveme", "application/octet-stream");
         ms_mimeTypes.setProperty("sh", "application/x-shar");
         ms_mimeTypes.setProperty("shar", "application/x-shar");
         ms_mimeTypes.setProperty("snd", "audio/basic");
         ms_mimeTypes.setProperty("src", "application/x-wais-source");
         ms_mimeTypes.setProperty("sv4cpio", "application/x-sv4cpio");
         ms_mimeTypes.setProperty("sv4crc", "application/x-sv4crc");
         ms_mimeTypes.setProperty("t", "application/x-troff");
         ms_mimeTypes.setProperty("tar", "application/x-tar");
         ms_mimeTypes.setProperty("tex", "application/x-tex");
         ms_mimeTypes.setProperty("texi", "application/x-texinfo");
         ms_mimeTypes.setProperty("texinfo", "application/x-texinfo");
         ms_mimeTypes.setProperty("text", "text/plain");
         ms_mimeTypes.setProperty("tif", "image/tiff");
         ms_mimeTypes.setProperty("tiff", "image/tiff");
         ms_mimeTypes.setProperty("tr", "application/x-troff");
         ms_mimeTypes.setProperty("tsv", "text/tab-separated-values");
         ms_mimeTypes.setProperty("txt", "text/plain");
         ms_mimeTypes.setProperty("ustar", "application/x-ustar");
         ms_mimeTypes.setProperty("uu", "application/octet-stream");
         ms_mimeTypes.setProperty("wav", "audio/x-wav");
         ms_mimeTypes.setProperty("wsrc", "application/x-wais-source");
         ms_mimeTypes.setProperty("xbm", "image/x-xbitmap");
         ms_mimeTypes.setProperty("xpm", "image/x-xpixmap");
         ms_mimeTypes.setProperty("xwd", "image/x-xwindowdump");
         ms_mimeTypes.setProperty("z", "application/octet-stream");
         ms_mimeTypes.setProperty("zip", "application/zip");
      }
   }

   /**
    * Returns <code>true</code> if file can be read.
    */
   private static boolean canBeRead(File f)
   {
      return f != null && f.canRead() && f.isFile();
   }

   /**
    * Retrieves root directory from {@link PSRhythmyxInfoLocator}. 
    */
   private static String getRootDirFromLocator()
   {
      return (String) PSRhythmyxInfoLocator.getRhythmyxInfo()
            .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
   }
}
