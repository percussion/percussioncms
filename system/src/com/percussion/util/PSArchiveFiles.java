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

import org.apache.tools.ant.taskdefs.Zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for working with archives.
 */
public class PSArchiveFiles
{

   /**
    * Opens the specified archive file. The classes calling this method are
    * responsible for closing this file.
    *
    * @param archiveName The name of the archive.  This should include the path
    * also, must not be <code>null</code>.
    * @param type, The type of the archive. If it is <code>null</code> or not
    * equal to {@link #JAR_FILE_TYPE}, assumes archive is of type
    * <code>ZipFile</code>.
    *
    * @return archive file, never <code>null</code>.
    *
    * @throws IllegalArgumentException if archiveName is <code>null</code>.
    * @throws IOException if an error occurs opening the archive.
    */
   public static ZipFile openArchive(String archiveName, String type)
      throws IOException
   {
      // validate input
      if (archiveName == null)
         throw new IllegalArgumentException("arvhiveName may not be null");

      if(type == null)
         type = ZIP_FILE_TYPE;

      // open the zip file
      File archiveFile = new File(archiveName);

      if(type.equalsIgnoreCase(JAR_FILE_TYPE))
         return new JarFile(archiveFile);
      else
         return new ZipFile(archiveFile);

   }

   /**
    * Locates the specified file in the archive and returns it as an InputStream.
    *
    * @param archiveFile The archive file in which the file entry to be read.
    * May not be <code>null</code> and must not be closed.
    * @param name The name of the file to retrieve.  Must have the path same as
    * the path specified in archive for this entry.
    *
    * @return the inputstream.  Never <code>null</code>. Caller must close when
    * finished with it.
    *
    * @throws IllegalArgmumentException if any param is <code>null</code>.
    * @throws IOException if an error occurs reading from the archive or if
    * file is not found in archive.
    * @throws ZipException if a ZIP format error has occurred.
    * @throws IllegalStateException if archiveFile has been closed.
    */
   public static InputStream getFile(ZipFile archiveFile, String name)
      throws IOException, ZipException
   {
      // validate input
      if (archiveFile == null)
         throw new IllegalArgumentException(
            "Archive file to read may not be null.");

      if (name == null)
         throw new IllegalArgumentException("File entry name may not be null.");

      // get the zip entry for the specified file
      ZipEntry entry = archiveFile.getEntry(name);
      if (entry == null)
      {
         Enumeration<? extends ZipEntry> entries = archiveFile.entries();
         while (entries.hasMoreElements())
         {
            ZipEntry ent = entries.nextElement();
            System.out.println(ent.getName());
         }
         throw new IOException("Archive File Entry " + name + " not found in " +
            archiveFile.getName());

      }

      // get the inputstream for that entry
      return archiveFile.getInputStream(entry);
   }

   /**
    * Locates the specified file in the archive and returns it's size
    *
    * @param archiveFile The archive file in which the file entry to be read.
    * May not be <code>null</code> and must not be closed.
    * @param name The name of the file to retrieve.  Must have the path same as
    * the path specified in archive for this entry.
    *
    * @return the inputstream.  Never <code>null</code>. Caller must close when
    * finished with it.
    *
    * @throws IllegalArgmumentException if any param is <code>null</code>.
    * @throws IOException if an error occurs reading from the archive or if
    * file is not found in archive.
    * @throws ZipException if a ZIP format error has occurred.
    * @throws IllegalStateException if archiveFile has been closed.
    */
   public static int getFileSize(ZipFile archiveFile, String name)
      throws IOException, ZipException
   {
      if (archiveFile == null)
         throw new IllegalArgumentException(
               "Archive file to read may not be null.");

      if (name == null)
         throw new IllegalArgumentException("File entry name may not be null.");

      // get the zip entry for the specified file
      ZipEntry entry = archiveFile.getEntry(name);
      if (entry == null)
      {
         throw new IOException("Archive File Entry " + name + " not found in " +
            archiveFile.getName());
      }

      // get the inputstream for that entry
      return (int)entry.getSize();
   }

   
   /**
    * Creates the specified archive file and opens output stream to it.
    * The classes calling this method are responsible for closing the stream.
    *
    * @param archive The file to create. May not be <code>null</code>. If it
    * exists it will be replaced.
    * @param type, The type of the archive. If it is <code>null</code> or not
    * equal to {@link #JAR_FILE_TYPE}, assumes archive is of type
    * {@link #ZIP_FILE_TYPE}.
    * @param manifest the manifest which can be set to output stream if it is
    * not <code>null</code> and archive is of type {@link #JAR_FILE_TYPE},
    * otherwise it is ignored.
    *
    * @return the archive output stream, never <code>null</code>.
    *
    * @throws IllegalArgumentException if archive is <code>null</code>.
    * @throws IOExeption if archive cannot be created or cannot get output
    * stream  from it.
    */
   public static ZipOutputStream createArchive(File archive, String type,
      Manifest manifest)
      throws IOException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if(type == null)
         type = ZIP_FILE_TYPE;

