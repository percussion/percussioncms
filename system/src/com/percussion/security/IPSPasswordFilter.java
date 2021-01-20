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
package com.percussion.security;

import com.percussion.extension.IPSExtension;

/**
 * IPSPasswordFilter is an interface to password encryption. Often when
 * passwords are stored (in a file, database, or wherever), they are first
 * encrypted. When authentication occurs, the password that is entered
 * by the user is encrypted by the same process and the encrypted versions
 * are compared byte-for-byte.
 *
 * IMPORTANT: Implementing classes are required to provide a meaningful
 * no-arguments constructor that will produce a working filter.
 */
public interface IPSPasswordFilter extends IPSExtension
{
   /**
    * This method is called by the Rhythmyx security provider before
    * authenticating a user. The password submitted in the request is
    * run through this filter, then checked against the stored password
    * character-for-character.
    *
    * @param password The clear-text password to be encrypted. Never
    * <CODE>null</CODE>.
    *
    * @return A string containing the encrypted password. Never
    * <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public String encrypt(String password);
}
