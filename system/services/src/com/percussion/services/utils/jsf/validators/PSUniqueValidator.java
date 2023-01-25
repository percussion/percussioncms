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
package com.percussion.services.utils.jsf.validators;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Makes sure that the provided value is unique.
 *
 * @author Andriy Palamarchuk
 */
public class PSUniqueValidator extends PSBaseValidator
{
   // see base
   public void validate(FacesContext context, UIComponent component,
         Object value)
   {
      if (getValueProvider() == null)
      {
         throw new IllegalStateException(
               "The value provider must be specified");
      }
      Collection<? extends Object> vals = getValueProvider().getAllValues();
      if (vals.isEmpty())
         return;
      boolean allStrings = true;
      boolean failed = false;
      if (value instanceof String)
      {
         for (Object o : vals)
         {
            if (!(o instanceof String))
            {
               allStrings = false;
               break;
            }
            else if (((String) o).equalsIgnoreCase((String) value))
            {
               failed = true;
               break;
            }
         }
      }
      
      if (!allStrings)
      {
         if (vals.contains(value))
         {
            failed = true;
         }
      }
      if (failed)
         fail(FacesMessage.SEVERITY_ERROR, "jsf@non_unique_value"); 
   }

   /**
    * The current value provider. Must be initialized before the first use.
    * @return the validator.
    * Not <code>null</code> after initialization.
    */
   public IPSUniqueValidatorValueProvider getValueProvider()
   {
      return m_valueProvider;
   }

   /**
    * @param provider the new value. Not <code>null</code>.
    * @see #getValueProvider()
    */
   public void setValueProvider(IPSUniqueValidatorValueProvider provider)
   {
      notNull(provider);
      m_valueProvider = provider;
   }

   /**
    * @see #getValueProvider()
    */
   private IPSUniqueValidatorValueProvider m_valueProvider;
}
