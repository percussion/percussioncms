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
package com.percussion.share.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

public abstract class PSAbstractTransformer<OLD,NEW> implements Transformer
{

    @SuppressWarnings("unchecked")
    public List<NEW> collect(Collection<OLD> old) {
        List<NEW> newList = new ArrayList<>();
        newList.addAll(CollectionUtils.collect(old, this));
        return newList;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object transform(Object old)
    {
        try {
            return doTransform((OLD)old);
        } catch (PSDataServiceException e) {
            //TODO: Not sure how to handle the error state here.
            throw new RuntimeException(e);
        }
    }
    
    protected abstract NEW doTransform(OLD old) throws PSDataServiceException;

}

