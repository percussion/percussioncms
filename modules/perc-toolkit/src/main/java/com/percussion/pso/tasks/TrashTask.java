/**

 * 
 * @author Stephen Bolton
 */
package com.percussion.pso.tasks;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSCacheProxy;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.services.schedule.IPSTask;
import com.percussion.services.schedule.IPSTaskResult;
import com.percussion.services.schedule.PSSchedulingException;
import com.percussion.services.schedule.PSSchedulingException.Error;
import com.percussion.services.schedule.data.PSTaskResult;
import com.percussion.services.schedule.impl.PSScheduleUtils;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.workflow.PSWorkflowRoleInfo;

/**
 *  * This is used to execute move and cleanup items using a trash folder and state
 * Items In this folder will be forced into a trash state if they are not
 * already. using a comma separated list of transitions to attempt. Items in a
 * trash state will be purged after an optional number of days. This includes
 * items not explicitly moved to the trash folder. Orphaned items will be moved
 * into the trash folder Folder structure //Folders/{trash folder
 * name}/{community name} will be used to split up items by folder Orphaned
 * items will be placed into a folder based upon the date they were found
 * //Folders/{trash folder name}/{community name}/Orphaned-20101112 sys_title of
 * orphaned items will be appended with (1) (2) etc. if there are duplicates If
 * more than 200 orphaned items are found for the community subfolders
 * 1-200,201-400 etc. will be created Orphaned items will be forced into trash
 * state when being moved Empty trash folders below the top level community
 * folder will be removed after processing. Separate view can be made to show
 * items with AA relationships to items in trash state so they can be manually
 * cleaned up before automatic removal. 
 * 
 * This code uses some api calls that should not be used in normal applications,
 * these have been used to bypass regular process steps and validation code to force
 * adding of items to folders, and transitions that may have failed through regular mechanisms.
 * 
 * 
 * @author stephenbolton
 * @version $Revision: 1.0 $
 */
public class TrashTask implements IPSTask {
	/**
	 * logger for this class.
	 */
	private static final Log log = LogFactory.getLog(TrashTask.class);
	/**
	 * Field ORPHAN_ITEM_SQL.
	 * (value is ""select contentid,communityid from contentstatus "
			+ "where contentid not in (select dependent_id from psx_objectrelationship "
			+ "where config_id = 3) and contentid > 3"")
	 */
	private static final String ORPHAN_ITEM_SQL = "select contentid,communityid from contentstatus "
			+ "where contentid not in (select dependent_id from psx_objectrelationship "
			+ "where config_id = 3) and contentid > 3";
	/**
	 * Field PURGE_ITEMS.
	 * (value is ""select c.contentid,c.lasttransitiondate from contentstatus c,states s "
			+ "where c.workflowappid = s.workflowappid and c.contentstateid = s.stateid "
			+ "and s.statename=? and c.lasttransitiondate < ?"")
	 */
	private static final String PURGE_ITEMS = "select c.contentid,c.lasttransitiondate from contentstatus c,states s "
			+ "where c.workflowappid = s.workflowappid and c.contentstateid = s.stateid "
			+ "and s.statename=? and c.lasttransitiondate < ?";

	/**
	 * Field MAX_ITEMS_IN_FOLDER.
	 * (value is 200)
	 */
	private static final int MAX_ITEMS_IN_FOLDER = 200;
	/**
	 * The relationship type for folder object
	 */
	public static final String FOLDER_RELATE_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

	/**
	 * Field rolemgr.
	 */
	private static IPSBackEndRoleMgr rolemgr = null;

	/**
	 * Field summ.
	 */
	private static IPSCmsContentSummaries summ = null;

	/**
	 * Field cws.
	 */
	private static IPSContentWs cws = null;

	/**
	 * Field gmgr.
	 */
	private static IPSGuidManager gmgr = null;
	/**
	 * Field wf.
	 */
	private static IPSWorkflowService wf = null;

	/**
	 * Method close.
	 * @param rs ResultSet
	 * @param ps Statement
	 * @param conn Connection
	 */
	private static void close(ResultSet rs, Statement ps, Connection conn) {
		if (null != rs) {
			try {
				rs.close();

			} catch (final SQLException e) {
				log.error("The result set cannot be closed.", e);
			} 
		}
		if (null != ps) {
			try {
				ps.close();
			} catch (final SQLException e) {
				log.error("The statement cannot be closed.", e);
			}
		}
		if (null != conn) {
			try {
				conn.close();
			} catch (final SQLException e) {
				log.error("The data source connection cannot be closed.", e);
			}
		}
		

	}

