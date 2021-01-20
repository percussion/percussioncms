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
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This implementation of configure handler is for configuring design objects.
 * Walks through all the property setters and calls <code>applyProperties</code>
 * on each setter.
 * 
 * @author bjoginipally
 * 
 */
public class PSObjectConfigHandler implements IPSConfigHandler
{

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#process(java.lang.Object)
    */
   public boolean process(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets)
   {
      boolean changed = false;
      for (IPSPropertySetter setter : m_setters)
      {
         Map<String, Object> props = setter.getProperties();
         if (props == null || props.isEmpty())
            continue;
            
         if (setter.applyProperties(obj, state, aSets))
            changed = true;
      }
      return changed;
   }

   /*
    * //see base class method for details
    */
   public boolean unprocess(Object obj, List<IPSAssociationSet> aSets)
   {
      boolean changed = false;
      for (IPSPropertySetter setter : m_setters)
      {
         Map<String, Object> props = setter.getProperties();
         if (props == null || props.isEmpty())
            continue;
            
         if (setter.deApplyProperties(obj, aSets))
            changed = true;
      }
      return changed;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#getPropertyDefs(java.lang.Object)
    */
   public Map<String, Object> getPropertyDefs(Object obj)
   {
      Map<String, Object> propDefs = new HashMap<String, Object>();
      for (IPSPropertySetter setter : m_setters)
      {
         setter.addPropertyDefs(obj, propDefs);
      }
      return propDefs;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#getPropertySetters()
    */
   public List<IPSPropertySetter> getPropertySetters()
   {
      return m_setters;
   }


   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#setPropertySetters(java.util.List)
    */
   public void setPropertySetters(List<IPSPropertySetter> propSetters)
   {
      m_setters = propSetters;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#getType()
    */
   public PSTypeEnum getType()
   {
      return m_typeEnum;
   }

   /**
    * Sets the type enum of the design object wired by spring.
    */
   public void setType(PSTypeEnum type)
   {
      m_typeEnum = type;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#getName()
    */
   public String getName()
   {
      return m_name;
   }

   /*
    * //see base interface method for details
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalStateException("name may not be null or empty.");
      
      m_name = name;
   }

   @Override
   public boolean equals(Object otherObj)
   {
      if (!(otherObj instanceof PSObjectConfigHandler))
         return false;
      PSObjectConfigHandler other = (PSObjectConfigHandler) otherObj;
      
      return new EqualsBuilder().append(m_name, other.m_name).append(
            m_setters, other.m_setters).isEquals();
   }
   
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_name).append(m_setters)
            .toHashCode();
   }
   
   /*
    * //see base class method for details
    */
   public List<PSPair<Object, ObjectState>> getDesignObjects(
         @SuppressWarnings("unused")
         Map<String, Object> cachedObjs)
   {
      throw new UnsupportedOperationException(
            "getDesignObjects() methed is not supported.");
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#getDefaultDesignObject(java.util.Map)
    */
   public Object getDefaultDesignObject(Map<String, Object> cachedObjs)
   {
      throw new UnsupportedOperationException(
      "getDefaultDesignObject() methed is not supported.");
   }
   
   /*
    * //see base class method for details
    */
   public List<PSPair<String, ObjectState>> getObjectNames()
   {
      if (isGetDesignObjects())   
         return getObjectNamesFromHandlerImpl();
      
      if (getType() == null)
         return Collections.emptyList();
      
      PSPair<String, ObjectState> pair = new PSPair<String, ObjectState>(
            getName(), ObjectState.BOTH);
      return Collections.singletonList(pair);
   }
   
   /**
    * Gets the object names along with its related state. This is only called
    * when {@link #isGetDesignObjects()} returns <code>true</code>.
    * <p>
    * Note, the derived class must override {@link #getCurNames()} and
    * {@link #getPrevNames()} methods if the derived class implements
    * this method.
    * </p>
    * 
    * @return the list of name/state pairs, never <code>null</code>, but may
    * be empty.
    */
   private List<PSPair<String, ObjectState>> getObjectNamesFromHandlerImpl()
   {
      List<PSPair<String, ObjectState>> result = new ArrayList<PSPair<String, ObjectState>>();
      Collection<String> curNames = getCurNames();
      Collection<String> prevNames = getPrevNames();
      
      if (curNames.isEmpty() && prevNames.isEmpty())
         return Collections.emptyList();
      
      // current only for a a refresh installed package
      if (prevNames == null || prevNames.isEmpty())
      {
         for (String name : curNames)
            result
                  .add(new PSPair<String, ObjectState>(name, ObjectState.CURRENT));
         return result;
      }

      // get object names in both current and previous
      List<String> names = new ArrayList<String>();
      names.addAll(curNames);
      names.retainAll(prevNames);
      for (String name : names)
         result.add(new PSPair<String, ObjectState>(name, ObjectState.BOTH));
      
      // get object names in current only
      names.clear();
      names.addAll(curNames);
      names.removeAll(prevNames);
      for (String name : names)
         result.add(new PSPair<String, ObjectState>(name, ObjectState.CURRENT));
      
      // get object names in previous only
      names.clear();
      names.addAll(prevNames);
      names.removeAll(curNames);
      for (String name : names)
         result.add(new PSPair<String, ObjectState>(name, ObjectState.PREVIOUS));
      
      return result;
   }
   
   /**
    * Gets the name of the design object from current properties.
    * This is used by {@link #getObjectNames()}.
    * 
    * @return the names, never <code>null</code>, but may be empty.
    */
   protected Collection<String> getCurNames()
   {
      throw new UnsupportedOperationException(
            "getCurNames() methed is not supported.");
   }
   
   /**
    * Gets the name of the design object from previous properties.
    * This is used by {@link #getObjectNames()}.
    * 
    * @return the names, never <code>null</code>, but may be empty.
    */
   protected Collection<String> getPrevNames()
   {
      throw new UnsupportedOperationException(
            "getPrevNames() methed is not supported.");
   }

   /*
    * //see base class method for details
    */
   public boolean isGetDesignObjects()
   {
      return false;
   }
   
   /*
    * //see base class method for details
    */
   @SuppressWarnings("unchecked")
   public Map<String, Object> getExtraProperties()
   {
      return Collections.EMPTY_MAP;
   }

   /*
    * //see base class method for details
    */
   public void setExtraProperties(@SuppressWarnings("unused")
   Map<String, Object> props)
   {
      throw new UnsupportedOperationException(
            "setExtraProperties() methed is not supported.");      
   }
   
   /*
    * //see base class method for details
    */
   public Map<String, Object> getPrevExtraProperties()
   {
      return m_prevExtraProps;
   }
   
   /*
    * //see base class method for details
    */
   public void setPrevExtraProperties(Map<String, Object> props)
   {
      m_prevExtraProps = props;
   }
   
   /*
    * //see base class method for details
    */
   public IPSGuid saveResult(IPSDesignModel model, Object obj,
         @SuppressWarnings("unused")
         ObjectState state, List<IPSAssociationSet> assocList)
   {
      model.save(obj, assocList);
      return model.getGuid(obj);
   }
   

   /*
    * //see base class method for details
    */
   public List<PSConfigValidation> validate(IPSConfigHandler other)
   {
      List<PSPair<String, ObjectState>> commonNames = getCommonObjectNames(other);
      List<PSConfigValidation> result = new ArrayList<PSConfigValidation>();
      List<PSConfigValidation> subResult;
      for (PSPair<String, ObjectState> pair : commonNames)
      {
         subResult = validate(pair.getFirst(), pair.getSecond(), other
               .getPropertySetters());
         result.addAll(subResult);
      }
      return result;
   }

   /**
    * Validates all setter's properties against the specified setters for a
    * given design object name. The properties of the specified setters have
    * already applied to the given design object.
    * 
    * @param name the name of the design object, never <code>null</code> or
    * empty.
    * @param state the state of the design object, default to
    * {@link ObjectState#BOTH} if unknown.
    * @param oSetters a list of setters with their properties that have already
    * applied to the design object, never <code>null</code>, may be empty.
    * 
    * @return the validated result. It never <code>null</code>, but may be
    * empty if there is no error or warning.
    */
   protected List<PSConfigValidation> validate(String name,
         IPSConfigHandler.ObjectState state, List<IPSPropertySetter> oSetters)
   {
      // validate all setters, one at a time
      List<PSConfigValidation> result = new ArrayList<PSConfigValidation>();
      for (IPSPropertySetter mySetter : getPropertySetters())
      {
         List<PSConfigValidation> setterResult;
         for (IPSPropertySetter oSetter : oSetters)
         {
            if (mySetter.getClass().equals(oSetter.getClass()))
            {
               setterResult = mySetter.validate(name, state, oSetter);
               result.addAll(setterResult);
            }
         }
      }
      
      // update the validation result
      for (PSConfigValidation v : result)
         v.setObjectType(getType());
      
      return result;
   }
   
   /**
    * Gets the object names (and its state) that are defined in both the
    * current and the specified handler.
    * 
    * @param other the specified handler, assumed not <code>null</code>.
    * 
    * @return the common name/state pairs. It may be empty if the type of the
    * handlers is not the same or not defined, or there is no common names. 
    * The state of the object is from the current handler.
    */
   protected List<PSPair<String, ObjectState>> getCommonObjectNames(
         IPSConfigHandler other)
   {
      // is the same design object?
      if (getType() == null || other.getType() == null
            || (!getType().equals(other.getType())))
         return Collections.emptyList();

      Map<String, ObjectState> myNames = getObjectNames(this);
      if (myNames.isEmpty())
         return Collections.emptyList();
      Map<String, ObjectState> otherNames = getObjectNames(other);
      if (otherNames.isEmpty())
         return Collections.emptyList();

      // find names in both names
      List<String> names = new ArrayList<String>();
      names.addAll(myNames.keySet());
      names.retainAll(otherNames.keySet());

      List<PSPair<String, ObjectState>> result = new ArrayList<PSPair<String, ObjectState>>();
      for (String name : names)
      {
         PSPair<String, ObjectState> pair = new PSPair<String, ObjectState>(
               name, myNames.get(name));
         result.add(pair);
      }
      return result;
   }
   
   /**
    * Gets the name/state pairs for the specified handler.
    * 
    * @param h the handler in question, assumed not <code>null</code>.
    * 
    * @return the name/state pairs, it may be empty if the type and/or 
    * name property are not defined in the handler, never <code>null</code>.
    */
   private Map<String, ObjectState> getObjectNames(IPSConfigHandler h)
   {
      List<PSPair<String, ObjectState>> pairs = h.getObjectNames();
      Map<String, ObjectState> map = new HashMap<String, ObjectState>();
      for (PSPair<String, ObjectState> p : pairs)
      {
         map.put(p.getFirst(), p.getSecond());
      }
      return map;
   }
   
   /**
    * The type of the design object. It may be <code>null</code> if there is
    * no design object for this handler.
    */
   private PSTypeEnum m_typeEnum;

   /**
    * Name of the design object, will be <code>null</code> if it has not
    * been set through a property in the config definition file on the handler
    * bean.
    */
   private String m_name = null;

   /**
    * List of property setters, wired in by spring from bean definition.
    */   
   private List<IPSPropertySetter> m_setters = 
      new ArrayList<IPSPropertySetter>();
   
   /**
    * The handler specific properties used in previous configuration. It may
    * be <code>null</code> if there is no previous configuration.
    */
   private Map<String, Object> m_prevExtraProps;
}
