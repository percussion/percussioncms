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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Zip;

/**
 * Extends the zip task to accept an additional attribute which specifies
 * the location of a comment file.  The contents of this file will be added as a
 * comment to the newly created zip file.
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="PSZip"
 *              class="com.percussion.ant.PSZip"
 *              classpath="c:\lib"/&gt;
 *  </code>
 * 
 *  <code>  
 *  &lt;PSZip destfile="c:/test.zip" basedir="c:/test"
 *            commentFile="c:/README"/&gt;
 *  </code>
 *
 * </pre>
 */
public class PSZip extends Zip
{
   /**
    * Sets the file to be used as a comment.
    * 
    * @param commentFile the path to the comment file.  May be <code>null</code>
    * or empty.
    */
   public void setCommentFile(File commentFile)
   {
      m_commentFile = commentFile;
   }
   
   /**
    * Reads the contents of {@link #m_commentFile}, sets as zip file comment,
    * then calls {@link Zip#execute()} to build the zip file.
    */
   public void execute() throws BuildException
   {
      BufferedReader b = null;
           
      try
      {
         if (m_commentFile != null && m_commentFile.exists())
         {
            String fileContents = "";
            String input;
            b = new BufferedReader(new FileReader(m_commentFile));
            
            while ((input = b.readLine()) != null)
            {
               fileContents += input + "\n";
            }
            
            if (fileContents.trim().length() > 0)
            {
               setComment(fileContents);
            }
         }
         
         super.execute();
      }
      catch(Exception e)
      {
         throw new BuildException("Error: " + e.getMessage());
      }
      finally
      {
         if (b != null)
         {
            try
            {
               b.close();
            }
            catch (IOException e)
            {
               
            }
         }
      }
   }
     
   /**
    * The contents of this file will be added as a zip file comment.
    */
   private File m_commentFile;
}
