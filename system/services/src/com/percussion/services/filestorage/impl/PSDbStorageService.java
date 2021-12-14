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
package com.percussion.services.filestorage.impl;

import com.percussion.server.PSServer;
import com.percussion.services.filestorage.IPSFileDigestService;
import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.IPSHashedFileDAO;
import com.percussion.services.filestorage.PSBinaryServiceException;
import com.percussion.services.filestorage.data.PSBinary;
import com.percussion.services.filestorage.data.PSBinaryData;
import com.percussion.services.filestorage.data.PSBinaryMetaEntry;
import com.percussion.services.filestorage.data.PSBinaryMetaKey;
import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.services.filestorage.data.PSMeta;
import com.percussion.services.filestorage.error.PSFileStorageException;
import com.percussion.util.PSPurgableTempFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * @author stephenbolton
 *
 */
/**
 * @author stephenbolton
 *
 */
@Transactional
public class PSDbStorageService implements IPSFileStorageService, InitializingBean 
{

    private static final String AUTO_IMPORT_BINARIES_TXT = "autoImportBinaries.txt";

   /**
    * Server property key to allow for disabling of metadata keys being added automatically when
    * found in documents. true default, false to disable
    */
   private static final String AUTO_ENABLE_BINARY_METADATA_KEYS = "autoEnableBinaryMetadataKeys";

   /**
    */
   private static final String META_EXTRACTOR_MAX_STRING_LENGTH_PROP_NAME = "metaExtractorMaxStringLength";

   /**
    * See {@link #getMaxStringLength()}.
    */
   private static final int DEFAULT_MAX_STRING_LENGTH = 10 * 1024 * 1024;

