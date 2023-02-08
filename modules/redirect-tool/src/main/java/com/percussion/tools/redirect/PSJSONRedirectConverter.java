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

public class PSJSONRedirectConverter extends PSBaseRedirectConverter{

    private static final String JSON_RULES_START="{";
    private static final String JSON_RULE="\"{0}\": { \"to\": \"{1}\", \"statusCode\": {2} }";
    private static final String JSON_RULES_END = "}";

    @Override
    public String convertVanityRedirect(PSPercussionRedirectEntry e) {

        StringBuilder sb = new StringBuilder(JSON_RULE.replace("{0}",
                  e.getCondition()).replace("{1}",e.getRedirectTo()).replace("{2}", "301"));
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public String convertRegexRedirect(PSPercussionRedirectEntry e) {
     return null;//return convertVanityRedirect(e);
    }

    @Override
    public String getFilename() {
        return "redirects.json";
    }
}
