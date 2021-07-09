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

package com.percussion.taxonomy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.utils.jspel.PSRoleUtilities;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

public class TaxonomySecurityHelper {

	public static final String TAXONOMY_ADMIN_GROUP = "Taxonomy_Admin";

	public static Collection<PSCommunity> getAllCommunities(){
		Collection<PSCommunity> ret = new ArrayList<PSCommunity>();
		
		IPSSecurityWs securityWs = PSSecurityWsLocator.getSecurityWebservice();
		
		ret = securityWs.loadCommunities("*");
		
		return ret;
		
	}
	
	public static List<String> getAllRoles() {
		List<String> ret = new ArrayList<String>();
		ret = new ArrayList(PSRoleMgrLocator.getRoleManager().getDefinedRoles());
		ret.remove(TAXONOMY_ADMIN_GROUP);
		Collections.sort(ret);
		return ret;
	}

	public static boolean amITaxonomyAdmin() {
		return amIMemeberOfOneOfTheses(Arrays.asList(new String[] { TAXONOMY_ADMIN_GROUP }));
	}

	public static List<String> getMyRoles() {
		List<String> ret = new ArrayList<String>();
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
			if (my_roles.contains(the_role))
				ret = true;
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
		return Jsoup.clean(input, Whitelist.none());
	}
}
