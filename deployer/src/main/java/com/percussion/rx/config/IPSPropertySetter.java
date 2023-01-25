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
package com.percussion.rx.config;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.error.PSNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * The interface that all property setter must implement. A property setter
 * is used to set the properties for a design object and its associations (if
 * there is any). A property setter is typically specified in a Spring bean
 * file and is created by the Spring framework.
 * 
 * @author bjoginipally
 */
public interface IPSPropertySetter
{
   /**
    * Apply the properties of the setter on the design object and/or its
    * associations (if there is any). 
    * 
    * @param obj the design object, it may be <code>null</code>.
    * 
    * @param state the state of the specified design object. It may be 
    * <code>null</code> if the design object is <code>null</code>, but it is
    * not <code>null</code> if the design object is not <code>null</code>.
    * 
    * @param aSets the list of association sets, may be <code>null</code> if
    * there is no association to be set on the design object. This method is
    * responsible to fill in the merged, replaced, or deleted association if
    * there is any.
    * 
    * @return <code>true</code> if the given object has been modified.
    */
   boolean applyProperties(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets);
   
   /**
    * De-apply the properties of the setter on the design object and/or its
    * associations (if there is any). The current properties are the properties
    * that were successfully applied to the object previously.
    * 
    * @param obj the design object, it may be <code>null</code>. The state
    * of the object is assumed to be {@link ObjectState#PREVIOUS}.
    * 
    * @param aSets the list of association sets, may be <code>null</code> if
    * there is no association to be set on the design object. This method is
    * responsible to fill in deleted association if there is any.
    * 
    * @return <code>true</code> if the given object has been modified.
    */
   boolean deApplyProperties(Object obj, List<IPSAssociationSet> aSets);

   /**
    * Gets all configurable properties. The properties may be wired in by Spring
    * framework, where value of the properties may be in the place holder
    * format, e.g., ${com.percussion.solution.RSS.name}.
    * <p>
    * Note, the value of the properties may be replaced with the value defined
    * in default/local configure files after the calls to
    * {@link #setProperties(Map)} (by the framework).
    * 
    * @return the properties, may be <code>null</code> or empty if there is no
    * configurable properties for this setter.
    */
   Map<String, Object> getProperties();
   
   /**
    * Sets the configurable properties. The value of the properties may be
    * replaced with the value retrieved from local config. The number of the new
    * properties may be less than the number of original properties that were
    * wired in by Spring framework.
    * 
    * @param props the new properties, may be <code>null</code> or empty if
    * there is no configurable properties for this setter.
    */
   void setProperties(Map<String, Object> props);
   
   /**
    * Gets the configurable properties that were previously applied.
    * 
    * @return the previously applied properties, may be <code>null</code> or
    * empty if there is no previous configurable properties.
    */
   Map<String, Object> getPrevProperties();

   /**
    * Sets the configurable properties that were previously applied.
    * 
    * @param props the previously applied properties, may be <code>null</code>
    * or empty if there is no configurable properties for this setter.
    */
   void setPrevProperties(Map<String, Object> props);
 
   /**
    * Validates the properties against another setter properties, which 
    * may have already applied to the specified design object.
    * 
    * @param objName the name of the design object, never <code>null</code> or
    * empty.
    * @param state the state of the design object if apply the properties,
    * never <code>null</code>.
    * @param setter the other setter that contains properties have already 
    * applied to the specified design object, never <code>null</code>.
    * 
    * @return a list of validation results. It may be empty if there is no
    * error or warnings.
    */
   List<PSConfigValidation> validate(String objName, ObjectState state,
         IPSPropertySetter setter);

   /**
    * Scan through all properties {@link #getProperties()}, for each property,
    * creates the property definitions as name/value pairs and add the created
    * property definition to the specified (property definition) holder.
    * <p>
    * Note, The properties from {@link #getProperties()} are expected in its
    * raw format, which may contain ${place-holder} in the value of the 
    * properties and the ${place-holders} have not bean replaced by the 
    * framework.
    * 
    * @param obj the object in question, it may be <code>null</code>. 
    * @param defs the holder for created property definitions, never 
    * <code>null</code>.
    */
   void addPropertyDefs(Object obj, Map<String, Object> defs) throws PSNotFoundException;
}