	/**
	 * Method initServices.
	 */
	private static void initServices() {
		if (null == rolemgr) {
			rolemgr = PSRoleMgrLocator.getBackEndRoleManager();
			summ = PSCmsContentSummariesLocator.getObjectManager();
			cws = PSContentWsLocator.getContentWebservice();
			gmgr = PSGuidManagerLocator.getGuidMgr();
			wf = PSWorkflowServiceLocator.getWorkflowService();

		}
	}

	/**
	 * Field userName.
	 */
	private String userName = PSSecurityProvider.INTERNAL_USER_NAME;
	/**
	 * Field trashTransitionsList.
	 */
	private List<String> trashTransitionsList;
	/**
	 * Field req.
	 */
	private PSRequest req;
	/**
	 * Field folderproc.
	 */
	private PSServerFolderProcessor folderproc;

	/**
	 * Field trashState.
	 */
	private String trashState;

	/**
	 * Field daysToKeep.
	 */
	int daysToKeep;

	/**
	 * Method perform.
	 * @param parameters Map<String,String>
	 * @return IPSTaskResult * @see com.percussion.services.schedule.IPSTask#perform(Map<String,String>) */
	public IPSTaskResult perform(Map<String, String> parameters) {
		initServices();

		// Number of days that items will remain in a trash state before being
		// purged.
		final String daystokeepString = parameters.get("numberOfDays");
		if (!StringUtils.isNumeric(daystokeepString))
			throw new IllegalArgumentException(
					"days to keep should be a number -1 to keep forever");
		daysToKeep = Integer.valueOf(daystokeepString);

		// Comma separated list of transition "trigger" names that will be
		// attempted to transition
		// to a trash state. In future this could be calculated.
		final String trashTransitions = parameters.get("transitionNames");
		trashTransitionsList = new ArrayList<String>();

		if (trashTransitions.contains(",")) {
			trashTransitionsList = Arrays.asList(trashTransitions.split(","));
		} else {
			trashTransitionsList.add(trashTransitions);
		}

		final String trashfolder = parameters.get("trashFolder");
		if (!trashfolder.startsWith("//Folders/"))
			throw new IllegalArgumentException(
					"Trash folder must start with //Folders/");
		if (!trashfolder.endsWith("/")) {
			trashfolder.subSequence(0, trashfolder.length() - 1);
		}
		// If true we will move orphan items into the trash folder
		// Unless there is a future patch, currently items being moved are
		// removed and then added to folder
		// there is a tiny period of time these will essentially be orphaned.
		// Running this at night should
		// reduce the possiblity of a false positive, the effect would be the
		// item would end up in the destination
		// folder and the trash folder, the transition to the trash state of
		// trash items would also kick in.
		final boolean moveorphans = parameters.get("moveOrphans").equalsIgnoreCase("true");

		trashState = parameters.get("trashState");

		// If true then if orphan items are found error status is returned
		// this can trigger email to be sent if set to email on error
		final boolean orphanIsError = parameters.get("orphanIsError").equalsIgnoreCase("true");
		
		
		log.debug("Started Trash Task, folder is " + trashfolder);

		IPSTaskResult result = null;
		boolean isComplete = true;
		String errorCause = null;
		final long startTime = System.currentTimeMillis();

		long endTime=0;

		try {

			// We have no Request object, so we need to set one up as the
			// rxserver user
			setupRequest();
			/* setup Server Request */
			// Move orphaned items into trash folder
			int numOrphans = 0;
			if (moveorphans) {
				cleanupOrphans(trashfolder, req);
			}
			parameters.put("orphanCount", String.valueOf(numOrphans));
			// Transition items in trash folder to trash state
			int trashFolderid;
			try {
				trashFolderid = folderproc.getIdByPath(trashfolder);
			} catch (PSCmsException e) {
				throw new FatalTaskException("Cannot get id folder id for "+trashfolder,e);
			}
			final PSLocator trashFolderLocator = summ.loadComponentSummary(
					trashFolderid).getCurrentLocator();
			
			trasitionFolderItems(trashFolderLocator);
			// Find items in trash state and purge if they have been in the
			// state longer than "daysToKeep"

			purgeOldItems(trashState, daysToKeep);

			if (orphanIsError && numOrphans>0) {
				isComplete=false;
				errorCause="Orphan Items moved to trash";
			}
			endTime = System.currentTimeMillis();
		} catch (FatalTaskException e) {
			isComplete = false;
			endTime = System.currentTimeMillis();
			log.error("Failed to execute command: ", e);
			errorCause = e.getLocalizedMessage();
		
		} finally {
			PSRequestInfo.resetRequestInfo();
		}

		result = new PSTaskResult(isComplete, errorCause,
				PSScheduleUtils.getContextVars(parameters, startTime, endTime));
		log.debug("Completed Trash Task complete="+isComplete);
		return result;
	}

	
	/**
	 * Method cleanupOrphans.
	 * @param trashfolder String
	 * @param req PSRequest
	 * @throws FatalTaskException
	 * @throws PSException * @throws PSErrorException * @throws NamingException * @throws PSORMException * @throws PSErrorResultsException * @throws SQLException */
	private int cleanupOrphans(String trashfolder, PSRequest req)
			throws FatalTaskException {
	
		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		final Date date = new Date();
		final String dateString = dateFormat.format(date);
		// find orphans 200 at a time
		// Orphaned Items not in folder cache so need to use relationships
		final Map<Integer, Set<Integer>> orphanedItems = getOrphanedItems();
		final PSServerFolderProcessor folderproc = new PSServerFolderProcessor(
				req, null);
		int orphanCount = 0;
		for (final Entry<Integer, Set<Integer>> entry : orphanedItems
				.entrySet()) {
			final int communityId = entry.getKey();
			final List<Integer> commOrphans = new ArrayList<Integer>(
					entry.getValue());

			final boolean useSubfolder = commOrphans.size() > MAX_ITEMS_IN_FOLDER;
			int count = 1;
			final String rootFolder = trashfolder + "/"
					+ getCommunityName(communityId) + "/Orphaned_Items_"
					+ dateString;

			String folder = new String(rootFolder);
			final Set<Integer> pageIds = new HashSet<Integer>();
			final Iterator<Integer> commOrphansIt = commOrphans.iterator();
			final boolean flushFolderCache = commOrphansIt.hasNext();
			try {
			while (commOrphansIt.hasNext()) {
				orphanCount++;
				final int orphanId = commOrphansIt.next();
				pageIds.add(orphanId);
				if (!commOrphansIt.hasNext()
						|| 0 == count % MAX_ITEMS_IN_FOLDER) {
					if (useSubfolder) {
						folder = new String(rootFolder + "/" + (count)
								+ "-" + (count + MAX_ITEMS_IN_FOLDER - 1));
					}

					List<IPSGuid> existingFolders;
					
						existingFolders = folderproc
								.findMatchingFolders(folder);
					
					int index_count = 0;
					String indexedFolder = new String(folder);
					// if folder exists try to find one with an index extension
					// which does not already
					while (null != existingFolders
							&& existingFolders.size() > 0) {
						index_count++;
						indexedFolder = folder + "_" + index_count;
						existingFolders = folderproc
								.findMatchingFolders(indexedFolder);
					}
					// Create required folder and ancestor folders
					log.debug("Creating folder for orphans " + indexedFolder);
					final List<PSFolder> addedFolders = cws
							.addFolderTree(indexedFolder);

					final PSLocator parentFolderLocator = addedFolders.get(
							addedFolders.size() - 1).getLocator();

					final List<PSComponentSummary> pageSumms = summ
							.loadComponentSummaries(pageIds);
					// find duplicates sys_titles in set of items we are going
					// to add to folder and change
					// them by adding (1) (2) etc
					dedupeTitles(pageSumms);

					// Add to folder but do not run any effects of validation
					// that would prevent operation

					/*
					 * Runs affects but not other folder tests
					 * PSRelationshipProcessor relation = new
					 * PSRelationshipProcessor( req);
					 * relation.add(FOLDER_RELATE_TYPE,
					 * folderIdToDepLocators(pageIds), parentFolderLocator);
					 */
					// Direct relationship add to create folder with no effects,
					// effect failure will not stop this
					final IPSRelationshipService svc = PSRelationshipServiceLocator
							.getRelationshipService();

					final PSRelationshipConfig config = PSRelationshipCommandHandler
							.getRelationshipConfig(FOLDER_RELATE_TYPE);

					for (final PSLocator depLocator : folderIdToDepLocators(pageIds)) {

						final PSRelationship rel = new PSRelationship(-1,
								parentFolderLocator, depLocator, config);
						svc.saveRelationship(rel);
					}

					// add to folder.
					pageIds.clear();
				}
				count++;
			}
			
			if (flushFolderCache) {
				PSCacheProxy.flushFolderCache();
			}
			} catch (PSCmsException e) {
				throw new FatalTaskException(e);
			} catch (PSException e) {
				throw new FatalTaskException(e);
			} catch (PSErrorResultsException e) {
				throw new FatalTaskException(e);
			} catch (PSErrorException e) {
				throw new FatalTaskException(e);
			}
		}
		return orphanCount;
	}

