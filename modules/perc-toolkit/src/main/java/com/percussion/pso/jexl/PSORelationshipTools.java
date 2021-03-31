/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.sandbox PSOFolderTools.java
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * @author MikeStarck
 *
 */
package com.percussion.pso.jexl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.relationships.IPSOParentFinder;
import com.percussion.pso.relationships.PSOParentFinder;
import com.percussion.pso.utils.SimplifyParameters;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * 
 *
 * @author MikeStarck
 *
 */
public class PSORelationshipTools extends PSJexlUtilBase implements IPSJexlExpression 
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSORelationshipTools.class);
   private static IPSGuidManager gmgr = null;
   
   /**
    * 
    */
   public PSORelationshipTools()
   {
      super();
   }
   
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
      }
   }
   
   
   
   @IPSJexlMethod(description="get the dependents of this item of a certain content type", 
         params={
		@IPSJexlParam(name="itemId", description="the item GUID"),
		@IPSJexlParam(name="contenttypename", type="String", description="the name of the content type we are testing for"),
		@IPSJexlParam(name="userName", type="String", description="the userName with which to make the request")})
   public List<PSItemSummary> getRelationships(IPSGuid itemId, String contenttypename, String userName) 

   throws PSErrorException, PSExtensionProcessingException   
   {
      String errmsg; 
      if(itemId == null || contenttypename == null || userName == null)
      {
         errmsg = "No dependents found for null guid or null contenttypename or null user"; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();

      IPSGuid contenttypeid;
      List<PSContentTypeSummary> ctypes = null;
      try
      {
         ctypes = cws.loadContentTypes(contenttypename);
         if (ctypes.size() > 0)
         {
        	 contenttypeid = ctypes.get(0).getGuid();
        	 }
         else
         {
             log.warn("Content type " + contenttypename + " not found"); 
        	 return new ArrayList<PSItemSummary>();
         }
      } catch (Exception e1)
      {
         log.error("Cannot load content types", e1); 
         throw new PSExtensionProcessingException(PSORelationshipTools.class.getName(), e1);
      } 
      
      List<PSItemSummary> dependents = null; 
      try
      {
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setDependentContentTypeId(contenttypeid.longValue()); 
         dependents = cws.findDependents(
				itemId, 
				filter,
                false);
      } catch (Exception e)
      {
        log.error("Unexpected exception " + e.getMessage(), e );
        throw new PSExtensionProcessingException(this.getClass().getCanonicalName(), e); 
      } 
      if(dependents.isEmpty())
      {
         errmsg = "cannot find dependents for " + itemId; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      return dependents;
   }
   
   @IPSJexlMethod(description="find all parent items", 
         params={@IPSJexlParam(name="guid",description="the current item guid"),
          @IPSJexlParam(name="slotName",description="the slot name")},returns="a list of all parent GUIDs")
   public List<IPSGuid> findAllParentIds(IPSGuid guid, String slotName)
      throws Exception
   {
      initServices();
      String id = gmgr.makeLocator(guid).getPart(PSLocator.KEY_ID); 
      return findAllParentIds(id, slotName);
   }
   
   @IPSJexlMethod(description="find all parent items", 
         params={@IPSJexlParam(name="contentid",description="the content id of the current item"),
          @IPSJexlParam(name="slotName",description="the slot name")},returns="a list of all parent GUIDs")
   public List<IPSGuid> findAllParentIds(String contentid, String slotName)
      throws Exception
   {
      initServices();
     
      IPSOParentFinder relFinder = new PSOParentFinder();
      Set<PSLocator> parentLocs = relFinder.findAllParents(contentid, slotName);
      List<IPSGuid> guids = new ArrayList<IPSGuid>(parentLocs.size()); 
      for(PSLocator loc : parentLocs)
      {
         guids.add(gmgr.makeGuid(loc)); 
      }
      return guids; 
   }

   @IPSJexlMethod(description="find parent items", 
         params={@IPSJexlParam(name="guid",description="the current item guid"),
          @IPSJexlParam(name="slotName",description="the slot name"),
          @IPSJexlParam(name="usePublic", description="use last public revision?")},
          returns="a list of all parent GUIDs")
   public List<IPSGuid> findParentIds(IPSGuid guid, String slotName, boolean usePublic)
      throws Exception
   {
      initServices();
      String id = gmgr.makeLocator(guid).getPart(PSLocator.KEY_ID); 
      return findParentIds(id, slotName, usePublic);
   }

   
   @IPSJexlMethod(description="find parent items", 
         params={@IPSJexlParam(name="contentid",description="the content id of the current item"),
          @IPSJexlParam(name="slotName",description="the slot name"),
          @IPSJexlParam(name="usePublic", description="use last public revision?")},
          returns="a list of all parent GUIDs")
   public List<IPSGuid> findParentIds(String contentid, String slotName, boolean usePublic)
      throws Exception
   {
      initServices();
     
      IPSOParentFinder relFinder = new PSOParentFinder();
      Set<PSLocator> parentLocs = relFinder.findParents(contentid, slotName, usePublic);
      List<IPSGuid> guids = new ArrayList<IPSGuid>(parentLocs.size()); 
      for(PSLocator loc : parentLocs)
      {
         guids.add(gmgr.makeGuid(loc)); 
      }
      return guids; 
   }
   /**
    * Is this page referenced in the landing page slot.
    * Will return true if the specified content id is referenced in a landing page slot.
    * Note that this function is limited to PUBLIC navons. Navons in QuickEdit will not 
    * consider the relationships of the current revision, only the  relationships of the 
    * last public revision. 
    * @param contentid the content id
    * @return <code>true</code> if this page has a public navon parent in the landing page slot. 
    * @throws Exception
    */
   @IPSJexlMethod(description="is this page referenced in the landing page slot",
         params={@IPSJexlParam(name="contentid", description="the content id of the current page")})
   public boolean isLandingPage(String contentid) throws Exception
   {
      IPSOParentFinder relFinder = new PSOParentFinder();
      PSNavConfig nc = PSNavConfig.getInstance(); 
      String landingSlot = nc.getLandingPageRelationship(); 
      Set<PSLocator> parents = relFinder.findParents(contentid, landingSlot, true);
      return !parents.isEmpty(); 
   }
   
   /**
    * Is this page referenced in the landing page slot in the current folder. 
    * Will return true if the specified content id is referenced in a landing page slot of the
    * navon in the current folder
    * @param contentid the content id.
    * @param folderid the folder id
    * @return <code>true</code> if this page is the landing page for the specified folder navon. 
    */
   @IPSJexlMethod(description="is this page referenced in the landing page slot in the current folder",
         params={@IPSJexlParam(name="contentid", description="the content id of the current page"),
          @IPSJexlParam(name="folderid", description="the folder id of the current folder")})
   public boolean isLandingPageInFolder(String contentid, String folderid) throws Exception
   {
      initServices();
      IPSOParentFinder relFinder = new PSOParentFinder();
      PSNavConfig nc = PSNavConfig.getInstance(); 
      String landingSlot = nc.getLandingPageRelationship();
      Set<PSLocator> parents = relFinder.findParents(contentid, landingSlot, true);
      
      if(parents.isEmpty())
      { //no parents at all 
         return false; 
      }
      //it's a landing page, check if it is in this folder.
      PSONavTools navTools = new PSONavTools();
      IPSNode navon = navTools.findNavNodeForFolder(folderid);
      if(navon == null)
      {
         log.warn("no navon found for folder id " + folderid);
         return false; 
      }
      int navonId = gmgr.makeLocator(navon.getGuid()).getId();
      for(PSLocator loc : parents)
      {
         if(navonId == loc.getId())
         {//navon id matches parent id
            log.trace("found matching parent navon " + loc.getId()); 
            return true; 
         }
      }
      return false;  
   }
   /**
    * Is this page referenced in the landing page slot. Convenience method for 
    * {@link #isLandingPage(String)}
    * @param guid the guid
    * @return code>true</code> if this page has a public navon parent in the landing page slot.
    * @throws Exception
    */
   @IPSJexlMethod(description="is this page referenced in the landing page slot",
         params={@IPSJexlParam(name="guid", description="the current item guid")})
   public boolean isLandingPageGuid(IPSGuid guid) throws Exception
   {
      initServices();
      String id = gmgr.makeLocator(guid).getPart(PSLocator.KEY_ID); 
      return isLandingPage(id); 
   }

   /**
    * Is this page referenced in the landing page slot for the current folder.
    * @param contentGuid the Item guid
    * @param folderGuid the Folder guid
    * @return code>true</code> if this page has a public navon parent in the landing page slot.
    * @throws Exception
    */
   @IPSJexlMethod(description="is this page referenced in the landing page slot",
         params={@IPSJexlParam(name="contentGuid", description="the current item guid"),
          @IPSJexlParam(name="folderGuid", description="the folder guid")})
   public boolean isLandingPageInFolderGuid(IPSGuid contentGuid, IPSGuid folderGuid) throws Exception
   {
      initServices();
      String contentid = gmgr.makeLocator(contentGuid).getPart(PSLocator.KEY_ID);
      String folderid = gmgr.makeLocator(folderGuid).getPart(PSLocator.KEY_ID); 
      return isLandingPageInFolder(contentid, folderid); 
   }

   @IPSJexlMethod(description="is this page referenced in the landing page slot",
         params={@IPSJexlParam(name="contentid", description="the current item id"),
          @IPSJexlParam(name="folderid", description="the folder id")})
   public boolean isLandingPageInFolderInt(int contentid, int folderid) throws Exception
   {
      String cid = String.valueOf(contentid);
      String fid = String.valueOf(folderid); 
      return isLandingPageInFolder(cid, fid); 
   }
   
   /**
    * Checks if an item has any non-public ancestors.  Convenience method for 
    * {@link #hasOnlyPublicAncestors(String, String, String)}
    * @param guid the current content item guid. 
    * @param slotName the slot name
    * @return <code>true</code> if all ancestors in the slot are public. 
    * @throws Exception
    */
   @IPSJexlMethod(description="Does this item have any non-public ancestors",
         params={@IPSJexlParam(name="guid",description="the content item GUID"),
                 @IPSJexlParam(name="slotName",description="slotName")})
   public boolean hasOnlyPublicAncestors(IPSGuid guid, String slotName)
      throws Exception
   {
      initServices();
      String contentid = gmgr.makeLocator(guid).getPart(PSLocator.KEY_ID);
      return hasOnlyPublicAncestors(contentid, slotName, null); 
   }
   /**
    * Checks if an item has any non-public ancestors.  The direct and indirect ancestors 
    * in the specified slot are scanned to make sure that they are in a workflow state 
    * consistent with the supplied validFlags. 
    * @see PSOParentFinder#hasOnlyPublicAncestors(String, String, List)
    * @param contentId the content id for the current item. 
    * @param slotName the slot name to scan
    * @param validFlags the validity flags considered public as a comma separated list. Defaults to 
    * &quot;y,i&quot; 
    * @return <code>true</code> if all ancestors in the slot are public. 
    * @throws Exception
    */
   @IPSJexlMethod(description="Does this item have any non-public ancestors",
         params={@IPSJexlParam(name="contentId",description="content id for item"),
                 @IPSJexlParam(name="slotName",description="slotName"),
                 @IPSJexlParam(name="validFlags",
                       description="validity flags considered public. Defaults to y,i")})
   public boolean hasOnlyPublicAncestors(String contentId, String slotName, String validFlags)
      throws Exception
   {
      if(StringUtils.isBlank(validFlags))
      {
         validFlags = "y,i"; 
      }
      List<String> vfList = SimplifyParameters.getValueAsList(validFlags);
      IPSOParentFinder relFinder = new PSOParentFinder();

      return relFinder.hasOnlyPublicAncestors(contentId, slotName, vfList);
      
   }
	
   @IPSJexlMethod(description="Return a list of slots that are populated for this item",
	         params={@IPSJexlParam(name="owner",description="guid for this item")})
   public List<String> getItemSlots(IPSGuid owner) 
   		throws Exception {
	   initServices();
	   PSRelationshipFilter filter = new PSRelationshipFilter();
	   IPSContentWs cws = PSContentWsLocator.getContentWebservice();
	   PSLocator ownerLoc = gmgr.makeLocator(owner);
	   filter.setOwner(ownerLoc);
	   List<String> slotnames = new ArrayList<String>();
	   for (PSAaRelationship rel : cws.loadContentRelations(filter,true)) {
		   if (!slotnames.contains(rel.getSlotName())) {
			   slotnames.add(rel.getSlotName());
		   }
	   }
	   return slotnames;
   }

   /**
    * @param gmgr the gmgr to set
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      PSORelationshipTools.gmgr = gmgr;
   }
}
