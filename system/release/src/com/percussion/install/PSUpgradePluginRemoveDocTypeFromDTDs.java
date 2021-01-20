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
package com.percussion.install;

import com.percussion.util.PSFileFilter;
import com.percussion.util.PSFilteredFileList;
import com.percussion.utils.tools.PSPatternMatcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

/**
 * This plugin removes the invalid DOCTYPE from .dtd and .pdt files.
 */
public class PSUpgradePluginRemoveDocTypeFromDTDs implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginRemoveDocTypeFromDTDs()
   {
   }

   /**
    * Implements the process function of IPSUpgardePlugin.
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println("Searching for DTDs with invalid DOCTYPE");

      File file = null;
      PrintWriter pw = null;
      ByteArrayOutputStream bos = null;
      FileInputStream fis = null;
      
      try
      {
         List fileList = null;
         
         File root = new File(RxUpgrade.getRxRoot());
         
         //search for all .dtd files under Rx root
         PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.dtd");
         PSFileFilter filter = new PSFileFilter(
            PSFileFilter.IS_FILE|PSFileFilter.IS_INCLUDE_ALL_DIRECTORIES);
         filter.setNamePattern(pattern);
         PSFilteredFileList  lister = new PSFilteredFileList(filter);
         fileList = lister.getFiles(root);
                              
         Iterator iter = fileList.iterator();
         while (iter.hasNext())
         {
            File dtdFile = (File) iter.next();

            String fileName = dtdFile.getName();
            
            //check if this DTD has a DOCTYPE that we want to remove
            BufferedWriter out = null;
            BufferedReader reader = null;
            boolean foundMalformedDtd = false;
            File tmpFile = null;
            
            try
            {
               InputStream is  = new FileInputStream(dtdFile);
               InputStreamReader isReader = new InputStreamReader(is, "UTF8");
            
               reader = new BufferedReader(isReader);
               
               tmpFile = File.createTempFile(dtdFile.getName(), ".upg");
      
               OutputStream os  = new FileOutputStream(tmpFile);
               OutputStreamWriter writer = new OutputStreamWriter(os, "UTF8");

               out = new BufferedWriter(writer);
         
               String line = null;
               while ((line = reader.readLine()) != null)
               {
                  if (line.indexOf("<!DOCTYPE") >= 0 || line.indexOf("]>") >= 0)
                  {
                     //skip it..
                     config.getLogStream().println("file: " +
                        dtdFile.getAbsolutePath() +
                        " has invalid line: " + line + " removing it..");
                     
                     foundMalformedDtd = true;
                     continue;
                  }
                  
                  out.write(line);
                  out.newLine();
               }
            }
            finally
            {
               if (out != null)
               {
                  try
                  {
                     out.close();
                  }
                  catch (Exception ex)
                  {
                     // no-op
                  }
               }

               if (reader != null)
               {
                  try
                  {
                     reader.close();
                  }
                  catch (Exception ex)
                  {
                     // no-op
                  }
               }
               
               try
               {
               
                  if (foundMalformedDtd)
                  {
                     //backup .dtd file
                     File bakFile = new File(dtdFile.getAbsolutePath() + ".bak");
                     if (bakFile.exists())
                        bakFile.delete();
                     
                     //backup original dtd file
                     copy(dtdFile, bakFile);
                     
                     //delete malformed dtd file
                     dtdFile.delete();
                     
                     //replace it with the one we cleaned up
                     tmpFile.renameTo(dtdFile);
                     
                     //is there a .pdt file? we need to replace it as well..
                     String dtdName = dtdFile.getName();
                     
                     //construct a corresponding .pdt file name
                     String pdtFileName = 
                        dtdName.substring(0, dtdName.length() - 4) + ".pdt";
   
                     File ptdFile = new File(dtdFile.getParent() +
                        File.separator + pdtFileName);
                        
                     if (ptdFile.exists())
                     {
                        ptdFile.delete();
                        copy(dtdFile, ptdFile);
                     }
                  }
               }
               catch(Exception ex)
               {
                  ex.printStackTrace(config.getLogStream());
               }
            }
            
         }
      }
      catch(Exception ex)
      {
         ex.printStackTrace(config.getLogStream());
      }


      config.getLogStream().println("leaving the plugin...");
      return null;
   }
   
   /**
    * Helper that copies a file from src to dest.
    * @param src source to copy from, never <code>null</code>.
    * @param dst dest to copy to, never <code>null</code>.
    * @throws IOException if IO problem happens.
    */
   private void copy(File src, File dst) throws IOException
   {
      if (src== null)
         throw new IllegalArgumentException("src may not be null");
      if (dst== null)
         throw new IllegalArgumentException("dst may not be null");
      
      InputStream in = null;
      OutputStream out = null;
      
      try
      {
         in = new FileInputStream(src);
         out = new FileOutputStream(dst);
   
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0)
         {
            out.write(buf, 0, len);
         }
      }
      catch(IOException ex)
      {
         throw ex;
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (Exception ex)
            {
               // no-op
            }
         }
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (Exception ex)
            {
               // no-op
            }
         }         
      }
   }
   
}
