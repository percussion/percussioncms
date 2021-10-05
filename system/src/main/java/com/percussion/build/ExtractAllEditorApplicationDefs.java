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

package com.percussion.build;

//java

import com.percussion.tools.simple.PSXmlExtractor;
import com.percussion.utils.xml.PSEntityResolver;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * This class will extract the def from the editor apps.  This is meant to be
 * used before manufacturing.  It will go through all apps in the specified
 * directory and create the def xml file. The DTD will be assigned so that
 * after installation the DOCTYPE element of the Xml file will point to the
 * correct DTD and the file may be successfully dropped into the workbench to
 * create a Content Editor resource.
 */
public class ExtractAllEditorApplicationDefs
{

   private static final Logger log = LogManager.getLogger(ExtractAllEditorApplicationDefs.class);

   /**
    * Extract the defs from the specified apps.
    */
   public ExtractAllEditorApplicationDefs()
   {
   }

   /**
    * Extracts the def files from the editor applications contained in
    * the applications directory.
    *
    * @param strAppDirectory Must not be <code>null</code> and must be a
    * directory not a file. The applications directory in which the editor
    * applications reside.  This directory needs to have subdirectories
    * each having an editor application.  The extracted xml document will
    * be put into the src directory.
    *
    * @param strDtdFile Must not be <code>null</code> and must exist.  The
    * DTD used to validate the resulting Xml file.
    *
    * @param strDocTypeFile The path used to construct a <code>DOCTYPE</code>
    * element referencing an externally defined <code>SYSTEM</code> DTD.  May
    * not be <code>null</code> or emtpy, and should be relative to the working
    * directory of the workbench, and assumes that the workbench has been 
    * installed along with the server.
    *
    * @throws <code>IllegalArgumentException</code> any params are invalid.
    *
    * Example: applications\rx_ce<name>\rx_ce<Name>.xml will create
    *          applications\rx_ce<name>\src\name.xml.
    */
   public static void extract(String strAppDirectory, String strDtdFile,
   String strDocTypeFile)
   {
      if(strAppDirectory == null)
         throw new IllegalArgumentException(
            "The applications directory must not be null");

      File appsDir = new File(strAppDirectory);
      if(!appsDir.isDirectory())
         throw new IllegalArgumentException(
            "The applications directory must be a directory.");

      if(strDtdFile == null)
         throw new IllegalArgumentException("The dtd file must not be null.");

      if(!new File(strDtdFile).exists())
         throw new IllegalArgumentException("The dtd file must exist.");

      if(strDocTypeFile == null || strDocTypeFile.trim().length() == 0)
         throw new IllegalArgumentException(
            "The doc type file path must not be null or empty.");
            
      // Setup the resolver to find the doctype
      int dtdIndex = strDtdFile.indexOf("dtd");
      if (dtdIndex == -1)
      {
         throw new IllegalArgumentException(
         "The dtd path must have the string dtd in the path");
      }
      String rootPath = strDtdFile.substring(0, dtdIndex);
      String dtdName = strDtdFile.substring(dtdIndex);
      dtdName = dtdName.replace('\\','/');
      PSEntityResolver res = PSEntityResolver.getInstance();
      res.setResolutionHome(new File(rootPath));
                                            
      File[] appDirs = appsDir.listFiles();
      for(int iApp = 0; iApp < appDirs.length; ++iApp)
      {
         File appDir = appDirs[iApp];
         if(appDir.isDirectory())
         {
            /**
             * The application directory inside of the applications
             * directory is always named the same as the application
             * file.
             *
             * Inside the application directory is a ApplicationFiles
             * directory which may or may not have a src directory.
             */

            //get the source application file
            String strAppFile = appDir.getAbsolutePath() + File.separator +
               appDir.getName() + ".xml";
            if(new File(strAppFile).exists())
            {
               /**
                * The destination src file should be named with
                * the base name of the application.  All apps start with
                * a prefix_.  Example: rx_*, sys_*.  Editors then have a ce
                * prefix after the _. Example: rx_ce*, sys_ce*.  
                * The name of the application the comes in proper case.
                * Example: rx_ceArticle.  We want the destination to bhe 
                * the app name in lower case. Example: rx_ceArticle would
                * have a article.xml in the src directory.
                */
               
               String strAppName = appDir.getName();

               //get the destination src file
               String strSrcFile = appDir.getAbsolutePath() +  File.separator +
                 "ApplicationFiles";

               File appFiles = new File(strSrcFile);
               //make sure the ApplicationFiles directory exists.
               if(!appFiles.exists())
                  appFiles.mkdir();

               strSrcFile += File.separator + "src";

               appFiles = new File(strSrcFile);
               //make sure the src directory exists.
               if(!appFiles.exists())
                  appFiles.mkdir();

               strSrcFile += File.separator;

               int iFind = strAppName.indexOf("_ce");
               if(iFind != -1)
               {
                  //move to the end of _ce
                  iFind += 3;
                  String strSrcName = strAppName.substring(iFind,
                    strAppName.length());
                  strSrcFile += strSrcName + ".xml";
                  strSrcFile = strSrcFile.toLowerCase();

                  String element = "PSXContentEditor";
                  FileInputStream in = null;
                  try
                  {
                     // load source doc
                     in = new FileInputStream(new File(strAppFile));
                     Document doc = PSXmlDocumentBuilder.createXmlDocument(in,
                        false);


                     // check for PSXContentEditor
                     PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
                     tree.setCurrent(doc.getDocumentElement());

                     Element el = tree.getNextElement(element);
                     if (el != null)
                     {
                        try
                        {
                           URL dtdUrl = new URL("file", null, 
                                          "/Rhythmyx/" + dtdName);
                           System.out.println("Extracting " + strAppName);
                           String strError = PSXmlExtractor.extract(
                             new File(strAppFile), new File(strSrcFile),
                             element, dtdUrl, null, null, strDocTypeFile);

                           if(strError != null && strError.length() > 0)
                           System.out.println("Error extracting " + strAppName +
                              " - " + strError);
                        }
                        catch(SAXException e)
                        {
                           log.error(e.getMessage());
                           log.debug(e.getMessage(), e);
                        }
                        catch(FileNotFoundException e)
                        {
                           log.error(e.getMessage());
                           log.debug(e.getMessage(), e);
                        }
                        catch(IOException e)
                        {
                           log.error(e.getMessage());
                           log.debug(e.getMessage(), e);
                        }
                        catch(IllegalArgumentException e)
                        {
                           log.error(e.getMessage());
                           log.debug(e.getMessage(), e);
                        }
                     }

                     in.close();
                     in = null;
                  }
                  catch(SAXException e)
                  {
                     log.error(e.getMessage());
                     log.debug(e.getMessage(), e);
                  }
                  catch(FileNotFoundException e)
                  {
                     log.error(e.getMessage());
                     log.debug(e.getMessage(), e);
                  }
                  catch(IOException e)
                  {
                     log.error(e.getMessage());
                     log.debug(e.getMessage(), e);
                  }
                  finally
                  {
                     if (in != null)
                     {
                        try {in.close();} catch (Exception e){}
                     }
                  }
               }
            }
         }
      }
   }
  
   /**
    * Main entry point from the command line.
    *
    * 2 arguments must be passed in, the applications directory and dtd file.
    */
   public static void main(String[] args)
   {
      if(args.length < 3)
      {
         System.out.println(
            "usage: java com.percussion.build.ExtractAllEditorApplicationDefs "+
            "<Application Directory> <Dtd File> <Doctype path>");
         return;
      }

      extract(args[0], args[1], args[2]);
   }

}
