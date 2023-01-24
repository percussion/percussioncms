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

import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSField;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.extension.PSExtensionException;


/**
 * This class just like {@link PSPreviewDocumentBuilder}, except it override
 * the "showInPreview" to "yes". 
 */
public class PSPreviewDocumentBuilderEx extends PSPreviewDocumentBuilder
{
   /**
    * Call {@link PSPreviewDocumentBuilder#PSPreviewDocumentBuilder(
    * PSContentEditor, PSEditorDocumentContext, PSDisplayMapper, int, boolean)}
    */
   public PSPreviewDocumentBuilderEx( PSContentEditor ce,
         PSEditorDocumentContext ctx, PSDisplayMapper dispMapper, int pageId,
         boolean isError )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( ce, ctx, dispMapper, pageId, isError );
   }


   // see base for desc
   boolean showField( PSField field )
   {
      return true; 
   }
}
