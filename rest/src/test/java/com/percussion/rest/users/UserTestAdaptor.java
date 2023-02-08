/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
