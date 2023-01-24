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