   private static final int MAX_META_LENGTH = 500;

   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSDbStorageService.class);

   private static String REPARSE_THREAD_NAME = "MetadataReparseThread";

   private static String IMPEXP_THREAD_NAME = "BinaryImpExpThread";

   private static ReparseThread reparseThread;

   private static ImpExpThread impExpThread;

   private TikaConfig tikaConfig = null;
   
   private IPSHashedFileDAO hashDao = null;

   private IPSFileDigestService digestService;

   /**
    * Create a reusable TikaConfig. This object takes a while to initialize.
    */
   public PSDbStorageService()
   {
     
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#store(java.io
    * .File)
    */
   @Transactional
   public String store(File file) throws Exception
   {

      PSMeta meta = populateTempFileMeta(file);
      PSBinary binary = hashDao.getBinary(meta.getHash());
      if (binary == null)
      {
         // InputStream closed by hibernate when persisted.
         InputStream is = new FileInputStream(file);
         meta = getMetaData(file, meta);
         binary = create(is, meta);
      }

      return binary.getHash();
   }
   
   public String getType(File file)
   {
      String type="application/octet-stream";
      String filename="";
      Metadata metadata = new Metadata();
      if (file instanceof PSPurgableTempFile)
      {
         PSPurgableTempFile ptFile = (PSPurgableTempFile)file;
         type=ptFile.getSourceContentType();
         metadata.add(Metadata.RESOURCE_NAME_KEY, ptFile.getSourceFileName());
         filename=ptFile.getSourceFileName();
      }
      else
      {
         metadata.add(Metadata.RESOURCE_NAME_KEY, file.getName());
      }
       
      try
      {
         MediaType mimetype = tikaConfig.getDetector().detect(
               TikaInputStream.get(file), metadata);
         if(!type.equals(mimetype.toString())) {
            return mimetype.toString();
         }
         log.debug("File " +filename+ " of type " + mimetype);
      }
      catch (IOException e)
      {
         log.debug("Cannot extract mimetype from file "+file.getAbsolutePath());
      }
 
      return type;
      
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#reparseMeta(
    * java.lang.String)
    */
   @Transactional
   public boolean reparseMeta(String hash)
   {
      PSBinary binary = hashDao.getBinary(hash);
      return reparseMeta(binary);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#store(java.io
    * .InputStream, com.percussion.services.filestorage.data.PSMeta,
    * java.lang.String, java.lang.String, java.lang.String)
    */
   @Transactional(rollbackFor = Exception.class)
   public String store(InputStream is, String contentType, String originalFilename, String encoding) throws Exception
   {
      PSPurgableTempFile file = createTempFileFromStream(is, contentType, originalFilename, encoding);
      return store(file);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#delete(java.
    * lang.String)
    */
   @Transactional(rollbackFor = Exception.class)
   public void delete(String hash) throws Exception
   {
      notNull(hash);
      notEmpty(hash);
      hashDao.delete(hash);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#getMeta(java
    * .lang.String)
    */
   @Transactional
   public PSMeta getMeta(String hash)
   {
      return convertFromDbMeta(getDbMeta(hash));
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#getStream(java
    * .lang.String)
    */
   @Transactional
   public InputStream getStream(String hash)
   {
      if (StringUtils.isNotEmpty(hash))
      {
         PSBinary binary = hashDao.getBinary(hash);
         if (binary != null)
            return binary.getData().getContent();
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#fileExists(java
    * .lang.String)
    */
   public boolean fileExists(String hash)
   {
      return hashDao.exists(hash);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#getAlgorithm()
    */
   public String getAlgorithm()
   {
      return this.digestService.getAlgorithm();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#getText(java
    * .lang.String)
    */
   @Transactional
   public String getText(String hash)
   {
      notNull(hash);

      String content = "";
      PSBinary hashFile = hashDao.getBinary(hash);

      if (hashFile != null)
      {
         PSMeta meta = convertFromDbMeta(hashFile.getMetaEntries());
         content = getText(hashFile.getData().getContent(), meta, getMaxStringLength());
      }

      return content;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#countOlderThan
    * (int)
    */
   @Transactional
   public long countOlderThan(int days)
   {
      return hashDao.countOlderThan(days);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSFileStorageService#deleteOlderThan
    * (int)
    */
   @Transactional
   public long deleteOlderThan(int days)
   {
      return hashDao.deleteOlderThan(days);
   }

   /**
    * 
    * Populates The {@link PSMeta} object with information extracted from the
    * {@link File} or {@link PSPurgableTempFile} this includes the mime type and
    * encoding as well as the original file name.
    * 
    * @param file
    * @return
    */
   private PSMeta populateTempFileMeta(File file)
   {
      PSMeta meta = new PSMeta();
      String hash = null;
      boolean parseError = false;
      String parseErrorString = "Error";
      try
      {
         hash = getHashForFile(file);
         meta.setHash(hash);
      }
      catch (FileNotFoundException e)
      {
         parseErrorString = "File " + file.getAbsolutePath() + " Not found";
         log.error(parseErrorString, e);
         parseError = true;
      }
      catch (NoSuchAlgorithmException e)
      {
         parseErrorString = "Hashing algorithm " + getAlgorithm() + " not known";
         log.error(parseErrorString, e);
         parseError = true;
      }
      catch (IOException e)
      {
         parseErrorString = "IO Exception reading file";
         log.error(parseErrorString, e);
         parseError = true;
      }
      catch (PSBinaryServiceException e)
      {
         parseErrorString = "Error processing file";
         log.error(parseErrorString, e);
         parseError = true;
      }
      if (parseError)
      {
         meta.setParseError(parseErrorString);
         return meta;
      }

      String filename;
      filename = file.getName();
      if (file instanceof PSPurgableTempFile)
      {
         log.debug("Item is a PSPurgableTempFile");
         PSPurgableTempFile temp = (PSPurgableTempFile) file;
         if (temp.getSourceContentType() != null)
         {
            meta.setMimeType(temp.getSourceContentType());
         }
         if (temp.getCharacterSetEncoding() != null)
         {
            meta.setEncoding(temp.getCharacterSetEncoding());
         }
         // Some browsers ie/opera pass full path, others just filename.

         filename = temp.getSourceFileName();
         if (filename == null)
         {
            throw new PSFileStorageException("A PSPurgeableTempFile requires a sourceFileName to be persisted");
         }

      }
      else
      {
         log.debug("Item is a File");
         filename = file.getName();

         // Some browsers ie/opera pass full path, others just filename.
      }
      filename = filename.replaceAll("\\\\", "/");
      if (filename.contains("/"))
      {
         filename = filename.substring(filename.lastIndexOf("/") + 1);
      }
      meta.setOriginalFilename(filename);
      meta.setLength(file.length());
      return meta;
   }

   /**
    * Extract the Tika based metadata from the file. The initial meta object
    * should contain information like filename and type to support the
    * extraction.
    * 
    * @param file
    * @param meta The initial metadata object.
    * @return PSMeta object contains hashmap of metadata as well as getters
    */
   private PSMeta getMetaData(File file, PSMeta meta)
   {

      Metadata tikaMeta = meta.getTikaMetadata();
      InputStream is = null;
      Parser parser = new AutoDetectParser(tikaConfig);

      ParseContext context = new ParseContext();
      try
      {
         // If we use a file in TikaInputStream the processing is done directly from this file otherwise
         // the contents are streamed to memory.
         is = TikaInputStream.get(file);
         try
         {
            parser.parse(is, new DefaultHandler(), tikaMeta, context);
         }
         catch (TikaException e)
         {
            errorGettingTikaMetadata(meta, e);
         }
         catch (IOException e)
         {
            errorGettingTikaMetadata(meta, e);
         }
         catch (SAXException e)
         {
            errorGettingTikaMetadata(meta, e);
         }
         /* Some documents can cause tika and the underlying parser to allocate way too much memory
            We may not be able to allocate enough memory to handle so we will try and recover and move
            on to next document.  You should not normally
            try and catch Errors, as they are usually non-recoverable.  In this case it is our only option or
            we will have to stop everything.  If the error is caused by an attempted allocation of a massive amount
            of memory, that memory may be freed up and we can continue.
            it is possible that another thread fails to allocate because of this and we cannot recover those threads.
            A safer solution would to be to handle the parsing on a separate jvm process so the server is not affected.
         */
         catch (OutOfMemoryError e) {
            // try to close input stream to release any  memory first before logging.
            IOUtils.closeQuietly(is);
            log.error("Out of memory error while processing document "+meta.getOriginalFilename()
                  + " with length "+meta.getLength() + " and hash " + meta.getHash()  
                  + " you may need to increase the Xmx value in RhythmyxServer.bin.lax or RhythmyxServer.lax file. "
                  + "This may just be a document that cannot be handled currently by the underlying Tika processor. " +
                  "This Document will be skipped");
            errorGettingTikaMetadata(meta, e);
         }
      }
      catch (FileNotFoundException e)
      {
         log.error("Cannot find file to Extract Metadata", e);
      }
      finally
      {
         // Related to PDFBOX-1009 Limit the CMap-cache to external CMaps
         // The static map is eating up memory when processing many documents.  Each
         // font takes up about 5Mb of memory and this can raise above 1Gb or usage.
         // we will clear here to release.
         IOUtils.closeQuietly(is);
      }
      meta.setTikaMetadata(tikaMeta);
      return meta;
   }

   /**
    * Calculate the Hash value for a file
    * 
    * @param file
    * @return The hash itself
    * @throws FileNotFoundException
    * @throws NoSuchAlgorithmException
    * @throws IOException
    */
   private String getHashForFile(File file) throws FileNotFoundException, NoSuchAlgorithmException, IOException
   {
      InputStream fis = null;
      String hash = null;
      try
      {
         fis = new FileInputStream(file);
         hash = digestService.createChecksum(fis);
      }
      finally
      {
        IOUtils.closeQuietly(fis);
      }
      return hash;
   }

   /**
    * Convert a stream to a {@link PSPurgableTempFile}, used for reprocessing
    * the metadata if required.
    * 
    * @param is
    * @param contentType
    * @param filename
    * @param encType
    * @return
    * @throws IOException
    * @throws FileNotFoundException
    */
   private PSPurgableTempFile createTempFileFromStream(InputStream is, String contentType, String filename,
         String encType) throws IOException, FileNotFoundException
   {
      PSPurgableTempFile f = new PSPurgableTempFile("dedup_", ".bin", null, filename, contentType, encType);

      FileOutputStream fos = new FileOutputStream(f);
      IOUtils.copy(is, fos);

      fos.close();
      return f;
   }

   /**
    * Utility method to get the stracktrace from a throwable object.
    * 
    * @param t throwable object.
    * 
    * @return the stacktrace, never <code>null</code>.
    */
   private String getStackTrace(Throwable t)
   {
      StringWriter stringWritter = new StringWriter();
      PrintWriter printWritter = new PrintWriter(stringWritter, true);
      t.printStackTrace(printWritter);
      printWritter.flush();
      stringWritter.flush();

      return stringWritter.toString();
   }

   /**
    * Extracts the text from the specified input stream and updates the
    * specified tika metadata accordingly. The stream is closed by this method.
    * If an error occurs, a  property will be added to the
    * metadata with a value which contains the error.
    * 
    * @param input stream.
    * @param meta metadata.
    * @param maxLength the maximum length of the returned string of text in
    *           bytes.
    * 
    * @return the extracted text, never <code>null</code>.
    */
   private String getText(InputStream input, PSMeta meta, int maxLength)
   {
      Metadata tikaMeta = meta.getTikaMetadata();
      String text = "";

      Tika tika = new Tika();
      tika.setMaxStringLength(maxLength);

      try
      {
         text = tika.parseToString(input, tikaMeta);
      }
      catch (Exception e)
      {
         String msg = e.getMessage();
         log.error("Error extracting Tika metadata: " + msg);
         meta.setParseError(msg + ":" + getStackTrace(e));
      }

      return text;
   }

   /**
    * To avoid unpredictable excess memory use, extractor will only extract
    * getMaxStringLength() first characters extracted from the input document..
    * 
    * @return size default is
    */
   private static int getMaxStringLength()
   {
      if (PSServer.getServerProps() == null)
         return DEFAULT_MAX_STRING_LENGTH;
      String prop = PSServer.getServerProps().getProperty(META_EXTRACTOR_MAX_STRING_LENGTH_PROP_NAME,
            "" + DEFAULT_MAX_STRING_LENGTH);
      Integer p = Integer.parseInt(prop);
      if (p < 0)
         return Integer.MAX_VALUE;
      return p;
   }

   /**
    * @return the dao
    */
   public IPSHashedFileDAO getHashDao()
   {
      return hashDao;
   }

   /**
    * @param hashDao the dao
    */
   public void setHashDao(IPSHashedFileDAO hashDao)
   {
      this.hashDao = hashDao;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#updateFilename(java.lang.String, java.lang.String)
    */
   @Override
   @Transactional
   public void updateFilename(String hash, String value)
   {
      PSBinary binary = this.hashDao.getBinary(hash);
      binary.setOriginalFilename(value);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#updateType(java.lang.String, java.lang.String)
    */
   @Override
   @Transactional
   public void updateType(String hash, String value)
   {
      PSBinary binary = this.hashDao.getBinary(hash);
      binary.setMimeType(value);
      reparseMeta(hash);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#updateEncoding(java.lang.String, java.lang.String)
    */
   @Override
   @Transactional
   public void updateEncoding(String hash, String value)
   {
      PSBinary binary = this.hashDao.getBinary(hash);
      binary.setEncoding(value);
      reparseMeta(hash);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#getMetaKeys()
    */
   @Override
   @Transactional
   public List<String> getMetaKeys()
   {
      List<String> keys = new ArrayList<>();
      for (PSBinaryMetaKey key : this.hashDao.getMetaKeys())
      {
         if (key.isEnabled())
            keys.add(key.getName());
      }
      return keys;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#enableMetaKey(java.lang.String)
    */
   @Override
   @Transactional
   public boolean enableMetaKey(String keyname)
   {
      PSBinaryMetaKey key = this.hashDao.getMetaKey(keyname);
      if (key != null)
      {
         key.setEnabled(true);
         return true;
      }
      return false;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#disableMetaKey(java.lang.String)
    */
   @Override
   @Transactional
   public boolean disableMetaKey(String keyname)
   {
      PSBinaryMetaKey key = this.hashDao.getMetaKey(keyname);
      if (key != null)
      {
         key.setEnabled(false);
         return true;
      }
      return false;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#reparseMetaAll()
    */
   @Override
   @Transactional
   public boolean reparseMetaAll()
   {
      this.hashDao.setReparseAllMeta();
      // Kick of reparse thread now.
      return processReparseMeta();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#getDisabledMetaKeys()
    */
   @Override
   @Transactional
   public List<String> getDisabledMetaKeys()
   {
      List<String> keys = new ArrayList<>();
      for (PSBinaryMetaKey key : this.hashDao.getMetaKeys())
      {
         if (!key.isEnabled())
            keys.add(key.getName());
      }
      return keys;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#exportAllBinary(java.lang.String)
    */
   public synchronized boolean exportAllBinary(String rootPath)
   {
      if (impExpThread == null || !impExpThread.isAlive())
      {
         impExpThread = new ImpExpThread(IMPEXP_THREAD_NAME);
         impExpThread.setImport(false);
         impExpThread.setBasePath(rootPath);
         impExpThread.start();
         return true;
      }
      return false;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#legacyTableExists()
    */
   @Override
   public boolean legacyTableExists()
   {
      return hashDao.hasLegacyTable();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#exportAllLegacyBinary(java.lang.String)
    */
   public synchronized boolean exportAllLegacyBinary(String rootPath)
   {
      if (impExpThread == null || !impExpThread.isAlive())
      {
         impExpThread = new ImpExpThread(IMPEXP_THREAD_NAME);
         impExpThread.setImport(false);
         impExpThread.setLegacy(true);
         impExpThread.setBasePath(rootPath);
         impExpThread.start();
         return true;
      }
      return false;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#isImpExpRunning()
    */
   public synchronized boolean isImpExpRunning()
   {
      return (impExpThread!=null && impExpThread.isAlive());
   }
   
   
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#isReparseMetaRunning()
    */
   public synchronized boolean isReparseMetaRunning()
   {
      return (reparseThread!=null && reparseThread.isAlive());
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#importAllBinary(java.lang.String)
    */
   public synchronized boolean importAllBinary(String rootPath)
   {
      if (impExpThread == null || !impExpThread.isAlive())
      {
         impExpThread = new ImpExpThread(IMPEXP_THREAD_NAME);
         impExpThread.setImport(true);
         impExpThread.setBasePath(rootPath);
         impExpThread.start();
         return true;
      }
      return false;
   }


   /**
    * @param batchSize
    * @return the number of items set to reparse
    */
   @Transactional
   public int reparseBatch(int batchSize)
   {
      List<PSBinary> items = this.hashDao.getReparseBatch(batchSize);
      int count = 0;
      for (PSBinary item : items)
      {
         count++;
         if (item.isReparseMeta())
            reparseMeta(item);
      }
      return (count);
   }

   /**
    * A Thread to reparse all the metadata asynchronously
    * @author stephenbolton
    * 
    */
   
   private class ReparseThread extends Thread
   {

      public final int BATCH_SIZE = 100;

      /**
       * @param name The name for the thread
       */
      ReparseThread(String name)
      {
         super(name);
         log.debug("created ReparseThread");
      }

      /* (non-Javadoc)
       * @see java.lang.Thread#run()
       */
      public void run()
      {
         log.debug("Starting ReparseThread");
         boolean moreItems = true;
         // If checking on server startup wait for 5 min before starting
         
         int count = 0;
         while (moreItems)
         {
            int batchCount = PSDbStorageService.this.reparseBatch(BATCH_SIZE);
            count += batchCount;
            moreItems = batchCount == BATCH_SIZE;
            log.debug("Re-parsed {} Binary Items. Total {}" ,batchCount, count);
            try
            {
               sleep(10);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }

         log.debug("Finished re-parsing metadata for binaries.  Shutting down thread");
      }
   }

   /**
    * A thread used for background importing and exporting of binary items
    * @author stephenbolton
    *
    */
   private class ImpExpThread extends Thread
   {
      private static final int IMP_EXP_PAGE_SIZE = 100;

      /**
       * Extension to check for sha1 file.  This file
       * is used to identify items used to import and to check
       * if file is already imported/exported.
       */
      private static final String SHA1_FILE_EXTENSION = ".sha1";

      private String basePath = "";

      private boolean m_isImport = false;

      private boolean m_isLegacy = false;

      public ImpExpThread(String name)
      {
         super(name);
      }

      public void run()
      {
         try
         {
            Thread.sleep(30000);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         if (m_isImport)
         {
            importAll();
         }
         else
         {
            exportAll();
         }

         log.debug("Finished importing/exporting.  Shutting down thread");
      }

      /**
       * Import all based upon the folder specified in the basePath property
       */
      private void importAll()
      {
         if (!StringUtils.isBlank(basePath))
         {
            File dir = new File(basePath);
            importFolder(dir);
         }
         // check auto Import file
         // Check if any folders need auto import e.g. Fast Forward

         try
         {
            File autoImportFile = new File(PSServer.getRxFile(AUTO_IMPORT_BINARIES_TXT));

            List<String> pathsToImport = null;
            if (autoImportFile != null && autoImportFile.exists())
            {
               pathsToImport = FileUtils.readLines(autoImportFile, "UTF-8");
               if (!pathsToImport.isEmpty())
               {
                  for (String path : pathsToImport)
                  {
                     File dir = new File(path);
                     if (dir != null && dir.exists())
                     {
                        log.debug("Auto importing binaries from directory " + path);
                        importFolder(dir);
                     }
                  }
                  // If success remove auto import file, otherwise we well retry
                  autoImportFile.delete();
               }
            }
         }
         catch (IOException e)
         {
            log.debug("Cannot read auto import file " + AUTO_IMPORT_BINARIES_TXT, e);

         }
         catch (IllegalArgumentException e)
         {
            log.debug("Cannot read auto import file " + AUTO_IMPORT_BINARIES_TXT, e);
         }
      }

      /**
       * Recursively parse folder looking for .sha1 files
       * to import the respective files without this extension in the
       * same foldder.
       * @param folder the base folder
       */
      public void importFolder(File folder)
      {
         if (folder.isDirectory())
         {
            File[] children = folder.listFiles();
            for (File child : children)
            {
               if (child.isDirectory())
               {
                  importFolder(child);
               }
               else
               {
                  if (child.getName().endsWith(SHA1_FILE_EXTENSION))
                  {
                     File mainFile = new File(folder, StringUtils.substringBefore(child.getName(), SHA1_FILE_EXTENSION));
                     String sha1FileString = null;
                     try
                     {
                        sha1FileString = loadSha1File(child);
                     }
                     catch (IOException e)
                     {
                        log.error("cannot load sha1 from file "+child.getAbsolutePath(),e);
                     }
                     boolean fileInDb = sha1FileString == null ? false : PSDbStorageService.this
                           .fileExists(sha1FileString);
                     if (log.isInfoEnabled()) {
                        if (!fileInDb) {
                           log.info("Importing file "+mainFile);
                        } 
                        else
                        {
                           log.info("Skipping Import existing file "+mainFile);
                        }
                        
                     }
                 
                     if (!fileInDb)
                     {
                        FileOutputStream shaOutputStream = null;
                        try
                        {
                           String newHash = PSDbStorageService.this.store(mainFile);
                           if (!sha1FileString.equals(newHash))
                           {
                              // update hash file, was either empty or wrong
                              shaOutputStream = new FileOutputStream(child);
                              IOUtils.write(newHash, shaOutputStream);
                           }
                        }
                        catch (Exception e)
                        {
                          log.error("Could not import item " +mainFile.getAbsolutePath(), e);
                        } 
                        finally 
                        {
                           IOUtils.closeQuietly(shaOutputStream);
                        }
                        
                     }
                  }
               }
            }
         }
      }

      /**
       * Export all binary items and store in the basePath folder.  The files
       * will be stored in folders based upon their hash to ensure uniqueness and to 
       * make sure that not too many folders/files are created under one folder.  Two
       * characters of the hash ensure a maximum of 256 subfolder.
       */
      @Transactional
      private void exportAll()
      {


         int pageNum = 0;
         List<PSBinary> page = PSDbStorageService.this.hashDao.findAllBinary(pageNum, IMP_EXP_PAGE_SIZE);
         while (page.size() > 0)
         {
            log.info("Exporting binaries itemCount="+(pageNum*IMP_EXP_PAGE_SIZE));
            for (PSBinary binary : page)
            {
               String hash = binary.getHash();
               String filename = binary.getOriginalFilename();

               InputStream inputStream = binary.getData().getContent();
               try
               {
                  exportFile(hash, filename, inputStream);
               }
               catch (IOException e)
               {
                  log.error("Failed to export file " +filename + "with hash " +hash,e);
               }

            }
            if (page.size() < IMP_EXP_PAGE_SIZE)
               break;
            page = PSDbStorageService.this.hashDao.findAllBinary(++pageNum, IMP_EXP_PAGE_SIZE);
         }

      }

      /**
       * Export a single file
       * @param hash the file hash
       * @param filename the filame to create the file with
       * @param inputStream The stream containing the file contents
       * @throws IOException 
       */
      private void exportFile(String hash, String filename, InputStream inputStream) throws IOException
      {
         File dir = getPathfromHash(hash);
         File shaFiles[] = dir.listFiles((dir1, filename1) -> filename1.endsWith(SHA1_FILE_EXTENSION));

         boolean foundHash = false;
         if (shaFiles!=null) {
            for (File testfile : shaFiles)
            {
   
               String sha1FileString = null;
   
               sha1FileString = loadSha1File(testfile);
   
               if (sha1FileString != null && sha1FileString.equalsIgnoreCase(hash))
                  foundHash = true;
            }
         }
         if (!foundHash)
         {

            FileOutputStream outputStream = null;
            FileOutputStream shaOutputStream = null;
            try
            {
               outputStream = new FileOutputStream(new File(dir, filename));
               shaOutputStream = new FileOutputStream(new File(dir, filename + SHA1_FILE_EXTENSION));
               IOUtils.copyLarge(inputStream, outputStream);
               // now write sha1 file {filename}.sha1
               IOUtils.write(hash, shaOutputStream);
            }
            catch (FileNotFoundException e)
            {
               log.error("Could not write file to export",e);
            }
            finally
            {
               IOUtils.closeQuietly(inputStream);
               IOUtils.closeQuietly(outputStream);
               IOUtils.closeQuietly(shaOutputStream);
            }

         }
      }

      /**
       * Loads in a file containing an sha1 hash and return the hash value
       * @param testfile
       * @return
       * @throws IOException 
       */
      private String loadSha1File(File testfile) throws IOException
      {
         FileInputStream shaStream = null;
         String sha1FileString = null;
         try
         {
            shaStream = new FileInputStream(testfile);
            sha1FileString = StringUtils.trimToNull(IOUtils.toString(shaStream));
         }
         finally
         {
            IOUtils.closeQuietly(shaStream);
         }
         return sha1FileString;
      }

      /**
       * Create a path based upon a hash value.  This ensures a balanced set of folders
       * with no folder containing too many entries.
       * @param hash
       * @return
       * @throws IOException 
       */
      private File getPathfromHash(String hash) throws IOException
      {
         // Use 4 levels. first 3 max out at 256 folders with 2 chars, remaining
         // should
         // never have many folders even with many millions of items.
         String f1 = hash.substring(0, 2);
         String f2 = hash.substring(2, 4);
         String f3 = hash.substring(4, 6);
         String f4 = hash.substring(6);
         File dir = new File(basePath + File.separator + f1 + File.separator + f2 + File.separator + f3
               + File.separator + f4);
         FileUtils.forceMkdir(dir);
         
         return dir;
      }

      /**
       * Is this an import or an export
       * @param isImport
       */
      public void setImport(boolean isImport)
      {
         m_isImport = isImport;
      }

      /**
       * Set the base path to import to or from
       * @param basePath
       */
      public void setBasePath(String basePath)
      {
         this.basePath = basePath;
      }

      /**
       * Is this a legacy export.  This will export from the old
       * PSX_BINARYSTORE table and the result can be imported into the
       * new table structure.
       * 
       * @param isLegacy
       */
      public void setLegacy(boolean isLegacy) {
         m_isLegacy = isLegacy;
      }

   }

   /**
    * Convert a PSMeta object into the Set of BSBInaryMetaEntries entities to be persisted
    * in the database
    * @param meta
    * @return the set of entities
    */
   @Transactional
   private Set<PSBinaryMetaEntry> convertMetaToDbMeta(PSMeta meta)
   {
      // Default to automatically add metadata keys when they are found
      // in documents.  A customer may want to have a fixed set to prevent users relying
      // on others and/or to reduce database storage requirements.
   
      String prop = PSServer.getProperty(AUTO_ENABLE_BINARY_METADATA_KEYS);
      boolean autoEnable = (prop!=null && prop.equalsIgnoreCase("false"))? false: true;  
      
      Set<PSBinaryMetaEntry> entries = new HashSet<>();

      for (Entry<String, String> entry : meta.entrySet())
      {

         PSBinaryMetaKey key = this.hashDao.findOrCreateMetaKey(entry.getKey(), autoEnable);
         if (key.isEnabled())
         {
            PSBinaryMetaEntry dbEntry = new PSBinaryMetaEntry(key, StringUtils.abbreviate(entry.getValue(),
                  MAX_META_LENGTH));
            entries.add(dbEntry);
         }

      }
      return entries;
   }

   /**
    * Create a PSMeta object from the PSBinaryMetaEntry entities
    * @param meta the set of entities
    * @return the PSMeta
    */
   private PSMeta convertFromDbMeta(Set<PSBinaryMetaEntry> meta)
   {
      PSMeta localMeta = new PSMeta();

      for (PSBinaryMetaEntry entry : meta)
      {
         localMeta.put(entry.getKey().getName(), entry.getValue());
      }
      return localMeta;
   }

   /**
    * update the database with the metadata for a particular hash
    * 
    * @param hash
    * @param meta
    */
   @Transactional
   public void setMeta(String hash, PSMeta meta)
   {
      notEmpty(hash);
      notNull(meta);

      PSBinary binary = hashDao.getBinary(hash);

      Set<PSBinaryMetaEntry> dbMeta = convertMetaToDbMeta(meta);
      binary.setMetaEntries(dbMeta);
      hashDao.save(binary);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSHashedFileDAO#create(java.lang.
    * String, java.io.InputStream, java.lang.String,
    * com.percussion.services.filestorage.data.PSHashedMeta)
    */
   @Transactional
   public PSBinary create(InputStream is, PSMeta meta)
   {

      String hash = meta.getHash();
      notNull(hash);
      PSBinaryData binaryData = new PSBinaryData(hashDao.createBlob(is,meta.getLength()));
      Set<PSBinaryMetaEntry> dbMeta = convertMetaToDbMeta(meta);
      PSBinary binary = new PSBinary(meta.getHash(), binaryData, dbMeta);
      binary.setMetaEntries(dbMeta);
      binary.setExtractError(meta.hasParseError());
      binary.setLastAccessedDate(new Date());
      hashDao.save(binary);

      return binary;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.filestorage.IPSHashedFileDAO#getMeta(java.lang
    * .String)
    */
   @Transactional
   public Set<PSBinaryMetaEntry> getDbMeta(String hash)
   {
      notEmpty(hash);
      Set<PSBinaryMetaEntry> dbmeta = new HashSet<>();
      PSBinary binary = hashDao.getBinary(hash);
      if (binary != null)
      {
         dbmeta = binary.getMetaEntries();
         Set<PSBinaryMetaEntry> filteredDbmeta = new HashSet<>();
         for (PSBinaryMetaEntry meta : dbmeta)
         {
            if (meta.getKey().isEnabled())
               filteredDbmeta.add(meta);
         }
         return filteredDbmeta;
      }
      else
         return dbmeta;
   }

   /**
    * Get the binary entity for the hash
    * @param hash
    * @return
    */
   public PSBinary getBinary(String hash)
   {

      PSBinary binary = hashDao.getBinary(hash);
      if (binary != null)
      {
         Date date = binary.getLastAccessedDate();
         Date today = DateUtils.truncate(new Date(), Calendar.DATE);
         // Truncate to day. We will only touch files once per day, and will
         // only
         // purge if older than day for safety.
         if (date == null || DateUtils.truncate(date, Calendar.DATE).getTime() < today.getTime())
         {
            binary.setLastAccessedDate(today);
         }
      }
      hashDao.save(binary);

      return binary;

   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileStorageService#touchAllHashes(java.util.Set)
    */
   @Transactional
   public void touchAllHashes(Set<PSHashedColumn> columns)
   {

      List<String> hashesToTouch = hashDao.getAllHashes(columns);
      List<String> hashesToTouchBatch = new ArrayList<>();
      for (Object result : hashesToTouch)
      {
         String hash = (String) result;
         hashesToTouchBatch.add(hash);
         log.debug("hash found =" + hash);
         if (hashesToTouchBatch.size()>=1000)
         {
            
            hashDao.touch(hashesToTouchBatch);
            hashesToTouchBatch.clear();
         }
      }
      if (hashesToTouchBatch.size() > 0)
         hashDao.touch(hashesToTouchBatch);

   }

   /**
    * Get the hashing algorithm. This cannot be modified after items are in the
    * system.
    */
   public IPSFileDigestService getDigestService()
   {
      return digestService;
   }

   /**
    * Defines which hashing algorithm to use. This cannot be modified after
    * items are in the system.
    * 
    * @param digestService
    */
   public void setDigestService(IPSFileDigestService digestService)
   {
      this.digestService = digestService;
   }

   /**
    * Generate an error message and add the error to the MetaData to make it
    * availiable to the user.
    * 
    * @param meta
    * @param e the Exception
    */
   private void errorGettingTikaMetadata(PSMeta meta, Throwable e)
   {
      String msg = e.getMessage();
      log.error("Error extracting Tika metadata " + msg, e);
      meta.setParseError(msg + ":" + getStackTrace(e));
   }

   /**
    * Delete the existing metadata for an item and recreate it using the known
    * original filename, type and encoding to help in extraction
    * 
    * @param binary
    * @return
    */
   @Transactional
   private boolean reparseMeta(PSBinary binary)
   {

      hashDao.deleteMeta(binary);

      InputStream fileData = binary.getData().getContent();
      PSPurgableTempFile storedFile = null;
      Set<PSBinaryMetaEntry> dbMeta = null;
      PSMeta meta = null;
      try
      {
         storedFile = createTempFileFromStream(fileData, binary.getMimeType(), binary.getOriginalFilename(),
               binary.getEncoding());
         meta = populateTempFileMeta(storedFile);
         meta = getMetaData(storedFile, meta);
         dbMeta = convertMetaToDbMeta(meta);
      }
      catch (FileNotFoundException e)
      {
         log.error("Cannot find file", e);
      }
      catch (IOException e)
      {
         log.error("IO Exception", e);
      }
      finally
      {
         IOUtils.closeQuietly(fileData);
      }
      if (dbMeta != null)
      {
         if (log.isDebugEnabled())
         {
            for (PSBinaryMetaEntry entry : dbMeta)
            {
               log.debug("entry id=" + entry.getKey().getId() + ":" + entry.getKey().getName());
            }
         }
         binary.setMetaEntries(dbMeta);
         binary.setExtractError(meta.getParseError() != null);
      }
      else
      {
         binary.setExtractError(true);
      }
      binary.setReparseMeta(false);
      hashDao.save(binary);
      return binary.isExtractError();
   }

   /**
    * Kick of a thread to process reparsing of items that have been marked in
    * the database. 
    */
   private synchronized boolean processReparseMeta()
   {
      if (reparseThread == null || !reparseThread.isAlive())
      {
         reparseThread = new ReparseThread(REPARSE_THREAD_NAME);
         reparseThread.start();
         return true;
      }
      return false;
   }

   @Override
   public void afterPropertiesSet() throws Exception
   {
      try
      {
         // This could be made configurable loading from a file if later
         // required.
         tikaConfig = new TikaConfig();
      }
      catch (MimeTypeException e)
      {
         log.error("Error initializing TikaConfig", e);
      }
      catch (IOException e)
      {
         log.error("Error initializing TikaConfig", e);
      }
      catch (TikaException e)
      {
         log.error("Error initializing TikaConfig", e);
      }
      
      //Check for auto install binaries e.g. fast forward
       importAllBinary(null);
      // Check if any any items still need re-parsing after startup;
       processReparseMeta();
      
   }

  
}
