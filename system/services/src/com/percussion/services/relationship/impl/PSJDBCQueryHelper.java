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
package com.percussion.services.relationship.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSRelationshipFilter;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipPropertyData;

import com.percussion.services.relationship.data.PSRelationshipData;
import static com.percussion.util.PSSqlHelper.qualifyTableName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.*;

/**
 * A helper class used for building (JDBC) SQL from a given relationship filter.
 */
class PSJDBCQueryHelper implements IPSQueryHelper {
    /**
     * Partial query, select clause from the relationship table, only set once
     */
    private static String ms_selectRelationshipTable = null;

    /**
     * Used to cache the ", PSX_OBJECTRELATIONSHIPPROP p "
     */
    private static String ms_fromP = null;

    /**
     * Used to cache "select * from PSX_OBJECTRELATIONSHIP r, CONTENTSTATUS c
     */
    private static String ms_selectFromRandC = null;

    /**
     * The logger for this class.
     */
    @SuppressWarnings("unused")
    private static final Logger ms_logger = LogManager.getLogger("RelationshipService");

    /**
     * A list of field names for PSRelationshipData class.
     */
    private static final String FN_RID = "RID";
    private static final String FN_CONFIGID = "CONFIG_ID";
    private static final String FN_OWNERID = "OWNER_ID";
    private static final String FN_OWNER_REV = "OWNER_REVISION";
    private static final String FN_DEPENDENTID = "DEPENDENT_ID";

    // a list of pre-defined user properties
    private static final String FN_FOLDER_ID = "FOLDER_ID";
    private static final String FN_SITE_ID = "SITE_ID";
    private static final String FN_SLOT_ID = "SLOT_ID";
    private static final String FN_SORT_RANK = "SORT_RANK";
    private static final String FN_VARIANT_ID = "VARIANT_ID";
    private static final String FN_INLINELINK = "INLINE_RELATIONSHIP";

    // a list of field names for PSRelationshipPersistentProperty
    private static final String FN_PROPERTY_NAME = "PROPERTYNAME";
    private static final String FN_PROPERTY_VALUE = "PROPERTYVALUE";

    /**
     * Literals used to construct HQL
     */
    private static final String SELECT_FROM_R = "select r.RID,r.CONFIG_ID,r.OWNER_ID,r.OWNER_REVISION,r.DEPENDENT_ID,r.DEPENDENT_REVISION,r.SLOT_ID,r.SORT_RANK,r.VARIANT_ID,r.FOLDER_ID,r.SITE_ID,r.INLINE_RELATIONSHIP,r.WIDGET_NAME from ";
    private static final String JOIN_OWNERID = " where r.OWNER_ID = c.CONTENTID and ";
    private static final String JOIN_DEPENDENTID = " where r.DEPENDENT_ID = c.CONTENTID and ";
    private static final String JOIN_RID = " r.RID = p.RID ";
    private static final String R_TABLE = "r.";
    private static final String P_TABLE = "p.";
    private static final String EQ = " = ?";
    private static final String AND = " and ";
    private static final String R_TABLE_CONFIGID = R_TABLE + FN_CONFIGID;
    private static final String R_TABLE_DEPENDENTID = R_TABLE + FN_DEPENDENTID;

    /**
     * The filter used to create HQL, set by constructor, never modified
     * after that.
     */
    final private PSRelationshipFilter m_filter;

    /**
     * A list of pre-defined user properties, which exist in the main
     * relationships table ({@link IPSConstants#PSX_RELATIONSHIPS}). Never
     * <code>null</code>, but may be empty.
     */
    @SuppressWarnings("unchecked")
    private Collection<Map.Entry<String, String>> m_pduProps = Collections.EMPTY_LIST;

    /**
     * A list of customer properties, which exist in the relationship
     * properties table ({@link IPSConstants#PSX_RELATIONSHIPPROPERTIES}).
     * Never <code>null</code>, but may be empty.
     */
    @SuppressWarnings("unchecked")
    private Collection<Map.Entry<String, String>> m_customProps = Collections.EMPTY_LIST;

    /**
     * The query string buffer, used to create HQL, never <code>null</code>,
     * may be empty.
     */
    private StringBuffer m_qryBuffer = new StringBuffer();

