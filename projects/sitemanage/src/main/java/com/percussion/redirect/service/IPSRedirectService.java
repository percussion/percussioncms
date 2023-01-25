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

package com.percussion.redirect.service;

import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.redirect.data.PSCreateRedirectRequest;
import com.percussion.redirect.data.PSRedirectStatus;
import com.percussion.redirect.data.PSRedirectValidationData;
import com.percussion.redirect.data.PSRedirectValidationResponse;

@Deprecated
public interface IPSRedirectService
{
	public static String REDIRECT_TYPE_DEFAULT = "CM1";
	public static String REDIRECT_CATEGORY_AUTOGEN = "AUTOGEN";
	
	/***
	 * Check the status of Redirect Management on this installation.
	 * @return
	 */
	public PSRedirectStatus status();
	
	/***
	 * Validate a Redirect Request
	 * @param data
	 * @return
	 */
    public PSRedirectValidationResponse validate(PSRedirectValidationData data);
   
    /***
     * Create a new redirect with the redirect service.
     * @param request
     * @return
     */
    public PSRedirectStatus createRedirect(PSCreateRedirectRequest request);
    
    /***
     * Get the current license.
     * @return  The current license or null if unlicensed. 
     */
    public PSModuleLicense getLicense();



}
