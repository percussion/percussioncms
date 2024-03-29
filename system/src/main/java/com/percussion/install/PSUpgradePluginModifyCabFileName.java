/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.install;

import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
/**
 * This plugin has been written to modify the word ocx cab file name in 
 * sys_Templates.xsl file.  This plugin should go into a module which 
 * need to be run on upgrade from 20011114 to 20020320 only.
 */

public class PSUpgradePluginModifyCabFileName implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginModifyCabFileName()
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

      config.getLogStream().println("Modifying sys_FileWord Template");

      File file = null;
      PrintWriter pw = null;
      ByteArrayOutputStream bos = null;
      FileInputStream fis = null;
      try
      {
         file = new File(RxUpgrade.getRxRoot() +
            "sys_resources" + File.separator + "stylesheets" +
            File.separator + "sys_Templates.xsl");
         fis = new FileInputStream(file);
         bos = new ByteArrayOutputStream();
         copyStream(fis, bos);
         String docStr = bos.toString();
         docStr = replaceCabName(docStr);
         pw = new PrintWriter(new FileOutputStream(file));
         pw.write(docStr);
         pw.flush();
     }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         try
         {
            if(pw != null)
            {
               pw.close();
               pw =null;
            }
            if(fis != null)
            {
               fis.close();
               fis =null;
            }
            if(bos != null)
            {
               bos.close();
               bos =null;
            }
         }
         catch(Throwable t)
         {
         }
      }
      config.getLogStream().println("leaving the process() of the plugin...");
      return null;
   }
   /**
    * Helper function replaces OCX_STRING_NEW by OCX_STRING_OLD if exits in 
    * the input String.
    * @param str Input string
    * @return the modified string.
    */

   private String replaceCabName(String str)
   {
      int loc1 = str.indexOf(OCX_STRING_NEW);
      if(loc1!=-1)
      {
         String left = str.substring(0, loc1);
         String right = str.substring(loc1+OCX_STRING_NEW.length());
         str = left + OCX_STRING_OLD + right;
      }
      return str;
   }
  /**
   * Method to copy Java InputStream to PoutputStream.
   * 
   * @param in Input stream tp copy from, never <code>null</code>.
   *
   * @param out Output stream to copy to, never <code>null</code>.
   *
   * @return number bytes copied
   *
   * @throws IOException in case of any error while copying.
   *
   */
   public static long copyStream(InputStream in, OutputStream out)
      throws IOException
   {
      int nCopied = 0;
      final byte[] buffer = new byte[ DEFAULT_BUFFER_SIZE ];
      int n = 0;
      while( -1 != (n = in.read( buffer )) )
      {
          out.write( buffer, 0, n );
          nCopied += n;
      }
      return nCopied;
   }

   /**
    * word ocx cab string to be replaced
    */
   static public final String OCX_STRING_NEW =
      "rx_resources/word/rxwordocx.cab#version=4,0,6,0";

   /**
    * word ocx cab string to be replaced by
    */
   static public final String OCX_STRING_OLD =
      "sys_resources/word/rxword.cab#version=4,0,3,0";

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
}