    /**
     * The parameter values list, used to record the values of the above
     * parameter names. Never <code>null</code>, may be empty.
     */
    private List<Object> m_paramValues = new ArrayList<>();

    /**
     * Determines whether to append a {@link #AND} before add further
     * restrictions while constructing the HQL. Default to <code>false</code>.
     */
    private boolean m_isAddAND = false;

    /**
     * The WHERE string that need to be added or appended before add further
     * restrictions while constructing the HQL. Default to <code>null</code>.
     */
    private String m_where = null;

    /**
     * It indicates whether the post filtering owner revision is needed or not.
     * <code>true</code> if the post filtering is needed; otherwise, there is
     * no need to post filter the owner revision. Defaults to <code>null</code>
     */
    private Boolean m_postFilterOwnerRev = null;

    /**
     * It maps the config id to its relationship configuration object.
     */
    private Map<Integer, PSRelationshipConfig> m_configMap = null;

    /**
     * It maps the config name to its config id. It is initialized by
     */
    private Map<String, Integer> m_nameMapToId = null;

    /**
     * Creates an instance
     *
     * @param filter the filter contains the query criteria.
     * @param configMap it maps config id to config object,
     *   never <code>null</code> or empty.
     * @param nameMapToId it maps config name to config id, never
     *   <code>null</code> or empty.
     */
    public PSJDBCQueryHelper(PSRelationshipFilter filter,
        Map<Integer, PSRelationshipConfig> configMap,
        Map<String, Integer> nameMapToId) {
        if ((configMap == null) || configMap.isEmpty()) {
            throw new IllegalArgumentException(
                "configMap may not be null or empty.");
        }

        if ((nameMapToId == null) || nameMapToId.isEmpty()) {
            throw new IllegalArgumentException(
                "nameMapToId may not be null or empty.");
        }

        m_filter = filter;
        m_configMap = configMap;
        m_nameMapToId = nameMapToId;

        // separate pre-defined user properties and custom properties
        if (m_filter.getProperties().size() > 0) {
            Collection<String> pduPropNames = PSRelationshipConfig.getPreDefinedUserPropertyNames();
            m_pduProps = new ArrayList<>();
            m_customProps = new ArrayList<>();

            // separate pre-defined and customer properties
            Set<Map.Entry<String, String>> props = m_filter.getProperties()
                                                           .entrySet();

            for (Map.Entry<String, String> prop : props) {
                if (pduPropNames.contains(prop.getKey())) {
                    m_pduProps.add(prop);
                } else {
                    m_customProps.add(prop);
                }
            }
        }
    }

    /**
     * @return the filter object, never <code>null</code>.
     */
    public PSRelationshipFilter getFilter() {
        return m_filter;
    }

    /**
     * @return <code>true</code> if need to filter by owner revision as part
     *    of the post filtering process.
     */
    public boolean mayFilterOwnerRev() {
        // see if this has been determined already
        if (m_postFilterOwnerRev != null) {
            return m_postFilterOwnerRev.booleanValue();
        }

        boolean filterOwnerRev = true;

        if (m_filter.getLimitToOwnerRevision()) {
            filterOwnerRev = false;
        } else if ((m_filter.getOwner() == null) ||
                (m_filter.getOwner().getRevision() == -1)) {
            filterOwnerRev = false;
        }

        return filterOwnerRev;
    }

    /**
     * @return <code>true</code> if need to filter by dependent revision as
     *    part of the post filtering process.
     */
    public boolean mayFilterDependentRev() {
        boolean filterDependentRev = true;

        if ((m_filter.getDependent() == null) ||
                (m_filter.getDependent().getRevision() == -1) ||
                (m_filter.getDependents().size() > 1)) {
            filterDependentRev = false;
        }

        return filterDependentRev;
    }