	/**
	 * Method dedupeTitles.
	 * @param pageSumms List<PSComponentSummary>
	
	
	 * @throws FatalTaskException  * @throws PSORMException */
	private void dedupeTitles(List<PSComponentSummary> pageSumms) throws FatalTaskException
			 {

		// List to check against
		final Map<String, List<PSComponentSummary>> dupCheck = new HashMap<String, List<PSComponentSummary>>();
		// Filtered list containing only summaries with duplicate titles.
		final Map<String, List<PSComponentSummary>> dupeTitles = new HashMap<String, List<PSComponentSummary>>();
		for (final PSComponentSummary summary : pageSumms) {
			final String name = summary.getName();
			List<PSComponentSummary> titleItems = dupCheck.get(name);
			if (null == titleItems) {
				titleItems = new ArrayList<PSComponentSummary>();
				dupCheck.put(name, titleItems);
			} else {
				log.debug("Dupe orphan item title found with name " + name);
				dupeTitles.put(name, titleItems);
			}
			titleItems.add(summary);
		}
		final List<PSComponentSummary> newTitleItems = new ArrayList<PSComponentSummary>();

		for (final Entry<String, List<PSComponentSummary>> entry2 : dupeTitles
				.entrySet()) {
			final String name = entry2.getKey();
			int index = 1;
			for (final PSComponentSummary item : entry2.getValue()) {
				// TODO : item title may be too large after change.

				final String newName = name + " (" + index + ")";
				log.debug("Modifying title of item " + item.getContentId()
						+ " to name " + newName);
				item.setName(newName);
				newTitleItems.add(item);
				index++;
			}
		}

		if (newTitleItems.size() > 0) {
			log.debug("Attempting to fixup orphaned dupe titles for "
					+ newTitleItems.size() + " items");
			final IPSCmsObjectMgr cms = PSCmsObjectMgrLocator
					.getObjectManager();
			try {
				cms.saveComponentSummaries(newTitleItems);
			} catch (PSORMException e) {
				throw new FatalTaskException(e);
			}
		}
	}

