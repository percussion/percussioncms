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
