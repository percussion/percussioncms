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

package com.percussion.soln.p13n.delivery.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.jcr.RepositoryException;

import com.percussion.soln.p13n.delivery.IDeliveryResponseItem;
import com.percussion.soln.p13n.delivery.IDeliveryProperty;
import com.percussion.soln.p13n.delivery.data.DeliveryItem;

public abstract class AbstractDeliveryResponseItem implements IDeliveryResponseItem {


    public Iterator<IDeliveryProperty> getProperties() throws RepositoryException {
        Iterator<IDeliveryProperty> it = new Iterator<IDeliveryProperty>() {
            private Iterator<Entry<String, String>> realIter = 
                getItemData().getProperties().entrySet().iterator();
            public boolean hasNext() {
                return realIter.hasNext();
            }

            public IDeliveryProperty next() {
                Entry<String, String> entry = realIter.next();
                return new DeliveryProperty(entry.getKey(), entry.getValue()); 
            }

            public void remove() {
                throw new UnsupportedOperationException("remove is not yet supported");
            }
        };
        return it;
    }

    public IDeliveryProperty getProperty(String name) throws RepositoryException {
        if(getItemData().getProperties() != null && getItemData().getProperties().containsKey(name)) {
           return new DeliveryProperty(name, getItemData().getProperties().get(name));
        }
        throw new RepositoryException("Property: " + name + " not found.");
    }


    public boolean hasProperty(String name) throws RepositoryException {
       return getItemData().getProperties().containsKey(name);
    }

    public abstract DeliveryItem getItemData();
    

}
