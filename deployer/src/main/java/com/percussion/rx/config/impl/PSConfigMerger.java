/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to load the design objects for each handler and
 * process the handler which actually applies the configuration on to design
 * objects and save the design objects.
 * 
 * @author bjoginipally
 */
public class PSConfigMerger
{
   /**
    * Merges the configuration on the design objects and saves them. Gets the
    * design model for each handler. Loads the design object for each handler.
    * Calls the process method on each handler with the design object or
    * <code>null</code>. (<code>null</code> in case if the handler has no
    * type enum and design object name). Saves the design objects. Throws run
    * time exception in case of error.
    * 
    * @param cfgHandlers the configure handlers, never <code>null</code>.
    * @param hasPrevProps <code>true</code> if there are previous properties.
    * @param isApplyConfig <code>true</code> if applying the configuration or
    * call {@link IPSConfigHandler#process(Object, ObjectState, List)} for each
    * handler; otherwise de-configure or call
    * {@link IPSConfigHandler#unprocess(Object, List)} for each handler.
    * 
    * @return a set of IDs of the configured design objects. It may be empty;
    * but never <code>null</code>.
    */
   public PSPair<Collection<IPSGuid>, PSConfigException> merge(List<IPSConfigHandler> cfgHandlers,
         boolean hasPrevProps, boolean isApplyConfig)
   {
      if (cfgHandlers == null)
         throw new IllegalArgumentException("cfgHandlers must not be null");
      List<IPSGuid> processedGuids = new ArrayList<IPSGuid>();
      PSConfigException exceptionDuringSave = null;
      IPSDesignModelFactory dmFactory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      //Get the model and load the objects
      for (IPSConfigHandler handler : cfgHandlers)
      {
         PSTypeEnum type = handler.getType();
         if (type == null)
         {
            m_handlerData.put(handler, new HandlerData(null,null,null));
            continue;
         }
         IPSDesignModel model = dmFactory.getDesignModel(type);
         List<PSPair<Object, ObjectState>> objs = getDesignObjectsWithState(type, model,
               handler, hasPrevProps);
         if (objs.isEmpty())
         {
            m_handlerData.put(handler, new HandlerData(null,null,null));
            continue;
         }

         m_handlerData.put(handler, new HandlerData(model, objs, model
               .getAssociationSets()));
      }

      // Process the handlers
      try
      {
         List<IPSAssociationSet> assocList;
         for (IPSConfigHandler handler : cfgHandlers)
         {
            IPSDesignModel model = m_handlerData.get(handler).mi_model;
            List<PSPair<Object, ObjectState>> objs = m_handlerData
                  .get(handler).mi_designObjects;
            assocList = m_handlerData.get(handler).mi_associationSets;
            if (objs == null || objs.isEmpty())
            {
               if (isApplyConfig)
                  handler.process(null, null, assocList);
               else
                  handler.unprocess(null, assocList);
            }
            else
            {
               for (PSPair<Object, ObjectState> op : objs)
               {
                  Object o = op.getFirst();
                  ObjectState state = op.getSecond();
                  if (model != null)
                  {
                     boolean processed = false;
                     if (isApplyConfig)
                        processed = handler.process(o, state, assocList);
                     else
                        processed = handler.unprocess(o, assocList);
                     if (processed)
                     {
                        IPSGuid processedGuid = handler.saveResult(model, o,
                              state, assocList);
                        if (processedGuid != null)
                           processedGuids.add(processedGuid);
                     }
                  }
               }
            }
         }
      }
      catch (PSConfigException e)
      {
         exceptionDuringSave = e;
      }
      
      return new PSPair<Collection<IPSGuid>, PSConfigException>(
            processedGuids, exceptionDuringSave);
   }

   /**
    * Creates a map of name of the property as key and its value as object, if
    * there are any exceptions getting the property values, then the exceptions
    * are added to a collection. Note: The handler processing stops as soon as
    * it hits the error and skips processing rest of the property setters and
    * properties if any.
    * 
    * @param cfgHandlers The config handlers with unresolved replacement names.
    * @return PSPair with the first object being Map of property names and
    * values as Objects. The object could be a String or Map or List and the
    * second object is list of exceptions. Neither the pair nor the parts
    * are null. The objects of the pairs may be empty.
    */
   @SuppressWarnings("unchecked")
   public PSPair<Map<String, Object>, List<Exception>> getPropertyDefs(
         List<IPSConfigHandler> cfgHandlers)
   {
      if (cfgHandlers == null)
         throw new IllegalArgumentException("cfgHandlers must not be null");
      Map<String, Object> props = new HashMap<String, Object>();
      List<Exception> cfgExceptions = new ArrayList<Exception>();
      PSPair<Map<String, Object>, List<Exception>> result = new PSPair(
            props, cfgExceptions);
      for (IPSConfigHandler handler : cfgHandlers)
      {
         try
         {
            PSTypeEnum type = handler.getType();
            if (type == null)
            {
               props.putAll(handler.getPropertyDefs(null));
               continue;
            }
            Object obj = getDesignObject(handler);
            props.putAll(handler.getPropertyDefs(obj));
         }
         catch (Exception e)
         {
            cfgExceptions.add(e);
         }
      }
      return result;
   }
   
