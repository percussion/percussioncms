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
package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.data.PSMetadataQuery;
import com.percussion.delivery.metadata.impl.utils.PSPair;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public PSPair<List<IPSMetadataEntry>, Integer> executeQuery(PSMetadataQuery query) throws Exception;
    public List<Object[]>  executeCategoryQuery(PSMetadataQuery query) throws Exception;
    /**
     * Based on the query hibernate return type would be different Following
     * enum is to handle the return type from the hibernate if NONE it returns
     * List<Object> If PROPERTY it returns List<[Object[])> If METADATA it
     * returns List<Object>
     * 
     * 
     */
    public enum SORTTYPE
    {
        NONE, PROPERTY, METADATA
    }

    // Column names in the properties table
    public static final String PROP_DATEVALUE_COLUMN_NAME = "datevalue";

    public static final String PROP_NUMBERVALUE_COLUMN_NAME = "numbervalue";

    public static final String PROP_STRINGVALUE_COLUMN_NAME = "stringvalue";

    public static final String PROP_VALUEHASH_COLUMN_NAME="valueHash";

    public static final String PROP_TEXTVALUE_COLUMN_NAME = "textvalue";

    public static final String SORT_ORDER_ASCEND = "asc";

    public static final String SORT_ORDER_DESCEND = "desc";
    
    public Integer getQueryLimit();
    
    public void setQueryLimit(Integer limit);

}
