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

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The base class for all data objects.
 * All data objects should extend this class or 
 * some derivative.
 * 
 * @author adamgent
 *
 */
public class PSAbstractDataObject implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Override
    public Object clone() throws CloneNotSupportedException {
        try
        {
            return BeanUtils.cloneBean(this);
        }
        catch (Exception e)
        {
            throw new CloneNotSupportedException();
        }
    }
    
}
