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
package com.percussion.licensemanagement.error;

/**
 * @author Lucas Piccoli
 *
 */
public class PSLicenseServiceException extends RuntimeException
{

    public PSLicenseServiceException()
    {
        super();
    }

    public PSLicenseServiceException(String message)
    {
        super(message);
    }

    public PSLicenseServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public static final String ERROR_SAVING_LICENSES = "Error occurred while saving the module licenses. Please see log for more details.";
    public static final String ERROR_FINDING_LICENSE = "Error occurred while fetching the license for the supplied module '{0}'. Please see log for more details.";
    public static final String LICENSE_NOT_FOUND = "License for the supplied module '{0}' not found.";
}
