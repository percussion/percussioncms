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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSCookieConsent;
import com.percussion.delivery.metadata.IPSCookieConsentDao;
import com.percussion.delivery.metadata.IPSCookieConsentService;
import com.percussion.delivery.metadata.data.PSCookieConsentQuery;
import com.percussion.delivery.metadata.rdbms.impl.PSDbCookieConsent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service for creating and retrieving cookie consent entries in the DB.
 * 
 * @author chriswright
 *
 */
public class PSCookieConsentService implements IPSCookieConsentService {

    private static final Logger MS_LOG = LogManager.getLogger(PSCookieConsentService.class.getName());

    private IPSCookieConsentDao consentDao;

    @Autowired
    public PSCookieConsentService(IPSCookieConsentDao consentDao) {
        MS_LOG.debug("Initializing consentDao.");
        this.consentDao = consentDao;
    }

    @Override
    public void save(Collection<PSCookieConsentQuery> consentQueries) {
        Collection<PSDbCookieConsent> consents = convertToDbCookieConsents(consentQueries);
        consentDao.save(consents);
    }

    @Override
    public Collection<IPSCookieConsent> getAllConsentStats() {
        return consentDao.getAllCookieConsentStats();
    }
    
    @Override
    public Collection<IPSCookieConsent> getAllConsentStatsForSite(String siteName)
    {
        return consentDao.getAllCookieStatsForSite(siteName);
    }
    
    @Override
    public void deleteAllCookieConsentEntries() throws Exception {
        consentDao.deleteAll();
    }
    
    @Override
    public void deleteCookieConsentEntriesForSite(String siteName)
            throws Exception {
        consentDao.deleteForSite(siteName);
    }
    
    @Override
    public Map<String, Integer> getAllConsentEntryTotals() throws Exception {
        return consentDao.getTotalsForAllSites();
    }
    
    @Override
    public Map<String, Integer> getCookieConsentEntryTotalsPerSite(String siteName)
            throws Exception {
        return consentDao.getTotalsForSite(siteName);
    }
    
    /**
     * Takes a new PSCookieConsentQuery from JavaScript request and maps 1 or more approved
     * cookie services to unique PSDbCookieConsent objects.  This will allow
     * for creating unique rows in the DB for each service/cookie approved.
     * @param consentQueries - Collection containing PSCookieConsentQuery objects - siteName, services, etc. approved for cookies.
     * @return A collection of unique PSDbCookieConsent objects corresponding to the approved cookies.
     * Never <code>null</code>, may be empty.
     */
    private Collection<PSDbCookieConsent> convertToDbCookieConsents(Collection<PSCookieConsentQuery> consentQueries) {
        Collection<PSDbCookieConsent> consents = new ArrayList<>();
        
        for (PSCookieConsentQuery query : consentQueries) {
            for (String service : query.getServices()) {
                consents.add(new PSDbCookieConsent(query.getSiteName(), service, 
                        query.getConsentDate(), query.getIP(), query.getOptIn()));
            }
        }
        
        return consents;
    }

    @Override
    public void updateOldSiteName(String oldSiteName, String newSiteName) {
        try {
            consentDao.updateOldSiteName(oldSiteName, newSiteName);
        } catch (Exception e) {
            MS_LOG.error("Error updateing site name in cookie consent service for old site: " + oldSiteName, e);
        }
    }
}
