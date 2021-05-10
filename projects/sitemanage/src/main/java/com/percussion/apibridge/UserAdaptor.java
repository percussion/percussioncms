/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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

package com.percussion.apibridge;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.rest.Status;
import com.percussion.rest.communities.Community;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.errors.DirectoryUserImportErrorException;
import com.percussion.rest.errors.DirectoryUserImportInvalidNameException;
import com.percussion.rest.errors.UnexpectedException;
import com.percussion.rest.errors.UnknownUserException;
import com.percussion.rest.errors.UnsupportedUserTypeException;
import com.percussion.rest.users.IUserAdaptor;
import com.percussion.rest.users.User;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.user.data.PSExternalUser;
import com.percussion.user.data.PSImportedUser;
import com.percussion.user.data.PSImportedUser.ImportStatus;
import com.percussion.user.data.PSUser;
import com.percussion.user.data.PSUserList;
import com.percussion.user.data.PSUserProviderType;
import com.percussion.user.service.IPSUserService;
import com.percussion.user.service.IPSUserService.PSDirectoryServiceStatus;
import com.percussion.user.service.IPSUserService.PSDirectoryServiceStatus.ServiceStatus;
import com.percussion.user.service.IPSUserService.PSImportUsers;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean
@Lazy
public class UserAdaptor extends SiteManageAdaptorBase implements IUserAdaptor{

	final static Logger log = LogManager.getLogger(UserAdaptor.class);

    @Autowired
    private IPSSecurityWs securityWs;

    @Autowired
    private IPSSecurityDesignWs securityDesignWs;

    @Autowired
    private IPSSystemWs systemWs;

    @Autowired
    private IPSGuidManager guidManager;

    @Autowired
	public UserAdaptor(IPSUserService userService, IPSItemWorkflowService itemWorkflowService) {
		super(userService, itemWorkflowService);
	}