	/**
	 * Method folderIdToDepLocators.
	 * @param folderIds Set<Integer>
	
	 * @return List<PSLocator> */
	private List<PSLocator> folderIdToDepLocators(Set<Integer> folderIds) {
		final List<PSLocator> folderLocators = new ArrayList<PSLocator>();
		for (final int id : folderIds) {
			folderLocators.add(new PSLocator(id, -1));
		}
		return folderLocators;
	}

	/**
	 * Method getCommunityName.
	 * @param id int
	
	 * @return String */
	private String getCommunityName(int id) {
		if (id==-1) return "unknown";
		 PSCommunity[] communities = null;
		try {
			communities = rolemgr
				.loadCommunities(new IPSGuid[] { new PSGuid(
						PSTypeEnum.COMMUNITY_DEF, id) });
		} catch (IllegalArgumentException e) {
			log.error("Unknown community with "+id);
			
		}
		if (communities == null || communities.length > 0) {
			return communities[0].getName();
		} else {
			return String.valueOf(id);
		}
	}

	/**
	 * Get the exception for the specified command.
	 * 
	 * @param command
	 *            the failure command, may be <code>null</code> or empty.
	 * @param stdErrorText
	 *            the standard error text from the failure, may be
	 *            <code>null</code> or empty.
	
	 * @return PSSchedulingException */
	private PSSchedulingException getException(String command,
			String stdErrorText) {
		PSSchedulingException se = null;
		if (StringUtils.isBlank(stdErrorText)) {
			se = new PSSchedulingException(Error.FAILED_RUN_COMMAND.ordinal(),
					command);
		} else {
			se = new PSSchedulingException(
					Error.FAILED_RUN_COMMAND_WITH_STDERROR.ordinal(), command,
					stdErrorText);
		}
		return se;
	}

