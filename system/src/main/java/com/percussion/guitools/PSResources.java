/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

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

