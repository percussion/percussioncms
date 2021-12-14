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
