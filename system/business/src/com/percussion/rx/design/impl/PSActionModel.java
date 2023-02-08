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
package com.percussion.rx.design.impl;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.error.PSException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

public class PSActionModel extends PSDesignModel
{
   
   @Override
   public Object load(IPSGuid guid)
   {
      return loadActionByGuid(guid, true);
   }

   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      return loadActionByGuid(guid, false);
   }

   @Override
   public Object load(String name)
   {
      return loadActionByName(name, true);
   }

   @Override
   public Object loadModifiable(String name)
   {
      return loadActionByName(name, false);
   }

   /**
    * Loads the readonly or modifiable action for the supplied name based on the
    * readonly flag.
    * 
    * @param name Must not be blank.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * action.
    * @return Object action object never <code>null</code>, throws
    * {@link RuntimeException} in case of an error.
    */
   private Object loadActionByName(String name, boolean readonly)
   {
      // As we do not have the readonly and modifiable versions of Action
      // objects we simply return modifiable object for now for both readonly
      // and modifiable.
      
      if(StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name must not be null");
      }
      PSAction action = null;
      try
      {
         PSComponentProcessorProxy proxy = 
            PSDesignModelUtils.getComponentProxy();
         Element[] elements = proxy.load(PSAction.getComponentType(
               PSAction.class), null);
         for (Element element : elements)
         {
            String elemName = element.getAttribute("name");
            if(name.equals(elemName))
            {
               action = new PSAction(element);
               break;
            }
         }
      }
      catch (PSException e)
      {  //FB: RV_EXCEPTION_NOT_THROWN NC 1-17-16
         throw new RuntimeException(e);
      }
      if (action == null)
      {
         String msg = "Failed to get the design object for name ({0}) of " +
               "type ({1})";
         Object[] args = { name,getType().name() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return action;
   }
   
   /**
    * Loads the readonly or modifiable action for the supplied guid based on the
    * readonly flag.
    * 
    * @param guid Must not be <code>null</code> and must be a action guid.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * action.
    * @return Object action object never <code>null</code>, throws
    * {@link RuntimeException} in case of an error.
    */
   private Object loadActionByGuid(IPSGuid guid, boolean readonly)
   {
      // As we do not have the readonly and modifiable versions of Action
      // objects we simply return modifiable object for now for both readonly
      // and modifiable.
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      
      PSAction action = null;
      try
      {
         PSComponentProcessorProxy proxy = 
            PSDesignModelUtils.getComponentProxy();
         Element[] elements = proxy.load(PSAction.getComponentType(
               PSAction.class), new PSKey[] {PSAction.createKey(
                     String.valueOf(guid.getUUID()))});
         if (elements.length > 0)
         {
            action = new PSAction(elements[0]);
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   
      if (action == null)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      
      return action;
   }
   
   @Override
   public IPSGuid nameToGuid(String name)
   {
      if(StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name must not be null");
      }
      PSAction action = (PSAction)load(name);
      return action.getGUID();
   }
   
   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj must not be null");

      if (!(obj instanceof PSAction))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      try
      {
         PSComponentProcessorProxy proxy = 
            PSDesignModelUtils.getComponentProxy();
         proxy.save(new IPSDbComponent[] { ((PSAction)obj) });
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      try
      {
         PSComponentProcessorProxy proxy = 
            PSDesignModelUtils.getComponentProxy();
         int result = proxy.delete(PSAction.getComponentType(
               PSAction.class), new PSKey[] {PSAction.createKey(
                     String.valueOf(guid.getUUID()))});
         if (result < 1)
         {
            String msg = "Failed to delete the design object for guid {0}";
            Object[] args = { guid.toString() };
            throw new RuntimeException(MessageFormat.format(msg, args));
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   }
}
