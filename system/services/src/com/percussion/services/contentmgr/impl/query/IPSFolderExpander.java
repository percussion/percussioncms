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
package com.percussion.services.contentmgr.impl.query;

import com.percussion.utils.guid.IPSGuid;

import java.util.List;

import javax.jcr.query.InvalidQueryException;


/**
 * Implement this interface to expand a folder path to a list of folder
 * guids. Defined as a separate interface primarily for testing purposes,
 * although extra decoupling can be handy.
 * 
 * @author dougrand
 */
public interface IPSFolderExpander
{
   /**
    * Expand the given folder path. The path is a slash separated folder
    * path using the '%' character as a wildcard. The path does not have to
    * be to a valid folder path.
    * @param path the path, never <code>null</code> or empty
    * @return zero or more guids that each identify a folder
    * @throws InvalidQueryException if a path is invalid.
    */
   List<IPSGuid> expandPath(String path) throws InvalidQueryException;
}
