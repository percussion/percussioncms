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
package com.percussion.ant;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author dougrand
 *
 * Check the tmx file for correct data. The classes in the tmx file must
 * actually exist. Spit out errors for the ones that don't exist.
 */
public class PSCheckTmxFile extends Task
{

   private static final Logger log = LogManager.getLogger(PSCheckTmxFile.class);

   public String getClassdir()
   {
      return m_classdir;
   }
   
   public void setClassdir(String classPath)
   {
      m_classdir = classPath;
   }
   
   public String getTmxPath()
   {
      return m_tmxPath;
   }
   
   public void setTmxPath(String tmxPath)
   {
      m_tmxPath = tmxPath;
   }
   
   public void setFailOnError(boolean fail)
   {
      m_failOnError = fail;
   }
   
   /**
    * Sets a list of property file paths that will be checked agaist the tmx file
    * for duplicate entries.
    * 
    * @param files a comma delimited list of property file paths. May
    * be empty. Assumed to never be <code>null</code>
    */
   public void setPropertyFiles(String files)
   {
      if(files.trim().length() == 0)
         return;
      StringTokenizer st = new StringTokenizer(files, ",");
      String token = null;
      while(st.hasMoreTokens())
      {
         token = st.nextToken().trim();
         if(token.length() > 0)
            m_propertyFiles.add(token);
      }      
   }
   
   /*
    *  (non-Javadoc)
    * @see org.apache.tools.ant.Task#execute()
    */
   public void execute() throws BuildException
   {
      try
      {
         // Load the tmx file
         DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                 new PSXmlSecurityOptions(
                         true,
                         true,
                         true,
                         false,
                         true,
                         false
                 ));

         DocumentBuilder builder = dbf.newDocumentBuilder();
         File tmxfile = new File(m_tmxPath);
         
         handleOutput("tmx file: " + m_tmxPath);
         handleOutput("classdir: " + m_classdir);
         
         if (tmxfile.exists() == false)
         {
            throw new BuildException("Can't find tmx file");
         }
         File classdir = new File(m_classdir);
         if (classdir.exists() == false)
         {
            throw new BuildException("Can't find the classpath");
         }
         
         Document doc = builder.parse(tmxfile);
         // Get all the tu elements and walk them
         NodeList tuElements = doc.getElementsByTagName("tu");
         int length = tuElements.getLength();
         
         boolean firsterror = true;
         for(int i = 0; i < length; i++)
         {
            Element tu = (Element) tuElements.item(i);
            String tuid = tu.getAttribute("tuid");
            
            // Add id to the tmx id list
            if(tuid != null)
               m_tmxIds.add(tuid);
            
            // Check all tuids that start with com.percussion and are therefore
            // references to classes. Example:
            // <tu tuid="com.percussion.cx.PSSearchDialog@Content Search">
            if (tuid != null && tuid.startsWith("com.percussion."))
            {
               // Get the classname, always before the @ sign
               int atsign = tuid.indexOf('@');
               if (atsign > 0)
               {
                  String classname = tuid.substring(0, atsign);
                  
                  // Don't check if there isn't a PSclassname in the
                  // string.
                  if (classname.indexOf(".PS") < 0) continue;
                  
                  // Convert the classname to a pathname by substituting 
                  // . to /
                  String classpath = classname.replace('.', '/') + ".class";
                  
                  File classfile = new File(classdir, classpath);
                  if (classfile.exists() == false)
                  {
                     if (firsterror)
                     {
                        firsterror = false;
                        handleErrorOutput("The following resources reference " +
                              "missing classes: \n");
                     }
                     handleErrorOutput(tuid);
                     handleErrorOutput("\n");
                  }                  
               }
            }
         }
         if (!firsterror)
         {
            handleErrorFlush("\n");
            m_foundErrors = true;
         }
         if(m_propertyFiles.size() > 0)
            checkForDupesInPropertyFiles();
         if(m_foundErrors && m_failOnError)
            throw new BuildException("Tmx Checker Validation failure.");
      }
      catch (BuildException be)
      {
         throw be;
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
      
   }
   
   /**
    * Checks for duplicate entries (i.e. entries sharing the same key) between
    * the specified property files and the TMX file.
    * @throws BuildException if an IO error occurs
    */
   private void checkForDupesInPropertyFiles()
      throws BuildException
   {
      Iterator it = m_propertyFiles.iterator();
      File file = null;
      Properties props = null;
      boolean foundErrors = false;
      
      while(it.hasNext())
      {
         boolean firstError = true;
         file = new File((String)it.next());
         if(!file.exists())
            throw new BuildException(
               "Property file does not exist: " + file.getAbsolutePath());
         try
         {
            props = loadProperties(file);
            Enumeration keys = props.keys();
            while(keys.hasMoreElements())
            {
               String key = (String)keys.nextElement();
               if(m_tmxIds.contains(key))
               {
                  if(firstError)
                  {
                     firstError = false;
                     foundErrors = true;
                     handleErrorOutput("Property file '" 
                        + file.getName()
                        + "' contains the following keys that also exist in"
                        + " the Tmx file:\n");
                  }
                  handleErrorOutput("\t" + key);                  
               }
            }
         }
         catch (IOException e)
         {
           throw new BuildException(
              "IO Error on property file load: " + e.getLocalizedMessage());
         }
         
      }
      if(foundErrors)
      {
         handleErrorFlush("\n");
         m_foundErrors = true;
      }
   }
   
   /**
    * Loads properties file into a <code>Properties</code>
    * object.
    * 
    * @param file the properties file to be loaded, assumed
    * not <code>null</code>.
    */
   private Properties loadProperties(File file)
      throws IOException
   {
      Properties props = new Properties();
      
      try(FileInputStream in = new FileInputStream(file) )
      {
         props.load(in);
      }
      
      return props;
   }
   
   /**
    * Where to find the tmx source file
    */
   private String m_tmxPath = null;
   
   /**
    * Class path directory
    */
   private String m_classdir = null;
   
   /**
    * List of property files to compare against the tmx file
    * looking for duplicates. Never <code>null</code>, may be empty.
    */
   private List<String> m_propertyFiles = new ArrayList<String>();
   
   /**
    * List of all ids in the tmx file, the values are added in the
    * {@link #execute()} method. Never <code>null</code>.
    */
   private List<String> m_tmxIds = new ArrayList<String>();
   
   /**
    * Flag indicating that we should fail on error. Defaults to <code>true</code>.
    */
   private boolean m_failOnError = true;
   
   /**
    * Flag indicating that errors were found 
    */
   private boolean m_foundErrors = false;
}
