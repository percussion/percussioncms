/*[ PSResources.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.guitools;

import javax.swing.*;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;

/**
 * This class maintains the resources that need to be i18n. A class was used 
 * instead of a property file because the designer needs other resources besides
 * strings (for example, Characters for mnemomics and KeyCodes for accel keys).
 * It provides some simple wrapper functions around specific object types.
 * <p>
 * It is incumbent upon the caller using the convenience methods in this class
 * to make sure the key they are requesting is for the correct type of object.
 */
public abstract class PSResources extends ListResourceBundle
{
   /**
    * Convenience method for getting KeyStroke resources.
    *
    * @returns the KeyStroke associated with the supplied key
    * 
    * @throws MissingResourceException if the key is not present in this bundle
    * or the object type is not KeyStroke.
    */
   public KeyStroke getKeyStroke(String strKey)
   {
      try
      {
         return((KeyStroke)getObject(strKey));   
      } 
      catch (ClassCastException e)
      {
         throw new MissingResourceException(strKey, "PSResources", strKey);
      }
   }
   
   /**
    * Convenience method for getting character resources.
    *
    * @returns the char associated with the supplied key (taken from a Character 
    * object)
    * 
    * @throws MissingResourceException if the key is not present in this bundle
    * or the object type is not Character.
    */
   public char getCharacter(String strKey)
   {
      try
      {
         return(((Character)getObject(strKey)).charValue());   
      } 
      catch (ClassCastException e)
      {
         throw new MissingResourceException(strKey, "PSResources", strKey);
      }
    }
}

