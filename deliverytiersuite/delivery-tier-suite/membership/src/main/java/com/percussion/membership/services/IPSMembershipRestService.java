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
package com.percussion.membership.services;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.percussion.delivery.services.IPSRestService;
import com.percussion.membership.data.*;

/**
 * @author natechadwick
 *
 */
@Path("/membership")
public interface IPSMembershipRestService extends IPSRestService {

	/**
	 * Rest service method to create a membership account.
	 * 
	 * @param membership Object containing the account info to create, may not be <code>null</code>.
	 * 
	 * @return A result object, never <code>null</code>.
	 */
	@POST
	@Path("/user")
	@Produces("application/json")
	public abstract PSMembershipResult createUser(
			PSMembershipAccount membership, @Context HttpHeaders header);

	/**
	 * Rest service method to change the state of an user account.
	 * 
	 * @param account a {@link PSAccountSummary} object with the data
	 * to process.
	 */
	@PUT
	@Path("/admin/account")
	public abstract void changeStateAccount(PSAccountSummary account);

	/**
	 * Rest service method to delete an user account.
	 * 
	 * @param account a {@link PSAccountSummary} object with the data
	 * to process.
	 */
	@DELETE
	@Path("/admin/account/{email:.*}")
	public abstract void deleteAccount(@PathParam("email") String email);

	@POST
	@Path("/session")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSGetUserResult getUser(PSUserSession psUserSession);

	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSLoginResult login(PSLoginRequest loginRequest);

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSMembershipResult logout(PSUserSession psUserSession);

	@POST
	@Path("/pwd/requestReset")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSMembershipResult requestPwdReset(
			PSResetRequest resetRequest, @Context HttpHeaders header);

	/**
	 * Rest service method to validate the reset key.
	 * 
	 * @param resetKey String containing the token key, may not be <code>null</code>.
	 * @param resetRequest An {@link PSMembershipAccount} object with the parameters to associate
	 * the new password to the user.
	 * @return An {@link PSGetUserResult} object containing the email, may be empty but 
	 * never <code>null</code>.
	 */
	@POST
	@Path("/pwd/validate/{resetKey:.*}")
	@Produces("application/json")
	public abstract PSGetUserResult validatePwdResetKey(
			@PathParam("resetKey") String resetKey);

	/**
	 * Rest service method to reset the user password.
	 * 
	 * @param resetKey String containing the token key, may not be <code>null</code>.
	 * @param resetRequest An {@link PSMembershipAccount} object with the parameters to associate
	 * the new password to the user.
	 * @return An {@link PSLoginResult} object containing the session, may be empty but 
	 * never <code>null</code>.
	 */
	@POST
	@Path("/pwd/reset/{resetKey:.*}")
	@Produces("application/json")
	public abstract PSLoginResult resetPwd(
			@PathParam("resetKey") String resetKey,
			PSMembershipAccount resetRequest);

	/**
	 * Rest service method to reset the user password.
	 * 
	 * @param resetKey String containing the token key, may not be <code>null</code>.
	 * @param resetRequest An {@link PSMembershipAccount} object with the parameters to associate
	 * the new password to the user.
	 * @return An {@link PSLoginResult} object containing the session, may be empty but 
	 * never <code>null</code>.
	 */
	@POST
	@Path("/registration/confirm/{rvkey:.*}")
	@Produces("application/json")
	public abstract PSLoginResult confirmAccount(
			@PathParam("rvkey") String confirmKey);

	@GET
	@Path("/admin/users")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract List<PSUserSummary> findUserGroups();

	@PUT
	@Path("/admin/user/group/{siteName}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract void updateUserGroups(PSUserGroup userSummary);

}