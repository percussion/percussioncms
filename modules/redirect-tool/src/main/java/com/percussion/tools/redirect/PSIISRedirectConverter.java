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

public class PSIISRedirectConverter extends PSBaseRedirectConverter{

    protected static final String FILENAME="iis-rewrites.config";


    public static final String START_REWRITE = "<rewrite>";
    public static final String START_RULES = "<rules>";
    public static final String START_RULE = "<rule>";
    public static final String MATCH = "<match url=\"{0}\" />";
    public static final String ACTION = "<action type=\"{0}\" url=\"{1}\" />";
    public static final String END_RULE = "</rule>";
    public static final String END_RULES = "</rules>";
    public static final String END_REWRITE = "</rewrite>";


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
    public String convertVanityRedirect(PSPercussionRedirectEntry e) {
        return super.convertVanityRedirect(e);
    }

    @Override
    public String convertRegexRedirect(PSPercussionRedirectEntry e) {
        return super.convertRegexRedirect(e);
    }

    @Override
    public String getFilename() {
        return FILENAME;
    }
}
