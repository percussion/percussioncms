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

package com.percussion.category.extension;

import com.percussion.category.data.PSCategory;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


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
			log.error(e.getMessage());
			log.debug(e.getMessage(),e);
		    throw new PSExtensionProcessingException
              ("Error converting categories to xml", e);
		}

		return doc;
	}
}
