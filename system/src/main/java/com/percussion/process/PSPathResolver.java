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
package com.percussion.process;

import java.io.File;
import java.util.Map;

/**
 * PSPathResolver class resolves file path. If a path relative to the Rhythmyx
 * root directory is specified as value, then it resolves it to an absolute
 * path with appropriate path separator.
 * When specifying the value for this resolver, always use Unix file separator.
 * The returned path contains separators appropriate for the OS.
 *
 * Example:
 * If the path is specified as:
 * &lt;param resolver="com.percussion.process.PSPathResolver"
 *           value="./sys_search/rware70/rx/config/rware.cfg"/>
 *
 * then <code>getValue</code> method returns the following value:
 *
 * On Windows (assuming "C:\Rhythmyx" is the Rhythmyx root directory) :
 * "C:\Rhythmyx\sys_search\rware70\rx\config\rware.cfg"
 *
 * On Unix (assuming "/home/Rhythmyx" is the Rhythmyx root directory) :
 * "/home/Rhythmyx/sys_search/rware70/rx/config/rware.cfg"
 */
public class PSPathResolver extends PSBasicResolver
{
   // see base class
   public String resolve(String var, Map ctx)
      throws PSResolveException
   {
      //use super to check contract
      boolean isDoubleQuoted = false;

      String expanded = super.resolve(var, ctx);

     if ( expanded.startsWith("\"") && expanded.endsWith("\""))
     {
        isDoubleQuoted = true;
        expanded = expanded.substring(1,expanded.length()-1);
     }
      File file = new File(expanded);

      expanded =  file.getAbsolutePath();

      if ( isDoubleQuoted )
         expanded = "\"" + expanded + "\"";
      return expanded;
   }
}




