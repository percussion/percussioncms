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
