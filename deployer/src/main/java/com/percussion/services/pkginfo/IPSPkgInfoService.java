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
package com.percussion.services.pkginfo;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSDuplicateNameException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pkginfo.data.PSPkgDependency;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgElementDependency;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * This is the primary interface for the Package Information Service. A Package
 * is a collection of design objects that make up an installable, configurable
 * "solution" such as a site, blog, rss feed,etc.
 * <p>
 * This service supports 3 objects:
 * <ul>
 * <li> Package Information (<code>PSPkgInfo</code>). There is one PkgInfo
 * object for each Solution created or installed on a server. </li>
 * <li> Package Element (<code>PSPkgElement</code>). Each design object
 * installed with a Package is tracked by a PkgInfo Element attached to the
 * PkgInfo object. </li>
 * <li> Package Element Dependency (<code>PSPkgElementDependency</code>).
 * Any external dependency of a Package is tracked by attaching a Package
 * Element Dependency object to the package (whether the dependency is present
 * on the server or not) </li>
 * </ul>
 * <p>
 * Methods exist for each object, where the <code>XXX</code> represents the
 * different object names. There may be situations where certain of these
 * methods are not applicable, but that is expected to be rare.
 * </ul>
 * <li><code>createXXX</code></li>
 * <li><code>copyXXX</code></li>
 * <li><code>saveXXX</code></li>
 * <li><code>deleteXXX</code></li>
 * <li><code>findXXX</code></li>
 * <li><code>loadXXX</code></li>
 * <li><code>loadXXXModifiable</code></li>
 * </ul>
 * <p>
 * <code>createXXX</code> methods should always be used to create new objects,
 * they should not be created directly, even if the type is known. Likewise for
 * <code>copyXXX</code>, <code>saveXXX</code>, <code>deleteXXX</code>.
 * <p>
 * <p>
 * All <code>loadXXX</code> and <code>findXXX</code> methods work with
 * cached objects. Cached objects are shared and therefore must not be modified
 * by the caller. There is no enforcement of this rule. If an object needs to be
 * modified, the <code>loadXXXModifiable</code> method must be called instead.
 * This method is guaranteed to load the object from persistent storage each
 * time it is called.
 * <p>
 * There are 2 distinctions between the <code>loadXXX</code> and
 * <code>findXXX</code> methods. The first is that <code>loadXXX</code> will
 * throw an exception if valid objects can't be returned for each supplied key;
 * <code>findXXX</code> will return nothing or <code>null</code>, depending
 * on what was supplied. If keys were supplied, <code>null</code> will be
 * returned for each key that doesn't have a matching object. If filters are
 * supplied, only the matching objects are returned. The 2nd distinction is that
 * the <code>loadXXX</code> methods only take keys as arguments, never
 * filters. Here, 'key' means some property value meant to match exactly 1
 * object, e.g. a GUID or a name.
 * <p>
 * <p>
 * The Package (PkgInfo) object has a parent-child relationship with the its
 * Configuration, Dependency, and Element objects. The Package object supports
 * both a list of IDs and a list of the actual object. Each method defines
 * whether the children will be realized, but in general, read/only objects have
 * realized children and objects loaded for modification don't have realized
 * children (todo: grok this and implement).
 * <p>
 * Generally, service methods don't throw checked exceptions. However, they
 * should document the unchecked exceptions that they know they throw.
 * 
 */
public interface IPSPkgInfoService
{
   // ---------------------------------------------------------------------------
   // Package Information object: PSPkgInfo
   // ---------------------------------------------------------------------------
   /**
    * Create a new instance of this Package Information object:
    * <code>PSPkgInfo</code>.
    * <p>
    * The object is not persisted, the {@link #savePkgInfo(PSPkgInfo)} method
    * must be called. The object will be assigned its unique identifier (GUID.)
    * 
    * @param name The internal name of the newly created object. May not be
    * <code>null</code> or empty string.
    * 
    * @return The newly created object, never <code>null</code>.
    */
   public PSPkgInfo createPkgInfo(String name);

