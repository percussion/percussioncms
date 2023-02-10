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
package com.percussion.services.sitemgr;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IPSSiteManager  {

    @Transactional(noRollbackFor = PSNotFoundException.class)
    IPSSite createSite();

    /*
     * @see com.percussion.services.sitemgr.IPSSiteManager#loadSitesModifiable()
     */
    List<IPSSite> loadSitesModifiable();

    IPSSite loadSiteModifiable(IPSGuid siteid) throws PSNotFoundException;

    IPSSite loadSiteModifiable(String siteName) throws PSNotFoundException;

    IPSSite findSiteFromDatabase(IPSGuid siteid);

    IPSSite loadUnmodifiableSite(IPSGuid siteid)
            throws PSNotFoundException;

    IPSSite findSite(IPSGuid siteid);

    IPSSite loadSite(IPSGuid siteid) throws PSNotFoundException;

    @SuppressWarnings("unchecked")
    List<IPSSite> findAllSites();

    @SuppressWarnings("unchecked")
    Map<IPSGuid, String> getAllSiteIdNames();

    @SuppressWarnings("unchecked")
    IPSSite findSite(String sitename);

    IPSSite loadSite(String sitename) throws PSNotFoundException;

    @Deprecated
    IPSSite findSiteByName(String sitename) throws PSSiteManagerException;

    @Transactional(noRollbackFor = PSNotFoundException.class)
    void saveSite(IPSSite site);

    @Transactional(noRollbackFor = PSNotFoundException.class)
    void deleteSite(IPSSite site);

    @Transactional(noRollbackFor = PSNotFoundException.class)
    IPSLocationScheme createScheme();

    /*
     * (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSSiteManager#loadScheme(int)
     */
    IPSLocationScheme loadScheme(IPSGuid schemeId)
            throws PSNotFoundException;

    IPSLocationScheme loadSchemeModifiable(IPSGuid schemeId)
            throws PSNotFoundException;

    /*
     * //see base class method for details
     */
    IPSLocationScheme loadScheme(int schemeId)
            throws PSNotFoundException;

    /*
     * //see base class method for details
     */
    @SuppressWarnings("unchecked")
    List<IPSLocationScheme> findSchemeByAssemblyInfo(
            IPSAssemblyTemplate template, IPSPublishingContext context,
            IPSGuid contenttypeid);

    /*
     * //see base class method for details
     */
    List<IPSLocationScheme> findSchemeByAssemblyInfo(IPSGuid templateid,
                                                     IPSPublishingContext context, IPSGuid contenttypeid);

    /*
     * //see base class method for details
     */
    @SuppressWarnings("unchecked")
    List<IPSLocationScheme> findSchemeByAssemblyInfo(IPSGuid templateid,
                                                     IPSGuid contextid, IPSGuid contenttypeid);

    @Transactional(noRollbackFor = PSNotFoundException.class)
    void saveScheme(IPSLocationScheme scheme);

    @Transactional(noRollbackFor = PSNotFoundException.class)
    void deleteScheme(IPSLocationScheme scheme);

    //see interface
    IPSPublishingContext loadContext(int contextid)
            throws PSNotFoundException;

    /*
     * @see com.percussion.services.sitemgr.IPSSiteManager#loadContext(int)
     */
    IPSPublishingContext loadContext(IPSGuid contextid)
            throws PSNotFoundException;

    IPSPublishingContext loadContextModifiable(IPSGuid contextid)
            throws PSNotFoundException;

    @SuppressWarnings("unchecked")
    IPSPublishingContext loadContext(String contextname)
            throws PSNotFoundException;

    @SuppressWarnings("unchecked")
    IPSPublishingContext findContextByName(String contextname)
            throws PSSiteManagerException;

    PSTypeEnum[] getTypes();

    @SuppressWarnings("unchecked")
    List<IPSCatalogSummary> getSummaries(PSTypeEnum type) throws PSNotFoundException, PSCatalogException;

    void loadByType(PSTypeEnum type, String item)
            throws PSCatalogException;

    @Transactional(noRollbackFor = PSNotFoundException.class)
    String saveByType(IPSGuid id) throws PSCatalogException;

    String getPublishPath(IPSGuid siteId, IPSGuid folderId)
            throws PSSiteManagerException, PSNotFoundException;

    IPSGuid getSiteFolderId(IPSGuid siteId, IPSGuid contentId)
            throws PSSiteManagerException, PSNotFoundException;

    /*
     * (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSSiteManager#getItemSites(com.percussion.utils.guid.IPSGuid)
     */
    @SuppressWarnings("unchecked")
    List<IPSSite> getItemSites(IPSGuid contentId);

    // implements method from IPSSiteManager interface
    boolean isContentTypePublishableToSite(IPSGuid contentTypeId,
                                           IPSGuid siteId) throws PSSiteManagerException, PSNotFoundException;

    IPSCacheAccess getCache();

    void setCache(IPSCacheAccess cache);

    IPSNotificationService getNotifications();

    void setNotifications(IPSNotificationService notifications);

    @SuppressWarnings("unchecked")
    List<IPSPublishingContext> findAllContexts() throws PSNotFoundException;

    @SuppressWarnings("unchecked")
    List<IPSLocationScheme> findAllSchemes();

    @SuppressWarnings("unchecked")
    List<String> findDistinctSiteVariableNames();

    @Transactional(noRollbackFor = PSNotFoundException.class)
    void deleteContext(IPSPublishingContext context);

    @SuppressWarnings("unchecked")
    List<IPSLocationScheme> findSchemesByContextId(IPSGuid contextid);

    @Transactional(noRollbackFor = PSNotFoundException.class)
    void saveContext(IPSPublishingContext context);

    @Transactional(noRollbackFor = PSNotFoundException.class)
    IPSPublishingContext createContext();

    @SuppressWarnings("unchecked")
    Map<Integer, String> getContextNameMap();

    Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> findSiteTemplatesAssociations();

    /**
     * Key for location scheme map
     */
    public static class LocationSchemeKey implements Serializable {
        /**
         * Serial id identifies versions of serialized data
         */
        private static final long serialVersionUID = 1L;

        /**
         * Holds the template id, initialized in the ctor
         */
        private IPSGuid mi_templateid;

        /**
         * Holds the context, initialized in the ctor
         */
        private IPSGuid mi_contextid;

        /**
         * Holds the content type id, initialized in the ctor
         */
        private IPSGuid mi_contenttypeid;

        /**
         * Ctor
         *
         * @param tid       template id, assumed never <code>null</code>
         * @param contextid context id, assumed never <code>null</code>
         * @param ctid      content type id, assumed never <code>null</code>
         */
        public LocationSchemeKey(IPSGuid tid, IPSGuid contextid,
                                 IPSGuid ctid) {
            mi_templateid = tid;
            mi_contextid = contextid;
            mi_contenttypeid = ctid;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LocationSchemeKey) {
                LocationSchemeKey lsk = (LocationSchemeKey) obj;
                return lsk.mi_contenttypeid.equals(mi_contenttypeid)
                        && lsk.mi_templateid.equals(mi_templateid)
                        && lsk.mi_contextid.equals(mi_contextid);
            }
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return mi_contenttypeid.hashCode() + mi_templateid.hashCode()
                    + mi_contextid.hashCode();
        }
    }
}
