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
