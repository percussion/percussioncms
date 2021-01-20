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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.workflow.data.PSTransitionBase;
import com.percussion.webservices.system.Transition;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Base class for converters that convert {@link PSTransitionBase} to
 * {@link Transition} objects.  {@link #copyProperties(Object, Object)} has
 * been overriden to handle copying of base class information.  Note that the 
 * from state and to state are explicitly handled by the workflow converter.
 */
public abstract class PSTransitionBaseConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}
    * 
    * @param beanUtils
    */
   public PSTransitionBaseConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      
      m_specialProperties.add("guid");
      m_specialProperties.add("name");
      m_specialProperties.add("stateId");
      m_specialProperties.add("toState");
      m_specialProperties.add("workflowId");
   }

   @Override
   protected void copyProperties(Object dest, Object origin) 
      throws IllegalAccessException, InvocationTargetException, 
      NoSuchMethodException
   {
      super.copyProperties(dest, origin);
      PSTransitionBase src = (PSTransitionBase) origin;
      Transition tgt = (Transition) dest;
      tgt.setId(src.getGUID().longValue());
   }
}

