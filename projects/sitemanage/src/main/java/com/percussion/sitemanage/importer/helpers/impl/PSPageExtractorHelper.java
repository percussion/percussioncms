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
package com.percussion.sitemanage.importer.helpers.impl;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.sitemanage.importer.utils.PSManagedTagsUtils.commentTag;
import static com.percussion.sitemanage.importer.utils.PSManagedTagsUtils.isManagedJSReference;

import static java.util.Arrays.asList;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.impl.PSWorkflowHelper;
import com.percussion.pagemanagement.data.IPSHtmlMetadata;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.queue.impl.PSSiteQueue;
import com.percussion.server.PSRequest;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSNameGenerator;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author LucasPiccoli
 * 
 */

class PageSaveRunner implements Runnable {
	private static final String STATUS_MESSAGE = "OUCH:";

	private Map<String, Object> requestInfoMap;

	private PSPage targetPage;

	private PSPageContent pageContent;

	private PSSiteImportCtx context;

	private IPSPageImportQueue pageImportQueue;

	private PSPageExtractorHelper pageExtractorHelper;

	private IPSIdMapper idMapper;

	private IPSPageCatalogService pageCatalogService;

	private boolean pageImport;

	public PageSaveRunner(Map<String, Object> requestInfoMap,
			PSPage targetPage, PSPageContent pageContent,
			PSSiteImportCtx context, PSPageExtractorHelper pageExtractorHelper,
			IPSIdMapper idMapper, IPSPageCatalogService pageCatalogService,
			boolean pageImport) {
		this.requestInfoMap = requestInfoMap;

		this.targetPage = targetPage;
		this.pageContent = pageContent;
		this.context = context;
		this.pageExtractorHelper = pageExtractorHelper;
		this.idMapper = idMapper;
		this.pageCatalogService = pageCatalogService;
		this.pageImport = pageImport;
	}

    @Override
    public void run()
    {

        Long siteId = context.getSite().getSiteId();
        int id = ((PSLegacyGuid) idMapper.getGuid(targetPage.getId())).getContentId();

        try
        {
            setRequestInfo(this.requestInfoMap);

            pageExtractorHelper.doPageExtraction(pageContent, context, targetPage, pageCatalogService);

            PSSiteQueue siteQueue = getSiteQueue(siteId);

            if (siteQueue.getImportingIds().contains(id))
                siteQueue.addImportedId(id);
        }
        catch (Exception e)
        {
            getSiteQueue(siteId).removeImportingId(id);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            context.getLogger().appendLogMessage(PSLogEntryType.ERROR, STATUS_MESSAGE, exceptionAsString);
        }
        finally
        {
            context.getLogger().removeFromWaitCount();
        }
    }

	public PSSiteQueue getSiteQueue(long siteId) {

		if (pageImportQueue == null) {
			pageImportQueue = (IPSPageImportQueue) getWebApplicationContext()
					.getBean("pageImportQueue");
		}

		return pageImportQueue.getPageIds(siteId);
	}

	public void setRequestInfo(Map<String, Object> requestInfoMap) {
		if (PSRequestInfo.isInited()) {
			PSRequestInfo.resetRequestInfo();
		}
		PSRequestInfo.initRequestInfo(requestInfoMap);
	}

}

@PSSiteManageBean("pageExtractorHelper")
@Lazy
public class PSPageExtractorHelper extends PSGenericMetadataExtractorHelper {

	private final static String STATUS_MESSAGE = "changing page information";

	private final static String LOG_CATEGORY = "Create HTML Widget";

	@Value("${pageExtractor:true}")
	private boolean pageImport = false;

	@Value("${extractMetaData:true}")
	private boolean extractMetaData = false;

	private boolean runSaveSyncronously = false;

	private IPSIdMapper idMapper;

	private IPSPageCatalogService m_pageCatalogService;

	private IPSPageService pageService;

	private IPSAssetService assetService;

	private IPSItemWorkflowService itemWorkflowService;

	private static HashMap<Long, PSTemplate> unassignedTemplateCache = new HashMap<Long, PSTemplate>();

	@Autowired
	public PSPageExtractorHelper(IPSPageService pageService,
			IPSAssetService assetService,
			IPSItemWorkflowService itemWorkflowService,
			IPSTemplateService templateService, IPSNameGenerator nameGenerator,
			IPSIdMapper idMapper) {
		super(templateService);
		this.pageService = pageService;
		this.assetService = assetService;
		this.itemWorkflowService = itemWorkflowService;
		this.templateService = templateService;
		this.nameGenerator = nameGenerator;
		this.idMapper = idMapper;

	}

