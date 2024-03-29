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
package com.percussion.ant;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This task gets the currently installed version in upgrade scenarios and
 * sets the ant property perc.previous.version
 * 
 * <br>
 * Example Usage: <br>
 * 
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="PSCheckVersion"
 *             class="com.percussion.ant.PSCheckVersion"
 *             classpath="c:\lib"/&gt;
 *  </code>
 * 
 * <code>  
 *  &lt;PSCheckVersion
 *        root="c:/Rhythmyx"
 *        jarFile="c:/lib/rxserver.jar"
 *        operation="install"/&gt;
 *  </code>
 */
public class PSCheckVersion extends Task
{

   private static final Logger log = LogManager.getLogger(PSCheckVersion.class);

   @Override
   public void execute() throws BuildException
   {
      Properties rxVersionProps = loadVersionProperties();
      InputStream in = null;
      JarFile jar = null;

      try
      {
         File jarFile = new File(m_jarFile);

         if (jarFile.exists())
         {
            jar = new JarFile(jarFile);
            JarEntry jarEntry = jar.getJarEntry(VERSION_FILE);
            if (jarEntry != null)
            {
               in = jar.getInputStream(jarEntry);
               Properties jarProps = new Properties();
               jarProps.load(in);

               String rxVersion = getVersion(rxVersionProps);
               String jarVersion = getVersion(jarProps);

               // check version compatibility
               if (!jarVersion.equals(rxVersion) && !rxVersion.equals("5.3.14[20160622]"))
               {
                  throw new Exception("The current patch version (" + jarVersion + ") is not compatible with the "
                        + "version of CMS (" + rxVersion + ") found at " + m_root);
               }

               String rxOptId = getOptionalId(rxVersionProps);
               String jarOptId = getOptionalId(jarProps);

               // versions match, check optional id's based on type of operation
               if (m_operation.equals(INSTALL_OP))
               {
                  if (jarOptId.equals(rxOptId))
                  {
                     // patch is installed
                     throw new Exception("The current patch (" + jarOptId + ") is installed on the CM1 "
                           + "installation found at " + m_root + ".  This "
                           + "patch must be uninstalled before it can be " + "installed again.");
                  }
               }
               else if (m_operation.equals(UNINSTALL_OP))
               {
                  if (!jarOptId.equals(rxOptId))
                  {
                     // patch is not installed
                     throw new Exception("The current patch (" + jarOptId + ") does not match the patch currently "
                           + "installed to the CM1 installation found at " + m_root + " (" + rxOptId + ")");
                  }
               }
            }
            else
            {
               throw new Exception("Could not locate " + VERSION_FILE + " in " + m_jarFile);
            }
         }
         else
         {
            throw new Exception(m_jarFile + " does not exist");
         }
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new BuildException(e);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               jar.close();
               in.close();
            }
            catch (IOException e)
            {
               log.error(PSExceptionUtils.getMessageForLog(e));
               log.debug(PSExceptionUtils.getDebugMessageForLog(e));
               log.info("An unexpected error occurred: " + e.getMessage());
            }
         }
      }
   }

   /**
    * Locates the Version.properties, the order of lookup is as follows:
    * 1) rxapp.ear/rxapp.war/WEB-INF/lib/rxserver.jar
    * 2) RxServices.war/WEB-INF/lib/rxclient.jar
    * 3) com.percussion.client_1.0.0/lib/rxclient.jar
    * 
    * @return properties object containing version information, never
    *         <code>null</code>, may be empty.
    */
   private Properties loadVersionProperties()
   {
      InputStream in = null;
      JarFile jar = null;
      JarEntry jarEntry = null;
      File versionFile = null;
      File versionStagingFile = null;

      try
      {
         // Look in CMLightMain jar
         File jarFile = new File(m_root + File.separator + SERVER_JAR_FILE);

         if (jarFile.exists())
         {
            jar = new JarFile(jarFile);
            jarEntry = jar.getJarEntry(VERSION_FILE);
         } else {
                  // check root, if Version.properties is there, we will use it
                  // instead.
                  versionFile = new File(m_root + File.separator + VERSION_PROPERTIES);
                  if (versionFile.exists())
                  {
                     Properties rawVersionProperties = new Properties();
                     FileInputStream versionfileStream = new FileInputStream(versionFile);
                     rawVersionProperties.load(versionfileStream);
                     versionfileStream.close();
                     return rawVersionProperties;
                  }
                  else {
                     // Check Staging directory to see if we are only a
                     // directory install.
                     versionStagingFile = new File(m_root + File.separator + DTS_STAGING + File.separator
                             + VERSION_PROPERTIES);
                     if (versionStagingFile.exists()) {
                        Properties rawVersionProperties = new Properties();
                        FileInputStream versionStagingfileStream = new FileInputStream(versionStagingFile);
                        rawVersionProperties.load(versionStagingfileStream);
                        versionStagingfileStream.close();
                        return rawVersionProperties;
                     } else {
                        throw new BuildException("Could not locate " + VERSION_PROPERTIES + " under " + m_root + " or "
                                + m_root + File.separator + DTS_STAGING);
                     }
                  }
         }

         if (jarEntry != null)
         {
            in = jar.getInputStream(jarEntry);
            Properties verProp = new Properties();
            verProp.load(in);
            return verProp;
         }
         else
         {
            throw new BuildException("Could not locate " + VERSION_FILE + " under " + m_root);
         }
      }
      catch (IOException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new BuildException("Error loading " + VERSION_FILE);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               jar.close();
               in.close();
            }
            catch (IOException e)
            {
               log.error(PSExceptionUtils.getMessageForLog(e));
               log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
         }
      }
   }

   /**
    * Gets the version information from a given set of properties.
    * 
    * @param p the properties containing the version information, assumed not
    *           <code>null</code>.
    * 
    * @return the version information in the following format:
    *         {major}.{minor}.{micro}[buildNumber]
    */
   private String getVersion(Properties p)
   {
      String major = p.getProperty("majorVersion");
      String minor = p.getProperty("minorVersion");
      String micro = p.getProperty("microVersion");
      String buildNumber = p.getProperty("buildNumber");

      String version = major + "." + minor + "." + micro;

      version += "[" + buildNumber + "]";

      return version;
   }

   /**
    * Gets the optional id from a given set of properties.
    * 
    * @param p the properties containing the version information, assumed not
    *           <code>null</code>.
    * 
    * @return the optional id.
    */
   private String getOptionalId(Properties p)
   {
      return p.getProperty("optionalId");
   }

   public void setRoot(String root)
   {
      if (root == null || root.trim().length() == 0)
      {
         throw new IllegalArgumentException("root may not be null or empty");
      }

      m_root = root;
   }

   public void setJarFile(String jarFile)
   {
      if (jarFile == null || jarFile.trim().length() == 0)
      {
         throw new IllegalArgumentException("jarFile may not be null or empty");
      }

      m_jarFile = jarFile;
   }

   public void setOperation(String operation)
   {
      if (operation == null || !operation.equalsIgnoreCase(INSTALL_OP) && !operation.equalsIgnoreCase(UNINSTALL_OP))
      {
         throw new IllegalArgumentException("operation may not be null and " + "must be either install or uninstall");
      }

      m_operation = operation;
   }

   /**
    * The Rhythmyx root directory.
    */
   private String m_root = "C:/Rhythmyx";

   /**
    * The location of the jar file, default is rxserver.jar.
    */
   private String m_jarFile = "rxserver.jar";

   /**
    * The type of patch operation to be performed if the version checking
    * succeeds. Valid values are {@link #INSTALL_OP} and {@link #UNINSTALL_OP}.
    * Defaults to {@link #INSTALL_OP}.
    */
   private String m_operation = "install";

   /**
    * The jar file to read the version properties from, relative to the Rhythmyx
    * root of a server installation.
    */
   private static final String SERVER_JAR_FILE = "jetty/base/webapps/Rhythmyx/WEB-INF/lib/perc-system-*.jar";

   /**
    * Directly read the version.properties if you fail to find it in the root of
    * the installation.
    */
   private static final String DTS_STAGING = "Staging";

   private static final String VERSION_PROPERTIES = "Version.properties";

   /**
    * The name of the properties file containing the version.
    */
   private static final String VERSION_FILE = "com/percussion/util/Version.properties";

   /**
    * Install operation.
    */
   private static final String INSTALL_OP = "install";

   /**
    * Uninstall operation.
    */
   private static final String UNINSTALL_OP = "uninstall";
}
