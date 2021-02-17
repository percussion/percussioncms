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

package com.percussion.share.service.impl;

import com.percussion.monitor.process.PSThumbnailProcessMonitor;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.thumbnail.PSScreenCapture;
import com.percussion.thumbnail.PSThumbnailImageUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.service.impl.PSSiteConfigUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class PSThumbnailRunner implements Runnable {

	private static AtomicInteger activeWorkers = new AtomicInteger(0);

	private IPSSiteTemplateService siteTemplateService;

	private IPSTemplateService templateService;

	private IPSPageService pageService;

	private boolean waitForCompletion;

	private Map<String, Object> requestInfoMap;

	private Set<Map.Entry<String, String>> sessionParameterMap = null;

	private static  AtomicInteger activeWorkerLimit = new AtomicInteger(1);
	
	private static  AtomicBoolean shutdownFlag = new AtomicBoolean();

	PSRequestContext requestContext = null;

	// Nota Bene - LinkedHashMap is FIFO:
	// http://docs.oracle.com/javase/6/docs/api/java/util/LinkedHashMap.html
	private static ConcurrentHashMap <String, Function> inProcess = new ConcurrentHashMap <>();

	private static final String PAGE_STRING = "-page.jpg";

	private static final String TEMPLATE_STRING = "-template.jpg";

	private static final String TPL_IMAGES_DIR = "rx_resources/images/TemplateImages";

	public enum Function {
		GENERATE_TEMPLATE_THUMBNAIL, DELETE_TEMPLATE_THUMBNAIL, GENERATE_PAGE_THUMBNAIL, DELETE_PAGE_THUMBNAIL, CHECK_FOR_PAGE_THUMBNAIL, CHECK_FOR_TEMPLATE_THUMBNAIL
	}

	private static final Logger log = LogManager.getLogger(PSThumbnailRunner.class);

	public static void setActiveWorkerLimit(Integer limit) {
		if(limit!=null){
			if (limit > -1) {
				activeWorkerLimit = new AtomicInteger(limit);
			}
		}
	}

	public static synchronized boolean scheduleThumbnailJob(String id,
			Function function) {
		boolean proceed = false;

		if (!shutdownFlag.get() && id != null && !inProcess.containsKey(id)) {
			inProcess.putIfAbsent(id, function);
			proceed = true;
			PSThumbnailProcessMonitor.incrementCount();
		}
		return proceed;
	}

	private static void completeJob(String id) {
		inProcess.remove(id);
	}

	private static synchronized boolean isWorkAvailable() {
		if (activeWorkers.get() < activeWorkerLimit.get()) {
			activeWorkers.incrementAndGet();
			return true;
		}
		log.debug("Active Thumbnail Runners: {}", activeWorkers);
		return false;
	}

	private static synchronized void leaveWork() {
		activeWorkers.decrementAndGet();
	}
	
	/**
	 * Clear the process queue
	 */
	public static synchronized void shutdown()
	{
	    shutdownFlag.set(true);
	    inProcess.clear();
	}

	public PSThumbnailRunner(IPSSiteTemplateService siteTemplateService,
			IPSTemplateService templateService, IPSPageService pageService,
			boolean waitForCompletion, Map<String, Object> requestInfoMap) {
		this.siteTemplateService = siteTemplateService;
		this.templateService = templateService;
		this.pageService = pageService;
		this.waitForCompletion = waitForCompletion;
		this.requestInfoMap = requestInfoMap;
	}

	@Override
	public void run() {
		if (isWorkAvailable()) {
			goToWork();
		}
	}

	private static PSWorkPackage getNextPSWorkPackage() {
		PSWorkPackage workPackage = null;
		if (!inProcess.isEmpty()) {
			String idForWork = inProcess.keySet().iterator().next();
			Function functionForWork = inProcess.get(idForWork);
			completeJob(idForWork);
			workPackage = new PSWorkPackage(idForWork, functionForWork);
		}
		return workPackage;
	}

	public void generateThumbnailNow(String id, Function function)
	{
		PSWorkPackage workPackage = new PSWorkPackage(id, function);
		this.init();
		performWork(workPackage);
	}
	
	private void goToWork() {
		
			this.init();
			PSWorkPackage workPackage = getNextPSWorkPackage();
			while (workPackage != null) {
			
				try {
				    if (!shutdownFlag.get())
				        performWork(workPackage);
				} catch (Exception e) {
					completeJob(workPackage.getId());
				}
				finally
				{
				    PSThumbnailProcessMonitor.decrementCount();
				}
				workPackage = getNextPSWorkPackage();
			}
			leaveWork();
	}

	private void performWork(PSWorkPackage workPackage) {
		try {
			workPackage.setPage(getPage(workPackage));
			workPackage.setTemplate(getTemplate(workPackage));
			workPackage.setSite(getSite(workPackage.getId(),
					workPackage.getFunction()));
			workPackage.setSiteFolderPath(getSiteFolder(workPackage));
			workPackage.setFileSuffix(getFileSuffix(workPackage
					.getFunction()));
			switch (workPackage.getFunction()) {
				case GENERATE_PAGE_THUMBNAIL:
					generateThumbnail(workPackage);
					break;
				case GENERATE_TEMPLATE_THUMBNAIL:
					generateThumbnail(workPackage);
					break;
				case CHECK_FOR_PAGE_THUMBNAIL:
					checkForPageThumbnail(workPackage);
					break;
				case CHECK_FOR_TEMPLATE_THUMBNAIL:
					checkForTemplateThumbnail(workPackage);
					break;
				case DELETE_PAGE_THUMBNAIL:
					delete(workPackage);
					break;
				case DELETE_TEMPLATE_THUMBNAIL:
					deleteTemplateThumbnail(workPackage);
					break;
			}
		} catch (PSDataServiceException e) {
			log.error(e.getMessage());
			log.debug(e.getMessage(),e);
		}
	}

	private void deleteTemplateThumbnail(PSWorkPackage workPackage) {
		// TODO Auto-generated method stub

	}

	private void delete(PSWorkPackage workPackage) {
		try {
			File root = new File(
					(PSSiteConfigUtils.getRootDirectory() + "/" + TPL_IMAGES_DIR)
							.replace("\\", "/"));
			Iterator iterator = FileUtils.listFiles(root, null, true)
					.iterator();

			while (iterator.hasNext()) {
				File file = (File) iterator.next();
				if (file.getName().contains(
						workPackage.getId() + TEMPLATE_STRING)
						|| file.getName().contains(
								workPackage.getId() + PAGE_STRING))
					Files.delete(file.toPath());
			}
		} catch (Exception e) {
			//FB: DMI_INVOKING_TOSTRING_ON_ARRAY NC 1-16-16
			log.debug("Failed to delete thumbnail for Page ID: "
					+ workPackage.getId() + e.getLocalizedMessage(),e);
		}
	}

	private void checkForPageThumbnail(PSWorkPackage workPackage) {
		String folderPath = workPackage.getPage().getFolderPath();
		String imgPath = (PSSiteConfigUtils.getRootDirectory() + "/"
				+ TPL_IMAGES_DIR + '/' + workPackage.getSite().getName() + '/'
				+ workPackage.getId() + PAGE_STRING).replace("\\", "/")
				.replace("//", "/");

		File f = new File(imgPath);
		if (!f.exists()) {
			workPackage.setFunction(Function.GENERATE_PAGE_THUMBNAIL);
			scheduleThumbnailJob(workPackage.getId(), workPackage.getFunction());
		}
	}

	private void checkForTemplateThumbnail(PSWorkPackage workPackage) {
		// Not implemented
	}

	private void generateThumbnail(PSWorkPackage workPackage) {

		if (workPackage.getPage() != null) {
			try {
				
				if (isTemplateFunction(workPackage.getFunction())) {
					if (workPackage.getTemplate().getName() != "Unassigned") {
						buildThumbnailsForTemplatesPages(workPackage);
						handleThumbnailGeneration(workPackage.getId(),
								workPackage.getFunction(),
								workPackage.getFileSuffix(),
								workPackage.getSiteFolderPath(), workPackage.getPage());
					}
				} else {
					handleThumbnailGeneration(workPackage.getId(),
							workPackage.getFunction(),
							workPackage.getFileSuffix(),
							workPackage.getSiteFolderPath(), workPackage.getPage());
				}
			} catch (Exception e) {
				//FB: DMI_INVOKING_TOSTRING_ON_ARRAY NC 1-16-16
				log.warn("Failed to generate thumbnail for id: "
						+ workPackage.getId() + " " + e.getLocalizedMessage()
						+ " failure occurred in generateThumbnail "
						, e);

			}
		}
		
	}

	// ===================================================================================================
	// Thumbnail Specific Methods
	// ===================================================================================================
	private void handleThumbnailGeneration(String id, Function function,
			String fileSuffix, String siteFolder, PSPage page) throws MalformedURLException {
		if (page != null) {
			String thumbnailFilePath = siteFolder + id + fileSuffix;
			String path = (page.getFolderPath() + "/" + page.getName()).replace(
					"//", "/");
			URL url = PSUrlUtils.createUrl("127.0.0.1", null, path,
					sessionParameterMap.iterator(), null, requestContext, true);
			try
			{

				PSScreenCapture.takeCapture(url.toString(), thumbnailFilePath,
						1180, 860);
				PSThumbnailImageUtils.resizeThumbnail(thumbnailFilePath);
					

			} catch (Exception e) {
				log.error("Thumbnail Exception: {}" , e.getMessage());
				log.debug(e);
			}
			File thumbNail = new File(thumbnailFilePath);
			if (!thumbNail.exists()) {
				try {
					PSScreenCapture.generateEmptyThumb(thumbnailFilePath);
				} catch (Exception e1) {
					log.error("Thumbnail Exception for empty thumb: {}" , e1.getMessage());
					log.debug(e1);
				}
			}
		}
	}

	private boolean validateConnect(String url) {
		boolean valid = false;
		try {
			if (!url.contains("/.system/")) {
				valid = true;
			}
		}

		catch (Exception e) {
			log.debug(e);
		}
		return valid;
	}

	private void buildThumbnailsForTemplatesPages(PSWorkPackage workPackage) throws PSDataServiceException {

		if ((!workPackage.getTemplate().getName().equals("Unassigned"))
				&& workPackage.getFunction() == Function.GENERATE_TEMPLATE_THUMBNAIL) {
		    
			PSPagedItemList pages = pageService.findPagesByTemplate(workPackage
					.getTemplate().getId(), 1, 10000, null, null, null);
			List<PSPathItem> pagePaths = pages.getChildrenInPage();
			
			// add additional items to the in process count
			int numPages = pagePaths.size();
			if (numPages > 0)
			    PSThumbnailProcessMonitor.incrementCount(numPages);

			for (PSPathItem pagePath : pagePaths) {
				try {
					PSPage thisPage = pageService.find(pagePath.getId());
					scheduleThumbnailJob(thisPage.getId(), Function.GENERATE_PAGE_THUMBNAIL);
				} catch (Exception e) {

					log.debug("Unable to generate thumbnail for a templates's child page with id: "
							+ pagePath.getId()
							+ " "
							+ e.getLocalizedMessage(),
							e);
					String imagePath = workPackage.getSiteFolderPath()
							+ pagePath.getId() + PAGE_STRING;
					File thumbNail = new File(imagePath);
					if (!thumbNail.exists()) {
						try {
							PSScreenCapture.generateEmptyThumb(imagePath);
						} catch (Exception e1) {
							log.error(ExceptionUtils.getStackFrames(e1));
						}
					}
				}
				finally
				{
				    if (numPages > 0)
				        PSThumbnailProcessMonitor.decrementCount(numPages);
				}
			}
		}
	}

	// ===================================================================================================
	// INITIALIZATION METHODS
	// ===================================================================================================

	private void init() {
		try {
			initializeRequest();
			initSessionVariablesForUrlAssembly();
		} catch (Exception e) {
			log.debug("Thumbnail Service Runner initalization failure for ID: successful thumbnail generation unlikely");
			log.debug(e);
		}
	}

	private void initSessionVariablesForUrlAssembly() {
		PSRequest request = (PSRequest) PSRequestInfo
				.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		String sessionId = request.getUserSessionId();
		requestContext = new PSRequestContext(request);
		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put(IPSHtmlParameters.SYS_SESSIONID, sessionId);
		sessionParameterMap = paramMap.entrySet();
	}

	private String getSiteFolder(PSWorkPackage work) throws IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException, PSValidationException {
		if (work.getSite() == null)
			work.setSite(getSite(work.getId(), work.getFunction()));
		String siteFolder = PSSiteConfigUtils.getRootDirectory()
				+ "/rx_resources/images/TemplateImages/"
				+ work.getSite().getName() + "/";

		File f = new File(siteFolder);
		if (!f.exists()) {
			f.mkdir();
		}

		return siteFolder;
	}

	private void initializeRequest() {
		if (PSRequestInfo.isInited()) {
			PSRequestInfo.resetRequestInfo();
		}
		PSRequestInfo.initRequestInfo(requestInfoMap);
	}

	private boolean isTemplateFunction(Function function) {
		boolean isTemplateFunc = false;
		if (function == Function.GENERATE_TEMPLATE_THUMBNAIL
				|| function == Function.CHECK_FOR_TEMPLATE_THUMBNAIL
				|| function == Function.DELETE_TEMPLATE_THUMBNAIL)
			isTemplateFunc = true;
		return isTemplateFunc;
	}

	private boolean isPageFunction(Function function) {
		boolean isPageFunc = false;
		if (function == Function.GENERATE_PAGE_THUMBNAIL
				|| function == Function.CHECK_FOR_PAGE_THUMBNAIL
				|| function == Function.DELETE_PAGE_THUMBNAIL)
			isPageFunc = true;
		return isPageFunc;
	}

	private PSTemplateSummary getTemplate(PSWorkPackage workPackage) throws IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException, PSValidationException {
		PSTemplateSummary template = null;
		if (workPackage.getTemplate() == null) {

			if (isTemplateFunction(workPackage.getFunction())) {
				template = templateService.find(workPackage.getId());
			} else if (isPageFunction(workPackage.getFunction())) {
				if (workPackage.getPage() == null)
					workPackage.setPage(pageService.find(workPackage.getId()));
				template = templateService.find(workPackage.getPage()
						.getTemplateId());
			}
		} else {
			template = workPackage.getTemplate();
		}
		return template;
	}

	private PSPage getPage(PSWorkPackage workPackage) throws PSDataServiceException {
		PSPage page = null;
		if (workPackage.getPage() == null) {
			if (isPageFunction(workPackage.getFunction())) {
				if (page == null) {
					page = pageService.find(workPackage.getId());
				}
			} else if (isTemplateFunction(workPackage.getFunction())) {
				PSTemplateSummary template = templateService.find(workPackage.getId());
				PSPagedItemList pages = pageService.findPagesByTemplate(
						template.getId(), 1, 2, null, null, null);
				List<PSPathItem> pagePaths = pages.getChildrenInPage();
				if (pages.getChildrenCount() > 0) {
					page = pageService.find(pagePaths.get(0).getId());
				}
			}
		} else {
			page = workPackage.getPage();
		}
		return page;
	}

	private String getFileSuffix(Function function) {
		String fileSuffix = null;
		if (isPageFunction(function)) {
			fileSuffix = PAGE_STRING;
		} else if (isTemplateFunction(function)) {
			fileSuffix = TEMPLATE_STRING;
		}
		return fileSuffix;
	}

	private PSSiteSummary getSite(String id, Function function) throws IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException, PSValidationException {
		String templateId = id;
		PSSiteSummary site = null;
		if (isPageFunction(function)) {
			templateId = pageService.find(id).getTemplateId();
		}
		List<PSSiteSummary> sites = siteTemplateService
				.findSitesByTemplate(templateId);
		if (!sites.isEmpty()) {
			site = sites.get(0);
		}
		return site;
	}

}
