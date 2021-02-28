/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.install.RxFileManager;
import org.apache.tools.ant.taskdefs.condition.Condition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PSVersionBuildNumberProdCondition is a condition which will return
 * <code>true</code> when <code>eval</code> is invoked if the installed build on
 * the system lies between the specified minimum and maximum builds.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the typedef:
 *
 *  <code>
 *  &lt;typedef name="versionBuildNumberProdCondition"
 *              class="com.percussion.ant.install.PSVersionBuildNumberProdCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to determine if current installation version lies within
 * specified build range.
 *
 *  <code>
 *  &lt;condition property="UPGRADE_50_57"&gt;
 *     &lt;versionBuildNumberProdCondition
 *           buildFrom="-1"
 *           buildTo="-1"
 *           majorVersionFrom="5"
 *           majorVersionTo="5"
 *           minorVersionFrom="0"
 *           minorVersionTo="7"/&gt;
 *  &lt;/condition&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSVersionBuildNumberProdCondition extends PSAction
implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      String strInstallDir = getRootDir();
      return checkVersion(strInstallDir);
   }

   /**
    * Required abstract method.  Name of condition.
    * @return the name of the condition.  Cannot be <code>null</code>.
    */
   public String defaultName()
   {
      return "Rx Version Build Number Product Condition";
   }

  /**************************************************************************
  * private functions
  **************************************************************************/

  /**
   * Returns the value of the specified property <code>propName</code>, -1
   * if the property does not exist or is not an integer.
   *
   * @param propName the name of the property whose value is required, may
   * not be <code>null</code> or empty
   * @param prop the properties object from which the value of the specified
   * property is to be obtained, may not be <code>null</code>
   *
   * @return the value of the specifed property, -1 if the specified property
   * does not exist or if any error occurs converting the property value to
   * integer.
   */
   private int getRequiredProperty(String propName, Properties prop)
   {
      if ((propName == null) || (propName.trim().length() < 1))
         throw new IllegalArgumentException("propName may not be null or empty");
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null");

      String strValue = prop.getProperty(propName);
      if ((strValue == null) || (strValue.trim().length() < 1))
      {
         PSLogger.logInfo(
            "Value of property : " + propName + " is null or empty.");
         return -1;
      }

      int value = -1;
      try
      {
         value = Integer.parseInt(strValue);
      }
      catch (Throwable t)
      {
         PSLogger.logInfo(
            "Value of property : " + propName + " is invalid.");
         PSLogger.logInfo(t.getLocalizedMessage());
         return -1;
      }
      return value;
   }

   /**
    * Checks if the installed build on the system lies between the specified
    * builds.
    *
    * @param strInstallDir the install directory, may be <code>null</code>.
    *
    * @return <code>true</code> if the installed build on the system lies
    * between the specified builds, <code>false</code> otherwise.
    */
   public synchronized boolean checkVersion(String strInstallDir)
   {
      int installMajorVersion = -1;
      int installMinorVersion = -1;
      int installMicroVersion = -1;
      int installBuild = -1;

      if (m_versionProps == null)
      {
         InputStream ins = null;
         try
         {
            if (strInstallDir == null)
               return false;

            if (!strInstallDir.endsWith(File.separator))
               strInstallDir += File.separator;

            // check if the "version.properties" file exists under the Rhythmyx
            // root directory
            File propFile = new File(strInstallDir +
               RxFileManager.PREVIOUS_VERSION_PROPS_FILE);
            if (propFile.exists() && propFile.isFile())
            {
               // load the version.properties file
               ins = new FileInputStream(propFile);
               m_versionProps = new Properties();
               m_versionProps.load(ins);
            }
         }
         catch(Exception e)
         {
            PSLogger.logInfo("ERROR : " + e.getMessage());
            PSLogger.logInfo(e);
            return false;
         }
         finally
         {
            if (ins != null)
            {
               try
               {
                  ins.close();
               }
               catch(IOException e)
               {
               }
            }
         }
      }

      if (m_versionProps != null)
      {
         installMajorVersion = getRequiredProperty(
            "majorVersion", m_versionProps);

         installMinorVersion = getRequiredProperty(
            "minorVersion", m_versionProps);

         installMicroVersion = getRequiredProperty(
               "microVersion", m_versionProps);
         
         installBuild = getRequiredProperty(
            "buildNumber", m_versionProps);
      }

      if ((installMajorVersion == -1) ||
         (installMinorVersion == -1) ||
         (installBuild == -1))
            return false;

      if (installMajorVersion < majorVersionFrom)
         return false;

      if (installMajorVersion == majorVersionFrom)
      {
         // check the minor version in this case
         if (installMinorVersion < minorVersionFrom)
            return false;

         if (installMinorVersion == minorVersionFrom)
         {
            
            if (installMicroVersion < microVersionFrom)
               return false;

            if (microVersionFrom<=0 || installMicroVersion == microVersionFrom)
            {
               
            // check the build number in this case
            if (installBuild < buildFrom)
               return false;
         }
      }
      }

      if (installMajorVersion > majorVersionTo)
         return false;

      if (installMajorVersion == majorVersionTo)
      {
         // check the minor version in this case
         if (installMinorVersion > minorVersionTo)
            return false;

         if (installMinorVersion == minorVersionTo)
         {
            
            // check the minor version in this case
            if (installMicroVersion > microVersionTo)
               return false;

            if (installMinorVersion == microVersionTo)
            {
               
               if (microVersionTo==-1 || installMinorVersion > microVersionTo)
            // check the build number in this case
            if ((buildTo != -1) && (installBuild > buildTo))
               return false;
         }
      }
      }

      return true;
   }

  /**************************************************************************
  * Bean property Accessors and Mutators
  **************************************************************************/

   /**
   * The minimum major version number which can be upgraded by the build
   * currently being installed.
   *
   * @return the minimum major version number which can be upgraded by the build
   * currently being installed.
   */
   public synchronized int getMajorVersionFrom()
   {
      return majorVersionFrom;
   }

   /**
   * Sets the minimum major version number which can be upgraded by the build
   * currently being installed.
   *
   * @param aMajorVersionFrom minimum major version number which can be
   * upgraded by the build currently being installed.
   */
   public synchronized void setMajorVersionFrom(int aMajorVersionFrom)
   {
      majorVersionFrom = aMajorVersionFrom;
   }

   /**
   * The minor version corresponding to <code>majorVersionFrom</code>.
   *
   * @return the minor version corresponding to <code>majorVersionFrom</code>.
   */
   public synchronized int getMinorVersionFrom()
   {
      return minorVersionFrom;
   }

   /**
   * Sets the minor version corresponding to <code>majorVersionFrom</code>.
   *
   * @param aMinorVersionFrom the minor version corresponding to
   * <code>majorVersionFrom</code>.
   */
   public synchronized void setMinorVersionFrom(int aMinorVersionFrom)
   {
      minorVersionFrom = aMinorVersionFrom;
   }

   /**
   * The build number corresponding to <code>majorVersionFrom</code> and
   * <code>minorVersionFrom</code>
   *
   * @return the build number corresponding to <code>majorVersionFrom</code> and
   * <code>minorVersionFrom</code>
   */
   public synchronized int getBuildFrom()
   {
      return buildFrom;
   }

   /**
   * Sets the build number corresponding to <code>majorVersionFrom</code> and
   * <code>minorVersionFrom</code>
   *
   * @param aBuildFrom the build number corresponding to
   * <code>majorVersionFrom</code> and <code>minorVersionFrom</code>
   */
   public synchronized void setBuildFrom(int aBuildFrom){
      buildFrom = aBuildFrom;
   }

   /**
   * The maximum major version number which can be upgraded by the build
   * currently being installed.
   *
   * @return the maximum major version number which can be upgraded by the build
   * currently being installed.
   */
   public synchronized int getMajorVersionTo()
   {
      return majorVersionTo;
   }

   /**
   * Sets the maximum major version number which can be upgraded by the build
   * currently being installed.
   *
   * @param aMajorVersionTo the maximum major version number which can be
   * upgraded by the build currently being installed.
   */
   public synchronized void setMajorVersionTo(int aMajorVersionTo)
   {
      majorVersionTo = aMajorVersionTo;
   }

   /**
   * The minor version corresponding to <code>majorVersionTo</code>.
   *
   * @return the minor version corresponding to <code>majorVersionTo</code>.
   */
   public synchronized int getMinorVersionTo()
   {
      return minorVersionTo;
   }

   /**
   * Sets the minor version corresponding to <code>majorVersionTo</code>.
   *
   * @param aMinorVersionTo the minor version corresponding to
   * <code>majorVersionTo</code>.
   */
   public synchronized void setMinorVersionTo(int aMinorVersionTo)
   {
      minorVersionTo = aMinorVersionTo;
   }

   /**
   * Sets the minor version corresponding to <code>majorVersionTo</code>.
   *
   * @param aMicroVersionTo the minor version corresponding to
   * <code>majorVersionTo</code>.
   */
   public synchronized void setMicroVersionTo(int aMicroVersionTo)
   {
      microVersionTo = aMicroVersionTo;
   }

   /**
    * The minor version corresponding to <code>majorVersionTo</code>.
    *
    * @return the minor version corresponding to <code>majorVersionTo</code>.
    */
    public synchronized int getMicroVersionTo()
    {
       return microVersionTo;
    }



   /**
   * The build number corresponding to <code>majorVersionTo</code> and
   * <code>minorVersionTo</code>
   *
   * @return the build number corresponding to <code>majorVersionTo</code> and
   * <code>minorVersionTo</code>
   */
   public synchronized int getBuildTo()
   {
      return buildTo;
   }

   /**
   * Sets the build number corresponding to <code>majorVersionTo</code> and
   * <code>minorVersionTo</code>
   *
   * @param aBuildTo the build number corresponding to
   * <code>majorVersionTo</code> and <code>minorVersionTo</code>
   */
   public synchronized void setBuildTo(int aBuildTo)
   {
      buildTo = aBuildTo;
   }

  /**************************************************************************
  * Bean properties
  **************************************************************************/

   /**
    * The minimum major version number which can be upgraded by the build
    * currently being installed.
    */
   private int majorVersionFrom = 4;

   
   /**
    * The minor version corresponding to <code>majorVersionFrom</code>.
    */
   private int minorVersionFrom = 0;

   /**
    * The minimum micro version number which can be upgraded by the build
    * currently being installed.
    */
   private int microVersionFrom = 0;
   
   /**
    * The build number corresponding to <code>majorVersionFrom</code> and
    * <code>minorVersionFrom</code>
    */
   private int buildFrom = 20011114;

   /**
    * The maximum major version number which can be upgraded by the build
    * currently being installed.
    */
   private int majorVersionTo = 4;

   /**
    * The micro version corresponding to <code>majorVersionTo</code>.
    */
   private int minorVersionTo = 51;

   /**
    * The maximum major version number which can be upgraded by the build
    * currently being installed.
    */
   private int microVersionTo = -1;

   /**
    * The build number corresponding to <code>majorVersionTo</code> and
    * <code>minorVersionTo</code>
    */
   private int buildTo = -1;

  /**************************************************************************
  * member variables
  **************************************************************************/

   /**
    * In memory representation of "version.properties" file under the
    * Rhythmyx root directory. May be <code>null</code>.
    */
   private static Properties m_versionProps = null;

}

