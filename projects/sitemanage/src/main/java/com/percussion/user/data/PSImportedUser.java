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

package com.percussion.user.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.user.service.IPSUserService;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * A single user that
 * <strong>may or may not</strong> have been
 * imported successfully.
 *
 * See {@link PSImportedUser#getStatus()}.
 *
 * @author adamgent
 *
 */
@XmlRootElement(name = "ImportedUser")
@JsonRootName("ImportedUser")
public class PSImportedUser extends PSAbstractUser {

    private static final long serialVersionUID = 1L;
    private ImportStatus status = ImportStatus.SUCCESS;

    /**
     * Was the user imported?
     * @return never <code>null</code>.
     */
    public ImportStatus getStatus()
    {
        return status;
    }


    public void setStatus(ImportStatus status)
    {
        this.status = status;
    }


    /**
     *
     * Indicates whether or not the users was imported.
     * @author adamgent
     *
     */
    public static enum ImportStatus {
        /**
         * The user was successfully imported.
         */
        SUCCESS,
        /**
         * The user already exists a back-end user.
         */
        DUPLICATE,
        /**
         * The user was not imported because its invalid name.
         */
        INVALID,
        /**
         * Some unknown error
         */
        ERROR
    }
}
