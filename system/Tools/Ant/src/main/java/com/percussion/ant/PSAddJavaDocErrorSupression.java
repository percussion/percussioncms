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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task to get around a bug that when viewed via eclipse a 
 * javascript error is occurring. This error is not affecting any functionality
 * but is annoying. To supress this error we inject a javascript error handler
 * into the index.html file that will supress all javascript errors.
 */
public class PSAddJavaDocErrorSupression extends Task
{
   
   /**
    * The directory where the javadoc is contained
    * @param dir cannot be <code>null</code>.
    */
   public void setDir(File dir)
   {
      m_dir = dir;
   }
   
   /* 
    * @see org.apache.tools.ant.Task#execute()
    */
   @Override
   public void execute()
   {
      if(m_dir == null || !m_dir.isDirectory())
         throw new BuildException("Directory cannot be null and must" +
            " be a valid directory.");
      try
      {
         System.out.println("Adding error suppression to: " + 
            new File(m_dir, INDEX_HTML_FILE).getAbsolutePath());
         StringBuffer contents = getContents();
         int idx = indexOfIgnoreCase(contents, "</TITLE>");
         if(idx != -1)
         {
            contents.insert(idx + 8, ERROR_SUPPRESS_STRING);
            saveFile(contents);
         }
      }
      catch (IOException e)
      {
         throw new BuildException(e);         
      }
   }
   
   /**
    * Helper method to find the index of a string ignoring
    * case.
    * @param sb assumed not <code>null</code>.
    * @param str assumed not <code>null</code>.
    * @return the index position or -1 if not found.
    */
   private int indexOfIgnoreCase(StringBuffer sb, String str)
   {
      String buffer = sb.toString().toLowerCase();
      return buffer.indexOf(str.toLowerCase());
   }
   
   /**
    * Retrieves the contents of the index html file as a
    * <code>StringBuffer</code>.
    * @return never <code>null</code>.
    * @throws IOException on any error.
    */
   private StringBuffer getContents()
      throws IOException
   {
      final StringBuffer sb = new StringBuffer();
      FileInputStream fis = null;
      try
      {
         fis = new FileInputStream(new File(m_dir, INDEX_HTML_FILE));
         int c = 0;
         while((c = fis.read()) != -1)
         {
            sb.append((char)c);            
         }
         return sb;
      }
      finally
      {
         if(fis != null)
         {
           fis.close();
         }
      }
   }
   
   /**
    * Saves the updated content back into the index.html file
    * @param contents assumed not <code>null</code>.
    * @throws IOException upon any error.
    */
   private void saveFile(final StringBuffer contents)
      throws IOException
   {
      FileOutputStream fos = null;
      try
      {
         fos = new FileOutputStream(new File(m_dir, INDEX_HTML_FILE));
         int len = contents.length();
         for(int i = 0; i < len; i++)
         {
            fos.write(contents.charAt(i));
         }
      }
      finally
      {
         if(fos != null)
         {
            fos.flush();
            fos.close();
         }
         
      }
   }
   
   public static void main(String[] args)
   {
      PSAddJavaDocErrorSupression task = new PSAddJavaDocErrorSupression();
      task.setDir(new File("E:\\rxMain\\system\\publicJavaDocs\\docs"));
      task.execute();
   }
   
   /**
    * The directory where the javadoc is contained, set
    * in {@link #setDir(File)}, never <code>null</code> after
    * that.
    */
   private File m_dir;
   
   /**
    * Constant for the name of the index.html file
    */
   private static final String INDEX_HTML_FILE = "index.html";
   
   /**
    * Constant for error suppression string
    */
   private static final String ERROR_SUPPRESS_STRING = 
      "\n<SCRIPT type=\"text/javascript\">\nwindow.onerror = new Function" + "" +
            "(\"return true\");\n</SCRIPT>\n";

}
