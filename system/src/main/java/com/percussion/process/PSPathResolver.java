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




