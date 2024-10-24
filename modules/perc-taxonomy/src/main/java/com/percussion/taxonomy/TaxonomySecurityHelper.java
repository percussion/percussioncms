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

package com.percussion.taxonomy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.utils.jspel.PSRoleUtilities;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

public class TaxonomySecurityHelper {

	public static final String TAXONOMY_ADMIN_GROUP = "Taxonomy_Admin";

	public static Collection<PSCommunity> getAllCommunities(){
		Collection<PSCommunity> ret;
		
		IPSSecurityWs securityWs = PSSecurityWsLocator.getSecurityWebservice();
		
		ret = securityWs.loadCommunities("*");
		
		return ret;
		
	}
	
	public static List<String> getAllRoles() {
		List<String> ret;
		ret = new ArrayList(PSRoleMgrLocator.getRoleManager().getDefinedRoles());
		ret.remove(TAXONOMY_ADMIN_GROUP);
		Collections.sort(ret);
		return ret;
	}

	public static boolean amITaxonomyAdmin() {
		return amIMemeberOfOneOfTheses(Arrays.asList(new String[] { TAXONOMY_ADMIN_GROUP }));
	}

	public static List<String> getMyRoles() {
		List<String> ret = new ArrayList<>();
		for (String the_role : StringUtils.split(PSRoleUtilities.getUserRoles(), ",")) {
			ret.add(StringUtils.strip(the_role));
		}
		Collections.sort(ret);
		return ret;
	}

	public static boolean amIMemeberOfOneOfTheses(Collection<String> search_in_roles) {
		Collection<String> my_roles = getMyRoles();
		boolean ret = false;
		for (String the_role : search_in_roles) {
			if (my_roles.contains(the_role)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public static void raise_error_if_cannot_admin() throws Exception {
		if (!amITaxonomyAdmin()) {
			// TODO throw new class
			throw new Exception("Taxonomy Permission Exception: must be admin");
		}
	}

	/***
	 * Minimal method for sanitizing input
	 * @param input
	 * @return
	 */
	public static String sanitizeInputForXSS(String input){
		return Jsoup.clean(input, Safelist.none());
	}
}
