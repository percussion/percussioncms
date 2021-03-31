package com.percussion.pso.workflow;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.pso.utils.PSOItemFolderUtilities;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSCommunityVisibility;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

/***
 * Workflow Action that will switch the item to the community specified by the 
 * defaultCommunityName custom property of the Folder containing the item. If the
 * item is involved in multiple folders that have this property configured,
 * an error will be thrown. 
 * 
 * @author natechadwick
 *
 */
public class PSOSwitchCommunityWorkflowAction implements IPSWorkflowAction{
	
	/** 
	 * The name of the default community to switch the item to.
	 */
	public static final String DEFAULT_COMMUNITY_NAME  = "defaultCommunityName";
	public static final String PARAM_BASE = "com.percussion.pso.workflow.PSOSwitchCommunityWorkflowAction";
	private static final Log log = LogFactory.getLog(PSOSwitchCommunityWorkflowAction.class);
	private static IPSGuidManager gmgr = null;
	private static IPSContentWs m_cws;
	private static IPSSecurityDesignWs secSvc = null;
	private boolean overrideVisibility = false;
	
	/***
	 * OverrideVisibility determines if the community switch will ignore
	 * visbility problems with the Workflow and State
	 * @return 
	 */
	public boolean getOverrideVisibility() {
		return overrideVisibility;
	}

	/***
	 * A boolean value that when true, will ignore validation of visibility rules. 
	 * False by default. 
	 * @param overrideVisibility
	 */
	public void setOverrideVisibility(boolean overrideVisibility) {
		this.overrideVisibility = overrideVisibility;
	}


	public void init(IPSExtensionDef exDef, File arg1)
			throws PSExtensionException {
		log.debug("Initializing Services..");
		initServices();
		
		log.debug("Initializing parameters");
		
		 String v = exDef.getInitParameter(PARAM_BASE + ".overrideVisibility");
	      if(v!=null && StringUtils.isNotBlank(v))
	      {
	         setOverrideVisibility(Boolean.parseBoolean(v)); 
	      }
		
	}

	public void performAction(IPSWorkFlowContext wfCtx, IPSRequestContext request)
			throws PSExtensionProcessingException {
			
		log.debug("Performing Community Switch..");
	    
	    String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
	    int folderid=0;

		try {
			folderid = PSOItemFolderUtilities.getItemParentFolderId(Integer.parseInt(contentid));
			
		} catch (NumberFormatException e1) {
			log.error("Error detecting parent Folder for item " + contentid,e1);
		} catch (PSCmsException e1) {
			log.error("Error detecting parent Folder for item " + contentid,e1);
		}
		
	    String user = request.getUserName();
	    String session = request.getUserSessionId();
	    String targetCommunity = getDefaultCommunityProperty(folderid);
	    
	    if(targetCommunity == null){
	    	log.warn("No " + DEFAULT_COMMUNITY_NAME + " property configured for the folder: " +  folderid + " , skipping Community Switching action");
	    	return; //Do nothing further as there is no community configured.
	    }
	    
	    IPSCatalogSummary comm = findCommunityByName(targetCommunity);
	    if(comm == null){
	    	log.warn("Unable to load community " +  targetCommunity + " , skipping Community Switching action");
	    	return; //Do nothing further as there is no community configured.
	    }
	    // Test to be sure that the Workflow and State are visible in the target commnity. 
	    boolean proceed=false;
	    if(!getOverrideVisibility()){
		   if(testCommunityVisibility(wfCtx, comm.getGUID(),user,session)){
			   proceed=true;
		   }
		}else{
			proceed=true;
		}
	    
	    if(proceed){
	    	log.info("Switching Community for " + contentid + " to " + comm.getGUID().getUUID());

	    	//This block of code executes a direct SQL update to 
	    	//update the community of the item. When the workflow action 
	    	//has been fired, the Transition has already occured, so in the 
	    	//case of an item in a Public state, editing the item with the API
	    	//will cause a cascade as we have to transition the item again
	    	//in order to edit it.  This should be changed if there is ever
	    	//a clean API to just edit an item sans Workflow.  I hate doing it this way but 
	    	//don't see another option. 
	    	
	    	try{

	    		setCommunitySQL(wfCtx.getContentID(),comm.getGUID().getUUID());
	    		
	    		log.info("Community for item " +  contentid + " changed to " + comm.getName());
			}finally{}
	    }
	    
	    		
	}
	
   private static void setCommunitySQL(int contentId, int communityid)
   {
      Connection conn = null;
      PreparedStatement stmt = null;
      try {
         log.debug("changing community for content id " + contentId); 
         conn = PSDatabasePool.getDatabasePool().getConnection();
         stmt = conn.prepareStatement(SQL_UPDATE);
         stmt.setInt(1, communityid);
         stmt.setInt(2, contentId);
         int rows = stmt.executeUpdate();
         log.debug("rows affected " + rows); 
         if(rows != 1)
         {
            log.warn("Switching Community: unexpected row count " + rows); 
         }
      }
      catch(Exception ex)
      {
         log.error("SQL Error " + ex.getMessage(), ex);
      }
      finally
      { 
         if(stmt != null)
         {
            try
            {
               stmt.close();
            } catch (SQLException e)
            {
               log.error("error releasing statement " + e.getMessage(), e); 
            } 
         }
         if(conn != null)
         {
            try
            {
            	PSDatabasePool.getDatabasePool().releaseConnection(conn);
            } catch (SQLException e)
            {
               log.error("error releasing connection " + e.getMessage(), e); 
            } 
         }
      }
   }
   
