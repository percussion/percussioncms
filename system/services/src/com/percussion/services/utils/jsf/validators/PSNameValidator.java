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
