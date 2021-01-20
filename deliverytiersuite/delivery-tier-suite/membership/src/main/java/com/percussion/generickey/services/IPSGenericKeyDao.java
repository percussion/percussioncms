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
package com.percussion.generickey.services;

import com.percussion.generickey.data.IPSGenericKey;

/**
 * DAO service for the reset key service.
 * 
 * @author LeonardoHildt
 */
public interface IPSGenericKeyDao
{
    /**
     * Create an instance of a generic key.  The generic key is not yet persisted.
     * This method is used to work as a factory and switch easily between mongo and rdbms.
     * 
     * @return The generic key object, never <code>null</code>.
     */
    public IPSGenericKey createKey();
    
    /**
     * Search for a reset key matching the supplied password reset key.
     * 
     * @param pwdResetKey The key to use, may not be <code>null</code> or empty.
     * 
     * @return The member, or <code>null</code> if not found. 
     */
    public IPSGenericKey findByResetKey(String resetKey);
    
    /**
     * Save the supplied reset key.  
     * 
     * @param resetKey The reset key to save, may not be <code>null</code>.
     * 
     * @throws PSMemberExistsException if a member with that user name already exists.
     * @throws Exception if there are any errors.
     */
    public void saveKey(IPSGenericKey resetKey) throws Exception;
    
    /**
     * Delete the supplied reset key.  
     * 
     * @param resetKey The reset key to delete, may not be <code>null</code>.
     * 
     * @throws PSMemberExistsException if a member with that user name already exists.
     * @throws Exception if there are any errors.
     */
    public void deleteKey(IPSGenericKey resetKey) throws Exception;
    
}
