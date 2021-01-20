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
package com.percussion.services.sitemgr;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.IPSCataloger;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Map;

/**
 * Initial basic site manager to allow the loading of site and site specific
 * information. This will be filled out in a later release, but here is a
 * read-only interface.
 * <p>
 * <b>Important</b> to use this manager and dereference related site objects,
 * you must be in the same <code>Session</code>, so this should be called
 * from within another manager.
 * <p>
 * At this point the site manager does not offer cataloging services.
 * 
 * @author dougrand
 */
public interface IPSSiteManager extends IPSCataloger
{
   /**
    * Create a new site object and initialize for the database
    * 
    * @return the new site object, never <code>null</code>
    */
   IPSSite createSite();

   /**
    * Load a site object from the cache, it may not be modified and saved
    * through  {@link #saveSite(IPSSite)}.
    * 
    * @param siteid the site ID, never <code>null</code>
    * 
    * @return the site. It may be <code>null</code> if the site does not exist.
    */
   IPSSite findSite(IPSGuid siteid);

   /**
    * Load a site object from the cache, it may not be modified and saved
    * through  {@link #saveSite(IPSSite)}.
    * 
    * @param siteName the site name, it may be <code>null</code> or empty.
    * 
    * @return the site. It may be <code>null</code> if the site does not exist.
    */
   IPSSite findSite(String siteName);
   
   /**
    * Load a site object from the cache, it may not be modified and saved
    * through  {@link #saveSite(IPSSite)}.
    * 
    * @param siteid the site ID, never <code>null</code>
    * 
    * @return the site, never <code>null</code>
    * 
    * @throws PSNotFoundException if the site does not exist
    */
   IPSSite loadSite(IPSGuid siteid) throws PSNotFoundException;

   /**
    * Load a site object by the site name. The site is stored from cache
    * and cannot be changed or saved through {@link #saveSite(IPSSite)}.
    * 
    * @param sitename the name of the site, never <code>null</code> or empty
    * 
    * @return the site, never <code>null</code>
    * 
    * @throws PSNotFoundException if the site does not exist
    */
   IPSSite loadSite(String sitename) throws PSNotFoundException;

   /**
    * Loads all available sites. The loaded sites may be modified and saved
    * through {@link #saveSite(IPSSite)}.
    * 
    * @return a list of Site objects, probably not empty, never
    * <code>null</code>. The returned objects can be modified.
    * 
    * @throws PSNotFoundException if failed to load a site.
    */
   List<IPSSite> loadSitesModifiable() throws PSNotFoundException;
   
   /**
    * Loads the specified site. The loaded site can be modified and saved
    * through {@link #saveSite(IPSSite)}
    *  
    * @param siteid the ID of the site, not <code>null</code>.
    * 
    * @return the loaded site, never <code>null</code>.
    * 
    * @throws PSNotFoundException if cannot load the specified site.
    */
   IPSSite loadSiteModifiable(IPSGuid siteid) throws PSNotFoundException;

   /**
    * Loads the specified site. The loaded site can be modified and saved
    * through {@link #saveSite(IPSSite)}
    *  
    * @param siteName the name of the site, not <code>null</code>.
    * 
    * @return the loaded site, never <code>null</code>.
    * 
    * @throws PSNotFoundException if cannot load the specified site.
    */
   IPSSite loadSiteModifiable(String siteName) throws PSNotFoundException;
   
   /**
    * Load a site object from the database
    * 
    * @param sitename the name of the site, never <code>null</code> or empty
    * @return the site, never <code>null</code>
    * @throws PSSiteManagerException if the site does not exist
    * @deprecated use {@link #loadSite(String)} instead.
    */
   IPSSite findSiteByName(String sitename) throws PSSiteManagerException;

   /**
    * Find and load all available sites. The sites are stored in cache, they
    * may not be modified and saved through {@link #saveSite(IPSSite)}.
    * 
    * @return a list of Site objects, probably not empty, never
    *         <code>null</code>
    */
   List<IPSSite> findAllSites();

   /**
    * Save or update the site in the database
    * 
    * @param site the site, never <code>null</code>
    */
   void saveSite(IPSSite site);

   /**
    * Delete the site from the database
    * 
    * @param site the site, never <code>null</code>
    */
   void deleteSite(IPSSite site);

   /**
    * Create and initialize a new location scheme. It is not persisted, you must
    * call {@link #saveScheme(IPSLocationScheme)} to persist it to permanent
    * storage.
    * 
    * @return a new scheme, never <code>null</code>
    */
   IPSLocationScheme createScheme();

   /**
    * Load a cached location scheme by ID if it exists in cached; otherwise
    * load the location scheme from the repository, save it into cache, then
    * return the cached object. The return object cannot be saved by calling
    * {@link #saveScheme(IPSLocationScheme)}.
    * 
    * @param schemeId the scheme's ID, never <code>null</code>.
    * 
    * @return the cached location scheme, never <code>null</code>
    * 
    * @throws PSNotFoundException if the scheme is not found
    */
   IPSLocationScheme loadScheme(IPSGuid schemeId) throws PSNotFoundException;

