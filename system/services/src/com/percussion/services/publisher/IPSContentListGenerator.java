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

import com.percussion.extension.IPSExtension;

import java.util.Map;

import javax.jcr.query.QueryResult;

/**
 * A generator creates a candidate list of content guids to be published.
 * 
 * @author dougrand
 * 
 */
public interface IPSContentListGenerator extends IPSExtension
{
   /**
    * Generate a list of candidate GUIDs for publishing.
    * 
    * @param parameters parameters that control the behavior of the generator,
    *        derived from the database and the request, may be
    *        <code>null</code> or empty.
    *        The engine will put the following parameters:
    *        <TABLE BORDER="1">
    *          <TR><TH>Parameter Name</TH><TH>Description</TH></TR>
    *          <TR><TD>{@link IPSHtmlParameters#SYS_SITEID}</TD>
    *          <TD>The ID of the publishing site.</TD></TR>
    *          <TR><TD>{@link IPSHtmlParameters#SYS_CONTEXT}</TD>
    *          <TD>The ID of the delivery context.</TD></TR>
    *        </TABLE>   
    *                
    * @return a list of query results to publish, may be empty but never
    *         <code>null</code>. Each row in the result will contain at least
    *         the values "rx:sys_contentid" and "rx:sys_contenttypeid". Data in
    *         the rows may be used in the expander. Only "rx:sys_contentid" will
    *         be used by the filter.
    *         
    * @throws PSPublisherException if any error occurs
    */
   QueryResult generate(Map<String, String> parameters)
         throws PSPublisherException;
}
