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

package com.percussion.redirect.service;

import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.redirect.data.PSCreateRedirectRequest;
import com.percussion.redirect.data.PSRedirectStatus;
import com.percussion.redirect.data.PSRedirectValidationData;
import com.percussion.redirect.data.PSRedirectValidationResponse;

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
