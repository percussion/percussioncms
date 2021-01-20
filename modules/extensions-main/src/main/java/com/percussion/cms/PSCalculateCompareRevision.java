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
package com.percussion.cms;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private static Log ms_log = LogFactory.getLog(PSCalculateCompareRevision.class);

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
         ms_log.error("Problem calculating revision for compare", e);
      }
   }

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // Do nothing on init
   }
}
