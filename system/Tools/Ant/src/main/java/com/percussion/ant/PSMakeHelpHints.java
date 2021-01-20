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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task to create the workbench's help hints xml file and moves the
 * required resources to a specified folder. This task requires that the 
 * Jericho Html parser jar be on the classpath. *
 */
public class PSMakeHelpHints extends Task
{
   
   /**
    * The location of the help mappings file (Required)
    * @param helpMappings
    */
   public void setHelpmappings(File helpMappings)
   {
      m_helpMappings = helpMappings;
   }
   
   /**
    * The help plugin directory where all the help files can be found
    * (Required)
    * @param helpPath
    */
   public void setHelppath(File helpPath)
   {
      m_helpPath = helpPath;
   }
   
   /**
    * The target path for the file to be created (Required)
    * @param target
    */
   public void setTarget(File target)
   {
      m_target = target;
   }
     
   /* 
    * @see org.apache.tools.ant.Task#execute()
    */
   @Override
   public void execute() throws BuildException
   {
      if(m_helpMappings == null)
         throw new BuildException("helpmappings is required.");
      if(m_helpPath == null)
         throw new BuildException("helppath is required.");
      if(m_target == null)
         throw new BuildException("target is required.");     
      
      try
      {
         PSHelpHintFileCreator creator = new PSHelpHintFileCreator(
            m_helpMappings, m_helpPath, m_target);
         creator.createFile();
         
      }
      catch (Exception e)
      {
         throw new BuildException(e);         
      }
   }
   
  
   
   
   private File m_helpMappings;
   private File m_helpPath;
   private File m_target;
   

}
