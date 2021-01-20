/*[ RxLoadSolarisSharedObjects.java ]***********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.installer.action;


/**
 * This class loads the solaris shared objects which are required at install
 * time. 
 */
public class RxLoadSolarisSharedObjects extends RxLoadSharedObjects
{
   @Override
   public void execute()
   {
      setSharedObjects(new String[]{
            "$RX_DIR$/release/installer/unix/libPSInstaller.so"}
            );
      super.execute();
   }
}


