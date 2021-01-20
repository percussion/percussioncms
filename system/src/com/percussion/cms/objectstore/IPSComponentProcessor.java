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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;

import org.w3c.dom.Element;

/**
 * Classes implementing this interface are responsible for providing the
 * capability to save, load and remove CMS components to/from the database.
 * Users of the CMS objectstore will use processors to serialize and
 * deserialize components that implement the IPSDbComponent interface.
 * <p>Although not explicitly shown, all methods may require authorization in
 * order to complete successfully. If the user is not authorized, either a
 * security exception is thrown or the exception will be wrapped in the
 * PSCmsException that all methods throw. The security information is passed
 * to the processor when it is created.
 *
 * @author Paul Howard
 * @version 1.0
 */
public interface IPSComponentProcessor
{
   /**
    * Obtains 0 or more serialized versions of formerly persisted objects of
    * the specified type that match the supplied locator(s). The returned xml
    * can be passed directly to the {@link IPSDbComponent#fromXml(Element)
    * fromXml} method of the component whose type was supplied. Typically,
    * this is the base name of the class (e.g., class <code>
    * com.percussion.cms.objectstore.PSSlot</code> would use the type <code>
    * PSSlot</code>).
    *
    * @param componentType  The base class name of the component being
    *    processed. Never empty or <code>null</code>. If the name is not found
    *    in the config, an exception is thrown.
    *
    * @param locators  References to existing objects which you wish to
    *    re-serialize. All entries must be of the same type. If an entry
    *    cannot be found for a key,
    *    no Element will be returned. If the size of the returned array is
    *    less than the # of keys supplied, it is the responsibility of the
    *    caller to determine which keys were skipped because an object could
    *    not be found. If <code>null</code>, all objects of the requested
    *    type are returned. If any entry is <code>null</code>, an exception
    *    is thrown.
    *
    * @return An array with 0 or more xml fragments, each appropriate to
    *    populate an instance of the component whose name was supplied. An
    *    entry will be <code>null</code> if the supplied key does not
    *    reference an object of the specified type.
    *
    * @throws PSCmsException If the processor for the supplied type is not
    *    found or cannot be instantiated, or any problems occur while getting
    *    the data.
    */
   public Element [] load(String componentType, PSKey [] locators)
      throws PSCmsException;


   /**
    * Inserts or updates an existing object. The default processor will call the
    * {@link IPSDbComponent#toDbXml(org.w3c.dom.Document, Element, 
    * IPSKeyGenerator, PSKey)} method of the supplied component and send that to
    * the resource specified in the processor's config data. If a custom
    * processor has been supplied, you would need to review the doc for that
    * processor to determine what actions are taken, The net result though is
    * that after this call completes, the object will be properly updated in the
    * database.
    * <p>
    * After successful update, the {@link IPSDbComponent#setPersisted()
    * setPersisted} method of the component is called to re-synchronize with the
    * database.
    * 
    * @param components
    *           The component(s) that need to be inserted or updated. The states
    *           of the supplied components will be checked first; any with
    *           DBSTATE_UNMODIFIED will be skipped. Never <code>null</code>.
    *           Any <code>null</code> entries will be skipped. The component
    *           type is obtained by calling getComponentType() on the elements.
    *           The type name is then used to find the processor properties in
    *           the config data. If an entry cannot be found, an exception is
    *           thrown.
    * 
    * @return A set of components equivalent to the ones supplied (they may or
    *         may not be the exact ones supplied, depending on the processor)
    *         where each one will return <code>true</code> for isPersisted.
    *         You will get back the same component if it was unmodified when
    *         supplied. The returned components are not necessarily in the same
    *         order as when they were supplied. This object also contains
    *         statistics specifying how many components were saved or deleted.
    * 
    * @throws PSCmsException
    *            If the processor for the supplied type is not found or cannot
    *            be instantiated, or any problems occur while getting the data.
    */
   public PSSaveResults save(IPSDbComponent [] components)
      throws PSCmsException;


   /**
    * Permanently deletes a specified set of components from the database.
    * The caller is responsible for calling the {@link 
    * IPSDbComponent#setPersisted()} method on each component for
    * which a key was supplied, if such a component exists.
    *
    * @param componentType  The value returned by the {@link
    *    IPSDbComponent#getComponentType() getComponentType} method of the
    *    component being processed. Never empty or <code>null</code>. If the
    *    name is not found in the config, an exception is thrown.
    *
    * @param locators  References to existing objects which you wish
    *    to permanently remove from the system. If an entry cannot be found
    *    for a key, that key is skipped and processing continues normally.
    *    If <code>null</code> is found in the array, it is skipped and no
    *    error is reported.
    *    <p>You can determine which keys were skipped (if any) by calling the
    *    <code>isAssigned</code> method; it will return <code>false</code>
    *    for all successful deletes.
    *
    * @return The number of components actually deleted.
    *
    * @throws PSCmsException If the processor for the supplied type is not
    *    found or cannot be instantiated, or any problems occur while removing
    *    the data.
    */
   public int delete(String componentType, PSKey [] locators)
      throws PSCmsException;


   /**
    * Convenience method that calls {@link #delete(String,PSKey[])} for each
    * group of components that have the same type.
    * <p>If the deletion is successful, the setPersisted method of each
    * IPSDbComponent is called.
    *
    * @param comps  The component(s) that need to be removed. The
    *    states of the supplied components will be checked first;
    *    any with unpersisted keys will be skipped.
    *    Never <code>null</code> and no entry may be <code>null</code>. The
    *    component type is obtained by calling
    *    getComponentType() on the entries. The type name is then used to find
    *    the processor properties in the config data. If an entry cannot be
    *    found, an exception is thrown.
    *
    * @return The number of components actually deleted.
    *
    * @throws PSCmsException If the processor for any of the types is not
    *    found or cannot be instantiated, or any problems occur while removing
    *    the data.
    */
   public int delete(IPSDbComponent[] comps)
      throws PSCmsException;


   /**
    * Convenience method that calls {@link #delete(IPSDbComponent[])} after
    * creating an array and adding the supplied component to it.
    */
   public int delete(IPSDbComponent comp)
      throws PSCmsException;
}