   /**
    * Create a new instance of this Package Information object:
    * <code>PSPkgInfo</code> and copy the content from the last entry if
    * exists.
    * <p>
    * The object is not persisted, the {@link #savePkgInfo(PSPkgInfo)} method
    * must be called. The object will be assigned its unique identifier (GUID.)
    * 
    * @param name The internal name of the newly created object. May not be
    * <code>null</code> or empty string.
    * 
    * @return The newly created object with the data from the last entry if
    * exists, never <code>null</code>.
    */
   public PSPkgInfo createPkgInfoCopy(String name);

   /**
    * Save the supplied object to persistent storage. Generally, the object must
    * have been created using {@link #loadPkgInfoModifiable(IPSGuid)} or
    * {@link #createPkgInfo(String)}. If the object is found in the cache, an
    * exception is thrown.
    * <p>
    * This method saves a "shallow copy", that is, only the properties of the
    * PkgInfo object are persisted. Saving of child objects (PSPkgElement,
    * PSPkgElementDependency) must be done by calls to the respective
    * <code>saveXXX</code> methods for those objects.
    * 
    * @param obj The PSPkgInfo object to save. Never <code>null</code>.
    * 
    * @throws IllegalStateException If the object is found in the cache, an
    * exception is thrown. That is, if the supplied object was obtained from the
    * <code>loadPkgInfo</code> or <code>findPkgInfo</code> methods, it may
    * be cached.
    * 
    * @throws PSDuplicateNameException If the name is not unique among all
    * objects of this type.
    */
   public void savePkgInfo(PSPkgInfo obj);

   /**
    * Permanently remove the given package's  Elements and Dependency entries 
    * info from persistent storage (and caches). If the object does not exist, 
    * then this call has no effect. 
    *  e.g. this will delete all its the package's child
    * objects, {@link PSPkgElement}, {@link PSPkgElementDependency} and
    * {@link PSPkgDependency} if there is any.
    * 
    * @param id The ID of the to be deleted package info, never
    * <code>null</code>.
    */
   public void deletePkgInfoChildren(IPSGuid id);

   /**
    * Deletes package Elements and Dependencies by name. 
    * This is a convenient method, the same as
    * {@link #deletePkgInfoChildren(IPSGuid)}, except it is deleted by name.
    * 
    * @param name The name of the object to permanently remove from persistent
    * storage, case-insensitive. Never <code>null</code> or empty.
    */
   public void deletePkgInfoChildren(String name);
   
   /**
    * Permanently remove the given package info from persistent storage (and
    * caches). If the object does not exist, then this call has no effect. Note,
    * this is a cascading delete, e.g. it will also delete all its child
    * objects, {@link PSPkgElement}, {@link PSPkgElementDependency} and
    * {@link PSPkgDependency} if there is any.
    * 
    * @param id The ID of the to be deleted package info, never
    * <code>null</code>.
    */
   public void deletePkgInfo(IPSGuid id);

   /**
    * Deletes a package info by name. This is a convenient method, the same as
    * {@link #deletePkgInfo(IPSGuid)}, except it is deleted by name.
    * 
    * @param name The name of the object to permanently remove from persistent
    * storage, case-insensitive. Never <code>null</code> or empty.
    */
   public void deletePkgInfo(String name);

   /**
    * Find the object whose name matches the supplied name, case-insensitive.
    * 
    * @param name May be <code>null</code> or empty.
    * 
    * @return The object that matches the name, case-insensitive, or
    * <code>null</code> if a match is not found.
    */
   public PSPkgInfo findPkgInfo(String name);

   /**
    * Find all PSPkgInfo objects currently on the system.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public List<PSPkgInfo> findAllPkgInfos();

   /**
    * Method that loads the PSPkgInfo object with the supplied id .
    * 
    * @param id The unique identifier, may not be <code>null</code>.
    * 
    * @return the object matching the requested id, never <code>null</code>.
    * 
    * @throws PSNotFoundException If a matching object is not found. The message
    * will contain the invalid id.
    */
   public PSPkgInfo loadPkgInfo(IPSGuid id);

   /**
    * This method is used to load a PSPkgInfo object that needs to be modified.
    * Children are not realized, but IDs are present.
    * 
    * @param id Never <code>null</code>.
    * 
    * @return The matching object, never <code>null</code>.
    * 
    * @throws PSNotFoundException If a matching object is not found. The message
    * will contain the invalid id.
    */
   public PSPkgInfo loadPkgInfoModifiable(IPSGuid id);

