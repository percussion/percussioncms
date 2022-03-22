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

package com.percussion.utils.jexl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JexlScriptFixes {

    /** The logger. */
    private static final Logger LOG = LogManager.getLogger(PSScript.class);

    public static final String REF_REGEX="[a-zA-Z_$][0-9a-zA-Z_$]*";
    public static final Pattern REF_EQUALS_REGEX = Pattern.compile("("+REF_REGEX+")=(\\$[0-9a-zA-Z_$]+)");
    public static final String REF_EQUALS_REGEX_REPL = "$1 = $2";
    public static final Pattern REF_NEQUALS_REGEX = Pattern.compile("!(\\$[a-zA-Z_$])");
    public static final String REF_NEQUALS_REGEX_REPL = "! $1";
    public static final Pattern FOR_REGEX = Pattern.compile("foreach\\s*\\(\\s*("+REF_REGEX+")\\s+in\\s+("+REF_REGEX+")\\s*\\)");

    public static final String FOR_REGEX_REPL = "for($1 : $2)";

    public static String fixScript(String scriptText, String ownerType, String ownerName)
    {

        scriptText = replace(scriptText,REF_EQUALS_REGEX,REF_EQUALS_REGEX_REPL,"Type: " + ownerType +"\n"
                + "Name: " + ownerName + "\n$ref=$ref2 syntax probably needs fixing.  =$ is special operator 'Ends With' now.  If attempting assignment add space eg. $ref = $ref");

        scriptText = replace(scriptText,REF_NEQUALS_REGEX,REF_NEQUALS_REGEX_REPL,"Type: " + ownerType +"\n"
                + "Name: " + ownerName + "\n!$ref syntax in jexl probably needs fixing.  !$ is a special operator 'Not Ends With' now, if testing negation should add space  e.g. if( ! $ref ) ");

        scriptText = replace(scriptText,FOR_REGEX,FOR_REGEX_REPL,"Type: " + ownerType +"\n"
                + "Name: " + ownerName + "\nloop syntax foreach(item in list) is now for(item : list)");


        return scriptText;
    }

    private static String replace(String scriptText, Pattern patt, String replace,  String warn)
    {
        Matcher matcher = patt.matcher(scriptText);

        if(matcher.find())
        {
           LOG.warn(warn);
           LOG.debug(scriptText);

            scriptText = matcher.replaceAll(replace);
        }
        return scriptText;
    }
}
