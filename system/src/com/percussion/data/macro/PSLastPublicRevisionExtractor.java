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
package com.percussion.data.macro;

import com.percussion.data.IPSDataErrors;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;

/**
 * Macro extractor to get the last public revision for the item of the
 * current request.
 */
public class PSLastPublicRevisionExtractor implements IPSMacroExtractor
{
   /**
    * This macro extractor extracts the last public revision or the current
    * item supplied with the execution data as HTML parameter or backend column.
    * The last public revision is determined through the item history and 
    * returned as a <code>String</code>. -1 will be returned if no public
    * revision was found.
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
               "$lastPublicRevision",
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
               "$lastPublicRevision",
               "empty",
               "request must supply the contentid"
            };
            
            throw new PSDataExtractionException(
               IPSDataErrors.MACRO_EXTRACTOR_INVALID_PARAMETER, args);
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
}
