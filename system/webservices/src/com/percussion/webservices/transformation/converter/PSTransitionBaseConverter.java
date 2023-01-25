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

