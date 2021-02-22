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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class PSDesignModel implements IPSDesignModel
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#delete(com.percussion.utils.guid.IPSGuid)
    */
   public void delete(@SuppressWarnings("unused")
   IPSGuid guid) throws PSNotFoundException {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      Object service = getService();
      Method deleteMethod = null;
      Method[] methods = getService().getClass().getMethods();
      String deleteMethodName = "delete" + getNormalizedTypeEnumName();
      for (Method method : methods)
      {
         if (!method.getName().equalsIgnoreCase(deleteMethodName))
         {
            continue;
         }
         Class[] params = method.getParameterTypes();
         if (params.length == 1 && params[0].getName().endsWith(".IPSGuid"))
         {
            deleteMethod = method;
            break;
         }
      }
      if (deleteMethod == null)
      {
         String msg = "Failed to delete the object with guid ({0}) " +
               "due to failure to find a delete method.";
         Object[] args = {guid.toString()};   
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      Object[] args = { guid };
      try
      {
         deleteMethod.invoke(service, args);
      }
      catch (Exception e)
      {
         String msg = "Failed to delete the object with guid ({0}).";
         Object[] margs = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, margs), e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#delete(java.lang.String)
    */
   public void delete(@SuppressWarnings("unused")
   String name) throws PSNotFoundException {
      IPSGuid guid = nameToGuid(name);
      if (guid == null)
      {
         String msg = "Failed to get the guid for the given design object"
               + " type '{0}' and name '{1}'";
         Object[] args = { getType().name(), name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      delete(guid);
   }

   /*
    * //see base interface method for details
    */
   public List<IPSGuid> findAllIds()
   {
      throw new UnsupportedOperationException("findAllIds() is not supported.");      
   }
   
   /*
    * //see base interface method for details
    */
   public Collection<String> findAllNames()
   {
      throw new UnsupportedOperationException(
            "findAllNames() is not supported.");      
   }

   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#guidToName(com.percussion.utils.guid.IPSGuid)
    */
   public String guidToName(IPSGuid guid) throws PSNotFoundException {
      Object obj = load(guid);
      Object name = null;
      try
      {
         Method method = obj.getClass().getMethod("getName",
               new Class[] {});
         if (method == null)
         {
            String msg = "Failed to get the name of the object for " +
                  "the supplied guid ({0}) as getName method does not " +
                  "exist on design object class.";
            Object[] args = { guid.toString() };
            throw new RuntimeException(MessageFormat.format(msg, args));

         }
         name = method.invoke(obj, new Object[] {});
      }
      catch (Exception e)
      {
          throw new RuntimeException(e);
      }
      return name.toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#load(java.lang.String)
    */
   public Object load(String name) throws PSNotFoundException {
      IPSGuid guid = nameToGuid(name);
      if (guid == null)
      {
         String msg = "Failed to get the guid for the given design object"
               + " type '{0}' and name '{1}'";
         Object[] args = { getType().name(), name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return loadModifiable(guid);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#load(com.percussion.utils.guid.IPSGuid)
    */
   public Object load(IPSGuid guid) throws PSNotFoundException {
      return loadDesignObject(guid, true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#loadModifiable(com.percussion.utils.guid.IPSGuid)
    */
   public Object loadModifiable(IPSGuid guid) throws PSNotFoundException {
      return loadDesignObject(guid, false);
   }

   /**
    * Checks for load or loadModifiable methods that take IPSGuid as argument on
    * the service based on the readonly flag and invokes that method and returns
    * the Object.
    * 
    * @param guid must not be <code>null</code> and must be of the type
    * supported by the design model.
    * @param readonly boolean flag to indicate whether to load the readonly
    * design object or modifiable object. if <code>true</code> loads the
    * readonly design object.
    * @return Object for the supplied guid, never <code>null</code>. Throws
    * {@link RuntimeException} in case of error.
    */
   @SuppressWarnings("unchecked")
   private Object loadDesignObject(IPSGuid guid, boolean readonly)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      Object designObj = null;
      Object service = getService();
      Method loadMethod = null;
      Method[] methods = getService().getClass().getMethods();
      String loadMethodName = "load" + getNormalizedTypeEnumName();
      if (!readonly)
         loadMethodName = "load" + getNormalizedTypeEnumName() + "Modifiable";
      for (Method method : methods)
      {
         if (!method.getName().equalsIgnoreCase(loadMethodName))
         {
            continue;
         }
         Class[] params = method.getParameterTypes();
         if (params.length == 1 && params[0].getName().endsWith(".IPSGuid"))
         {
            loadMethod = method;
            break;
         }
      }
      if (loadMethod == null)
      {
         throw new RuntimeException(
               "Failed to load the object due to failure "
                     + "to find a load method.");
      }
      Object[] args = { guid };
      try
      {
         designObj = loadMethod.invoke(service, args);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      if (designObj == null)
      {
         String msg = "Failed to load the design object for guid ({0}) "
               + "of type ({1})";
         Object[] margs = { guid.toString(), getType().name() };
         throw new RuntimeException(MessageFormat.format(msg, margs));
      }
      return designObj;

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#loadModifiable(java.lang.String)
    */
   public Object loadModifiable(String name) throws PSNotFoundException {
      IPSGuid guid = nameToGuid(name);
      if (guid == null)
      {
         String msg = "Failed to get the guid for the given design object "
               + "type ({0}) and name ({1})";
         Object[] args = { getType().name(), name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return loadModifiable(guid);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#nameToGuid(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public IPSGuid nameToGuid(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be blank");
      IPSGuid guid = null;
      Object service = getService();
      Method findByNameMethod = null;
      Method[] methods = getService().getClass().getMethods();
      Object designObj = null;
      for (Method method : methods)
      {
         if (!method.getName().equalsIgnoreCase(
               "find" + getNormalizedTypeEnumName() + "ByName"))
         {
            continue;
         }
         Class[] params = method.getParameterTypes();
         if (params.length == 1 && params[0].getName().endsWith(".String"))
         {
            findByNameMethod = method;
            break;
         }
      }
      if (findByNameMethod == null)
      {
         String msg = "Failed to get the guid of the object for the supplied "
               + "name ({0}) due to failure to find a find{1}ByName "
               + "method on the service.";
         Object[] args = { name, getType().name() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      Object[] args = { name };
      try
      {
         designObj = findByNameMethod.invoke(service, args);
         if (designObj != null)
            guid = getMatchingGuid(Collections.singletonList(designObj), name);
      }
      catch (Exception e)
      {
         String msg = "Failed to get the guid of the object for the supplied "
            + "name ({0}) of type ({1}).";
         Object[] margs = { name, getType().name() };
         throw new RuntimeException(MessageFormat.format(msg, margs),e);
      }
      return guid;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#save(java.lang.Object)
    */
   public void save(Object obj)
   {
      save(obj, null);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#save(java.lang.Object,
    * java.util.List)
    */
   @SuppressWarnings("unchecked")
   public void save(Object obj, @SuppressWarnings("unused")
   List<IPSAssociationSet> associationSets)
   {
      Method saveMethod = null;
      Object service = getService();
      Method[] methods = service.getClass().getMethods();
      for (Method method : methods)
      {
         if (!method.getName().equalsIgnoreCase("save" + getNormalizedTypeEnumName()))
         {
            continue;
         }
         Class[] params = method.getParameterTypes();
         if (params.length == 1)
         {
            saveMethod = method;
            break;
         }
      }
      if (saveMethod == null)
      {
         throw new RuntimeException(
               "Failed to load the object due to failure "
                     + "to find a load method.");
      }
      Object[] args = { obj };
      try
      {
         saveMethod.invoke(service, args);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Sets the type enum of the design object model.
    * 
    * @param type Must not be <code>null</code>.
    */
   public void setType(PSTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type must not be null");
      m_type = type;
   }

   /**
    * 
    * @return the type enum for the design model. May be <code>null</code> if
    * not set.
    */
   public PSTypeEnum getType()
   {
      return m_type;
   }

   /**
    * Sets the object of the underlying service that has crud services for the
    * design object.
    * 
    * @param service Must not be <code>null</code>.
    */
   public void setService(Object service)
   {
      if (service == null)
         throw new IllegalArgumentException("service must not be null");
      m_service = service;
   }

   /**
    * 
    * @return The object of the underlying service that has crud services for
    * the design object. May be <code>null</code>, if not set.
    */
   protected Object getService()
   {
      return m_service;
   }

   /**
    * Checks whether the supplied guid type is of the model type or not.
    * 
    * @param guid must not be <code>null</code>.
    * @return <code>true</code> if the guid is valid otherwise
    * <code>false</code>.
    */
   protected boolean isValidGuid(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid must not be null");
      return m_type.getOrdinal() == guid.getType();
   }

   /**
    * Convenient method to find the guid of the design object by its name from
    * the supplied list of objects. Loops through the supplied objects and uses
    * reflection to find the name of the each object by method name "getName"
    * and if it matches with the supplied name, then returns the guid returned
    * by getGUID method.
    * 
    * As it uses reflection to get the guid through two methods, getName and
    * getGuid(case insensitive), use it only if the object has those
    * implementations.
    * 
    * @param objects list of design objects, must not be <code>null</code>
    * @param name the name of the object, must not be <code>null</code>.
    * @return The guid of the object having the supplied name or
    * <code>null</code>, if not found.
    * @throws SecurityException
    * @throws NoSuchMethodException
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    */
   @SuppressWarnings("unchecked")
   protected IPSGuid getMatchingGuid(List<Object> objects, String name)
      throws SecurityException, NoSuchMethodException,
      IllegalArgumentException, IllegalAccessException,
      InvocationTargetException
   {
      if (objects == null)
         throw new IllegalArgumentException("objects must not be null");
      if (name == null)
         throw new IllegalArgumentException("name must not be null");

      IPSGuid guid = null;
      for (Object object : objects)
      {
         Class clazz = object.getClass();
         Method method = clazz.getMethod("getName", new Class[0]);
         String n = (String) method.invoke(object, new Object[0]);
         if (!n.equalsIgnoreCase(name))
            continue;
         Method[] methods = clazz.getMethods();
         for (Method gm : methods)
         {
            if (gm.getName().equalsIgnoreCase("getguid"))
            {
               guid = (IPSGuid) gm.invoke(object, new Object[0]);
               break;
            }
         }
      }
      return guid;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.design.IPSDesignModel#getAssociationSets()
    */
   public List<IPSAssociationSet> getAssociationSets()
   {
      return null;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.design.IPSDesignModel#getVersion(com.percussion.utils.guid.IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public Long getVersion(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid must not be null");

      Long version = null;
      Class clazz = null;
      try
      {
         Object designObj = load(guid);
         clazz = designObj.getClass();
         Method method = clazz.getMethod("getVersion", new Class[0]);
         version = Long.valueOf((Integer) method.invoke(designObj,
               new Object[0]));
      }
      catch (NoSuchMethodException e)
      {
         String msg = "Design object class {0} does not implement "
            + "getVersion(IPSGuid).";
         Object[] args = { clazz.toString() };
         ms_logger.warn(MessageFormat.format(msg, args));
      }
      catch (Exception e)
      {
         String msg = "Failed to get the version for design object guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      return version;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.design.IPSDesignModel#getVersion(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public Long getVersion(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be null or empty");
        
      Long version = null;
      Class clazz = null;
      try
      {
         Object designObj = load(name);
         clazz = designObj.getClass();
         Method method = clazz.getMethod("getVersion", new Class[0]);
         version = Long.valueOf((Integer) method.invoke(designObj,
               new Object[0]));
      }
      catch (NoSuchMethodException e)
      {
         String msg = "Design object class {0} does not implement "
            + "getVersion(String).";
         Object[] args = { clazz.toString() };
         ms_logger.warn(MessageFormat.format(msg, args));
      }
      catch (Exception e)
      {
         String msg = "Failed to get the version for design object name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      return version;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.design.IPSDesignModel#getGuid(java.lang.Object)
    */
   public IPSGuid getGuid(Object object)
   {
      IPSGuid guid = null;
      Class clazz = object.getClass();
      Method[] methods = clazz.getMethods();
      for (Method gm : methods)
      {
         if (gm.getName().equalsIgnoreCase("getguid"))
         {
            try
            {
               guid = (IPSGuid) gm.invoke(object, new Object[0]);
            }
            catch (Exception e)
            {
               String msg = "Design object class {0} does not implement "
                  + "getGuid() method.";
               Object[] args = { clazz.toString() };
               ms_logger.warn(MessageFormat.format(msg, args));
            }
            break;
         }
      }
      return guid;
   }
   
   /**
    * @return Returns the normalized type name. May be <code>null</code>, if not set
    * through the bean configuration.
    */
   public String getNormalizedTypeEnumName()
   {
      return StringUtils.isBlank(m_normalizedTypeName) ? m_type.name()
            : m_normalizedTypeName;
   }

   /**
    * Sets the normalized type name. For example name of the PSTypeEnum of
    * content list is CONTENT_LIST, the normalized name is CONTENTLIST.
    * 
    * @param normalizedTypeName Normalized name to set.
    */
   public void setNormalizedTypeEnumName(String normalizedTypeName)
   {
      m_normalizedTypeName = normalizedTypeName;
   }
   
   /**
    * The type enum of the object for which the design model corresponds to.
    * Will be <code>null</code> till the design model factory assigns this
    * value when a model is requested.
    */
   private PSTypeEnum m_type = null;
   
   /**
    * The normalized type enum name. The type enums for some of the design
    * objects have underscores and some of them have _DEF at the end, where as
    * the service calls load type normalized name.
    */
   private String m_normalizedTypeName = null;

   /**
    * The object of the underlying service that has crud services for the design
    * object.
    */
   private Object m_service = "";

   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSDesignModel");
}
