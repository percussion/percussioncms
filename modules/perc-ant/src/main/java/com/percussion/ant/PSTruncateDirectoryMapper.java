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
