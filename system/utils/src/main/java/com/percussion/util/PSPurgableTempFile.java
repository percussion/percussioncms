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

package com.percussion.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class is used to create a temporary file which is deleted when
 * it is garbage collected. Consider to use <code>PSPurgableFileInputStream
 * </code> if need to delete the file upon closing the input stream.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSPurgableTempFile extends File implements AutoCloseable
{
   /**
    * Convenience constructor that calls {@link #PSPurgableTempFile(String, 
    * String, File, String, String, String) PSPurgableTempFile(prefix, 
    * suffix, dir, null, null, null)}.
    */
   public PSPurgableTempFile(String prefix, String suffix, File dir)
      throws IOException
   {
      this(prefix, suffix, dir, null, null, null);
   }

   /**
    * Construct a purgable temp file storing additional information
    * about the source.
    *
    * @param prefix the prefix to use for the temp file name, must be at 
    *    least 3 characters.
    * @param suffix the suffix to use for the temp file name, may be 
    *    <code>null</code> to use the default <code>.tmp</code>.
    * @param dir the directory to create the file in, may be <code>null</code> 
    *    to use the default temp dir.
    * @param sourceFile the fully qualified path if the source of this 
    *    tempfile is a file. May be <code>null</code> or <code>zero</code> 
    *    length if the source is not a file or not known.
    * @param contentType the type of content the source, may be 
    *    <code>null</code>. The character set is often defined in the content 
    *    type string.
    * @param encType the method used to encode this file, may be 
    *    <code>null</code> if this is a binary file or if the encoding is 
    *    not known. This is not the character set encoding for the file.
    * @throws IOException if any IO operation fails.
    */
   public PSPurgableTempFile(String prefix, String suffix, File dir,
      String sourceFile, String contentType, String encType)
      throws IOException
   {
      super(calculatePath(prefix,suffix,dir));

      m_contentProperties = new HashMap();

      try
      {
         if (contentType != null)
            m_contentType = PSBaseHttpUtils.parseContentType(contentType,
               m_contentProperties);
      }
      catch (IllegalArgumentException e)
      {
         // no-op we just won't have the info
      }

      m_sourceFile = sourceFile;
      
      m_encType = encType;
   }

   /**
    * Calculate the path. If the directory is specified then we just use it
    * (or rather {@link File#createTempFile(java.lang.String, java.lang.String, java.io.File)}
    * will use it) after creating the directory if it doesn't exist. If not then
    * we'll use the system temp directory, extracted on class load.
    * @param prefix the prefix, see the ctor for details
    * @param suffix the suffix, see the ctor for details
    * @param dir the directory, may be <code>null</code>
    * @return the computed path
    * @throws IOException
    */
   private static String calculatePath(String prefix, String suffix, File dir)
         throws IOException
   {
      if (dir == null)
      {
         dir = ms_psxTempDirectory;
      }
      File tempFile = null;
      // Instead of checking folder existance every time
      // If we fail to create the temp file then try to 
      // check the folder.  Ony need to synchronize when trying
      // to fixup folder for first time.
      try
      {
         tempFile = createTempFile(prefix, suffix, dir);
      } catch (IOException e)
      {
         synchronized(makeTempLock)
         {
            try {
               if (!dir.exists())
                  dir.mkdirs();
               tempFile = createTempFile(prefix, suffix, dir);
            } catch (IOException ex)
            {
               throw new IllegalArgumentException("Cannot create tempfile in dir "+dir.getCanonicalPath(), ex);
            }
         }
         
      }
      return tempFile.getAbsolutePath();
   }

   /**
    * Sets up the temp file directory structure if necessary.
    * @throws IOException
    */
   private static void createTempFileDir() throws IOException
   {      
      if (!ms_psxTempDirectory.exists())
      {
         ms_psxTempDirectory.mkdirs();
      }
   }
   
   /**
    * Returns the name of the source file of this
    * file, or <CODE>null</CODE> if it is not known.
    *
    * @return   the content type as a string
    */
   public String getSourceFileName()
   {
      return m_sourceFile;
   }
   
   /**
    * Returns the content type of the source file of this
    * file, or <CODE>null</CODE> if it is not known.
    *
    * @return   the content type as a string
    */
   public String getSourceContentType()
   {
      return m_contentType;
   }

    public void setSourceContentType(String contentType) {
        this.m_contentType = contentType;
    }

    /**
    * Returns the name of the encoding scheme (the content-transfer-encoding,
    * not charset encoding) used to create this file, or <CODE>null</CODE>
    * if the encoding is not known.
    *
    * @return   the transfer encoding scheme if known; <CODE>null</CODE>
    *          otherwise
    */
   public String getTransferEncoding()
   {
      return m_encType;
   }

   /**
    * Returns the name of the character set used to encode this text
    * file, or <CODE>null</CODE> if this is a binary file or if the
    * encoding is not known.
    *
    * @return   the encoding type as a string
    */
   public String getCharacterSetEncoding()
   {
      return (String) m_contentProperties.get("charset");
   }

   /**
    * Deletes the file if exists. This method should be called after the file
    * is no longer needed.
    */
   public void release()
   {
      // if the file hasn't been deleted, do so now
      if (!m_isDeleted)
      {
            try {
               m_isDeleted = delete();
               if(!m_isDeleted)
                  ms_log.debug("Could not delete tempfile "+this.getAbsolutePath(),new Throwable("STACKTRACE"));
               
            } catch (Exception e)
            {
               ms_log.debug("Could not release temp file "+this.getAbsolutePath(),e);
            }
      }
   }

   /**
    * Fully qualified path of the source of this file if it is known.
    */
   private String m_sourceFile = null;
   
   /**
    * Content type of the source of this file if it is known.
    */
   private String m_contentType = null;

   /**
    * Encoding type of the source of this file if it is known.
    */
   private String m_encType = null;

   /**
    * Indicates if the temp file has been deleted, this is to avoid
    * to check whether the file exists more than once. Initialize to
    * <code>false</code>, only modified once by {@link #release()}
    */
   private boolean m_isDeleted = false;

   /**
    * The content properties associated with this file.  Determined
    * at initialization time by parsing the content-type string for
    * possible attributes.  Never modified after construction.  Never
    * <code>null</code> after construction.  May be empty.
    */
   HashMap m_contentProperties = null;
   
   /**
    * The temp directory is calculated based on the user's configuration. This 
    * is done on class load. The directory is a subdirectory of the temp dir,
    * which allows us to delete all the contained files on init without worrying 
    * that they belong to another application. Never <code>null</code> after
    * initialization, and never modified after initialization.
    */
   private static File ms_psxTempDirectory = null;

   /**
    * On first load, calculate the temp dir location and cleanup any leftover
    * temp files in that directory.
    */
   static
   {
      try
      {
         File temp = createTempFile("temp","tmp");
         ms_psxTempDirectory = new File(temp.getParentFile(), "rxtemp" + "."
               + System.getProperty("user.name"));
         createTempFileDir();
         
         // Clear all files in temp directory
         File tempfiles[] = ms_psxTempDirectory.listFiles();
         for(int i = 0; i < tempfiles.length; i++)
         {
            tempfiles[i].delete();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }      
   }
   

   /**
    * logger
    */
   private static Log ms_log = LogFactory.getLog(PSPurgableTempFile.class);
   
   
   private static Object makeTempLock = new Object();
   /**
    * The id used for serialization.
    */
   private static final long serialVersionUID = 5509791791093541540L;

   /**
    * Closes this resource, relinquishing any underlying resources.
    * This method is invoked automatically on objects managed by the
    * {@code try}-with-resources statement.
    *
    * <p>While this interface method is declared to throw {@code
    * Exception}, implementers are <em>strongly</em> encouraged to
    * declare concrete implementations of the {@code close} method to
    * throw more specific exceptions, or to throw no exception at all
    * if the close operation cannot fail.
    *
    * <p> Cases where the close operation may fail require careful
    * attention by implementers. It is strongly advised to relinquish
    * the underlying resources and to internally <em>mark</em> the
    * resource as closed, prior to throwing the exception. The {@code
    * close} method is unlikely to be invoked more than once and so
    * this ensures that the resources are released in a timely manner.
    * Furthermore it reduces problems that could arise when the resource
    * wraps, or is wrapped, by another resource.
    *
    * <p><em>Implementers of this interface are also strongly advised
    * to not have the {@code close} method throw {@link
    * InterruptedException}.</em>
    * <p>
    * This exception interacts with a thread's interrupted status,
    * and runtime misbehavior is likely to occur if an {@code
    * InterruptedException} is {@linkplain Throwable#addSuppressed
    * suppressed}.
    * <p>
    * More generally, if it would cause problems for an
    * exception to be suppressed, the {@code AutoCloseable.close}
    * method should not throw it.
    *
    * <p>Note that unlike the {@link Closeable#close close}
    * method of {@link Closeable}, this {@code close} method
    * is <em>not</em> required to be idempotent.  In other words,
    * calling this {@code close} method more than once may have some
    * visible side effect, unlike {@code Closeable.close} which is
    * required to have no effect if called more than once.
    * <p>
    * However, implementers of this interface are strongly encouraged
    * to make their {@code close} methods idempotent.
    *
    * @throws Exception if this resource cannot be closed
    */
   @Override
   public void close() throws Exception {
      release();
   }
}
