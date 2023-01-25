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
package com.percussion.ant.install;

import org.apache.tools.ant.taskdefs.Copy;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * This task extends Ant's copy task allowing for a refresh of files depending
 * on various project properties.  The type of copy operation is controlled by
 * a <code>replaceType</code> attribute, which allows three values: always,
 * date, and never.  If this attribute is not specified, the replace type will
 * default to always replace.  If a refresh of all files is required,
 * determined by {@link PSAction#refreshFiles()}, then the default Ant copy task
 * behavior (replace by date) will be used. 
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="PSCopy"
 *              class="com.percussion.ant.install.PSCopy"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to copy one or more files.
 *
 *  <code>
 *  &lt;PSCopy tofile="C:/Rhythmyx/file.bak" file="C:/Rhythmyx/file"/&gt;
 *  </code>
 *
 * </pre>
 *
 * @author peterfrontiero
 */
public class PSCopy extends Copy
{
   // see base class
   @Override
   public void execute()
   {
      if (!PSAction.refreshFiles())
      {
         if (getReplaceType().equalsIgnoreCase(ALWAYS))
            setOverwrite(true);
      }
      else
         setOverwrite(false);
           
      super.execute();
   }

   /**
    * See base class method {@link Copy#doFileOperations()} for details.
    */
   @Override
   protected void doFileOperations()
   {
      if (!PSAction.refreshFiles() && getReplaceType().equalsIgnoreCase(NEVER))
         modifyFileMap();
      super.doFileOperations();
   }
   
   /**
    * This method modifies the map of files to be copied so that existing files
    * will not be overwritten.  Used in never replace copy operations.
    */
   private void modifyFileMap()
   {
      if (fileCopyMap.size() > 0)
      {
         Enumeration e = fileCopyMap.keys();
         while (e.hasMoreElements())
         {
            String fromFile = (String) e.nextElement();
            String[] toFiles = (String[]) fileCopyMap.get(fromFile);
            ArrayList<String> toFilesList = new ArrayList<String>();       
            
            for (int i = 0; i < toFiles.length; i++)
            {
               String toFile = toFiles[i];
               if (!(new File(toFile)).exists())
                  toFilesList.add(toFile);
            }
            
            toFiles = new String[toFilesList.size()];
            for (int j = 0; j < toFilesList.size(); j++)
               toFiles[j] = toFilesList.get(j);
            fileCopyMap.put(fromFile, toFiles);
         }
      }
   }
   
   /**
    * Returns the type of replace option for this copy operation.
    * 
    * @return see {@link #m_replaceType}.
    */
   public String getReplaceType()
   {
      return m_replaceType;
   }
   
   /**
    * Sets the type of replace option for this copy operation.
    * 
    * @return see {@link #m_replaceType}.
    */
   public void setReplaceType(String replaceType)
   {
      if (!replaceType.equalsIgnoreCase(ALWAYS) &&
            !replaceType.equalsIgnoreCase(DATE) &&
            !replaceType.equalsIgnoreCase(NEVER))
            
      {
         throw new IllegalArgumentException("replaceType must be one of the " +
               " following : always, date, never");
      }
      
      m_replaceType = replaceType;
   }
   
   /**
    * The type of replace option.  Valid values are:
    * <br><br>
    * {@link #ALWAYS}
    * <br>
    * {@link #NEVER}
    * <br>
    * {@link #DATE}
    * <br><br>
    * Defaults to always replace.
    */
   private String m_replaceType = ALWAYS;
   
   /**
    * Constant for the always file replace option.  Indicates that existing
    * files will be overwritten.
    */
   private static String ALWAYS = "always";
   
   /**
    * Constant for the replace by date option.  Indicates that existing
    * files will only be overwritten if source files are newer.
    */
   private static String DATE = "date";
   
   /**
    * Constant for the never file replace option.  Indicates that existing
    * files will not be overwritten.
    */
   private static String NEVER = "never";
}
