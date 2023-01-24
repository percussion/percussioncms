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

import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * An abridged version of the JSR170 Node Property for Delivery Items.
 * @author adamgent
 *
 */
public interface IDeliveryProperty {

    public boolean getBoolean() throws ValueFormatException, RepositoryException;

    public Calendar getDate() throws ValueFormatException, RepositoryException;
    
    public boolean isMultiple() throws RepositoryException;

    public double getDouble() throws ValueFormatException, RepositoryException;

    public long getLong() throws ValueFormatException, RepositoryException;

    public String getName() throws RepositoryException;

    public String getString() throws ValueFormatException, RepositoryException;

    public int getType() throws RepositoryException;

    public Value getValue() throws ValueFormatException, RepositoryException;

    public Value[] getValues() throws ValueFormatException, RepositoryException;
}
