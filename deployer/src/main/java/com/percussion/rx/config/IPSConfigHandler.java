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
package com.percussion.rx.config;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.util.List;
import java.util.Map;

/**
 * Configure handler represents a bean from configure definition file
 * instantiated by spring framework<br>
 * The following is an example of a typical bean. See configDef schema for more
 * details.
 * 
 * <pre>
 * &lt;bean id=&quot;SnipTemplate&quot; class=&quot;com.percussion.rx.config.impl.PSObjectConfigHandler&quot;&gt;
 *  &lt;property name=&quot;name&quot; value=&quot;rffSnCallout&quot;/&gt;
 *  &lt;property name=&quot;type&quot; value=&quot;TEMPLATE&quot;/&gt;
 *  &lt;property name=&quot;propertySetters&quot;&gt;
 *  &lt;bean class=&quot;com.percussion.rx.config.impl.PSSimplePropertySetter&quot;&gt;
 *  &lt;property name=&quot;properties&quot;&gt;
 *  &lt;map&gt;
 *  &lt;entry key=&quot;label&quot; value=&quot;${com.percussion.RSS.templateLabel}&quot;/&gt;
 *  &lt;/map&gt;
 *  &lt;/property&gt;
 *  &lt;/bean&gt;
 *  &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * It consists of design object info properties and property setters to set the
 * property values on to the design objects. This class is responsible for
 * applying the properties on the design objects through the property setters.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSConfigHandler
{
   /**
    * The state of a design object.
    */
   public enum ObjectState
   {
      /**
       * The design object is defined in current configuration only.
       */
      CURRENT,

      /**
       * The design object is defined in previous configuration only.
       */
      PREVIOUS,

      /**
       * The design object is defined both current and previous configurations.
       */
      BOTH
   }

   /**
    * This method responsible to process the properties. If name and type
    * properties exist on the handler then the service calls this method with
    * the design object otherwise <code>null</code>. This method is
    * responsible for calling walking through the property setters and apply the
    * properties on the design objects.
    * 
    * @param obj the (single) design object. It may be <code>null</code> if
    * the "type" and "name"/"names" are not provided.
    * 
    * @param state the state of the specified design object. It may be
    * <code>null</code> if the design object is <code>null</code>, but it
    * is not <code>null</code> if the design object is not <code>null</code>.
    * 
    * @param associationSets list of association sets, may be <code>null</code>.
    * 
    * @return <code>true</code> if the design object has been modified.
    */
   boolean process(Object obj, ObjectState state,
         List<IPSAssociationSet> associationSets);

   /**
    * This is the opposite operation as
    * {@link #process(Object, ObjectState, List)}. It de-configures the
    * properties which were applied previously. This is called during un-install
    * process.
    * 
    * @param obj the (single) design object. It may be <code>null</code> if
    * the "type" and "name"/"names" are not provided.
    * 
    * @param associationSets list of association sets, may be <code>null</code>.
    */
   boolean unprocess(Object obj, List<IPSAssociationSet> associationSets);

   /**
    * Returns the property setters of the handler, may be <code>null</code> or
    * empty.
    * 
    * @return list of property setters.
    */
   List<IPSPropertySetter> getPropertySetters();

   /**
    * Set properties for this handler this may be wired by spring framework. The
    * properties that needs to be applied on the design object.
    * 
    * @param setters property setters
    */
   void setPropertySetters(List<IPSPropertySetter> setters);

   /**
    * The type enum of the design object wired by spring, may be
    * <code>null</code> if the property with name as "type" is not provided.
    * 
    * @return the type enum, may be <code>null</code>.
    */
   PSTypeEnum getType();

   /**
    * Gets name of the design object.
    * 
    * @return The name of the design object, it may be <code>null</code> if
    * the name is not defined.
    */
   String getName();

   /**
    * Sets the name of the design object.
    * 
    * @param name the name of the design object. It may not be <code>null</code>
    * or empty.
    */
   void setName(String name);

   /**
    * Gets the Design Objects that are loaded, created or find from the cached
    * Design Objects (which are managed by the system). The handler must look
    * for the Design Objects from the given cache first before load or create a
    * new ones for the returned Design Objects.
    * <p>
    * Note, the {@link #isGetDesignObjects()} must be <code>true</code>;
    * otherwise this method must not be called.
    * 
    * @param cachedObjs the cached Design Objects. It maps name to its object.
    * The Design Objects are loaded or created by other handlers or the system.
    * This method must maintain this cache, that is to set the loaded or created
    * Design Objects into this cache, so that it can be used by other handlers.
    * 
    * @return the Design Objects (with their state) that are loaded, created or
    * find from the cached Design Objects. It never <code>null</code>, but
    * may be empty if there is no loaded or created object from the handler.
    */
   List<PSPair<Object, ObjectState>> getDesignObjects(
         Map<String, Object> cachedObjs);

   /**
    * Gets the Design Object names along with its related state. This is similar
    * with {@link #getDesignObjects(Map)}, except this returns names of the
    * object, but not the design objects themself.
    * 
    * @return the list of name/state pairs. It never <code>null</code>, but
    * may be empty if there is no loaded or created object from the handler.
    */
   public List<PSPair<String, ObjectState>> getObjectNames();

   /**
    * Determines if the handler provides the configured Design Objects.
    * 
    * @return <code>true</code> if the Design Objects will be provided by the
    * handler.
    */
   boolean isGetDesignObjects();

   /**
    * Returns additional properties that are specific for this handler.
    * 
    * @return the additional properties if there is any. It may not be
    * <code>null</code>, but may be empty if there is no additional
    * properties for this handler. In case there are handler specific
    * properties, then it must not be empty, the map key is the name of the
    * property, which maps to its value. The map value may be <code>null</code>.
    */
   Map<String, Object> getExtraProperties();

   /**
    * Sets the handler specific properties.
    * 
    * @param props the handler specific properties, never <code>null</code>,
    * but may be empty. The map key is the name of the property, which maps to
    * its value.
    */
   void setExtraProperties(Map<String, Object> props);

   /**
    * Gets the extra properties used in previous configuration.
    * 
    * @return the previous properties, it may be <code>null</code> or empty if
    * there is no previous properties.
    */
   Map<String, Object> getPrevExtraProperties();

   /**
    * Sets the extra properties used in previous configuration.
    * 
    * @param props the extra properties used in previous configuration, it may
    * be <code>null</code> or empty.
    */
   void setPrevExtraProperties(Map<String, Object> props);

   /**
    * Saves the processed result. This is typically called after
    * {@link #process(Object, List)}.
    * 
    * @param model the model of the design object, never <code>null</code>.
    * @param obj the design object that has been processed by
    * {@link #process(Object, List)}, never <code>null</code>.
    * @param state the state of the specified design object. It may be
    * <code>null</code> if the design object is <code>null</code>, but it
    * is not <code>null</code> if the design object is not <code>null</code>.
    * @param assocList the associations that has been processed by
    * {@link #process(Object, List)}, it may be <code>null</code> or empty if
    * there is no associations to be processed.
    * @return the guid of the updated object.
    */
   IPSGuid saveResult(IPSDesignModel model, Object obj, ObjectState state,
         List<IPSAssociationSet> assocList);

   /**
    * Validates the design objects specified in current configure against the
    * objects specified in another handler, which may exist in different
    * configure (or package).
    * 
    * @param other the handler to validate against with, it is not
    * <code>null</code>.
    * 
    * @return a list of validation results. It may be empty if there is no error
    * or warnings.
    */
   List<PSConfigValidation> validate(IPSConfigHandler other);

   /**
    * Returns the property defs of all the setters the handler consists of. It
    * is a map of property replacement names and values. The value is an Object
    * and could be a String, List or Map.
    * 
    * @param obj The design object from which the values of properties are
    * obtained. This may be <code>null</code>, its is implementations should
    * take care of it.
    * @return A map of replacement name of the property and the value of the
    * property, never <code>null</code>, may be empty.
    */
   Map<String, Object> getPropertyDefs(Object obj);

   /**
    * Gets the first available design object that is either loaded, created or
    * find from the cached Design Objects (which are managed by the system). The
    * handler must look for the Design Objects from the given cache first before
    * load or create a new ones for the returned Design Objects.
    * <p>
    * Note, the {@link #isGetDesignObjects()} must be <code>true</code>;
    * otherwise this method must not be called.
    * 
    * @param cachedObjs the cached Design Objects. It maps name to its object.
    * The Design Objects are loaded or created by other handlers or the system.
    * This method must maintain this cache, that is to set the loaded or created
    * Design Objects into this cache, so that it can be used by other handlers.
    * 
    * @return the Design Object that is either loaded, created or find from the
    * cached Design Objects. It may be <code>null</code>, if not found.
    */
   Object getDefaultDesignObject(Map<String, Object> cachedObjs);
}