   /**
    * Load a location scheme by ID from the repository.
    * 
    * @param schemeId the location scheme ID, never <code>null</code>.
    * 
    * @return the loaded location scheme, never <code>null</code>. This
    *    object can be saved by {@link #saveScheme(IPSLocationScheme)}.
    * 
    * @throws PSNotFoundException if the scheme is not found
    */
   IPSLocationScheme loadSchemeModifiable(IPSGuid schemeId)
      throws PSNotFoundException;
   
   /**
    * Load a location scheme by id from the database
    * 
    * @param scheme the scheme's id
    * @return a location scheme, never <code>null</code>
    * @throws PSNotFoundException if the scheme is not found
    * 
    * @deprecated use {@link #loadScheme(IPSGuid)} instead.
    */
   IPSLocationScheme loadScheme(int scheme) throws PSNotFoundException;

   /**
    * Find the scheme
    * 
    * @param template the template, never <code>null</code>
    * @param context the context, never <code>null</code>
    * @param contenttypeid the content type's id, never <code>null</code>
    * @return zero or more matching location scheme objects
    * @throws PSSiteManagerException if there is a problem
    * 
    * @deprecated use
    * {@link #findSchemeByAssemblyInfo(IPSGuid, IPSGuid, IPSGuid)} instead
    */
   List<IPSLocationScheme> findSchemeByAssemblyInfo(
         IPSAssemblyTemplate template, IPSPublishingContext context,
         IPSGuid contenttypeid) throws PSSiteManagerException;
   
   /**
    * Find the scheme
    * 
    * @param templateid the templateid, never <code>null</code>
    * @param context the context, never <code>null</code>
    * @param contenttypeid the content type's id, never <code>null</code>
    * @return zero or more matching location scheme objects
    * @throws PSSiteManagerException if there is a problem
    * 
    * @deprecated use
    * {@link #findSchemeByAssemblyInfo(IPSGuid, IPSGuid, IPSGuid)} instead
    */
   List<IPSLocationScheme> findSchemeByAssemblyInfo(
         IPSGuid templateid, IPSPublishingContext context,
         IPSGuid contenttypeid) throws PSSiteManagerException;

   /**
    * Find the scheme
    * 
    * @param templateid the template ID, never <code>null</code>
    * @param contextid the context ID, never <code>null</code>
    * @param contenttypeid the content type's ID, never <code>null</code>
    * @return zero or more matching location scheme objects
    * @throws PSSiteManagerException if there is a problem
    */
   List<IPSLocationScheme> findSchemeByAssemblyInfo(
         IPSGuid templateid, IPSGuid contextid,
         IPSGuid contenttypeid) throws PSSiteManagerException;

   /**
    * Save or update the location scheme to the database
    * 
    * @param scheme the scheme, never <code>null</code>
    */
   void saveScheme(IPSLocationScheme scheme);

   /**
    * Delete the location scheme from the database
    * 
    * @param scheme the scheme, never <code>null</code>
    */
   void deleteScheme(IPSLocationScheme scheme);

   /**
    * Convenience method that creates a GUID and calls
    * {@link #loadContext(IPSGuid)}.
    * 
    * @deprecated Use {@link #loadContext(IPSGuid)}.
    */
   IPSPublishingContext loadContext(int contextid)
         throws PSNotFoundException;

   /**
    * Load a publishing context by ID. It may be loaded from cache. So the
    * returned object should be treated immutable and it should not be saved
    * by calling {@link #saveContext(IPSPublishingContext)}.
    * 
    * @param contextid the context's id
    * @return a publishing context, never <code>null</code>
    * @throws PSNotFoundException if the context is not found
    */
   IPSPublishingContext loadContext(IPSGuid contextid)
         throws PSNotFoundException;

   /**
    * Load a publishing Context by ID from the repository.
    * @param contextid
    * @return
    * @throws PSNotFoundException
    */
   IPSPublishingContext loadContextModifiable(IPSGuid contextid)
      throws PSNotFoundException;
   
   /**
    * Find a publishing context by the specified name
    * 
    * @param contextname the name of the context, never <code>null</code> or
    *    empty
    * @return the desired context, never <code>null</code>
    * @throws PSNotFoundException if there is no context with the given
    * name
    */
   IPSPublishingContext loadContext(String contextname)
      throws PSNotFoundException;

   /**
    * Find a publishing context by name from the database
    * 
    * @param contextname the name of the context, never <code>null</code> or
    *           empty
    * @return the desired context, never <code>null</code>
    * @throws PSSiteManagerException if there is no context with the given
    * name
    * @deprecated use {@link #loadContext(String)} instead.
    */
   IPSPublishingContext findContextByName(String contextname)
         throws PSSiteManagerException;

