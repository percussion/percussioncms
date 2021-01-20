/******************************************************************************
 *
 * [ RxPubDocsModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;

import java.util.Calendar;


/**
 * This model represents a panel/console that asks if the user would like to
 * trim any rows from the RXPUBDOCS table by specifying a date prior to which
 * all publishing history will be removed.
 */
public class RxPubDocsModel extends RxIAModel
{
   /**
    * Constructs an {@link RxPubDocsModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxPubDocsModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   protected void initModel()
   {
      super.initModel();
   }
   
   /**
    * Panel validator to validate supplied date information. If any errors
    * exist, this will disallow moving to next panel/console in the installer
    * wizard.
    * 
    * @return <code>true</code> if the validation passed, <code>false</code>
    * otherwise.
    */
   private boolean validateData()
   {
      boolean isOK = true;
      int year;
      int month;
      int day;
      
      String err = RxInstallerProperties.getResources().getString(
      "dateValueErr");
      String title = RxInstallerProperties.getResources().getString(
      "dateValueErrorTitle");
      try
      {
         if (getDate().trim().length() == 0)
         {
            // A date was not specified, so we will not trim
            setTrimTable(false);
            return true;
         }
         
         String[] splitDate = getDate().split("/");
         if (splitDate.length != 3)
            isOK = false;
         else
         {
            String strMonth = splitDate[0];
            String strDay = splitDate[1];
            String strYear = splitDate[2];
            
            if (strYear.trim().length() < 4 ||
                  strMonth.trim().length() < 2 ||
                  strDay.trim().length() < 2)
               isOK = false;
            else
            {
               year = Integer.parseInt(strYear);
               month = Integer.parseInt(strMonth);
               day = Integer.parseInt(strDay);
               
               if (year <= 0 || year > Calendar.getInstance().get(
                     Calendar.YEAR))
                  isOK = false;
               else if (month <= 0 || month > 12)
                  isOK = false;
               else if (day <= 0 || day > 31)
                  isOK = false;
               else
                  setTrimTable(true);
            }
         }
      }
      catch ( NumberFormatException nex)
      {
         isOK = false;
      }
      
      if (!isOK)
         displayErrorMessage(title, err);
      
      return isOK;
   }
   
   @Override
   public boolean queryExit()
   {
      super.queryExit();
      
      return validateData(); 
   }
   
   @Override
   public String getTitle()
   {
      // Return the empty string here to override the generic title of the 
      // parent class.  A specific title is not required for this model's
      // associated panel/console.
      return "";
   }
   
   /**
    * Helper method to display Error message, used by validateData
    *
    * @param title for the error dialog never <code>null</code>
    * @param errorMsg the message to be displayed.
    */
   private void displayErrorMessage(String title, String errorMsg)
   {
      getUserInput(title, errorMsg, null);
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * Returns trim table flag.
    * @return <code>true</code> if the table should be trimmed,
    * <code>false</code> otherwise.
    */
   public static boolean getTrimTable()
   {
      return ms_bTrimTable;
   }
   
   /**
    * Setter for trim table flag.
    * 
    * @param trimTable if <code>true</code>, the RXPUBDOCS table will be
    * trimmed to the date specified by {@link #ms_strDate}.
    */
   public void setTrimTable(boolean trimTable)
   {
      ms_bTrimTable = trimTable;
      propertyChanged("TrimTable");
   }
   
   /**
    * Returns keep date.
    * @return year, never <code>null</code>, may be empty
    */
   public static String getDate()
   {
      return ms_strDate;
   }
   
   /**
    * Setter for keep date.
    * 
    * @param date all data prior to this date will be deleted from RXPUBDOCS.
    */
   public void setDate(String date)
   {
      ms_strDate = date;
      propertyChanged("Date");
   }   
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * Indicates if the RXPUBDOCS table should be trimmed
    */
   private static boolean ms_bTrimTable = false;
   
   /**
    * Date string, never <code>null</code, may be empty
    */
   private static String ms_strDate = "";
}
