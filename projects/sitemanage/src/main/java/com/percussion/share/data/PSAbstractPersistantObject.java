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


    @Override
    public int hashCode() {
        //Do not use the id to generate a hash code.
        return HashCodeBuilder.reflectionHashCode(this, new String[] {"id"});
    }


    
    private static final long serialVersionUID = 1L;

}
