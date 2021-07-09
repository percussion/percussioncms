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
package com.percussion.i18n.rxlt;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.tools.PSCopyStream;
import com.percussion.util.PSFileFilter;
import com.percussion.util.PSFilteredFileList;
import com.percussion.utils.tools.PSPatternMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class handle the processing of creating locale specific Rhythmyx
 * resources files. These are basically copies of the files for the default
 * language. The idea of copying the resources is to provide a way of using the
 * untranslated versions of the resources until translation is done. This way the
 * implementor does not need to worry about searching for the resources that need
 * translation when a new language is added to the system. All the resources are
 * automatically copied by RXLT and hence only translation is left for the
 * implementer.
 */
public class PSLocaleRxResourceCopyHandler
   extends PSIdleDotter
{

   private static final Logger log = LogManager.getLogger(PSLocaleRxResourceCopyHandler.class);

   /**
    * Constructor. Needs valid Rhythmyx root directory and the new language
    * string.
    * @param rxroot must not be <code>empty</code>.
    * @param languageString must not be <code>null</code> or <code>empty</code>.
    * @throws IllegalArgumentException if rxroot or languageString
    * is <code>null</code> or <code>empty</code>
    */
   public PSLocaleRxResourceCopyHandler(String rxroot, String languageString)
   {
      if(rxroot == null || rxroot.trim().length() < 1)
      {
         throw new IllegalArgumentException(
            "Rhythmyx root directory must not be empty");
      }
      if(languageString == null || languageString.trim().length() < 1)
      {
         throw new IllegalArgumentException(
            "Language string must not be empty for making " +
            "copies of Rhythmyx resources");
      }
      m_rxroot = rxroot;
      m_languagestring = languageString;
   }

   /**
    * Calls {@link #processResourceCopy(boolean) processResourceCopy(true)}
    */
   public void processResourceCopy() throws IOException
   {
      processResourceCopy(true);
   }
   
   /**
    * This method handles copying of all Rhythmyx resources as per the
    * localization scheme used. Refer to implementor's documentation for the
    * details of the scheme.
    * 
    * @param doLog <code>true</code> to log messages, <code>false</code> 
    * otherwise.
    * 
    * @throws IOException if file copy fails for IO reasons
    */
   public void processResourceCopy(boolean doLog)
      throws IOException
   {
      boolean isLogEnabled = PSCommandLineProcessor.isLogEnabled();
      try
      {
         PSCommandLineProcessor.setIsLogEnabled(doLog);
         makeJSCopy();
         makeCSSCopy();
         makeXMLCopy();
         makeImagesCopy();
      }
      finally
      {
         endDotSession();
         PSCommandLineProcessor.setIsLogEnabled(isLogEnabled);
      }
   }

   /**
    * Gets file references to all existing resource files for the language
    * string passed to the constructor.
    *
    * @return An iterator over zero or more <code>File</code> objects, never
    * <code>null</code>.
    */
   public Iterator getResourceFiles()
   {
      List files = new ArrayList();

      // get js file
      File jsFile = getJSFile(m_languagestring, false);
      if (jsFile.exists())
         files.add(jsFile);

      // get css file
      File cssFile = getCSSFile(m_languagestring, false);
      if (cssFile.exists())
         files.add(cssFile);

      // get image files
      List imgFiles = getImageFiles(m_languagestring, false);
      if (imgFiles != null)
         files.addAll(imgFiles);

      // get user options xml file
      File xmlFile = getXMLFile(m_languagestring, false);
      if (xmlFile.exists())
         files.add(xmlFile);

      return files.iterator();
   }

   /**
    * Get file reference to the specified language's javascript resource file.
    *
    * @param languageString The language for which the file is returned,
    * assumed not <code>null</code> or empty.
    * @param isSystem <code>true</code> to specify the system resources
    * directory, <code>false</code> to specify the rx resources directory.
    *
    * @return The file reference, never <code>null</code>, may not actually
    * exist.
    */
   private File getJSFile(String languageString, boolean isSystem)
   {
      String resourceFolder = isSystem ? SYSTEM_JS_DIR : RX_JS_DIR;
      return new File(m_rxroot + File.separator +
         resourceFolder + File.separator + languageString,
         JS_ERROR_MESSAGE_FILE);
   }

   /**
    * Get file reference to the specified language's css resource file.
    *
    * @param languageString The language for which the file is returned,
    * assumed not <code>null</code> or empty.
    * @param isSystem <code>true</code> to specify the system resources
    * directory, <code>false</code> to specify the rx resources directory.
    *
    * @return The file reference, never <code>null</code>, may not actually
    * exist.
    */
   private File getCSSFile(String languageString, boolean isSystem)
   {
      String resourceFolder = isSystem ? SYSTEM_CSS_DIR : RX_CSS_DIR;
      return new File(m_rxroot + File.separator +
         resourceFolder + File.separator + languageString,
         CSS_TEMPLATE_FILE);
   }

   /**
    * Get file reference to the specified language's XML resource file.
    *
    * @param languageString The language for which the file is returned,
    * assumed not <code>null</code> or empty.
    * @param isSystem <code>true</code> to specify the system resources
    * directory, <code>false</code> to specify the rx resources directory.
    *
    * @return The file reference, never <code>null</code>, may not actually
    * exist.
    */
   private File getXMLFile(String languageString, boolean isSystem)
   {
      String resourceFolder = isSystem ? SYSTEM_CSS_DIR : RX_CSS_DIR;
      return new File(m_rxroot + File.separator +
         resourceFolder + File.separator + languageString,
         USER_OPTIONS_FILE);
   }

   /**
    * Get file reference to the specified language's images resource files
    * directory.
    *
    * @param languageString The language for which the directory is returned,
    * assumed not <code>null</code> or empty.
    * @param isSystem <code>true</code> to specify the system resources
    * directory, <code>false</code> to specify the rx resources directory.
    *
    * @return The file reference, never <code>null</code>, may not actually
    * exist.
    */
   private File getImageFileDir(String languageString, boolean isSystem)
   {
      String resourceFolder = isSystem ? SYSTEM_IMAGE_DIR : RX_IMAGE_DIR;
      return new File(m_rxroot + File.separator +
         resourceFolder, languageString);
   }

   /**
    * Recursively get all images files below the specified images directory.
    *
    * @param languageString The language for which the files are returned,
    * assumed not <code>null</code> or empty.
    * @param isSystem <code>true</code> to specify the system resources
    * directory, <code>false</code> to specify the rx resources directory.
    *
    * @return The list of files, will be <code>null</code> only if the specified
    * language's images directory does not exist.  Otherwise will contain all
    * files that exist below that directory, including files within any
    * subdirectories, recurisvely.
    */
   private List getImageFiles(String languageString, boolean isSystem)
   {
      List listFiles = null;
      File dir = getImageFileDir(languageString, isSystem);
      if(dir.exists())
      {
         PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.*");
         PSFileFilter filter = new PSFileFilter(
            PSFileFilter.IS_FILE|PSFileFilter.IS_INCLUDE_ALL_DIRECTORIES);
         filter.setNamePattern(pattern);
         PSFilteredFileList  lister = new PSFilteredFileList(filter);
         listFiles = lister.getFiles(dir);
      }

      return listFiles;
   }


   /**
    * Convenience method for copying JavaScript resources
    * @throws IOException if file copying fails for IO reasons
    */
   private void makeJSCopy()
      throws IOException
   {
      PSCommandLineProcessor.logMessage("copyingJavaScriptResourcesFor",
         m_languagestring);

      File srcFile = getJSFile(PSI18nUtils.DEFAULT_LANG, true);
      File tgtFile = getJSFile(m_languagestring, false);
      if(tgtFile.exists())
      {
         PSCommandLineProcessor.logMessage(
            "resourceFileExistsNotOverWrittern", tgtFile.getCanonicalPath());
         return;
      }
      if(!srcFile.exists())
      {
         PSCommandLineProcessor.logMessage(
            "resourceFileNotExist", srcFile.getCanonicalPath());
         PSCommandLineProcessor.logMessage(
            "resourceFileNotbeCreated", tgtFile.getCanonicalPath());
         return;
      }

      String[]args = {srcFile.getCanonicalPath(), tgtFile.getCanonicalPath()};
      PSCommandLineProcessor.logMessage("copyingFileTo", args);

      if(tgtFile.getParentFile() != null)
         tgtFile.getParentFile().mkdirs();

      try(FileInputStream fis = new FileInputStream(srcFile)){
         try(FileOutputStream fos = new FileOutputStream(tgtFile)) {
            PSCopyStream.copyStream(fis, fos);
            fos.flush();
         }
      }

   }

   /**
    * Convenience method for copying CSS resources
    * @throws IOException if file copying fails for IO reasons
    */
   private void makeCSSCopy()
      throws IOException
   {
      PSCommandLineProcessor.logMessage("copyingCSSResourcesFor",
         m_languagestring);

      File srcFile = getCSSFile(PSI18nUtils.DEFAULT_LANG, true);
      File tgtFile = getCSSFile(m_languagestring, false);
      if(tgtFile.exists())
      {
         PSCommandLineProcessor.logMessage(
            "resourceFileExistsNotOverWrittern", tgtFile.getCanonicalPath());
         return;
      }
      if(!srcFile.exists())
      {
         PSCommandLineProcessor.logMessage(
            "resourceFileNotExist", srcFile.getCanonicalPath());
         PSCommandLineProcessor.logMessage(
            "resourceFileNotbeCreated", tgtFile.getCanonicalPath());
         return;
      }

         String[]args = {srcFile.getCanonicalPath(), tgtFile.getCanonicalPath()};
         PSCommandLineProcessor.logMessage("copyingFileTo", args);

         if(tgtFile.getParentFile() != null)
            tgtFile.getParentFile().mkdirs();

         try(FileInputStream fis = new FileInputStream(srcFile)) {
            try(FileOutputStream fos = new FileOutputStream(tgtFile)) {
               PSCopyStream.copyStream(fis, fos);
            }
         }

   }

   /**
    * Convenience method for copying XML resources
    * @throws IOException if file copying fails for IO reasons
    */
   private void makeXMLCopy()
      throws IOException
   {
      PSCommandLineProcessor.logMessage("copyingXMLResourcesFor",
         m_languagestring);

      File srcFile = getXMLFile(PSI18nUtils.DEFAULT_LANG, true);
      File tgtFile = getXMLFile(m_languagestring, false);
      if(tgtFile.exists())
      {
         PSCommandLineProcessor.logMessage(
            "resourceFileExistsNotOverWrittern", tgtFile.getCanonicalPath());
         return;
      }
      if(!srcFile.exists())
      {
         PSCommandLineProcessor.logMessage(
            "resourceFileNotExist", srcFile.getCanonicalPath());
         PSCommandLineProcessor.logMessage(
            "resourceFileNotbeCreated", tgtFile.getCanonicalPath());
         return;
      }

      String[]args = {srcFile.getCanonicalPath(), tgtFile.getCanonicalPath()};
      PSCommandLineProcessor.logMessage("copyingFileTo", args);

      if(tgtFile.getParentFile() != null)
         tgtFile.getParentFile().mkdirs();

      try(FileInputStream fis = new FileInputStream(srcFile)) {
         try (FileOutputStream fos = new FileOutputStream(tgtFile)) {
            PSCopyStream.copyStream(fis, fos);
         }
      }

   }

   /**
    * Convenience method for copying Image resources
    * @throws IOException if file copying fails for IO reasons
    */
   private void makeImagesCopy()
      throws IOException
   {
      PSCommandLineProcessor.logMessage("copyingImageResourcesFor",
         m_languagestring);
      //start displaying idle dots
      showDots(true);
      List listFiles = getImageFiles(PSI18nUtils.DEFAULT_LANG, true);
      if(listFiles == null)
      {
         PSCommandLineProcessor.logMessage("imageDirNotExist",
            getImageFileDir(PSI18nUtils.DEFAULT_LANG, true).getCanonicalPath());
         return;
      }
      //stop displaying idle dots
      showDots(false);

      File file = null;
      File newFile = null;
      String newPath = "";
      for(int i=0; listFiles!=null && i<listFiles.size(); i++)
      {
         file = (File)listFiles.get(i);
         newPath = getNewImagePath(file.getCanonicalPath());
         newFile = new File(newPath);
         if(newFile.exists())
         {
         PSCommandLineProcessor.logMessage(
            "resourceFileExistsNotOverWrittern", newFile.getCanonicalPath());
            continue;
         }
         try
         {
            String[]args = {file.getCanonicalPath(), newFile.getCanonicalPath()};
            PSCommandLineProcessor.logMessage("copyingFileTo", args);

            if(newFile.getParentFile() != null)
               newFile.getParentFile().mkdirs();

            try(FileInputStream fis = new FileInputStream(file)) {
               try (FileOutputStream fos = new FileOutputStream(newFile)) {
                  PSCopyStream.copyStream(fis, fos);
               }
            }
         }
         catch(IOException e)
         {
            String[]args = {file.getCanonicalPath(), newFile.getCanonicalPath()};
            PSCommandLineProcessor.logMessage("errorCopyingFileTo", args);
            PSCommandLineProcessor.logMessage("errorMessageException",
               e.getMessage());
         }
      }
   }

   /**
    * This method searches for "sys_resources" in the path string and replaces
    * with "rx_resources". Also default language string with the language string
    * this class associated with. The idea is to create copies of all images from
    * the sys_resources/images/en-us directory to
    * rx_resources/images/<newlangaugestring> directory.
    * @param path must not be <code>null</code> or <code>empty</code>
    * @return the new path generated based on the old path and generation
    * scheme, may be <code>null</code> or <code>empty</code>
    */
   private String getNewImagePath(String path)
   {
      if(path == null || path.trim().length() < 1)
      {
         throw new IllegalArgumentException("path must not be empty");
      }
      StringTokenizer tokenizer = new StringTokenizer(path, File.separator, true);
      String token = null;
      String newPath = "";
      while(tokenizer.hasMoreTokens())
      {
         token = tokenizer.nextToken();
         if(token.equals("sys_resources"))
         {
            token = "rx_resources";
         }
         else if(token.equals(PSI18nUtils.DEFAULT_LANG))
         {
            token = m_languagestring;
         }
         newPath += token;
      }
      return newPath;
   }

   /**
    * Rhythmyx root directory. Initialized in the constructor,
    * never <code>null</code>.
    */
   private String m_rxroot = "";
   /**
    * New language string. Initialized in the constructor. Never <code>null</code>
    * or <code>empty</code>
    */
   private String m_languagestring = "";

   /**
    * Name of the JavaScript system resource folder relative to the Rhythmyx
    * root.
    */
   static public final String SYSTEM_JS_DIR = "sys_resources" + File.separator
      + "js";

   /**
    * Name of the JavaScript rx resource folder relative to the Rhythmyx root.
    */
   static public final String RX_JS_DIR = "rx_resources" + File.separator
      + "js";

   /**
    * Name of the CSS system resource folder relative to the Rhythmyx root.
    */
   static public final String SYSTEM_CSS_DIR = "sys_resources" + File.separator
      + "css";

   /**
    * Name of the CSS rx resource folder relative to the Rhythmyx  root.
    */
   static public final String RX_CSS_DIR = "rx_resources" + File.separator
      + "css";

   /**
    * Name of the Images system resource folder relative to the Rhythmyx root.
    */
   static public final String SYSTEM_IMAGE_DIR = "sys_resources" + File.separator
      + "images";

   /**
    * Name of the Images rx resource folder relative to the Rhythmyx root.
    */
   static public final String RX_IMAGE_DIR = "rx_resources" + File.separator
      + "images";

   /**
    * Name of the only JavaScript message map file. This file will have locale
    * specific message map.
    */
   static public final String JS_ERROR_MESSAGE_FILE =
      "globalErrorMessages.js";

   /**
    * Name of the only CSS template file in Rhythmyx.
    */
   static public final String CSS_TEMPLATE_FILE = "templates.css";

   /**
    * Name of the user options xml file that needs to be copies.
    */
   static public final String USER_OPTIONS_FILE = "UserOptions.xml";

   /**
    * Main method for testing purpose.
    * @param args
    */
   public static void main(String[] args)
   {
      PSLocaleRxResourceCopyHandler localeRxResourceCopyHandler =
         new PSLocaleRxResourceCopyHandler("d:/Rhythmyx", "fr-fr");
      try
      {
          localeRxResourceCopyHandler.processResourceCopy();
      }
      catch(IOException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
}
