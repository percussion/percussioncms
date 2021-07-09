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
package com.percussion.role.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractNamedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.configuration.annotation.IsInvariant;
import net.sf.oval.constraint.Length;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.ValidateWithMethod;

/**
 * A role in the system and the users which are members of it.
 */
@XmlRootElement(name = "Role")
@JsonRootName("Role")
public class PSRole extends PSAbstractNamedObject
{
    private static final long serialVersionUID = 1L;

    /**
     * Message used when role name contains invalid characters.
     */
    private static final String INVALID_CHAR_ERROR_MSG = 
        "invalid_character";
    
    /**
     * Message used when role description is too long.
     */
    private static final String DESCR_LENGTH_ERROR_MSG = 
        "The maximum length of a role description is 255 characters.";
    
    /**
     * Message used when role name is too long.
     */
    private static final String NAME_LENGTH_ERROR_MSG = 
        "The maximum length of a role name is 50 characters.";



    private String oldName;

    private String description;
    
    private String homepage;

    @NotNull
    private List<String> users = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    @IsInvariant
    @NotNull
    @NotBlank
    @Length(max= 50, message = NAME_LENGTH_ERROR_MSG)
    @ValidateWithMethod(methodName = "isValidName", parameterType = String.class, message = INVALID_CHAR_ERROR_MSG)
    public String getName()
    {
        return super.getName();
    }

    @IsInvariant
    @Length(max = 255, message = DESCR_LENGTH_ERROR_MSG)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
	
    public String getHomepage() 
    {
        return homepage;
    }

    public void setHomepage(String homepage) 
    {
        this.homepage = homepage;
    }
    
    public List<String> getUsers()
    {
        return users;
    }
    
    public void setUsers(List<String> users)
    {
        this.users = users;
    }

    @Override
    protected boolean isValidName(String name)
    {
        Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!\\s-]");
        return !regex.matcher(name).find()  && super.isValidName(name);
    }
    
    /**
     * Validates a role description.
     * 
     * @param description
     * 
     * @return <code>true</code> if the description is <code>null</code> or no longer than 255 characters,
     * <code>false</code> otherwise.
     */
    protected boolean isValidDescription(String description)
    {
        return (description == null) || (description.length() <= 255);
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    @Override
    public PSRole clone()
    {
        PSRole role = (PSRole) super.clone();
        role.setDescription(this.getDescription());
        if (this.getUsers() != null) {
            role.setUsers(new ArrayList<>(this.getUsers()));
        }
        return role;
    }


}
