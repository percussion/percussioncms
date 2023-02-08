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

package com.percussion.cx.javafx;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @web http://java-buddy.blogspot.com/
 */
public class PSFileSaver
{
   private static final Logger log = LogManager.getLogger(PSFileSaver.class);
   private static final String LCAFILENAME = "LifecycleAnalysisTemplate.xls";
   private static final Map<String, FileChooser.ExtensionFilter> EXTMAP; //map to hold file type extensions
   String srcURL;
   String fileName;
   String fileExtension;
   
   static {
      TreeMap<String, FileChooser.ExtensionFilter> extensionTypes = new TreeMap<>();
      extensionTypes.put("*.*", new FileChooser.ExtensionFilter("All Files", "*.*"));
      extensionTypes.put(".jpg", new FileChooser.ExtensionFilter("JPG", "*.jpg"));
      extensionTypes.put(".jpeg", new FileChooser.ExtensionFilter("JPEG", "*.jpeg"));
      extensionTypes.put(".png", new FileChooser.ExtensionFilter("PNG", "*.png"));
      extensionTypes.put(".pdf", new FileChooser.ExtensionFilter("PDF", "*.pdf"));
      extensionTypes.put(".doc", new FileChooser.ExtensionFilter("Microsoft Word Document *.doc", "*.doc"));
      extensionTypes.put(".docx", new FileChooser.ExtensionFilter("Microsoft Word Document *.docx", "*.docx"));
      extensionTypes.put(".xls", new FileChooser.ExtensionFilter("Microsoft Excel Document", "*.xls"));
      extensionTypes.put(".txt", new FileChooser.ExtensionFilter("Text File", "*.txt"));
      extensionTypes.put(".css", new FileChooser.ExtensionFilter("Cascading Stylesheets File", "*.css"));
      extensionTypes.put(".html", new FileChooser.ExtensionFilter("Hyper Text Markup Language File", "*.html"));
      extensionTypes.put(".js", new FileChooser.ExtensionFilter("JavaSript File", "*.js"));
      
      EXTMAP = Collections.unmodifiableMap(extensionTypes);
   }

   /**
    * Used for LifeCycleAnalysis
    * @param srcURL url of the xls binary
    */
   public PSFileSaver(String srcURL)
   {
      this.srcURL = srcURL;
      // if we have other xls options, include them here
      if (srcURL.contains("LifecycleAnalysis"))
      {
         fileName = LCAFILENAME;
      }
   }
   
   /**
    * This constructor can be used to pass in the fileName for the save dialog
    * @param srcURL the URL of the binary
    * @param fileName the fileName should be the name of the file as well as the extension, i.e. my_file.pdf
    */
   public PSFileSaver(String srcURL, String fileName) {
      this.srcURL = srcURL;
      try
      {
         this.fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         log.error("Unable to decode file name: " + e);
      }
      int extensionIndex = StringUtils.lastIndexOf(fileName, ".");
      if(extensionIndex != -1)
         fileExtension = fileName.substring(extensionIndex).toLowerCase();
      else
         fileExtension = "";
      
   }

   public void startFileSaver()
   {
      Platform.runLater(new Runnable()
      {
         @Override
         public void run()
         {
            Stage stage = new Stage();

            showFileSaver(stage);
         }
      });
   }

   public void showFileSaver(final Stage primaryStage)
   {
      primaryStage.setTitle("File Saver");

      FileChooser fileChooser = new FileChooser();

      // Set extension filter
      for(FileChooser.ExtensionFilter extension : EXTMAP.values()) {
         fileChooser.getExtensionFilters().add(extension);
      }
      
      String home = System.getProperty("user.home");
      File defaultFolder = new File(home + "/Downloads");

      // Show save file dialog
      // Choose the file
      FileChooser.ExtensionFilter extFilter = EXTMAP.get(fileExtension);
      if(extFilter==null)
         extFilter = EXTMAP.get("*.*");
      fileChooser.setSelectedExtensionFilter(extFilter);
      fileChooser.setInitialFileName(fileName);
      fileChooser.setInitialDirectory(defaultFolder);
      File file = fileChooser.showSaveDialog(primaryStage);
      if (file != null)
      {
         saveFile(this.srcURL, file);
      }
   }

   private void saveFile(String urlString, File file)
   {
      try
      {
         URL url = new URL(urlString);
         FileUtils.copyURLToFile(url, file);
      }
      catch (IOException ex)
      {
         log.error("Unable to save file: " + ex);
      }
   }

}
