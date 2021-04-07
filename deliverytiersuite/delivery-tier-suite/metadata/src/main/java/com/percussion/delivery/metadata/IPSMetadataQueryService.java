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
package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.data.PSMetadataQuery;
import com.percussion.delivery.metadata.impl.utils.PSPair;

import java.util.List;

/**
 * @author erikserating
 *
 */
public interface IPSMetadataQueryService
{
    /**
     * Executes a query against the metadata query service.
     *
     * @param query the metadata query, cannot be <code>null</code>.
     * @return PSPair which contains list of result objects, those are sorted
     *         according to the orderby in the query and number of results also
     *         determined by the maxresults value in the query and total count
     *         of available objects for the passed in query criteria
     * @throws Exception on query parsing error
     */

    public PSPair<List<IPSMetadataEntry>, Integer> executeQuery(PSMetadataQuery query) throws Exception;

    /**
     * Based on the query hibernate return type would be different Following
     * enum is to handle the return type from the hibernate if NONE it returns
     * List<Object> If PROPERTY it returns List<[Object[])> If METADATA it
     * returns List<Object>
     *
     *
     */
     enum SORTTYPE
    {
        NONE, PROPERTY, METADATA
    }

     String PROP_DATEVALUE_COLUMN_NAME = "datevalue";

     String PROP_NUMBERVALUE_COLUMN_NAME = "numbervalue";

     String PROP_STRINGVALUE_COLUMN_NAME = "stringvalue";

     String PROP_VALUEHASH_COLUMN_NAME="valueHash";

     String PROP_TEXTVALUE_COLUMN_NAME = "textvalue";

     String SORT_ORDER_ASCEND = "asc";

     String SORT_ORDER_DESCEND = "desc";

     Integer getQueryLimit();

      void setQueryLimit(Integer limit);

}