/******************************************************************************
 *
 * [ RxCheckTempSpace.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAFileUtils;
import com.percussion.installer.RxVariables;


/**
 * This action checks for the minimum required disk space for installing
 * Rhythmyx.
 */
public class RxCheckTempSpace extends RxIAAction
{
   @Override
   public void execute()
   {
      try
      {
         String mbRequired = getInstallValue(InstallUtil.getVariableName(
               getClass().getName(),
               MB_REQUIRED_VAR));
         setInstallValue(RxVariables.RX_REQUIRED_TEMP_SPACE, mbRequired);
         Integer mbRequiredInt = new Integer(mbRequired);
         setMbRequired(mbRequiredInt.longValue());
         
         if (getFileService() != null)
         {
            String parts[] = getFileService().getPartitionNames();
            if (parts != null && parts.length > 0)
            {
               String strTempDir = RxIAFileUtils.getTempDir();
               String strTempPart = RxIAFileUtils.getPartitionName(
                     getFileService(),
                     strTempDir,
                     parts);
               
               if (strTempPart != null)
               {
                  long lReq = m_lMbRequired*1048576;
                  long lFree = getFileService().getPartitionFreeSpace(
                        strTempPart,
                        lReq);
                  
                  if (lFree < lReq)
                  {
                     setInstallValue(RxVariables.RX_TEMP_SPACE_OK, "false");
                  }
                  else
                     setInstallValue(RxVariables.RX_TEMP_SPACE_OK, "true");
                  
               }
            }
         }
      }
      catch (Exception e)
      {
         RxLogger.logError("RxCheckTempSpace#execute : " + e.getMessage());
         e.printStackTrace();
      }
   }
   
   /**********************************************************************
    * Bean properties
    *********************************************************************/
   
   /**
    * The accessor for the required amount of temp space.
    *
    * @return The amount of free mb required in the temp directory
    */
   public long getMbRequired()
   {
      return (m_lMbRequired);
   }
   
   /**
    * The mutator for the required amount of temp space.
    *
    * @param lKbRequired - The amount of free mb required in the temp directory
    */
   public void setMbRequired(long lKbRequired)
   {
      m_lMbRequired = lKbRequired;
   }
   
   /**
    * The amount of mb required to be available in the temp directory.
    */
   private long m_lMbRequired = 0;
   
   /**
    * The variable name for the required mb parameter passed in via the IDE.
    */
   private static final String MB_REQUIRED_VAR = "mbrequired";
}
