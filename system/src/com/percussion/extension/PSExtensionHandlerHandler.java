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
package com.percussion.extension;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.server.PSConsole;
import com.percussion.util.IOTools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * The extension handler handler is a type of Java extension handler
 * that manages extension handlers themselves. Remember that extension
 * handlers are themselves a kind of extension.
 */
class PSExtensionHandlerHandler extends PSJavaExtensionHandler
{
   /**
    * @see PSJavaExtensionHandler#init
    */
   public synchronized void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      super.init(def, codeRoot);
   }

   /**
    * @see IPSExtensionHandler#getName
    */
   public String getName()
   {
      return IPSExtensionHandler.HANDLER_HANDLER;
   }

   /**
    * @see IPSExtensionHandler#update(IPSExtensionDef,Iterator)
    */
   public synchronized void update(IPSExtensionDef def, Iterator resources)
      throws PSExtensionException, PSNotFoundException
   {
      // If the new extension is installed under a different directory
      // than the old extension, we move all the files from the old
      // directory into the new directory (but we don't overwrite files
      // which already exist in the new directory).

      // Get the current version's definition
      PSExtensionRef ref = def.getRef();
      IPSExtensionDef oldDef = super.getExtensionDef(ref);

      // Get the directory of the current version
      File oldDir = getCodeBase(oldDef);

      /* Update the old version - this will update the def to point to the
         new version */
      super.update(def, resources);

      // Get the directory of the new version
      File newDir = getCodeBase(def);

      try
      {
         if (!oldDir.equals(newDir))
         {
            newDir.mkdirs();
            recursiveCopy(oldDir, newDir, false);

            // fix the pening removal in the copied file to the new version
            File configFile = new File(newDir.getCanonicalPath() + 
                                       File.separator + 
                                       IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
            if (configFile.isFile())
               fixPendingRemovals(configFile);
      }
      }
      catch (IOException e)
      {
         // TODO: i18n and code
         throw new PSExtensionException(0, e.toString());
      }
   }
   
   /**
    * Loop through all pending removals and fix the handler version for each 
    * entry. The fixed pending removals will be stored back to the source
    * configuration file.
    *
    * @param configFile the extensions vonfiguration file to fix
    * @throws PSExtensionException for any error dealing with the config file
    * @throws IOException for any file handling IO error.
    */
   private static void fixPendingRemovals(File configFile)
      throws PSExtensionException, IOException
   {
      if (configFile == null || !configFile.isFile())
         throw new IllegalArgumentException(
            "we need a valid configuration file");
      
      PSExtensionHandlerConfiguration config = 
         new PSExtensionHandlerConfiguration(
            configFile, new PSExtensionDefFactory());

      for (Iterator i=config.getPendingRemovals(); i.hasNext();)
         config.setPendingRemoval(fixRemoveContext((File) i.next()));

      config.store(configFile, true);
   }
   
   /**
    * Because the entire handler structure is copied before we process
    * pending removals, we have to fix the context for the pending removal
    * entries in the extensions.xml file. We need to increment the handler
    * version by 1, so the correct versions are processed the next time
    * pending removals are processed.
    *
    * @param context the current context, not <code>null</code>
    * @return the corrected context (handler version increased by one), the
    *    original context if not handler context is found.
    * @throws IOException for any file handling error
    * @throws IllegalArgumentException for any illegal arguments passed
    */
   private static File fixRemoveContext(File context) throws IOException,
                                                             PSExtensionException
   {
      if (context == null)
         throw new IllegalArgumentException("we need a valid context");
      
      String strContext = context.getCanonicalPath();
      String strNewContext = strContext;
      
      String search = IPSExtensionHandler.EXTENSIONS_SUBDIR + File.separator + 
                      IPSExtensionHandler.HANDLER_CONTEXT + File.separator;
      int found = strContext.indexOf(search);
      
      if (found != -1)
      {
         int beginHandler = found + search.length();
         if (beginHandler != -1)
         {
            int beginVersion = strContext.indexOf(File.separator, beginHandler);
            if (beginVersion != -1)
            {
               beginVersion += File.separator.length();
               int endVersion = strContext.indexOf(File.separator, beginVersion);
               if (endVersion != -1)
               {
                  String strVersion = strContext.substring(beginVersion, endVersion);
                  try
                  {
                     int version = Integer.parseInt(strVersion) + 1;

                     strNewContext = strContext.substring(0, beginVersion) + 
                                     version + 
                                     strContext.substring(endVersion, strContext.length());
                  }
                  catch (NumberFormatException e)
                  {
                     // this should never happen
                     PSConsole.printMsg("Extension", e);
                  }
               }
            }
         }
      }
      
      return new File(strNewContext);
   }

   /**
    * Recursively copies all the files from the given source directory
    * to the given destination directory, preserving the directory
    * structure.
    *
    * @param source The source directory. Must not be <CODE>null</CODE>.
    *
    * @param dest The destination directory. Must exist prior to
    * calling. Must not be <CODE>null</CODE>.
    *
    * @param overWrite Files already existing in dest will be overwritten
    * if and only if <CODE>true</CODE>.
    */
   static int recursiveCopy(File source, File dest, boolean overWrite)
      throws IOException, PSExtensionException
   {
      File[] sourceFiles = source.listFiles();
      int numCopied = 0;

      if (sourceFiles == null)
         return numCopied;

      for (int i = 0; i < sourceFiles.length; i++)
      {
         File sourceFile = sourceFiles[i];
         File destFile = new File(dest, sourceFile.getName());
         if (sourceFile.isDirectory())
         {
            if (!destFile.isFile())
            {
               destFile.mkdir();
               numCopied += recursiveCopy(sourceFile, destFile, overWrite);
            }
         }
         else if (overWrite || !destFile.exists())
         {
            InputStream in = null;
            OutputStream out = null;
            try
            {
               in = new BufferedInputStream(
                  new FileInputStream(sourceFile));

               out = new BufferedOutputStream(
                  new FileOutputStream(destFile));

               IOTools.copyStream(in, out, 8192);
               numCopied++;
            }
            finally
            {
               if (in != null)
               {
                  try { in.close(); } catch (IOException e) { /* ignore */ }
               }

               if (out != null)
               {
                  try { out.close(); } catch (IOException e) { /* ignore */ }
               }
            }
         }
      }
      return numCopied;
   }
}
