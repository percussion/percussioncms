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

