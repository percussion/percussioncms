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
package com.percussion.fastforward.relationship;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.util.IPSHtmlParameters;

/**
 * This class is intended to be used as a FieldOutputTranslation within a
 * content editor, to populate a field's value with a list of content ids of the
 * relationship owners in a specific slot.
 * 
 * @author James Schultz
 * @since 6.0
 */
public class PSExtractIdsFromSlotExit extends PSDefaultExtension implements
   IPSUdfProcessor
{

   /**
    * Returns a list of the owner content ids from the relationships in the slot
    * identified by the "slotname" parameter that have the request's content
    * item as their dependent.
    * 
    * @param params the parameter values. required: "slotname" for name of the
    * slot whose relationships will be used.
    * @param request the current request context.
    * @return a list of owner content ids as ";" delimited string from the
    * slot's matching relationships, or <code>null</code> if there are no
    * matching relationships, e.g. <code>692;651;339</code>.
    * @throws PSConversionException if request does not include a sys_contentid
    * parameter, if "slotname" parameter is missing or empty, if slot cannot be
    * found, or if relationship API throws exception.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      // get the current content item from the request
      String contentId = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      if (StringUtils.isNumeric(contentId))
      {
         int cid = Integer.parseInt(contentId);

         // get the slot name from the supplied parameters
         Map<String, String> paramMap = getParameters(params);
         String slotname = paramMap.get("slotname");
         if (StringUtils.isBlank(slotname))
            throw new PSConversionException(0,
               "must provide non-blank slotname parameter value");

         try
         {
            PSChildRelationshipParser parser = new PSChildRelationshipParser(PSRelationshipProcessor.getInstance());
            List<Integer> ids = parser.parse(cid, slotname);
            String idsString = null;
            if (ids != null)
            {
               idsString = "";
               for (int i=0; i<ids.size(); i++)
               {
                  idsString += ids.get(i).toString();
                  if (i < ids.size()-1)
                     idsString += ";";
               }
            }
            
            return idsString;
         }
         catch (PSAssemblyException e)
         {
            ms_log.error("Failed to find slot <" + slotname + ">", e);
            throw new PSConversionException(0, e);
         }
         catch (PSCmsException e)
         {
            ms_log.error("Failure in relationship API", e);
            throw new PSConversionException(0, e);
         }
      }
      else
      {
         /*
          * not every request will have a content id (such as creating a new
          * item), so don't throw exception if it is missing.
          */
         ms_log.debug("skipping extract; no content id in request");
         return null;
      }
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Logger ms_log = LogManager
      .getLogger(PSExtractIdsFromSlotExit.class);
}
