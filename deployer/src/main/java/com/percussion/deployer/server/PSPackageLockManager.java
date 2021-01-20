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

package com.percussion.deployer.server;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveManifest;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.IOTools;
import com.percussion.util.PSArchiveFiles;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipFile;

/**
 * An application that manages the lock of one or more specified packages.  The
 * lock of a package translates to an "editable" flag in the package's archive
 * info.  See {@link PSArchiveInfo#isEditable()}.
 */
public class PSPackageLockManager
{
   /**
    * Invokes this application, must be run from the Rhythmyx root directory.
    *  
    * @param args Expects the following:
    * <ul>
    * <li>-lock/unlock - (arg is case-insensitive) will lock/unlock all package
    * files specified by the proceeding argument, which may either be an
    * absolute or relative path to a package file or directory of package files
    * </li>
    * <li>-h[elp] - will display the commandline help (arg is case-insenstive)
    * </li>
    * </ul>  
    */
   public static void main(String[] args)
   {
      boolean lock = false;
      String filePath = null;
      
      if (args.length > 0)
      {
         String arg = args[0];
         if (arg.equalsIgnoreCase("-lock") || arg.equalsIgnoreCase("-unlock"))
         {
            lock = arg.equalsIgnoreCase("-lock") ? true : false;
            
            if (args.length > 1)
            {
               filePath = args[1];
            }
            else
            {
               showUsageAndExit();
            }
         }
         else if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("-help"))
         {
            showUsageAndExit();
         }
         else
         {
            showUsageAndExit();
         }
      }
      else
      {
         showUsageAndExit();
      }
          
