/******************************************************************************
 *
 * [ RxCompScanPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxCompatibilityScan;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.model.RxCompScanModel;


/**
 * AWT Implementation of the RxCompScanModel.
 */
public class RxCompScanPanel extends RxTextDisplayPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxCompScanModel csm = new RxCompScanModel(this);
      setModel(csm);
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      //Get results of scan
      String results = RxCompatibilityScan.getResults();
      
      if (results.trim().length() > 0)
      {
         //There were errors/warnings, so display them
         setText(results);
         
         //Check for errors
         if (RxCompatibilityScan.getExceptionsStatus())
         {
            //Disable Back/Next buttons as upgrade cannot proceed
            setNxtButtonEnabled(true);
            setPreviousButtonEnabled(true);
            
            //Restore original Version.properties
            /*
            try
            {
               InstallUtil.restoreVersionPropertyFile(
                     getInstallValue(RxVariables.INSTALL_DIR));
            }
            catch (Exception e)
            {
               RxLogger.logError("RxCompScanPanel : Unable to restore "
                     + "Version.properties file");
            }
            */
         }
      }
      else
      {
         //Compatibility scan was successful, display success message
         String compScanResults = RxInstallerProperties.getString(
               "compScanResults");
         String compScanSuccess = RxInstallerProperties.getString(
               "compScanSuccess");
         setText(compScanResults + "\n\n\n" + compScanSuccess);
         
         //Enable Back/Next buttons
         setNxtButtonEnabled(true);
         setPreviousButtonEnabled(true);
      }
   }

   @Override
   public void exiting()
   {
   }

   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxCompScanModel getDM()
   {
      if ( m_dM == null )
         m_dM = (RxCompScanModel) getModel();
      return m_dM;
   }

   /**
    * See {@link #getDM()}.
    */
   private RxCompScanModel m_dM  = null;
}
