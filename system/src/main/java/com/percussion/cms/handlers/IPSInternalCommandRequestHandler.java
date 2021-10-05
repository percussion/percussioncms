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
package com.percussion.cms.handlers;

import com.percussion.server.PSRequest;
import org.w3c.dom.Document;

/**
 * This interface gives command handlers the ability to perform internal
 * requests.
 */
public interface IPSInternalCommandRequestHandler
{
   /**
    * Makes an internal request using the provided request for the supplied
    * command.
    *
    * @param request the request to use, not <code>null</code>.
    * @param command the command handler name, not <code>null</code> or
    *    empty.
    * @return the result XML document, never <code>null</code>.
    */
   public Document makeInternalRequest(PSRequest request, String command);
}
