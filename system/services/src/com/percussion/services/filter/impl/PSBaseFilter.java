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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
   private static final Logger ms_log = LogManager.getLogger(PSBaseFilter.class);

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
