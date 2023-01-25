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
import com.percussion.rest.communities.Community;
import com.percussion.rest.communities.CommunityList;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "User")
@Schema(name = "User", description = "Represents a User.")
public class User {
	
	@Schema(name="userName", required=true,description="The User Id of the user")
	private String userName;
	
	@Schema(name="firstName", required=false,description="The first name of the user - Read only for LDAP users")
	private String firstName;
	
	@Schema(name="lastName", required=false,description="The last name of the user - Read only for LDAP users")
	private String lastName;
	
	@Schema(name="email", required=false,description="The email address of the User - read only for LDAP users.")
	private String emailAddress;
	
	@Schema(name="userType", required=true,description="The UserType of the user. INTERNAL or DIRECTORY ")
	private String userType;
	
	@Schema(name="password", required=true,description="The user's password. May only be set.")
	private String password;

	@Schema(name="bookmarkedPages", required=false,description="List of Pages bookmarked by this user.")
	private List<LinkRef> bookmarkedPages;
	
	@Schema(name="recentPages", required=false,description="List of Pages Recently edited by this user.")
	private List<LinkRef> recentPages;

	@Schema(name="recentAssetTypes", required=false,description="List of Recently used Asset types by this user.")
	private List<LinkRef> recentAssetTypes;

	@Schema(name="recentAssetFolders", required=false,description="List of Recently used Asset folders by this user.")
	private List<LinkRef> recentAssetFolders;

	@Schema(name="recentSiteFolders", required=false,description="List of Recently used Site Folders this user.")
	private List<LinkRef> recentSiteFolders;

	@Schema(name="recentTemplates", required=false,description="List of Recently used templates by this user.")
	private List<LinkRef> recentTemplates;
	
	@Schema(name="personalPage", required=false,description="The qualifed folder path to this users Personal Page")
	private LinkRef personalPage;
	
	@Schema(name="personAssets", required=false,description="A list of PersonAssets that represent this user.")
	private List<LinkRef> personAssets;

	@Schema(name="roles", required=false, description="A list of the Role names that this user belongs to.")
	private List<String> roles;

	@Schema(name="selectedCommunity", description="The Community that the user currently has selected.")
    private Community selectedCommunity;

	@Schema(name="userCommunities", description="The list of communities that the user belongs to.")
    private CommunityList userCommunities;

    public CommunityList getUserCommunities() {
        return userCommunities;
    }

    public void setUserCommunities(CommunityList userCommunities) {
        this.userCommunities = userCommunities;
    }

    public Community getSelectedCommunity() {
        return selectedCommunity;
    }

    public void setSelectedCommunity(Community selectedCommunity) {
        this.selectedCommunity = selectedCommunity;
    }

    public String getUserName() {
		if(userName==null)
			userName="";
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		if(firstName == null)
			firstName="";
		
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		if(lastName == null)
			lastName="";
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailAddress() {
		if(emailAddress==null)
			emailAddress = "";
		
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getUserType() {
		if(userType == null)
			userType="";
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public List<LinkRef> getBookmarkedPages() {
		if(bookmarkedPages == null)
			bookmarkedPages = new ArrayList<>();
		
		return bookmarkedPages;
	}

	public void setBookmarkedPages(List<LinkRef> bookmarkedPages) {
		this.bookmarkedPages = bookmarkedPages;
	}

	public List<LinkRef> getRecentPages() {
		if(recentPages == null)
			recentPages = new ArrayList<>();
		
		return recentPages;
	}

	public void setRecentPages(List<LinkRef> recentPages) {
		this.recentPages = recentPages;
	}

	public List<LinkRef> getRecentAssetTypes() {
		if(recentAssetTypes == null)
			recentAssetTypes = new ArrayList<>();
		
		return recentAssetTypes;
	}

	public void setRecentAssetTypes(List<LinkRef> recentAssetTypes) {
		this.recentAssetTypes = recentAssetTypes;
	}

	public List<LinkRef> getRecentAssetFolders() {
		if(recentAssetFolders==null)
			recentAssetFolders = new ArrayList<>();
		
		return recentAssetFolders;
	}

	public void setRecentAssetFolders(List<LinkRef> recentAssetFolders) {
		this.recentAssetFolders = recentAssetFolders;
	}

	public List<LinkRef> getRecentSiteFolders() {
		if(recentSiteFolders == null)
			recentSiteFolders = new ArrayList<>();

		return recentSiteFolders;
	}

	public void setRecentSiteFolders(List<LinkRef> recentSiteFolders) {
		this.recentSiteFolders = recentSiteFolders;
	}

	public List<LinkRef> getRecentTemplates() {
		if(recentTemplates == null)
			recentTemplates = new ArrayList<>();
		
		return recentTemplates;
	}

	public void setRecentTemplates(List<LinkRef> recentTemplates) {
		this.recentTemplates = recentTemplates;
	}

	public LinkRef getPersonalPage() {
		if(personalPage == null)
			personalPage = new LinkRef();
		return personalPage;
	}

	public void setPersonalPage(LinkRef personalPage) {
		this.personalPage = personalPage;
	}

	public List<LinkRef> getPersonAssets() {
		if(personAssets == null){
			personAssets = new ArrayList<>();
		}
		return personAssets;
	}

	public void setPersonAssets(List<LinkRef> personAssets) {
		this.personAssets = personAssets;
	}

	public List<String> getRoles() {
		if(roles == null)
			roles = new ArrayList<>();
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	public String getPassword() {
		if(password==null)
			password = "";
		
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User(){}
}
