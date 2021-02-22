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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.zip.CRC32;

/**
 * The IOTools class contains IO helper utilities.
 */
public class IOTools
{
   /**
    * Private constructor.
    *
    * This class doesn't need to ever be constructed.
    * It contains only static methods.
    */
   private IOTools()
   {
   }

   /**
    * Convenience method.
    * Copies all of the bytes from the InputStream to the
    * OutputStream using a default buffer size of 8k.
    *
    * @see  #copyStream(InputStream, OutputStream, int)
    */
   public static long copyStream(InputStream in, OutputStream out)
      throws IOException
   {
      return copyStream(in, out, 8192);
   }
   
   /**
    * Convenience method.
    * Copies all of the bytes from the InputStream to the
    * OutputStream using a default buffer size of 8k.
    *
    * @see  #copyStream(InputStream, OutputStream, int)
    */
   public static long copyStream(InputStream in, OutputStream out, int bufSize)
      throws IOException
   {
      return copyStream(in, out, bufSize, -1L);
   }

   /**
    * Convenience method.
    * Copies all of the bytes from the InputStream to the
    * out file.
    * 
    * @param in Never <code>null</code> caller is responsible to
    *           close.
    * @param outFile Never <code>null</code>.
    * @return The number of bytes copied.
    * @throws IOException
    */
   public static long copyStreamToFile(InputStream in, File outFile)
      throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException(
            "Supplied input stream must not be null.");
      if (outFile == null)
         throw new IllegalArgumentException(
            "Supplied output file must not be null.");
      
      long copiedBytes = 0L;
      try(FileOutputStream out = new FileOutputStream(outFile)){
         copiedBytes =  copyStream(in, out);
      }

