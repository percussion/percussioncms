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
package com.percussion.cms;

import com.percussion.data.PSConversionException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * The compare panel defaults to using the previous revision if the second
 * revision isn't supplied, and there is either only a single content id
 * supplied, or the two content ids match.
 * 
 * @author dougrand
 */
public class PSCalculateCompareRevision implements IPSRequestPreProcessor
{
   private static final String SYS_REVISION2 = "sys_revision2";
   private static final Logger log = LogManager.getLogger(PSCalculateCompareRevision.class);

   /**
    * Calculate the right revision to use for the second revision. Returns an
    * empty string if the second revision is not determined. The arguments to
    * this extension are: <table>
    * <tr>
    * <th>Index</th>
    * <th>Description</th>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>1st item content id, required</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>2nd item content id, optional</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>1st item revision, required</td>
    * </tr>
    * <tr>
    * <td>3</td>
    * <td>2nd item revision, optional</td>
    * </tr>
    * </table>
    * <ul>
    * <li> If there are two different content ids then the 2nd item revision
    * value is returned without processing.
    * <li> If the second item revision is supplied, then nothing is changed
    * in the request
    * <li> If the second item revision is not supplied, and the two content ids
    * match, or the second content id is not supplied, then the first revision
    * less 1 is set in place of the current sys_revision2.
    * If the first revision is 1 then sys_revision2 is set to that value.
    * </ul>
    * 
    */
   @SuppressWarnings("unused")
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      try
      {
         PSExtensionParams p = new PSExtensionParams(params);
         Number contentid1 = p.getNumberParam(0, null, true);
         Number contentid2 = p.getNumberParam(1, null, false);
         Number revision1 = p.getNumberParam(2, null, true);
         Number revision2 = p.getNumberParam(3, null, false);
   
         if (contentid2 == null)
         {
            contentid2 = contentid1;
            request.setParameter("sys_contentid2", contentid1.toString());
         }
         
         if (revision2 != null || !contentid1.equals(contentid2))
         {
            return; // Do nothing
         }
   
         if (contentid1.equals(contentid2) || contentid2 == null)
         {
            int r1 = revision1.intValue();
            if (r1 > 1)
            {
               request.setParameter(SYS_REVISION2, Integer.toString(r1 - 1));
            }
            else
            {
               request.setParameter(SYS_REVISION2, "1");
            }
         }
      }
      catch(PSConversionException e)
      {
         log.error("Problem calculating revision for compare, Error: {}", PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // Do nothing on init
   }
}
