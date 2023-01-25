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
