/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.*;
import com.percussion.cms.objectstore.server.PSBinaryFileValue;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.*;
import com.percussion.pso.restservice.IItemRestService;
import com.percussion.pso.restservice.exception.ItemRestException;
import com.percussion.pso.restservice.exception.ItemRestNotModifiedException;
import com.percussion.pso.restservice.model.Error;
import com.percussion.pso.restservice.model.*;
import com.percussion.pso.restservice.model.Item;
import com.percussion.pso.restservice.model.Value;
import com.percussion.pso.restservice.model.Error.ErrorCode;
import com.percussion.pso.restservice.model.results.PagedResult;
import com.percussion.pso.restservice.support.IImportItemSystemInfo;
import com.percussion.pso.restservice.support.ImportItemSystemInfoLocator;
import com.percussion.pso.restservice.utils.ItemServiceHelper;
import com.percussion.pso.utils.HTTPProxyClientConfig;
import com.percussion.pso.utils.PSOEmailUtils;
import com.percussion.server.*;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.*;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.*;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.springframework.stereotype.Service;

import javax.jcr.*;
import javax.jcr.query.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

/**
 */
@Service(value = "restItemService")
@Path("/Content/")
@Produces("text/xml")
public class ItemRestServiceImpl implements IItemRestService {

	private static final int PAGE_SIZE = 1000;
	//private static final String FEED_STATUS_RAN = "FEED RAN SUCCESSFULLY";
	//private static final String FEED_STATUS_NO_UPDATE = "FEED NOT UPDATED";
	private static final String FEED_STATUS_UPDATED = "RAN SUCCESSFULLY";
	private static final String FEED_STATUS_ERROR = "UPDATE FAILED";
	private static final String EMAIL_NOTIFICATION_PROPS = "emailnotification.properties";

	/**
	 * Logger for this class
	 */

	private static final Logger log = LogManager.getLogger(ItemRestServiceImpl.class);
	
	private static IPSWorkflowService wf = null;
	
	/**
	 * Field isi.
	 */
	protected static IImportItemSystemInfo isi = null;
	/**
	 * Field gmgr.
	 */
	private static IPSGuidManager gmgr = null;
	/**
	 * Field cws.
	 */
	private static IPSContentWs cws = null;
	/**
	 * Field summ.
	 */
	private static IPSCmsContentSummaries summ = null;
	/**
	 * Field aService.
	 */
	private static IPSAssemblyService aService = null;
	/**
	 * Field system.
	 */
	private static IPSSystemWs system = null;
	/**
	 * Field sitemgr.
	 */
	private static IPSSiteManager sitemgr = null;
	/**
	 * Field contentMgr.
	 */
	private static IPSContentMgr contentMgr = null;
	/**
	 * Field filter.
	 */
	private static IPSFilterService filter;

	/**
	 * Field uri.
	 */
	private UriInfo uri;
	/**
	 * Field emailProps.
	 */
	private Properties emailProps; 
	/**
	 * Method setUriInfo.
	 * 
	 * @param uri
	 *            UriInfo
	 */
	@Context
	public void setUriInfo(UriInfo uri) {
		this.uri = uri;
	}

	/**
	 * Constructor for ItemRestServiceImpl.
	 */
	public ItemRestServiceImpl() {
		super();
	}

	/**
	 * Initialize service pointers.
	 */
	private static void initServices() {
		
		
		if (gmgr == null) 
			gmgr = PSGuidManagerLocator.getGuidMgr();
		
		if(cws ==null)
			cws = PSContentWsLocator.getContentWebservice();
		
		if(summ==null)
			summ = PSCmsContentSummariesLocator.getObjectManager();
		
		if(aService==null)
			aService = PSAssemblyServiceLocator.getAssemblyService();
		
		if(system==null)
			system = PSSystemWsLocator.getSystemWebservice();
		
		if(sitemgr==null)
			sitemgr = PSSiteManagerLocator.getSiteManager();
		
		if(contentMgr==null)
			contentMgr = PSContentMgrLocator.getContentMgr();
		
		if(filter==null)
			filter = PSFilterServiceLocator.getFilterService();
		
		if(isi==null)
			isi = ImportItemSystemInfoLocator.getImportItemSystemInfo();
	
		 if(wf== null)
			wf = PSWorkflowServiceLocator.getWorkflowService();
	}

	/**
	 * Field pageSize.
	 */
	private int pageSize = 1000;

	private boolean inlineDoc=false;

