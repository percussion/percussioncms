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
