/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.server.webservices.crosssite;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;

/**
 * This class handles the modification of the AA relationships of the dependent
 * item based on the items existence in multiple site folders for move action.
 * Implements mainly the base class method
 * {@link #modifyLinks(PSAaFolderDependent)}.
 */
public class PSCrossSiteFolderMoveActionProcessor extends
   PSCrossSiteFolderActionProcessor
{
   protected PSCrossSiteFolderMoveActionData data;
   
   public PSCrossSiteFolderMoveActionProcessor(PSCrossSiteFolderMoveActionData data)
   {
      super(data);
      this.data = data;
   }

   /**
    * Only ctor. Invokes base class version. Does additional initialization.
    *
    */
   public PSCrossSiteFolderMoveActionProcessor(
      PSLocator sourceFolderId, List<PSLocator> children,
      PSLocator targetFolderId) throws PSCmsException, PSNotFoundException {
      super(sourceFolderId, children);
      if (targetFolderId == null)
      {
         throw new IllegalArgumentException("targetFolderId must not be null");
      }
      PSCrossSiteFolderMoveActionData theData = getData();
      log.debug("Initializing move action processor...");
      theData.setTargetFolderId(targetFolderId);
      log.debug("Target folderid is:" + targetFolderId.getId());
      Integer[] sites = computeSiteForFolder(theData.getTargetFolderId());
      theData.setTargetSiteIds(asList(sites));
      sort(theData.getTargetSiteIds());
      if (sites.length == 0)
         theData.setTargetSiteId(null);
      else
      {
         if (sites.length > 1)
         {
            log.warn("folder with folderid = {}" , theData.getTargetFolderId().getId()
               + "resolves to multiple sites: {}" , sites);
            log.warn("Using the first in the list");
         }
         theData.setTargetSiteId(sites[0]);
      }
      theData.setActionCategory(evaluateActionCategory());
      log.debug("The move action category based on the data is: "
         + theData.getActionCategory().toString());
      buildDescendents();
   }
   
   public PSCrossSiteFolderMoveActionData getData() {
      if (data == null) {
         PSCrossSiteFolderMoveActionData d = new PSCrossSiteFolderMoveActionData();
         data = d;
      }
      return data;
   }

   @Override
   protected boolean skipFolder(PSLocator folder, PSRelationshipSet rs, int depth)
   {
      if (getData().getActionCategory() == 
         PSMoveActionCategoryEnum.ACTION_CATEGORY_REORGANIZE) {
         return true;
      }
      return false;
   }

   /**
    * See move action matrix in the functional specification for more details of
    * each case.
    */
   @Override
   public void modifyLinks(PSAaFolderDependent depItem) throws PSCmsException
   {
      log.debug("Modifying AA relationships for the dependent item: "
         + depItem.getItem().getId() + "...");
      Iterator<PSRelationship> iter = depItem.getAaRelationships().iterator();
      /*
       * IMPORTANT: The target folderid for the relationships will be target
       * folder id for the immediate children and the parent folder id for the
       * grand children.
       */
      PSLocator targetFolderid = depItem.isGrandChild() ? depItem
         .getSrcFolder() : data.getTargetFolderId();
      //
      while (iter.hasNext())
      {
         PSRelationship rel = iter.next();
         log.debug("processing relationship: " + rel.toString());
         switch (data.getActionCategory())
         {
            case ACTION_CATEGORY_REORGANIZE:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, String.valueOf(targetFolderid
                        .getId()));
                     break;
                  default:
                     break;
               }
               break;
            case ACTION_CATEGORY_RECLASSIFY:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                     setSiteId(rel);
                     setFolderId(rel, depItem, String
                        .valueOf(targetFolderid.getId()));
                     break;
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, String.valueOf(targetFolderid
                        .getId()));
                     break;
                  case CROSSSITE_LINK_SITE_ONLY:
                     if(depItem.isLastOnSite(data.getSourceSiteId()))
                     {
                        setSiteId(rel);
                     }
                     break;
                  default:
                     break;
               }

               break;
            case ACTION_CATEGORY_ARCHIVE:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, String.valueOf(targetFolderid
                        .getId()));
                     break;
                  default:
                     break;
               }

               break;

            case ACTION_CATEGORY_DEPLOY:
               switch (getCrossSiteLinkType(rel))
               {
                  case CROSSSITE_LINK_BOTH:
                     setSiteId(rel);
                     setFolderId(rel, depItem, String.valueOf(targetFolderid
                        .getId()));
                     break;
                  case CROSSSITE_LINK_FOLDER_ONLY:
                     setFolderId(rel, depItem, String.valueOf(targetFolderid
                        .getId()));
                     break;
                  case CROSSSITE_LINK_SITE_ONLY:
                     setSiteId(rel);
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
    * Set the site id property of the supplied relationship to the target
    * siteid. The modification is performed only if the source siteid of the
    * dependent item is empty or matches with the siteid property of the
    * supplied relationship. The idea of this is not to touch a relationship
    * that does not have the item's source siteid as its property.
    * 
    * @param rel relationship in which the siteid is to be set, assumed not
    * <code>null</code>.
    * @return <code>true</code> if the property needed to set,
    * <code>false</code> otherwise.
    */
   private boolean setSiteId(PSRelationship rel)
   {
      String newSiteId = (data.getTargetSiteId() == null) ? "" : data.getTargetSiteId()
         .toString();
      String srcSiteId = (data.getSourceSiteId() == null) ? "" : data.getSourceSiteId()
         .toString();
      String oldSiteid = rel.getProperty(IPSHtmlParameters.SYS_SITEID);
      if (srcSiteId.length() == 0 || srcSiteId.equals(oldSiteid))
      {
         rel.setProperty(IPSHtmlParameters.SYS_SITEID, newSiteId);
         return true;
      }
      return false;
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
      root.setAttribute("ationCategory", data.getActionCategory().getName());
      PSXmlDocumentBuilder.addElement(doc, root, "targetFolderId", ""
         + ((data.getTargetFolderId() == null) ? "" : "" + data.getTargetFolderId().getId()));
      PSXmlDocumentBuilder.addElement(doc, root, "targetSiteId", ""
         + ((data.getTargetSiteId() == null) ? "" : "" + data.getTargetSiteId()));
      return doc;
   }

   /**
    * Evaluate the move action category based on the source and traget site ids.
    * Assume the source and target siteids are evaluated already from the source
    * and traget folders.
    * 
    * @return appropriate enumeration value.
    */
   private PSMoveActionCategoryEnum evaluateActionCategory()
   {
      if (data.getSourceSiteId() == null && data.getTargetSiteId() == null)
      {
         return PSMoveActionCategoryEnum.ACTION_CATEGORY_NONE;
      }
      if (data.getSourceSiteId() != null && data.getTargetSiteId() != null)
      {
         Collection<Integer> c = getSharedSiteIds();
         if (!c.isEmpty())
         {
            return PSMoveActionCategoryEnum.ACTION_CATEGORY_REORGANIZE;
         }
         return PSMoveActionCategoryEnum.ACTION_CATEGORY_RECLASSIFY;
      }
      if (data.getSourceSiteId() == null)
         return PSMoveActionCategoryEnum.ACTION_CATEGORY_DEPLOY;

      return PSMoveActionCategoryEnum.ACTION_CATEGORY_ARCHIVE;
   }

   private Collection<Integer> getSharedSiteIds()
   {
      List<Integer> sourceSites = data.getSourceSiteIds();
      List<Integer> targetSites = data.getTargetSiteIds();
      @SuppressWarnings("unchecked")
      Collection<Integer> c = CollectionUtils.intersection(sourceSites,
            targetSites);
      return c;
   }


   /**
    * Name of the action this processor handles.
    */
   public static final String ACTION_NAME = "Move";
}
