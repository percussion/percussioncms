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

package com.percussion.assetmanagement.web.service;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetEditUrlRequest;
import com.percussion.assetmanagement.data.PSAssetEditor;
import com.percussion.assetmanagement.data.PSAssetFolderRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSContentEditCriteria;
import com.percussion.assetmanagement.forms.data.PSFormSummary;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.test.PSDataServiceRestClient;
import java.util.Collection;
import java.util.List;

public class PSAssetServiceRestClient extends PSDataServiceRestClient<PSAsset>
{
   public PSAssetServiceRestClient(String url)
   {
       super(PSAsset.class, url, "/Rhythmyx/services/assetmanagement/asset/");
   }

   public String createAssetWidgetRelationship(PSAssetWidgetRelationship awRel)
   {
       return POST(concatPath(getPath(),"createAssetWidgetRelationship"), objectToRequestBody(awRel));
   }

   public void clearAssetWidgetRelationship(PSAssetWidgetRelationship awRel)
   {
       POST(concatPath(getPath(), "clearAssetWidgetRelationship"), objectToRequestBody(awRel));
   }

   public PSContentEditCriteria getContentEditCriteria(
         PSAssetEditUrlRequest request)
   {
      return postObjectToPath(concatPath(getPath(), "contentEditCriteria"),
            request, PSContentEditCriteria.class);
   }

   public void addAssetToFolder(String folderPath, String assetId)
   {
       PSAssetFolderRelationship fr = new PSAssetFolderRelationship();
       fr.setAssetId(assetId);
       fr.setFolderPath(folderPath);
       postObjectToPath(concatPath(getPath(),"addAssetToFolder"), fr);
   }

   public void remove(String assetId, String folderPath)
   {
       PSAssetFolderRelationship fr = new PSAssetFolderRelationship();
       fr.setAssetId(assetId);
       fr.setFolderPath(folderPath);
       postObjectToPath(concatPath(getPath(),"remove"), fr);
   }

   public List<PSAssetDropCriteria> getWidgetAssetCriteria(String id, Boolean isPage)
   {
       return getObjectsFromPath(concatPath(getPath(), "/assetWidgetDropCriteria/",
               id, isPage.toString()),
               PSAssetDropCriteria.class);
   }

   public List<PSAssetEditor> getAssetEditors()
   {
       return getObjectsFromPath(concatPath(getPath(), "/assetEditors/"),
               PSAssetEditor.class);
   }

   public String getAssetEditUrl(String assetId)
   {
       return GET(concatPath(getPath(), "/assetEditUrl", assetId));
   }

   public void forceDelete(String assetId)
   {
       GET(concatPath(getPath(), "/forceDelete", assetId));
   }

   public void forceRemove(String assetId, String folderPath)
   {
       PSAssetFolderRelationship fr = new PSAssetFolderRelationship();
       fr.setAssetId(assetId);
       fr.setFolderPath(folderPath);
       postObjectToPath(concatPath(getPath(),"forceRemove"), fr);
   }

   public PSNoContent validateDelete(String assetId)
   {
       return getObjectFromPath(concatPath(getPath(), "/validateDelete", assetId), PSNoContent.class);
   }

   public Collection<PSFormSummary> getForms()
   {
       return getObjectsFromPath(concatPath(getPath(), "/forms"), PSFormSummary.class);
   }
     
   public PSAsset load(String assetId)
   {
       return getObjectFromPath(concatPath(getPath(), "/", assetId), PSAsset.class);
   }
  
   
   /**
    * Helper method to create (and save) an asset.
    *
    * @param name never <code>null</code>.
    * @param folderPath if <code>null</code>, the asset will not be saved to a folder.
    *
    * @return the new asset, never <code>null</code>.
    */
   public PSAsset createAsset(String name, String folderPath)
   {
       notNull(name, "name");

       PSAsset asset = new PSAsset();
       asset.getFields().put("sys_title", name + System.currentTimeMillis());
       asset.setType("percRawHtmlAsset");
       asset.getFields().put("html", "TestHTML");
       if (folderPath != null)
       {
           asset.setFolderPaths(asList(folderPath));
       }

       return save(asset);
   }
}
