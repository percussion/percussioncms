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
package com.percussion.services.filter.impl;

import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.filter.PSFilterException;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for all percussion written item filters
 * 
 * @author dougrand
 */
public abstract class PSBaseFilter implements IPSExtension, IPSItemFilterRule
{
   /**
    * Logger for the base filter
    */
   Log ms_log = LogFactory.getLog(PSBaseFilter.class);

   /**
    * Priority for this rule, set via the init parameters
    */
   private int m_priority = 0;

   public int getPriority()
   {
      return m_priority;
   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      String prio = def
            .getInitParameter("com.percussion.services.filter.priority");
      if (!StringUtils.isBlank(prio))
      {
         try
         {
            m_priority = Integer.parseInt(prio);
         }
         catch (Exception e)
         {
            ms_log.warn("Bad priority given for filter " + this.getClass()
                  + " priority=" + prio);
         }
      }
   }

   /**
    * Implement in subclass
    */
   public abstract List<IPSFilterItem> filter(List<IPSFilterItem> items,
         Map<String, String> params) throws PSFilterException;
}
