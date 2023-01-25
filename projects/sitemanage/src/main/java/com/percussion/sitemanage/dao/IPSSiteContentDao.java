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

package com.percussion.sitemanage.dao;

import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteSummary;

public interface IPSSiteContentDao {
    /**
     * The name of the home page item created automatically during site creation.
     */
    String HOME_PAGE_NAME = "index.html";

    void createRelatedItems(PSSite site);

    void copy(PSSite srcSite, PSSite destSite);

    PSPage getHomePage(PSSiteSummary site) throws PSNavException, PSDataServiceException;

    String getNavTitle(PSSiteSummary siteSummary) throws PSNavException, PSDataServiceException;

    void loadTemplateInfo(PSSite site) throws PSDataServiceException;

    void deleteRelatedItems(PSSiteSummary summary) throws IPSGenericDao.DeleteException;
}
