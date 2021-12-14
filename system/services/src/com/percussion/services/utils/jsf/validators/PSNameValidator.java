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
package com.percussion.services.utils.jsf.validators;

import com.percussion.utils.string.PSStringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

/**
 * Validates a name. 
 * 
 * @author dougrand
 * 
 */
public class PSNameValidator extends PSBaseValidator
{   
   @SuppressWarnings("unused")
   public void validate(FacesContext ctx, UIComponent comp, Object value)
         throws ValidatorException
   {
      String str = getString(value, true);

      Character ch = PSStringUtils.validate(str, PSStringUtils.SPACE_CHARS);
      if (ch != null)
      {
         fail(FacesMessage.SEVERITY_ERROR, "space character is not allowed");
      }
      else
      {
         if (!PSStringUtils.validateNameStart(str))
         {
            fail(FacesMessage.SEVERITY_ERROR, "'" + str.charAt(0)
                  + "' character is not allowed at the beginning of a name.");
         }

         ch = PSStringUtils.validate(str, PSStringUtils.INVALID_NAME_CHARS);
         if (ch != null)
         {
            fail(FacesMessage.SEVERITY_ERROR, "'" + ch
                  + "' character is not allowed.");
         }
      }
   }
}
