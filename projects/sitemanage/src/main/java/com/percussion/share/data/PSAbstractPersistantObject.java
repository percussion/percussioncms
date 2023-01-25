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

import com.percussion.pagemanagement.data.PSPage;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Classes can extend this class to be persistant.
 * All the proper methods that are needed for hibernate have been extended.
 * 
 * @author adamgent
 *
 */
public abstract class PSAbstractPersistantObject extends PSAbstractDataObject implements Serializable {

    public abstract String getId();
    
    public abstract void setId(String id);

    private static final long serialVersionUID = 1L;

}
