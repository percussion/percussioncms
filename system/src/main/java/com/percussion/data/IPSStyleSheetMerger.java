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

package com.percussion.data;

import com.percussion.server.PSRequest;
import org.w3c.dom.Document;

import java.io.OutputStream;


/**
 * The IPSStyleSheetMerger interface must be implemented by all
 * processors capable of merging an XML document with a type of style sheet
 * to generate HTML output.
 * <p>
 * The style sheet implementations must be named PS{Type}StyleSheetMerger
 * where {Type} represents the style sheet type in proper case format. For
 * instance, the following are possible style sheet types:
 * <table border="2">
 *   <tr><th>Style Sheet Type</th><th>Expected Class Name</th></tr>
 *   <tr><td>XSL</td>             <td>PSXslStyleSheetMerger</td></tr>
 *   <tr><td>CSS1</td>            <td>PSCss1StyleSheetMerger</td></tr>
 *   <tr><td>CSS2</td>            <td>PSCss2StyleSheetMerger</td></tr>
 * </table>
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSStyleSheetMerger {
   /**
    * Merge the style sheet defined in the XML document to generate
    * HTML output. The <code>stylesheet</code> processing instruction
    * must exist in the XML document and define the style sheet to use.
    *
    * @param   req         the request document which may provide additional
    *                        context information required by the processor
    *
    * @param   doc         the XML document to be processed
    *
    * @param   out         the output stream to which the results will be
    *                        written
    *
    * @exception   PSUnsupportedConversionException
    *                        if the style sheet defined in the XML document
    *                        is of an unsupported type
    */
   public void merge(PSRequest req, Document doc, OutputStream out)
      throws PSConversionException, PSUnsupportedConversionException;
}

