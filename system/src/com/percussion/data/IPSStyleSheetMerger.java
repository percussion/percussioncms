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

package com.percussion.data;

import com.percussion.server.PSRequest;

import java.io.OutputStream;

import org.w3c.dom.Document;


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

