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