    /**
     * Append the criteria that requires join the owner id (of the
     * relationship table) to the content id (of the contentstatus table).
     * Note, this must be called first for creating a HQL.
     * @throws SQLException
     */
    private void appendOwnerJoinCriteria() throws SQLException {
        if (m_filter.getLimitToEditOrCurrentOwnerRevision()) {
            appendSelectJoinOwnerId(m_qryBuffer);
            m_qryBuffer.append(
                " ((c.EDITREVISION > 0 and r.OWNER_REVISION = c.EDITREVISION) or (c.EDITREVISION <= 0 and r.OWNER_REVISION = c.CURRENTREVISION))");

            m_isAddAND = true;
        }

        if (m_filter.getLimitToPublicOwnerRevision()) {
            if (m_isAddAND) {
                m_qryBuffer.append(AND);
            } else {
                appendSelectJoinOwnerId(m_qryBuffer);
            }

            m_qryBuffer.append(" r.OWNER_REVISION = c.PUBLIC_REVISION");

            m_isAddAND = true;
        }

        if (m_filter.getOwnerContentTypeId() != -1) {
            if (m_isAddAND) {
                m_qryBuffer.append(AND);
            } else {
                appendSelectJoinOwnerId(m_qryBuffer);
            }

            m_qryBuffer.append("c.CONTENTTYPEID").append(EQ);
            m_paramValues.add(new Long(m_filter.getOwnerContentTypeId()));
            m_isAddAND = true;
        }

        if (m_filter.getOwnerObjectType() != -1) {
            if (m_isAddAND) {
                m_qryBuffer.append(AND);
            } else {
                appendSelectJoinOwnerId(m_qryBuffer);
            }

            m_qryBuffer.append("c.OBJECTTYPE").append(EQ);
            m_paramValues.add(new Integer(m_filter.getOwnerObjectType()));
            m_isAddAND = true;
        }
    }

    /**
     * Append the criteria that requires join the dependent id (of the
     * relationship table) to the content id (of the contentstatus table).
     * Note, this must be called after the {@link #appendOwnerJoinCriteria()}.
     * @throws SQLException
     */
    private void appendDependentJoinCriteria() throws SQLException {
        if ((m_filter.getDependentContentTypeIds() != null) &&
                (!m_filter.getDependentContentTypeIds().isEmpty())) {
            if (m_isAddAND) {
                throw new UnsupportedOperationException(
                    "Cannot join both owner and dependent ids (from " +
                    IPSConstants.PSX_RELATIONSHIPS +
                    " table) to CONTENTSTATUS table.");
            }

            appendSelectJoinDependentId(m_qryBuffer);

            if (m_filter.getDependentContentTypeIds().size() == 1) {
                m_qryBuffer.append("c.CONTENTTYPEID").append(EQ);
                m_paramValues.add(new Long(m_filter.getDependentContentTypeId()));
            } else {
                appendInClause("c.CONTENTTYPEID",
                    m_filter.getDependentContentTypeIds(), true);

                /*
                 m_qryBuffer.append("c.CONTENTTYPEID").append(" IN (:").append(
                 FN_DEPENDENT_CTYPEIDS).append(") ");
                 m_paramNames.add(FN_DEPENDENT_CTYPEIDS);
                 m_paramValues.add(m_filter.getDependentContentTypeIds());
                 */
            }

            m_isAddAND = true;
        }

        if (m_filter.getDependentObjectType() != -1) {
            if (m_isAddAND && (m_filter.getDependentContentTypeId() != -1)) {
                throw new UnsupportedOperationException(
                    "Cannot join both owner and dependent ids (from " +
                    IPSConstants.PSX_RELATIONSHIPS +
                    " table) to CONTENTSTATUS table.");
            }

            if (m_isAddAND) {
                m_qryBuffer.append(AND);
            } else {
                appendSelectJoinDependentId(m_qryBuffer);
            }

            m_qryBuffer.append("c.OBJECTTYPE").append(EQ);
            m_paramValues.add(new Integer(m_filter.getDependentObjectType()));
            m_isAddAND = true;
        }

        // add the SELECT clause if has not done yet
        if (!m_isAddAND) {
            if (ms_selectRelationshipTable == null) {
                ms_selectRelationshipTable = SELECT_FROM_R +
                    qualifyTableName(IPSConstants.PSX_RELATIONSHIPS) + " r ";
            }

            m_qryBuffer.append(ms_selectRelationshipTable);

            if (joinPropertiesTable()) {
                m_qryBuffer.append(fromPropertyTable());
                m_qryBuffer.append("where ");
                m_qryBuffer.append(JOIN_RID);
                m_isAddAND = true;
            } else {
                m_where = "where ";
            }
        }
    }

