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
package com.percussion.cms;

import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSNotFoundException;
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