   /**
    * Convenient method to get the design object from local storage if exists,
    * otherwise loads from the model and returns the object. Updates the local
    * storage.
    * 
    * @param type The type enum of the object assumed not <code>null</code>.
    * @param model The model of the object, assumed not <code>null</code>.
    * @param handler the handler that will be used to process the returned
    * Design Objects, assumed not <code>null</code>.
    * @param hasPrevProps <code>true</code> if there are previous properties.
    * 
    * @return the objects, it never <code>null</code>, but may be empty if
    * cannot find any objects for the given handler.
    */
   @SuppressWarnings("unchecked")
   private List<PSPair<Object, ObjectState>> getDesignObjectsWithState(PSTypeEnum type,
         IPSDesignModel model, IPSConfigHandler handler, boolean hasPrevProps)
   {
      Map<String, Object> typeMap = m_designObjects.get(type);
      if (typeMap== null)
      {
         typeMap = new HashMap<String, Object>();
         m_designObjects.put(type, typeMap);
      }

      // get the Design Objects from the handler?
      if (handler.isGetDesignObjects())
         return handler.getDesignObjects(typeMap);
      
      // get the Design Objects by name 
      List<PSPair<Object, ObjectState>> objs = new ArrayList<PSPair<Object, ObjectState>>();
      
      // load object from current configure
      String name = handler.getName();
      Object obj = typeMap.get(name);
      if (obj == null)
      {
         obj = model.loadModifiable(name);
         typeMap.put(name, obj);
      }
      
      // load object from previous configure if there is any
      ObjectState state = hasPrevProps ? ObjectState.BOTH : ObjectState.CURRENT;
      objs.add(new PSPair<Object, ObjectState>(obj, state));
      
      return objs;
   }

   /**
    * Convenient method to get the design object from local storage if exists,
    * otherwise loads from the model and returns the object. Updates the local
    * storage. If the handlers {@link IPSConfigHandler#isGetDesignObjects()} is
    * <code>true</code> then calls handlers
    * {@link IPSConfigHandler#getDefaultDesignObject(Map)} to get the design
    * object and returns it.
    * 
    * @param handler the handler from which the object is loaded.
    * 
    * @return the design object, never <code>null</code> throws
    * {@link RuntimeException} if failed to find the model and the
    * IPSDesignModelFactory throws {@link RuntimeException} if it fails to load
    * the design object with the given name.
    */
   private Object getDesignObject(IPSConfigHandler handler)
   {
      PSTypeEnum type = handler.getType();
      IPSDesignModelFactory dmFactory = PSDesignModelFactoryLocator
      .getDesignModelFactory();
      IPSDesignModel model = dmFactory.getDesignModel(type);
      if(model == null)
      {
         throw new PSConfigException("Failed to find the design model " +
               "for the handler with type \"" + type + "\"");
      }
      Map<String, Object> typeMap = m_designObjects.get(type);
      if (typeMap== null)
      {
         typeMap = new HashMap<String, Object>();
         m_designObjects.put(type, typeMap);
      }
      if (handler.isGetDesignObjects())
      {
         return handler.getDefaultDesignObject(typeMap);
      }

      // load object from current configure
      String name = handler.getName();
      Object obj = typeMap.get(name);
      if (obj == null)
      {
         obj = model.loadModifiable(name);
         typeMap.put(name, obj);
      }
      return obj;
   }
   
   /**
    * Helper class to hold data for object handlers.
    * @author bjoginipally
    *
    */
   class HandlerData
   {
      HandlerData(IPSDesignModel model,
            List<PSPair<Object, ObjectState>> designObjects,
            List<IPSAssociationSet> associationSets)
      {
         mi_model = model;
         mi_designObjects = designObjects;
         mi_associationSets = associationSets;
      }
      IPSDesignModel mi_model;
      List<PSPair<Object, ObjectState>> mi_designObjects;
      List<IPSAssociationSet> mi_associationSets;
   }
   
   /**
    * Handler data map
    */
   private Map<IPSConfigHandler, HandlerData> m_handlerData = 
      new HashMap<IPSConfigHandler, HandlerData>();
   
   /**
    * This is a map of type enum and a map of object name and actual object. If
    * multiple handlers use the same design object, we get the design object
    * from this map.
    */
   private Map<PSTypeEnum, Map<String, Object>> m_designObjects = 
      new HashMap<PSTypeEnum, Map<String, Object>>();
}
