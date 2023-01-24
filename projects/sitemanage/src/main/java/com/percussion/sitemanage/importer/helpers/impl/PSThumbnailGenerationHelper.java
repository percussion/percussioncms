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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

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
			Thread.currentThread().interrupt();
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
