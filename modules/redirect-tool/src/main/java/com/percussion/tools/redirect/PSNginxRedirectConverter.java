/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.tools.redirect;

public class PSNginxRedirectConverter extends PSBaseRedirectConverter{

    public static final String NGINX_RULE="rewrite {0} {1}";
    public static final String NGINX_FLAGS = "last";
    public static final String NGINX_RULE_END=";";



    @Override
    public String convertVanityRedirect(PSPercussionRedirectEntry e) {

        StringBuilder sb = new StringBuilder(NGINX_RULE.replace("{0}",
                "^"+ e.getCondition()).replace("{1}",e.getRedirectTo()));
        sb.append(" ").append(NGINX_FLAGS);
        sb.append(NGINX_RULE_END);
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public String convertRegexRedirect(PSPercussionRedirectEntry e) {
        StringBuilder sb = new StringBuilder(NGINX_RULE.replace("{0}",
                e.getCondition()).replace("{1}",e.getRedirectTo()));
        sb.append(" ").append(NGINX_FLAGS);
        sb.append(NGINX_RULE_END);
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public String getFilename() {
        return "nginx_rules.conf";
    }
}