	@Override
	@SuppressFBWarnings("RU_INVOKE_RUN")
	public void process(PSPageContent pageContent, PSSiteImportCtx context)
			throws PSSiteImportException {
		try {
			startTimer();

			PSPage targetPage = getTargetPage(context);
			final Map<String, Object> requestInfoMap = PSRequestInfo
					.copyRequestInfoMap();

			PSRequest request = (PSRequest) requestInfoMap
					.get(PSRequestInfo.KEY_PSREQUEST);
			requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST,
					request.cloneRequest());

			PageSaveRunner pageSaveRunner = new PageSaveRunner(requestInfoMap,
					targetPage, pageContent, context, this, idMapper,
					getPageCatalogService(), pageImport);

			Thread t = new Thread(pageSaveRunner);
			t.setDaemon(true);

			if (this.runSaveSyncronously)
				t.run();
			else
				t.start();

			context.getLogger()
					.appendLogMessage(PSLogEntryType.STATUS, STATUS_MESSAGE,
							"The page body was successfully imported into HTML widget.");
			endTimer();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			context.getLogger().appendLogMessage(PSLogEntryType.ERROR,
					STATUS_MESSAGE, exceptionAsString);
		}
	}

	public void doPageExtraction(PSPageContent pageContent,
			PSSiteImportCtx context, PSPage targetPage,
			IPSPageCatalogService pageCatalogService)
			throws PSSiteImportException {
		if (extractMetaData) {
			doExtractMetaData(pageContent, context);
		} else {
			// still need to update page meta-data with the description
			setDescriptionOnPage(pageContent, context);
		}

		// Get HTML widget from template
		PSTemplate template = templateService.load(context.getTemplateId());

		List<PSWidgetItem> widgets = template.getWidgets();
		if (widgets == null || widgets.size() == 0) {
			context.getLogger()
					.appendLogMessage(PSLogEntryType.ERROR, LOG_CATEGORY,
							"The page body could not be imported, no HTML widget found.");
			return;
		}

		String extractedBodyHtml = extractBody(pageContent, context);

		PSAsset localAsset = createHTMLLocalContent(extractedBodyHtml,
				itemWorkflowService, assetService, nameGenerator);

		PSPair<PSWidgetItem, PSAsset> widgetAssetPair = new PSPair<PSWidgetItem, PSAsset>(
				widgets.get(0), localAsset);

		itemWorkflowService.checkOut(targetPage.getId());
		addContentToWidgetOnPage(targetPage, widgetAssetPair, assetService);
		itemWorkflowService.checkIn(targetPage.getId());

		importPageIfNecessary(context, pageCatalogService, pageImport);
	}

	/**
	 * Set the description on the page and save it.
	 * 
	 * @param pageContent
	 *            The content of the page
	 * @param context
	 *            The current import context
	 */
	private void setDescriptionOnPage(PSPageContent pageContent,
			PSSiteImportCtx context) {
		if (context.isCanceled()) {
			return;
		}

		try {
			context.getLogger().appendLogMessage(PSLogEntryType.STATUS,
					EXTRACT_METADATA, "Updating page description.");

			IPSHtmlMetadata targetItem = getTargetItem(context);
			setDescriptionInMetadata(pageContent, context.getLogger(),
					targetItem);
			saveTargetItem(targetItem);
		} catch (Exception e) {
			context.getLogger().appendLogMessage(PSLogEntryType.ERROR,
					EXTRACT_METADATA, "Page description could not be updated.");
			context.getLogger().appendLogMessage(PSLogEntryType.STATUS,
					EXTRACT_METADATA,
					"Page description could not be updated: " + e.getMessage());
			log.error("Error updating description while importing a page", e);
		}

	}

	@Override
	protected void setMetadataToTargetItem(PSPageContent pageContent,
			IPSSiteImportLogger logger, IPSHtmlMetadata targetItem) {
		// first run base class method
		super.setMetadataToTargetItem(pageContent, logger, targetItem);

		// now update description
		setDescriptionInMetadata(pageContent, logger, targetItem);
	}

	private void setDescriptionInMetadata(PSPageContent pageContent,
			IPSSiteImportLogger logger, IPSHtmlMetadata targetItem) {
		String description = pageContent.getDescription();
		if (isBlank(description)) {
			logger.appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
					"No description meta tag was extracted from the page.");

		}

		targetItem.setDescription(description);
	}

	private IPSPageCatalogService getPageCatalogService() {

		if (m_pageCatalogService == null) {
			m_pageCatalogService = (IPSPageCatalogService) getWebApplicationContext()
					.getBean("pageCatalogService");
		}
		return m_pageCatalogService;
	}

	public static synchronized void importPageIfNecessary(
			PSSiteImportCtx context, IPSPageCatalogService pageCatalogService,
			boolean pageImport) {
		// Something in the depths of this is not thread safe. Thus the sync and
		// static
		final String STATUS_MESSAGE = "changing page information";
		if (pageImport) {
			if (context.isCanceled()) {
				return;
			}

			context.getLogger().appendLogMessage(
					PSLogEntryType.STATUS,
					STATUS_MESSAGE,
					"Starting to move imported page " + context.getPageName()
							+ " to the actual location");

			// Call method to import the page
			try {
				pageCatalogService.createImportedPage(context
						.getCatalogedPageId());
				context.getLogger().appendLogMessage(
						PSLogEntryType.STATUS,
						STATUS_MESSAGE,
						"Successfully moved imported page " + context.getPageName()
								+ " to the actual location");
			} catch (Exception e) {
				String errorMsg = "Could not move the imported page "
						+ context.getPageName()
						+ "to the matching site folder.";
				context.getLogger().appendLogMessage(PSLogEntryType.ERROR,
						STATUS_MESSAGE, errorMsg);
				context.getLogger()
						.appendLogMessage(
								PSLogEntryType.STATUS,
								STATUS_MESSAGE,
								errorMsg + " The error was: "
										+ e.getLocalizedMessage());

				throw new PSSiteImportException(errorMsg, e);
			}

			
		}
	}

	/**
	 * Adds the given widget on a page. Basically it creates instances of
	 * {@link PSRelationship} where the owner is the page, and the dependant is
	 * the given asset, using the widget id (slot id) from the
	 * {@link PSWidgetItem} object.
	 * 
	 * @param page
	 *            {@link PSPage} the page to use on the relationship. Assumed
	 *            not <code>null</code>.
	 * @param widgetAssetPairs
	 *            {@link List}<{@link PSPair}<{@link PSWidgetItem},
	 *            {@link PSAsset}>>
	 * @return {@link List}<{@link PSAssetWidgetRelationship}> with the created
	 *         relationships. Never <code>null</code> but may be empty.
	 */
	private static List<PSAssetWidgetRelationship> addContentToWidgetOnPage(
			PSPage targetPage, PSPair<PSWidgetItem, PSAsset> widgetAssetPair,
			IPSAssetService assetService) {
		List<PSAssetWidgetRelationship> relationships = new ArrayList<PSAssetWidgetRelationship>();
		String ownerId = targetPage.getId();

		PSWidgetItem widget = widgetAssetPair.getFirst();
		PSAsset asset = widgetAssetPair.getSecond();

		PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(
				ownerId, Long.parseLong(widget.getId()),
				widget.getDefinitionId(), asset.getId(), 1, widget.getName());
		assetService.createAssetWidgetRelationship(awRel);

		relationships.add(awRel);

		return relationships;
	}

	private PSPage getTargetPage(PSSiteImportCtx context) {
		if (isBlank(context.getCatalogedPageId())) {
			return pageService.findPage(context.getPageName(), context
					.getSite().getFolderPath());
		}
		return pageService.find(context.getCatalogedPageId());
	}

	@Override
	public void rollback(PSPageContent pageContent, PSSiteImportCtx context) {
		throw new UnsupportedOperationException();
	}

	private static String extractBody(PSPageContent pageContent,
			PSSiteImportCtx context) {
		try {
			// The source document as modified by other helpers.
			// At this point the body image links have been updated.
			Document doc = pageContent.getSourceDocument();
			Attributes bodyTagAttributes = doc.body().attributes();
			if (bodyTagAttributes != null && bodyTagAttributes.size() > 0) {
				context.getLogger()
						.appendLogMessage(
								PSLogEntryType.STATUS,
								"Extract page body",
								"Body attributes found. The attributes won't be imported. The attributes removed are: "
										+ bodyTagAttributes.html());

				for (Attribute attribute : bodyTagAttributes.asList()) {
					doc.body().removeAttr(attribute.getKey());
				}
			}
			context.getLogger().appendLogMessage(PSLogEntryType.STATUS,
					STATUS_MESSAGE,
					"Page body extraction finished successfully.");

			commentOutManagedJSReferences(doc.body(), context.getLogger());

			pageContent.setBodyContent(doc.body().html());
			return StringEscapeUtils.unescapeHtml(doc.body().html());
		} catch (RuntimeException e) {
			context.getLogger().appendLogMessage(PSLogEntryType.ERROR,
					LOG_CATEGORY,
					"Unable to create HTML widget from page body.");
			context.getLogger().appendLogMessage(
					PSLogEntryType.STATUS,
					LOG_CATEGORY,
					"Unable to create HTML widget from page body: "
							+ e.getLocalizedMessage());
			return new String();
		}
	}

	/**
	 * Comment out a managed js reference in the body element.
	 * 
	 * @param body
	 *            {@link Element} to comment the references from. Assumed not
	 *            <code>null</code>.
	 * @param logger
	 *            {@link IPSSiteImportLogger} to append the message. Assumed not
	 *            <code>null</code>.
	 */
	private static void commentOutManagedJSReferences(Element body,
			IPSSiteImportLogger logger) {
		Elements scriptTags = body.select("script");
		for (Element scriptTag : scriptTags) {
			if (isManagedJSReference(scriptTag)) {
				logger.appendLogMessage(PSLogEntryType.STATUS,
						COMMENTED_JS_REFERENCE_FROM_BODY, scriptTag.toString());
				commentTag(body, scriptTag);
			}
		}
	}

	private static PSAsset createHTMLLocalContent(String htmlContent,
			IPSItemWorkflowService itemWorkflowService,
			IPSAssetService assetService, IPSNameGenerator nameGenerator) {
		PSAsset asset = new PSAsset();
		String assetName = nameGenerator.generateLocalContentName();
		asset.setName(assetName);
		asset.setType("percRawHtmlAsset");
		asset.getFields().put(IPSHtmlParameters.SYS_TITLE, assetName);
		asset.getFields().put("html", htmlContent);
		int workflowId = itemWorkflowService
				.getWorkflowId(PSWorkflowHelper.LOCAL_WORKFLOW_NAME);
		asset.getFields().put(IPSHtmlParameters.SYS_WORKFLOWID,
				String.valueOf(workflowId));
		asset = assetService.save(asset);
		return asset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.percussion.sitemanage.importer.helpers.impl.
	 * PSGenericMetadataExtractorHelper
	 * #saveTargetItem(com.percussion.pagemanagement.data.IPSHtmlMetadata)
	 */
	@Override
	protected void saveTargetItem(IPSHtmlMetadata targetItem) {
		PSPage page = (PSPage) targetItem;
		// the target Item in this case is a PSPage

		long workflowTimer = System.nanoTime();

		itemWorkflowService.checkOut(page.getId());
		pageService.save(page);
		itemWorkflowService.checkIn(page.getId());

		PSHelperPerformanceMonitor.updateStats(
				"PSPageMetaDataExtractor:PageWorkflow",
				((System.nanoTime() - workflowTimer) / 1000000));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.percussion.sitemanage.importer.helpers.impl.
	 * PSGenericMetadataExtractorHelper
	 * #addHtmlWidgetToTemplate(com.percussion.sitemanage.data.PSSiteImportCtx)
	 */
	@Override
	protected void addHtmlWidgetToTemplate(PSSiteImportCtx context) {
		PSTemplate template = unassignedTemplateCache.get(context.getSite()
				.getSiteId());

		if (template == null) {
			if (unassignedTemplateCache.size() > 5) {
				unassignedTemplateCache.clear();
			}

			// Load site's home page template
			template = templateService.load(this.getPageCatalogService()
					.getCatalogTemplateIdBySite(context.getSite().getName()));
			// Set Theme (Only first time)
			if (isBlank(template.getTheme())) {
				template.setTheme(context.getThemeSummary().getName());
			}

			// add the widget only if it is not already created
			if (isEmpty(template.getWidgets())) {
				// Create Raw HTML widget and add the widget to the template
				PSWidgetItem rawHtmlWidget = PSPageManagementUtils
						.createRawHtmlWidgetItem("1");
				template.getRegionTree().setRegionWidgets(REGION_CONTENT,
						asList(rawHtmlWidget));

				context.getLogger()
						.appendLogMessage(PSLogEntryType.STATUS,
								ADD_HTML_WIDGET,
								"The HTML widget was successfully added to the Unassigned template.");

				// Save template and finish
				templateService.save(template);

				context.getLogger()
						.appendLogMessage(PSLogEntryType.STATUS,
								EXTRACT_METADATA,
								"Metadata was successfully saved to the Unassigned template.");
			}
			unassignedTemplateCache
					.put(context.getSite().getSiteId(), template);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.percussion.sitemanage.importer.helpers.impl.
	 * PSGenericMetadataExtractorHelper
	 * #getTargetItem(com.percussion.sitemanage.data.PSSiteImportCtx)
	 */
	@Override
	protected IPSHtmlMetadata getTargetItem(PSSiteImportCtx context) {
		return getTargetPage(context);
	}

	/**
	 * The name generator, initialized by constructor, never <code>null</code>
	 * after that.
	 */
	private IPSNameGenerator nameGenerator;

	@Override
	public String getHelperMessage() {
		return STATUS_MESSAGE;
	}

	public boolean isPageImport() {
		return pageImport;
	}

	public void setPageImport(boolean pageImport) {
		this.pageImport = pageImport;
	}

	public boolean isRunSaveSyncronously() {
		return runSaveSyncronously;
	}

	public void setRunSaveSyncronously(boolean runSaveSyncronously) {
		this.runSaveSyncronously = runSaveSyncronously;
	}

	public boolean isExtractMetaData() {
		return extractMetaData;
	}

	public void setExtractMetaData(boolean extractMetaData) {
		this.extractMetaData = extractMetaData;
	}

}
