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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.util.List;
import java.util.Map;

/**
 * Catalogs the locales on the system and returns the number of them.
 */
public class PSGetLocaleCountAction extends PSAAActionBase
{
   // see interface for details
   @SuppressWarnings("unused") //exception
   public PSActionResponse execute(
         @SuppressWarnings("unused") Map<String, Object> params)
         throws PSAAClientActionException
   {
      IPSContentDesignWs cd = PSContentWsLocator.getContentDesignWebservice();
      List locales = cd.findLocales(null, null);
      return new PSActionResponse(String.valueOf(locales.size()),
               PSActionResponse.RESPONSE_TYPE_PLAIN);
   }
}
