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
