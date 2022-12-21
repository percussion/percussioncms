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

public class PSApacheRedirectConverter extends PSBaseRedirectConverter{


    @Override
    public String convertVanityRedirect(PSPercussionRedirectEntry r) {
        if(!r.getCategory().equalsIgnoreCase("VANITY") &&
                !r.getCategory().equalsIgnoreCase("AUTOGEN")){
            throw new IllegalArgumentException("Redirect type must be Vanity");
        }

        StringBuilder sb = new StringBuilder(REWRITE_RULE).append(" ");

        sb.append("\"");
        sb.append("^" + r.getCondition());
        sb.append("\" ");

        sb.append("\"");
        if(getAbsolutePrefix()!=null){
            if(!r.getRedirectTo().startsWith("http")) {
                sb.append(getAbsolutePrefix());
            }
        }
        sb.append(r.getRedirectTo());
        sb.append("\" ");

        //Case insensitive
        sb.append("[NC");

        if(r.getPermanent().equalsIgnoreCase("true"))
            sb.append(",R=permanent");

        sb.append("]");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public String convertRegexRedirect(PSPercussionRedirectEntry r) {
        if(!r.getCategory().equalsIgnoreCase("REGEX")){
            throw new IllegalArgumentException("Redirect type must be Regex");
        }

        StringBuilder sb = new StringBuilder(REWRITE_RULE).append(" ");

        sb.append("\"");
        sb.append(r.getCondition());
        sb.append("\" ");

        sb.append("\"");
        sb.append(r.getRedirectTo());
        sb.append("\" ");

        //Case insensitive
        sb.append("[NC");

        if(r.getPermanent().equalsIgnoreCase("true"))
            sb.append(",R=permanent");

        sb.append("]");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public String getFilename() {
        return "apache-redirects.conf";
    }


    private static final String REWRITE_RULE="RewriteRule";

}