    /**
     * Creates IN Clause statement and appends to the query buffer.
     *
     * @param valueName the column name or alias of the IN clause, assumed not
     *   <code>null</code> or empty.
     * @param valueList the values in the IN clause, assumed not
     *   <code>null</code> or empty.
     * @param addParamValues <code>true</code> if need to add the values
     *   into m_paramValues.
     */
    private void appendInClause(String valueName, Collection valueList,
        boolean addParamValues) {
        m_qryBuffer.append(valueName).append(" IN (");

        Iterator values = valueList.iterator();
        boolean isFirst = true;

        while (values.hasNext()) {
            if (isFirst) {
                m_qryBuffer.append("?");
            } else {
                m_qryBuffer.append(",?");
            }

            Object value = values.next();

            if (addParamValues) {
                m_paramValues.add(value);
            }

            isFirst = false;
        }

        m_qryBuffer.append(")");
    }

    /**
     * Append the criterias that can simply added to the WHERE clause
     * Note, this must be called after the
     * {@link #appendDependentJoinCriteria()}.
     */
    private void appendRestCriterias() {
        // handle the config_ids
        Set<Integer> ids = getRelationshipNameIds(m_filter);

        if (!ids.isEmpty()) {
            appendAndWhere();

            if (ids.size() == 1) {
                m_qryBuffer.append(R_TABLE).append(FN_CONFIGID).append(EQ);
                m_paramValues.add(ids.iterator().next());
            } else {
                appendInClause(R_TABLE_CONFIGID, ids, true);
            }

            m_isAddAND = true;
        }

        // handle the owner id and/or owner revision
        if (m_filter.getOwner() != null) {
            appendAndWhere();

            m_qryBuffer.append(R_TABLE).append(FN_OWNERID).append(EQ);
            m_paramValues.add(new Integer(m_filter.getOwner().getId()));
            m_isAddAND = true;

            if (m_filter.getLimitToOwnerRevision() || useOwnerRevision(ids)) {
                // avoid post filter owner revision
                m_postFilterOwnerRev = Boolean.FALSE;

                appendAndWhere();

                m_qryBuffer.append(R_TABLE).append(FN_OWNER_REV).append(EQ);
                m_paramValues.add(new Integer(m_filter.getOwner().getRevision()));
                m_isAddAND = true;
            }
        }

        // handle where clause for the dependent
        if (m_filter.getDependents() != null) {
            appendAndWhere();

            if (m_filter.getDependents().size() == 1) {
                m_qryBuffer.append(R_TABLE).append(FN_DEPENDENTID).append(EQ);
                m_paramValues.add(new Integer(m_filter.getDependent().getId()));
            } else {
                appendInClause(R_TABLE_DEPENDENTID, m_filter.getDependents(),
                    false);

                for (PSLocator loc : m_filter.getDependents())
                    m_paramValues.add(new Integer(loc.getId()));
            }

            m_isAddAND = true;
        }

        if (m_filter.getRelationshipId() > 0) {
            appendAndWhere();

            m_qryBuffer.append(R_TABLE).append(FN_RID).append(EQ);
            m_paramValues.add(new Integer(m_filter.getRelationshipId()));
            m_isAddAND = true;
        }

        appendPropertiesRestriction();
        appendCrossSiteLinksRestriction();
    }

