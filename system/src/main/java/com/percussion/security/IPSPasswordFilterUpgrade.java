/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.security;

/***
 * Provides an interface for Password Filters to use when upgrading
 * encryption algorithms used in Password hashing.
 *
 */
public interface IPSPasswordFilterUpgrade {

    /***
     * Will encrypt the password using the hashing / encryption
     * routine used in the previous version of the software.
     *
     * This is to allow Security Providers to re-encrypt passwords
     * on login after a security update.
     *
     * @param password
     * @return
     */
    String legacyEncrypt(String password);

    String getLegacyAlgorithm();
}
