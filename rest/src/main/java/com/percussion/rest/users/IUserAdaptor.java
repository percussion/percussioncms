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
