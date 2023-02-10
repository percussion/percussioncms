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

import java.net.URI;
import java.util.List;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.rest.Status;
import com.percussion.rest.errors.BackendException;
import com.percussion.webservices.PSErrorResultsException;


public interface IUserAdaptor {
	
	 	public User getUser(URI baseURI, String userName) throws PSErrorResultsException, PSInternalRequestCallException, BackendException;

	    public User updateOrCreateUser(URI baseURI, User user) throws BackendException;

	    public void deleteUser(URI baseURI, String userName) throws BackendException;
	    
	    public List<String> findUsers(URI baseURI, String pattern) throws BackendException;
	    
	    public Status checkDirectoryStatus();
	    
	    public List<String> searchDirectory(String pattern);
	    
}
