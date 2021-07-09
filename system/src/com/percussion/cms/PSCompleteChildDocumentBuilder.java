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

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSServerErrors;

import java.util.Collections;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
      return Collections.EMPTY_LIST.iterator();
   }
}



