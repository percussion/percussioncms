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
package com.percussion.relationship;

import com.percussion.error.PSSqlException;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSRelationshipUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This exit decides upon table lookups whether or not the requested
 * translation relationship can be created.
 */
public class PSTranslationConstraint extends PSDefaultExtension
   implements IPSRequestPreProcessor
{

   private static final Logger log = LogManager.getLogger(PSTranslationConstraint.class);

   /**
    * Pre processes the supplied request and throws a
    * <code>PSRequestValidationException</code> if a translation for the
    * current item and requested language already exists. If the request already
    * contains non empty value for {@link IPSHtmlParameters#SYS_DEPENDENTID},
    * no validation is done assuming the translation is not being created in
    * this request and already exists.
    * 
    * @param params the exit parameters, not used for this exit and therefore
    *           can be <code>null</code> or empty.
    * @param request the request to operate on, not <code>null</code>. The
    *           request must provide the HTML parameters {#link
    *           com.percussion.util.IPSHtmlParameters.SYS_CONTENTID} and {#link
    *           com.percussion.util.IPSHtmlParameters.SYS_LANG} and {#link
    *           com.percussion.util.IPSHtmlParameters.SYS_RELATIONSHIPTYPE}.
    * @throws PSAuthorizationException if the requestor is not authorized to
    *            perform the requested operation.
    * @throws PSRequestValidationException if the current item already has been
    *            translated to the requested language.
    * @throws PSParameterMismatchException is never thrown from this class.
    * @throws PSExtensionProcessingException for any exit processing errors.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         String depId = request.getParameter(IPSHtmlParameters.SYS_DEPENDENTID,
               "").trim();
         log.info("depId = {}", depId);
         if(depId.length() > 0) //Dependent already known, no need to validate
            return;

         String[] htmlParameters =
         {
            IPSHtmlParameters.SYS_CONTENTID,
            IPSHtmlParameters.SYS_LANG,
            IPSHtmlParameters.SYS_RELATIONSHIPTYPE
         };

         Map parameters = new HashMap();
         for (int i=0; i<htmlParameters.length; i++)
         {
            String parameter = request.getParameter(htmlParameters[i], "")
                  .trim();
            if (parameter.length() == 0)
            {
               Object[] args = {htmlParameters[i], "null or empty"};
               throw new PSExtensionProcessingException(
                  IPSExtensionErrors.EXT_MISSING_HTML_PARAMETER_ERROR,
                     args);
            }

            parameters.put(htmlParameters[i], parameter);
         }

         if (PSRelationshipUtils.doesTranslationExist(request))
         {
            Object[] args =
            {
               parameters.get(IPSHtmlParameters.SYS_LANG),
               parameters.get(IPSHtmlParameters.SYS_CONTENTID)
            };
            throw new PSRequestValidationException(
               IPSExtensionErrors.TRANSLATION_ALREADY_EXISTS, args);
         }
      }
      catch (PSException e)
      {
         if (e instanceof PSRequestValidationException)
            throw (PSRequestValidationException) e;

         if (e instanceof PSExtensionProcessingException)
            throw (PSExtensionProcessingException) e;

         throw new PSExtensionProcessingException(
            e.getErrorCode(), e.getErrorArguments());
      }
      catch (SQLException e)
      {
         throw new PSExtensionProcessingException(0,
            PSSqlException.getFormattedExceptionText(e));
      }
      catch (IOException e)
      {
         throw new PSExtensionProcessingException(0, e.getLocalizedMessage());
      }
   }
}