   // ---------------------------------------------------------------------------
   // Package Element object: PSPkgElement
   // ---------------------------------------------------------------------------

   /**
    * Create a new instance of this Package Element object. The object is not
    * persisted, the {@link #savePkgElement(PSPkgElement)} method must be
    * called. The object will be assigned its unique identifier (GUID.)
    * 
    * @param parentId The GUID of the parent PkgInfo object. May not be
    * <code>null</code>.
    * 
    * @return The newly created object, never <code>null</code>.
    */
   public PSPkgElement createPkgElement(IPSGuid parentId);

   /**
    * Save the supplied object to persistent storage. Generally, the object must
    * have been created using {@link #loadPkgElementModifiable(IPSGuid)} or
    * {@link #createPkgElement(IPSGuid)}. If the object is found in the cache,
    * an exception is thrown.
    * 
    * @param obj Never <code>null</code>. The PSPkgElement object to save.
    * 
    * @throws IllegalStateException If the object is found in the cache, an
    * exception is thrown. That is, if the supplied object was obtained from the
    * <code>loadPkgElement</code> or <code>findPkgElement</code> methods, it
    * may be cached.
    * 
    * @throws PSDuplicateNameException If the name is not unique among all
    * objects of this type.
    */
   public void savePkgElement(PSPkgElement obj);

   /**
    * Permanently remove the designated object from persistent storage (and
    * caches). If the object does not exist, then this call has no effect.
    * 
    * @param id The unique identifier, never <code>null</code>.
    */
   public void deletePkgElement(IPSGuid id);

   /**
    * Returns the list of Guids of Package Element objects that correspond to
    * the given PkgInfo object. The returned list can be used as input to the
    * <code>findPkgElements/loadPkgElements</code> methods
    * 
    * @param parentPkgInfo The GUID of the parent Package Info object(PSPkgInfo)
    * that owns the Package Element objects. Never <code>null<code>.
    * 
    * @return A list of Guids for the Package Element objects. This list may
    * be used as input into the <code>load/find</code> methods. 
    * Never <code>null<code>, may be empty.
    */
   public List<IPSGuid> findPkgElementGuids(IPSGuid parentPkgInfo);

   /**
    * Find all children of the supplied package. For installed packages, this
    * includes every object that was installed as part of the package. For
    * uninstalled packages, this includes only those objects that were
    * originally installed, but then could not be uninstalled for some reason.
    * 
    * @param pkgId The id of the parent package. Never <code>null</code>.
    * 
    * @return All objects that are considered part of the supplied package.
    * Never <code>null</code>, may be empty.
    */
   public List<PSPkgElement> findPkgElements(IPSGuid pkgId);

   /**
    * Find an individual package object given its id.
    * 
    * @param id The unique identifier, may not be <code>null</code>.
    * 
    * @return the object matching the requested id, may be <code>null</code>.
    */
   public PSPkgElement findPkgElement(IPSGuid id);

   /**
    * Find the design object that is in an installed package that matches the
    * supplied GUID. If there is an object matching this GUID in an uninstalled
    * package, it is not considered.
    * 
    * @param objId The object type identifier, may not be <code>null</code>.
    * 
    * @return The object whose object GUID matches the supplied GUID. May be
    * <code>null</code>.
    */
   public PSPkgElement findPkgElementByObject(IPSGuid objId);

   /**
    * Load all PSPkgElement objects whose GUIDS are supplied.
    * 
    * @param ids The list of PSPkgElement GUIDs to match . Never
    * <code>null</code>. No <code>null</code> entries allowed.
    * 
    * @return All objects whose GUID matches list of PSPkgElement GUIDs. A
    * <code>List</code> is returned whose length is equal to the number of
    * supplied GUIDs. Each entry is the object matching the GUID. Never
    * <code>null</code>. Never empty.
    * 
    * @throws PSNotFoundException If a matching object is not found. The message
    * will contain the invalid id.
    */
   public List<PSPkgElement> loadPkgElements(List<IPSGuid> ids);

