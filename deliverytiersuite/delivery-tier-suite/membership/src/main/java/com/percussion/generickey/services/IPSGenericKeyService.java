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
package com.percussion.generickey.services;


/**
 * Generic key service, to generate unique keys with duration. 
 *
 */
public interface IPSGenericKeyService 
{
    public static final long DAY_IN_MILLISECONDS = 86400000;

	/**
	 * Generates a unique key with the supplied duration, validity of the key is
	 * checked against the creation time with the duration and current system
	 * time.
	 * 
	 * @param duration in milliseconds.
	 * @return The generated key, never blank.
	 */
	public String generateKey(long duration) throws Exception;

	/**
	 * Checks whether the supplied key is still valid or not. The key is valid
	 * if it exists and if the current system time is less than the key creation
	 * time plus duration.
	 * 
	 * @param key may be blank.
	 * @return <code>true</code> if the key is still valid otherwise <code>false</code>.
	 */
	public boolean isValidKey(String key) throws Exception;
	
	/**
	 * Deletes the supplied key if exists.
	 * @param key the key to delete
	 */
	public void deleteKey(String key) throws Exception;
	
}
