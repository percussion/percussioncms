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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.percussion.delivery.metadata.IPSBlogPostVisit;
import com.percussion.delivery.metadata.IPSBlogPostVisitDao;
import com.percussion.delivery.metadata.IPSBlogPostVisitService;
import com.percussion.delivery.metadata.IPSCookieConsentService;
import com.percussion.delivery.metadata.data.PSBlogPostVisit;
import com.percussion.delivery.metadata.data.PSCookieConsentQuery;
import com.percussion.delivery.metadata.data.PSVisitQuery;
import org.springframework.stereotype.Component;

@Component
public class PSBlogPostVisitService implements IPSBlogPostVisitService, InitializingBean {
	private Map<String, IPSBlogPostVisit> inMemoryVisitMap = new ConcurrentHashMap<>();
	private List<PSCookieConsentQuery> inMemoryCookieConsentMap = new ArrayList<>();
	private ScheduledExecutorService visitExecutor = Executors.newScheduledThreadPool(1);
	private long lastSave;
	private IPSBlogPostVisitDao visitDao;
	private IPSCookieConsentService cookieService;
    private static final Logger log = LogManager.getLogger(PSBlogPostVisitService.class);
    private Integer schedulerInitialDelay = INTIAL_DELAY_SECONDS;
    private Integer schedulerSaveInterval = SAVE_INTERVAL_SECONDS;
    
    @Autowired
    public PSBlogPostVisitService(IPSBlogPostVisitDao visitDao, IPSCookieConsentService cookieService,
            Integer schedulerSaveInterval) {
    	this.visitDao = visitDao;
    	this.cookieService = cookieService;
    	if (schedulerSaveInterval != null) {
    		this.schedulerSaveInterval = schedulerSaveInterval;
    	}
    	log.debug("Save Interval: " + schedulerSaveInterval);
    	log.debug("Initial Delay: " + schedulerInitialDelay);
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		startScheduler();
	}
    
	@Override
    public void startScheduler() {
    	Runnable scheduledTask = new Runnable() {
			@Override
			public void run() {
				lastSave = System.currentTimeMillis();
				saveVisits();
				saveCookieConsentEntries();
			}
		};
		// removed schedulerInitialDelay as a property in properties/beans files as this is now being loaded
		// and started from afterPropertiesSet() which avoids the initial wait time to check if this service
		// is running
		visitExecutor.scheduleAtFixedRate(scheduledTask, schedulerInitialDelay, schedulerSaveInterval, TimeUnit.SECONDS);
    }
    
	private void saveVisits() {
		try {
			if (inMemoryVisitMap.size() < 1) {
				return;
			}
			Collection<IPSBlogPostVisit> visits = new ArrayList<>(inMemoryVisitMap.values());
			inMemoryVisitMap.clear();
			log.debug("Saving visits");
			log.debug("Visits size: " + inMemoryVisitMap.size());
			visitDao.save(visits);
		} catch (Exception e) {
			log.error("Failed save to hit counts", e);
		}
	}
	
    private void saveCookieConsentEntries() {
        try {
            if (inMemoryCookieConsentMap.size() < 1) {
                return;
            }
            
            this.cookieService.save(inMemoryCookieConsentMap);
            inMemoryCookieConsentMap.clear();
        }
        catch (Exception e) {
            log.error("Error saving cookie consent entries.", e);
        }
    }
	
	@Override
	public List<String> getTopVisitedBlogPosts(PSVisitQuery visitQuery) throws Exception {
		TIMEPERIOD tp = TIMEPERIOD.fromName(visitQuery.getTimePeriod());
		if (tp == null) {
			tp = TIMEPERIOD.WEEK;
		}
		return visitDao.getTopVisitedPages(visitQuery.getSectionPath(), tp.getDays(), convertToLimit(visitQuery.getLimit()), visitQuery.getSortOrder());
	}

	@Override
	public void trackBlogPost(String pagePath) {
		IPSBlogPostVisit visit = inMemoryVisitMap.get(pagePath);
		if (visit == null) {
			inMemoryVisitMap.put(pagePath, new PSBlogPostVisit(pagePath, new Date(), BigInteger.ONE));
		} else {
			visit.setHitCount(visit.getHitCount().add(BigInteger.ONE));
		}
	}
	
    @Override
    public void logCookieConsentEntry(PSCookieConsentQuery query) {
        inMemoryCookieConsentMap.add(query);
    }
	
	@Override
	public void delete(Collection<String> pagepaths) {
		visitDao.delete(pagepaths);
	}
	
	public int convertToLimit(String limit) {
		if (StringUtils.isBlank(limit)) {
			return 0;
		}
		limit = limit.toUpperCase().replace("R-", "");
		int res = 5;
		try {
			res = Integer.parseInt(limit);
		} catch (NumberFormatException e) {
			log.warn("Failed to parse the limit parameter, defaulting to 5");
		}
		return res;
	}

	@Override
	public boolean visitSchedulerStatus() {
		// if the difference between current time and last save is NOT greater than the 
		// set save interval time doubled in milliseconds 
		return !( (System.currentTimeMillis() - lastSave) >= (2 * schedulerSaveInterval * 1000) );
	}

    @PreDestroy
    public void beandestroy() {
        log.debug("Calling most-read-blog-posts thread shutdown.");
        visitExecutor.shutdown();
        
        if(visitExecutor != null) {
            try {
                // wait 1 second for closing all threads
                log.debug("calling most-read-blog-posts thread await termination.");
                visitExecutor.awaitTermination(1, TimeUnit.SECONDS);
            } 
            catch (InterruptedException e) {
                log.debug("interrupting most-read-blog-posts thread.");
                Thread.currentThread().interrupt();
            }
            finally {
                if (visitExecutor != null && !visitExecutor.isShutdown()) {
                    log.debug("calling shutdownNow for most-read-blog-posts thread.");
                    visitExecutor.shutdownNow();
                }
            }
        }
    }

    @Override
    public void updatePostsAfterSiteRename(String prevSiteName,
                                           String newSiteName) {
        try {
            visitDao.updatePostsAfterSiteRename(prevSiteName, newSiteName);
        } catch (Exception e) {
            log.error("Error updating blog post visit updates after site rename.", e);
        }
    }
    
}
