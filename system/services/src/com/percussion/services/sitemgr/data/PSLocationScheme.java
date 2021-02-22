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
package com.percussion.services.sitemgr.data;

import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;

import com.percussion.xml.PSXmlDocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 * The location scheme represents how a content item should be represented in a
 * URL or in a directory tree. The location scheme creates the "path" to the
 * item. For the URL this is appended onto the site root, and for filesystem
 * publishing this is appended to the publishing root.
 *
 * @author dougrand
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSLocationScheme")
@Table(name = "RXLOCATIONSCHEME")
public class PSLocationScheme implements IPSCatalogItem, IPSLocationScheme,
    Serializable, Cloneable, IPSCatalogIdentifier {
    /**
     * Serial id identifies versions of serialized data
     */
    private static final long serialVersionUID = 1L;

    /**
     * Root node name of this object's XML representation.
     */
    public static final String XML_NODE_NAME = "PSXLocationScheme";

    // private XML constants
    private static final String XML_PARAM_NODE_NAME = "PSXLocationSchemeParam";
    private static final String XML_ATTR_CT_ID_NAME = "content-type-id";
    private static final String XML_ATTR_CTX_ID_NAME = "context-id";
    private static final String XML_ATTR_DESCR_NAME = "description";
    private static final String XML_ATTR_GEN_NAME = "generator";
    private static final String XML_ATTR_SCHEME_ID_NAME = "scheme-id";
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_TEMP_ID_NAME = "template-id";
    private static final String XML_ATTR_TYPE_NAME = "type";
    private static final String XML_ATTR_VALUE_NAME = "value";
    private static final String XML_ATTR_SEQ_NAME = "sequence";
    @Id
    @Column(name = "SCHEMEID")
    long schemeId = -1L;
    @Version
    @Column(name = "VERSION")
    Integer version;
    @Basic
    @Column(name = "SCHEMENAME")
    String name;
    @Basic
    @Column(name = "DESCRIPTION")
    String description;
    @Basic
    @Column(name = "VARIANTID")
    long templateId = -1L;
    @Basic
    @Column(name = "CONTENTTYPEID")
    long contentTypeId = -1L;
    @Basic
    @Column(name = "CONTEXTID")
    long contextId = -1L;
    @Basic
    @Column(name = "GENERATOR")
    String generator;
    @OneToMany(targetEntity = PSLocationSchemeParameter.class, cascade =  {
        CascadeType.ALL}
    , fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "SCHEMEID", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSLocationScheme_Parameters")
    @Fetch(FetchMode.SUBSELECT)
    Set<PSLocationSchemeParameter> parameters = new HashSet<>();

    /**
     * Provide backward compatible. See {@link #getContext()}.
     */
    transient IPSPublishingContext m_context;

    /**
     * Determines if this is a cloned object.
     */
    transient boolean m_isCloned = false;

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getContentTypeId()
     */
    public Long getContentTypeId() {
        if (contentTypeId == -1L) {
            return null;
        } else {
            return contentTypeId;
        }
    }

    /**
     * Determines if this is a cloned object.
     * Note, the cloned object cannot be saved through the CRUD service.
     *
     * @return <code>true</code> if this is a cloned object; otherwise it is
     * not a cloned object.
     */
    public boolean isCloned() {
        return m_isCloned;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setContentTypeId(java.lang.Long)
     */
    public void setContentTypeId(Long contentTypeId) {
        if (contentTypeId == null) {
            throw new IllegalArgumentException("contentTypeId may not be null");
        }

        this.contentTypeId = contentTypeId.longValue();
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getContext()
     */
    public IPSGuid getContextId() {
        if (contextId == -1L) {
            return null;
        } else {
            return PSGuidUtils.makeGuid(contextId, PSTypeEnum.CONTEXT);
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setContext(com.percussion.services.sitemgr.IPSPublishingContext)
     */
    public void setContextId(IPSGuid contextId) {
        if (contextId == null) {
            throw new IllegalArgumentException("contextId may not be null");
        }

        this.contextId = contextId.longValue();
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getContext()
     */
    public IPSPublishingContext getContext() {
        throw new UnsupportedOperationException(
            "Use getContextId() to get the ID of the context.");
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setContext(com.percussion.services.sitemgr.IPSPublishingContext)
     */
    public void setContext(
        @SuppressWarnings("unused")
    IPSPublishingContext context) {
        throw new UnsupportedOperationException(
            "Use setContextId(IPSGuid) to set the ID of the context.");
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getGenerator()
     */
    public String getGenerator() {
        return generator;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setGenerator(java.lang.String)
     */
    public void setGenerator(String generator) {
        if (StringUtils.isBlank(generator)) {
            throw new IllegalArgumentException(
                "generator may not be null or empty");
        }

        this.generator = generator;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the global unique id for this item
     *
     * @return the globally unique id, never <code>null</code>. See
     * {@link IPSGuid}for more information.
     */
    public IPSGuid getGUID() {
        return PSGuidUtils.makeGuid(schemeId, PSTypeEnum.LOCATION_SCHEME);
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSCatalogItem#setGUID(IPSGuid)
     */
    public void setGUID(IPSGuid guid) {
        if (guid == null) {
            throw new IllegalArgumentException("guid may not be null");
        }

        if (this.schemeId != -1L) {
            throw new IllegalStateException("guid can only be set once");
        }

        //todo - change to long in db to be consistent
        this.schemeId = guid.longValue();
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getTemplateId()
     */
    public Long getTemplateId() {
        if (templateId == -1L) {
            return null;
        } else {
            return templateId;
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setTemplateId(java.lang.Long)
     */
    public void setTemplateId(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId may not be null");
        }

        this.templateId = templateId.longValue();
    }

    /**
     * Used to set the id for this location scheme.
     *
     * @param id the new id, may not be <code>null</code>.
     */
    public void setId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id may not be null");
        }

        this.schemeId = id.longValue();
    }

    /**
     * Get the hibernate version information for this object.
     *
     * @return returns the version, may be <code>null</code>.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Set the hibernate version information for this object.
     *
     * @param version The version to set.
     *
     * @throws IllegalStateException if an attempt is made to set a previously
     * set version to a non-<code>null</code> value.
     */
    public void setVersion(Integer version) {
        if ((this.version != null) && (version != null)) {
            throw new IllegalStateException("Version can only be set once");
        }

        this.version = version;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        PSLocationScheme b = (PSLocationScheme) obj;

        return new EqualsBuilder().append(schemeId, b.schemeId)
                                  .append(contentTypeId, b.contentTypeId)
                                  .append(description, b.description)
                                  .append(name, b.name)
                                  .append(parameters, b.parameters)
                                  .append(templateId, b.templateId)
                                  .append(contextId, b.contextId).isEquals();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description)
                                    .append(parameters).toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getParameterNames()
     */
    public List<String> getParameterNames() {
        List<PSLocationSchemeParameter> sorted = getSortedParameters();

        List<String> rval = new ArrayList<>();

        for (PSLocationSchemeParameter p : sorted) {
            rval.add(p.getName());
        }

        return rval;
    }

    /**
     * Get the parameters sorted by sequence.
     * @return the parameters sorted by sequence, never <code>null</code> but
     * could be empty.
     */
    @SuppressWarnings("unchecked")
    private List<PSLocationSchemeParameter> getSortedParameters() {
        if (parameters.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<PSLocationSchemeParameter> sorted = new ArrayList<>();
        sorted.addAll(parameters);

        if (sorted.size() == 1) {
            return sorted;
        }

        Collections.sort(sorted,
            new Comparator<PSLocationSchemeParameter>() {
                public int compare(PSLocationSchemeParameter o1,
                    PSLocationSchemeParameter o2) {
                    Integer first = (o1.sequence != null) ? o1.sequence : 0;
                    Integer second = (o2.sequence != null) ? o2.sequence : 0;

                    return first - second;
                }
            });

        return sorted;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getParameterValue(java.lang.String)
     */
    public String getParameterValue(String paramname) {
        for (PSLocationSchemeParameter p : parameters) {
            if (p.getName().equals(paramname)) {
                return p.getValue();
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getParameterType(java.lang.String)
     */
    public String getParameterType(String paramname) {
        for (PSLocationSchemeParameter p : parameters) {
            if (p.getName().equals(paramname)) {
                return p.getType();
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#getParameterSequence(java.lang.String)
     */
    public Integer getParameterSequence(String paramname) {
        for (PSLocationSchemeParameter p : parameters) {
            if (p.getName().equals(paramname)) {
                return p.getSequence();
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#setParameter(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setParameter(String paramname, String type, String value) {
        addParameter(paramname, 0, type, value);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#addParameter(java.lang.String, int, java.lang.String, java.lang.String)
     */
    public void addParameter(String paramname, int sequence, String type,
        String value) {
        if (StringUtils.isBlank(paramname)) {
            throw new IllegalArgumentException(
                "paramname may not be null or empty");
        }

        if (sequence < 0) {
            throw new IllegalArgumentException("sequence may not be negative");
        }

        PSLocationSchemeParameter param = null;

        // First see if the parameter exists
        for (PSLocationSchemeParameter p : parameters) {
            if (p.getName().equals(paramname)) {
                param = p;

                break;
            }
        }

        List<PSLocationSchemeParameter> sorted = null;

        if (param == null) {
            sorted = getSortedParameters();
            param = new PSLocationSchemeParameter();
            param.setParameterId((int) PSGuidHelper.generateNextLong(
                    PSTypeEnum.LOCATION_PROPERTY));
            param.setName(paramname);
            parameters.add(param);

            Iterator<PSLocationSchemeParameter> piter = sorted.iterator();
            PSLocationSchemeParameter existing = null;

            for (int i = 0; (i < sequence) && piter.hasNext(); i++) {
                existing = piter.next();
            }

            if (existing == null) {
                param.setSequence(10);
            } else {
                param.setSequence(existing.getSequence() + 1);
            }

            param.setScheme(this);
        }

        param.setType(type);
        param.setValue(value);

        // Fix order information
        int seq = 10;
        sorted = getSortedParameters();

        for (PSLocationSchemeParameter p : sorted) {
            p.setSequence(seq);
            seq += 10;
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.services.sitemgr.IPSLocationScheme#removeParameter(java.lang.String)
     */
    public void removeParameter(String n) {
        PSLocationSchemeParameter param = null;

        for (PSLocationSchemeParameter p : parameters) {
            if (p.getName().equals(n)) {
                param = p;

                break;
            }
        }

        if (param != null) {
            parameters.remove(param);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        PSLocationScheme copy = (PSLocationScheme) super.clone();
        copy.parameters = new HashSet<>(parameters);
        copy.m_isCloned = true;

        return copy;
    }

    /**
     * Restores this object's state from its XML representation (string).  See
     * {@link #toXML()} for format of XML.  See
     * {@link IPSCatalogItem#fromXML(String)} for more info on method
     * signature.
     */
    public void fromXML(String xmlsource)
        throws IOException, SAXException, PSInvalidXmlException {
        PSStringUtils.notBlank(xmlsource, "xmlsource may not be null or empty");

        Reader r = new StringReader(xmlsource);
        Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);
        NodeList nodes = doc.getElementsByTagName(XML_NODE_NAME);

        if (nodes.getLength() == 0) {
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
                XML_NODE_NAME);
        }

        Element elem = (Element) nodes.item(0);
        int ctId = PSXmlUtils.checkAttributeInt(elem, XML_ATTR_CT_ID_NAME, true);
        setContentTypeId(new Long(ctId));

        String ctxId = PSXmlUtils.checkAttribute(elem, XML_ATTR_CTX_ID_NAME,
                true);
        setContextId(new PSGuid(ctxId));

        String descr = PSXmlUtils.checkAttribute(elem, XML_ATTR_DESCR_NAME,
                false);

        if (descr.length() > 0) {
            setDescription(descr);
        } else {
            setDescription(null);
        }

        String gen = PSXmlUtils.checkAttribute(elem, XML_ATTR_GEN_NAME, true);
        setGenerator(gen);

        String nm = PSXmlUtils.checkAttribute(elem, XML_ATTR_NAME, false);

        if (nm.length() > 0) {
            setName(nm);
        } else {
            setName(null);
        }

        int schId = PSXmlUtils.checkAttributeInt(elem, XML_ATTR_SCHEME_ID_NAME,
                true);
        setId(Long.valueOf(schId));

        int tempId = PSXmlUtils.checkAttributeInt(elem, XML_ATTR_TEMP_ID_NAME,
                true);
        setTemplateId(new Long(tempId));

        NodeList paramNodes = elem.getElementsByTagName(XML_PARAM_NODE_NAME);

        for (int i = 0; i < paramNodes.getLength(); i++) {
            Element param = (Element) paramNodes.item(i);
            String n = PSXmlUtils.checkAttribute(param, XML_ATTR_NAME, true);
            String type = PSXmlUtils.checkAttribute(param, XML_ATTR_TYPE_NAME,
                    true);
            String value = PSXmlUtils.checkAttribute(param,
                    XML_ATTR_VALUE_NAME, true);
            int sequence = PSXmlUtils.checkAttributeInt(param,
                    XML_ATTR_SEQ_NAME, true);

            addParameter(n, sequence, type, value);
        }
    }

    /**
     * Serializes this object's state to its XML representation as a string.  The
     * format is:
     * <pre><code>
     * &lt;!ELEMENT PSXLocationScheme (PSXLocationSchemeParam*)>
     * &lt;!ATTLIST PSXLocationScheme
     *    content-type-id CDATA #REQUIRED
     *    context-id CDATA #REQUIRED
     *    description CDATA
     *    generator CDATA #REQUIRED
     *    name CDATA
     *    scheme-id CDATA #REQUIRED
     *    template-id CDATA #REQUIRED
     * >
     * &lt;!ATTLIST PSXLocationSchemeParam>
     *    name CDATA #REQUIRED
     *    type CDATA #REQUIRED
     *    value CDATA #REQUIRED
     *    sequence CDATA #REQUIRED
     * >
     * </code></pre>
     *
     * See {@link IPSCatalogItem#toXML()} for more info.
     */
    public String toXML() {
        Document doc = PSXmlDocumentBuilder.createXmlDocument();

        Element root = doc.createElement(XML_NODE_NAME);
        root.setAttribute(XML_ATTR_CT_ID_NAME, String.valueOf(contentTypeId));
        root.setAttribute(XML_ATTR_CTX_ID_NAME, getContextId().toString());

        if (description != null) {
            root.setAttribute(XML_ATTR_DESCR_NAME, description);
        }

        root.setAttribute(XML_ATTR_GEN_NAME, generator);

        if (name != null) {
            root.setAttribute(XML_ATTR_NAME, name);
        }

        root.setAttribute(XML_ATTR_SCHEME_ID_NAME, String.valueOf(schemeId));
        root.setAttribute(XML_ATTR_TEMP_ID_NAME, String.valueOf(templateId));

        for (PSLocationSchemeParameter param : parameters) {
            Element child = doc.createElement(XML_PARAM_NODE_NAME);
            child.setAttribute(XML_ATTR_NAME, param.getName());
            child.setAttribute(XML_ATTR_TYPE_NAME, param.getType());
            child.setAttribute(XML_ATTR_VALUE_NAME, param.getValue());
            child.setAttribute(XML_ATTR_SEQ_NAME, param.getSequence().toString());
            root.appendChild(child);
        }

        doc.appendChild(root);

        return PSXmlDocumentBuilder.toString(doc);
    }

    /**
     * Copy the parameters from the source to the target object.
     * Note, the ID the parameters are not copied.
     *
     * @param src the source object, assumed not <code>null</code>.
     * @param tgt the target object, assumed not <code>null</code>.
     */
    private void copyParameters(IPSLocationScheme src, PSLocationScheme tgt) {
        List<String> pnames = tgt.getParameterNames();

        // copy parameters from the source 1st
        int i = 0;

        for (String pname : src.getParameterNames()) {
            tgt.addParameter(pname, i++, src.getParameterType(pname),
                src.getParameterValue(pname));
            pnames.remove(pname);
        }

        // remove remaining parameters from the target
        for (String pname : pnames) {
            tgt.removeParameter(pname);
        }
    }

    /*
     * //see base class method for details
     */
    public void copy(IPSLocationScheme other) {
        if (other == null) {
            throw new IllegalArgumentException("other may not be null.");
        }

        if (!(other instanceof PSLocationScheme)) {
            throw new IllegalArgumentException(
                "other must be instance of PSLocationScheme.");
        }

        PSLocationScheme src = (PSLocationScheme) other;
        name = src.name;
        description = src.description;
        contentTypeId = src.contentTypeId;
        templateId = src.templateId;
        contextId = src.contextId;
        generator = src.generator;

        copyParameters(src, this);
    }
}
