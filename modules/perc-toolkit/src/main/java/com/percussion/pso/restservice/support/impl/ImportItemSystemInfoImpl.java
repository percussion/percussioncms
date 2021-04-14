/*******************************************************************************

 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.support.impl;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.restservice.support.IImportItemSystemInfo;
import com.percussion.services.assembly.*;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class ImportItemSystemInfoImpl implements IImportItemSystemInfo {

	/**
	 * Logger for this class
	 */
	private static final Log log = LogFactory.getLog(ImportItemSystemInfoImpl.class);

	
	/**
	 * Field contentTypeNameMap.
	 */
	private static Map<Long,String> contentTypeNameMap = new ConcurrentHashMap<Long,String>();
	/**
	 * Field siteNameMap.
	 */
	private static Map<Integer,String> siteNameMap = new ConcurrentHashMap<Integer,String>();
	/**
	 * Field slotNameMap.
	 */
	private static Map<Integer,String> slotNameMap = new ConcurrentHashMap<Integer,String>();
	/**
	 * Field gmgr.
	 */
	private static IPSGuidManager gmgr = null;
	/**
	 * Field cws.
	 */
	private static IPSContentWs cws=null;
//	private static IPSCmsContentSummaries summ = null; 
	/**
	 * Field aService.
	 */
	private static IPSAssemblyService aService = null;
//	private static IPSSystemWs system = null;
	/**
	 * Field sitemgr.
	 */
	private static IPSSiteManager sitemgr = null;
	/**
	 * Field rolemgr.
	 */
	private static IPSBackEndRoleMgr rolemgr = null;
	/**
	 * Field wf.
	 */
	private static IPSWorkflowService wf = null;
