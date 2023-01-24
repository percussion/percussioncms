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

package com.percussion.soln.p13n.delivery;

import java.util.Iterator;

import javax.jcr.RepositoryException;

/**
 * 
 * A item in the delivery system that is ready to be processed and then returned to 
 * the visitor.
 * The item is a subset of the JCR 170 Node interface.
 * 
 * @author adamgent
 *
 */
public interface IDeliveryResponseItem {
    
    public String getId();
    
    /**
     * Gets property or fail.
     * @param name never <code>null</code>.
     * @return never <code>null</code>.
     * @throws RepositoryException
     */
    public IDeliveryProperty getProperty(String name) throws RepositoryException;
    
    /**
     * 
     * @return never <code>null</code>.
     * @throws RepositoryException
     */
    public Iterator<IDeliveryProperty> getProperties() throws RepositoryException;
    
    /**
     * 
     * @param name never <code>null</code>.
     * @return <code>true</code> if property exists.
     * @throws RepositoryException
     */
    public boolean hasProperty(String name) throws RepositoryException;
      
}
