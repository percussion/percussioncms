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
