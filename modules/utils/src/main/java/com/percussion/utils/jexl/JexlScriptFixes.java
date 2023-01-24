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
