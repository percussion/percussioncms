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

import java.util.Map;

/**
 * PSLiteralResolver is the default resolver to be used when no resolver is
 * specified using the "resolver" attribute. The resolver just returns the
 * value passed to it.
 */
public class PSLiteralResolver extends PSBasicResolver
{
   /**
    * Just returns the supplied template except if it was <code>null</code>,
    * in which case "" is returned.
    */   
   protected String resolve(String template, Map ctx)
      throws PSResolveException
   {
      if ((template == null))
      {
         template = "";
      }
      //suppress eclipse warning
      if (null == ctx);
      return template;
   }
}

