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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileNameMapper;

/**
 * This is an ant mapper that will truncate a directory mapping
 * by dropping anything that appears before the specified truncation
 * string. Correctly handles differences in Unix and Windows-style paths.
 * <p>
 * <pre>
 *    USAGE:
 * 
 *        &lt;mapper classname="com.percussion.ant.PSTruncateDirectoryMapper"
 *               classpathref="antExt.class.path"
 *               to="com\"/&gt;   
 *
 *    classname = the name of the PSTruncateDirectoryMapper class
 *    classpathref = the reference id to the class path that points
 *                   to the above class
 *    to = The truncation string, anything before this will be removed from
 *         the directory path
 *  
 * </pre>
 * </p>
 * 
 */
public class PSTruncateDirectoryMapper implements FileNameMapper
{
   
   /**
    * Ignored.
    */
   public void setFrom(@SuppressWarnings("unused") String from) {}

   /**
    * Enter string to truncate directory string. Strips off
    * everything preceeding the truncation string in the
    * directory path.
    * 
    * @param to the string to truncate the directory path. Everything
    * before this string will be chopped off. Cannot be <code>null</code>
    */
   public void setTo(String to) {
      if(null == to || to.trim().length() == 0)
         throw new BuildException(
            "to attribute is required for PSTruncateDirectoryMapper.");
      m_truncBefore_Slash = to.replace("\\", "/");            
      m_truncBefore_BackSlash = to.replace("/", "\\");            
   }
   
   /**
    * Truncates the file path by stripping off everything
    * that comes before the truncation string that was specified
    * in the "to" attribute. Return <code>null</code> if the truncation
    * string is not found.
    * 
    * @param sourceFileName the raw source file path string
    * @return the truncated path string
    */
   public String[] mapFileName(String sourceFileName)
   {
      final int start = Math.max(
            sourceFileName.indexOf(m_truncBefore_Slash),
            sourceFileName.indexOf(m_truncBefore_BackSlash)); 
      if (start == -1)
         return null;
      String res = modifyExtCase(sourceFileName.substring(start));
      return new String[] {res};
   }

   /**
    * Modifies extension case
    * @param s the file path string to be modified
    * @return the modified string
    */
   private String modifyExtCase(String s)
   {
      int pos = s.indexOf('.');
      if(pos < 0)
         return s;

      String path = s.substring(0, pos);
      String ext = s.substring(pos).toLowerCase();


      return path + ext;

   }

   /**
    * The truncation string with slashes.
    * Intialized in {@link #setTo(String)}.
    * Never <code>null</code> or empty after that.
    */
   private String m_truncBefore_Slash;

   /**
    * The truncation string with back slashes.
    * Intialized in {@link #setTo(String)}.
    * Never <code>null</code> or empty after that.
    */
   private String m_truncBefore_BackSlash;
}
