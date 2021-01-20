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

import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.server.PSRequest;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.share.service.impl.PSThumbnailRunner;
import com.percussion.share.service.impl.PSThumbnailRunner.Function;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.utils.request.PSRequestInfo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("thumbnailGenerationHelper")
@Lazy
public class PSThumbnailGenerationHelper extends PSImportHelper {
	private static final String STATUS_MESSAGE = "generating thumbnails";
	private IPSSiteTemplateService siteTemplateService;
	private IPSTemplateService templateService;
	private IPSPageService pageService;

	@Autowired
	public PSThumbnailGenerationHelper(
			IPSSiteTemplateService siteTemplateService,
			IPSTemplateService templateService, IPSPageService pageService,
			IPSNotificationService notificationService) {
		super();
		this.siteTemplateService = siteTemplateService;
		this.templateService = templateService;
		this.pageService = pageService;

	}

	@Override
	public void process(PSPageContent pageContent, PSSiteImportCtx context)
			throws PSSiteImportException {
		//This thread is async... stuff is still happening
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// NOOP
		}
		
		startTimer();
		final Map<String, Object> requestInfoMap = PSRequestInfo
				.copyRequestInfoMap();
		PSRequest request = (PSRequest) requestInfoMap
				.get(PSRequestInfo.KEY_PSREQUEST);
		requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());

		PSThumbnailRunner runner = new PSThumbnailRunner(siteTemplateService,
				templateService, pageService, true, requestInfoMap);
		runner.generateThumbnailNow(context.getTemplateId(), Function.GENERATE_TEMPLATE_THUMBNAIL);
		endTimer();
		/*
		
		*/
		
	}

	@Override
	public void rollback(PSPageContent pageContent, PSSiteImportCtx context) {
		// NOOP - this is an optional helper

	}

	@Override
	public String getHelperMessage() {
		return STATUS_MESSAGE;
	}

}
