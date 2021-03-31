/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.jexl;
import javax.jcr.RepositoryException;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.utils.guid.IPSGuid;
public interface IPSOObjectFinder
{
   /**
    * Gets the Legacy Component Summary for an item by GUID. 
    * @param guid the item GUID.
    * @return the Component Summary for the item. Never <code>null</code>
    * @throws PSException when the item is not found. 
    */
   @IPSJexlMethod(description = "get the Legacy Component Summary for an item", params = {@IPSJexlParam(name = "guid", description = "the item GUID")})
   public PSComponentSummary getComponentSummary(IPSGuid guid)
         throws PSException;
   /**
    * Gets the legacy component summary by content id 
    * @param contentid the content id
    * @return the Component Summary for the item. Never <code>null</code>
    * @throws PSException
    */
   @IPSJexlMethod(description = "get the Legacy Component Summary for an item", params = {@IPSJexlParam(name = "content", description = "the content id")})
   public PSComponentSummary getComponentSummaryById(String contentid)
         throws PSException;
   /**
    * Gets the content type summary for a content type by GUID. 
    * @param guid the Content Type GUID
    * @return the content type summary or <code>null</code> if the 
    * content type is not found. 
    */
   @IPSJexlMethod(description = "get the content type summary for a specified type", params = {@IPSJexlParam(name = "guid", description = "the content type GUID")})
   public PSContentTypeSummary getContentTypeSummary(IPSGuid guid);
   /**
    * Gets the JSESSIONID value for the current session.
    * @return the jsessionid
    * @deprecated in 6.5 and later, replaced by PSSessionUtils.getJSessionId(). 
    */
   @IPSJexlMethod(description = "Get the JSESSIONID value for the current request", params = {})
   public String getJSessionId();
   /**
    * Gets the PSSessionId for the current session. 
    * @return the pssessionid.
    */
   @IPSJexlMethod(description = "Get the PSSESSIONID value for the current request", params = {})
   public String getPSSessionId();
   /**
    * Gets the users current locale.
    * @return the users current locale, or <code>null</code> if none is defined. 
    */
   @IPSJexlMethod(description = "get the users current locale", params = {})
   public String getUserLocale();
   /**
    * Gets the name of the current user community.
    * @return the user community name, or <code>null</code> if none
    * is defined. 
    */
   @IPSJexlMethod(description = "get the users current community name", params = {})
   public String getUserCommunity();
   /**
    * Gets the users current community id. 
    * @return the community id, or <code>null</code> if none is defined. 
    */
   @IPSJexlMethod(description = "get the users current community id", params = {})
   public String getUserCommunityId();
   /**
    * Get the GUID for a give content id and revision.
    * @param contentid the content id
    * @param revision the revision
    * @return the GUID. Never <code>null</code>
    */
   @IPSJexlMethod(description = "get the GUID by Content Id and Revision", params = {
         @IPSJexlParam(name = "contentid", description = "the content id"),
         @IPSJexlParam(name = "revision", description = "the revision")})
   public IPSGuid getGuidById(String contentid, String revision);
   /**
    * Gets the GUID for a content id.  The revision independent guid is 
    * returned. 
    * @param contentid the content id; 
    * @return the GUID. Never <code>null</code>
    */
   @IPSJexlMethod(description = "get the GUID by Content Id", params = {@IPSJexlParam(name = "contentid", description = "the content id")})
   public IPSGuid getGuidById(String contentid);
   /**
    * Gets the Node for a content item by GUID. 
    * @param guid the content item GUID
    * @return the Node, or <code>null</code> if the node was not found. 
    * @throws RepositoryException
    */
   @IPSJexlMethod(description = "get the node for a particular guid", params = {@IPSJexlParam(name = "guid", description = "the GUID for the item")})
   public IPSNode getNodeByGuid(IPSGuid guid) throws RepositoryException;
   /**
    * Gets the guid for a site by id. 
    * @param siteid the site id
    * @return the guid. Never <code>null</code>
    */
   @IPSJexlMethod(description = "get the site guid for a given id", params = {@IPSJexlParam(name = "siteid", description = "the id for the site")})
   public IPSGuid getSiteGuid(int siteid);
   /**
    * Get the template guid for given id
    * @param templateid the template id
    * @return the template guid. 
    */
   @IPSJexlMethod(description = "get the template guid for a given id", params = {@IPSJexlParam(name = "template", description = "the id for the template")})
   public IPSGuid getTemplateGuid(int templateid);

   /**
    * Get the Community Name for given id
    * @param communityId the comm id
    * @return the community name
    */
   @IPSJexlMethod(description="get the community name for a  given community id", 
	         params={@IPSJexlParam(name="communityId",description="the id for the community")})
	        public String getCommunityName(int communityId);
   
}