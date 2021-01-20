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
package com.percussion.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Determines the path to the release documents directory from the specified
 * major, minor, micro version and root directory passed in. Sets a property 
 * if documentation exists for the specified version else the property does
 * not get set.
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="PSGetReleaseDocumentsPath"
 *             class="com.percussion.ant.PSGetReleaseDocumentsPath"
 *  </code>
 *
 * Now use the task to determine the correct release documents directory
 * to use, the path will be put into the ${RELEASE_DOCS} property.
 *
 *  <code>
 *  &lt;PSGetReleaseDocumentsPath property="RELEASE_DOCS"
 *                         rootdir="\\zeus\public\releasedocs"
 *                         major="5"
 *                         minor="0"
 *                         micro="1"/&gt;
 * </code>
 *
 *    The 'property' attribute is optional and defaults to "RELEASE.DOCS.PATH"
 *    if not supplied.
 *
 */
public class PSGetReleaseDocumentsPath extends Task
{

   // Mutators for various task attributes

   public void setRootdir(String dir)
   {
      m_rootDir = dir;
   }

   public void setMajor(String major)
   {
      m_major = major;
   }

   public void setMinor(String minor)
   {
      m_minor = minor;
   }

   public void setMicro(String micro)
   {
      m_micro = micro;
   }

   public void setProperty(String property)
   {
      m_property = property;
   }

   /* (non-Javadoc)
    * @see org.apache.tools.ant.Task#execute()
    */
   public void execute() throws BuildException
   {
      if(m_rootDir == null || m_rootDir.length() == 0)
         throw new BuildException("'rootdir' is required and cannot be empty.");
      if(m_major == null || m_major.length() == 0)
         throw new BuildException("'major' is required and cannot be empty. ");
      if(m_minor == null || m_minor.length() == 0)
         throw new BuildException("'minor' is required and cannot be empty. ");
      if(m_micro == null || m_micro.length() == 0)
         throw new BuildException("'micro' is required and cannot be empty. ");
      if(!isNumeric(m_major) || m_major.length() != 1)
         throw new BuildException(
            "'major' must be a number with a length of one digit.");
      if(!isNumeric(m_minor) || m_minor.length() != 1)
         throw new BuildException(
            "'minor' must be a number with a length of one digit.");
      if(!isNumeric(m_micro) || m_micro.length() != 1)
         throw new BuildException(
            "'micro' must be a number with a length of one digit.");
      File rootDir = new File(m_rootDir);
      if(!rootDir.exists() || !rootDir.isDirectory())
         throw new BuildException(
            "'rootDir' directory does not exist or there is " +
            "a network problem.");

      String version = m_major + "." + m_minor;
      if(Integer.parseInt(m_micro) > 0)
         version += "." + m_micro;
      List versionDirs = getAllVersionDirectories(rootDir);

      if(!versionDirs.contains(version)) 
         return;
         
      String absPath = rootDir.getAbsolutePath();
      String sep = absPath.indexOf("/") == -1 ? "\\" : "/";

      getProject().setProperty(m_property, absPath + sep + version);
   }

   /**
    * Returns a list of all numeric version directories under root
    * @param rootdir the root directory where all of the version directories are located
    * , cannot be <code>null</code>.
    * @return a list of all version directories, never <code>null</code>, and should not
    * be empty.
    */
   private List getAllVersionDirectories(File rootdir)
   {
      if(rootdir == null)
         throw new IllegalArgumentException("rootdir cannot be null.");
      List<String> dirs = new ArrayList<String>();
      File[] children = rootdir.listFiles();

      for(int i = 0; i < children.length; i++)
      {
         if(children[i].isDirectory())   
         {
            dirs.add(children[i].getName());
         }
      }

      return dirs;
   }

   /**
    * Determines if the string passed in is a numeric
    * @param s the string to evaluate, cannot be <code>null</code>.
    * @return <code>true</code> if the string is a numeric
    */
   private boolean isNumeric(String s)
   {
      if(s == null)
         throw new IllegalArgumentException("String cannot be null.");
      boolean isNumber = true;
      try
      {
         Float.parseFloat(s);
      }
      catch(NumberFormatException e)
      {
         isNumber = false;
      }
      return isNumber;
   }

   private String m_rootDir;
   private String m_major;
   private String m_minor;
   private String m_micro;
   private String m_property = "RELEASE.DOCS.PATH";




}
