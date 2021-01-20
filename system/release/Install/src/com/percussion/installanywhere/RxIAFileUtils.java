/******************************************************************************
 *
 * [ RxIAFileUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installanywhere;

import com.installshield.util.FileUtils;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.file.FileService;
import com.percussion.installer.action.RxLogger;
import com.percussion.utils.string.PSStringUtils;

import java.io.IOException;
import java.net.URL;


/**
 * This is a wrapper class for the install platform file utility class which
 * provides file system operation functionality.
 * 
 * @author peterfrontiero
 */
public class RxIAFileUtils
{
   /**
    * Creates a normalized file name.  Calls
    * {@link FileUtils#normalizeFileName(java.lang.String)}.
    * 
    * @param file the path of the file to be normalized, may not be
    * <code>null</code> or empty.
    * 
    * @return the normalized file path.
    */
   public static String normalizeFileName(String file)
   {
      PSStringUtils.notBlank(file);
      
      return FileUtils.normalizeFileName(file);
   }
   
   /**
    * Creates a normalized file name.  Calls
    * {@link FileUtils#normalizeFileName(java.lang.String, char)}.
    * 
    * @param file the path of the file to be normalized, may not be
    * <code>null</code> or empty.
    * @param normalSeparator the separator character to use for normalization,
    * may not be <code>null</code> or empty.
    * 
    * @return the normalized file path.
    */
   public static String normalizeFileName(String file, char normalSeparator)
   {
      PSStringUtils.notBlank(file);
      PSStringUtils.notBlank("" + normalSeparator);
         
      return FileUtils.normalizeFileName(file, normalSeparator);
   }
   
   /**
    * Creates a temporary file.  Calls
    * {@link FileUtils#createTempFile(java.net.URL, java.lang.String)}.
    * 
    * @param resource the source for the temporary file, may not be
    * <code>null</code>.
    * @param name a temporary file will be created with this name, may not be
    * <code>null</code> or empty.
    * 
    * @return the path of the temporary file.
    * @throws IOException if an error occurs loading the resource or creating
    * the file.
    */
   public static String createTempFile(URL resource, String name) throws
   IOException
   {
      if (resource == null)
         throw new IllegalArgumentException("resource may not be null");
      
      PSStringUtils.notBlank(name);
      
      return FileUtils.createTempFile(resource, name);
   }
   
   /**
    * Retrieves the name of the specified file.  Calls
    * {@link FileUtils#getName(java.lang.String)}.
    * 
    * @param file the path of the file or directory, may not be
    * <code>null</code> or empty.
    * 
    * @return the name of the file or directory.
    */
   public static String getName(String file)
   {
      PSStringUtils.notBlank(file);
      
      return FileUtils.getName(file);
   }
   
   /**
    * Sets a given file to be executable on the current system.
    * Calls {@link FileService#setFileExecutable(java.lang.String)}.
    * 
    * @param fs the {@link FileService} object used to perform the operation,
    * may not be <code>null</code>.
    * @param file the path of the file, may not be <code>null</code> or empty.
    */
   public static void setFileExecutable(FileService fs, String file)
   {
      if (fs == null)
         throw new IllegalArgumentException("fs may not be null");
      
      PSStringUtils.notBlank(file);
      
      try
      {
         fs.setFileExecutable(file);
      }
      catch (ServiceException e)
      {
         RxLogger.logError("RxIAFileUtils#setFileExecutable : " +
               e.getMessage());
      }
   }
   
   /**
    * Determines if a given file or directory exists on the current system.
    * Calls {@link FileService#fileExists(java.lang.String)}.
    * 
    * @param fs the {@link FileService} object used to perform the operation,
    * may not be <code>null</code>.
    * @param file the path of the file or directory, may not be
    * <code>null</code> or empty.
    * 
    * @return <code>true</code> if the file exists, <code>false</code>
    * otherwise or if an error occurred.
    */
   public static boolean fileExists(FileService fs, String file)
   {
      if (fs == null)
         throw new IllegalArgumentException("fs may not be null");
      
      PSStringUtils.notBlank(file);
           
      try
      {
         return fs.fileExists(file);
      }
      catch (ServiceException e)
      {
         RxLogger.logError("RxIAFileUtils#fileExists : " + e.getMessage());
      }
      
      return false;
   }
   
