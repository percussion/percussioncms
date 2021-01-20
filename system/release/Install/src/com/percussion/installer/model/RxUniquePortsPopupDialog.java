/******************************************************************************
 *
 * [ RxUniquePortsPopupDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.model;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAModel;


/**
 * Util class to help figure if a warning screen be shown or not..
 */
public class RxUniquePortsPopupDialog
{
   /**
    * Constructs a new unique ports popup dialog object.
    */
   public RxUniquePortsPopupDialog()
   {
      super();
   }
   
   /**
    * Initializes a new dialog object and informs it to show the warning.
    * 
    * @param model the {@link RxIAModel} instance required to access
    * ui functionality, never <code>null</code>.
    */
   public void doUniquePortsMsg(RxIAModel model)
   {
      if (model == null)
         throw new IllegalArgumentException("model may not be null");
      
      RxUniquePortsPopupDialog rxPortsMsg = new RxUniquePortsPopupDialog();
      rxPortsMsg.manageUniquePortsMessage(model);
   }
   
   /**
    * Displays a dialog informing the user about unique ports and asking if the
    * dialog should be displayed again.
    * 
    * @param model required for ui access, never <code>null</code>.
    */
   public void manageUniquePortsMessage(RxIAModel model) 
   {
      if (model == null)
         throw new IllegalArgumentException("model may not be null");
      
      //show the warning
      
      /* get all the popup's strings */
      String noRemind = "No";
      String yesRemind = "Yes";
      String msg = RxInstallerProperties.getResources().getString(
      "uniquePortsMsg");
      String title = RxInstallerProperties.getResources().getString(
      "uniquePortsTitle");
      /* get temp flag file info */
      if (ms_showAgain)
      {
         int response =
            model.getUserInput(
                  title, 
                  msg,
                  null,
                  yesRemind,
                  noRemind
            );
         
         ms_showAgain = response == RxIAModel.BUTTON1_RESPONSE;
      }
   }
   
   /**
    * Flag indicating whether or not to show the port warning dialog in
    * {@link #manageUniquePortsMessage(RxIAModel)}.
    * <code>true</code> to show it, <code>false</code> to hide it.
    */
   private static boolean ms_showAgain = true;
}