   /**
    * Gets a publishing path for the specified site and folder. This path is
    * constructed with the publishing name of the folders, which is either the
    * property of 'sys_pubFilename' of the folder (if exists) or the name of the
    * folder (if 'sys_pubFilename' property does not exist).
    * 
    * @param siteId the id of the specified site, never <code>null</code>.
    * @param folderId the id of the specified folder, never <code>null</code>.
    *           It must be a folder under the root folder of the specified site.
    * 
    * @return the publishing path. It is relative to the root folder of the 
    *    specified site, and is in the form of "/a/b/c/", where 'a' is the 
    *    immediate child folder of the site root folder, 'b' the child folder 
    *    of 'a', and 'c' is the publishing name of the specified folder. It is
    *    "/" if the specified folder is the root folder of the specified site.
    *    It is <code>null</code> if cannot find the root folder of the site 
    *    or the root folder of the site is not defined.
    *    
    * @throws PSSiteManagerException if an error occurs while looking up the
    *    path.
    */
   String getPublishPath(IPSGuid siteId, IPSGuid folderId) 
      throws PSSiteManagerException;

   /**
    * Find and return the folder id that the given item is contained in for a
    * given site. If the item is in more than one folder, this returns the first
    * found.
    * 
    * @param siteId the site guid, never <code>null</code>
    * @param contentId the content guid, never <code>null</code>
    * @return the id of the parent folder in the given site or <code>null</code>
    *         if no match can be found
    * @throws PSSiteManagerException
    */
   public IPSGuid getSiteFolderId(IPSGuid siteId, IPSGuid contentId)
         throws PSSiteManagerException;

   /**
    * Find and return all the sites the item exists in. The sites are ordered by
    * the name of the sites it exists in.
    * 
    * @param contentId the content GUID, never <code>null</code>
    * @return the sites the item exists in never <code>null</code> may be
    *         empty, if the item does not exist in any site.
    */
   public List<IPSSite> getItemSites(IPSGuid contentId);
   
   /**
    * Check if the content type with supplied GUID is publishable to the
    * specified site. This is done by checking if at least one template for the
    * content type is registered to publish for the supplied site.
    * 
    * @param contentTypeId GUID of the content item to check, must not be
    *           <code>null</code>.
    * @param siteId GUID of the site to check, may be <code>null</code> in
    *           which case the check will be done against all sites in the
    *           system and returns <code>true</code> even if any of the sites
    *           can publish any of the item's templates.
    * @return <code>true</code> if the content type is publishable to the
    *         supplied site or any site if supplied site is <code>null</code>
    * @throws PSSiteManagerException
    */
   public boolean isContentTypePublishableToSite(IPSGuid contentTypeId,
         IPSGuid siteId) throws PSSiteManagerException;

   /**
    * Load a site instance using an in-memory cache if possible. The returned
    * object will be accessible from multiple threads, and so should not be
    * modified or used with the {@link #saveSite(IPSSite)} method. See
    * {@link #loadSite(IPSGuid)} for more information.
    * 
    * @param siteid the id of the object to retrieve, never <code>null</code>
    * @return the site, never <code>null</code>
    * @throws PSNotFoundException if cannot find the site.
    * 
    * @deprecated use {@link #loadSite(IPSGuid)} instead.
    */
   public IPSSite loadUnmodifiableSite(IPSGuid siteid)
         throws PSNotFoundException;

   /**
    * Find all the available publishing contexts.
    * 
    * @return the available publishing contexts, never <code>null</code> and
    * never empty on a normal system. A list is returned for the convenience of
    * the caller, no order is guaranteed.
    */
   List<IPSPublishingContext> findAllContexts();
   
   /**
    * Find all the available location schemes.
    * 
    * @return the available location schemes, never <code>null</code>.  A list
    * is returned for the convenience of the caller, no order is guaranteed.
    */
   List<IPSLocationScheme> findAllSchemes();
   
   /**
    * Find all the unique names for site variables. Used to aid in the user 
    * interface.
    * 
    * @return the in use site variable names, never <code>null</code>.
    */
   List<String> findDistinctSiteVariableNames();
   
   /**
    * Delete the given publishing context. Has no effect if the context
    * does not exist in the database.
    * 
    * @param context the publishing context to delete, never <code>null</code>.
    */
   void deleteContext(IPSPublishingContext context);
   
   /**
    * Save the given publishing context if the context has never been saved,
    * otherwise updates the context in the database.
    * 
    * @param context the publishing context to save, never <code>null</code>.
    */
   void saveContext(IPSPublishingContext context);
   
   /**
    * Find all schemes associated with the given context id.
    * 
    * @param contextid the context id
    * @return the associated schemes, may be empty if the context id doesn't 
    * exist or if there are no associated schemes.
    */
   List<IPSLocationScheme> findSchemesByContextId(IPSGuid contextid);
   
   /**
    * Create and initialize a new context. It is not persisted, you must call
    * {@link #saveContext(IPSPublishingContext)} to persist it to permanent
    * storage.
    * 
    * @return a new context, never <code>null</code>.
    */
   IPSPublishingContext createContext();

   /**
    * @return a map associating the integer values for a context and the 
    * context name, never <code>null</code>.
    */
   Map<Integer, String> getContextNameMap();
   
   
   Map<IPSGuid, String> getAllSiteIdNames();

}