    /**
     *
     * @param ids
     * @return
     */
    private boolean useOwnerRevision(Set<Integer> ids) {
        // make sure the config type/names are specified
        if (ids.isEmpty()) {
            return false; // don't know
        }

        // make sure the owner revision is specified
        PSLocator owner = m_filter.getOwner();

        if ((owner == null) || (owner.getRevision() == -1)) {
            return false; // don't know
        }

        // make sure all specified configs 'useOwnerRevision' == 'true'
        PSRelationshipConfig config;

        for (Integer id : ids) {
            config = m_configMap.get(id);

            if (config != null) {
                if (!config.useOwnerRevision()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Creates a query from the filter and executes the query.
     *
     * @sess the session object contains connection. It may not be
     *   <code>null</code>.
     *
     * @return a list of relationship object (from the query resultset),
     *   <code>null</code>, but may be empty.
     */
    public List<PSRelationshipData> executeQuery(Session sess) {
        if (sess == null) {
            throw new IllegalArgumentException("sess may not be null.");
        }

        Connection conn = null;
        SQLQuery stmt = null;
        Iterator<?> rs = null;

        try {
            // prepare query
            appendOwnerJoinCriteria();
            appendDependentJoinCriteria();
            appendRestCriterias();

            String qryString = m_qryBuffer.toString();

            // execute the query
            //conn = PSConnectionHelper.getDbConnection(null);
            stmt = sess.createSQLQuery(qryString);

            int i = 0;

            for (Object value : m_paramValues) {
                if (value instanceof Integer) {
                    stmt.setParameter(i, ((Integer) value).intValue());
                } else if (value instanceof Long) {
                    stmt.setLong(i, ((Long) value).longValue());
                } else if (value instanceof String) {
                    stmt.setString(i, (String) value);
                } else {
                    throw new IllegalStateException("Unsupported value type: " +
                        value.getClass().getName());
                }

                i++;
            }

            rs = stmt.list().iterator();

            List<PSRelationshipData> dataList = new ArrayList<>();
            PSRelationshipData rdata;

            // retrieve the list of relationships
            while (rs.hasNext()) {
                rdata = new PSRelationshipData();

                Object[] result = (Object[]) rs.next();
                rdata.setId((Integer) result[0]);
                rdata.setConfigId((Integer) result[1]);
                rdata.setOwnerId((Integer) result[2]);
                rdata.setOwnerRevision((Integer) result[3]);
                rdata.setDependentId((Integer) result[4]);
                rdata.setDependentRevision((Integer) result[5]);

                if (result[6] != null) {
                    rdata.setSlotId((Long) result[6]);
                }

                if (result[7] != null) {
                    rdata.setSortRank((Integer) result[7]);
                }

                if (result[8] != null) {
                    rdata.setVariantId((Integer) result[8]);
                }

                if (result[9] != null) {
                    rdata.setFolderId((Integer) result[9]);
                }

                if (result[10] != null) {
                    rdata.setSiteId((Long) result[10]);
                }

                rdata.setInlineRelationship((String) result[11]);
                rdata.setWidgetName((String) result[12]);

                dataList.add(rdata);
            }

            return dataList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // ignore, should not happen here.
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Appends {@link #AND} or {@link #m_where} before appending further
     * restrictions.
     */
    private void appendAndWhere() {
        if (m_isAddAND) {
            m_qryBuffer.append(AND);
        }
        else if (m_where != null) {
            m_qryBuffer.append(m_where);
        }
    }

    /**
     * Appends SELECT clause, join owner id (with CONTENTSTATUS table) and
     * possible join rid (with PSX_RELTAIOTNSHIPPROPERTIES table).
     *
     * @param qryBuffer the buffer used to append the query string, assumed
     *    not <code>null</code>.
     * @throws SQLException
     */
    private void appendSelectJoinOwnerId(StringBuffer qryBuffer)
        throws SQLException {
        qryBuffer.append(selectFromRandC());

        if (joinPropertiesTable()) {
            qryBuffer.append(fromPropertyTable());
        }

        qryBuffer.append(JOIN_OWNERID);

        if (joinPropertiesTable()) {
            qryBuffer.append(JOIN_RID).append(AND);
        }
    }

    /**
     * Appends SELECT clause, join dependent id (with CONTENTSTATUS table) and
     * possible join rid (with PSX_RELTAIOTNSHIPPROPERTIES table).
     *
     * @param qryBuffer the buffer used to append the query string, assumed
     *    not <code>null</code>.
     * @throws SQLException
     */
    private void appendSelectJoinDependentId(StringBuffer qryBuffer)
        throws SQLException {
        qryBuffer.append(selectFromRandC());

        if (joinPropertiesTable()) {
            qryBuffer.append(fromPropertyTable());
        }

        qryBuffer.append(JOIN_DEPENDENTID);

        if (joinPropertiesTable()) {
            qryBuffer.append(JOIN_RID).append(AND);
        }
    }

    /**
     * Filtering the retrieved custom properties against the properties
     * specified in the relationship filter. The additional filtering is done
     * in case insensitive.
     *
     * @param props the to be filtered customer properties. It may be empty
     *    or <code>null</code>.
     *
     * @return <code>true</code> if the supplied customer properties matches
     *    the filter criteria; otherwise return <code>false</code>.
     */
    public boolean filterCustomProperties(
        Collection<PSRelationshipPropertyData> props) {
        if (m_customProps.size() <= 1) { // no additional filtering needed.

            return true;
        }

        if ((props == null) || (props.size() < m_customProps.size())) {
            return false;
        }

        PSRelationshipPropertyData prop;

        for (Map.Entry<String, String> entry : m_customProps) {
            prop = getCustomProperty(entry.getKey(), props);

            if (prop == null) {
                return false;
            } else if (!entry.getValue().equalsIgnoreCase(prop.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets a property with the supplied name from the given property list.
     *
     * @param propName the to be retrieved property name, assumed not
     *    <code>null</code>
     * @param props a list of properties, assunmed not <code>null</code>.
     *
     * @return the property, may be <code>null</code> if cannot find one.
     */
    private PSRelationshipPropertyData getCustomProperty(String propName,
        Collection<PSRelationshipPropertyData> props) {
        for (PSRelationshipPropertyData prop : props) {
            if (propName.equalsIgnoreCase(prop.getName())) {
                return prop;
            }
        }

        return null;
    }

    /**
     * Determines whether to join the relationship properties table.
     *
     * @return <code>true</code> if join the properties table, there are
     *    customer properties specified in the filter; otherwise return
     *    <code>false</code>.
     */
    private boolean joinPropertiesTable() {
        return (!m_customProps.isEmpty());
    }

    /**
     * Append the criteria of limiting to cross site or folder relationships
     * if it is configured in the filter.
     */
    private void appendCrossSiteLinksRestriction() {
        if (!m_filter.getLimitToCrossSiteLinks()) {
            return;
        }

        appendAndWhere();
        m_qryBuffer.append("((" + FN_FOLDER_ID + " is not null) or (" +
            FN_SITE_ID + " is not null))");
        m_isAddAND = true;
    }

    /**
     * Append the property restriction (that is specified in the filter) to
     * the constructed HQL.
     */
    private void appendPropertiesRestriction() {
        // handle pre-defined user properties
        if (!m_pduProps.isEmpty()) {
            for (Map.Entry<String, String> prop : m_pduProps) {
                appendAndWhere();

                if (prop.getKey()
                            .equalsIgnoreCase(PSRelationshipConfig.PDU_FOLDERID)) {
                    m_qryBuffer.append(R_TABLE).append(FN_FOLDER_ID).append(EQ);
                    m_paramValues.add(new Integer(prop.getValue()));
                    m_isAddAND = true;
                } else if (prop.getKey()
                                   .equalsIgnoreCase(PSRelationshipConfig.PDU_INLINERELATIONSHIP)) {
                    m_qryBuffer.append(R_TABLE).append(FN_INLINELINK).append(EQ);
                    m_paramValues.add(prop.getValue());
                    m_isAddAND = true;
                } else if (prop.getKey()
                                   .equalsIgnoreCase(PSRelationshipConfig.PDU_SITEID)) {
                    m_qryBuffer.append(R_TABLE).append(FN_SITE_ID).append(EQ);
                    m_paramValues.add(new Long(prop.getValue()));
                    m_isAddAND = true;
                } else if (prop.getKey()
                                   .equalsIgnoreCase(PSRelationshipConfig.PDU_SLOTID)) {
                    m_qryBuffer.append(R_TABLE).append(FN_SLOT_ID).append(EQ);
                    m_paramValues.add(new Long(prop.getValue()));
                    m_isAddAND = true;
                } else if (prop.getKey()
                                   .equalsIgnoreCase(PSRelationshipConfig.PDU_SORTRANK)) {
                    m_qryBuffer.append(R_TABLE).append(FN_SORT_RANK).append(EQ);
                    m_paramValues.add(new Integer(prop.getValue()));
                    m_isAddAND = true;
                } else if (prop.getKey()
                                   .equalsIgnoreCase(PSRelationshipConfig.PDU_VARIANTID)) {
                    m_qryBuffer.append(R_TABLE).append(FN_VARIANT_ID).append(EQ);
                    m_paramValues.add(new Long(prop.getValue()));
                    m_isAddAND = true;
                }
            }
        }

        // handle custom properties. It is tricky to construct a query
        // when there are more than one custom properties. We will just
        // use one (the 1st one) custom property here, then perform additional
        // filtering afterwards
        if (!m_customProps.isEmpty()) {
            Map.Entry<String, String> prop = m_customProps.iterator().next();

            appendAndWhere();
            m_qryBuffer.append(P_TABLE).append(FN_PROPERTY_NAME).append(EQ);
            m_paramValues.add(prop.getKey());
            m_isAddAND = true;

            appendAndWhere();
            m_qryBuffer.append(P_TABLE).append(FN_PROPERTY_VALUE).append(EQ);
            m_paramValues.add(prop.getValue());
        }
    }

    /**
     * Gets the relationship name ids from the supplied filter. It looks for the
     * relationship names from {@link PSRelationshipFilter#getNames()},
     * {@link PSRelationshipFilter#getCategory()} and
     * {@link PSRelationshipFilter#getType()}.
     *
     * @param filter the filter, assumed not <code>null</code>.
     *
     * @return the ids. It may be empty if there is no names, category or type
     *    specified in the given filter.
     */
    @SuppressWarnings("unchecked")
    private Set<Integer> getRelationshipNameIds(PSRelationshipFilter filter) {
        if (filter.getNames().isEmpty() && (filter.getCategory() == null) &&
                (filter.getType() == null)) {
            return (Set<Integer>) Collections.EMPTY_SET;
        }

        HashSet<Integer> ids = new HashSet<>();
        Integer id;

        // get the names first
        for (String name : filter.getNames()) {
            id = m_nameMapToId.get(name);

            if (id != null) {
                ids.add(id);
            } else {
                ids.add(-1); // add an unknown 
            }
        }

        // get the names from the category
        if (filter.getCategory() != null) {
            Collection<PSRelationshipConfig> configs = m_configMap.values();

            for (PSRelationshipConfig config : configs) {
                if (config.getCategory().equalsIgnoreCase(filter.getCategory())) {
                    id = m_nameMapToId.get(config.getName());

                    if (id != null) {
                        ids.add(id);
                    }
                }
            }
        }

        // get names from the relationship type
        if (filter.getType() != null) {
            if (filter.getType().equals(PSRelationshipFilter.FILTER_TYPE_SYSTEM)) {
                ids.addAll(getConfigIdsFromType(
                        PSRelationshipFilter.FILTER_TYPE_SYSTEM));
            } else if (filter.getType()
                                 .equals(PSRelationshipFilter.FILTER_TYPE_USER)) {
                ids.addAll(getConfigIdsFromType(
                        PSRelationshipFilter.FILTER_TYPE_USER));

                // if there is such (user) type, then make up a fake id to purposely
                // force query return nothing.
                if (ids.isEmpty()) {
                    ids.add(-1);
                }
            }
        }

        return ids;
    }

    /**
     * Gets all configuration ids for a given relationship type.
     *
     * @param type the relationship type, it is either
     * {@link PSRelationshipFilter#FILTER_TYPE_SYSTEM} or
     * {@link PSRelationshipFilter#FILTER_TYPE_USER}
     *
     * @return the config ids, may be empty, but never <code>null</code>.
     */
    private Set<Integer> getConfigIdsFromType(String type) {
        Set<Integer> ids = new HashSet<>();

        Collection<PSRelationshipConfig> configs = m_configMap.values();

        for (PSRelationshipConfig config : configs) {
            if (config.getType().equals(type)) {
                Integer id = m_nameMapToId.get(config.getName());

                if (id != null) {
                    ids.add(id);
                }
            }
        }

        return ids;
    }

    /**
     * @return ', PSX_OBJECTRELATIONSHIPPROP p'
     * @throws SQLException
     */
    private String fromPropertyTable() throws SQLException {
        if (ms_fromP == null) {
            ms_fromP = ", " +
                qualifyTableName(IPSConstants.PSX_RELATIONSHIPPROPERTIES) +
                " p ";
        }

        return ms_fromP;
    }

    /**
     * @return {@link #SELECT_FROM_R} r, CONTENTSTATUS c
     * @throws SQLException
     */
    private String selectFromRandC() throws SQLException {
        if (ms_selectFromRandC == null) {
            ms_selectFromRandC = SELECT_FROM_R +
                qualifyTableName(IPSConstants.PSX_RELATIONSHIPS) + " r, " +
                qualifyTableName(IPSConstants.CONTENT_STATUS_TABLE) + " c";
        }

        return ms_selectFromRandC;
    }
}