   /**
    * Retrieves the parent of the specified file.  Calls
    * {@link FileService#getParent(java.lang.String)}.
    * 
    * @param fs the {@link FileService} object used to perform the operation,
    * may not be <code>null</code>.
    * @param file the path of the file or directory, may not be
    * <code>null</code> or empty.
    * 
    * @return the parent directory of the given file, empty if an error
    * occurred.
    */
   public static String getParent(FileService fs, String file)
   {
      if (fs == null)
         throw new IllegalArgumentException("fs may not be null");
      
      PSStringUtils.notBlank(file);
           
      try
      {
         return fs.getParent(file);
      }
      catch (ServiceException e)
      {
         RxLogger.logError("RxIAFileUtils#getParent : " + e.getMessage());
      }
      
      return "";
   }
   
   /**
    * Determines if a given directory is writable on the current system.  Calls
    * {@link FileService#isDirectoryWritable(java.lang.String)}.
    * 
    * @param fs the {@link FileService} object used to perform the operation,
    * may not be <code>null</code>.
    * @param file the path of the directory, may not be <code>null</code> or
    * empty.
    * 
    * @return <code>true</code> if the directory is writable, <code>false</code>
    * otherwise or if an error occurred.
    */
   public static boolean isDirectoryWritable(FileService fs, String file)
   {
      if (fs == null)
         throw new IllegalArgumentException("fs may not be null");
      
      PSStringUtils.notBlank(file);
           
      try
      {
         return fs.isDirectoryWritable(file);
      }
      catch (ServiceException e)
      {
         RxLogger.logError("RxIAFileUtils#fileExists : " + e.getMessage());
      }
      
      return false;
   }
   
   /**
    * Creates a directory on the current system.  Calls
    * {@link FileService#createDirectory(java.lang.String)}.
    * 
    * @param fs the {@link FileService} object used to perform the operation,
    * may not be <code>null</code>.
    * @param dir the path of the directory, may not be <code>null</code> or
    * empty.
    */
   public static void createDirectory(FileService fs, String dir)
   {
      if (fs == null)
         throw new IllegalArgumentException("fs may not be null");
      
      PSStringUtils.notBlank(dir);
           
      try
      {
         fs.createDirectory(dir);
      }
      catch (ServiceException e)
      {
         RxLogger.logError("RxIAFileUtils#createDirectory : " + e.getMessage());
      }
   }
   
   /**
    * Deletes a directory on the current system.  Calls
    * {@link FileService#deleteDirectory(java.lang.String)}.
    * 
    * @param fs the {@link FileService} object used to perform the operation,
    * may not be <code>null</code>.
    * @param dir the path of the directory, may not be <code>null</code> or
    * empty.
    */
   public static void deleteDirectory(FileService fs, String dir)
   {
      if (fs == null)
         throw new IllegalArgumentException("fs may not be null");
      
      PSStringUtils.notBlank(dir);
           
      try
      {
         fs.createDirectory(dir);
      }
      catch (ServiceException e)
      {
         RxLogger.logError("RxIAFileUtils#deleteDirectory : " + e.getMessage());
      }
   }
   
   /**
    * Retrieves the temporary directory of the system.  Calls
    * {@link FileUtils#getTempDir()}.
    *
    * @return the location of the system's temporary directory.
    */
   public static String getTempDir()
   {
      return FileUtils.getTempDir();            
   }
   
   /**
    * Attempts to map a given path to one of the provided partitions.  Calls
    * {@link FileService#getPartitionName(
    * java.lang.String, java.lang.String[])}.
    * 
    * @param fs the {@link FileService} object} used to perform the operation,
    * may not be <code>null</code>.
    * @param path absolute path which will be mapped, may not be
    * <code>null</code> or empty.
    * @param parts the set of partitions, never <code>null</code>.
    * 
    * @return the partition to which the path has been matched, or the path
    * itself if a match was not found.  May be <code>null</code> if an error
    * occurred.
    */
   public static String getPartitionName(FileService fs, String path,
         String[] parts)
   {
      String partitionName = null;   
     
      try
      {
         partitionName = fs.getPartitionName(path, parts);
      }
      catch (ServiceException e)
      {
         RxLogger.logError("RxIAFileUtils#getPartitionName : " +
               e.getMessage());
      }
      
      return partitionName;
   }
}
