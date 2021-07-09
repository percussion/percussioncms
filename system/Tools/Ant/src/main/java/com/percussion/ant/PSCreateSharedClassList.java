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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Creates the xml list of shared classes that will be put into
 * the rxclient jar file.
 */
public class PSCreateSharedClassList extends Task
{

   
   /**
    * Sets the source directory
    * @param dir the source directory, cannot be <code>null</code>
    * , must be a directory and must exist.
    */
   public void setDir(File dir)
   {
      m_dir = dir;
   }
   
   /**
    * Sets the path of the xml file to be written.
    * @param file should not be <code>null</code>.
    */
   public void setXmlfile(File file)
   {
      m_xmlfile = file;
   }
   
   /**
    * Sets the name of the patternset
    * @param name the pattern set name defaults to 
    *    "SHARED_SERVER_FILES_PATTERN"
    */
   public void setPatternsetname(String name)
   {
      m_patternSetName = name;
   }
   
   /**
    * Sets the type of patternset
    * @param type the type of patternset "include" or "exclude"
    * defaults to "include"
    */
   public void setType(String type)
   {
      if(type != null)
         m_type = type.toLowerCase();
   }    
   
   /**
    * Does the actual work
    */
   public void execute() throws BuildException
   {
      if(m_dir == null || !m_dir.exists())
         throw new BuildException("dir must be specified and exist.");
      if(!m_dir.isDirectory())
         throw new BuildException("dir must be a valid directory.");
      if(m_type == null || 
         !(m_type.equals("include") || m_type.equals("exclude")))
         throw new BuildException("type must be \"include\" or \"exclude\".");
      log("Creating shared classes xml file from the package.nmk files.");      
         
      try
      {
         m_paths.clear(); // Make sure list is empty
         getAllPackageNmkPaths(m_dir);
         addComment(TOP_COMMENT);
         addFileSetStart();            
         Iterator it = m_paths.iterator();
         while(it.hasNext())
         {
             File current = (File)it.next();
             PSMakefileInterpreter interp = new PSMakefileInterpreter(current);
             interp.interpret(); 
             createEntries(interp.getMacros());
              
         }
         addFileSetEnd();
         writeFile();
             
      }
      catch (IOException e)
      {
         throw new BuildException(e.getMessage());
      }  
      
   }
   
   /**
    * Extracts the package name and shared files and adds the appropriate
    * xml entries.
    * @param macros the makefile interpreter macros, should not be
    * <code>null</code>.
    */
   private void createEntries(Map macros)
   {
      String thePackage = 
        ((String)macros.get(PACKAGE_MACRO)).trim().replace('.', '/');
      String shared = (String)macros.get(SHARED_CLASSES_MACRO);
      StringTokenizer st = new StringTokenizer(shared, " \t\r\n");
      if(st.hasMoreTokens())
      {
         addBreak();
         addComment(thePackage.toUpperCase());         
      }   
      while(st.hasMoreTokens())
      {
         addPattern(thePackage + "/" + st.nextToken());
      }
      
   }
   
   /**
    * Writes the buffer to the disk using the path in m_xmlfile.
    */
   private void writeFile() throws IOException
   {
      FileWriter writer = null;
      try
      {
         writer = new FileWriter(m_xmlfile);
         writer.write(m_buffer.toString());
      }
      finally 
      {
         if(writer != null)
            writer.close();
      }
      
   }
   
   /**
    * Prepares the list of all package.nmk files starting from
    * the specified directory. The paths are placed in m_paths.
    * @param dir the directory to search in, cannot be <code>null</code> and
    * must be a directory    
    */
   private void getAllPackageNmkPaths(File dir)
   {
      if(dir == null || !dir.isDirectory())
         throw new IllegalArgumentException(
            "dir cannot be null and must be a directory.");
      File[] children = dir.listFiles();
      for(int i = 0; i < children.length; i++)
      {
         if(children[i].isDirectory())
         {
            getAllPackageNmkPaths(children[i]);
         }
         else
         {
            if(children[i].getName().equalsIgnoreCase(PACKAGE_NMK))
            {
               m_paths.add(children[i]);              
            }
         }
      }
   }
   
      
   /**
    * Adds the starting fileset element to the buffer.
    */
   private void addFileSetStart()
   {
      m_buffer.append("<project>\n<patternset id=\"");
      m_buffer.append(m_patternSetName);
      m_buffer.append("\">\n");
   }
   
   /**
    * Adds the ending fileset entry to the buffer.
    */
   private void addFileSetEnd()
   {
      m_buffer.append("</patternset>\n</project>");      
   }
   
   /**
    * Adds an pattern entry to the xml buffer
    * @param path may be <code>null</code>.
    */
   private void addPattern(String path)
   {
      if(path == null || path.trim().length() == 0)
         return;
      m_buffer.append("<");
      m_buffer.append(m_type);
      m_buffer.append(" name=\"");
      m_buffer.append(path);
      m_buffer.append(".class\"/>\n");
      // Add pattern for inner classes
      m_buffer.append("<");
      m_buffer.append(m_type);
      m_buffer.append(" name=\"");
      m_buffer.append(path);
      m_buffer.append("$*.class\"/>\n");
   }
   
   /** 
    * Adds a comment to the buffer.
    * @param comment may be <code>null</code>
    */
   private void addComment(String comment)
   {
      if(comment == null || comment.trim().length() == 0)
         return;
      m_buffer.append("<!-- ");
      m_buffer.append(comment);
      m_buffer.append(" -->\n");
   }
   
   /**
    * Adds a line break to the buffer
    */
   private void addBreak()
   {
      m_buffer.append("\n");
   }
   
   /**
    * List of package.nmk paths, never <code>null</code>, may be empty.
    */
   private List<File> m_paths = new ArrayList<File>();
   
   /**
    * The source directory from which to start the package.nmk search.
    */
   private File m_dir;
     
   /**
    * The file that will hold the xml 
    */
   private File m_xmlfile;
   
   /**
    * The patternset name - defaults to "SHARED_SERVER_FILES_PATTERN"
    */
   private String m_patternSetName = "SHARED_SERVER_FILES_PATTERN";
   
   /** 
    * The type of patternset, either "include" or "exclude".
    * Defaults to "include" 
    */
   private String m_type = "include";
   
   /**
    * The string buffer used to build up the xml file.
    */
   private StringBuffer m_buffer = new StringBuffer();
   
   public final static String PACKAGE_NMK = "package.nmk";
   public final static String PACKAGE_MACRO = "PACKAGE";
   public final static String SHARED_CLASSES_MACRO = "SHARED_CLASSES";
   public final static String TOP_COMMENT = 
      "Shared file includes for rxclient.jar ~~ auto generated by the build";

}
