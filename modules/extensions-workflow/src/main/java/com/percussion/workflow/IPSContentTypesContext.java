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

package com.percussion.workflow;

/**
 * An interface that defines methods for content types context. 
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */

import java.sql.SQLException;

public interface IPSContentTypesContext
{
/**
 * Gets Query Request for the current entry in the context.
 *
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   quesry request 
 *
 */
   public String getContentTypeQueryRequest() throws SQLException;

/**
 * Gets Update Request for the current entry in the context.
 *
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   update request 
 *
 */
   public String getContentTypeUpdateRequest() throws SQLException;

/**
 * Gets New Request for the current entry in the context.
 *
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   new request 
 *
 */
   public String getContentTypeNewRequest() throws SQLException;

/**
 * Gets content type name for the current entry in the context.
 *
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   content type name 
 *
 */
   public String getContentTypeName() throws SQLException;

/**
 * Gets content type description for the current entry in the context.
 *
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   content type description 
 *
 */
   public String getContentTypeDescription() throws SQLException;

/**
 * Closes the context freeing all JDBC resources.
 *
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   none
 *
 */
   public void close();
}
