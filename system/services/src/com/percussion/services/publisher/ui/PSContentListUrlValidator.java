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
package com.percussion.services.publisher.ui;

import com.percussion.server.PSServer;
import com.percussion.services.utils.jsf.validators.PSBaseValidator;
import com.percussion.util.IPSHtmlParameters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

/**
 * Validates that the url for the content list is valid
 * 
 * @author dougrand
 */
public class PSContentListUrlValidator extends PSBaseValidator
{

   /** (non-Javadoc)
    * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
   @SuppressWarnings("unused")
   public void validate(FacesContext ctx, UIComponent comp, Object value)
         throws ValidatorException
   {
      String url = getString(value, true).trim();
      if (!url.startsWith(PSServer.getRequestRoot()))
      {
         fail(FacesMessage.SEVERITY_ERROR, "jsf@content_list_url_request_root",
               PSServer.getRequestRoot());
      }
      if (!url.contains("/contentlist"))
      {
         if (!url.contains(".xml"))
         {
            fail(FacesMessage.SEVERITY_ERROR, "jsf@legacy_url_xml_ext");
         }
      }
      else
      {
         // Check for required parameters
         if (!url.contains(IPSHtmlParameters.SYS_DELIVERYTYPE))
         {
            fail(FacesMessage.SEVERITY_ERROR, "jsf@clist_url_required_param",
                  IPSHtmlParameters.SYS_DELIVERYTYPE);
         }
         if (!url.contains(IPSHtmlParameters.SYS_CONTENTLIST))
         {
            fail(FacesMessage.SEVERITY_ERROR, "jsf@clist_url_required_param",
                  IPSHtmlParameters.SYS_CONTENTLIST);
         }         
      }
   }

}
