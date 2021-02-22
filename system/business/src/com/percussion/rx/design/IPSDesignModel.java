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
package com.percussion.rx.design;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.List;

/**
 * Design Object model to perform load, save and delete operations on design
 * objects. It also provides convertion methods from name to guid and guid to
 * name. This is a low level model and ignores the locks while performing these
 * operations. The objects must be instantiated with the type enum. 
 * 
 * @author bjoginipally
 * 
 */
public interface IPSDesignModel
{

   /**
    * Loads the read only design object of the supplied guid.
    * 
    * @param guid Guid of the design object that needs to be loaded. Must not be
    * <code>null</code> must be of type with which the design object has been
    * instantiated.
    * @return Object Design object corresponding to the supplied guid, never
    * <code>null</code>, throws <code>RunTimeException</code> in case of
    * error.
    */
   public Object load(IPSGuid guid) throws PSNotFoundException;

   /**
    * Convenient method to load the read only design object with the given name
    * of the object.
    * 
    * @param name The name of the design object must not be <code>null</code>
    * or empty.
    * @see #load(IPSGuid) for details.
    */
   public Object load(String name) throws PSNotFoundException;

   /**
    * Loads the modifiable design object of the supplied guid.
    * 
    * @param guid Guid of the design object that needs to be loaded. Must not be
    * <code>null</code> must be of type with which the design object has been
    * instantiated.
    * @return Object Design object corresponding to the supplied guid, never
    * <code>null</code>, throws <code>RunTimeException</code> in case of
    * error.
    */
   public Object loadModifiable(IPSGuid guid) throws PSNotFoundException;

   /**
    * Convenient method to load the modifiable design object with the given name
    * of the object.
    * 
    * @param name The name of the design object must not be <code>null</code>
    * or empty.
    * @see #load(IPSGuid) for details.
    */
   public Object loadModifiable(String name) throws PSNotFoundException;

   /**
    * Saves the supplied design object.
    * 
    * @param obj Design object to be saved, must not be <code>null</code> and
    * must be of the same object of type with which the model is initiated,
    * throws <code>RunTimeException</code> in case of error.
    */
   public void save(Object obj);

   /**
    * Saves the supplied design object and its associations with the other
    * objects.
    * 
    * @param obj Design object to be saved, must not be <code>null</code> and
    * must be of the same object of type with which the model is initiated.
    * 
    * @param associationSets List of association sets, if <code>null</code> or
    * empty the original associations are not touched. Must be of the type
    * supported by the design model. throws <code>RunTimeException</code> in
    * case of error.
    * 
    */
   public void save(Object obj, List<IPSAssociationSet> associationSets);

   /**
    * Deletes the design object corresponding to the supplied guid.
    * 
    * @param guid Guid of the design object that needs to be deleted. Must not
    * be <code>null</code> must be of type with which the dsign object has
    * been instantiated, throws <code>RunTimeException</code> in case of
    * error.
    */
   public void delete(IPSGuid guid) throws PSNotFoundException;

   /**
    * Convenient method to delete the design object with the given name of the
    * object.
    * 
    * @param name The name of the design object must not be <code>null</code>
    * or empty.
    * @see #delete(IPSGuid) for details
    */
   public void delete(String name) throws PSNotFoundException;

   /**
    * Gets the guid of the design object corresponding to the supplied name.
    * 
    * @param name The name of the design object must not be <code>null</code>
    * or empty.
    * @return IPSGuid corresponding to the name of the design object, may be
    * <code>null</code> if no guid of the type exists with the given name.
    */
   public IPSGuid nameToGuid(String name);

   /**
    * Gets the name of the design object corresponding to the supplied guid.
    * 
    * @param guid Guid of the design object for which the name is needed. Must
    * not be <code>null</code> and must be of type with which the design object
    * has been instantiated
    * @return String name of the design object. Never <code>null</code>, may
    * be empty.
    */
   public String guidToName(IPSGuid guid) throws PSNotFoundException;
   
   /**
    * Returns the list of association sets that are available for the design
    * model. May be <code>null</code> if the design model does not support the
    * object associations.
    * 
    * @return list of association sets or <code>null</code>, if the design
    * object does not support associations.
    */
   public List<IPSAssociationSet> getAssociationSets();
   
   /**
    * Returns the current version of the design object corresponding to the
    * supplied guid. May be <code>null</code> if the object does not support
    * version.
    * 
    * @param guid Guid of the design object whose version is requested. Must not
    * be <code>null</code> must be of type with which the design object has been
    * instantiated.
    * @return version of the design object or <code>null</code> if the design
    * object does not support version.
    */
   public Long getVersion(IPSGuid guid);
   
   /**
    * Returns the current version of the design object corresponding to the
    * supplied name. May be <code>null</code> if the object does not support
    * version.
    * 
    * @param name The name of the design object whose version is requested. Must
    * not be <code>null</code> or empty.
    * @return version of the design object or <code>null</code> if the design
    * object does not support version.
    */
   public Long getVersion(String name);
   
   /**
    * Finds all IDs for the current design model.
    * 
    * @return all IDs of the design object for this model, which is in no 
    * particular order. It may be empty if there is no design object for this 
    * model, never <code>null</code>.
    */
   public Collection<IPSGuid> findAllIds();
   
   /**
    * Finds all names of the design object for the current design model.
    * 
    * @return all names of the design object for this model, which is in no 
    * particular order. It may be empty if there is no design object for this 
    * model, never <code>null</code>.
    */
   public Collection<String> findAllNames();
   
   /**
    * Gets the guid of the object.
    * @param obj Design object for which the guid needs to be returned.
    * @return The guid of the object or <code>null</code> if the object does
    * not have a getguid(case insensitive) method.
    */
   public IPSGuid getGuid(Object obj);
}
