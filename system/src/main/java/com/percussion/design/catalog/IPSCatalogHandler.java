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

package com.percussion.design.catalog;

import org.w3c.dom.Document;


/**
 * The IPSCatalogHandler interface must be implemented by all catalog
 * request handlers. Each type of catalog request has its own handler.
 * The handler must construct the appropriate request from the given
 * input parameters.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSCatalogHandler {
   /**
    * Format the catalog request based upon the specified request
    * information. If any required information is missing, an exception
    * will be thrown.
    *
    * @param      out   the output stream to which the properly
    *                     formatted request will be written
    *
    * @param      req   the request information
    *
    *
    */
   public abstract Document formatRequest(java.util.Properties req);
}

