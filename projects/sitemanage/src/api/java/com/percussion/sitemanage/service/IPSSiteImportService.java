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
package com.percussion.sitemanage.service;

import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;

/**
 * @author LucasPiccoli
 *
 */
public interface IPSSiteImportService
{

    public PSSiteImportCtx importSiteFromUrl(PSSite site, String userAgent) throws PSSiteImportException;
    
    /**
     * Imports a cataloged page
     * 
     * @param site the imported site, not <code>null</code>. 
     * @param pageId the cataloged page, not blank.
     * @param userAgent the user agent that was used to import the site, not blank.
     * @param context the import context. This is used through out the whole import process. 
     * This is also used to cancel the import process by {@link PSSiteImportCtx#setCanceled(boolean)}.
     * 
     * @return the import context, not <code>null</code>.
     * 
     * @throws PSSiteImportException if an error occurs.
     */
    public PSSiteImportCtx importCatalogedPage(PSSite site, String pageId, String userAgent, PSSiteImportCtx context) throws PSSiteImportException;

}
