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

package com.percussion.category.extension;

import com.percussion.category.data.PSCategory;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;


public class PSCategoryPostExit implements IPSResultDocumentProcessor {
	
	public static final Logger log = LogManager.getLogger(PSCategoryPostExit.class);

	@Override
	public void init(IPSExtensionDef def, File codeRoot) {
	}
	
	@Override
	public boolean canModifyStyleSheet() {
		return false;
	}

	@Override
	public Document processResultDocument(Object[] params, IPSRequestContext request, Document resultDoc)
			throws PSParameterMismatchException, PSExtensionProcessingException {

		org.w3c.dom.Document doc = null;
		// Getting the data from the xml. Then filter it out based on the selecatable and deleted attributes.
		// Also filter it based on the the toplevelcategory that was set as the control property.
		// Finally set the required information in a Document object and return.
		
		String siteName = request.getParameter("sitename");
		String parentCategory = request.getParameter("parentCategory");
		
		if(StringUtils.isBlank(parentCategory) || parentCategory.equalsIgnoreCase("root"))
			parentCategory = null;
		
		if(siteName.equals("null"))
		    siteName=null;
		try {

			PSCategory categoriesToReturn = PSCategoryControlUtils.getCategories(siteName,parentCategory, false, true);
		
		if(categoriesToReturn == null)
			throw new PSExtensionProcessingException
			("Either non of the categories is selectable or the category xml is empty ! PSCategoryPostExit.processResultDocument()", new PSException());
		
		String returnString = PSCategoryControlUtils.getCategoryXmlInString(categoriesToReturn);
		
			doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(returnString.trim()), false);
		} catch (PSDataServiceException | IOException | SAXException e) {
			log.error(PSExceptionUtils.getMessageForLog(e));
			log.debug(PSExceptionUtils.getDebugMessageForLog(e));
		    throw new PSExtensionProcessingException
              ("Error converting categories to xml", e);
		}

		return doc;
	}
}
