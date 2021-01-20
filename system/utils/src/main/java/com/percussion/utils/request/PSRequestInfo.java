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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.utils.request;


import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * This class stores a variety of per request information. This is initialized
 * in a servlet at the start of a request, and cleared at the end of the
 * request. The per-request information is stored locally to the thread and is
 * therefore not accessible to other requests.
 * <P>
 * Information is stored under keys. The available keys are string values that
 * are defined on this class. The values are specified as part of the per-key
 * documentation. Note that not every key is necessarily available in every
 * environment this class is used in.
 * 
 * @author dougrand
 */
public class PSRequestInfo extends PSRequestInfoBase
{
   /**
    * Initialize request information from a servlet. This method calls
    * {@link #initRequestInfo(Map)}with the derived information.
    *
    * @param req the servlet request, never <code>null</code>
    */
   public static void initRequestInfo(HttpServletRequest req)
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req may not be null");
      }
      Map<String,Object> initial = new HashMap<String,Object>();
      initial.put(PSRequestInfoBase.KEY_JSESSIONID, req.getSession().getId());

      initial.put(PSRequestInfoBase.KEY_USER, req.getRemoteUser());
      initRequestInfo(initial);
   }


}
