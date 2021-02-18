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
package com.percussion.sitemanage.importer.helpers;

import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.error.PSTemplateImportException;

/**
 * IPSImportHelper - Common interface for all site import helpers
 * 
 * @author LucasPiccoli
 * 
 */
public interface IPSImportHelper
{

    public static final String COMMENTED_OUT_ELEMENT = "Comment Out Managed Element";

    public static final String COMMENTED_JS_REFERENCE_FROM_HEAD = "Remove JQuery from <head> Element";

    public static final String COMMENTED_JS_REFERENCE_FROM_BODY = "Remove JQuery from <body> Element";
    
    /**
     * Processes the content of the imported page.
     * 
     * @param pageContent The parsed content of the imported page to be
     *            processed by the helper.
     * @param context The context object containing logger, site data and common
     *            information to be shared among all helpers.
     * @throws PSSiteImportException When any kind of unexpected error occurs
     *             processing the pageContent through the helper.
     */
    public void process(PSPageContent pageContent, PSSiteImportCtx context) throws PSSiteImportException, PSTemplateImportException, IPSPageService.PSPageException;

    /**
     * Call this method to undo all the operations done by the helper in its
     * process method. If any unexpected error occurs processing pageContent
     * from a mandatory helper, then rollback is called to undo all the
     * operations done by process method.
     * 
     * @param pageContent The parsed content of the imported page needed to
     *            rollback the helper processing.
     * @param context The context object containing logger, site data and common
     *            information to be shared among all helpers.
     */
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context) throws PSDataServiceException;
    
    /**
     * This method gets import status message from the helper implementing this
     * interface. This message will be displayed to the user in import dialog,
     * to show how steps in the import process are being executed and the
     * overall progress made.
     * 
     * @param statusMessagePrefix A prefix to attach to the beginning of the
     *            status message. For example: "Importing site:",
     *            "Importing template:".
     * @return the generated status message.
     */
    public String getStatusMessage(String statusMessagePrefix);

}
