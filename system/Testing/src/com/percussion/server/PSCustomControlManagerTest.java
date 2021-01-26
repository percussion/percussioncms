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
 
package com.percussion.server;

import com.percussion.util.IOTools;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the custom control manager.
 */
public class PSCustomControlManagerTest
{
   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();
   private String rxdeploydir;

   @Before
   public void setup() throws IOException {

      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir", temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }

   public PSCustomControlManagerTest(){}

   private boolean matchString(Set<String> set, String compareTo){
      boolean matched = false;
      for(String s : set){
         if(s.equalsIgnoreCase(compareTo)){
            matched = true;
            break;
         }
      }
      return matched;
   }
   /**
    * Tests all control mgr functionality
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testAll() throws Exception
   {
      FileWriter fw = null;
      PSCustomControlManager ctrlMgr = null;
            
      try
      {
         // create temporary directories
         File sysStylesheetsDir = new File(temporaryFolder.getRoot().getAbsolutePath() + '/' +
               PSCustomControlManager.SYS_STYLESHEETS_DIR);
         sysStylesheetsDir.deleteOnExit();
         assertTrue(sysStylesheetsDir.mkdirs());

         File rxResourcesDir = new File(temporaryFolder.getRoot().getAbsolutePath() + "/" + PSCustomControlManager.CUSTOM_CONTROLS_DIR);
         rxResourcesDir.deleteOnExit();
         assertTrue(rxResourcesDir.mkdirs());

         File control1 = new File(temporaryFolder.getRoot().getAbsolutePath() + "/" + PSCustomControlManager.CUSTOM_CONTROLS_DIR,"control1.xsl");
         control1.deleteOnExit();
         IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/server/rx_resources/stylesheets/controls/control1.xsl"),control1);

         File control2 = new File(temporaryFolder.getRoot().getAbsolutePath() + "/" + PSCustomControlManager.CUSTOM_CONTROLS_DIR,"control2.xsl");
         control2.deleteOnExit();
         IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/server/rx_resources/stylesheets/controls/control2.xsl"),control2);

         File control4 = new File(temporaryFolder.getRoot().getAbsolutePath() + "/" + PSCustomControlManager.CUSTOM_CONTROLS_DIR,"control4.xsl");
         control4.deleteOnExit();
         IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/server/rx_resources/stylesheets/controls/control4.xsl"),control4);

         File control5 = new File(temporaryFolder.getRoot().getAbsolutePath() + "/" + PSCustomControlManager.CUSTOM_CONTROLS_DIR,"control5.xsl");
         control5.deleteOnExit();
         IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/server/rx_resources/stylesheets/controls/control5.xsl"),control5);

         File control6 = new File(temporaryFolder.getRoot().getAbsolutePath() + "/" + PSCustomControlManager.CUSTOM_CONTROLS_DIR,"control6.xsl");
         control6.deleteOnExit();
         IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/server/rx_resources/stylesheets/controls/control6.xsl"),control6);


         // create temporary imports file
         File importsFile = new File(sysStylesheetsDir.getAbsolutePath(),"customControlImports.xsl");
         importsFile.deleteOnExit();
         fw = new FileWriter(importsFile);
         fw.write(CONTROL_IMPORTS_CONTENT);
         fw.close();


         File activeEdit = new File(sysStylesheetsDir.getAbsolutePath(),"/activeEdit.xsl");
         assertTrue(activeEdit.createNewFile());
         activeEdit.deleteOnExit();
         IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/server/sys_resources/stylesheets/activeEdit.xsl"),activeEdit);

         File singleFieldEdit = new File(sysStylesheetsDir.getAbsolutePath(), "/singleFieldEdit.xsl");
         assertTrue(singleFieldEdit.createNewFile());
         singleFieldEdit.deleteOnExit();
         IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/server/sys_resources/stylesheets/singleFieldEdit.xsl"),singleFieldEdit);

         // get custom control manager, initialize
         ctrlMgr = PSCustomControlManager.getInstance();
         ctrlMgr.init(temporaryFolder.getRoot());

         // initially two imports
         Set<String> imports = ctrlMgr.getImports();
         assertEquals(2, imports.size());
         String ctrlFile1Path = control1.getAbsolutePath();
         String ctrlFile2Path = control2.getAbsolutePath();


         assertTrue(matchString(imports,ctrlMgr.createImport(ctrlFile1Path)));
         assertTrue(matchString(imports,ctrlMgr.createImport(ctrlFile2Path)));
         
         // check multiple calls to init
         boolean didThrow = false;
         try
         {
            ctrlMgr.init(temporaryFolder.getRoot());
         }
         catch (IllegalStateException e)
         {
            didThrow = true;
         }
         assertTrue(didThrow);

      }
      finally
      {
         if (fw != null)
         {
            try
            {
               fw.close();
            }
            catch (IOException e)
            {
               
            }
         }
      }
   }               
      
   @After
   public void tearDown()
   {
      File resourcesDir = new File(RESOURCE_PATH + "/sys_resources");
      IOTools.deleteFile(resourcesDir);
      
      IOTools.deleteFile(CTRL_FILE3);
   }
   
   /**
    * Defines the path to the files used by this unit test.
    */
   private static final String RESOURCE_PATH =
      "/com/percussion/server/";
  
   /**
    * The initial custom control imports file content.
    */   
   private static final String CONTROL_IMPORTS_CONTENT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
      "<!DOCTYPE xsl:stylesheet [\n" +
         "<!ENTITY % HTMLlat1 SYSTEM \"/Rhythmyx/DTD/HTMLlat1x.ent\">\n" +
            "%HTMLlat1;\n" +
         "<!ENTITY % HTMLsymbol SYSTEM \"/Rhythmyx/DTD/HTMLsymbolx.ent\">\n" +
            "%HTMLsymbol;\n" +
         "<!ENTITY % HTMLspecial SYSTEM \"/Rhythmyx/DTD/HTMLspecialx.ent\">\n" +
            "%HTMLspecial;\n" +
      "]>\n" +
      "<xsl:stylesheet version=\"1.1\" " +
      "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" " +
      "xmlns:psxctl=\"URN:percussion.com/control\" " +
      "xmlns=\"http://www.w3.org/1999/xhtml\" " +
      "exclude-result-prefixes=\"psxi18n\" " +
      "xmlns:psxi18n=\"urn:www.percussion.com/i18n\" >\n" +
      "</xsl:stylesheet>";
   
   /**
    * The name of the temporary control.
    */
   private static final String CTRL3_NAME = "control3";
   
   /**
    * The relative path of the temporary control file.
    */
   private static final String CTRL_FILE3_PATH = 
      PSCustomControlManager.CUSTOM_CONTROLS_DIR + '/' + CTRL3_NAME + ".xsl";
   
   /**
    * The temporary control file.
    */
   private static final File CTRL_FILE3 = new File(RESOURCE_PATH + '/'
      + CTRL_FILE3_PATH);
}