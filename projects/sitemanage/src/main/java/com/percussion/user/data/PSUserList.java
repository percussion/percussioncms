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
package com.percussion.user.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of users.
 * <p>
 * Some tools have problems serializing a list of strings
 * hence this wrapping object.
 * 
 * @author adamgent
 * @author DavidBenua
 *
 */
@XmlRootElement(name = "UserList")
@JsonRootName("UserList")
public class PSUserList extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;
    private List<String> users;
    
    public PSUserList()
    {
        users = new ArrayList<>();
    }

    /**
     * @return the users
     */
    public List<String> getUsers()
    {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(List<String> users)
    {
        this.users = users;
    }
    
    
}