	@Override
	public User getUser(URI baseURI, String userName) throws BackendException {
		try {
			User ret = null;

			PSUser user = userService.find(userName);

			if (user == null) {
				throw new UnknownUserException();
			} else {
				ret = new User();
				ret.setUserName(user.getName());
				ret.setEmailAddress(user.getEmail());
				ret.setUserType(user.getProviderType().name());
				ret.setRoles(user.getRoles());

				String communityId = null;
				String communityName = null;
				PSRequest req = PSSecurityFilter.getCurrentRequest();
				PSUserSession userSession = null;
				List<String> userCommunities = null;
				String session = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
				String puser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);

				if (req != null) {
					userSession = req.getUserSession();
					communityName = userSession.getUserCurrentCommunity();
					communityId = userSession.getCommunityId(req, communityName);
					userCommunities = userSession.getUserCommunities(req);
				}

				if (!StringUtils.isEmpty(communityId)) {
					IPSGuid guid = guidManager.makeGuid(Long.parseLong(communityId), PSTypeEnum.COMMUNITY_DEF);
					ArrayList<IPSGuid> guids = new ArrayList<>();
					guids.add(guid);


					List<PSCommunity> comms = securityDesignWs.loadCommunities(guids, false, true, session, puser);

					ret.setSelectedCommunity(ApiUtils.convertPSCommunity(comms.get(0)));

				}

				//Load up the available communities for this user
				if (userCommunities != null) {
					ArrayList<Community> availableComms = new ArrayList<>();
					ArrayList<IPSGuid> availGuids = new ArrayList<>();
					for (String s : userCommunities) {
						availGuids.add(new PSGuid(PSTypeEnum.COMMUNITY_DEF, Integer.parseInt(s)));
					}

					List<PSCommunity> psCommunities = securityDesignWs.loadCommunities(availGuids, false, true, session, puser);
					if (psCommunities != null && psCommunities.size() > 0) {
						ret.setUserCommunities(ApiUtils.convertPSCommunities(psCommunities));
					}
				}

			}
			return ret;
		} catch (PSDataServiceException | PSInternalRequestCallException | PSErrorResultsException e) {
			throw new BackendException(e);
		}
	}



	@Override
	public User updateOrCreateUser(URI baseURI, User user) throws BackendException {
		try {
			User ret = null;
			PSUser newUser = null;
			boolean isNewUser = false;

			try {
				PSUserList findUsers = userService.getUserNames(user.getUserName());
				if (findUsers.getUsers().contains(user.getUserName())) {
					newUser = userService.find(user.getUserName());
				} else {
					newUser = new PSUser();
					isNewUser = true;
				}
			} catch (Throwable t) {
				newUser = new PSUser();
				isNewUser = true;
			}

			newUser.setName(user.getUserName());
			newUser.setRoles(user.getRoles());

			if (!user.getEmailAddress().isEmpty()) {
				newUser.setEmail(user.getEmailAddress());
			}
		
	/*	if(!user.getPassword().isEmpty()){
			newUser.setPassword(user.getPassword());
		}
		*/
			newUser.setRoles(user.getRoles());


			/**
			 * This block of code is pretty goofy. Way to many user related objects. Stuck with it for now
			 */
			if (!isNewUser) {
				newUser = userService.update(newUser);
			} else {
				if (user.getUserType().equalsIgnoreCase(PSUserProviderType.INTERNAL.name())) {
					newUser = userService.create(newUser);
				} else if (user.getUserType().equalsIgnoreCase(PSUserProviderType.DIRECTORY.name())) {
					PSImportUsers newUsers = new PSImportUsers();
					List<PSExternalUser> dirUsers = new ArrayList<>();
					dirUsers.add(new PSExternalUser(user.getUserName()));
					newUsers.setExternalUsers(dirUsers);
					List<PSImportedUser> importUsers = userService.importDirectoryUsers(newUsers);

					if (importUsers != null) {
						PSImportedUser impU = importUsers.get(0);

						//Handle new imports and treat duplicates as if they should be updates - once the user is imported update them to get the other data added.
						if (impU.getStatus().compareTo(ImportStatus.SUCCESS) == 0 || impU.getStatus().compareTo(ImportStatus.DUPLICATE) == 0) {
							newUser.setEmail(user.getEmailAddress());
							newUser.setName(user.getUserName());
							newUser.setProviderType(PSUserProviderType.DIRECTORY);
							newUser.setRoles(user.getRoles());
							newUser = userService.update(newUser);
						} else {
							//Import failed.  Need to throw an error.
							if (impU.getStatus().compareTo(ImportStatus.ERROR) == 0) {
								throw new DirectoryUserImportErrorException();
							} else if (impU.getStatus().compareTo(ImportStatus.INVALID) == 0) {
								throw new DirectoryUserImportInvalidNameException();
							} else {
								throw new UnexpectedException();
							}
						}
					} else {
						//Import failed with no error or results - meaning something ate an exception it shouldn't have.
						throw new UnexpectedException();
					}
				} else {
					//Just in case we add a new user type / Security provider and this code hasn't been updated.
					throw new UnsupportedUserTypeException();
				}
			}

			ret = copyUser(newUser, new User());
			return ret;
		} catch (PSDataServiceException e) {
			throw new BackendException(e);
		}
	}

	private User copyUser(PSUser pu, User u){
		
		u.setUserName(pu.getName());
		u.setRoles(pu.getRoles());
		u.setEmailAddress(pu.getEmail());
		u.setUserType(pu.getProviderType().name());
		return u;
	}
	
	@Override
	public void deleteUser(URI baseURI, String userName) throws BackendException {
		try {
			userService.delete(userName);
		} catch (PSDataServiceException e) {
			throw new BackendException(e);
		}

	}

	@Override
	public List<String> findUsers(URI baseURI, String pattern) throws BackendException {
		try {
			List<String> ret = null;

			ret = userService.getUserNames(pattern).getUsers();

			return ret;
		} catch (PSDataServiceException e) {
			throw new BackendException(e);
		}
	}

	@Override
	public Status checkDirectoryStatus() {
		Status ret = new Status(404, "Not Found");
		
		try{
			PSDirectoryServiceStatus psStatus = userService.checkDirectoryService();
			
			if(psStatus.getStatus() == ServiceStatus.ENABLED){
				ret.setStatusCode(200);
				ret.setMessage(psStatus.getStatus().name());
			}else{
				ret.setStatusCode(404);
				ret.setMessage(psStatus.getStatus().name());
			}
		}catch(Exception e){
			ret.setStatusCode(500);
			ret.setMessage(e.getMessage());
		}
		
		return ret;
	}

	@Override
	public List<String> searchDirectory(String pattern) {
		List<String> ret = new ArrayList<>();
		
		List<PSExternalUser> users = userService.findUsersFromDirectoryService(pattern);
	
		if(users != null){
			for(PSExternalUser u : users){
				ret.add(u.getName());
			}
		}
		return ret;
	}


}
