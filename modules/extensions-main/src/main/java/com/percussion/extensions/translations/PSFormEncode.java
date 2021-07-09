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
package com.percussion.extensions.translations;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * Will turn a &lt;form&gt; tag into a &lt;div&gt; tag that has
 * a special attribute marker to indicate it is actually a form. 
 * Must use with the <code>PSFormDecode</code> input translation
 * to be sure the form tag is put back to its normal syntax.
 * 
 * Requires the first parameter to be the field name of the field to
 * be translated.
 *  
 * This is used to get around an issues with form tags not working
 * in EditLive single instance.
 * 
 * @author erikserating
 *
 */
public class PSFormEncode implements IPSFieldInputTransformer
{

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSUdfProcessor#
    * processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   @SuppressWarnings("unused")
   public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      String name = ep.getStringParam(0, null, false);
      if(StringUtils.isBlank(name))
      {
         Object[] args = new Object[]{"name"};
         throw new PSConversionException(
            IPSExtensionErrors.MISSING_REQUIRED_PARAM_NO, args);
      }
      String value = request.getParameter(name);
      return PSFormEncodeDecodeHelper.encode(value);
   }

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSExtension#
    * init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
   {
      // no-op      
   }

  

}
