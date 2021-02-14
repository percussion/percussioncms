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

package com.percussion.recent.service.rest;

import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.share.data.PSItemProperties;

import java.util.List;

public interface IPSRecentService
{
    List<PSItemProperties> findRecentItem(boolean ignoreArchivedItems);
    List<PSTemplateSummary> findRecentTemplate(String siteName);
    List<PSPathItem> findRecentSiteFolder(String siteName);
    List<PSPathItem> findRecentAssetFolder();
    List<PSWidgetContentType> findRecentAssetType();

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
