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
package com.percussion.services.publisher.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import org.xml.sax.SAXException;

import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Represents a content list in the database
 *
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSContentList")
@Table(name = "RXCONTENTLIST")
public class PSContentList implements IPSContentList {
    @Id
    @Column(name = "CONTENTLISTID")
    long contentListId;
    @SuppressWarnings("unused")
    @Version
    private Integer version;
    @Basic
    String name;
    @Basic
    String description;
    @Basic
    Integer type = 0;
    @Basic
    String url;
    @Basic
    String generator;
    @Basic
    String expander;
    @Basic
    @Column(name = "EDITIONTYPE")
    String editionType;
    @Basic
    @Column(name = "FILTER_ID")
    Long filterId = null;
    @OneToMany(targetEntity = PSContentListGeneratorParam.class, cascade =  {
        CascadeType.ALL}
    , fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "CONTENT_LIST_ID")
    @Fetch(FetchMode.SUBSELECT)
    Set<PSContentListGeneratorParam> generatorArguments = new HashSet<>();
    @OneToMany(targetEntity = PSTemplateExpanderParam.class, cascade =  {
        CascadeType.ALL}
    , fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "CONTENT_LIST_ID")
    @Fetch(FetchMode.SUBSELECT)
    Set<PSTemplateExpanderParam> expanderArguments = new HashSet<>();

    /**
     * The item filter as a transient object, can only exist when the
     * Content List object is loaded from service layer; otherwise it is
     * <code>null</code> (as not defined).
     */
    transient IPSItemFilter m_filter = null;

    /*
     * //see base class method for details
     */
    public boolean isLegacy() {
        return StringUtils.isBlank(generator) && StringUtils.isBlank(expander) &&
        (filterId == null);
    }

    /* (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#getGeneratorParams()
     */
    public Map<String, String> getGeneratorParams() {
        Map<String, String> rval = new HashMap<>();

        if (generatorArguments != null) {
            for (PSContentListGeneratorParam p : generatorArguments) {
                rval.put(p.getName(), p.getValue());
            }
        }

        return rval;
    }

