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
package com.percussion.data.macro;

import com.percussion.data.IPSDataErrors;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.error.PSException;
import com.percussion.security.PSUserEntry;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;

/**
 * Macro extractor to get the authtype dependent revision for the item of the
 * current request.
 */
public class PSContextDependentRevisionExtractor implements IPSMacroExtractor
{
   /**
    * Extract the revision depending on the request authtype. The request 
    * authtype is retrievd from the HTML parameters. If not provided, it 
    * defaults to ALL (0). For the ALL authtype (0) this macro extracts 
    * the tip revision if the checkout user is the current user, otherwise the 
    * current revision. For all other authtypes this will return the current 
    * revision if the item is in a public state. Otherwise the last public 
    * revision as determined from the item history or -1 if the item has never 
    * been public. 
    * This macro requires the content id provided either as HTML parameter or 
    * in the execution data with column name 
    * <code>IPSConstants.ITEM_PKEY_CONTENTID</code>.
    * An <code>IllegalArgumentException</code> will be thrown if a required 
    * parameter is missing.
    *  
    * @see {@link IPSMacroExtractor.extract(PSExecutionData)} for parameter 
    *    details.
    */
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      try
      {
         PSRequest request = data.getRequest();
         
         String contentid = PSMacroUtils.extractContentId(data);
         if (contentid == null)
         {
            Object[] args =
            {
               IPSHtmlParameters.SYS_CONTENTID,
               "$contextDependentRevision",
               "null",
               "request must supply the contentid"
            };
            
            throw new PSDataExtractionException(
               IPSDataErrors.MACRO_EXTRACTOR_INVALID_PARAMETER, args);
         }
            
         contentid = contentid.trim();
         if (contentid.length() == 0)
         {
            Object[] args =
            {
               IPSHtmlParameters.SYS_CONTENTID,
               "$contextDependentRevision",
               "empty",
               "request must supply the contentid"
            };
            
            throw new PSDataExtractionException(
               IPSDataErrors.MACRO_EXTRACTOR_INVALID_PARAMETER, args);
         }
         
         String authtype = request.getParameter(
            IPSHtmlParameters.SYS_AUTHTYPE, AUTHTYPE_ALL);
         if (authtype.equals(AUTHTYPE_ALL))
         {
            String checkoutUser = PSMacroUtils.extractCheckoutUser(
               contentid, data);
               
            PSUserEntry[] userEntries = 
               request.getUserSession().getAuthenticatedUserEntries();
               
            // there should never be more then 1 user entry
            PSUserEntry user = null;
            if (userEntries != null)
               user = userEntries[0];
               
            if (checkoutUser != null && user != null)
            {
               if (checkoutUser.equals(user.getName()))
               {
                  String tipRevision = PSMacroUtils.extractTipRevision(
                     contentid, data);
                  if (tipRevision == null || tipRevision.trim().length() == 0)
                  {
                     // this should never happen
                     throw new IllegalStateException(
                        "processed item is in an invalid state");
                  }
                        
                  return tipRevision;
               }
            }
            
            String currentRevision = PSMacroUtils.extractCurrentRevision(
               contentid, data);
            if (currentRevision == null || currentRevision.trim().length() == 0)
            {
               // this should never happen
               throw new IllegalStateException(
                  "processed item is in an invalid state");
            }
                  
            return currentRevision;
         }
         
         if (PSMacroUtils.isItemPublic(request, contentid))
         {
            String currentRevision = PSMacroUtils.extractCurrentRevision(
               contentid, data);
            if (currentRevision == null || currentRevision.trim().length() == 0)
            {
               // this should never happen
               throw new IllegalStateException(
                  "processed item is in an invalid state");
            }
                  
            return currentRevision;
         }
         
         String lastPublicRevision = PSMacroUtils.getLastPublicRevision(
            contentid);

         return lastPublicRevision;
      }
      catch (PSException e)
      {
         throw new PSDataExtractionException(e.getErrorCode(), 
            e.getErrorArguments());
      }
   }
   
   /**
    * Constant that defines the one of the possible values for sys_authtype 
    * HTML parameter. It means all content.
    */
   private static final String AUTHTYPE_ALL = "0";
}