	/**
	 * Method getFromRxItem.
	 * 
	 * @param id
	 *            int
	 * @param rev
	 *            int
	 * @return Item
	 */
	public Item getFromRxItem(int id,int rev) {
		boolean isFolder=false;
		initServices();
		Item item = new Item();
		item.setContentId(id);
		item.setRevision(rev);

		try {

			PSComponentSummary summary = summ.loadComponentSummary(id);
			if (summary == null) {
				item.addError(ErrorCode.NOT_FOUND,"Item with content id "+id+" is not found");
			} else {

				if (rev == -1) {
					// need to retun error if content id does not exist summary is null.
					rev = summary.getHeadLocator().getRevision();
				}
				List<IPSGuid> guids = Collections.singletonList(gmgr.makeGuid(new PSLocator(id,rev)));
				String community = isi.getCommunityName(summary.getCommunityId());

				Node node = contentMgr.findItemsByGUID(guids, null).get(0);

				int contenttypeid = Integer.valueOf(node.getProperty("rx:sys_contenttypeid").getString());
				log.debug("content type id ={}", contenttypeid);
				String contentTypeName = isi.getContentTypeName(contenttypeid);
				if (contenttypeid==101) {
					isFolder=true;
					PSRequest req = (PSRequest) PSRequestInfo
					.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
					PSServerFolderProcessor folderproc =  PSServerFolderProcessor.getInstance();
					String globalTemplate = folderproc.getGlobalTemplateProperty(id);
					PSComponentSummary[] children = folderproc.getChildSummaries(new PSLocator(id,-1));
					FolderInfo folderInfo = new FolderInfo();
					List<ItemRef> childRefs = new ArrayList<ItemRef>();
					for (int i=0; i<children.length; i++) {
						PSComponentSummary child = children[i];
						ItemRef ref = new ItemRef();
						ref.setContentId(child.getContentId());
						ref.setContentType(isi.getContentTypeName(child.getContentTypeId()));
						ref.setHref(generateItemLink(child.getContentId(), -1));
						childRefs.add(ref);
						ref.setTitle(child.getName());
					}
					folderInfo.setFolderItems(childRefs);
					String pubFileName = folderproc.getPubFileName(id);

					PSFolderAcl[] acls = folderproc.getFolderAcls(new int[] {id});
					PSFolderAcl acl = acls[0];

					FolderAcl itemAcl = new FolderAcl();
					String aclComm = isi.getCommunityName(acl.getCommunityId());
					itemAcl.setCommunityName(aclComm);
					log.debug("Folder communityname is {}", aclComm);
					List<AclItem> aclitems = new ArrayList<AclItem>();

					Iterator entries = acls[0].iterator();
					PSObjectAclEntry entry;
					while (entries.hasNext())
					{
						log.debug("Found AclEntry");
						entry = (PSObjectAclEntry) entries.next();
						String name = entry.getName();
						String type = PSObjectAclEntry.ACL_ENTRY_TYPES[entry.getType()];
						boolean read = entry.hasReadAccess();
						boolean write = entry.hasWriteAccess();
						boolean admin = entry.hasAdminAccess();
						boolean deny = entry.hasNoAccess();
						log.debug("AclEntry name={} type={}", name, type);
						AclItem aclItem = new AclItem();
						aclItem.setAdmin(admin);
						aclItem.setRead(read);
						aclItem.setWrite(write);
						aclItem.setDeny(deny);
						aclItem.setType(type);
						aclItem.setName(name);
						aclitems.add(aclItem);
					}
					itemAcl.setEntries(aclitems);
					folderInfo.setFolderAcl(itemAcl);
					folderInfo.setGlobalTemplate(globalTemplate);
					folderInfo.setPubFileName(pubFileName);
					item.setFolderInfo(folderInfo);

					List<String> folderPaths = Arrays.asList(folderproc.getItemPaths(new PSLocator(id,-1)));
					item.setFolders(folderPaths);
				} else {


					PSItemDefinition itemdef = isi.getItemDefinition(contentTypeName);

					item.setFields(getFromRxFields(node, itemdef));
					item.setChildren(getFromRxChildren(node, itemdef));
					item.setContentId(id);

					item.setCommunityName(community);
					item.setRevision(rev);
					List<String> folderPaths = Arrays.asList(cws.findFolderPaths(guids.get(0)));
					item.setFolders(folderPaths);


					int stateid = summary.getContentStateId();
					int workflowid = summary.getWorkflowAppId();

					log.debug("State  id = {}", stateid);
					log.debug("Workflow id = {}", workflowid);
					if(workflowid > 0) {
						item.setWorkflow(isi.getWorkflowName(workflowid));
						item.setState(isi.getStateName(workflowid, stateid));
					}

				}
				item.setRelationships(getRelationships(id, rev,true));
				item.setDepRelationships(getRelationships(id, rev,false));
				item.setCheckoutUserName(summary.getCheckoutUserName());
				item.setTitle(summary.getName());
				item.setLocale(summary.getLocale());
				item.setContentType(contentTypeName);

			}
		} catch (PSErrorException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (RepositoryException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (Exception e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error( e.getMessage());
			log.debug(e.getMessage(), e);
		} 

		return item;

	}

	public Item updateItem(Item item) {
		return updateItem(item, false);
	}

	/**
	 * Method updateItem.
	 * 
	 * @param item
	 *            Item
	 * @return Item
	 * @see IItemRestService#updateItem(Item)
	 */
	@POST
	@Path("/")
	@Consumes("text/xml")
	public Item updateItem(Item item,
			@QueryParam("updateOnly") @DefaultValue("false") boolean updateOnly) {
		log.debug("UpdateOnly set to {}", updateOnly);
		try {
	
		initServices();
		
		int id = -1;
		log.debug("Id referenced from item is {}", item.getContentId());
		if (item.getContentId() == null || item.getContentId() == -1) {
			log.debug("No content id need to locate");
			locateItem(item);
		}
	
		// Item currentItem =null;
		boolean requireCheckout = false;
		boolean requireCheckin = false;
		List<IPSGuid> guids = null;
		List<PSCoreItem> psItems = null;
		PSCoreItem psItem = null;
		PSComponentSummary summary=null;
		List<PSItemStatus> status = null;
		boolean updateItem = true;
		
		
		boolean isEdit = (item.getContentId() != null && item.getContentId() > 0);
		
			if (isEdit) {
				id = item.getContentId();

				// convert content id to guids, revision -1 should enforce
				// current version of requests.
				IPSGuid guid = gmgr.makeGuid(new PSLocator(id, -1));
				guids = Collections.singletonList(guid);
				// currentItem = getItem(id);

				// Check for timestamp modification
				if (item.getUpdatedDateField()!=null) {
					String fieldName = item.getUpdatedDateField();
					if (fieldName!=null && fieldName.length()>0) {
						Field updateField = item.getField(fieldName);
	
						if(updateField!=null){
							if (updateField.getValue() instanceof DateValue) {
								DateValue value = (DateValue)updateField.getValue();
	
								// maybe more efficient to get individual field.
								Item currentItem = getItem(id);
								if(currentItem!= null){
									Field currentField = currentItem.getField(fieldName);
									if(currentField!=null){
										DateValue currentValue = (DateValue)currentField.getValue();			
										if(value == null || value.getDate()==null || currentValue==null || currentValue.getDate()==null){
											log.debug("{} has a Null Date Value.. unable to compare.", fieldName);
										}else{
											log.debug("Comparing Date values..");
											if (!value.getDate().after(currentValue.getDate())) {
												log.debug("Skipping item that has not been modified using field {}", fieldName);
												updateItem=false;
											}
										
										}
								   }
								}
							}
						}
					}
				}
				
				if (updateItem) {
					summary = summ.loadComponentSummary(id);
					requireCheckout = summary.getContentTypeId() == 101 ? false
							: true;
					switchCommunity(summary.getCommunityId());

					
					psItems = cws.loadItems(guids, true, true, false,false);
					psItem = psItems.get(0);
				}
			} else {
				
				// New item
				switchCommunity(isi.getCommunityId(item.getCommunityName()));

				String contentType = item.getContentType();
				if (contentType == null) {
					log.error("Content Type not specified for import create");
					throw new ItemRestException("Content type not specified for new item");
				}
				psItems = cws.createItems(contentType, 1);
				psItem = psItems.get(0);
				id = psItem.getContentId();
				updateItem=true;
			}

			if (updateItem) {
			
				log.debug("Updating fields");
				updateFields(item, psItem);
				
				// FileProcessor.process(psItem); // Turned of temporarily checks
				// for file fields even if we did not set.

				// TODO : Check if folder really refers to an item
				PSRequest req = (PSRequest) PSRequestInfo
				.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
				PSServerFolderProcessor folderproc =  PSServerFolderProcessor.getInstance();

				if (item.getFolders() != null) {
					for (String folder : item.getFolders()) {
						PSComponentSummary foldersum = null;
						try {
							foldersum = folderproc.getSummary(folder);
						} catch (PSCmsException e) {
							log.debug("get summary threw exception, ignoring just want to check if item refers to a non folder");
						}
						
						if (foldersum != null
								&& !foldersum.isFolder()) {
							throw new IllegalArgumentException(
									"Folder path refers to an item that is not a folder "
									+ folder);
						}
					}
					psItem.setFolderPaths(item.getFolders());
				}

				// params, enable revisions, checkin
				log.debug("Saving item ");
				String checkoutUser="";
				String userName = "";
				
				if(updateItem){
					
					if(summary!=null){
					checkoutUser = summary.getCheckoutUserName();
					
					if (checkoutUser != null && checkoutUser.length() > 0) {
						PSRequest cxreq = (PSRequest) PSRequestInfo
						.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
						IPSRequestContext ctx = new PSRequestContext(cxreq);
						userName = ctx.getUserContextInformation(
								"User/Name", "").toString();
						log.debug("Current username is {}", userName);
						// Default to user
						String forceCheckinType = item.getForceCheckin() == null ? "user"
								: item.getForceCheckin();
						if (forceCheckinType.equals("always")
								|| (forceCheckinType.equals("user")
										&& userName.equals(checkoutUser))) {
							requireCheckin = true;
						} else {
							throw new ItemRestException("Item " + id
									+ " is checked out to user " + checkoutUser
									+ " ,not configured to force checkin");
						}
						}
					}
					
					if (requireCheckout) {
						if (requireCheckin) {
							if(!userName.equals(checkoutUser)){
								//No point checking the item in if we already have it checked out.
								cws.checkinItems(guids, "Checkin by System");
							}
						}
						
						log.debug("Getting the summary and revision number");
			

						if(isEdit){
							status = cws.prepareForEdit(guids);

							PSComponentSummary tmpSummary = summ.loadComponentSummary(psItems.get(0).getContentId());
							psItems.get(0).setEditRevision(tmpSummary.getEditLocator().getRevision());
							psItems.get(0).setRequestedRevision(tmpSummary.getEditLocator().getRevision());
							psItems.get(0).setRevision(tmpSummary.getEditLocator().getRevision());
						}
						
						// If checked out revision will have increased, relationship
						// ids will change as well as workflow
						// ephox fields will have new relationship ids also
						
					}
				}
				    log.debug("Before cws Save psItems");
					guids = cws.saveItems(psItems, false, false);
					log.debug("cws psItems Saved");
					
					if (guids.size()>0) {
						id = guids.get(0).getUUID();
						item.setContentId(id);
					}
					
					
					if (item.getRelationships()!=null) {
						updateRelationships(item, getRelationships(id, -1, true), id,
								false);
					}	
					
			    log.debug("Before Content is released from Edit");
			    
			     
				if (status != null) {
					boolean checkInOnly = item.getCheckInOnly() != null && item.getCheckInOnly()? true : false;
					PSItemStatus ps = status.get(0);	
					if(ps.getToState() != null){
					    if (ps.getToState().equals("Quick Edit")){
					 	      ps.setFromState("Review");
						      ps.setFromStateId(new Long(2));
						      status.set(0, ps);
					     }
					}
					else{
						ps.setFromState("Review");
						ps.setFromStateId(new Long(2));
						status.set(0, ps);
					}
					cws.releaseFromEdit(status,checkInOnly);
					
				} else {
					cws.checkinItems(guids, "Initial import.");
				}
				
				log.debug("Content released from Edit");

				if(!isEdit){
					
					//log.debug("Setting Request User to rxserver.");
					//PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, "rxserver");
					
					//If a transition is specified.  Give it a whirl 
					 if(item.getTransition() != null && !item.getTransition().equals("")){
						try{	
							system.transitionItems(Collections.singletonList(guids.get(0)), item.getTransition());
						}catch(Exception ex){
							try{
								log.error(ex.getMessage());
								log.debug("Draft Transition failed. {} Trying edit transition {}", item.getTransition(), item.getEditTransition() );
								system.transitionItems(Collections.singletonList(guids.get(0)), item.getEditTransition());
							}catch(Exception e){
								log.error(e.getMessage());
								log.debug(e.getMessage(), e);
								log.warn("Unable to transition item using Transition Trigger {}", item.getTransition());
								item.addError(ErrorCode.UNKNOWN_ERROR, "Unable to transition item to " + item.getState());
							}
						}
					}
				}else{
					//If a transition is specified.  Give it a whirl - only for the new items though.
					 if(item.getEditTransition() != null && !item.getEditTransition().equals("")){
						try{
							system.transitionItems(Collections.singletonList(guids.get(0)), item.getEditTransition());
						}catch(Exception e){
							try{
								log.error(e.getMessage());
								log.debug("Edit Transition failed. {} Trying new item transition {}", item.getEditTransition(), item.getTransition());
								system.transitionItems(Collections.singletonList(guids.get(0)), item.getTransition());
							}catch(Exception ex){
								log.error(ex.getMessage());
								log.warn("Unable to transition item using Transition Trigger {}", item.getEditTransition());
								item.addError(ErrorCode.UNKNOWN_ERROR, "Unable to transition item to " + item.getState());
							}
						}
					}
				}
									
				if (!updateOnly) {
					if (id > 0) {
						List<Error> errors = item.getErrors();
						item = getItem(id);
						if (errors != null) {
							if (item.getErrors() != null) {
								item.getErrors().addAll(errors);
							} else {
								item.setErrors(errors);
							}
						}
					} else {

						item.addError(ErrorCode.ASSEMBLY_ERROR,
						"Cannot get item with content id -1");
					}
				}
			} else {
				// skipping item
				item.addError(ErrorCode.SKIP, "Skipping item");
			}

		} catch (PSUserNotMemberOfCommunityException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error", e);
		} catch (PSErrorsException e) {
			for (Entry<IPSGuid, Object> error : e.getErrors().entrySet()) {
				log.error(e.getMessage());
				log.debug("Error class is {}", error.getClass().getName());
				if (error instanceof PSInternalRequestCallException) {
					PSInternalRequestCallException irce = (PSInternalRequestCallException)error;
					log.error(e.getMessage());
					log.debug("Cause is {}", irce.getCause().getMessage());
				}
				PSErrorException errorEx = (PSErrorException) error.getValue();

				int errorid = error.getKey().getUUID();

				item.addError(
						ErrorCode.UNKNOWN_ERROR,
						"Item " + errorid + " failed with error "
						+ errorEx.getErrorMessage());
				log.error("id={} message = {} stack=", errorid, errorEx.getMessage(), errorEx.getStack());
				log.error("Error", e);
			}

		} catch (PSUnknownContentTypeException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (PSErrorException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (IOException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (PSErrorResultsException e) {
			for (Entry<IPSGuid, Object> error : e.getErrors().entrySet()) {
				PSErrorException errorEx = (PSErrorException) error.getValue();
				int errorid = error.getKey().getUUID();
				item.addError(
						ErrorCode.UNKNOWN_ERROR,
						"Item " + errorid + " failed with error "
						+ errorEx.getErrorMessage());
				log.error("id={} message = {} stack={}", errorid, errorEx.getMessage(), errorEx.getStack());
				log.error("Error {}", e.getMessage());
				log.debug(e.getMessage(), e);
			}
		} catch (PSDataExtractionException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (ItemRestException e) {
			log.error("Unexpected exception",e);
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
			item.addError(e.getErrorCode(), e.getMessage());
		}catch(ItemRestNotModifiedException e){
			log.info("Skipping updates to item...");
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
		}  catch (Exception e) {
			log.error("Unexpected Exception",e);
			log.error("Error {}", e.getMessage());
			log.debug(e.getMessage(), e);
		item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
		} 

		return item;

	}

	/**
	 * Method updateItems.
	 * 
	 * @param items
	 *            Items
	 * @return Items
	 */
	@POST
	@Path("/")
	@Consumes("text/xml")
	public Items updateItems(Items items) {
		if (items != null)
			for (Item item : items.getItems()) {
				log.debug("Processing item");
				item = updateItem(item);
			} else {
				log.warn("Items is null");	
			}
		return items;
	}

	/**
	 * Method convertPSFieldtoField.
	 * 
	 * @param field
	 *            Property
	 * @param parentNode
	 *            Node
	 * @param psfield
	 *            PSField
	 * @param itemdef
	 *            PSItemDefinition
	 * @param child
	 *            boolean
	 * @return Field
	 * @throws RepositoryException
	 */
	private Field convertPSFieldtoField(Property field, Node parentNode,
			PSField psfield, PSItemDefinition itemdef, boolean child)
	throws RepositoryException {
		initServices();

		Field newField = new Field();

		// Get definition fails for child field need some better way of
		// detecting multi valued
		boolean isMultiValue = (child) ? false : field.getDefinition()
				.isMultiple();

		boolean isBinary = psfield.getDataType().equals("binary");

		newField.setName(field.getName().substring(3));
		if (isMultiValue) {
			List<Value> values = new ArrayList<Value>();
			for (javax.jcr.Value value : Arrays.asList(field.getValues())) {
				values.add(toXmlValue(value));
			}
			newField.setValues(values);
		} else if (isBinary) {
			String contentid = parentNode.getProperty("rx:sys_contentid")
			.getString();
			String revisionid = parentNode.getProperty("rx:sys_revision")
			.getString();

			String href = generateFileLink(Integer.parseInt(contentid),
					Integer.parseInt(revisionid), field.getName().substring(3));
			log.debug("href=" + href);
			FileValue binary = new FileValue();
			binary.setHref(href);

			if(inlineDoc) {
				String base64body = getBase64Field(field.getName(), parentNode);
				binary.setStringValue(base64body);
			}
			newField.setValue(binary);
		} else {
			newField.setValue(toXmlValue(field.getValue()));
		}

		return newField;
	}

	private String getBase64Field(String field, Node node) {
		String mime_prop = "application/octet-stream";
		try {
			Property prop = node.getProperty( field);
			mime_prop = node.getProperty(field + "_type").getString();
			log.debug("mime type is " + mime_prop);
			String orig = prop.getString();
			if (orig != null && orig.length() > 0) {
				return Base64Utility.encode(prop.getString().getBytes());
			}
		} catch (RepositoryException e) {
			log.debug(e);
		}
		return "";
	}

	/**
	 * Method getFromRxFields.
	 * 
	 * @param item
	 *            Node
	 * @param itemdef
	 *            PSItemDefinition
	 * @return List<Field>
	 */
	private List<Field> getFromRxFields(Node item, PSItemDefinition itemdef) {
		initServices();
		List<Field> fields = new ArrayList<Field>();

		Iterator<PSField> iterator = itemdef.getParentFields();

		while (iterator.hasNext()) {
			try {
				PSField psfield = iterator.next();
				String type = psfield.getDataType();
				String fieldname = psfield.getSubmitName();
				if (!fieldname.equals("sys_title")
						&& (type.equals("binary") || item.hasProperty("rx:"
								+ fieldname))) {
					Property field = item.getProperty("rx:" + fieldname);
					Field newField = convertPSFieldtoField(field, item,
							psfield, itemdef, false);
					fields.add(newField);
				}
			} catch (RepositoryException e) {
				log.error(e, e);
			}
		}
		return fields;
	}

	/**
	 * Method getFromRxChildren.
	 * 
	 * @param item
	 *            Node
	 * @param itemdef
	 *            PSItemDefinition
	 * @return List<Child>
	 */
	@SuppressWarnings("unchecked")
	private List<Child> getFromRxChildren(Node item, PSItemDefinition itemdef) {
		List<Child> children = new ArrayList<Child>();

		for (PSFieldSet child : itemdef.getComplexChildren()) {
			try {
				Child newChild = new Child();
				children.add(newChild);

				List<ChildRow> rows = new ArrayList<ChildRow>();

				log.debug("Adding child field set " + child.getName());
				newChild.setName(child.getName());
				newChild.setRows(rows);
				NodeIterator ni;

				ni = item.getNodes(child.getName());

				while (ni.hasNext()) {
					log.debug("Adding child row");
					Node n = ni.nextNode();

					ChildRow cr = new ChildRow();
					rows.add(cr);
					List<Field> fields = new ArrayList<Field>();
					cr.setFields(fields);
					Iterator<PSField> iterator = child.getEveryField();

					while (iterator.hasNext()) {

						try {
							PSField psfield = iterator.next();
							String type = psfield.getDataType();
							String fieldname = psfield.getSubmitName();
							log.debug("adding child field " + fieldname);
							if (type.equals("binary")
									|| n.hasProperty("rx:" + fieldname)) {
								Property field = n.getProperty("rx:"
										+ fieldname);
								Field newField = convertPSFieldtoField(field,
										n, psfield, itemdef, true);
								fields.add(newField);
							}
						} catch (RepositoryException e) {
							log.error(e, e);
						}
					}
				}

			} catch (RepositoryException e) {
				log.debug(e);
			}
		}
		return children;

	}

	/**
	 * Method toXmlValue.
	 * 
	 * @param oldValue
	 *            javax.jcr.Value
	 * @return Value
	 */
	private Value toXmlValue(javax.jcr.Value oldValue) {
		Value newValue = null;
		log.debug("Value type is " + oldValue.getType());
		try {

			if (oldValue.getString().contains("class=\"rxbodyfield\"")) {

				newValue = new XhtmlValue();

				newValue.setStringValue(oldValue.getString());
			} else if (oldValue.getType() == PropertyType.DATE) {
				DateValue dateValue = new DateValue();
				dateValue.setDate(oldValue.getDate().getTime());
				newValue = dateValue;
			} else {
				newValue = new StringValue();
				newValue.setStringValue(oldValue.getString());
			}

		} catch (ValueFormatException e) {
			log.error(e,e);
		} catch (IllegalStateException e) {
			log.error(e,e);
		} catch (RepositoryException e) {
			log.error(e,e);
		}
		return newValue;
	}

	/**
	 * Method getRelationships.
	 * 
	 * @param id
	 *            int
	 * @param rev
	 *            int
	 * @param isOwner
	 *            boolean
	 * @return Relationships
	 */
	private Relationships getRelationships(int id, int rev, boolean isOwner) {
		Relationships xmlRels = new Relationships();
		PSRelationshipFilter filter = new PSRelationshipFilter();
		List<Translation> translations = xmlRels.getTranslations();
		List<Copy> copies = xmlRels.getCopies();
		List<Slot> slots = xmlRels.getSlots();

		if (isOwner) {
			if (rev <= 0) {
				filter.setOwnerId(id);
				filter.limitToEditOrCurrentOwnerRevision(true);
			} else {
				filter.setOwner(new PSLocator(id, rev));

			}
		} else {
			filter.setDependent(new PSLocator(id, -1));
			filter.getLimitToEditOrCurrentOwnerRevision();
			filter.limitToEditOrCurrentOwnerRevision(true);
		}
		try {
			List<PSRelationship> rels = system.loadRelationships(filter);
			log.debug("found " + rels.size() + " relationships");
			for (PSRelationship rel : rels) {
				String category = rel.getConfig().getCategory();
				String name = rel.getConfig().getName();

				PSLocator dependent = rel.getDependent();
				PSLocator owner = rel.getOwner();
				int dependentId = dependent.getId();
				int cid = isOwner ? dependent.getId() : owner.getId();
				int revision = isOwner ? dependent.getRevision() : owner
						.getRevision();
				log.debug("category = " + category);
				log.debug("Name = " + name);
				log.debug("dependentid=" + dependentId);
				log.debug("ownerid=" + dependentId);
				log.debug("revision = " + revision);

				Map<String, String> props = rel.getAllProperties();
				for (Entry<String, String> entry : props.entrySet()) {
					log.debug("Prop name=" + entry.getKey() + " value="
							+ entry.getValue());
				}

				if (category.equals(PSRelationshipConfig.CATEGORY_TRANSLATION)) {
					PSComponentSummary summary = summ.loadComponentSummary(cid);
					String locale = summary.getLocale();
					Translation trans = new Translation();
					trans.setLocale(locale);
					trans.setContentId(cid);
					trans.setHref(generateItemLink(cid, revision));
					trans.setRelId(rel.getId());
					trans.setRevision(revision);

					if (translations == null) {
						translations = new ArrayList<Translation>();
					}
					translations.add(trans);
				} else if (category.equals(PSRelationshipConfig.CATEGORY_COPY)) {

					Copy copy = new Copy();
					copy.setContentId(cid);
					copy.setRelId(rel.getId());

					copy.setRevision(revision);
					copy.setHref(generateItemLink(cid, revision));
					if (copies == null) {
						copies = new ArrayList<Copy>();
					}
					copies.add(copy);
				} else if (category
						.equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY)) {
					log.debug("This is an AA Relationship");
					SlotItem newSlotItem = new SlotItem();
					String slotid = props.get("sys_slotid");

					String templateid = props.get("sys_variantid");
					String siteid = props.get("sys_siteid");
					String sortrank = props.get("sys_sortrank");
					String folderid = props.get("sys_folderid");

					newSlotItem.setContentId(cid);
					newSlotItem.setRelId(rel.getId());

					newSlotItem.setRevision(revision);
					newSlotItem.setHref(generateItemLink(cid, revision));
					if (templateid != null) {
						newSlotItem.setTemplate(isi.getTemplateName(Integer
								.parseInt(templateid)));
					}

					if (siteid != null) {
						newSlotItem.setSite(isi.getSiteName(Integer
								.parseInt(siteid)));
					}

					if (folderid != null) {
						newSlotItem.setFolder(isi.getFolderPath((Integer
								.parseInt(folderid))));
					}
					if (sortrank != null) {
						newSlotItem.setSortRank(Integer.parseInt(sortrank));
					}
					String slotname = null;
					if (slotid != null) {
						slotname = isi.getSlotName(Integer.parseInt(slotid));

						log.debug("Slotname is " + slotname);
						Slot slot = new Slot();
						slot.setName(slotname);
						if (slots == null)
							slots = new ArrayList<Slot>();

						if (!slots.contains(slot)) {
							slot.setType(rel.getConfig().getName());
							slots.add(slot);
							log.debug("cannot find slot " + slot.getName());
						} else {
							log.debug("slot already exists");
							slot = slots.get(slots.indexOf(slot));
							log.debug("Got existing slot" + slot.getName());
						}
						if (slot != null) {
							List<SlotItem> items = slot.getItems();
							if (items == null)
								items = new ArrayList<SlotItem>();
							log.debug("Adding new slot item to list");
							items.add(newSlotItem);
							// Better to do all sorting in one go
							Collections.sort(items);
							slot.setItems(items);
						}
					}

				}
			}

			xmlRels.setCopies(copies);
			xmlRels.setSlots(slots);
			xmlRels.setTranslations(translations);

		} catch (PSErrorException e) {
			log.error(e,e);
		}
		return xmlRels;

	}

	/**
	 * Method locateItem.
	 * 
	 * @param item
	 *            Item
	 * @throws ItemRestException 
	 */
	public void locateItem(Item item) throws ItemRestException {
		int foundId = -1;
		if (item.getContentId() != null && item.getContentId() > 0) {
			log.debug("Item already located, contentid is "
					+ item.getContentId());
		} else {
			String keyField = item.getKeyField();
			if (keyField == null) {
				log.error("No Content Id or keyfield specified for item cannot locate");
				return;
			}
			
			
			if (keyField.equals("rxpath")) {
				 String name = item.getTitle();
				 List<String> paths = item.getFolders();
				 PSRequest req = (PSRequest) PSRequestInfo
					.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
					PSServerFolderProcessor folderproc =  PSServerFolderProcessor.getInstance();
				
			
				 for (String path : paths) {
					 String fullpath = path+"/"+name;
					 int id=-1;
					try {
						id = folderproc.getIdByPath(fullpath);
					} catch (PSCmsException e) {
						log.debug("Cannot get id by path for path "+fullpath,e);
						throw new ItemRestException("Cannot get id by path for path "+fullpath);
					}
					 
					 if (id>0) {
						 if (foundId==-1) {
							 foundId = id;
							 log.debug("Found id by path for "+fullpath+"  setting item id to "+id);
							 item.setContentId(id);
						 } else {
								throw new ItemRestException("Item specifies multiple folders locating by path but currently these a different items "+id+" and "+foundId);
						 }
					 }
						 
				 }
				 
				 if (foundId==-1) {
					 log.debug("Cannot locate item create?");
						if (item.getUpdateType() != null
								&& item.getUpdateType().equals("ref")) {
						
							throw new ItemRestException(ErrorCode.NOT_FOUND,"cannot locate item by path and item is a reference title="+name+" pahts="+paths);
							
						}

				 }
				
				 
			} else if (keyField.equals("nav")) {
			    List<String> paths = item.getFolders();
			    if (paths.size()>1) {
			    	throw new ItemRestException("Navigation items should never be in more than one folder :"+paths);
			    } else if (paths.size()==0){
			    	throw new ItemRestException("Navigation items need to be in one folder :"+paths);	  
			    } else {
			    	int navonId = findNavIdForFolderPath(paths.get(0));
			    	log.debug("located nav item for folder "+paths.get(0)+" : "+navonId);
			    	 item.setContentId(navonId);
			    }
			    if (foundId==-1) {
					 log.debug("Cannot locate item create?");
						if (item.getUpdateType() != null
								&& item.getUpdateType().equals("ref")) {
						
							throw new ItemRestException(ErrorCode.NOT_FOUND,"cannot locate navitem by path and item is a reference folder="+paths);
							
						}

				 }
			} else {
			String value = null;
				for (Field field : item.getFields()) {
					if (field.getName().equals(keyField)) {
						value = field.getValue().getStringValue();
						break;
					}
				}
				if (value == null) {
					log.error("Cannot get value for keyfield " + keyField);
					return;
				}
				String query = "select rx:sys_contentid from nt:base";
				String where = "rx:"
					+ keyField + "='" + value + "'";
				String path = item.getContextRoot();
	
				PagedResult res = jcrSearch(query, 1,where, null);
				int size = res.getItemRefs().size();
				if (size == 0) {
					log.error("Cannot locate item create?");
					if (item.getUpdateType() != null
							&& item.getUpdateType().equals("ref")) {
						log.error("Cannot locate item and item is a reference");
						item.addError(ErrorCode.NOT_FOUND,
								"Cannot locate keyfield " + keyField + " path="
								+ path);
	
					}
				} else if (size > 1) {
					throw new ItemRestException("Duplicate items detected for keyfield " + keyField
							+ " path=" + path);
				} else if (size==1){
					foundId = res.getItemRefs().get(0).getContentId();
					item.setContentId(foundId);
					log.debug("located item id=" + foundId);
				}
				
				
				if (foundId==-1) {
					 log.debug("Cannot locate item create?");
						if (item.getUpdateType() != null
								&& item.getUpdateType().equals("ref")) {
						
							throw new ItemRestException(ErrorCode.NOT_FOUND,"cannot locate item by path and item is a reference keyfield="+keyField+" path="+path);
							
						}

				 }
				
			}

		}
	    
	}

	/**
	 * Method updateRelationships.
	 * 
	 * @param item
	 *            Item
	 * @param currentRels
	 *            Relationships
	 * @param check
	 *            boolean
	 * @return boolean
	 */
	private boolean updateRelationships(Item item, Relationships currentRels,
			int id, boolean check) {
		PSLocator owner = new PSLocator(id, -1);
		Relationships updateRels = item.getRelationships();
		if (updateRels != null) {
			PSRelationshipProcessor proc = PSWebserviceUtils
			.getRelationshipProcessor();

			List<Slot> updateSlots = updateRels.getSlots();
			if (updateSlots != null) {

				// TODO: Best to calculate slot type based upon slot itself,
				// Currently rely on type to be specified.
				for (Slot slot : updateSlots) {
					log.debug("Updating slot " + slot.getName());
					List<SlotItem> updateItems = slot.getItems();
					List<SlotItem> existingItems = new ArrayList<SlotItem>();
					if (currentRels != null && currentRels.getSlots() != null) {

						for (Slot existSlot : currentRels.getSlots()) {
							if (existSlot.getName().equals(slot.getName())) {
								existingItems.addAll(existSlot.getItems());
							}
						}

					}

						if (check) {
							return true;
						} else {
							// We now know relationships have changed easiest to
							// Delete all and re-add due to ordering.

							List<Relationship> relsToDelete = new ArrayList<Relationship>(
									existingItems);
							log.debug("slot type is" + slot.getType());
							deleteRelationships(owner, slot.getType(),
									relsToDelete);
							IPSGuid ownerGuid = gmgr.makeGuid(owner);
							for (SlotItem itemToAdd : updateItems) {

								try {
									List<IPSGuid> relGuids = Collections
									.singletonList(gmgr.makeGuid(new PSLocator(
											itemToAdd.getContentId(),
											-1)));

									// TODO: Need to cache id lookups.
									IPSGuid folderId = null;
									log.debug("Getting slot id");

									IPSGuid slotId = aService.findSlotByName(
											slot.getName()).getGUID();
									IPSGuid siteId = null;
									IPSGuid templateId = null;
									log.debug("Getting folder id");
									if (itemToAdd.getFolder() != null) {
										List<IPSGuid> folderIds = cws
										.findPathIds(itemToAdd
												.getFolder());
										if (folderIds.size() > 0) {
											// returns all parts of path, get
											// last for id of this folder.
											folderId = folderIds.get(folderIds
													.size());
										} else {
											log.error("Cannot get guid for folder "
													+ itemToAdd.getFolder());
										}
									}
									log.debug("Getting template id");
									if (itemToAdd.getTemplate() != null) {
										templateId = aService
										.findTemplateByName(
												itemToAdd.getTemplate())
												.getGUID();
									}
									log.debug("Getting site id");
									if (itemToAdd.getSite() != null) {
										siteId = sitemgr.findSiteByName(
												itemToAdd.getSite()).getGUID();
									}

									log.debug("creating relationships");
									cws.addContentRelations(ownerGuid,
											relGuids, folderId, siteId, slotId,
											templateId, -1);
								} catch (Exception e) {
									log.debug("Cannot create relationship ", e);
								}
							}

						}
					
				}
			}
			if (updateRels.getTranslations() != null) {
				List<Translation> updateTrans = new ArrayList<Translation>(
						updateRels.getTranslations());
				List<Translation> existingTrans = new ArrayList<Translation>();

				if (currentRels != null
						&& currentRels.getTranslations() != null) {
					existingTrans.addAll(currentRels.getTranslations());
				}

				List<Relationship> itemsToDelete = new ArrayList<Relationship>(
						existingTrans);
				itemsToDelete.removeAll(updateTrans);
				log.debug("Need to remove " + itemsToDelete.size()
						+ " relationships");

				List<Translation> itemsToAdd = new ArrayList<Translation>(
						updateTrans);
				itemsToAdd.removeAll(existingTrans);
				log.debug("Need to add " + itemsToAdd.size() + " relationships");

				if (itemsToDelete.size() > 0) {
					if (check) {
						return true;
					} else {

						deleteRelationships(owner,
								PSRelationshipConfig.TYPE_TRANSLATION,
								itemsToDelete);

					}
				}
				log.debug("Adding relationships for id=" + owner.getId()
						+ " revision=" + owner.getRevision());

				if (itemsToAdd.size() > 0) {
					if (check) {
						return true;
					} else {
						try {
							PSRelationshipConfig relconf = proc
							.getConfig(PSRelationshipConfig.TYPE_TRANSLATION);
							PSRelationshipSet relSet = new PSRelationshipSet();
							for (Translation trans : itemsToAdd) {
								PSLocator dependent = new PSLocator(
										trans.getContentId(), -1);
								PSRelationship newRel = new PSRelationship(-1,
										owner, dependent, relconf);
								// Possibly test whether locale in Translation
								// matches local of actual dependent
								relSet.add(newRel);
							}

							proc.save(relSet);
						} catch (PSCmsException e) {
							log.debug("Cannot add Relationships with config "
									+ PSRelationshipConfig.TYPE_TRANSLATION, e);
						}
					}
				}
			}

		}
		return false;

	}

	/**
	 * Method deleteRelationships.
	 * 
	 * @param owner
	 *            PSLocator
	 * @param type
	 *            String
	 * @param rels
	 *            List<Relationship>
	 */
	private void deleteRelationships(PSLocator owner, String type,
			List<Relationship> rels) {
		PSRelationshipProcessor proc = PSWebserviceUtils
		.getRelationshipProcessor();

		log.debug("Deleting relationships for id={} revision={}", owner.getId(), owner.getRevision());
		int[] rids = new int[rels.size()];
		for (int i = 0; i < rels.size(); i++) {
			rids[i] = rels.get(i).getRelId();
			log.debug("Deleting rid={}", rids[i]);
		}

		try {
			proc.delete(type, owner, rids);
			log.debug("Deleted Relationships");
		} catch (PSCmsException e) {
			log.error("Cannot delete relationships", e);
		}

	}

	/**
	 * Method getItemXml.
	 * 
	 * @param item
	 *            Item
	 * @return String
	 */
	public String getItemXml(Item item) {
		StringWriter sw = new StringWriter();
		try {

			JAXBContext jc = JAXBContext
			.newInstance(new Class[] { Item.class });
			Marshaller m = jc.createMarshaller();
			m.setProperty("jaxb.fragment", Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(item, sw);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		}
		sw.flush();
		return sw.toString();
	}

	/**
	 * Method getItemFromStream.
	 * 
	 * @param is
	 *            InputStream
	 * @return Item
	 * @throws JAXBException
	 */
	public Item getItemFromStream(InputStream is) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(new Class[] { Item.class });
		Unmarshaller um = jc.createUnmarshaller();
		Item item = (Item) um.unmarshal(is);
		return item;
	}

	/**
	 * Method getItemDOM.
	 * 
	 * @param item
	 *            Item
	 * @return Document
	 */
	public Document getItemDOM(Item item) {
		DocumentResult dr = new DocumentResult();
		try {

			JAXBContext jc = JAXBContext
			.newInstance(new Class[] { Item.class });
			Marshaller m = jc.createMarshaller();
			m.setProperty("jaxb.fragment", Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(item, dr);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		}

		return dr.getDocument();
	}

	private PagedResult jcrSearch(String q, Integer n, String path) {
		return jcrSearch(q, n, "", path);
	}

	private PagedResult jcrSearch(String q, Integer n, String w, String path) {
		initServices();
		Map<String, String> pmap = new HashMap<String, String>();
		PagedResult resultPage = new PagedResult();
		List<ItemRef> refs = new ArrayList<ItemRef>();
		Set<Integer> ids = new TreeSet<Integer>();
		Query query;
		if (n == null)
			n = 1;

		RowIterator rows = null;

		try {
			boolean moreResults = true;
			while (moreResults && ids.size() < PAGE_SIZE) {
				String where = " where rx:sys_contentid >" + n.toString();
				if (w.length() > 0) {
					where += " and " + w + " ";
				}
				if (path != null && path.length() > 1 && path.startsWith("/")) {
					where += " and jcr:path like '" + path + "%" + "'";
				}
				log.debug("Starting query " + q + where);
				query = contentMgr.createQuery(q + where
						+ " order by rx:sys_contentid", Query.SQL);
				QueryResult qresults = contentMgr.executeQuery(query,
						PAGE_SIZE + 10, pmap);
				log.debug("Query returned ");
				rows = qresults.getRows();
				int rowcount = 0;
				while (rows.hasNext() && ids.size() < PAGE_SIZE) {
					rowcount++;
					Row row = rows.nextRow();
					String contentid = row.getValue("rx:sys_contentid")
					.getString();
					int id = Integer.valueOf(contentid);
					ids.add(id);
					n = id;
				}
				if (rowcount == 0)
					moreResults = false;
			}

			if (ids.size() == PAGE_SIZE && moreResults) {
				resultPage.setNextId(n);
				resultPage.setNext(generatePagedLink(n));
			}

			for (Integer id : ids) {
				ItemRef ref = new ItemRef();
				ref.setContentId(id);
				ref.setHref(generateItemLink(id, -1));
				refs.add(ref);
			}
			resultPage.setItemRefs(refs);
		} catch (InvalidQueryException e) {
			log.error(e,e);
		} catch (RepositoryException e) {
			log.error(e,e);
		}
		return resultPage;
	}

	/**
	 * Method getItem.
	 * 
	 * @param id
	 *            int
	 * @return Item
	 * @see IItemRestService#getItem(int)
	 */
	@GET
	@Path("{id}")
	public Item getItem(@PathParam("id") int id) {
		log.debug("Getting item id=" + id);
		return getFromRxItem(id, -1);
	}

	/**
	 * Method getItemRev.
	 * 
	 * @param id
	 *            int
	 * @param rev
	 *            int
	 * @return Item
	 * @see IItemRestService#getItemRev(int, int)
	 */
	@GET
	@Path("{id}/{rev}")
	public Item getItemRev(@PathParam("id") int id, @PathParam("rev") int rev) {
		return getFromRxItem(id, rev);
	}

	/**
	 * Method updateFields.
	 * 
	 * @param item
	 *            Item
	 * @param psItem
	 *            PSCoreItem
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ItemRestException
	 * @throws ItemRestNotModifiedException 
	 */
	private void updateFields(Item item, PSCoreItem psItem)
	throws FileNotFoundException, IOException, ItemRestException, ItemRestNotModifiedException {
		boolean titleFieldSet = false;
		if (item.getFields() != null) {
			for (Field field : item.getFields()) {
				PSItemField psField = psItem.getFieldByName(field.getName());
				if (psField != null) {
					if (field.getName().equals("sys_title"))
						titleFieldSet = true;
					updateFieldValue(field, psField);
				} else {
					log.debug("Cannot find field " + field.getName()
							+ " Ignoring");
				}
			}

			// Pull sys_title from attribute
			if (!titleFieldSet && item.getTitle() != null
					&& item.getTitle().length() > 0) {
				PSItemField psField = psItem.getFieldByName("sys_title");
				psField.addValue(new PSTextValue(item.getTitle()));
			}

		}

	}

	/**
	 * Method updateFieldValue.
	 * 
	 * @param field
	 *            Field
	 * @param psField
	 *            PSItemField
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ItemRestException
	 * @throws ItemRestNotModifiedException 
	 */
	private void updateFieldValue(Field field, PSItemField psField)
	throws FileNotFoundException, IOException, ItemRestException, ItemRestNotModifiedException {

	psField.clearValues();

		log.debug("updating field " + field.getName());

		if (field.getValues() != null) {
			for (Value value : field.getValues()) {
				IPSFieldValue newValue = getFieldValue(value, psField);
				if (newValue!=null) {
					psField.addValue(newValue);
				}
			}
		} else {
			if (field.getValue() != null) {
				IPSFieldValue newValue = getFieldValue(field.getValue(),psField);
				if (newValue!=null) {
					psField.addValue(newValue);
				}
			} else {
				log.error("Field value is null");
			}
		}
	}

	/**
	 * Method getFieldValue.
	 * 
	 * @param value
	 *            Value
	 * @param psField
	 * @return IPSFieldValue
	 * @throws ItemRestException
	 * @throws ItemRestNotModifiedException 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private IPSFieldValue getFieldValue(Value value, PSItemField psField) throws ItemRestException, ItemRestNotModifiedException {
		PSItemFieldMeta m_fieldMeta = psField.getItemFieldMeta();

		IPSFieldValue newValue = null;
		IPSFieldValue oldValue = psField.getValue();
		
		String dataType = m_fieldMeta.getFieldDef().getDataType();
		log.debug("Field "+psField.getName()+"DataType is "+dataType);
		if (m_fieldMeta.isBinary())
		{
			if (value instanceof FileValue) {
				FileValue binValue = (FileValue)value;

				String base64 = binValue.getStringValue();
				if (base64 != null && base64.length()>0) {
					try {
						newValue = new PSBinaryValue(Base64Utility.decode(base64));
					} catch (Base64Exception e) {
						throw new ItemRestException("Cannot base64 decode file body for field " +psField.getName());
					}

				} else if (binValue.getHref()!=null){
		
					URI ref=null;
					try {
						ref = new URI(binValue.getHref());
					} catch (URISyntaxException e1) {
						log.debug(e1,e1);
				        throw new ItemRestException("Error processing " + binValue.getHref() );						
					}
					
					if(ref.isAbsolute()){
						log.debug("Using file "+ binValue.getHref());
						File file = new File(binValue.getHref());

							if (file.exists()) {
								try {
									newValue = new PSBinaryFileValue(file);
								} catch (FileNotFoundException e) {
									throw new ItemRestException("Cannot find file to upload to field "+psField.getName()+":"+file.getAbsolutePath(),e);
								} catch (IOException e) {
									throw new ItemRestException("Error reading file to upload to field "+psField.getName()+":"+file.getAbsolutePath(),e);

								}
					}else{
						
						HttpClient client = new HttpClient();
						
						//Set up the proxy server if there is one.
						HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
						
						if(!proxy.getProxyServer().equals("")){
							log.debug("Setting Proxy server to " + proxy.getProxyServer()+ ":" + proxy.getProxyPort());
							client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
						}
						client.getParams().setConnectionManagerTimeout(2000);
					
					
						
						GetMethod get=null;
						PSPurgableTempFile tmp=null;
						try{
	
							get = new GetMethod(ref.toURL().toString());
							
							//Add the modification check headers if we have valid params.
//							if(binValue.getETag()!=null && !binValue.getETag().trim().equals("")){
//								get.addRequestHeader(HTTP_IFNONEMATCH, binValue.getETag());
//							}
//							
//							if(binValue.getLastModified()!=null && !binValue.getLastModified().trim().equals("")){
//								get.addRequestHeader(HTTP_IFMODIFIED,binValue.getLastModified());
//							}
							
   						   int  code = client.executeMethod(get);

					      if(code != HttpStatus.SC_OK) {
					        log.error("Unable to fetch remote resource, status code: " + code);
					        throw new ItemRestException("Error processing " + binValue.getHref() + " HTTP request failed.");
					      }

					      tmp = new PSPurgableTempFile("pso", null, null);
					      FileOutputStream out = new FileOutputStream((File)tmp);
					      
					        byte[] buffer = new byte[1024];
							int count = -1;
							MessageDigest md = MessageDigest.getInstance("MD5");
							DigestInputStream in = new DigestInputStream(get.getResponseBodyAsStream(),md);
							
							while ((count = in.read(buffer)) != -1) {
								out.write(buffer, 0, count);
							}
							out.flush();
							out.close();
							in.close();
							
							//Now compare the digest to the version in the system already. 
							byte[] new_md5 =  md.digest();
							log.debug("New Item MD5 Checksum is " + new_md5);
							byte[] old_md5;						
							//newValue = new PSPurgableFileValue(tmp);					

							
							if(oldValue!=null){
								PSPurgableTempFile old_t=null;
								if(oldValue instanceof PSBinaryValue){
									PSBinaryValue t = (PSBinaryValue)oldValue;
									old_t = t.getValueFile();
								}
								
							//Only need to compare checksums if the file size is the same;
								md.reset();
								in = new DigestInputStream(new FileInputStream(old_t),md);
								while ((count = in.read(buffer)) != -1) {}
								
								old_md5 = md.digest();
								log.debug("Existing item MD5 Checksum = " +  old_md5);		
								if(Arrays.equals(old_md5,new_md5)){
									log.debug("Checksums Match!  No need to import this value!");
									throw new ItemRestNotModifiedException("Checksums Match!  No need to import this file!");
								}
							}
							
							
						} catch (MalformedURLException e) {
							log.debug(e,e);
							throw new ItemRestException("Error processing " + binValue.getHref(), e);
						} catch (HttpException e) {
							log.debug(e,e);
							throw new ItemRestException("Error processing " + binValue.getHref(), e);
						} catch (IOException e) {
							log.debug(e,e);
							throw new ItemRestException("Error processing " + binValue.getHref(), e);
						} catch (NoSuchAlgorithmException e) {
							log.debug(e,e);
						}finally{
							if(get!=null)
								get.releaseConnection();
						}
						
					}

					
					} else {
						log.debug("No File value removing field contents");
						newValue = new PSBinaryValue(new byte[]{});
					}

				}


				//	newValue = new PSBinaryFileValue(resultText.getBytes());
			}
		} else if (dataType.equals(PSField.DT_BOOLEAN)){
			log.debug("Found boolean fields value is ");
			String val = (value.getStringValue().equals("true") || value.getStringValue()=="1") ? "1":"0";
			newValue = new PSTextValue(val);
		}
		else if ((m_fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_TEXT) 
				|| (m_fieldMeta.getBackendDataType()
						== PSItemFieldMeta.DATATYPE_NUMERIC)){
			newValue = new PSTextValue(value.getStringValue());
		} else if (m_fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_DATE) {
			if(((DateValue)value) != null)
				newValue = new PSDateValue( ((DateValue)value).getDate()); 
		}

		return newValue;
	}

	/**
	 * Method updateItem.
	 * 
	 * @param id
	 *            int
	 * @param item
	 *            Item
	 * @return Item
	 * @see IItemRestService#updateItem(int,
	 *      Item)
	 */
	@POST
	@Path("{id}")
	@Consumes("text/xml")
	public Item updateItem(@PathParam("id") int id, Item item) {
		initServices();
		log.debug("Id referenced from path is " + id);
		if (item.getContentId() == null) {
			item.setContentId(id);
		} else if (item.getContentId().intValue() != id) {
			item.addError(ErrorCode.UNKNOWN_ERROR, "Content id from path different than content id specified in item");
			return item;
		}
		return updateItem(item);
	}

	/**
	 * Method purgeItem.
	 * 
	 * @param id
	 *            int
	 * @return Item
	 * @see IItemRestService#purgeItem(int)
	 */
	@DELETE
	@Path("{id}")
	public Item purgeItem(@PathParam("id") int id) {
		initServices();
		Item item = new Item();
		item.setContentId(id);
		try {
			PSRequest req = (PSRequest) PSRequestInfo
			.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

			IPSRequestContext ctx = new PSRequestContext(req);
			String userName = ctx.getUserContextInformation("User/Name", "")
			.toString();

			String session = req.getServerRequest().getUserSession().getId();

			log.debug("Found user " + userName);
			log.debug("Found session " + session);

			log.debug("Deleting item " + id);
			// convert content id to guids, revision -1 should enforce
			// current version of requests.
			IPSGuid guid = gmgr.makeGuid(new PSLocator(id, -1));
			List<IPSGuid> guids = Collections.singletonList(guid);
			Item currentItem = getItem(id);
			boolean requireCheckout = currentItem.getContentType().equals(
			"Folder") ? false : true;
			if (currentItem.getErrors() != null
					&& currentItem.getErrors().size() > 0) {
				item.setErrors(currentItem.getErrors());
			} else {

				// Change to correct community. Folders do not return community
				// for allCommunities id =-1
				if (currentItem.getCommunityName() != null
						&& currentItem.getCommunityName().length() > 0) {
					system.switchCommunity(currentItem.getCommunityName());
				}
				// Force checkin item. checkin is ok even if it is already
				// checked in
				// This ensures all ateims are in the correct checked in state
				// before processing.
				// Revision may increment if it is checked out but there will be
				// a snapshot of the item before
				// we modify it.

				if (requireCheckout) {
					log.debug("Forcing Checking in item now");
					cws.checkinItems(guids, "Forced checkin by Importer");
					cws.prepareForEdit(guids);
				}

				cws.deleteItems(guids);
				item.addError(ErrorCode.PURGED);

			}
		} catch (PSDataExtractionException e) {
			// TODO Auto-generated catch block
			log.error("Cannot Purge item", e);
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
		} catch (PSUserNotMemberOfCommunityException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.debug("Current user is not in community of item, ", e);
		} catch (PSErrorsException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e,e);
		} catch (PSErrorResultsException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e,e);
		} catch (PSErrorException e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e,e);

		} catch (Exception e) {
			item.addError(ErrorCode.UNKNOWN_ERROR, e.getMessage());
			log.error(e,e);
		}
		return item;
	}

	@DELETE
	@Path("/PurgeFolder/{target:.*}")
	public Response PurgeAllFolderContent(@PathParam("target")String target) {
		
		//TODO
		
		ResponseBuilder builder = Response.status(Status.OK);
		builder.type("text/plain");
		builder.entity(target + " deleted successfully");
	
		return builder.build();
	}
	
	/**
	 * Method updateItem.
	 * 
	 * @param templateName
	 *            String
	 * @param body
	 *            String
	 * @param debug
	 *            boolean
	 * @return Items
	 */
	@POST
	@Path("import/{template}")
	public Items updateItem(@PathParam("template") String templateName,
			String body, @QueryParam("debug") boolean debug,
			@QueryParam("param") String param) {
		initServices();
		log.debug("Import template is " + templateName);
		/*
		 * PSRequest req2 = (PSRequest) PSRequestInfo
		 * .getRequestInfo(PSRequestInfo.KEY_PSREQUEST); HttpServletRequest req
		 * = req2.getServletRequest();
		 */
		Items items = new Items();
		IPSAssemblyItem asmItem = new PSAssemblyWorkItem();
		Items output = null;
		String assemblyResult = "";
		try {

			IPSAssemblyTemplate template = aService
			.findTemplateByName(templateName);

			Map<String, Object> bindings = new HashMap<String, Object>();

			asmItem.setParameterValue("sys_itemfilter", "preview");
			asmItem.setParameterValue("sys_template",
					String.valueOf(template.getGUID().getUUID()));
			asmItem.setParameterValue("sys_contentid", "1");
			PSLegacyGuid guid = new PSLegacyGuid(new PSLocator(1, -1));
			asmItem.setId(guid);
			asmItem.setFilter(filter.findFilterByName("preview"));
			
			log.debug("Import Body is " + body);
			
			bindings.put("$importbody", body);
			bindings.put("$importparam", param);
			
			asmItem.setBindings(bindings);
			asmItem.setTemplate(template);

			List<IPSAssemblyResult> asmResult = aService.assemble(Collections
					.singletonList(asmItem));
			log.debug("Got assembly Result");
			// Item output =
			// ItemServiceHelper.getItemFromXml(asmResult.get(0).getResultStream());

			InputStream is = asmResult.get(0).getResultStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			assemblyResult = sb.toString();
		} catch (Exception e) {
			items.addError(ErrorCode.ASSEMBLY_ERROR, "Assembly Error", e);
		}
		try {
			log.debug("Assembly result is " + assemblyResult);
			output = ItemServiceHelper.getItemsFromXml(assemblyResult);

			if (output != null) {
				log.debug("Got Items");
				items = output;

				if (!debug) {
					try {
						items = updateItems(output);
					} catch (Exception e) {
						items.addError(ErrorCode.ASSEMBLY_ERROR,
								"Error importing item" + assemblyResult, e);
					}
				}

			}
		} catch (Exception e) {
			items.addError(ErrorCode.ASSEMBLY_ERROR,
					"Assembly output xml invalid:" + assemblyResult, e);
		}		
		return items;
	}

	/**
	 * Method getFolders.
	 * 
	 * @param n
	 *            Integer
	 * @return PagedResult
	 * @see IItemRestService#getFolders(Integer)
	 */
	@GET
	@Path("/Folders/")
	public PagedResult getFolders(@QueryParam("n") Integer n) {
		return jcrSearch("select rx:sys_contentid from rx:folder ", n, "/");
	}

	/**
	 * Method getItems.
	 * 
	 * @param n
	 *            Integer
	 * @return PagedResult
	 * @see IItemRestService#getItems(Integer)
	 */
	@GET
	@Path("/AllContent/")
	public PagedResult getItems(@QueryParam("n") Integer n) {
		return jcrSearch("select rx:sys_contentid from nt:base ", n, "/");
	}

	/**
	 * Method getItems.
	 * 
	 * @param path
	 *            String
	 * @param n
	 *            Integer
	 * @return PagedResult
	 * @see IItemRestService#getItems(String,
	 *      Integer)
	 */
	@GET
	// On upgrade use @Path("/Sites/{search:.*}") remove limited
	// @Path(value="/Sites/{search}", limited=false)
	@Path("/Sites/{search:.*}")
	public PagedResult getItems(@PathParam("search") String path,
			@QueryParam("n") Integer n) {
		return jcrSearch("select rx:sys_contentid from nt:base ", n, "/Sites/"
				+ path + "%");
	}
	
	@GET
	@Path("/Type/{typename}")
	public PagedResult getTypeItems(@PathParam("typename") String type,
			@QueryParam("n") Integer n) {
		return jcrSearch("select rx:sys_contentid from "+type+" ", n, "");
	}

	
	
	/**
	 * Method generateItemLink.
	 * 
	 * @param contentid
	 *            int
	 * @param revision
	 *            int
	 * @return String
	 */
	public String generateItemLink(int contentid, int revision) {
		// builder starts with current URI and has appended path of getCustomer
		// method
		String stResult = "";
		if (uri != null) {
			UriBuilder ub = uri.getBaseUriBuilder().clone();
			URI result;
			if (revision > 0) {
				result = ub
				.path(this.getClass())
				.path("{id}")
				.path("{rev}")
				.build(String.valueOf(contentid),
						String.valueOf(revision));
			} else {
				result = ub.path(this.getClass()).path("{id}")
				.build(String.valueOf(contentid));
			}
			stResult = result.toASCIIString();
		}
		return stResult;
	}

	/**
	 * Method generateItemLink.
	 * 
	 * @param contentid
	 *            int
	 * @param revision
	 *            int
	 * @return String
	 */
	public String generateFileLink(int contentid, int revision, String field) {
		// builder starts with current URI and has appended path of getCustomer
		// method
		String stResult = "";
		if (uri != null) {
			UriBuilder ub = uri.getBaseUriBuilder().clone();
			URI result;
			if (revision > 0) {
				result = ub
				.path(IItemRestService.class)
				.path("{id}/{rev}/field/{fieldname}")
				.build(String.valueOf(contentid),
						String.valueOf(revision), field);
			} else {
				result = ub.path(IItemRestService.class)
				.path("{id}/field/{fieldname}")
				.build(String.valueOf(contentid), field);
			}
			stResult = result.toASCIIString();
		}
		return stResult;
	}

	/**
	 * Method generatePagedLink.
	 *
	 * @param next
	 *            int
	 * @return String
	 */
	public String generatePagedLink(int next) {
		UriBuilder builder = this.uri.getAbsolutePathBuilder();

		builder.queryParam("n", Integer.valueOf(next).toString());
		log.debug("Paged Link is =" + builder.build().toASCIIString());
		return builder.build().toASCIIString();
	}

	@GET
	@Path("{id}/field/{fieldname}")
	@Produces("*/*")
	public Response getFile(@PathParam("id") int id,
			@PathParam("fieldname") String field) {
		initServices();

		log.debug("Getting file item id=" + id + " field=" + field);
		PSComponentSummary summary = summ.loadComponentSummary(id);
		PSLocator head = summary.getHeadLocator();
		return getFile(head.getId(), head.getRevision(), field);
	}

	@GET
	@Path("{id}/{rev}/field/{fieldname}")
	@Produces("*/*")
	public Response getFile(@PathParam("id") int id,
			@PathParam("rev") int revision, @PathParam("fieldname") String field) {
		initServices();

		log.debug("Getting file item id=" + id + " field=" + field);

		PSLocator head = new PSLocator(id, revision);
		List<IPSGuid> guids = Collections.singletonList(gmgr.makeGuid(head));

		Node node;
		InputStream is = null;
		String mime_prop = "application/octet-stream";
		try {
			node = contentMgr.findItemsByGUID(guids, null).get(0);
			Property prop = node.getProperty("rx:" + field);
			mime_prop = node.getProperty("rx:" + field + "_type").getString();
			log.debug("mime type is " + mime_prop);
			if (prop.getValue()!=null){
				log.debug("Value class is " + prop.getValue().getClass());
				is = prop.getStream();
			}
		} catch (RepositoryException e) {
			log.debug(e);
		}
		ResponseBuilder rb;
		if (is!=null) {
			rb = Response.ok(is, mime_prop);
		} else {
			rb = Response.status(Status.NOT_FOUND);
		}
		return rb.build();

	}




	public void switchCommunity(int communityid)
	throws PSUserNotMemberOfCommunityException {
		PSRequest req = (PSRequest) PSRequestInfo
		.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

		PSUserSession pssess = req.getUserSession();
		Object curComm = pssess
		.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
		if (curComm != null) {
			int comm = Integer.valueOf(curComm.toString());
			log.debug("Community not changed " + comm);
			if (comm == communityid)
				return;
		}
		system.switchCommunity(isi.getCommunityName(communityid));
	}
	
	private int findNavIdForFolderPath(String path) throws ItemRestException {
		PSRelationshipFilter filter = new PSRelationshipFilter();
		PSRequest req = (PSRequest) PSRequestInfo
		.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		PSServerFolderProcessor folderproc =  PSServerFolderProcessor.getInstance();
		int folderid=-1;
	    int navonId = -1;
		try {
			folderid = folderproc.getIdByPath(path);
		} catch (PSCmsException e) {
			log.error("cannot get folderid for path "+path,e);
			throw new ItemRestException("cannot get folderid for path "+path);
		}
		if (folderid>0) {
	      filter.setOwner(new PSLocator(folderid, 0)); 
	      filter.setName(PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT);
	      Set<Long> typeIds = new HashSet<Long>();
	      PSNavConfig navConfig = PSNavConfig.getInstance(); 
	      typeIds.add(new Long(navConfig.getNavonType().getUUID()));
	      typeIds.add(new Long(navConfig.getNavTreeType().getUUID())); 
	      filter.setDependentContentTypeIds(typeIds);
	      
	      List<PSRelationship> rels;
		try {
			rels = system.loadRelationships(filter);
		} catch (PSErrorException e) {
			log.error("Cannot get folder relationships for path "+path,e);
			throw new ItemRestException("Cannot get folder relationships for path "+path);
		}
	 
	      if(rels.size() > 1)
	      {
	         log.error("More than one Navon found in Folder. Possible invalid tree."); 
	      } else if(rels.size() == 1)
	      {
	    	 navonId=rels.get(0).getOwner().getId();
	      } 
	     
		}
		 return navonId;
	}

	@GET
	@Path("/importfeeds/")
	public Response updateItems(@QueryParam("debug")boolean debug, @QueryParam("content_type")String content_type) {

		log.debug("Invoked updateItems");
		
		initServices();

		log.debug("Returned From InitServices");
		
		log.debug("Feed Content Type is " + content_type);

		PagedResult ret = new PagedResult();
		List<ItemRef> refs = new ArrayList<ItemRef>();
		int lastId = 0;
		
		ret= jcrSearch("SELECT rx:sys_contentid FROM rx:" + content_type, 0, null);		
	
		refs = ret.getItemRefs();

		log.debug("Found " + refs.size() + content_type + " Items...");
		
			while(refs.size()>0){
				
				for(int i = 0;i<refs.size();i++){
					try{
						lastId = refs.get(i).getContentId();
						log.debug("Updating content_id " + lastId);
						updateItem(false,lastId,0);
						log.debug("content_id " + lastId + " updated.");
					}catch(Exception ex){
						log.debug(ex,ex);
					}
					}
		
				//See if there are more results.
				ret= jcrSearch("SELECT rx:contentid FROM rx:" + content_type, lastId, null);		
				refs = ret.getItemRefs();			
				
			}
		
		//@TODO: Add additional support.
		//If we got this far, give them the good news.
		ResponseBuilder builder = Response.status(Status.OK);
		builder.type("text/plain");
		builder.entity("Feed processing completed.");
	
		return builder.build();
		
	}
	
	/***
	 * Given the specified content id, will load the Feed Definition
	 * parameters and execute the import template specified by the feed.
	 */
	@GET
	@Path("/importfeed/")
	public Response updateItem(@QueryParam("debug")boolean debug, @QueryParam("sys_contentid")int sys_contentid, @QueryParam("sys_folderid")int sys_folderid) {
		initServices();

		log.debug("Feed sys_contentid is " + sys_contentid);
	
		// Make sure that the item is checked in
		try{
			log.debug("Auto checking in Feed Item");
			IPSGuid feedGuid = gmgr.makeGuid(new PSLocator(sys_contentid, -1));
			cws.checkinItems(Collections.singletonList(feedGuid),"auto-checkin");
		} catch (PSErrorsException e) {
			log.debug("Auto checkin of Feed Item failed.");
			log.debug(e,e);
		}
		
		
		Items items = new Items();
		IPSAssemblyItem asmItem = new PSAssemblyWorkItem();
		Items output = null;
		String assemblyResult = "";
		try {

			Item feedDef = getFromRxItem(sys_contentid, -1);
		
			Field feedUrl = feedDef.getField("feedUrl");
			Field targetfolder = feedDef.getField("targetFolder");
			Field feedFormat  = feedDef.getField("feedFormat");
			Field targetCommunity = feedDef.getField("targetCommunity");
			Field targetWorkflow = feedDef.getField("targetWorkflow");
			Field targetState = feedDef.getField("targetWorkflowState");
			Field targetContentType = feedDef.getField("targetContentType");
			Field targetTransition = feedDef.getField("targetTransition");
			Field feedFolderLayout = feedDef.getField("feedFolderLayout");
			Field importAttachments = feedDef.getField("importAttachments");
			Field targetAttachmentFolder = 	feedDef.getField("targetAttachmentFolder");
			Field attachmentMIMETypes = feedDef.getField("attachmentMIMETypes");
			Field cachedETag = feedDef.getField("cached_etag");
			Field cachedLastModified = feedDef.getField("cached_lastmodified");
			Field attach_useitemname = feedDef.getField("attach_useitemname");
			Field targetImageContentType = feedDef.getField("targetImageContentType");
			Field importFeedItems = feedDef.getField("importFeedItems");
			Field targetEditTransition = feedDef.getField("targetEditTransition");
			
			if (feedFormat == null) {	//If allowed, it's archived. So don't process, but don't add to error list when caught.			
				log.debug("FeedFormat is not defined for content ID " + sys_contentid);
				throw new ArchivedException("FeedFormat is not defined for archived feed with content ID " + sys_contentid);
			}	
			IPSAssemblyTemplate template = aService.findTemplateByName(feedFormat.getStringValue());

			Map<String, Object> bindings = new HashMap<String, Object>();

			asmItem.setParameterValue("sys_itemfilter", "preview");
			asmItem.setParameterValue("sys_template",
					String.valueOf(template.getGUID().getUUID()));
			
			asmItem.setParameterValue("sys_contentid", "1");
			PSLegacyGuid guid = new PSLegacyGuid(new PSLocator(1, -1));
			asmItem.setId(guid);
			asmItem.setFilter(filter.findFilterByName("preview"));
				
			bindings.put("sourceFeedId", sys_contentid);
			log.debug("$sourceFeedId=" + sys_contentid);
			
			bindings.put("$feedFolderId", sys_folderid);
			log.debug("$feedFolderId=" + sys_folderid);
			
			if(feedUrl!=null){
				bindings.put("$feedUrl", feedUrl.getStringValue());
				log.debug("$feedUrl=" + feedUrl.getStringValue());
			}
			
			if(targetImageContentType!=null){
				bindings.put("$targetImageContentType", targetImageContentType.getStringValue());
				log.debug("$targetImageContentType=" + targetImageContentType.getStringValue());
			}
			
			if(importFeedItems!=null){
				bindings.put("$importFeedItems", importFeedItems.getStringValue());
				log.debug("$importFeedItems=" + importFeedItems.getStringValue());
			}
			
			if(targetfolder!=null){
				bindings.put("$targetFolder", targetfolder.getStringValue());
				log.debug("$targetFolder=" + targetfolder.getStringValue());
			}
			
			if(targetCommunity!=null){
				bindings.put("$targetCommunity", targetCommunity.getStringValue());
				log.debug("$targetCommunity=" + targetCommunity.getStringValue());
			}
			
			if(targetWorkflow!=null){
				bindings.put("$targetWorkflow", targetWorkflow.getStringValue());
				log.debug("$targetWorkflow=" + targetWorkflow.getStringValue());
			}
			
			if(targetState!=null){
				bindings.put("$targetWorkflowState", targetState.getStringValue());
				log.debug("$targetWorkflowState=" + targetState.getStringValue());
			}
			
			if(attach_useitemname!=null){
				bindings.put("$attach_useitemname", attach_useitemname.getStringValue());
				log.debug("$attach_useitemname=" + attach_useitemname.getStringValue());
			}
			if(targetContentType!=null){
				bindings.put("$targetContentType",targetContentType.getStringValue());
				log.debug("$targetContentType=" + targetContentType.getStringValue());
			}
			
			if(targetTransition!=null){
				bindings.put("$targetTransition", targetTransition.getStringValue());
				log.debug("$targetTransition=" + targetTransition.getStringValue());
			}
			
			if(targetEditTransition!=null){
				bindings.put("$targetEditTransition", targetEditTransition.getStringValue());
				log.debug("$targetEditTransition=" + targetEditTransition.getStringValue());
			}
			
			if(feedFolderLayout!=null){
				bindings.put("$feedFolderLayout", feedFolderLayout.getStringValue());
				log.debug("$feedFolderLayout=" + feedFolderLayout.getStringValue());
			}
		
			if(targetAttachmentFolder!=null){
				bindings.put("$targetAttachmentFolder", targetAttachmentFolder.getStringValue());
				log.debug("$targetAttachmentFolder=" + targetAttachmentFolder.getStringValue());
			}
		
			if(attachmentMIMETypes!=null){
				bindings.put("$attachmentMIMETypes", attachmentMIMETypes.getStringValue());
				log.debug("$attachmentMIMETypes=" + attachmentMIMETypes.getStringValue());
			}
			
			if(importAttachments!=null){
				bindings.put("$importAttachments", importAttachments.getStringValue());
				log.debug("$importAttachments=" + importAttachments.getStringValue());
			}
		
			if(cachedETag!=null){
				bindings.put("$feedCachedETag", cachedETag.getStringValue());
				log.debug("$feedCachedETag=" + cachedETag.getStringValue());				
			}
			
			if(cachedLastModified!=null){
				bindings.put("$feedCachedLastModified", cachedLastModified.getStringValue());
				log.debug("$feedCachedLastModified=" + cachedLastModified.getStringValue());
			}
			
			asmItem.setBindings(bindings);
			asmItem.setTemplate(template);

			List<IPSAssemblyResult> asmResult = aService.assemble(Collections.singletonList(asmItem));
			
			log.debug("Got assembly Result");
			
			InputStream is = asmResult.get(0).getResultStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			assemblyResult = sb.toString();
			log.debug("Assembly result is " + assemblyResult);
			output = ItemServiceHelper.getItemsFromXml(assemblyResult);

			if (output != null) {
				log.debug("Got Items");
				items = output;

				if (!debug) {
					try {
						items = updateItems(output);
					} catch (Exception e) {
						items.addError(ErrorCode.ASSEMBLY_ERROR,
								"Error importing item" + assemblyResult, e);
					}
				}

			}
	
		} catch (Exception e) {
			if (e instanceof ArchivedException )			
				log.debug(e.getMessage());
			else {
				items.addError(ErrorCode.ASSEMBLY_ERROR, "Assembly Error" + assemblyResult, e);
				log.debug("Assembly Error" + assemblyResult,e);
			}
		}
		
	// send an email when there are updates or errors
		
		if (items.hasItems()) {						
			String body = "Content ID of Updated Item: " + sys_contentid;
			String subject = "STATUS: " + FEED_STATUS_UPDATED;
			sendEmailNotification(subject, body);
		}	
		
		if (items.hasErrors()) {
			String str = null;
			StringBuffer sb = new StringBuffer("Errors found in Content ID: " + sys_contentid);
			sb.append("\n Error is: ");
			List <Error>mailErrors = items.getErrors();
			int i = 0;
			int size = mailErrors.size();
			for (Error err : mailErrors) {
				str = err.getErrorMessage();
				if (str == null || str.equals(""))
					str = "no error message";
				   sb.append(str);
				   i++;
				   if (i < size)
					   sb.append(", ");
			}
			String body = new String(sb);
			String subject = "STATUS: " + FEED_STATUS_ERROR;
			sendEmailNotification(subject, body);
		}
					
		//@TODO: Add additional support.
		//If we got this far, give them the good news.
		
		ResponseBuilder builder = Response.status(Status.OK);
		builder.type("text/plain");
		builder.entity("Feed processing completed.");
		return builder.build();	
	}

	@GET
	@Path("/find/v/{value}/k/{keyfield}/p/{contextRoot}/")
	public Item findByKeyField(String value, String keyfield, String contextRoot){
		initServices();
		
		Item ret = new Item();

		String query = "select rx:sys_contentid from nt:base";
		String where = "rx:"
			+ keyfield + "='" + value + "'";
		String path = contextRoot;

		PagedResult res = jcrSearch(query, 1,where, null);
		int size = res.getItemRefs().size();
		if (size > 1) {
			log.error("Duplicate items detected for keyfield " + keyfield
					+ " path=" + path);
		}
		//Carry on and just load the 	first one.
		if (size!=0){
			int foundId = res.getItemRefs().get(0).getContentId();
			ret = getFromRxItem(foundId, -1);
			log.debug("located item id=" + foundId);
		}

		return ret;
	}
	
	/**
	 * Method to send email notification configured via
	 *
	 * @param esubject subject
	 * @param ebody body
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ItemRestException
	 * @throws ItemRestNotModifiedException 
	 */
	public void sendEmailNotification(String esubject, String ebody) {
		
		Properties props = new Properties();
		try {
		String propFile = PSServer.getRxFile(PSServer.BASE_CONFIG_DIR + "/Workflow/" + EMAIL_NOTIFICATION_PROPS);		
		props.load(new FileInputStream(propFile));	
	     
		String from_line 	= props.getProperty("from_line");
		if (from_line.equals(""))
			from_line = null;
		String to_line		= props.getProperty("to_line");	
		if (to_line.equals(""))
			to_line = null;
		String cc_line 		= props.getProperty("cc_line");	
		if (cc_line.equals(""))
			cc_line = null;
		String bcc_line 	= props.getProperty("bcc_line");	
		if (bcc_line.equals(""))
			bcc_line = null;
		
		StringBuffer body_buffer = new StringBuffer("");
		String email_body = props.getProperty("body");
		if(!email_body.equals(""))
			body_buffer.append(email_body);
		body_buffer.append("\n");
		body_buffer.append(ebody);
		body_buffer.append("\n");
		String body_ps = props.getProperty("body_ps");
		body_buffer.append(body_ps);
		String body = new String(body_buffer);
		
		StringBuffer subject_buffer = new StringBuffer("");
		String email_subject = props.getProperty("subject");
		if(!email_subject.equals(""))
			subject_buffer.append(email_subject);
		subject_buffer.append(esubject);
		String subject = new String(subject_buffer);
		
		PSOEmailUtils.sendEmail(from_line, to_line, cc_line, bcc_line, subject, body);
		}
		//If properties file does not exist, don't send email
			catch(IOException e) {	
				
			}
	}
	
}
	


	