      FileOutputStream fout = new FileOutputStream(archive);

      if(type.equalsIgnoreCase(JAR_FILE_TYPE))
      {
         if(manifest != null)
            return new JarOutputStream(fout, manifest);
         else
            return new JarOutputStream(fout);
      }
      else
         return new ZipOutputStream(fout);
   }

   /**
    * Adds passed in file or directory and files under the directory(if the
    * passed in file object is directory) to archive. If the <code>file</code>
    * is directory, <code>filter</code> can be passed to get list of files to
    * add.
    *
    * @param archiveOutputStream The archive stream to which files should be
    * added, may not be <code>null</code>.
    * @param file The file to add to archive, may not be <code>null</code>.
    * @param rootPath the path to which the file being added to archive should
    * be relative. If this is <code>null</code> or the file is not under one of
    * the directories of the <code>rootPath</code>, the passed in file's
    * canonical path is added as path to the entry in the archive.
    * @param filter the filter used to get list of files to add if the passed in
    * file is a directory. If this is <code>null</code> all files and
    * subdirectories under the passed in directory will be added.
    * @param out The names of files being added to archive are written to this
    * stream. Useful for debugging. If it is <code>null</code>, ignored logging.
    *
    * @throws IllegalArgumentException if <code>archiveOutputStream</code> or
    * <code>file</code> is <code>null</code>.
    * @throws IOException if file can not be added to archive.
    * @throws ZipException if a ZIP format error has occurred.
    **/
   public static void addFilesToArchive(ZipOutputStream archiveOutputStream,
      File file, String rootPath, FilenameFilter filter, OutputStream out)
      throws IOException, ZipException
   {
      if(archiveOutputStream == null)
         throw new IllegalArgumentException(
            "The archive stream to which files should be added may not be null."
            );

      if(file == null)
         throw new IllegalArgumentException(
            "The file or directory to be added may not be null."
            );

      String filePath = file.getCanonicalPath();

      if( rootPath != null && filePath.length() > rootPath.length() &&
          filePath.substring(0, rootPath.length()).equalsIgnoreCase(rootPath) )
      {
         filePath = filePath.substring(rootPath.length());
      }
      PrintWriter pw = null;
      if(out != null)
      {
         pw = new PrintWriter(out, true);
      }

      if(file.isDirectory())
      {
         File[] files;
         if(filter == null)
            files = file.listFiles();
         else
            files = file.listFiles(filter);

         if(files.length > 0)
         {
            for(int i = 0; i < files.length; i++)
            {
               addFilesToArchive(archiveOutputStream, files[i],
                  rootPath, filter, out);
            }
         }
         else
         {
            if(pw != null)
               pw.println(filePath);

            //add forward slash at the end to make the entry as directory entry
            if(!filePath.endsWith("/"))
               filePath += "/";
            archiveFile(archiveOutputStream, filePath, file);
         }
      }
      else {
         if(pw != null)
            pw.println(filePath);
         archiveFile(archiveOutputStream, filePath, file);
      }
   }

   /**
    * Creates the specified file or directory entry in the archive and copies
    * the contents to archive if it is a file.
    *
    * Version of {@link #archiveFile(ZipOutputStream, String, File)}
    * with an additional <code>extra</code> parameter described below.
    *
    * @param extra the value to be set in the optional extra field data for
    * the entry corresponding to the specified file entry, may be
    * <code>null</code>
    */
   public static void archiveFile(ZipOutputStream archiveOutputStream,
      String fileEntryPath, File sourceFile, byte[] extra)
      throws IOException, ZipException
   {
      if(archiveOutputStream == null)
         throw new IllegalArgumentException(
            "The archive stream to which file should be added may not be null."
            );

      if (fileEntryPath == null)
         throw new IllegalArgumentException(
            "The path for this file entry may not be null.");

      if (sourceFile == null)
         throw new IllegalArgumentException("The source file may not be null.");


      // Add entry to output stream.
      ZipEntry entry;
      if(archiveOutputStream instanceof JarOutputStream)
         entry = new JarEntry(fileEntryPath);
      else
         entry = new ZipEntry(fileEntryPath);

      if (extra != null)
         entry.setExtra(extra);

      archiveOutputStream.putNextEntry(entry);

      if(sourceFile.isFile())
      {
         // read in file and write it out
         FileInputStream in = new FileInputStream(sourceFile);

         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0)
            archiveOutputStream.write(buf, 0, len);

         in.close();
      }

      archiveOutputStream.closeEntry();
   }

   /**
    * Creates the specified file or directory entry in the archive and copies
    * the contents to archive if it is a file.
    *
    * @param archiveOutputStream The archive stream to which file should be
    * added, may not be <code>null</code>.
    * @param fileEntryPath The path with filename for the file entry that will
    * be created in the archive. Generally path should be a path relative to the
    * directory to which this archive need to be extracted, may not be
    * <code>null</code>.
    * @param sourceFile The file to add, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is <code>null</code>.
    * @throws IOException if the source file is not found or if there is an
    * error writing to the archive.
    * @throws ZipException if a ZIP format error has occurred.
    *
    * @see #createArchive for creating archive output stream.
    */
   public static void archiveFile(ZipOutputStream archiveOutputStream,
      String fileEntryPath, File sourceFile)
      throws IOException, ZipException
   {
      archiveFile(archiveOutputStream, fileEntryPath, sourceFile, null);
   }

    /**
    * Extracts all files in the archive to the specified directory. If an
    * exception happens in the process of extraction, it logs the message for
    * that entry and continues with the next entry.
    *
    * @param archiveFile The archive file from which files to be extracted, may
    * not be <code>null</code>.
    * @param extractDir The directory to which files to be extracted, may not be
    * <code>null</code> and should be a valid directory.
    * @param printFileNames if <code>true</code>, prints to console the files
    * extracting from archive otherwise not.
    * @param log The names of files being extracted from archive are logged to
    * this stream. Useful for debugging. If it is <code>null</code>, ignored
    * logging.
    *
    * @return error message log if any in the process of extracting files,
    * never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if any param is <code>null</code>.
    * @throws IOException if error happens in the process of extracting entries
    * of archive.
    * @throws ZipException if a ZIP format error has occurred.
    **/
   public static String extractFilesFromArchive(ZipFile archiveFile,
      String extractDir, OutputStream log)
      throws IOException, ZipException
   {
      if(archiveFile == null)
         throw new IllegalArgumentException(
            "Archive file to extract from can not be null.");

      if(extractDir == null)
         throw new IllegalArgumentException(
            "The directory to which files to be extracted can not be null.");

      File directory = new File(extractDir);
      if(!(directory.exists() && directory.isDirectory()))
         throw new IllegalArgumentException(
            "The directory to which files to be extracted must exist.");

      String errorMsg = "";

      PrintWriter pw = null;
      if(log != null)
      {
         pw = new PrintWriter(log, true);
         pw.println("Extracting files from archive");
      }

      for(Enumeration files=archiveFile.entries(); files.hasMoreElements(); )
      {
         ZipEntry entry = (ZipEntry)files.nextElement();
         StringBuffer errorBuf = new StringBuffer();

         // Check whether the directory exists for this file. If not, create it.
         String dir = "";
         String name = entry.getName();
         //don't extract manifest file
         if(name.equals(JarFile.MANIFEST_NAME))
            continue;

         if(pw != null)
            pw.println(name);

         if(entry.isDirectory())
         {
            //As directory entry ends with forward slash remove that
            dir = name.substring(0, name.length()-1);
         }
         else
         {
            int index = -1;
            if((index = name.lastIndexOf(File.separator)) != -1)
               dir = name.substring(0, index);
            else
               dir = "."; //to indicate extract to the extract base directory
         }

         if(!dir.equals("."))
         {
            File file = new File(extractDir, dir);
            if(!file.exists())
            {
               if(!file.mkdirs())
                  return "Could not make directory " + file.getCanonicalPath();
            }
         }

         //Since entry is directory, just return
         if(entry.isDirectory())
            continue;

         InputStream in = null;
         FileOutputStream out = null;

         try {
            in = archiveFile.getInputStream(entry);

            if(!extractDir.endsWith(File.separator))
               extractDir += File.separator;

            File file = new File(extractDir, entry.getName());
            if (!file.toPath().normalize().startsWith(extractDir))
               throw new IllegalArgumentException(
                       "Archive file to extract from is not having correct path.");
            out = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
               out.write(buf, 0, len);
            }

            in.close();
            out.close();
         }
         catch(IOException ioe){
            errorMsg = "Error extracting file " + entry.getName() + "\n";
            errorMsg += ioe.getLocalizedMessage() + "\n";
            try {
               if(in != null)
                  in.close();
            }
            catch(IOException e) {
               //do nothing
            }
            try {
               if(out != null)
                  out.close();
            }
            catch(IOException e) {
               //do nothing
            }
            return errorMsg;
         }
      }

      return errorMsg;
   }

   /**
    * Returns the extra field data for the entry corresponding to the supplied
    * file ref, or <code>null</code> if an enry for the supplied file ref
    * does not exist in the archive or the entry does not have extra field data.
    *
    * @param archiveFile The archive file from which extra field data is to be
    * retrieved.  May not be <code>null</code>.
    * @param archiveEntryPath The path of the file in the archive whose entry
    * stores extra field data.  May not be <code>null</code> or empty.
    *
    * @return the extra field data for the entry, or <code>null</code> if an
    * enry for the supplied file ref does not exist in the archive or the entry
    * does not have extra field data.
    *
    * @throws IllegalArgumentException if <code>archiveFile</code> is
    * <code>null</code> or <code>archiveEntryPath</code> is <code>null</code>
    * or empty
    */
   public static byte[] getExtra(ZipFile archiveFile, String archiveEntryPath)
   {
      if (archiveFile == null)
         throw new IllegalArgumentException(
            "Archive file may not be null.");

      if ((archiveEntryPath == null) || (archiveEntryPath.trim().length() < 1))
         throw new IllegalArgumentException(
            "archiveEntryPath may not be null or empty.");

      ZipEntry entry = archiveFile.getEntry(archiveEntryPath);
      if (entry != null)
         return entry.getExtra();
      return null;
   }


   /**
    * Sets the optional extra field data for the entry corresponding to the
    * supplied file ref.
    *
    * @param archiveFile The archive file in which extra field data is to be
    * set. May not be <code>null</code>.
    * @param archiveEntryPath The path of the file in the archive whose entry
    * will store the extra field data.  May not be <code>null</code> or empty.
    * @param extra the extra field data bytes to store in the archive, may
    * not be <code>null</code> and its length should not exceed 0xFFFFF bytes
    *
    * @throws IllegalArgumentException if <code>archiveFile</code> is
    * <code>null</code> or <code>archiveEntryPath</code> is <code>null</code>
    * or empty or if <code>extra</code> is <code>null</code> or if its
    * length is greater than 0xFFFFF bytes or if no entry is found in the
    * archive file corresponding to supplied file ref
    */
   public static void setExtra(ZipFile archiveFile, String archiveEntryPath, byte[] extra)
   {
      if (archiveFile == null)
         throw new IllegalArgumentException(
            "Archive file may not be null.");

      if ((archiveEntryPath == null) || (archiveEntryPath.trim().length() < 1))
         throw new IllegalArgumentException(
            "archiveEntryPath may not be null or empty.");

      if (extra == null)
         throw new IllegalArgumentException("extra may not be null");

      ZipEntry entry = archiveFile.getEntry(archiveEntryPath);
      if (entry != null)
         entry.setExtra(extra);
   }


   /** Constant to indicate 'zip' file type of archive file. **/
   public static final String ZIP_FILE_TYPE = "zip";

   /** Constant to indicate 'jar' file type of archive file. **/
   public static final String JAR_FILE_TYPE = "jar";
}
