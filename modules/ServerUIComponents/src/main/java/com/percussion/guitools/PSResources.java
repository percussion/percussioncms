/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

