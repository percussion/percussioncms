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
import com.percussion.tools.simple.PSCETemplateGenerator;
import com.percussion.utils.xml.PSEntityResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This is a utility class to create the template files from all content editor 
 * applications. This is meant to be used before manufacturing.  It will go 
 * through all apps in the specified directory and create the template xml files.
 */
public class PSCETemplateFilesCreator
{

    private static final Logger log = LogManager.getLogger(PSCETemplateFilesCreator.class);

  /**
   * Default constructor
   */
  public PSCETemplateFilesCreator()
  {
  }

  /**
   * Creates the template files for all content editor application files exist
   * in the application subdirectories of specified <code>strAppDirectory
   * </code> directory. Assumes the application as a content editor application
   * if the application name has the string 'rx_ce'. The templates are created
   * in a directory 'sys_psxTemplates' under the specified <code>strAppDirectory
   * </code> directory. The name of the template file is genereated by appending
   * the name after 'rx_ce' to 'sys_'.
   * <br>
   * For example if the application file name is 'rx_ceArticle', then the
   * template file is 'sys_Article'.
   * <br>
   * Uses {@link com.percussion.tools.simple.PSCETemplateGenerator#createTemplate
   * createTEmplate} for creating templates. Uses the specified dtd file for
   * validating the created template file.
   *
   *
   * @param strAppDirectory Must not be <code>null</code> and must be a
   * directory not a file. The applications directory which the editor
   * applications reside.  This directory needs to have subdirectories
   * each having an editor application.
   *
   * @param strDtdFile Must not be <code>null</code> and must exist.
   *
   * @param strDocTypePath Must not be <code>null</code>..
   *
   * @throws IllegalArgumentException if any params are invalid.
   *
   * Example: applications\rx_ce<name>\rx_ce<Name>.xml will create a template
   *          applications\sys_psxTemplates\sys_<name>.xml.
   */
  public static void createTemplates(String strAppDirectory, String strDtdFile, 
     String strDocTypePath)
  {
      if(strAppDirectory == null)
          throw new IllegalArgumentException(
                      "The applications directory must not be null");

      File appsDir = new File(strAppDirectory);
      if(!appsDir.isDirectory())
          throw new IllegalArgumentException(
                      "The appications directory must be a directory.");

      if(strDtdFile == null)
          throw new IllegalArgumentException(
                      "The dtd file must not be null.");

      if(!new File(strDtdFile).exists())
          throw new IllegalArgumentException(
                      "The dtd file must exist.");
     
      if(strDocTypePath == null)
          throw new IllegalArgumentException(
                      "The doctype path must not be null.");

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

              //get the source application file name
              String strAppFile = appDir.getAbsolutePath() + File.separator +
                                  appDir.getName() + ".xml";
              if(new File(strAppFile).exists())
              {

                  String strAppName = appDir.getName();

                  int iFind = strAppName.indexOf("rx_ce"); 
                  if(iFind != -1)
                  {
                      //move to the end of rx_ce
                      iFind += 5; 
                      String strSrcName = strAppName.substring(iFind,
                         strAppName.length());
                      String targetFile = "sys_" + strSrcName + ".xml";

                      targetFile = appsDir.getAbsolutePath() + 
                         File.separator + TEMPLATES_DIRECTORY_NAME +  
                         File.separator + targetFile;

                      try
                      {
                           URL dtdUrl = new URL("file", null, 
                                        "/Rhythmyx/" + dtdName); 
                           PSCETemplateGenerator generator = 
                              new PSCETemplateGenerator ();
                           generator.createTemplate( new File(strAppFile),
                              new File(targetFile), 
                              dtdUrl,
                              strDocTypePath);
                      }
                      catch(PSCETemplateGenerator.PSCreateTemplateException e)
                      {
                          log.error(e.getMessage());
                          log.debug(e.getMessage(), e);
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
              "usage: java com.percussion.build.PSCETemplateFilesCreator "+
              "<Application Directory> <Dtd File> <Doctype Path>");
          return;
      }

      createTemplates(args[0], args[1], args[2]);
  }

   /**
    * Templates files directory name.
    */
   private static final String TEMPLATES_DIRECTORY_NAME = "sys_psxTemplates";

}
