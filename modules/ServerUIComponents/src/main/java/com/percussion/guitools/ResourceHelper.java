/*[ ResourceHelper.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.guitools;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * This is a class that aids in retrieving different kinds of resources from a
 * resource bundle. It has static methods to get mnemonics, accelerator keys, 
 * icons, etc.
 * <p>
 * To use this class, resources must use the following key naming convention:
 * <ul>
 * <li> mn_<base_resource_key> for mnemonics </li>
 * <li> ks_<base_resource_key> for accel keys </li>
 * <li> gif_<base_resource_key> for icon file names </li>
 * <li> tt_<base_resource_key> for tooltip text </li>
 * <li> pt_<base_resource_key> for Points (can be used for cursor hotspot) </li>
 * </ul>
 * This allows all resources associated with the same UI object to be accessed
 * with the 'same' key from the caller's point of view.
 */
public class ResourceHelper
{
   /**
    * Returns the character that is the mnemonic for the for the supplied 
    * action, or 0 if the action does not have a mnemonic.
    */
   public static char getMnemonic(PSResources rb, String strBaseKeyName)   
   {
      try
      {
         return (rb.getCharacter("mn_" + strBaseKeyName));
      } 
      catch (MissingResourceException e)
      {
         return(0);
      } 
   }

   /**
    * Checks the supplied resource bundle for an accelerator key by the
    * supplied name. If one is found it is returned, otherwise null is 
    * returned.
    */
   public static KeyStroke getAccelKey(PSResources rb, String strBaseKeyName)
   {
      try
      {
         return (rb.getKeyStroke("ks_" + strBaseKeyName));
      } 
      catch (MissingResourceException e)
      {
         return(null);
      }
   }

   /**
    * Checks the supplied resource bundle for a tool tip string by the
    * supplied name. If a non-empty one is found it is returned, otherwise null is 
    * returned.
    */
   public static String getToolTipText(PSResources rb, String strBaseKeyName)
   {
      try
      {
         String strTip = rb.getString("tt_" + strBaseKeyName);
         return (strTip.length() > 0 ? strTip : null);
      } 
      catch (MissingResourceException e)
      {
         return(null);
      } 
   }

   /**
    * Checks the supplied resource bundle for an icon filename whose key is
    * gif_<strBaseKeyName>. If a non-empty one is found, the icon is loaded and  
    * it is returned, otherwise <code>null</code> is returned. Uses the <code>
    * Class</code> instance of <code>rb</code> to load the image file.
    *
    * @para rb the resource bundle to search for the filename, using strBaseKeyName
    * as the key
    *
    * @param strBaseKeyName must be a valid string
    *
    * @throws MissingResourceException If the icon filename is present in the
    * resource bundle, but the file cannot be found or loaded.
    */
   public static ImageIcon getIcon(PSResources rb, String strBaseKeyName)
   {
      String strFilename = null;
      try
      {
         strFilename = rb.getString("gif_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
         return(null);
      } 

      if (strFilename.length() > 0)
      {
         return BitmapManager.getBitmapManager(
            rb.getClass()).getImage(strFilename);
      }
      return(null);
   }

   /**
    * @returns the point found in the supplied resource bundle under the supplied
    * key name. If one is not found, a point of 0,0 is returned. If debugging
    * is enabled and the resource isn't found, an assertion is issued.
    */
   public static Point getPoint(PSResources rb, String strBaseKeyName)
   {
      Point pt = null;
      try
      {
         pt = (Point) rb.getObject("pt_" + strBaseKeyName);
      } 
      catch (MissingResourceException e)
      {
         final String [] astrParams = 
         {
            strBaseKeyName
         };
         pt = new Point();
      }
      catch (ClassCastException e)
      {
         final String [] astrParams = 
         {
            strBaseKeyName
         };
         pt = new Point();
      }
      return (pt);
   }
   
   /**
    * Gets the resource bundle used by all the classes in this package.
    * 
    * @return the resource bundle, never <code>null</code> 
    * 
    * @throws MissingResourceException if the resources properties is not found.
    */
   public static ResourceBundle getResources()
   {
      if(sm_res == null)
      {
         sm_res = ResourceBundle.getBundle(
               "com.percussion.guitools.GuitoolsResources",
               Locale.getDefault() );
      }
      
      return sm_res;
   }   
   static ResourceBundle sm_res = null;
}



