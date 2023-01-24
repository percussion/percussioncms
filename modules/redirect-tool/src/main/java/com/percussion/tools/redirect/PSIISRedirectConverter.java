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

public class PSIISRedirectConverter extends PSBaseRedirectConverter{

    protected static final String FILENAME="iis-rewrites.config";


    public static final String START_REWRITE = "<rewrite>";
    public static final String START_RULES = "<rules>";
    public static final String START_RULE = "<rule name=\"{0}\">";
    public static final String MATCH = "<match url=\"{0}\" />";
    public static final String ACTION = "<action type=\"{0}\" url=\"{1}\" />";
    public static final String END_RULE = "</rule>";
    public static final String END_RULES = "</rules>";
    public static final String END_REWRITE = "</rewrite>";
    private int counter = 0;

    /*
<rewrite>
  <rules>
    <rule name="Rewrite to article.aspx">
      <match url="^article/([0-9]+)/([_0-9a-z-]+)" />
      <action type="Rewrite" url="article.aspx?id={R:1}&amp;title={R:2}" />
    </rule>
  </rules>
</rewrite>
     */

    @Override
    public String convertVanityRedirect(PSPercussionRedirectEntry r) {
        if(!r.getCategory().equalsIgnoreCase("VANITY") &&
                !r.getCategory().equalsIgnoreCase("AUTOGEN")){
            throw new IllegalArgumentException("Redirect type must be Vanity");
        }
        counter++;
        String condition = r.getCondition();
        if(condition.startsWith("/")){
            condition = condition.replaceFirst("/", "^");
        }
        String name = r.getCondition().replaceAll("[^a-zA-Z0-9]", "") + counter;
        StringBuilder sb = new StringBuilder(START_RULE.replace("{0}",name)).append(System.lineSeparator());

        sb.append(MATCH.replace("{0}",condition)).append(System.lineSeparator());

        String action = "Rewrite";
        if(r.getRedirectTo().startsWith("http")){
            action = "Redirect";
        }
        sb.append(ACTION.replace("{0}",action).replace("{1}", r.getRedirectTo()));
        sb.append(System.lineSeparator());

        sb.append(END_RULE).append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public String convertRegexRedirect(PSPercussionRedirectEntry r) {

        if(!r.getCategory().equalsIgnoreCase("REGEX")){
            throw new IllegalArgumentException("Redirect type must be Regex");
        }
        counter++;
        String name = r.getCondition().replaceAll("[^a-zA-Z0-9]", "")+ counter;
        StringBuilder sb = new StringBuilder(START_RULE.replace("{0}",name)).append(System.lineSeparator());


        sb.append(MATCH.replace("{0}",r.getCondition())).append(System.lineSeparator());

        sb.append(ACTION.replace("{0}","Rewrite").replace("{1}", r.getRedirectTo()));
        sb.append(System.lineSeparator());

        sb.append(END_RULE);
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public String getFilename() {
        return FILENAME;
    }
}
