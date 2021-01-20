/*[ RxLoadSharedObjects.java ]***********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.installer.action;

import com.percussion.installanywhere.RxIAAction;

import java.io.File;


/**
 * Shared objects must be included in the installer's $DO_NOT_INSTALL$ folder.
 * These objects are then loaded during installation. This class must be
 * extended to load objects for each platform.  If it has windows dlls then
 * a windows platform conditional must be added through the IDE.  Similar
 * conditions must be added for solaris and linux shared objects.
 */
public abstract class RxLoadSharedObjects extends RxIAAction
{
   @Override
   public void execute()
   {
      try
      {
         for (int i = 0; i < m_sharedObjects.length; i++)
         {
            String sharedObject = m_sharedObjects[i];
            File sharedObjectFile = getResourceFile(sharedObject);
            if (sharedObjectFile != null)
            {
               String sofName = sharedObjectFile.getName();
               RxLogger.logInfo("Loading library : " + sofName);
               System.load(sharedObjectFile.getAbsolutePath());
               RxLogger.logInfo("Library loaded : " + sofName);
            }
            else
            {
               RxLogger.logError("Could not locate shared object : " +
                     sharedObject);
            }
         }
      }
      catch (Exception ex)
      {
         RxLogger.logInfo("ERROR : " + ex.getMessage());
         RxLogger.logInfo(ex);
      }
   }
   
   /***************************************************************************
    * Bean properties
    ***************************************************************************/
   
   /**
    * Returns the shared objects to bundle during build and load during install.
    *
    * @return the shared objects to bundle during build and load during install,
    * never <code>null</code> or empty
    */
   public String[] getSharedObjects()
   {
      return m_sharedObjects;
   }
   
   /**
    * Sets the shared objects to bundle during build and load during install.
    *
    * @param sharedObjects shared objects to bundle during build and load during
    * install, never <code>null</code> or empty
    *
    * @throws IllegalArgumentException if sharedObjects is <code>null</code>
    * or empty
    */
   public void setSharedObjects(String[] sharedObjects)
   {
      if ((sharedObjects == null) || (sharedObjects.length == 0))
         throw new IllegalArgumentException(
         "sharedObjects may not be null or empty");
      m_sharedObjects = sharedObjects;
   }
   
   /**************************************************************************
    * properties
    **************************************************************************/
   
   /**
    * The shared objects to bundle during build and load during install,
    * never <code>null</code> after {@link #setSharedObjects(String[])} is
    * called.  Each subclass must initalize this set of objects.  Each value
    * must match the path as it appears for its corresponding entry under the
    * installer's $DO_NOT_INSTALL$ folder in the IDE. 
    */
   private String[] m_sharedObjects;
}


