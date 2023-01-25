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
