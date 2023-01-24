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

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSServerErrors;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.Iterator;

/**
 * The output document is built up over a number of steps. Most of the work
 * is performed by the base class. The main purpose of this class is to create
 * the appropriate build step needed when creating this document.
 * <p>Unlike other builders, this builder isn't trying to create a content
 * editor type doc that will be used to generate an html page for end user
 * editing. It is creating a dataset that includes all of the non-binary
 * fields in a given child, regardless of the showInPreview and showInSummary
 * flags.
 */
public class PSCompleteChildDocumentBuilder extends PSEditorDocumentBuilder
{
   /**
    * Processes the supplied editor definition, creating an executable plan
    * that will be used when requests are made. Adds a single build step which
    * creates a table view of the child data. See {@link
    * PSModifyDocumentBuilder#PSModifyDocumentBuilder(PSContentEditor,
    * PSEditorDocumentContext, int, boolean ) base} class for params and their
    * requirements that aren't described below.
    *
    * @param mapping A mapping that contains a complex child.
    *
    * @throws IllegalArgumentException if ce, ctx or mapping is <code>null
    *    </code>.
    */
   public PSCompleteChildDocumentBuilder( PSContentEditor ce,
         PSEditorDocumentContext ctx, PSDisplayMapping mapping,
         int pageId, boolean isError )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( ce, ctx, pageId );
      if ( null == mapping )
         throw new IllegalArgumentException( "mapping can't be null" );

      PSFieldSet fields = ((PSContentEditorPipe) ce.getPipe()).getMapper().
            getFieldSet( mapping.getDisplayMapper().getFieldSetRef());
      if ( null == fields )
         throw new PSNotFoundException( IPSServerErrors.CE_MISSING_FIELDSET,
               mapping.getDisplayMapper().getFieldSetRef());

      addBuildStep( new PSTableValueBuilder( fields,
            mapping.getUISet(), mapping.getDisplayMapper(), this, true ));
   }

   /**
    * See base for description. Overridden because we don't care about this
    * stuff.
    *
    * @return Always <code>null</code> which prevents the information from
    *    being added to the doc.
    */
   protected Node createSectionLinkElement( Document doc,
         Iterator linkExtractors, PSExecutionData data )
      throws PSDataExtractionException
   {
      return null;
   }

   /**
    * See base for description. Overridden because we don't care about this
    * stuff.
    *
    * @return Always <code>null</code> which prevents the information from
    *    being added to the doc.
    */
   protected Iterator getActionLinks( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      return Collections.emptyList().iterator();
   }
}



