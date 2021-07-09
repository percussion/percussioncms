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
package com.percussion.pagemanagement.dao.impl;

import static org.apache.commons.lang.StringUtils.*;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService.PSResourceDefinitionInvalidIdException;

public class PSResourceDefinitionUniqueId {
    
    private String groupId;
    private String localId;
    
    
    public PSResourceDefinitionUniqueId(String uniqueId) throws PSResourceDefinitionInvalidIdException {
        super();
        init(uniqueId);
    }
    public PSResourceDefinitionUniqueId(String groupId, String localId) throws PSResourceDefinitionInvalidIdException {
        super();
        init(groupId, localId);
    }
    public void init(String groupId, String localId) throws PSResourceDefinitionInvalidIdException {
        setGroupId(groupId);
        setLocalId(localId);
        
    }
    public String getGroupId()
    {
        return groupId;
    }
    public void setGroupId(String groupId) throws PSResourceDefinitionInvalidIdException {
        validateId("groupId", groupId);
        this.groupId = groupId;
    }
    public String getLocalId()
    {
        return localId;
    }
    public void setLocalId(String localId) throws PSResourceDefinitionInvalidIdException {
        validateId("localId", localId);
        this.localId = localId;
    }

    
    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public String getUniqueId() {
        return groupId + IPSResourceDefinitionService.NAMESPACE_SEPARATOR + localId;
    }
    
    public void init(String uniqueId) throws PSResourceDefinitionInvalidIdException {
        if (isBlank(uniqueId)) {
            throw new PSResourceDefinitionInvalidIdException("PSResourceDefinitionUniqueId cannot be blank");
        }
        
        String[] pair = split(uniqueId, ".");
        if (pair.length == 2 && isNotBlank(pair[0]) && isNotBlank(pair[1])) {
            setGroupId(pair[0]);
            setLocalId(pair[1]);
        }
        else {
            throw new PSResourceDefinitionInvalidIdException("PSResourceDefinitionUniqueId is not valid. value: " + uniqueId);
        }
        
    }
    
    
    public static void validateId(String name, String id) throws PSResourceDefinitionInvalidIdException {
        if (isBlank(id)) {
            throw new PSResourceDefinitionInvalidIdException(name + " cannot be blank");
        }
        if( contains(id, ".") ) {
            throw new PSResourceDefinitionInvalidIdException(name 
                    + " cannot contain " 
                    + IPSResourceDefinitionService.NAMESPACE_SEPARATOR  );
        }
    }
}