	   private static final String SQL_UPDATE = "UPDATE CONTENTSTATUS SET COMMUNITYID = ? where CONTENTID = ?";
	   
	
	/****
	 * Method to determine if the Workflow and State are valid in the specified community. 
	 * @param wfCtx An active workflow context.
	 * @param communityId  IPSGuid for the community to be checked. 
	 * @param user Active user
	 * @param session Active Session id
	 * @return True if the Workflow and State are visible in the community, false if not. 
	 */
	private boolean testCommunityVisibility(IPSWorkFlowContext wfCtx, IPSGuid communityId, String user, String session) {
		boolean match_wf = false;
		boolean match_state = false;
		
		try{
		List<IPSGuid> comms = Collections.singletonList(communityId);
		
		List<PSCommunityVisibility> v = secSvc.getVisibilityByCommunity(comms, PSTypeEnum.WORKFLOW, user, session);
		
		//Make sure the target workflow is in the list
		for(PSCommunityVisibility c : v){
			for(PSObjectSummary o : c.getVisibleObjects()){
				
				if((o.getGUID().getUUID() == wfCtx.getWorkflowID())){
					log.debug("Found Workflow " + o.getName() + " in Community.");
					match_wf = true;
					break;
				}
			}
		}
		
		log.debug("Workflow valid in community = " + match_wf);
		
		/* NOTE: Apparently State visibility is not implemented. 
		 * v = secSvc.getVisibilityByCommunity(comms, PSTypeEnum.WORKFLOW_STATE, user, session);
		
		//Make sure the target workflow state is also in the list
		for(PSCommunityVisibility c : v){
			for(PSObjectSummary o : c.getVisibleObjects()){
				if((o.getId() == wfCtx.getStateID())){
					log.debug("Found Workflow State " + o.getName() + " in Community.");
					match_state = true;
					break;
				}
			}
		}*/
		
			log.debug("Workflow State valid in community = " + match_state);
		} catch (RemoteException e) {
			log.error("Error calculating Workflow visibility in new community", e);
		} catch (PSErrorResultsException e) {
			log.error("Error calculating Workflow visibility in new community", e);
		}finally{}
		
		return (match_wf);
	}

	/**
	 * Initialize the backend CMS services. 
	 */
	private void initServices(){
			 gmgr = PSGuidManagerLocator.getGuidMgr();
			 m_cws = PSContentWsLocator.getContentWebservice();
	         secSvc = PSSecurityWsLocator.getSecurityDesignWebservice();  	   
	}

	
	/***
	 * Locate the given community by name. 
	 * @param name
	 * @return
	 */
	private PSCommunity findCommunityByName(String name){
		
		IPSBackEndRoleMgr service = PSRoleMgrLocator.getBackEndRoleManager();
		List<PSCommunity> comms = service.findCommunitiesByName(name);
		
		if(comms != null && !comms.isEmpty()){
			return comms.get(0); //Return the first community. 
		}else{
			return null;
		}
	}
	
	/***
	 * Get the defaultCommunityName property for the specified folder. 
	 * @param folder_id A valid IPSGuid for the target folder.
	 * @return the name configured for the folder.  May return null if Community or property is not found. 
	 */
	private String getDefaultCommunityProperty(int folder_id){
		String community=null;
		int nextParentFolderId=0;
		
		if(folder_id==0){
			log.debug("Null folder passed to getDefaultCommunityProperty, returning null");
			return community;
		}
		Map<String,String> props = getFolderProperties(folder_id);
		if(props.containsKey(DEFAULT_COMMUNITY_NAME)){
			community = props.get(DEFAULT_COMMUNITY_NAME);
			log.info("Default community identified in parent folder " + folder_id + ". Community: " + community);
			return community;
		}
		//start recursive code to check the next parent folder if no defaultCommunity is set
		else {
				try {
					nextParentFolderId = PSOItemFolderUtilities.getItemParentFolderId(folder_id);
				} catch (NumberFormatException e) {
					log.error("Cannot retreive parent folder ID: current folder ID is not a number", e);
				} catch (PSCmsException e) {
					log.error("Error retreiving next parent folder ID", e);
				}
				
			if(nextParentFolderId != 0 && nextParentFolderId != 1){
				community = getDefaultCommunityProperty(nextParentFolderId);
			}
		}
		return community;
	}
	

	/***
	 * Get the properties for the given folder. 
	 * @param id The id of the folder. 
	 * @return A Map containing folder properties in Name, Value pairs.  Never null. May be empty.  
	 */
	@SuppressWarnings("unchecked")
	private Map<String,String> getFolderProperties(int id){
		
		Map<String, String> props = new HashMap<String, String>();
		
		try{
			
			String[] folders = new String[] {PSOItemFolderUtilities.getFolderPath(id)};
		
			//There should only be one folder returned.
			PSFolder folder = m_cws.loadFolders(folders).get(0);
			
			//Convert the folder properties into a map and return the result
	         Iterator<PSFolderProperty> it = folder.getProperties();
	         while (it.hasNext()) {
	             PSFolderProperty prop = it.next();
	             props.put(prop.getName(), prop.getValue());
	         }
			} catch (PSErrorResultsException e) {
				log.error("Error looking up properties for folder: " + id + ".",e);
			} catch (PSCmsException e) {
				log.error("Error looking up path for folder: " + id + ".",e);
			}
		
		return props;
	}
	
	 
	
}
