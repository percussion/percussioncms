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

package com.percussion.search.lucene.textconverter;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.search.lucene.IPSLuceneConstants;
import com.percussion.server.PSServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Uses Apache Tika to extract text from many different document types.
 * This is added as a replacement for most of the old system text converters
 * 
 */
public class PSTikaTextConvertor implements IPSLuceneTextConverter
{
   /**
    * Reference to log for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSTikaTextConvertor.class);
   /**
    * Server property to set the write limit.  This is the maximum number of characters
    * to store in the String sent to the indexer.  This can be very large for a large document
    * If a document is over this limit it will only index the text before the limit.
    */
   private static final String INDEX_WRITE_LIMIT = "indexWriteLimit";
   /**
    * Creating a new TikaConfig object takes a long time, so we will create a singleton
    */
   private static TikaConfig m_tikaConfig = null;

   /*
    * Default write limit, just under 10M
    */
   private static int writeLimit = 5000000;
   
   private static Set<String> m_mimeTypes = new HashSet<String>();
   
   static
   {
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_PDF);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_MSWORD);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSWORD_DOC);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSWORD_TEMPLATE);      
      
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_VNDMSEXCEL);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_EXCEL);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSEXCEL_SHEET);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSEXCEL_TEMPLATE);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_VNDMSPOWERPOINT);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_MSPOWERPOINT);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSPOWERPOINT_PRES);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_OPENXML_MSPOWERPOINT_TEMPLATE);
      m_mimeTypes.add(StringUtils.lowerCase(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_RTF));
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_TEXT_BY_HTML);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_XHTML);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_XML);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_TEXT_BY_XML);
      m_mimeTypes.add(IPSLuceneConstants.MIME_TYPE_PLAIN_BY_TEXT);
   }


   public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
   {
     
   }

   private synchronized static void getTikaConfig() throws PSExtensionProcessingException
   {
      if (m_tikaConfig == null)
      {
         try
         {
            m_tikaConfig = new TikaConfig();
            
            if (PSServer.getServerProps()!=null) {
            String prop = PSServer.getServerProps().getProperty(INDEX_WRITE_LIMIT);
            if (prop!=null) {
               try {
               writeLimit=Integer.parseInt(prop);
               } catch (NumberFormatException e) {
                  ms_log.error("property "+INDEX_WRITE_LIMIT+" in server.properties is not an integer "+prop);
               }
            } 
            }
         }
         catch (MimeTypeException e)
         {
            throw new PSExtensionProcessingException("Cannot get TikaConfig", e);
         }
         catch (IOException e)
         {
            throw new PSExtensionProcessingException("Cannot get TikaConfig", e);
         }
         catch (TikaException e)
         {
            throw new PSExtensionProcessingException("Cannot get TikaConfig", e);
         }
      }
   }

   public String getConvertedText(InputStream is, String mimetype) throws PSExtensionProcessingException
   {
      if (!m_mimeTypes.contains(mimetype))
         return "";
      
      getTikaConfig();
      
      TikaInputStream tis = null;

      Parser parser = new AutoDetectParser(m_tikaConfig);
      Metadata metadata = new Metadata();
      metadata.set(HttpHeaders.CONTENT_TYPE, mimetype);
      WriteOutContentHandler handler = new WriteOutContentHandler(writeLimit);
      BodyContentHandler bodyhandler = new BodyContentHandler(handler);

      try
      {
         tis = TikaInputStream.get(is);
         // getFile() Forces tika to stream to temporary file. parse uses
         // hasFile to decide whether processing should be done
         // using file or in memory. We want to preserve memory.
         // TikaInputStream.get(File file) also sets hasFile.
         tis.getFile();
         parser.parse(tis, bodyhandler, metadata, new ParseContext());
      }
      catch (Exception e)
      {
         if (handler.isWriteLimitReached(e))
         {
            ms_log.warn("Document text is larger than current index write limit of "
                  + INDEX_WRITE_LIMIT
                  + " chars. "
                  + "Only text up to this will be indexed, you can increase limit by setting indexWriteLimit property in server.properties file. "
                  + "Increasing will use more memory.");
         }
         else
         {
            ms_log.warn("Document cannot be indexed, set debug trace to see full stack: ",
                  ExceptionUtils.getRootCause(e));
            ms_log.debug("Document cannot be indexed", e);
         }
         throw new PSExtensionProcessingException(e.getMessage(),e);
      }
      /*
       * Some documents can cause tika and the underlying parser to allocate way
       * too much memory We may not be able to allocate enough memory to handle
       * so we will try and recover and move on to next document. You should not
       * normally try and catch Errors, as they are usually non-recoverable. In
       * this case it is our only option or we will have to stop everything. If
       * the error is caused by an attempted allocation of a massive amount of
       * memory, that memory may be freed up and we can continue. it is possible
       * that another thread fails to allocate because of this and we cannot
       * recover those threads. A safer solution would to be to handle the
       * parsing on a separate jvm process so the server is not affected.
       */
      catch (OutOfMemoryError e)
      {
         // try to close input stream to release any memory first before
         // logging.
         IOUtils.closeQuietly(tis);
         ms_log.error("Out of memory error while processing document while indexing"
               + " you may need to increase the java heap allocated to the CM1 server. "
               + "This may just be a document that cannot be handled currently by the underlying Tika processor. "
               + "This Document will be skipped");
      }
      finally
      {
         // Related to PDFBOX-1009 Limit the CMap-cache to external CMaps
         // The static map is eating up memory when processing many documents.  Each
         // font takes up about 5Mb of memory and this can raise above 1Gb or usage.
         // we will clear here to release.
         IOUtils.closeQuietly(tis);
         IOUtils.closeQuietly(is);
      }

      return bodyhandler.toString();
   }
}
