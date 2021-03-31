/*******************************************************************************
 * (c) 2005-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * @author davidbenua
 *
 */
package com.percussion.pso.jexl;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

/**
 * JEXL function for locating various legacy objects by GUID. 
 * These functions are commonly available in the Java API, but 
 * not directly accessible in JEXL. 
 *
 * @author davidbenua
 *
 */
public class PSOObjectFinder extends PSJexlUtilBase
      implements
      IPSJexlExpression, IPSOObjectFinder
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOObjectFinder.class);
 
   /**
    * Content Web Service pointer. 
    */
   private static IPSContentWs cws = null; 
   
   private static IPSGuidManager gmgr = null; 
   
   private static IPSContentMgr cmgr = null; 
   
   private static IPSSecurityWs sws = null; 

   private static IPSWorkflowService wf=null;
   /**
    * 
    */
   public PSOObjectFinder()
   {
      super();
      
   }
   
   /**
    * Initialize Java services. Must be called before any 
    * Java Services are accessed. 
    */
   private static void initServices()
   {
	  if(cws == null)
		cws = PSContentWsLocator.getContentWebservice();

	  if(wf== null)
		wf = PSWorkflowServiceLocator.getWorkflowService();
    
	  if(gmgr==null)
		gmgr = PSGuidManagerLocator.getGuidMgr(); 
      
	  if(cmgr==null)
		cmgr = PSContentMgrLocator.getContentMgr(); 
      
	  if(sws==null)
    	 sws = PSSecurityWsLocator.getSecurityWebservice();
      
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getComponentSummary(com.percussion.utils.guid.IPSGuid)
    */
   @IPSJexlMethod(description="get the Legacy Component Summary for an item",
         params={@IPSJexlParam(name="guid",description="the item GUID")})
   public PSComponentSummary getComponentSummary(IPSGuid guid) throws PSException
   {
      return PSOItemSummaryFinder.getSummary(guid); 
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getComponentSummaryById(java.lang.String)
    */
   @IPSJexlMethod(description="get the Legacy Component Summary for an item",
         params={@IPSJexlParam(name="content",description="the content id")})
   public PSComponentSummary getComponentSummaryById(String contentid) throws PSException
   {
      return PSOItemSummaryFinder.getSummary(contentid); 
   }
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getContentTypeSummary(com.percussion.utils.guid.IPSGuid)
    */
   @IPSJexlMethod(description="get the content type summary for a specified type",
         params={@IPSJexlParam(name="guid",description="the content type GUID")}) 
   public PSContentTypeSummary getContentTypeSummary(IPSGuid guid)
   {
      initServices();
      List<PSContentTypeSummary> ctypes = cws.loadContentTypes(null); 
      for(PSContentTypeSummary ctype : ctypes)
      {
         if(ctype.getGuid().longValue() == guid.longValue())
         {
            log.debug("found Content type" + ctype.getName()); 
            return ctype; 
         }
      }
      return null;
   }
 
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getJSessionId()
    */
   @IPSJexlMethod(description="Get the JSESSIONID value for the current request",
      params={})
   public String getJSessionId()
   {
       String jsession = PSRequestInfo.
           getRequestInfo(PSRequestInfo.KEY_JSESSIONID).toString();
       log.debug("JSESSIONID=" + jsession);
       return jsession;
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getPSSessionId()
    */
   @IPSJexlMethod(description="Get the PSSESSIONID value for the current request",
         params={})
   public String getPSSessionId()
   {
      PSRequest req = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String sessionid = req.getUserSessionId();
      log.debug("PSSessionId=" + sessionid ); 
      return sessionid;
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getUserLocale()
    */
   @IPSJexlMethod(description="get the users current locale",
         params={})
   public String getUserLocale()
   {
      PSUserSession session = getSession();
      Object obj = session.getPrivateObject(IPSHtmlParameters.SYS_LANG);
      if(obj != null)
      {
         return obj.toString(); 
      }
      return null;
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getUserCommunity()
    */
   @IPSJexlMethod(description="get the users current community name",
         params={})
   public String getUserCommunity()
   {
      PSUserSession session = getSession();
      return session.getUserCurrentCommunity();       
   }

   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getUserCommunityId()
    */
   @IPSJexlMethod(description="get the users current community id",
         params={})
   public String getUserCommunityId()
   {
      PSUserSession session = getSession();
      Object obj = session.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      if(obj != null)
      {
         return obj.toString(); 
      }
      return null;
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getGuidById(java.lang.String, java.lang.String)
    */
   @IPSJexlMethod(description="get the GUID by Content Id and Revision",
         params={@IPSJexlParam(name="contentid",description="the content id"),
                 @IPSJexlParam(name="revision", description="the revision")}) 
   public IPSGuid getGuidById(String contentid, String revision)
   {
      initServices();
      PSLocator loc = new PSLocator(contentid, revision);
      return gmgr.makeGuid(loc);
   }

   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getGuidById(java.lang.String)
    */
   @IPSJexlMethod(description="get the GUID by Content Id",
         params={@IPSJexlParam(name="contentid",description="the content id")})
   public IPSGuid getGuidById(String contentid)
   {
      initServices();
      PSLocator loc = new PSLocator(contentid);
      return gmgr.makeGuid(loc);
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getNodeByGuid(com.percussion.utils.guid.IPSGuid)
    */
   @IPSJexlMethod(description="get the node for a particular guid", 
         params={@IPSJexlParam(name="guid",description="the GUID for the item")})
   public IPSNode getNodeByGuid(IPSGuid guid) throws RepositoryException
   {
      initServices(); 
      List<Node> nodes = cmgr.findItemsByGUID(Collections.<IPSGuid>singletonList(guid), null);
      if(nodes.size() > 0)
      { 
         return (IPSNode)nodes.get(0); 
      }
      return null; 
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getSiteGuid(int)
    */
   @IPSJexlMethod(description="get the site guid for a given id", 
         params={@IPSJexlParam(name="siteid",description="the id for the site")})
   public IPSGuid getSiteGuid(int siteid)
   {
      initServices();
      IPSGuid guid = gmgr.makeGuid(siteid, PSTypeEnum.SITE); 
      log.debug("Site guid is " + guid);
      return guid;
   }
   
   /**
    * @see com.percussion.pso.jexl.IPSOObjectFinder#getTemplateGuid(int)
    */
   @IPSJexlMethod(description="get the template guid for a given id", 
         params={@IPSJexlParam(name="template",description="the id for the template")})
   public IPSGuid getTemplateGuid(int templateid)
   {
      initServices();
      IPSGuid guid = gmgr.makeGuid(templateid, PSTypeEnum.TEMPLATE); 
      log.debug("Template guid is " + guid);
      return guid;
   }
   
   @IPSJexlMethod(description="get the community name for a  given community id", 
	         params={@IPSJexlParam(name="communityId",description="the id for the community")})
 public String getCommunityName(int communityId) {
	   initServices();
	   List<PSCommunity> communities = sws.loadCommunities(null);
	   String communityName = null;
	   for(PSCommunity comm :  communities) {
		if (communityId == comm.getGUID().getUUID()) {
			communityName = comm.getName();
			break;
		}
	   }
	   return communityName;
 }
   
   /***
    * Returns a PSState object for the given state and workflow.  This can be used to get 
    * the publishable flag on a given workflow state. 
    * 
    * For Example:
    * 
    * $summary = $user.psoObjectFinder.getComponentSummary($sys.assemblyItem.getId()) 
    * $state = $user.psoObjectFinder.getWorkflowState($summary.getContentStateId(),$summary.getWorkflowAppId()) 
    * 
    * <h1>STATE CURRENT VALUE=${state.getContentValidValue()}</h1>
    * 
    * @param stateId
    * @param workflowAppId
    * @return
    */
	 @IPSJexlMethod(description="Get the workflow info for a given item", 
		         params={@IPSJexlParam(name="stateId",description="the stateId"),@IPSJexlParam(name="workflowAppId",description="Returns the State definition for the specified workflow state.")})
	public PSState getWorkflowState(int stateId,int workflowAppId) {
		   initServices();
		   PSState state = wf.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE,stateId),new PSGuid(PSTypeEnum.WORKFLOW,workflowAppId));
		   return state;
	}   
   
   
   /**
    * Gets the user session. 
    * @return the user session
    */
   private PSUserSession getSession()
   {
      PSRequest req = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSUserSession session = req.getUserSession();
      return session; 
   }
   
  
   
   /**
    * @param cws The cws to set. This routine should only be used
    * for unit testing. 
    */
   public static void setCws(IPSContentWs cws)
   {
      PSOObjectFinder.cws = cws;
   }

   /**
    * @param gmgr the gmgr to set
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      PSOObjectFinder.gmgr = gmgr;
   }

   /**
    * @param cmgr the cmgr to set
    */
   public static void setCmgr(IPSContentMgr cmgr)
   {
      PSOObjectFinder.cmgr = cmgr;
   }


}
