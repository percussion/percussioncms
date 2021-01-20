/******************************************************************************
 *
 * [ RxComponent.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.model;

import com.percussion.util.IPSBrandCodeConstants;

import java.io.File;

import org.apache.commons.lang.StringUtils;


/**
 * This class represents a Rhythmyx install component.  This can be a product or
 * product feature.  The name of the component along with a code value, a set of
 * one or more files indicating the component is installed, and the availability
 * and selection status of the component is maintained.
 * 
 * @author peterfrontiero
 */
public class RxComponent
{
   /**
    * Constructs an {@link RxComponent} object.
    * 
    * @param name the name of the component, may not be <code>null</code> or
    * empty.
    * @param code the {@link IPSBrandCodeConstants} code value for the
    * component, -1 if one does not exist.
    * @param files the set of relative file locations indicating that the
    * component is installed, never <code>null</code>, may be empty.
    */
   public RxComponent(String name, int code, String[] files)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (files == null)
         throw new IllegalArgumentException("files may not be null");
      
      m_name = name;
      m_code = code;
      m_files = files;
   }
   
   /**
    * The name used to identify this component.
    * 
    * @return component name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * The code value for this component.
    * 
    * @return code value, -1 if the component does not have one.
    */
   public int getCode()
   {
      return m_code;
   }
   
   /**
    * The files used to determine if the component is installed.
    * 
    * @return set of relative file locations as an array, never
    * <code>null</code>, may be empty.
    */
   public String[] getFiles()
   {
      return m_files;
   }
   
   /**
    * Checks the availability of the component for install.
    * 
    * @return <code>true</code> if the component is available for install,
    * <code>false</code> otherwise.
    */
   public boolean isAvailable()
   {
      return m_available;
   }
   
   /**
    * Sets the availability of the component for install.
    * 
    * @param available <code>true</code> if the component is available for
    * install, <code>false</code> otherwise.
    */
   public void setAvailable(boolean available)
   {
      m_available = available;
   }
   
   /**
    * Checks the selection status of the component.
    * 
    * @return <code>true</code> if the component has been selected for install,
    * <code>false</code> otherwise.
    */
   public boolean isSelected()
   {
      return m_selected;
   }
   
   /**
    * Sets the selection status of the component.
    * 
    * @param selected <code>true</code> if the component has been selected for
    * install, <code>false</code> otherwise.
    */
   public void setSelected(boolean selected)
   {
      m_selected = selected;
   }
   
   /**
    * Determines if the component is currently installed.  A component is
    * determined to be installed if its set of file indicators is empty or any
    * of the file indicators exists.
    *  
    * @param rootDir the Rhythmyx root directory, may not be <code>null</code>
    * or empty.
    * 
    * @return <code>true</code> if the component is installed,
    * <code>false</code> otherwise.
    */
   public boolean isInstalled(String rootDir)
   {
      if (StringUtils.isBlank(rootDir))
         throw new IllegalArgumentException("rootDir may not be null or empty");
      
      boolean installed = false;
      
      if (getFiles().length == 0)
         installed = true;
      {
         for (String file : getFiles())
         {
            if ((new File(rootDir, file)).exists())
            {
               installed = true;
               break;
            }
         }
      }
      
      return installed;
   }
   
   /**
    * See {@link #getName()}.
    */
   private String m_name;
   
   /**
    * See {@link #getCode()}.
    */
   private int m_code;
   
   /**
    * See {@link #getFiles()}.
    */
   private String[] m_files;
   
   /**
    * See {@link #isAvailable()}, {@link #setAvailable(boolean)}.
    */
   private boolean m_available = true;
   
   /**
    * See {@link #isSelected()}, {@link #setSelected(boolean)}.
    */
   private boolean m_selected = true;
}
