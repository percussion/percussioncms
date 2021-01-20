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
package com.percussion.services.publisher;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.extension.IPSExtension;
import com.percussion.services.publisher.data.PSContentListItem;

import java.util.List;
import java.util.Map;

import javax.jcr.query.QueryResult;

/**
 * A template expander takes a content guid and returns zero or more template
 * guids to use for publishing (or any other purpose).
 * 
 * @author dougrand
 */
public interface IPSTemplateExpander extends IPSExtension
{
   /**
    * Find the templates that are appropriate for the given content guid
    * 
    * @param results The filtered results from the generator, having had items
    *        removed by the filter. The expander can consider any data from the
    *        rows when deciding what templates to expand to. Generally
    *        "rx:sys_contenttypeid" is important in this process, but the
    *        expander may consider other properties. Never <code>null</code>.
    * 
    * @param parameters parameters from the request and the database that
    *        control the behavior of the template expander. May be
    *        <code>null</code> or empty if the specific expander allows this.
    *        The engine will put the following parameters:
    *        <TABLE BORDER="1">
    *          <TR><TH>Parameter Name</TH><TH>Description</TH></TR>
    *          <TR><TD>{@link IPSHtmlParameters#SYS_SITEID}</TD>
    *          <TD>The ID of the publishing site.</TD></TR>
    *          <TR><TD>{@link IPSHtmlParameters#SYS_CONTEXT}</TD>
    *          <TD>The ID of the delivery context.</TD></TR>
    *        </TABLE>
    * 
    * @param summaryMap a map of content id to component summary passed from the
    *        engine to the expander as an efficiency measure. All content IDs in
    *        <code>results</code> will be in this map.
    *        
    * @return some number of items to create the content list from. The number
    *         of items in the list is dependent on the expansion.
    * @throws PSPublisherException
    */
   List<PSContentListItem> expand(QueryResult results,
         Map<String, String> parameters,
         Map<Integer, PSComponentSummary> summaryMap)
         throws PSPublisherException;
}
