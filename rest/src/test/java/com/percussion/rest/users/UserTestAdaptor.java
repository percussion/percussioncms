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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rest.users;

import com.percussion.rest.LinkRef;
import com.percussion.rest.Status;
import com.percussion.rest.errors.UnknownUserException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
@Component
@Lazy
public class UserTestAdaptor implements IUserAdaptor {

	private List<User> testUserData = null;
	
	@Override
	public User getUser(URI baseURI, String userName) {
		User ret = null;
		if(testUserData == null)
			setup();
		return ret;
	}

	@Override
	public User updateOrCreateUser(URI baseURI, User user) {
		User ret = null;
		if(testUserData == null)
			setup();
		
		User toUpdate = null;
		for(User u: testUserData){
			if(u.getUserName().equalsIgnoreCase(user.getUserName())){
				//Update
				toUpdate = u;
				break;
			}
		}
		
		if(toUpdate == null){
			//New user
		}else{
			//Update
			toUpdate.setBookmarkedPages(user.getBookmarkedPages());
			toUpdate.setEmailAddress(user.getEmailAddress());
			toUpdate.setFirstName(user.getFirstName());
			toUpdate.setLastName(user.getLastName());
			
		}
		return ret;
	}

	@Override
	public void deleteUser(URI baseURI, String userName) {
		if(testUserData == null)
			setup();

		User toDelete= null;
		for(User u: testUserData){
			if(u.getUserName().equalsIgnoreCase(userName)){
				toDelete = u;
				break;
			}
		}
		
		if(toDelete!= null)
			testUserData.remove(toDelete);
		else
			throw new UnknownUserException();
	}

	@Override
	public List<String> findUsers(URI baseURI, String pattern) {
		return null; //TODO:
	}


	private void setup(){
	
		List<String> roles = new ArrayList<String>();
		roles.add("Editor");
		roles.add("Contributor");
	
		User a = new User();
		a.setUserName("a.user");
		a.setEmailAddress("a.email");
		a.setFirstName("a.first");
		a.setLastName("a.last");
	
		a.setRoles(roles);
			
		LinkRef aref = new LinkRef();
		aref.setName("a.userpage");
		aref.setHref("#");
		a.setPersonalPage(aref);
		LinkRef aPersonAsset = new LinkRef();
		List<LinkRef> apAssets = new ArrayList<LinkRef>();
		apAssets.add(aPersonAsset);
		a.setPersonAssets(apAssets);
	
		
		User b = new User();
		b.setUserName("b.user");
		a.setEmailAddress("b.email");
		a.setFirstName("b.first");
		a.setLastName("b.last");
	
		a.setRoles(roles);
			
		LinkRef bref = new LinkRef();
		bref.setName("b.userpage");
		bref.setHref("#");
		b.setPersonalPage(aref);
		LinkRef bPersonAsset = new LinkRef();
		List<LinkRef> bpAssets = new ArrayList<LinkRef>();
		bpAssets.add(bPersonAsset);
		b.setPersonAssets(bpAssets);
		this.testUserData = new ArrayList<User>();
	
		this.testUserData.add(a);
		this.testUserData.add(b);
		
	}

	@Override
	public Status checkDirectoryStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> searchDirectory(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}
}
