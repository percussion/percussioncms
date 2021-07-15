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
package com.percussion.pagemanagement.dao.impl;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSWidgetItemIdGenerator;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageSummary;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageService.PSPageException;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.PSJcrNodeFinder;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * CRUDS page objects. Business logic such as validation is not done here.
 * 
 * @author adamgent
 * 
 */
@Component("pageDao")
@Lazy
public class PSPageDao extends PSAbstractContentItemDao<PSPage> implements
		IPSPageDao {
	/**
	 * Logger for this service.
	 */

	private static final Logger log = LogManager.getLogger(PSPageDao.class);

	private IPSWidgetItemIdGenerator widgetItemIdGenerator;

	private IPSIdMapper idMapper;

	private PSJcrNodeFinder jcrNodeFinder;

	/**
	 * The cached page id, which is lazily coded.
	 */
	private Long pageContentTypeId;

	@Autowired
	public PSPageDao(IPSContentItemDao contentItemDao, IPSIdMapper idMapper,
			IPSWidgetItemIdGenerator widgetItemIdGenerator,
			IPSContentMgr contentMgr) {
		super(contentItemDao, idMapper);
		this.idMapper = idMapper;
		this.widgetItemIdGenerator = widgetItemIdGenerator;
		jcrNodeFinder = new PSJcrNodeFinder(contentMgr,
				IPSPageService.PAGE_CONTENT_TYPE, "sys_title");
	}

	@Override
	protected PSPage createObject() {
		return new PSPage();
	}

	@Override
	protected String getType() {
		return IPSPageService.PAGE_CONTENT_TYPE;
	}

	@Override
	protected PSPage getObjectFromContentItem(PSContentItem contentItem) {
		PSPage page = super.getObjectFromContentItem(contentItem);

		page.setWorkflowId(Integer.parseInt((String) contentItem.getFields()
				.get(IPSHtmlParameters.SYS_WORKFLOWID)));

		return page;
	}

	private void handleThumbnail(String id) {
		PSNotificationEvent notifyEvent = new PSNotificationEvent(
				EventType.PAGE_LOAD, id);
		IPSNotificationService srv = PSNotificationServiceLocator
				.getNotificationService();
		srv.notifyEvent(notifyEvent);
	}

	public PSPage findPageByPath(String fullFolderPath) throws PSPageException {
		try {
			PSContentItem contentItem = getContentItemDao().findItemByPath(
					fullFolderPath);
			if (contentItem == null) {
				return null;
			}

			//Do not return content items that aren't Page's!
			if (contentItem.isPage()) {
				PSPage page = getObjectFromContentItem(contentItem);
				handleThumbnail(page.getId());
				return page;
			} else {
				return null;
			}
		}catch(Exception e){
			throw new PSPageException(e.getMessage(),e);
		}
	}

	@Override
	public List<Integer> getPageIdsByFieldNameAndValue(String fieldName,
			String fieldValue) throws PSPageException {
		notNull(fieldName, "fieldName should not be null");
		notNull(fieldValue, "fieldValue should not be null");

		long pageTypeId = getPageContentTypeId();
		return PSContentMgrLocator.getContentMgr()
				.findItemsByLocalFieldValue(pageTypeId, fieldName, fieldValue);
	}

	public PSPage findPage(String name, String folderPath)
			throws PSDataServiceException {
		PSContentItem contentItem = getContentItemDao().findItemByPath(name,
				folderPath);
		if (contentItem == null || !contentItem.isPage()) {
			return null;
		}
		PSPage page = find(contentItem.getId());
		handleThumbnail(page.getId());
		return page;
	}


	public void delete(String id) throws PSDataServiceException {
		isTrue(isNotBlank(id), "id may not be blank");
		delete(id, false);
	}

	public void delete(String id, boolean force) throws PSDataServiceException {
		isTrue(isNotBlank(id), "id may not be blank");

			PSPropertiesValidationException pve = new PSPropertiesValidationException(
					null, "delete");
			if (!force) {
				getContentItemDao().validateDelete(id, pve);
				try {
					pve.throwIfInvalid();
				} catch (PSSpringValidationException e) {
					throw new DeleteException(e.getMessage(),e);
				}
			}
			super.delete(id);
			PSNotificationEvent notifyEvent = new PSNotificationEvent(
					EventType.PAGE_DELETE, id);
			IPSNotificationService srv = PSNotificationServiceLocator
					.getNotificationService();
			srv.notifyEvent(notifyEvent);

	}

	public List<PSPage> findPagesBySiteAndTemplate(String path,
			String templateId) throws PSDataServiceException {
		isTrue(isNotBlank(templateId), "templateId may not be blank");

		List<PSPage> pages = new ArrayList<>();

		Map<String, String> whereFields = new HashMap<>();
		whereFields.put("templateid", templateId);
		List<IPSNode> nodes = jcrNodeFinder.find(path, whereFields);
		for (IPSNode node : nodes) {
			pages.add(find(idMapper.getString(node.getGuid())));

		}

		return pages;
	}

	public List<PSPageSummary> findPagesBySiteAndWf(String path,
			int workflowId, int stateId) throws PSDataServiceException {
		isTrue(isNotBlank(path), "path may not be blank");

		List<PSPageSummary> sums = new ArrayList<>();

		Map<String, String> whereFields = new HashMap<>();
		whereFields.put("sys_workflowid", String.valueOf(workflowId));
		if (stateId != -1) {
			whereFields.put("sys_contentstateid", String.valueOf(stateId));
		}
		List<IPSNode> nodes = jcrNodeFinder.find(path, whereFields);
		for (IPSNode node : nodes) {
			sums.add(find(idMapper.getString(node.getGuid())));
			handleThumbnail(idMapper.getString(node.getGuid()));
		}

		return sums;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void convertToObject(PSContentItem contentItem, PSPage page) {
		Map<String, Object> f = contentItem.getFields();
		String name = (String) f.get("sys_title");
		String title = (String) f.get("page_title");
		String regionOverrides = (String) f.get("region_overrides");
		String linkTitle = (String) f.get("resource_link_title");
		String templateId = (String) f.get("templateid");
		String description = (String) f.get("page_description");
		String noindex = (String) f.get("page_noindex");
		String summary = (String) f.get("page_summary");
		String author = (String) f.get("page_authorname");
		List<String> tags = (List<String>) f.get("page_tags");
		String templateContentMigrationVersion = (String) f
				.get("template_content_migration_version");
		String migrationEmptyWidgetFlag = (String) f
				.get("migrationemptywidgets");

		// TODO: Adam serializer will probably break here.
		PSRegionBranches pageRegionBranches = null;
		if (isNotBlank(regionOverrides)) {
			pageRegionBranches = PSSerializerUtils.unmarshal(regionOverrides,
					PSRegionBranches.class);
		} else {
			pageRegionBranches = new PSRegionBranches();
		}

		page.setName(name);
		page.setTitle(title);
		page.setRegionBranches(pageRegionBranches);
		page.setTemplateId(templateId);
		page.setLinkTitle(linkTitle);
		page.setDescription(description);
		page.setNoindex(noindex);
		String folderPath = getFolderPath(contentItem);
		page.setFolderPath(folderPath);
		page.setSummary(summary);
		page.setAuthor(author);
		page.setTags(tags);
		PSHtmlMetadataUtils.fromMap(page, f);

		if (isNumeric(templateContentMigrationVersion)) {
			page.setTemplateContentMigrationVersion(templateContentMigrationVersion);
		}
		page.setMigrationEmptyWidgetFlag("yes"
				.equalsIgnoreCase(migrationEmptyWidgetFlag));
	}

	protected String trimAt(final String stringForTrim, int length) {
		String out = null;
		if (stringForTrim != null) {
			out = stringForTrim.trim();
			if ((length != -1)) {
				out = StringUtils.substring(out, 0, length);
			}
		}
		return out;
	}

	@Override
	protected void convertToItem(PSPage page, PSContentItem contentItem) {
		Map<String, Object> f = contentItem.getFields();

		f.put("sys_title", trimAt(page.getName(), 255));
		f.put("page_title", trimAt(page.getTitle(), 255));
		f.put("resource_link_title", page.getLinkTitle());
		f.put("templateid", page.getTemplateId());
		f.put("page_noindex", page.getNoindex());
		f.put("page_description", page.getDescription());
		f.put("page_summary", page.getSummary());
		f.put("page_authorname", page.getAuthor());
		f.put("page_tags", page.getTags());
		f.put("template_content_migration_version",
				page.getTemplateContentMigrationVersion());
		f.put("migrationemptywidgets",
				page.isMigrationEmptyWidgetFlag() ? "yes" : "no");
		if (page.getWorkflowId() != null) {
			f.put(IPSHtmlParameters.SYS_WORKFLOWID,
					Integer.toString(page.getWorkflowId()));
		}

		if (page.getRegionBranches() != null) {
			widgetItemIdGenerator.generateIds(page.getRegionBranches());
			String pb = PSSerializerUtils.marshal(page.getRegionBranches());
			f.put("region_overrides", pb);
		}

		PSHtmlMetadataUtils.toMap(page, f);
	}

	@Override
	public List<PSPageSummary> findAllSummaries() {
		throw new UnsupportedOperationException(
				"findAllSummaries is not yet supported");
	}

	@Override
	public PSPageSummary findSummary(@SuppressWarnings("unused") String id) {
		throw new UnsupportedOperationException(
				"findSummary is not yet supported");
	}

	@Override
	public long getPageContentTypeId() throws PSPageException {
		if (pageContentTypeId == null) {
			try {
				PSItemDefManager defMgr = PSItemDefManager.getInstance();
				pageContentTypeId = defMgr
						.contentTypeNameToId(IPSPageService.PAGE_CONTENT_TYPE);
			} catch (PSInvalidContentTypeException e) {
				log.error(e.getMessage());
				log.debug(e.getMessage(),e);
				throw new PSPageException(e.getMessage());
			}
		}

		return pageContentTypeId;
	}

	@Override
	public List<PSPage> findAllPagesBySite(String sitePath) throws PSDataServiceException {

		isTrue(isNotBlank(sitePath), "sitePath may not be blank");

		List<PSPage> pages = new ArrayList<>();

		Map<String, String> whereFields = new HashMap<>();
		List<IPSNode> nodes = jcrNodeFinder.find(sitePath, whereFields);
		for (IPSNode node : nodes) {
			pages.add(find(idMapper.getString(node.getGuid())));

		}

		return pages;
	}
}
