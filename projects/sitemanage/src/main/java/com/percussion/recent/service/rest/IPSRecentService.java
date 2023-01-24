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

package com.percussion.recent.service.rest;

import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.exception.PSDataServiceException;

import java.util.List;

public interface IPSRecentService
{
    List<PSItemProperties> findRecentItem(boolean ignoreArchivedItems);
    List<PSTemplateSummary> findRecentTemplate(String siteName);
    List<PSPathItem> findRecentSiteFolder(String siteName);
    List<PSPathItem> findRecentAssetFolder();
    List<PSWidgetContentType> findRecentAssetType() throws PSDataServiceException;

    void addRecentItem(String value);
    void addRecentTemplate(String siteName, String value);
    void addRecentSiteFolder(String value);
    void addRecentAssetFolder(String value);
    void addRecentAssetType(String value);
    
    void addRecentItemByUser(String userName, String value);
    void addRecentTemplateByUser(String userName, String siteName, String value);
    void addRecentSiteFolderByUser(String userName, String value);
    void addRecentAssetFolderByUser(String userName, String value);
    void addRecentAssetTypeByUser(String userName, String value);
 
    void deleteUserRecent(String user);
    void deleteSiteRecent(String siteName);

    void updateSiteNameRecent(String oldSiteName, String newSiteName);
}