      try
      {
         PSPackageLockManager manager = new PSPackageLockManager();
         manager.update(new File(filePath), lock);
      }
      catch (Exception e)
      {
         System.err.println("Error encountered during conversion");
         e.printStackTrace(System.err);
      }
   }

   /**
    * Recursive method which updates the lock of the specified package or
    * packages.
    * 
    * @param packageFile The package file or directory of package files to be
    * updated.  May not be <code>null</code>.
    * @param lock <code>true</code> to lock the package(s), <code>false</code>
    * to unlock the package(s). 
    * 
    * @throws PSUnknownNodeTypeException If there is a problem with an XML file
    * format.
    * @throws SAXException If the XML doc is malformed.
    * @throws IOException If there is an error reading from a file.
    * @throws PSDeployException If there is an archive error.
    */
   @SuppressWarnings("unchecked")
   public void update(File packageFile, boolean lock) 
      throws IOException, SAXException, PSDeployException,
      PSUnknownNodeTypeException
   {
      if (packageFile == null)
      {
         throw new IllegalArgumentException("packageFile may not be null");
      }
      
      if (packageFile.isDirectory())
      {
         File[] files = packageFile.listFiles();
         for (File file : files)
         {         
             update(file, lock);
         }
      }
      else
      {
         if (!packageFile.getName().endsWith(
               IPSDeployConstants.ARCHIVE_EXTENSION))
         {
            return;
         }
         
         String action = lock ? "Locking" : "Unlocking";
                   
         System.out.println(action + " package file: " + packageFile.getName());
         
         File tmpFile = null;
         PSArchive oldArchive = null;
         PSArchive newArchive = null;
         PSArchiveHandler oldHandler = null;
         InputStream in = null;
         ZipFile zip = null;
         File tmp = null;
         FileOutputStream out = null;
         File tmpArchive = null;
                 
         try
         {
            // load archive manifest, info
            oldArchive = new PSArchive(packageFile);
            oldHandler = new PSArchiveHandler(oldArchive);
            PSArchiveManifest manifest = oldArchive.getArchiveManifest(); 
            PSArchiveInfo info = oldArchive.getArchiveInfo(true);
                       
            // update archive info (lock/unlock)
            zip = new ZipFile(packageFile);
            in = PSArchiveFiles.getFile(zip, PSArchive.ARCHIVE_INFO_PATH);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in,
                  false);
            in.close();
            info = new PSArchiveInfo(doc.getDocumentElement());
            info.setEditable(!lock);
            
            // create new archive
            tmpArchive = File.createTempFile("tmp", null);
            newArchive = new PSArchive(tmpArchive, info);
            newArchive.storeArchiveManifest(manifest);
            
            // add dependency files
            Iterator files = manifest.getFiles();
            while (files.hasNext())
            {
               PSDependencyFile depFile =
                  (PSDependencyFile) files.next();
               in = oldHandler.getFileData(depFile);
               addFile(in, depFile.getArchiveLocation().getPath().replace(
                     '\\', '/'), newArchive);
               in.close();
            }
            
            // add configuration files
            PSArchiveDetail detail = info.getArchiveDetail();
            PSDescriptor descriptor = detail.getExportDescriptor();
            String configDef = descriptor.getConfigDefFile();
            if (configDef.trim().length() > 0)
            {
               copyConfigFile("configurations/impl_config.xml", oldArchive,
                     newArchive);
            }
            
            String localConfig = descriptor.getLocalConfigFile();
            if (localConfig.trim().length() > 0)
            {
               copyConfigFile("configurations/local_config.xml", oldArchive,
                     newArchive);
            }
                        
            oldHandler.close();
            newArchive.close();
            zip.close();
            
            // update original package
            IOTools.copyFileStreams(tmpArchive, packageFile);
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
            
            if (oldHandler != null)
            {
               oldHandler.close();
            }
            
            if (newArchive != null)
            {
               newArchive.close();
            }
            
            if (zip != null)
            {
               zip.close();
            }
            
            if (tmp != null)
            {
               tmp.delete();
            }
            
            if (tmpArchive != null)
            {
               tmpArchive.delete();
            }
            
            if (tmpFile != null)
            {
               IOTools.deleteFile(tmpFile);
            }
         }
      }
   }

   /**
    * Write the usage text to the log and exits the program.
    */
   private static void showUsageAndExit()
   {
      System.out.println(
         "PackageLockManager.bat [-lock | -unlock] [package | package dir] "
            + "-h[elp]\n"
            + "Example (single package): PackageLockManager.bat -lock "
            + "C:\\Rhythmyx\\myPackage.ppkg\n"
            + "Example (package dir): PackageLockManager.bat -unlock "
            + "C:\\Rhythmyx\\myPackages");
      System.exit(1);
   }
   
   /**
    * Adds a file to an archive from an input stream.
    * 
    * @param in InputStream from which the file will be generated and copied,
    * assumed not <code>null</code>.  Will not be closed by this method.
    * @param entryPath The entry path under which the file will be added to the
    * archive, assumed not <code>null</code>.
    * @param archive The archive, assumed not <code>null</code>.
    * 
    * @throws IOException If an error occurs processing streams.
    * @throws PSDeployException If an error occurs storing the file.
    */
   private void addFile(InputStream in, String entryPath, PSArchive archive)
      throws IOException, PSDeployException
    
   {
      File tmp = null;
      FileOutputStream out = null;
      
      try
      {
         tmp = File.createTempFile("tmp", null);
         out = new FileOutputStream(tmp);
         IOTools.copyStream(in, out);
         out.close();
         archive.storeFile(tmp, entryPath);
         tmp.delete();
      }
      finally
      {
         if (tmp != null)
         {
            tmp.delete();
         }
         
         if (out != null)
         {
            out.close();
         }
      }
   }
   
   /**
    * Copies a configuration file from one archive to another.
    * 
    * @param entryPath The configuration file archive entry path, assumed not
    * <code>null</code>.
    * @param srcArchive The source archive, assumed not <code>null</code>.
    * @param tgtArchive The target archive, assumed not <code>null</code>.
    * 
    * @throws PSDeployException If an archive error occurs.
    * @throws IOException If a file error occurs.
    */
   private void copyConfigFile(String entryPath,
      PSArchive srcArchive, PSArchive tgtArchive) throws PSDeployException,
      IOException
   {
      InputStream in = null;
      
      try
      {      
         in = srcArchive.getFile(entryPath);
         addFile(in, entryPath, tgtArchive);
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }
   
}
