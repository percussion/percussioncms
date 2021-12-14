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
package com.percussion.ant.install;

import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.utils.xml.PSEntityResolver;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This task is used to set static properties which can be accessed by all
 * subclasses.
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="action"
 *              class="com.percussion.ant.install.PSAction"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to set the properties and execute.
 *
 *  <code>
 *  &lt;action rootDir="C:/Rhythmyx"/&gt;
 *  </code>
 *
 * </pre>
 *
 * @author peterfrontiero
 */
public class PSAction extends Task
{
   /**
    * This will handle initialization of the install logger, loading of
    * PreviousVersion.properties for upgrades, and setting of the entity
    * resolver's resolution home used to find DTD's.  It also determines if
    * all files should be refreshed by date.
    */
   public void execute() throws BuildException
   {
      PSLogger.init(ms_rootDir);
      InstallUtil.writePreviousVersion(ms_rootDir);
      // If not set saxon get used for sax parser which fails with tika and
      // possibly other code.  This usually gets called in PSFilter for any
      // regular request to the server.
      PSEntityResolver.setResolutionHome(new File(ms_rootDir));
      System.setProperty("javax.xml.parsers.SAXParserFactory",
            "com.percussion.xml.PSSaxParserFactoryImpl");
      System.setProperty("javax.xml.transform.TransformerFactory",
            "com.icl.saxon.TransformerFactoryImpl");

       String derbyHome = System.getProperty("derby.system.home");
       if (derbyHome == null || Files.notExists(Paths.get(derbyHome)))
       {
           derbyHome = ms_rootDir+File.separator+"Repository";
           log("Setting derby home to "+derbyHome);
           System.setProperty("derby.system.home",derbyHome);

       }
      // So tasks can find saxon without it being in the system class loader
      
      Thread.currentThread().setContextClassLoader(PSAction.class.getClassLoader());

      
      Target t = getOwningTarget();
      Project p = t.getProject();
      String refreshProp = p.getProperty(m_refreshProperty);
      if (refreshProp != null)
         ms_bRefreshFiles = true;
   }

   /**
    * Converts the given absolute file path into a url.
    *
    * @param file the absolute path to the file.
    *
    * @return the file url representation of the path, may be <code>null</code>.
    */
   protected URL getResource(String file)
   {
      URL fileUrl = null;

      try
      {
         fileUrl = (new File(file)).toURI().toURL();
      }
      catch (MalformedURLException e)
      {
         PSLogger.logError("ERROR: " + e.getMessage());
      }

      return fileUrl;
   }

   /**
    * Accessor for the root installation directory.
    *
    * @return the Rhythmyx root installation directory, may be <code>null</code>.
    */
   protected String getRootDir()
   {
      return ms_rootDir;
   }

   /**
    * Utility method to convert a string of comma-separated values into an
    * array.
    *
    * @param arrayVals the comma-separated values, may be <code>null</code> or
    * empty.
    * @return an array of string values, never <code>null</code>, may be empty.
    */
   protected String[] convertToArray(String arrayVals)
   {
      String[] arr;

      if (arrayVals == null || arrayVals.trim().length() == 0)
         arr = new String[0];
      else
      {
         String[] vals = arrayVals.split(",");
         if (vals.length == 0)
         {
            arr = new String[1];
            arr[0] = arrayVals.trim();
         }
         else
         {
            arr = new String[vals.length];
            for (int i = 0; i < vals.length; i++)
               arr[i] = vals[i].trim();
         }
      }

      return arr;
   }

   /**
    * Accessor for the refresh files flag
    * 
    * @return see {@link #ms_bRefreshFiles}.
    */
   public static boolean refreshFiles()
   {
      return ms_bRefreshFiles;
   }
   
   /**
    * Sets the root installation directory.
    *
    * @param installLocation the absolute install location.
    */
   public void setRootDir(String installLocation)
   {
       ms_rootDir = installLocation;
       System.setProperty("rxdeploydir",installLocation);
       Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
   }

   /**
    * Sets the refresh property name.
    * 
    * @param refreshProperty the name of the property which indicates a refresh
    * should be performed.
    */
   public void setRefreshProperty(String refreshProperty)
   {
      m_refreshProperty = refreshProperty;
   }

   /**
    * Sets wether the action should fail on error.
    *
    * @param failonerror
    */
   public void setFailonerror(boolean failonerror){
      m_failonerror = failonerror;
   }

   public void setSilenceErrors(boolean silenceErrors){
      m_silenceerrors = silenceErrors;
   }

   public boolean isFailonerror(){
      return m_failonerror;
   }

   public boolean isSilenceErrors(){
      return m_silenceerrors;
   }
   /**
    * The root installation directory.
    */
   private static volatile String ms_rootDir;

   
   /**
    * If <code>true</code>, then all file copy operations using the
    * {@link PSCopyFileAction} task will be performed as replace by date.  
    * Defaults to <code>false</code>.
    */
   private static volatile boolean ms_bRefreshFiles = false;

   /**
    * If false then the Action should not fail if an error occurs, when true (default) errors
    * should be reported and fail the task.
    */
   private static volatile boolean m_failonerror = true;


   /***
    * If true then the Action should not report any errors to logs.
    */
   private static volatile boolean m_silenceerrors = false;

   /**
    * The name of the Ant property which if found indicates that a refresh of
    * all files will be performed.  Default is REFRESH.
    */
   private String m_refreshProperty = "REFRESH";
}