	/**
	
	
	
	 * @return Map<Integer,Set<Integer>> * @throws FatalTaskException
	 * @throws NamingException * @throws SQLException */
	private Map<Integer, Set<Integer>> getOrphanedItems()
			throws FatalTaskException {
		final Map<Integer, Set<Integer>> orphanIds = new HashMap<Integer, Set<Integer>>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = PSConnectionHelper.getDbConnection();
			stmt = conn.prepareStatement(ORPHAN_ITEM_SQL);
			rs = stmt.executeQuery();
			// Group Orphan results by community id

			while (rs.next()) {
				final int id = rs.getInt("contentid");
				final int comm = rs.getInt("communityid");
				Set<Integer> comSet = orphanIds.get(comm);
				if (null == comSet) {
					comSet = new HashSet<Integer>();
					orphanIds.put(comm, comSet);
				}
				comSet.add(id);
			}

			if (log.isDebugEnabled()) {
				for (final int comId : orphanIds.keySet()) {
					log.debug("Community : " + comId + "has "
							+ orphanIds.get(comId).size()
							+ " Orphaned content items");
				}
			}
		} catch (NamingException e) {
			throw new FatalTaskException(e);
		} catch (SQLException e) {
			throw new FatalTaskException(e);
		} finally {
			close(rs, stmt, conn);
		}
		return orphanIds;
	}

	/**
	 * Method getStateName.
	 * @param wfid int
	 * @param stateid int
	
	 * @return String */
	public String getStateName(int wfid, int stateid) {
		initServices();
		log.debug("Getting state name");
		final PSState state = wf.loadWorkflowState(new PSGuid(
				PSTypeEnum.WORKFLOW_STATE, stateid), new PSGuid(
				PSTypeEnum.WORKFLOW, wfid));
		log.debug("Got state name" + state.getName());
		return state.getName();
	}



	
	/**
	 * Method purgeOldItems.
	 * @param state String
	 * @param numberOfDays int
	
	
	
	
	 * @throws FatalTaskException
	 * @throws PSErrorsException * @throws NamingException * @throws SQLException * @throws PSErrorException */
	private void purgeOldItems(String state, int numberOfDays)
			throws FatalTaskException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = PSConnectionHelper.getDbConnection();
			stmt = conn.prepareStatement(PURGE_ITEMS);
			stmt.setString(1, state);

			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -daysToKeep);
			log.debug("Checking to purge recycle items older than = "
					+ new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()));

			stmt.setDate(2, new java.sql.Date(cal.getTime().getTime()));
			rs = stmt.executeQuery();

			// Group Orphan results by community id
			while (rs.next()) {
				final int id = rs.getInt("contentid");
				final IPSGuid guid = gmgr.makeGuid(new PSLocator(id, -1));
				log.debug("Attempting to purge item id=" + id);
				try {
					cws.deleteItems(Collections.singletonList(guid));
				} catch (PSErrorsException e) {
					log.error("Error purging Item "+id,e);
				} catch (PSErrorException e) {
					log.error("Error purging Item "+id,e);
				}
				log.debug("Item Purged =" + id);
			}

		} catch (NamingException e) {
			throw new FatalTaskException("SQL Naming exception trying to find purgeable items",e);
		} catch (SQLException e) {
			throw new FatalTaskException("SQL Exception trying to find purgeable items",e);
			
		} finally {
			close(rs, stmt, conn);
		}

	}

	/**
	 * Method setupRequest.
	 */
	private void setupRequest() {
		userName = PSSecurityProvider.INTERNAL_USER_NAME;
		req = PSRequest.getContextForRequest();
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(PSRequestInfo.KEY_PSREQUEST, req);
		params.put(PSRequestInfo.KEY_USER, userName);
		params.put(PSRequestInfo.KEY_JSESSIONID, req.getServletRequest()
				.getSession().getId());
		PSRequestInfo.initRequestInfo(params);
		log.debug("Current username is " + userName);

		folderproc = new PSServerFolderProcessor(req, null);

	}

	/**
	 * Method transitionItems.
	 * @param contentid int
	 * @return boolean * @throws FatalTaskException
	 * @throws PSExtensionProcessingException * @throws PSAuthorizationException * @throws PSNotFoundException * @throws PSExtensionException * @throws PSParameterMismatchException * @throws PSRequestValidationException */
	private boolean transitionItems(int contentid) throws
			FatalTaskException {
		final IPSRequestContext ctx = new PSRequestContext(req);

		final PSComponentSummary sum = summ.loadComponentSummary(contentid);
		final PSState state = wf.loadWorkflowState(new PSGuid(
				PSTypeEnum.WORKFLOW_STATE, sum.getContentStateId()),
				new PSGuid(PSTypeEnum.WORKFLOW, sum.getWorkflowAppId()));
		final Set<String> allowedTransitions = new HashSet<String>();
		final List<PSTransition> transitions = state.getTransitions();
		for (final PSTransition trans : transitions) {
			allowedTransitions.add(trans.getTrigger());
		}
		log.debug("Available transitions for item" + allowedTransitions);
		allowedTransitions.retainAll(trashTransitionsList);
		log.debug("Filtered Transitions :" + allowedTransitions);

		if (allowedTransitions.size() > 0) {
			final String action = allowedTransitions.iterator().next();
			final IPSExtensionManager manager = PSServer
					.getExtensionManager(null);
			final PSExtensionRef ref = new PSExtensionRef(
					"Java/global/percussion/workflow/sys_wfPerformTransition");
			IPSRequestPreProcessor transProc = null;
			try {
				transProc = (IPSRequestPreProcessor) manager
						.prepareExtension(ref, null);
			
			// String action = "Move to Recycle Bin";
			// String action = "29";
			req.setParameter("WFAction", action);
			Object[] transParams = { String.valueOf(contentid), userName,
					action };
			final PSWorkflowRoleInfo wfRoleInfo = new PSWorkflowRoleInfo();

			wfRoleInfo.setUserActingRoleNames(Collections
					.singletonList("Admin"));

			req.setPrivateObject(
					PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT,
					wfRoleInfo);

			// Assignment type normally set by authenticate user extension.
			// Bypass to set as Admin.
			req.setParameter(PSWorkFlowUtils.ASSIGNMENT_TYPE_CURRENT_USER,
					String.valueOf(PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN));
			req.setParameter("commenttext", "Transition by scheduled task");
		
				transProc.preProcessRequest(transParams, ctx);
			
			final PSExtensionRef wfHistoryRef = new PSExtensionRef(
					"Java/global/percussion/workflow/sys_wfUpdateHistory");
			final IPSResultDocumentProcessor wfHistoryProc = (IPSResultDocumentProcessor) manager
					.prepareExtension(wfHistoryRef, null);
			transParams = new Object[] { String.valueOf(contentid), userName };
			wfHistoryProc.processResultDocument(transParams, ctx, null);
			} catch (PSAuthorizationException e) {
				throw new FatalTaskException(e);
			} catch (PSRequestValidationException e) {
				throw new FatalTaskException(e);
			} catch (PSParameterMismatchException e) {
				throw new FatalTaskException(e);
			} catch (PSExtensionProcessingException e) {
				throw new FatalTaskException(e);
			} catch (PSNotFoundException e) {
				throw new FatalTaskException(e);
			} catch (PSExtensionException e) {
				throw new FatalTaskException(e);
			} 
			// Evict component summary cache for item
			final IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
			cache.evict(contentid, "PSComponentSummary");
			return true;
		} else
			return false;
	}

	/**
	 * Method trasitionFolderItems.
	 * @param trashFolderLocator PSLocator	
	 * @throws FatalTaskException
	 * @throws PSRequestValidationException * @throws PSExtensionProcessingException * @throws PSParameterMismatchException * @throws PSCmsException * @throws PSErrorException * @throws PSErrorsException * @throws PSAuthorizationException * @throws PSNotFoundException * @throws PSExtensionException */
	private void trasitionFolderItems(PSLocator trashFolderLocator)
			throws FatalTaskException {
		trasitionFolderItems(trashFolderLocator, 0);
	}

	/**
	 * Method trasitionFolderItems.
	 * 
	 * Warning this is not a regular process for transitioning items, do not use as a regular example
	 * use System web service to do regular transitions PSSystemWsLocator.getSystemWebservice()
	 * This code is used to transition without running any other extensions or validation
	 * The transition does still need to exist.
	 * 
	 * @param folder PSLocator
	 * @param level int
	 * @return boolean * @throws FatalTaskException
	 * @throws PSParameterMismatchException * @throws PSAuthorizationException * @throws PSExtensionException * @throws PSErrorException * @throws PSCmsException * @throws PSNotFoundException * @throws PSExtensionProcessingException * @throws PSErrorsException * @throws PSRequestValidationException */
	private boolean trasitionFolderItems(PSLocator folder, int level)
			throws FatalTaskException {
		boolean containsItems = false;
		final PSServerFolderProcessor folderproc = new PSServerFolderProcessor(
				req, null);
		PSComponentSummary[] summaries = null;
		try{
			summaries = folderproc.getChildSummaries(folder);
		} catch (PSCmsException e) {
			throw new FatalTaskException("Cannot get summaries for folder "+folder,e);
		} 
		
		log.debug("Checking folder for transition/delete "
				+ getFolderPathForId(folder.getId()));
		
		for (final PSComponentSummary summary : summaries) {
			if (summary.isFolder()) {
				containsItems |= trasitionFolderItems(
						summary.getCurrentLocator(), level + 1);
			} else {
				final int stateId = summary.getContentStateId();
				final int wfId = summary.getWorkflowAppId();
				final String stateName = getStateName(wfId, stateId);

				log.debug("Found content item " + summary.getName()
						+ " in state " + stateName);
				if (!stateName.equals(trashState)) {
					log.debug("Item  not trash state, will attempt to transition");
					final boolean hasTransitioned = transitionItems(summary
							.getContentId());
					if (!hasTransitioned) {
						log.debug("Could not find valid transition to move from "
								+ stateName + " to " + trashState);
					}
					containsItems = true;
				} else {
					log.debug("item in trash state,  checking to see if purge requrired.");
					final Date stateEnteredDate = summary.getStateEnteredDate();
					log.debug("State entered data = "
							+ new SimpleDateFormat("dd/MM/yyyy")
									.format(stateEnteredDate));
					final Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, -daysToKeep);
					if (cal.getTime().after(stateEnteredDate)) {
						log.debug("State Entered before" + daysToKeep
								+ " days purge candidate, ");
						
						try {
							cws.deleteItems(Collections.singletonList(gmgr
									.makeGuid(new PSLocator(summary.getContentId(),
											-1))));
						} catch (PSErrorsException e) {
							throw new FatalTaskException("Cannot purge item "+summary.getContentId(),e);
						} catch (PSErrorException e) {
							throw new FatalTaskException("Cannot purge item "+summary.getContentId(),e);
							
						}
					} else {
						log.debug("Item not ready to trash yet");
						containsItems = true;
					}

				}

			}
		}
		// Delete folders if they are empty and not directly within the trash
		// folder
		if (!containsItems && level > 1) {
			try {
			log.debug("Deleting empty folder "
					+ folderproc.getItemPaths(folder)[0]);
				folderproc.delete("PSFolder", new PSKey[] { folder });
			} catch (PSCmsException e) {
				throw new FatalTaskException("Cannot delete folder folder="+folder.getId(),e);
			}
		}
		
		return containsItems;
	}

	/**
	 * Method getFolderPathForId.
	 * @param id int
	 * @return String
	 */
	private String getFolderPathForId(int id) {
		String path = "";
		try {
			path = folderproc.getItemPaths(new PSLocator(id,-1))[0];
		} catch (PSCmsException e) {
			log.debug("Cannot get folder path for id "+id);
		}
		return path;
	}
	/**
	 * Method init.
	 * @param arg0 IPSExtensionDef
	 * @param arg1 File
	 * @throws PSExtensionException
	 * @see com.percussion.extension.IPSExtension#init(IPSExtensionDef, File)
	 */
	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		// TODO Auto-generated method stub
	}

	/**
	 */
	public class FatalTaskException extends Exception {
		/**
		 * Constructor for FatalTaskException.
		 * @param e Exception
		 */
		public FatalTaskException(Exception e) {
			super(e);
		}
		/**
		 * Constructor for FatalTaskException.
		 * @param s String
		 * @param e Exception
		 */
		public FatalTaskException(String s,Exception e) {
			super(s,e);
		}
		
	}
	
}