   /**
    * Convenience method that puts the supplied id in a list and calls
    * {@link #loadPkgElements(List)}.
    * 
    * @param id The unique identifier, may not be <code>null</code>.
    * 
    * @return the object matching the requested id, may be <code>null</code>.
    * 
    * @throws PSNotFoundException If a matching object is not found. The message
    * will contain the invalid id.
    */
   public PSPkgElement loadPkgElement(IPSGuid id);

   /**
    * This method is used to load a PSPkgElement object that needs to be
    * modified. Children are not realized, but IDs are present.
    * 
    * @param id Never <code>null</code>.
    * 
    * @return The matching object, never <code>null</code>.
    * 
    * @throws PSNotFoundException If a matching object is not found. The message
    * will contain the invalid id.
    */
   public PSPkgElement loadPkgElementModifiable(IPSGuid id);

   // *******Package dependency methods
   /**
    * Creates a new instance of {@link PSPkgDependency} object. The object is
    * not persisted, the {@link #savePkgDependency(PSPkgDependency)} method must
    * be called.
    * 
    * @return Object of the newly created {@link PSPkgDependency}, never
    * <code>null</code>.
    */
   public PSPkgDependency createPkgDependency();

   /**
    * Saves the supplied package dependency object. Generally, the object must
    * have been created using {@link #createPkgDependency()} or loaded by
    * {@link #loadPkgDependencies(IPSGuid)}.
    * 
    * @param pkgDependency The PSPkgDependency that needs to be saved, must not
    * be <code>null</code>.
    */
   public void savePkgDependency(PSPkgDependency pkgDependency);

   /**
    * Loads the package dependency objects for the supplied guid of
    * {@link PSPkgInfo} considering it as owner guid if the depType is
    * <code>true</code> otherwise dependent.
    * 
    * @param guid The guid of the type {@link PSTypeEnum#PACKAGE_INFO}, must
    * not be <code>null</code>.
    * @param depType A boolean flag to indicate whether to consider the supplied
    * guid as owner or dependent. <code>true</code> indicates owner and
    * <code>false</code> indicates dependent.
    * 
    * 
    * @return list of {@link PSPkgDependency} objects, never <code>null</code>,
    * may be empty.
    */
   public List<PSPkgDependency> loadPkgDependencies(IPSGuid guid,
         boolean depType);

   /**
    * Loads the modifiable package dependency objects for the supplied guid of
    * type {@link PSTypeEnum#PACKAGE_INFO} considering it as owner guid if the
    * depType is <code>true</code> otherwise dependent.
    * 
    * @param guid The guid of the type {@link PSTypeEnum#PACKAGE_INFO}, must
    * not be <code>null</code>.
    * @param depType A boolean flag to indicate whether to consider the supplied
    * guid as owner or dependent. <code>true</code> indicates owner and
    * <code>false</code> indicates dependent.
    * @return list of {@link PSPkgDependency} objects, never <code>null</code>,
    * may be empty.
    */
   public List<PSPkgDependency> loadPkgDependenciesModifiable(IPSGuid guid,
         boolean depType);

   /**
    * Finds and returns the owner package info guids of the supplied package
    * info guid.
    * 
    * @param guid guid of the {@link PSPkgInfo} object, must not be
    * <code>null</code>.
    * @return list of {@link PSPkgInfo} object guids that are owners of the
    * supplied guid, may be empty never never <code>null</code>.
    */
   public List<IPSGuid> findOwnerPkgGuids(IPSGuid guid);

   /**
    * Finds and returns the dependent package info guids of the supplied package
    * info guid.
    * 
    * @param guid guid of the {@link PSPkgInfo} object, must not be
    * <code>null</code>.
    * @return list of {@link PSPkgInfo} object guids that are dependents of the
    * supplied guid, may be empty never never <code>null</code>.
    */
   public List<IPSGuid> findDependentPkgGuids(IPSGuid guid);

   /**
    * Permanently removes the PSPkgDependency object corresponding to the
    * supplied id from persistent storage. If the objects do not exist, then
    * this call has no effect.
    * 
    * @param pkgDepId The id of PSPkgDependency object to be deleted.
    */
   public void deletePkgDependency(long pkgDepId);
}
