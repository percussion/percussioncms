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
package com.percussion.itemmanagement.data;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;


/**
 * Encapsulates user information for an item.  This includes the user who has the item checked out, the current logged
 * in user, and the assignment type of the current logged in user.
 */
@XmlRootElement(name="ItemUserInfo")
public class PSItemUserInfo extends PSAbstractDataObject
{
    /**
     * Default constructor. For serializers.
     */
    public PSItemUserInfo()
    {
    }

    /**
     * Constructs an instance of the class.
     * 
     * @param itemName never blank.
     * @param checkOutUser the name of the user to which the item is currently checked out, never <code>null</code>.
     * @param currentUser the name of the user which is currently logged in, never blank.
     * @param assignmentType the string representation of PSAssignmentTypeEnum, never blank.
     */
    public PSItemUserInfo(String itemName, String checkOutUser, String currentUser, String assignmentType)
    {
        notEmpty(itemName, "itemName");
        notNull(checkOutUser, "checkOutUser");
        notEmpty(currentUser, "currentUser");
        notNull(assignmentType, "assignmentType");
        notEmpty(assignmentType, "assignmentType");
        
        this.itemName = itemName;
        this.checkOutUser = checkOutUser;
        this.currentUser = currentUser;
        this.assignmentType = assignmentType;
    }
    
    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public String getCheckOutUser()
    {
        return checkOutUser;
    }

    public void setCheckOutUser(String checkOutUser)
    {
        this.checkOutUser = checkOutUser;
    }

    public String getCurrentUser()
    {
        return currentUser;
    }

    public void setCurrentUser(String currentUser)
    {
        this.currentUser = currentUser;
    }

    public String getAssignmentType()
    {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType)
    {
        this.assignmentType = assignmentType;
    }

    @NotNull
    @NotEmpty
    private String itemName;
    
    @NotNull
    private String checkOutUser;
    
    @NotNull
    @NotEmpty
    private String currentUser;
        
    @NotNull
    private String assignmentType;
}
