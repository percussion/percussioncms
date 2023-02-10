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
