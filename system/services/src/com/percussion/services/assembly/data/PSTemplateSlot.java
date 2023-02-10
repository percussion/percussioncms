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
package com.percussion.services.assembly.data;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.utils.xml.IPSXmlSerialization;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.xml.sax.SAXException;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * This object represents a single slot that is related to a template. Templates
 * are related to slots via a many to many relationship.
 *
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSTemplateSlot")
@Table(name = "RXSLOTTYPE")
@NaturalIdCache
@NamedQuery(name = "slot.findSlotsByNames",
                query = "select s from PSTemplateSlot s where lower(s.name) in (:names)")
public class PSTemplateSlot
        implements
        IPSTemplateSlot,
        IPSCatalogSummary,
        IPSCatalogItem,
        IPSCloneTuner,
        Serializable {
    /**
     * Serial id identifies versions of serialized data
     */
    private static final long serialVersionUID = 1L;

    static {
        // Register types with XML serializer for read creation of objects
        PSXmlSerializationHelper.addType("slot-type-association",
                PSTemplateTypeSlotAssociation.class);
    }

    @OneToMany(targetEntity = PSTemplateTypeSlotAssociation.class, mappedBy="id.slotId", cascade = {CascadeType.ALL, CascadeType.MERGE}, fetch = FetchType.EAGER, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSTemplateSlot_Associations")
    @Fetch(FetchMode.SUBSELECT)
    public Set<PSTemplateTypeSlotAssociation> slotAssociations = new HashSet<>();


    @Id
    @Column(name = "SLOTID")
    private long id;
    @Version
    @Column(name = "VERSION")
    private Integer version;
    @Basic
    @NaturalId(mutable=true)
    @Column(name = "SLOTNAME",unique=true)
    private String name;
    @Basic
    @Column(name = "LABEL")
    private String label;
    @Basic
    @Column(name = "SLOTDESC")
    private String description;
    @Basic
    @Column(name = "SYSTEMSLOT")
    private Integer systemslot;
    @Basic
    @Column(name = "SLOTTYPE")
    private int slottype;
    @Basic
    @Column(name = "RELATIONSHIPNAME")
    private String relationshipName;
    @Basic
    @Column(name = "FINDER")
    private String finder;
    @OneToMany(mappedBy = "containingSlot", targetEntity = PSSlotContentFinderParam.class, cascade =
            {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSTemplateSlot_Template")
    @Fetch(FetchMode.SUBSELECT)
    private Set<PSSlotContentFinderParam> finderArguments = new HashSet<>();
    @ManyToMany(targetEntity = PSAssemblyTemplate.class, mappedBy = "slots")
    private Set<IPSAssemblyTemplate> slotTemplates = new HashSet<>();

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogSummary#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set, never <code>null</code> or empty
     */
    public void setName(String n) {
        if (StringUtils.isBlank(n)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }
        name = n;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogSummary#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param d The description to set.
     */
    public void setDescription(String d) {
        description = d;
    }

    /**
     * Accessor for xml serialization only
     *
     * @return Returns the slottype.
     */
    public int getSlottype() {
        return slottype;
    }

    /**
     * Set the type of the slot
     *
     * @param type the type, never <code>null</code>
     */
    public void setSlottype(SlotType type) {
        if (type == null) {
            throw new IllegalArgumentException("type may not be null");
        }
        slottype = type.ordinal();
    }

    /**
     * Accessor for xml serialization only
     *
     * @param st The slottype to set.
     */
    public void setSlottype(int st) {
        slottype = st;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#getSlottypeEnum()
     */
    @IPSXmlSerialization(suppress = true)
    public SlotType getSlottypeEnum() {
        return SlotType.valueOf(slottype);
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#getFinderName()
     */
    public String getFinderName() {
        return finder;
    }

    /**
     * @param f The finder to set.
     */
    public void setFinderName(String f) {
        finder = f;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#getFinderArguments()
     */
    public Map<String, String> getFinderArguments() {
        return finderArguments.stream()
                .collect(toMap(PSSlotContentFinderParam::getName, PSSlotContentFinderParam::getValue));
    }

    /**
     * Handle the setting of arguments by calling the add method repeatedly.
     *
     * @param arguments the new arguments, may be <code>null</code>
     */
    public void setFinderArguments(Map<String, String> arguments) {
        finderArguments.clear();
        arguments.forEach(this::addFinderArgument);

    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#addFinderArgument(java.lang.String,
     * java.lang.String)
     */
    public void addFinderArgument(String n, String value) {
        if (StringUtils.isBlank(n)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }
        if (StringUtils.isBlank(value)) {
            return;
        }

        PSSlotContentFinderParam newparam = new PSSlotContentFinderParam(this, n, value);

        finderArguments.add(newparam);

    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#removeFinderArgument(java.lang.String)
     */
    public void removeFinderArgument(String n) {
        if (StringUtils.isBlank(n)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }
        PSSlotContentFinderParam param = new PSSlotContentFinderParam(this, n, null);
        finderArguments.remove(param);
    }

    /**
     * Get the object version.
     *
     * @return the object version, <code>null</code> if not initialized yet.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Set the object version. The version can only be set once in the life cycle
     * of this object.
     *
     * @param version the version of the object, must be >= 0.
     */
    public void setVersion(Integer version) {
        if (this.version != null && version != null)
            throw new IllegalStateException("version can only be initialized once");

        if (version != null && version < 0)
            throw new IllegalArgumentException("version must be >= 0");

        this.version = version;
    }

    /**
     * @return Returns the relationshipName.
     */
    public String getRelationshipName() {
        return relationshipName;
    }

    /**
     * @param rn The relationshipName to set.
     */
    public void setRelationshipName(String rn) {
        relationshipName = rn;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#toXML()
     */
    public String toXML() throws IOException, SAXException {
        return PSXmlSerializationHelper.writeToXml(this);
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#fromXML(java.lang.String)
     */
    public void fromXML(String xmlsource) throws IOException, SAXException {
        id = 0L; // Avoid problems during restore
        this.version=null;
        PSXmlSerializationHelper.readFromXML(xmlsource, this);
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#getGUID()
     */
    public IPSGuid getGUID() {
        return new PSGuid(PSTypeEnum.SLOT, id);
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
     */
    public void setGUID(IPSGuid newguid) throws IllegalStateException {
        id = newguid.longValue();
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#isSystemSlot()
     */
    public boolean isSystemSlot() {
        return systemslot != null && systemslot == 1;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#setSystemSlot(boolean)
     */
    public void setSystemSlot(boolean sslot) {
        if (sslot)
            systemslot = 1;
        else
            systemslot = null;
    }

    /**
     * Get the collection of content type and templates that this slot is
     * associated with. The returned set can be modified, but no change to the
     * underlying association will be made until the corresponding set method is
     * called with the new data.
     * <p>
     * The first element in each pair is the guid to the content type, the second
     * is the template's guid.
     *
     * @return get the slotAssociations set, never <code>null</code>
     */
    @IPSXmlSerialization(suppress = true)
    public Collection<PSPair<IPSGuid, IPSGuid>> getSlotAssociations() {

        return  slotAssociations.stream()
                .map(a -> new PSPair<IPSGuid,IPSGuid>(new PSGuid(PSTypeEnum.NODEDEF, a.getContentTypeId())
                        , new PSGuid(PSTypeEnum.TEMPLATE, a.getTemplateId()))).collect(toList());
    }

    /**
     * Set the slot associations - used for serialization only
     *
     * @param newassociations The slotAssociations to set.
     */
    public void setSlotAssociations(
            Collection<PSPair<IPSGuid, IPSGuid>> newassociations) {
        this.slotAssociations.clear();
        Set<PSTemplateTypeSlotAssociation> coll =
        Objects.requireNonNull(newassociations)
                .stream()
                .map(p -> new PSTemplateTypeSlotAssociation(p.getFirst(), p.getSecond(), this.id))
                .collect(toSet());
        this.slotAssociations.addAll(coll);

    }

    /**
     * Get the slot associations - used for MSM
     *
     * @return the slot associations as an arrau
     */
    public PSTemplateTypeSlotAssociation[] getSlotTypeAssociations() {
        return slotAssociations.toArray(new PSTemplateTypeSlotAssociation[0]);
    }

    /**
     * Set the slot associations, used by MSM
     *
     */
    public void setSlotTypeAssociations(
            PSTemplateTypeSlotAssociation[] associations) {
        this.slotAssociations.clear();
        this.slotAssociations.addAll(Arrays.stream(associations).collect(toSet()));
    }

    /**
     * Add a slot type association, used for MSM
     *
     * @param association the association to add, never <code>null</code>
     */
    public void addSlotTypeAssociation(PSTemplateTypeSlotAssociation association) {
        if (association == null) {
            throw new IllegalArgumentException("object may not be null");
        }
        slotAssociations.add(association);
    }

    /**
     * Add a single association to the set, if the association already exists
     * this call will have no effect.
     *
     * @param pair a new pair to add, the first element is the content type guid
     *             and the second is the template guid, never <code>null</code>
     */
    public void addSlotAssociation(PSPair<IPSGuid, IPSGuid> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("pair may not be null");
        }

        PSTemplateTypeSlotAssociation tts = new PSTemplateTypeSlotAssociation(pair.getFirst(),
                pair.getSecond(), this.id);
        this.slotAssociations.add(tts);
    }

    /**
     * Remove a single association from the set, if the association does not
     * exist this call will have no effect
     *
     * @param pair a pair to remove, the first element is the content type guid
     *             and the second is the template guid, never <code>null</code>
     */
    public void removeSlotAssociation(PSPair<IPSGuid, IPSGuid> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("pair may not be null");
        }
        PSTemplateTypeSlotAssociation tts = new PSTemplateTypeSlotAssociation(pair.getFirst(),
                pair.getSecond(), this.id);
        this.slotAssociations.remove(tts);
    }

    /**
     * Get a map that associates a particular content type with a set of template
     * ids for this slot
     *
     * @return a map, never <code>null</code> but could be empty
     */
    protected Map<IPSGuid, Set<IPSGuid>> getSlotAssociationMap() {

        return slotAssociations.stream()
                .collect(
                        groupingBy(
                                a -> new PSGuid(PSTypeEnum.NODEDEF, a.getContentTypeId()), HashMap::new,
                                mapping(a -> new PSGuid(PSTypeEnum.TEMPLATE, a.getTemplateId()), toSet())));

    }

    /**
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSTemplateSlot{");
        sb.append("slotAssociations=").append(slotAssociations);
        sb.append(", id=").append(id);
        sb.append(", version=").append(version);
        sb.append(", name='").append(name).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", systemslot=").append(systemslot);
        sb.append(", slottype=").append(slottype);
        sb.append(", relationshipName='").append(relationshipName).append('\'');
        sb.append(", finder='").append(finder).append('\'');
        sb.append(", finderArguments=").append(finderArguments);
        sb.append(", slotTemplates=").append(slotTemplates);
        sb.append('}');
        return sb.toString();
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.assembly.IPSTemplateSlot#getLabel()
     */
    public String getLabel() {
        return label == null ? name : label;
    }

    /**
     * @param l The label to set, never <code>null</code> or empty
     */
    public void setLabel(String l) {
        if (StringUtils.isBlank(l)) {
            label = null;
        } else
            label = l;
    }

    /**
     * Get the type
     *
     * @return get the type for this object
     */
    @IPSXmlSerialization(suppress = true)
    public PSTypeEnum getType() {
        return PSTypeEnum.SLOT;
    }


    @IPSXmlSerialization(suppress = true)
    @Override
    public Set<IPSAssemblyTemplate> getSlotTemplates() {
        return slotTemplates;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.percussion.services.data.IPSCloneTuner#tuneClone(long)
     */
    public Object tuneClone(long newId) {
        id = newId;
        for (PSTemplateTypeSlotAssociation assoc : slotAssociations) {
            assoc.setSlotId(newId);
        }
        if (finderArguments != null) {
            for (PSSlotContentFinderParam arg : finderArguments) {
                arg.setContainingSlot(this);
            }
        }
        // TODO tune the object missing anything?
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSTemplateSlot)) return false;
        PSTemplateSlot that = (PSTemplateSlot) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