    /**
     * Set the generator arguments. This method carefully folds the new arguments
     * into the old arguments.
     *
     * @param newargs the new arguments, never <code>null</code>
     */
    public void setGeneratorParams(Map<String, String> newargs) {
        if (newargs == null) {
            throw new IllegalArgumentException("newargs may not be null");
        }

        // before accessing, do the check if the generatorArguments is valid
        if (generatorArguments == null) {
            return;
        }

        // First remove any old argument that no longer belongs
        Set<String> removals = new HashSet<>();

        for (PSContentListGeneratorParam param : generatorArguments) {
            if (!newargs.keySet().contains(param.getName())) {
                removals.add(param.getName());
            }
        }

        for (String n : removals) {
            removeGeneratorParam(n);
        }

        // Add or modify existing, the add method takes care of this
        for (String n : newargs.keySet()) {
            String value = newargs.get(n);
            addGeneratorParam(n, value);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#getExpanderParams()
     */
    public Map<String, String> getExpanderParams() {
        Map<String, String> rval = new HashMap<>();

        if (expanderArguments != null) {
            for (PSTemplateExpanderParam p : expanderArguments) {
                rval.put(p.getName(), p.getValue());
            }
        }

        return rval;
    }

    /**
     * Set the expander arguments. This method carefully folds the new arguments
     * into the old arguments.
     *
     * @param newargs the new arguments, never <code>null</code>
     */
    public void setExpanderParams(Map<String, String> newargs) {
        if (newargs == null) {
            throw new IllegalArgumentException("newargs may not be null");
        }

        // before accessing, do the check if the expanderArguments is valid
        if (expanderArguments == null) {
            return;
        }

        // First remove any old argument that no longer belongs
        Set<String> removals = new HashSet<>();

        for (PSTemplateExpanderParam param : expanderArguments) {
            if (!newargs.keySet().contains(param.getName())) {
                removals.add(param.getName());
            }
        }

        for (String n : removals) {
            removeExpanderParam(n);
        }

        // Add or modify existing, the add method takes care of this
        for (String n : newargs.keySet()) {
            String value = newargs.get(n);
            addExpanderParam(n, value);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#addGeneratorParam(java.lang.String,
     *      java.lang.String)
     */
    public void addGeneratorParam(String n, String value) {
        if (StringUtils.isBlank(n)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }

        if (StringUtils.isBlank(value)) {
            removeGeneratorParam(n);

            return;
        }

        if (generatorArguments != null) {
            for (PSContentListGeneratorParam p : generatorArguments) {
                if (p.getName().equals(n)) {
                    p.setValue(value);

                    return;
                }
            }
        }

        IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
        PSContentListGeneratorParam newparam = new PSContentListGeneratorParam();
        newparam.setId(mgr.createGuid(PSTypeEnum.INTERNAL).longValue());
        newparam.setName(n);
        newparam.setValue(value);
        newparam.setContentList(this);

        if (generatorArguments == null) {
            generatorArguments = new HashSet<>();
        }

        generatorArguments.add(newparam);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#removeGeneratorParam(java.lang.String)
     */
    public void removeGeneratorParam(String n) {
        if (StringUtils.isBlank(n)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }

        if (generatorArguments == null) {
            return;
        }

        PSContentListGeneratorParam found = null;

        for (PSContentListGeneratorParam p : generatorArguments) {
            if (p.getName().equals(n)) {
                found = p;

                break;
            }
        }

        if (found != null) {
            generatorArguments.remove(found);
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#addExpanderParam(java.lang.String,
     *      java.lang.String)
     */
    public void addExpanderParam(String n, String value) {
        if (StringUtils.isBlank(n)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }

        if (StringUtils.isBlank(value)) {
            removeExpanderParam(n);

            return;
        }

        if (expanderArguments != null) {
            for (PSTemplateExpanderParam p : expanderArguments) {
                if (p.getName().equals(n)) {
                    p.setValue(value);

                    return;
                }
            }
        }

        IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
        PSTemplateExpanderParam newparam = new PSTemplateExpanderParam();
        newparam.setId(mgr.createGuid(PSTypeEnum.INTERNAL).longValue());
        newparam.setName(n);
        newparam.setValue(value);
        newparam.setContentList(this);

        if (expanderArguments == null) {
            expanderArguments = new HashSet<>();
        }

        expanderArguments.add(newparam);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#removeExpanderParam(java.lang.String)
     */
    public void removeExpanderParam(String n) {
        if (StringUtils.isBlank(n)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }

        if (expanderArguments == null) {
            return;
        }

        PSTemplateExpanderParam found = null;

        for (PSTemplateExpanderParam p : expanderArguments) {
            if (p.getName().equals(n)) {
                found = p;

                break;
            }
        }

        if (found != null) {
            expanderArguments.remove(found);
        }
    }

    /**
     * Get the content list id, only used in serialization
     *
     * @return the content list id, never <code>null</code> for a persisted
     *         object, may be <code>null</code> otherwise
     */
    public long getContentListId() {
        return contentListId;
    }

    /**
     * @param contentListId
     */
    public void setContentListId(Integer contentListId) {
        this.contentListId = contentListId;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#getEditionType()
     */
    public PSEditionType getEditionType() {
        if (editionType == null) {
            editionType = "2"; // Default
        }

        Integer et = new Integer(editionType);

        return PSEditionType.valueOf(et);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#setEditionType(com.percussion.services.publisher.data.PSEditionType)
     */
    public void setEditionType(PSEditionType editionType) {
        this.editionType = Integer.toString(editionType.getTypeId());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#getExpander()
     */
    public String getExpander() {
        return expander;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#setExpander(java.lang.String)
     */
    public void setExpander(String expander) {
        this.expander = expander;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#getGenerator()
     */
    public String getGenerator() {
        return generator;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#setGenerator(java.lang.String)
     */
    public void setGenerator(String generator) {
        this.generator = generator;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#getUrl()
     */
    public String getUrl() {
        return url;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#setUrl(java.lang.String)
     */
    public void setUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url may not be null or empty");
        }

        this.url = url;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public PSContentList clone() {
        PSContentList newclist = new PSContentList();

        newclist.setDescription(getDescription());
        newclist.setEditionType(getEditionType());
        newclist.setExpander(getExpander());
        newclist.setGenerator(getGenerator());
        newclist.setExpanderParams(getExpanderParams());
        newclist.setGeneratorParams(getGeneratorParams());
        newclist.setFilterId(getFilterId());
        newclist.setUrl(getUrl());
        newclist.setType(getType());
        newclist.setContentListId((int) PSGuidHelper.generateNextLong(
                PSTypeEnum.CONTENT_LIST));
        newclist.setName("copied" + newclist.getContentListId());

        return newclist;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof PSContentList)) {
            return false;
        }

        PSContentList cl = (PSContentList) arg0;

        return new EqualsBuilder().append(type, cl.type)
                                  .append(description, cl.description)
                                  .append(editionType, cl.editionType)
                                  .append(expander, cl.expander)
                                  .append(filterId, cl.filterId)
                                  .append(generator, cl.generator)
                                  .append(expanderArguments,
            cl.expanderArguments)
                                  .append(generatorArguments,
            cl.generatorArguments).append(name, cl.name).append(url, cl.url)
                                  .isEquals();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (name != null) ? name.hashCode() : (-1);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#toXML()
     */
    public String toXML() throws IOException, SAXException {
        return PSXmlSerializationHelper.writeToXml(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#fromXML(java.lang.String)
     */
    public void fromXML(String xmlsource) throws IOException, SAXException {
        PSXmlSerializationHelper.readFromXML(xmlsource, this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#getGUID()
     */
    public IPSGuid getGUID() {
        return new PSGuid(PSTypeEnum.CONTENT_LIST, contentListId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
     */
    public void setGUID(IPSGuid newguid) throws IllegalStateException {
        contentListId = newguid.longValue();
    }

    /**
     * @return Returns the version.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#getFilter()
     */
    public IPSGuid getFilterId() {
        if (filterId == null) {
            return null;
        } else {
            return PSGuidUtils.makeGuid(filterId, PSTypeEnum.ITEM_FILTER);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.publisher.IPSContentList#setFilter(com.percussion.services.filter.IPSItemFilter)
     */
    public void setFilterId(IPSGuid filter) {
        if (filter == null) {
            this.filterId = null;
        } else {
            this.filterId = filter.longValue();
        }

        m_filter = null;
    }

    @IPSXmlSerialization(suppress = true)
    public IPSItemFilter getFilter() {
        return m_filter;
    }

    public void setFilter(IPSItemFilter filter) {
        if (filter == null) {
            setFilterId(null);
        } else {
            setFilterId(filter.getGUID());
        }

        m_filter = filter;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#getType()
     */
    public Type getType() {
        int tordinal = (type == null) ? 0 : type.shortValue();

        return Type.valueOf(tordinal);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.services.publisher.IPSContentList#setType(com.percussion.services.publisher.IPSContentList.Type)
     */
    public void setType(Type newtype) {
        if (newtype == null) {
            throw new IllegalArgumentException("newtype may not be null");
        }

        type = newtype.ordinal();
    }
}
