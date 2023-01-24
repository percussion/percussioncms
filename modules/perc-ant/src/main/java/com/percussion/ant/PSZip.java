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
import org.apache.tools.ant.taskdefs.Zip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
