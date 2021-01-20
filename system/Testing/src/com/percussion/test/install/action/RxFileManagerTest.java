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
package com.percussion.test.install.action;

import com.percussion.install.OSEnum;
import com.percussion.install.RxFileManager;
import com.percussion.util.PSOsTool;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.*;

/***
 * Tests to cover the RxFileManager utilities.
 * 
 * @author nate
 *
 */
public class RxFileManagerTest
{

   @Test
   @Ignore("Failing on Windows")
   public void testisDTSDirValid()
   {
    assertTrue("Valid directory not detected.",RxFileManager.isDTSDir(this.getClass().getResource(".").getPath()));
    assertFalse("Invalid DTS directory returned as valid",RxFileManager.isDTSDir("this ain't a real directory!"));
    assertFalse("Null is not a valid directory",RxFileManager.isDTSDir(null));
    assertTrue("Percussion not detected as a valid DTS dir",RxFileManager.isDTSDir(this.getClass().getResource("Percussion").getPath()));
    assertTrue("Folder with spaces not detected as a valid DTS dir",RxFileManager.isDTSDir(this.getClass().getResource("New Folder").getPath().replace("%20", " ")));
    
   }
      
   /***
    * Test the installation properties file. 
    */
   @Test
   public void testInstallationPropFile(){
      
      RxFileManager o = new RxFileManager();
      
      try{
         o.setSystemInstallationPropertiesFile(null);
      }catch(IllegalArgumentException e){
         //Good
      }
      
      try{
         o.setSystemInstallationPropertiesFile("");
      }catch(IllegalArgumentException e){
         //Good
      }
      
      try{
         o.setSystemInstallationPropertiesFile("     ");
      }catch(IllegalArgumentException e){
         //Good
      }
      
      o.setSystemInstallationPropertiesFile("dtsinstallation.properties");
      assertEquals("dtsinstallation.properties", "dtsinstallation.properties");
      
   }

   /***
    * Test the 
    * @throws IOException 
    */
   @Test
   public void testSystemInstallationPropertiesAbsolute() throws IOException{
  
      Properties p = new Properties();
      URL prop_file = this.getClass().getResource("dtsinstall.properties");
      OSEnum os = OSEnum.Linux;
      
      if(PSOsTool.isWindowsPlatform())
         os = OSEnum.Windows;
      
      p.put(RxFileManager.INSTALL_PROP, prop_file.getPath().substring(prop_file.getPath().lastIndexOf("/")));
    
      //Setup the properties file
      RxFileManager.saveProperties(p,prop_file.getPath());
      RxFileManager.setSystemInstallationPropertiesFile("dtsinstall.properties");
      RxFileManager.setProgramDir(this.getClass().getResource("Program Files").getPath());
     
      os = OSEnum.Windows;
      assertEquals(this.getClass().getResource("Program Files").getPath() + File.separatorChar + "Percussion" + File.separatorChar + "dtsinstall.properties", RxFileManager.getSystemInstallationPropertiesAbsolute(os));
      
      os = OSEnum.Linux;
      assertEquals(this.getClass().getResource("Program Files").getPath() + File.separatorChar + "dtsinstall.properties", RxFileManager.getSystemInstallationPropertiesAbsolute(os));
      
      
      //Test X86
      os = OSEnum.Windows;
      RxFileManager.setProgramDir(this.getClass().getResource("Program Files (x86)").getPath());
      assertEquals(this.getClass().getResource("Program Files (x86)").getPath() + File.separatorChar + "Percussion" + File.separatorChar + "dtsinstall.properties", RxFileManager.getSystemInstallationPropertiesAbsolute(os));
     
      //Test X86
      os = OSEnum.Linux;
      RxFileManager.setProgramDir(this.getClass().getResource("Program Files (x86)").getPath());
      assertEquals(this.getClass().getResource("Program Files (x86)").getPath() + File.separatorChar + "dtsinstall.properties", RxFileManager.getSystemInstallationPropertiesAbsolute(os));
      
      
   }

   /***
    * Test retrieval of DTS system properties.
    * @throws IOException 
    */
   @Test
   @Ignore("Failing on Windows")
   public void testGetDTSSystemFileProperties() throws IOException{
 
      OSEnum os= OSEnum.Linux;
      
      if(PSOsTool.isWindowsPlatform())
         os = OSEnum.Windows;
      
      //Try reading from a typical location on windows.
      if( PSOsTool.isWindowsPlatform()){
         RxFileManager.setProgramDir(this.getClass().getResource("Program Files").getPath().replace("%20"," "));
         Properties props = RxFileManager.getDTSSystemFileProperties("dtsinstall.properties","cm1install.properties",os);
        
         assertEquals("Property file value didn't match!", "/home/percussion/DTS;",props.getProperty(RxFileManager.INSTALL_PROP,""));
         
        //Now lets test the CM1 fallback.    
         RxFileManager.setProgramDir(this.getClass().getResource("home/percussion").getPath().replace("%20"," "));
         props = RxFileManager.getDTSSystemFileProperties("dtsinstall.properties","cm1install.properties",os);
         
         assertEquals("CM1 fail over property file not matched.","C:\\Program Files\\Percussion\\;C:\\Percussion\\CM1",props.getProperty(RxFileManager.INSTALL_PROP,""));
      }else{
         //Linux
         
         
      }
   }
   


   @Test(expected = IllegalArgumentException.class)
   public void testNullProgramDir(){
      RxFileManager.setProgramDir(null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testEmptyProgramDir(){
      RxFileManager.setProgramDir("");
   }

   @Test
   public void testProgramDirTrailing(){
      RxFileManager.setProgramDir("test" + File.separator);
      assertEquals("ProgramDir should have trimmed /","test",RxFileManager.getProgramDir());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testLoadPropertiesNull() throws IOException{
      RxFileManager.loadProperties(null);
   }
  
   @Test(expected = IllegalArgumentException.class)
   public void testLoadPropertiesEmpty() throws IOException{
      RxFileManager.loadProperties("");
   }  
   
   @Test(expected = IllegalArgumentException.class)
   public void testSavePropertiesEmpty() throws IOException{
      RxFileManager.saveProperties(null,null);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void testSavePropertiesNullPropFile() throws IOException{
      RxFileManager.saveProperties(new Properties(),null);
   }
  
   @Test(expected = IllegalArgumentException.class)
   public void testSavePropertiesNullEmptyFile() throws IOException{
      RxFileManager.saveProperties(new Properties(),"");
   }
  
   @Test
   public void testRootDir(){
      @SuppressWarnings("unused")  //the constructor supports it and it is used so need to test it.
      RxFileManager r = new RxFileManager("Root"); //We know this is sketch - still needs tested as so much legacy code is present. @TODO: Rewrite the installer.
      
      assertEquals("Root should match","Root",RxFileManager.getRootDir());
      
      RxFileManager.setRootDir("NewRoot");
      assertEquals("Root should match", "NewRoot",RxFileManager.getRootDir());
   }
   
   @Test
   public void getServerConfigLocation(){
      RxFileManager r = new RxFileManager();
      
      assertEquals(r.getInstallerConfigLocation() + File.separator + RxFileManager.REPOSITORY_FILE,r.getRepositoryFile());

   }
}
