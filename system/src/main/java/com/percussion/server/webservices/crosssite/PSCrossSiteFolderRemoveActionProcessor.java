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

package com.percussion.server.webservices.crosssite;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * This class handles the modifcation of the AA relationships of the dependent
 * item based on the items existence in multiple site folders for remove action.
 * Implements the base class method {@link #modifyLinks(PSAaFolderDependent)}.
 */
public class PSCrossSiteFolderRemoveActionProcessor extends
   PSCrossSiteFolderActionProcessor
{
   
   
   public PSCrossSiteFolderRemoveActionProcessor(
         PSCrossSiteFolderActionData data)
   {
      super(data);
   }


   /**
    * Only ctor. Invokes base class version.
    *
    */
   public PSCrossSiteFolderRemoveActionProcessor(
      PSLocator sourceFolderId, List<PSLocator> children) throws PSCmsException, PSNotFoundException {
      super(sourceFolderId, children);
      buildDescendents();
   }

   
   public PSCrossSiteFolderActionData getData() {
      if (data == null) {
         PSCrossSiteFolderRemoveActionData d = new PSCrossSiteFolderRemoveActionData();
         data = d;
      }
      return data;
   }

   
   /**
    * See remove action matrix in the functional specification for more details
    * of each case.
    */
   @Override
   public void modifyLinks(PSAaFolderDependent depItem) {
      log.debug("Modifying AA relationships for the dependent item: {}",
         depItem.getItem().getId());
      Iterator<PSRelationship> iter = depItem.getAaRelationships().iterator();
      PSRemoveActionCategoryEnum actionCategory = evaluateActionCategory(depItem);
      while (iter.hasNext())
      {
         PSRelationship rel = iter.next();
         log.debug("processing relationship: {}",  rel.toString());
         switch (actionCategory)
         {
            case ACTION_CATEGORY_ONLY_SITEFOLDER:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, "");
                     break;
                  case CROSSSITE_LINK_SITE_ONLY:
                     isSiteIdMatches(rel);
                     break;
                  default:
                     break;
               }
               break;
            case ACTION_CATEGORY_SAMESITE_MULTIPLE_FOLDERS:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, "");
                     break;
                  default:
                     break;
               }

               break;
            case ACTION_CATEGORY_MULTIPLE_SITES:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, "");
                     break;
                  case CROSSSITE_LINK_SITE_ONLY:
                     isSiteIdMatches(rel);
                     break;
                  default:
                     break;
               }

               break;

            case ACTION_CATEGORY_NONSITE_FOLDER:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, "");
                     break;
                  default:
                     break;
               }
               break;
            default:
               break;
         }

      }
   }

   /**
    * Helper to find if the supplied relationship's siteid property value
    * matches with the source sieid of the item in process.
    * 
    * @param rel relationship object, assumed not <code>null</code>.
    * @return <code>true</code> if the property value matches with source
    * siteid, <code>false</code> otherwise.
    */
   private boolean isSiteIdMatches(PSRelationship rel)
   {
      return data.getSourceSiteId().toString().equals(
         rel.getProperty(IPSHtmlParameters.SYS_SITEID));
   }

   @Override
   public String getActionName()
   {
      return ACTION_NAME;
   }

   @Override
   public Document getProcessReport()
   {
      Document doc = super.getProcessReport();
      Element root = doc.getDocumentElement();
      root.setAttribute("folderAction", getActionName());
      return doc;
   }

   /**
    * Evaluate the remove move action category based on the dependent items
    * existece in site folders.
    * 
    * @param depItem dependent item that is being removed from the site folder,
    * must not be <code>null</code>.
    * @return appropriate enumeration value.
    */

   private PSRemoveActionCategoryEnum evaluateActionCategory(
      PSAaFolderDependent depItem)
   {
      if (depItem == null)
      {
         throw new IllegalArgumentException("depItem must not be null");
      }
      // Source folder is not a site folder - easy
      if (data.getSourceSiteId() == null)
         return PSRemoveActionCategoryEnum.ACTION_CATEGORY_NONSITE_FOLDER;

      // It is the last one on the source site
      if (depItem.isLastOnSite(data.getSourceSiteId()))
      {
         // but exists on other sites
         if (depItem.getSites().length > 1)
            return PSRemoveActionCategoryEnum.ACTION_CATEGORY_MULTIPLE_SITES;
         // does not exist on any other site
         return PSRemoveActionCategoryEnum.ACTION_CATEGORY_ONLY_SITEFOLDER;
      }
      // remaining same site but multiple folders
      return PSRemoveActionCategoryEnum.ACTION_CATEGORY_SAMESITE_MULTIPLE_FOLDERS;
   }

   /**
    * Name of the action this processor handles.
    */
   public static final String ACTION_NAME = "Remove";
}
