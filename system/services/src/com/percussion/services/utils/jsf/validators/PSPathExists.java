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

import com.percussion.cms.PSCmsException;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

/**
 * Checks that the path exists within the Rx system. Forwarde or backslashes may
 * be used as separators. Case of path parts is insensitive. Trailing separators
 * are ignored. The root path must start with {@link #VALID_ROOT}.
 *
 * @author paulhoward
 */
public class PSPathExists extends PSBaseValidator
{
   /**
    * See class description.
    * 
    * @param context Supplied by the framework, assumed never <code>null</code>.
    * @param comp Supplied by the framework, assumed never <code>null</code>.
    * @param data 
    * 
    * @throws ValidatorException If the supplied path is not found within the
    * Rx folder hierarchy.
    */
   public void validate(FacesContext context, UIComponent comp, Object data)
      throws ValidatorException
   {
      PSServerFolderProcessor fp = PSServerFolderProcessor.getInstance();
      String path = data.toString();
      if (StringUtils.isBlank(path))
         return;
      
      // make sure the path is started with VALID_ROOT
      if (!isValidRoot(path))
         fail(FacesMessage.SEVERITY_ERROR, "jsf@invalid_root_path", VALID_ROOT);
      
      int id = -1;
      try
      {
         id = fp.getIdByPath(path);
         if (id < 0)
            fail(FacesMessage.SEVERITY_ERROR, "jsf@non_existent_path"); 
      }
      catch (PSCmsException e)
      {
         fail(FacesMessage.SEVERITY_FATAL, e.getLocalizedMessage());
      }
   }
   
   /**
    * Determines if the supplied path has a valid root, {@link #VALID_ROOT}.
    * @param path the path in question, assumed not <code>null</code> or empty.
    * @return <code>true</code> if the path does have a valid root.
    */
   private boolean isValidRoot(String path)
   {
      if (path.length() < VALID_ROOT.length())
         return false;
      
      String r = path.substring(0, VALID_ROOT.length());
      return r.equalsIgnoreCase(VALID_ROOT);
   }
   
   /**
    * The root path for a valid Site Root Path.
    */
   public static final String VALID_ROOT = "//Sites";
}
