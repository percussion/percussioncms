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

import com.percussion.HTTPClient.Codecs;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.jexl.PSDocumentUtils;
import com.percussion.services.utils.general.PSAssemblyServiceUtils;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This exit will add the inline variant body to the result document if
 * this is of type "rxvariant"
 */
public class PSGetInlineVariantBody extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{

   public boolean canModifyStyleSheet()
   {
     return false;
   }

   @SuppressWarnings("unused")
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
   throws PSParameterMismatchException, PSExtensionProcessingException
   {
      boolean tokenizeScriptTags = false;
      if(params.length > 0 && params[0] instanceof PSTextLiteral)
      {
         String val = ((PSTextLiteral)params[0]).getText();
         tokenizeScriptTags = val.equalsIgnoreCase("true") ? true : false;
      }
      Element root = resultDoc.getDocumentElement();
      String docurl = root.getAttribute("urlstring");
      String inlinetype = root.getAttribute("inlinetype");
      if(StringUtils.isNotBlank(inlinetype) && inlinetype.equals("rxvariant"))
      {
          if(StringUtils.isNotBlank(docurl))
          {
             try
            {
               PSDocumentUtils docUtils = new PSDocumentUtils();
               String inlineText = root
                     .getAttribute(PSSingleValueBuilder.INLINE_TEXT);
               if (StringUtils.isNotBlank(inlineText))
                  docurl = docurl + "&" + PSSingleValueBuilder.INLINE_TEXT
                        + "=" + Codecs.URLEncode(inlineText);               
               String doc = PSAssemblyServiceUtils.getAssembledDocument(
                  docurl, null);
               String body = docUtils.extractBody(doc);
              if(tokenizeScriptTags)
              {
                  // We replace the start of the beginning and ending <script> tags
                  // which will be put back via javascript when in memory. This
                  // is because script tags cannot be in a javascript string
                  // assignment statement.
                  body = StringUtils.replace(body,
                     SCRIPT_END, SCRIPT_END_TOKEN);
                  body = StringUtils.replace(body,
                     SCRIPT_START, SCRIPT_START_TOKEN);
              }
               Element bodyEl = resultDoc.createElement("body");
               bodyEl.appendChild(resultDoc.createTextNode(body));
               root.appendChild(bodyEl);
            }
            catch (Exception e)
            {
               throw new PSExtensionProcessingException("Exception", e);
            }
          }
      }
      return resultDoc;
   }

   /**
    * Constant that represents the script end tag
    */
   private static final String SCRIPT_END = "</script";

   /**
    * Constant that represents the script start tag
    */
   private static final String SCRIPT_START = "<script";

   /**
    * Constant that represents the script end tag token
    */
   private static final String SCRIPT_END_TOKEN = "@RX_SCRIPT_END@";

   /**
    * Constant that represents the script start tag token
    */
   private static final String SCRIPT_START_TOKEN = "@RX_SCRIPT_START@";


}
