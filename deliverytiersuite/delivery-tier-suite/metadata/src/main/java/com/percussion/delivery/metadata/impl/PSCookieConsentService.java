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

package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSCookieConsent;
import com.percussion.delivery.metadata.IPSCookieConsentDao;
import com.percussion.delivery.metadata.IPSCookieConsentService;
import com.percussion.delivery.metadata.data.PSCookieConsentQuery;
import com.percussion.delivery.metadata.rdbms.impl.PSDbCookieConsent;
import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Service for creating and retrieving cookie consent entries in the DB.
 * 
 * @author chriswright
 *
 */
public class PSCookieConsentService implements IPSCookieConsentService {

    private static final Logger log = LogManager.getLogger(PSCookieConsentService.class.getName());

    private final IPSCookieConsentDao consentDao;

    @Autowired
    public PSCookieConsentService(IPSCookieConsentDao consentDao) {
        log.debug("Initializing consentDao.");
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
            log.error("Error updating site name in cookie consent service for old site: {} Error: {}" ,
                    oldSiteName, PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }
}