      return copiedBytes;
   }

   /**
    * Copies all of the bytes from the InputStream to the
    * OutputStream.  The output buffer will be flushed, but 
    * neither stream will be closed by this method.  It is
    * the responsibility of the caller to close both streams.
    *
    * @param   in  The input stream to get bytes from.
    *             Never <code>null</code>.
    *
    * @param   out The output stream to send bytes to.
    *             Never <code>null</code>.
    *
    * @param   bufSize The number of bytes to transfer
    *             at a time.
    * 
    * @param   limit the number of bytes copied, if  -1 then no limit.
    *
    * @return  The number of bytes transferred.
    * 
    * @throws  IOException  If an I/O exception occurs during stream
    *          processing.
    *
    * @throws  IllegalArgumentException If any argument is invalid.
    */
   public static long copyStream(
      InputStream in, OutputStream out, int bufSize, long limit)
      throws IOException
   {
      if (bufSize <= 0)
         bufSize = 8192;   // Default to 8k.

      byte[] buf = new byte[bufSize];

      long bytesSent = 0L;

      if (in == null || out == null)
         throw new IllegalArgumentException(
            "Supplied streams must not be null.");

      while (true)
      {
         // Adjust buffer size so we don't exceed limit
         if(limit != -1L && (limit - bytesSent) < bufSize)
            buf = new byte[(int)(limit - bytesSent)];
            
         int read = in.read(buf);
         
         if (read < 0)
            break; // end of input stream reached

         out.write(buf, 0, read);

         bytesSent += read;
         
         if(limit != -1L && bytesSent >= limit)
            break;
      }

      out.flush();

      return bytesSent;
   }

   /**
    * Convenience method.
    * Copies all of the bytes from the InputStream to a
    * Writer with an 8k buffer.
    *
    * @see #writeStream(Reader, Writer, int)
    */
   public static long writeStream(Reader in, Writer out)
      throws IOException
   {
      return writeStream(in, out, 8192);
   }

  /**
    * Copies all of the characters from the supplied Reader
    * to a Writer.  The output buffer will be flushed, but 
    * neither stream will be closed by this method.  It is
    * the responsibility of the caller to close both streams.
    *
    * @param   in  The input reader to get characters from.
    *             Never <code>null</code>.
    *
    * @param   out The output writer to send characters to.
    *             Never <code>null</code>.
    *
    * @param   bufSize The number of characters to transfer
    *          at a time.  If the value specified is zero or
    *          negative, the default size (8k) will be used.
    *          
    * @return  The number of characters transferred.
    * 
    * @throws  IOException  If an I/O exception occurs during stream
    *          processing.
    *
    * @throws  IllegalArgumentException If any argument is invalid.
    */
   public static long writeStream(Reader in, Writer out, int bufSize)
      throws IOException
   {
      if (bufSize <= 0)
         bufSize = 8192;   // Default to 8k.

      char[] buf = new char[bufSize];


      if (in == null || out == null)
         throw new IllegalArgumentException(
            "Reader and Writer must be supplied.");

      long charsSent = 0L;

      while (true)
      {
         int read = in.read(buf);
         
         if (read < 0)
            break; // end of input stream reached

         out.write(buf, 0, read);

         charsSent += read;
      }

      out.flush();

      return charsSent;
   }
   
   /**
    * Compares two streams for equality byte-by-byte.
    * 
    * @param alpha compared to <code>bravo</code>, not <code>null</code>. 
    * @param bravo compared to <code>alpha</code>, not <code>null</code>.
    * 
    * @return <code>true</code> if the two streams are the same size and
    * identical; <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if either parameter is <code>null</code>.
    * @throws IOException if a problem occurs reading from the streams.
    */ 
   public static boolean compareStreams(InputStream alpha, InputStream bravo)
      throws IOException
   {
      if (null == alpha || null == bravo)
         throw new IllegalArgumentException("Neither stream may be null");
      
      boolean equal = true;
      boolean endofbothstreams = false;
      int byteIndex = -1; // tracks position in the stream, useful for debugging
      
      while (equal && !endofbothstreams)
      {
         byteIndex++;
         int alpha_byte = alpha.read();
         int bravo_byte = bravo.read();
         
         // if a stream has ended, its byte will equal -1
         if (alpha_byte == -1 && bravo_byte == -1)
            endofbothstreams = true;
         else if (alpha_byte != bravo_byte)
         {
            equal = false;
            
            // useful code when debugging (commented out for production)
            /*
            System.out.println( "\tDifference at byte 0x" + 
               Integer.toHexString( byteIndex ) + ": 0x" + 
               Integer.toHexString( alpha_byte ) + " != 0x" +
               Integer.toHexString( bravo_byte ) );
            */
         }
      }
      
      return equal;
   }   

   
   /**
    * Compares the characters read by two readers for equality.
    * 
    * @param alpha compared to <code>bravo</code>, not <code>null</code>. 
    * @param bravo compared to <code>alpha</code>, not <code>null</code>.
    * 
    * @return <code>true</code> if the two readers are the same size and have
    * identical characters; <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if either parameter is <code>null</code>.
    * @throws IOException if an error occurs while reading.
    */ 
   public static boolean compareReaders(Reader alpha, Reader bravo)
      throws IOException
   {
      if (null == alpha || null == bravo)
         throw new IllegalArgumentException("Neither reader may be null");
      
      boolean equal = true;
      boolean endofboth = false;
      int index = -1; // tracks position in the stream, useful for debugging
      
      while (equal && !endofboth)
      {
         index++;
         int alpha_char = alpha.read();
         int bravo_char = bravo.read();
         
         // if a stream has ended, its byte will equal -1
         if (alpha_char == -1 && bravo_char == -1)
            endofboth = true;
         else if (alpha_char != bravo_char)
         {
            equal = false;
            
            // useful code when debugging (commented out for production)
            System.out.println( "\tDifference at char 0x" + 
               Integer.toHexString( index ) + ": 0x" + 
               Integer.toHexString( alpha_char ) + " != 0x" +
               Integer.toHexString( bravo_char ) );
         }
      }
      
      return equal;
   }   
   
   /**
    * Copy file helper.  Note that the underlying method of copying may result
    * in a lock on the files for a few seconds following the copy.
    * 
    * @param source never <code>null</code>.
    * @param dest never <code>null</code>.
    * @throws IOException on any file error.
    */
   public static void copyFile(File source, File dest) throws IOException 
   {
      if (source==null)
         throw new IllegalArgumentException("source may not be null");
      if (dest==null)
         throw new IllegalArgumentException("dest may not be null");
      
      FileChannel in = null, out = null;
      try {
         in = new FileInputStream(source).getChannel();
         out = new FileOutputStream(dest).getChannel();

         long size = in.size();
         MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);

         out.write(buf);
     
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
         if (out != null)
         {
            out.close();
         }
      }
   }
   
   /**
    * Copy file helper.  Uses streams instead of channels.
    * @param source never <code>null</code>.
    * @param dest never <code>null</code>.
    * @throws IOException on any file error.
    */
   public static void copyFileStreams(File source, File dest) throws IOException 
   {
      if (source==null)
         throw new IllegalArgumentException("source may not be null");
      if (dest==null)
         throw new IllegalArgumentException("dest may not be null");
      
      FileInputStream in = null;
      FileOutputStream out = null;
      try
      {
         in = new FileInputStream(source);
         out = new FileOutputStream(dest);
         copyStream(in, out);
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
         if (out != null)
         {
            out.close();
         }
      }
   }
   
   /**
    * Copies the provided file to the provided directory.
    * If the provided file is a directory copies it recursively.
    * If any of the copied files already exist they are overwritten. 
    * 
    * @param file file to copy, never <code>null</code>.
    * @param targetDir directory to create copies in, never <code>null</code>.
    */
   public static void copyToDir(File file, File targetDir) throws IOException
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
      if (targetDir == null)
         throw new IllegalArgumentException("targetDir may not be null");
            
      final File copy = new File(targetDir, file.getName());
      if (file.isFile())
      {
         copyFileStreams(file, copy);
      }
      else
      {
         assert file.isDirectory();
         copy.mkdir();
         for (final File childFile : file.listFiles())
         {
            copyToDir(childFile, copy);
         }
      }
   }

   /**
    * Copies the contents of the directory to the provided directories.
    *  
    * @param dir directory to copy, never <code>null</code>.
    * @param targetDirs list of directories to create copies in, never
    * <code>null</code>.
    */
   public static void copyToDirs(File dir, List targetDirs) throws IOException
   {
      if (dir == null)
         throw new IllegalArgumentException("dir may not be null");
      if (targetDirs == null)
         throw new IllegalArgumentException("targetDirs may not be null");
      
      File[] copyFiles = dir.listFiles();
      for (int i = 0; i < copyFiles.length; i++)
      {
         File srcFile = copyFiles[i];
         for (int j = 0; j < targetDirs.size(); j++)
         {
            File destDir = (File) targetDirs.get(j);
            copyToDir(srcFile, destDir);
         }
      }
   }
   
   /**
    * Deletes file or directory. If directory is not empty recursively deletes it.
    * @param file file to delete.
    */
   public static void deleteFile(File file)
   {
      if (file.isDirectory()) {
         for (final String childFile : file.list())
        {
            deleteFile(new File(file, childFile));
        }
     }
 
     // The directory is now empty so delete it
     file.delete();
   }
   
   /**
    * Get the file content as string (utf-8 encoded).
    * 
    * @param file file to read content from, may not be <code>null</code>.
    * @return content as string, never <code>null</code>, may be empty.
    * @throws IOException
    */
   public static String getFileContent(File file) throws IOException
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
      
      InputStream is = null;      
      try
      {
         is = new FileInputStream(file);
         return getContent(is);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
            }
         }
         
      }
   }
   
   /**
    * Get the stream content as string (utf-8 encoded). It is
    * the responsibility of the caller to close the stream.
    * 
    * @param stream stream to read content from, may not be <code>null</code>.
    * @return content as string, never <code>null</code>, may be empty.
    * @throws IOException
    */
   public static String getContent(InputStream stream) throws IOException
   {
      if (stream == null)
         throw new IllegalArgumentException("stream may not be null");
      
      InputStream is = null;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try
      {
         IOTools.copyStream(stream, bos);
         bos.flush();
         return bos.toString("UTF8");
      }
      finally
      {         
         if (bos != null)
         {
            try
            {
               bos.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }
   
   
   /**
    * Creates a copy of the given file in the default temporary directory
    * 
    * @param file the file to copy, may not be <code>null</code>
    * 
    * @return the temporary file
    * @throws IOException 
    */
   public static File createTempFile(File file) throws IOException
   {
      if (file == null)
      {
         throw new IllegalArgumentException("file may not be null");
      }
      
      String name = file.getName();
      String prefix = name;
      String suffix = null;
      int dotIndex = name.indexOf(".");
      if (dotIndex != -1)
      {
         prefix = name.substring(0, dotIndex);
         suffix = name.substring(dotIndex + 1);
      }
      
      File tempFile = File.createTempFile(prefix, suffix);
      IOTools.copyFileStreams(file, tempFile);
      
      return tempFile;
   }
   
   /**
    * Creates a backup of the given file by appending .000, .001,
    * etc. to its name.
    *
    * @param file the resulting backup file will be file.000,...,etc., may not
    * be <code>null</code> and must exist.
    * @return a file reference to the newly created backup.
    * @throws IOException
    */
   public static File createBackupFile(File file) throws IOException
   {
      if (file == null || !file.exists())
      {
         throw new IllegalArgumentException("file must not be null and must exist");
      }
      
      String name = file.getName();
      int dotIndex = name.lastIndexOf(".");
      if (dotIndex != -1)
         name = name.substring(0, dotIndex);
                  
      File parentFile = file.getParentFile();
      File backupFile = new File(parentFile, name + ".000");
      int backupNum = 1;
      
      while (backupFile.exists())
      {
         String backupStr = backupNum + "";
         if (backupStr.length() == 1)
            backupStr = "00" + backupStr;
         else if (backupStr.length() == 2)
            backupStr = "0" + backupStr;
         
         backupFile = new File(parentFile, name + "." + backupStr);
         backupNum++;
      }
      
      copyFileStreams(file, backupFile);
      return backupFile;
   }
   
   /**
    * Get the checksum for the supplied file as a CRC-32 value.
    * 
    * @param file The file, may not be <code>null</code>.
    * @return The checksum of the file.
    * @throws IOException on any file error. 
    */
   public static long getChecksum(File file) throws IOException
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
      
      String content = getFileContent(file);
                 
      return getChecksum(content);
   }
   
   /**
    * Get the checksum for the supplied string as a CRC-32 value.
    * 
    * @param str The string, may not be <code>null</code>.
    * @return The checksum of the file.
    */
   public static long getChecksum(String str)
   {
      if (str == null)
         throw new IllegalArgumentException("str may not be null");
      
      byte[] bytes = str.getBytes();
      
      CRC32 crc = new CRC32();
      crc.update(bytes);
            
      return crc.getValue();
   }
}
