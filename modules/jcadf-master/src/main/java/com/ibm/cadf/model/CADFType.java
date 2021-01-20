/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.model;

import java.io.Serializable;

import com.ibm.cadf.exception.CADFException;

public abstract class CADFType implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String CADF_SCHEMA_1_0_0 = "cadf:";

    public static final String CADF_VERSION_1_0_0 = "http://schemas.dmtf.org/cloud/audit/1.0/";

    // Valid cadf:Event record "types"
    public enum EVENTTYPE
    {

        EVENTTYPE_ACTIVITY("activity"),
        EVENTTYPE_REVOKE("revoke"),
        EVENTTYPE_MONITOR("monitor"),
        EVENTTYPE_CONTROL("control");

        public String value;

        private EVENTTYPE(String value)
        {
            this.value = value;
        }
    }

    public static boolean isValidEventType(String value)
    {
        for (EVENTTYPE event : EVENTTYPE.values())
        {
            if (event.name().equals(value))
            {
                return true;
            }
        }
        return false;
    }

    // Valid cadf:Event record "Reporter" roles

    public enum REPORTER_ROLES
    {

        REPORTER_ROLE_OBSERVER("observer"),
        REPORTER_ROLE_MODIFIER("modifier"),
        REPORTER_ROLE_RELAY("relay");

        String value;

        private REPORTER_ROLES(String value)
        {
            this.value = value;
        }
    }

    public static boolean isValidReporterRoles(String value)
    {
        for (REPORTER_ROLES event : REPORTER_ROLES.values())
        {
            if (event.value.equals(value))
            {
                return true;
            }
        }
        return false;
    }

    // TODO : validate method should be modified to return error message with details of missing mandatory fields.
    // Validation to ensure all required attributes are set.
    public abstract boolean isValid() throws CADFException;

}