//	private static IPSContentMgr contentMgr = null;
//	private static IPSFilterService filter;
//	
	/**
	 * Field itemDefMgr.
	 */
	private static PSItemDefManager itemDefMgr=null;
	  /**
	   * Method initServices.
	   */
	  private static void initServices()
	   {
	      if(gmgr == null)
	      {
	    		gmgr = PSGuidManagerLocator.getGuidMgr(); 
	    		cws = PSContentWsLocator.getContentWebservice();
	    		rolemgr = PSRoleMgrLocator.getBackEndRoleManager();
	    		sitemgr = PSSiteManagerLocator.getSiteManager();
	    		aService = PSAssemblyServiceLocator.getAssemblyService();
	    		wf = PSWorkflowServiceLocator.getWorkflowService();
	    		itemDefMgr = PSItemDefManager.getInstance();
	    		List<PSContentTypeSummary> ctypes = cws.loadContentTypes(null);
				for (PSContentTypeSummary type : ctypes) {
					contentTypeNameMap.put(type.getGuid().longValue(), type.getName()); 
				}
			
			
	      }
	   }
	

	
	
	/**
	 * Method getWorkflowName.
	 * @param id int
	 * @return String
	 * @see IImportItemSystemInfo#getWorkflowName(int)
	 */
	public String getWorkflowName(int id) {
		initServices();
		log.debug("Getting workflow name");
		PSWorkflow workflow = wf.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW,id));
		log.debug("got workflow name "+workflow.getName());
		return workflow.getName();
	}
	/**
	 * Method getStateName.
	 * @param wfid int
	 * @param stateid int
	 * @return String
	 * @see IImportItemSystemInfo#getStateName(int, int)
	 */
	public String getStateName(int wfid,int stateid) {
		initServices();
		log.debug("Getting state name");
		PSState state = wf.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE,stateid),new PSGuid(PSTypeEnum.WORKFLOW,wfid));
		log.debug("Got state name"+state.getName());
		return state.getName();
	}


	/**
	 * Method getSiteName.
	 * @param id int
	 * @return String
	 * @see IImportItemSystemInfo#getSiteName(int)
	 */
	public String getSiteName(int id) {
		initServices();
		log.debug("Getting site name");

		String siteName=String.valueOf(id);
//		if (siteNameMap.containsKey(id)) {
//			siteName=siteNameMap.get(id);
//		} else {
//
			IPSSite site=null;
			try {
				site = sitemgr.loadSite(new PSGuid(PSTypeEnum.SITE, id));
				siteName=site.getName();
				siteNameMap.put(id, siteName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("Cannot load site", e);
			}
//		}
		return siteName;

	}

	/**
	 * Method getFolderPath.
	 * @param id int
	 * @return String
	 * @see IImportItemSystemInfo#getFolderPath(int)
	 */
	public String getFolderPath(int id) {
		initServices();
		log.debug("Getting folder path");
		try {
			List<PSFolder> folderList = cws.loadFolders(Collections.singletonList(gmgr.makeGuid(new PSLocator(id,-1))));
			log.debug("Got folder Path");
			return folderList.get(0).getFolderPath();
		} catch (PSErrorResultsException e) {
			log.error("Cannot get folder path for foder id "+id);
		} 

		return null;
	}

	/**
	 * Method getSlotName.
	 * @param id int
	 * @return String
	 * @see IImportItemSystemInfo#getSlotName(int)
	 */
	public String getSlotName(int id) {
		initServices();
		log.debug("Getting slot name");
		String slotname=slotNameMap.get(id);
		if (slotname==null) {
			IPSTemplateSlot slot;
			try {
				slot = aService.loadSlot(new PSGuid(PSTypeEnum.SLOT, id));
				slotname=slot.getName();
			} catch (Exception e) {
				log.debug("Cannot load slot "+id,e);
				slotname=String.valueOf(id);
			}


		}
		return slotname;
	}

	/**
	 * Method getTemplateName.
	 * @param id int
	 * @return String
	 * @see IImportItemSystemInfo#getTemplateName(int)
	 */
	public String getTemplateName(int id) {
		initServices();
		log.debug("Getting template name");
		String templatename=null;
//		String templatename=templateNameMap.get(id);

		if (templatename==null) {
			try{
				IPSAssemblyTemplate template =  aService.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, id),false);
				templatename=template.getName();
			} catch (PSAssemblyException e) {
				log.error("cannot get template for id "+id,e);
				templatename=String.valueOf(id);
			}

		}

		IPSAssemblyTemplate template=null;
		try {
			template = aService.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, id),false);
		} catch (PSAssemblyException e) {
			log.error("Cannot find template with id "+id);
		}
		log.debug("Got template name");
		return (template==null) ? Integer.toString(id) : template.getName();
	}
	/**
	 * Method getContentTypeName.
	 * @param id long
	 * @return String
	 * @see IImportItemSystemInfo#getContentTypeName(long)
	 */
	public String getContentTypeName(long id) {
		initServices();
		
		return(contentTypeNameMap.get(id));  
	}
	/**
	 * Method getCommunityName.
	 * @param id int
	 * @return String
	 * @see IImportItemSystemInfo#getCommunityName(int)
	 */
	public String getCommunityName(int id) {
		initServices();
		String name=null;
		if (id > 0) {
//			if (!communityNameMap.containsKey(id)) {
				PSCommunity[] communities = rolemgr.loadCommunities(new IPSGuid[]
				                                                                {new PSGuid(PSTypeEnum.COMMUNITY_DEF, id)});
				if (communities.length==1) {
					//communityNameMap.put(id, communities[0].getName());
					name=communities[0].getName();
				} else {
					name = Integer.toString(id);
				}
//			} else {
//				name=communityNameMap.get(id);
//			}
				}
		return name;
	}
	public int getCommunityId(String name) {
		initServices();
		   List<PSCommunity> comms = rolemgr.findCommunitiesByName(name);
		   if (comms!=null && comms.size()>0) {
			   return comms.get(0).getGUID().getUUID();
		   } else {
			   return -1;
		   }
	}
	
	
	/**
	 * Method getItemDefinition.
	 * @param contentType String
	 * @return PSItemDefinition
	 * @throws PSInvalidContentTypeException
	 * @see IImportItemSystemInfo#getItemDefinition(String)
	 */
	public PSItemDefinition getItemDefinition(String contentType) throws PSInvalidContentTypeException
	
	{
		initServices();
		return itemDefMgr.getItemDef(contentType,PSItemDefManager.COMMUNITY_ANY);
	}
}
