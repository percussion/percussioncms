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
package com.percussion.generickey.data;

import java.util.Date;

/**
 * Data object representing a reset key managed by the generic key service.
 * 
 * @author Leonardo Hildt
 *
 */
public interface IPSGenericKey
{
    /**
     * Get the reset Id of this reset key.
     * 
     * @return The reset key id, never <code>null</code> or empty, "0" if this 
     * reset key has not been persisted.
     */
    public String getResetKeyId();
    
    /**
     * Set the reset Id of this reset key.
     * 
     * @param resetKeyId The id, may not be <code>null</code> or empty.
     */
    public void setResetKeyId(String resetKeyId);
    
    /**
     * Set the date-time the password reset was last requested.
     *  
     * @param pwdResetDate The date, may be <code>null</code> to clear the date
     */
    public abstract void setExpirationDate(Date expirationDate);

    /**
     * Get the the date-time the password reset was last requested.
     * 
     * @return The date, may be <code>null</code>.
     */
    public abstract Date getExpirationDate();
    
    /**
     * Get the key used to identify a password reset request for this membership account.
     * 
     * @return The key, never empty, may be <code>null</code>.
     */
    public String getGenericKey();
    
    /**
     * Set the key used to identify a password reset request for this membership account.
     * 
     * @param pwdResetKey The key, never empty, may be <code>null</code> to clear the key.
     */
    public void setGenericKey(String resetKey);

}

