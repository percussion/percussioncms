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
