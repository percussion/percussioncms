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

