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
