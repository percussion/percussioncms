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

package com.percussion.pathmanagement.web.service;

import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.test.PSDataServiceRestClient;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class PSPathServiceRestClient extends PSDataServiceRestClient<PSPathItem>
{
   public PSPathServiceRestClient(String url)
   {
      super(PSPathItem.class, url, "/Rhythmyx/services/pathmanagement/path/");
   }

   @Override
   protected String getGetPath(String id)
   {
      return getPath() + "item/" + id;
   }

   public PSPathItem find(String path)
   {
       return getObjectFromPath(concatPath(getPath(), "item", path));
   }
   
   public PSPathItem findById(String id)
   {
       return getObjectFromPath(concatPath(getPath(), "item/id", id));
   }
   
   public PSItemProperties findItemProperties(String path)
   {
       return getObjectFromPath(concatPath(getPath(), "itemProperties", path), PSItemProperties.class);
   }
   
   public PSFolderProperties findFolderProperties(String id)
   {
       return getObjectFromPath(concatPath(getPath(), "folderProperties", id), PSFolderProperties.class);
   }

   public void saveFolderProperties(PSFolderProperties props)
   {
       PSNoContent response = postObjectToPath(concatPath(getPath(), "saveFolderProperties"), props, PSNoContent.class);
       if (!response.getOperation().equals("saveFolderProperties"))
       {
           throw new RuntimeException("\"saveFolderProperties\" operation failed.");
       }
   }

   public List<PSPathItem> findChildren(String path)
   {
      return getObjectsFromPath(concatPath(getPath(), "folder", path));
   }
   
   public PSPagedItemList findChildren(String path, Integer startIndex, Integer maxResults, String child)
   {
       return findChildren(path, startIndex, maxResults, child, null);
   }
   
   public PSPagedItemList findChildren(String path, Integer startIndex, Integer maxResults, String child, Integer displayFormat)
   {
      return findChildren(path, startIndex, maxResults, child, displayFormat, null, null);
   }
   
   public PSPagedItemList findChildren(String path, Integer startIndex, Integer maxResults, String child, Integer displayFormat, String sortColumn, String sortOrder)
   {
      return findChildren(path, startIndex, maxResults, child, displayFormat, sortColumn, sortOrder, null, null);
   }
   
   public PSPagedItemList findChildren(String path, Integer startIndex, Integer maxResults, String child, Integer displayFormat, String sortColumn, String sortOrder, String category, String type)
   {
      String startIndexQueryParam = startIndex != null ? "startIndex=" + startIndex : StringUtils.EMPTY;
      String maxResultsQueryParam = maxResults != null ? "maxResults=" + maxResults : StringUtils.EMPTY;
      String childQueryParam = child != null ? "child=" + child : StringUtils.EMPTY;
      String displayQueryParam = displayFormat != null ? "displayFormatId=" + displayFormat : StringUtils.EMPTY;
      String sortColumnParam = sortColumn != null ? "sortColumn=" + sortColumn : StringUtils.EMPTY;
      String sortOrderParam = sortOrder != null ? "sortOrder=" + sortOrder : StringUtils.EMPTY;
      String categoryParam = category != null ? "category=" + category : StringUtils.EMPTY;
      String typeParam = type != null ? "type=" + type : StringUtils.EMPTY;
      
      String param = addQueryParam(StringUtils.EMPTY, startIndexQueryParam);
      param = addQueryParam(param, maxResultsQueryParam);
      param = addQueryParam(param, childQueryParam);
      param = addQueryParam(param, displayQueryParam);
      param = addQueryParam(param, sortColumnParam);
      param = addQueryParam(param, sortOrderParam);
      param = addQueryParam(param, categoryParam);
      param = addQueryParam(param, typeParam);
      
      return getObjectFromPath(concatPath(getPath(), "paginatedFolder", path + param), PSPagedItemList.class);
   }

   private String addQueryParam(String existingParameterList, String newQueryParam)
   {
       if (StringUtils.isEmpty(newQueryParam))
           return existingParameterList;
       
       if (StringUtils.isEmpty(existingParameterList))
           return "?" + newQueryParam;
       
       return existingParameterList + "&" + newQueryParam;
   }
   
   public PSPathItem findRoot()
   {
      return getObjectFromPath(concatPath(getPath(), "root"));
   }
   
   public PSPathItem addFolder(String path)
   {
       return getObjectFromPath(concatPath(getPath(), "addFolder", path)); 
   }
   
   public PSPathItem addNewFolder(String path)
   {
       return getObjectFromPath(concatPath(getPath(), "addNewFolder", path));
   }
   
   public String deleteFolder(PSDeleteFolderCriteria criteria)
   {
       return postObjectToPath(concatPath(getPath(), "deleteFolder"), criteria);
   }

   public PSPathItem renameFolder(PSRenameFolderItem item)
   {
       return postObjectToPath(concatPath(getPath(), "renameFolder"), item, PSPathItem.class);
   }
   
   public void moveItem(PSMoveFolderItem request)
   {
       PSNoContent response = postObjectToPath(concatPath(getPath(), "moveItem"), request, PSNoContent.class);
       if (!response.getOperation().equals("moveItem"))
       {
           throw new RuntimeException("\"moveItem\" operation failed.");
       }
   }
   
   public String validateFolderDelete(String path)
   {
       return GET(concatPath(getPath(), "validateFolderDelete", path));
   }
   
   public List<PSItemProperties> findItemProperties(PSItemByWfStateRequest request)
   {
       return postObjectToPathAndGetObjects(concatPath(getPath(), "item/wfState"), request, PSItemProperties.class);
   }
   
   public String findLastExistingPath(String path)
   {
       return GET(concatPath(getPath(), "lastExisting", path));
   }
}
