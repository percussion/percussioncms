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
package com.percussion.rxfix.dbfixes;

import com.percussion.cms.IPSConstants;
import com.percussion.rxfix.IPSFix;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.impl.PSGuidManager;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Fix the next number table entries. Do this by looping through the list of
 * entries and queries, checking that the current entry in the next number table
 * is larger than the calculated value.
 */
public class PSFixNextNumberTable extends PSFixDBBase implements IPSFix {
    /**
     * Constructor
     *
     * @throws SQLException
     * @throws NamingException
     */
    public PSFixNextNumberTable() throws NamingException, SQLException {
        super();
    }

    /**
     * The list of next numbers to update the island of misfit toys. The first element for each entry is
     * the name of the primary key column. The next element is the table. The
     * last is the next number table primary key value. Note the use of comments
     * to prevent the code formatter from wrapping the entire block.
     */
    private final String ms_nextNumberInfo[] =
            {
                    "ACTIONID", "RXMENUACTION", "actionid", //
                    "ID", "PSX_ATTRIBUTE_NAMES", "Attribute", //
                    "ID", "PSX_ATTRIBUTE_VALUES", "AttributeValue", //
                    "COMMUNITYID", "RXCOMMUNITY", "communityid", //
                    "COMPONENTID", "RXSYSCOMPONENT", "componentid", //
                    "STATUS_ID", "PSX_CONFIG_STATUS", "CONFIG_STATUS_ID", //
                    "CONTENTID", "CONTENTSTATUS", "CONTENT", //
                    "CONTENTLISTID", "RXCONTENTLIST", "contentlistid", //
                    "CONTENTSTATUSHISTORYID", "CONTENTSTATUSHISTORY", "CONTENTSTATUSHISTORY", //
                    "CONTENTTYPEID", "CONTENTTYPES", "CONTENTTYPES", //
                    "CONTEXTID", "RXCONTEXT", "contextid", //
                    "DISPLAYID", "PSX_DISPLAYFORMATCOLUMNS", "displaycolumnid", //
                    "DISPLAYID", "PSX_DISPLAYFORMATS", "displayformatid", //
                    "ARCHIVE_LOG_ID", "DPL_ARCHIVE_LOG_SUMMARY", "DPL_ARCHIVE_LOG_SUMMARY", //
                    "LOG_SUMMARY_ID", "DPL_LOG_SUMMARY", "DPL_LOG_SUMMARY", //
                    "EDITIONCLISTID", "RXEDITIONCLIST", "editionclistid", //
                    "EDITIONID", "RXEDITION", "editionid", //
                    "QUEUEID", "PSX_SEARCHINDEXQUEUE", "FTSIndexQueue", //
                    "ID","PSX_RECENT","HIB_PSX_RECENT", //
                    "LOCALEID", "RXLOCALE", "localeid", //
                    "log_seq_id", "pslog", "log_seq_id", //
                    "SITEID", "RXSITES", "newsiteid", //
                    "PROPERTYID", "PSX_PERSISTEDPROPERTYMETA", "PersistedProperty", //
                    "PKG_DEPENDENCY_ID", "PSX_PKG_DEPENDENCY", "PKG_DEPENDENCY_ID", //
                    "PROPERTYID", "RXSYSCOMPONENTPROPERTY", "propertyid", //
                    "TASK_ID","PSX_EDITION_TASK","PSX_EDITION_TASK", //
                    "LOGENTRYID", "PSX_IMPORTLOGENTRY", "PSX_IMPORTLOGENTRY", //
                    "LINKID", "PSX_MANAGEDLINK", "PSX_MANAGEDLINK", //
                    "SYSID", "PSX_OBJECTACL", "PSX_OBJECTACL", //
                    "SYSID", "PSX_PROPERTIES", "PSX_PROPERTIES", //
                    "CONFIG_ID", "PSX_RELATIONSHIPCONFIGNAME", "PSX_RELATIONSHIPCONFIGNAME", //
                    "ID", "PSX_SCH_NOTIF_TEMPLATE", "PSX_SCH_NOTIF_TEMPLATE", //
                    "LOG_ID","PSX_SCH_TASK_LOG","PSX_SCH_TASK", //
                    "SUMMARYID", "PSX_SITEIMPORTSUMMARY", "PSX_SITEIMPORTSUMMARY", //
                    "WIDGETBUILDERDEFINITIONID","PSX_WIDGETBUILDERDEFINITION","PSX_WIDGETBUILDERDEFINITIONID", //
                    "PUBSERVERID", "PSX_PUBSERVER", "pubserver", //
                    "SERVERPROPERTYID", "PSX_PUBSERVER_PROPERTIES", "pubserver_property", //
                    /* "", "", "PUBLICATIONS", // */
                    "ID", "PSX_ROLES", "Role", //
                    "ROLEID", "ROLES", "ROLES", //
                    "EDITIONCLISTID", "RXEDITIONCLIST", "RXEDITIONCLIST", //
                    "LOOKUPID", "RXLOOKUP", "RXLOOKUP", //
                    "STATUS_ID", "PSX_PUBLICATION_STATUS", "PUBLICATIONS", //
                    "RID", IPSConstants.PSX_RELATIONSHIPS, "RXRELATEDCONTENT", //
                    "SCHEMEID", "RXLOCATIONSCHEME", "schemeid", //
                    "SCHEMEPARAMID", "RXLOCATIONSCHEMEPARAMS", "schemeparamid", //
                    "SEARCHID", "PSX_SEARCHFIELDS", "searchfieldid", //
                    "SEARCHID", "PSX_SEARCHES", "searchid", //
                    "SLOTID", "RXSLOTTYPE", "slotid", //
                    "SORT_RANK",IPSConstants.PSX_RELATIONSHIPS,"SORT_RANK", //
                    "ID", "PSX_SUBJECTS", "Subject", //
                    "PROPERTYID", "RXASSEMBLERPROPERTIES", "variableid", //
                    "TEMPLATE_ID", "PSX_TEMPLATE", "variantid" //
            };

    /*
     * Templates for the SQL Statements used in this fix routine
     */
    private static PSStringTemplate ms_selectTemplate = new PSStringTemplate(
            "select NEXTNR from {schema}.NEXTNUMBER where KEYNAME = ?");

    private static PSStringTemplate ms_updateTemplate = new PSStringTemplate(
            "update {schema}.NEXTNUMBER set NEXTNR = ? where KEYNAME = ?");

    private static PSStringTemplate ms_insertTemplate = new PSStringTemplate(
            "insert into {schema}.NEXTNUMBER values (?, ?)");

    @Override
    public void fix(boolean preview) throws Exception {
        super.fix(preview);
        Connection c = PSConnectionHelper.getDbConnection();
        PSConnectionDetail details = PSConnectionHelper.getConnectionDetail();
        String table = null;
        PreparedStatement stcur = null;
        PreparedStatement upnext = null;
        PreparedStatement innext = null;
        ResultSet rs = null;
        PreparedStatement stmax = null;
        IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
        try {
            stcur = PSPreparedStatement.getPreparedStatement(
                    c, ms_selectTemplate.expand(m_defDict));
            upnext = PSPreparedStatement
                    .getPreparedStatement(c, ms_updateTemplate.expand(m_defDict));
            innext = PSPreparedStatement
                    .getPreparedStatement(c, ms_insertTemplate.expand(m_defDict));

            for (int i = 0; i < ms_nextNumberInfo.length; i += 3) {
                String keycol = ms_nextNumberInfo[i];
                table = details.getOrigin() + "." + ms_nextNumberInfo[i + 1];
                String nnkey = ms_nextNumberInfo[i + 2];

                logDebug(null, "Checking next number key " + nnkey);

                stmax = PSPreparedStatement.getPreparedStatement(
                        c, "select max(" + keycol + ") from " + table);
                rs = stmax.executeQuery();
                if (rs.next()) {
                    int maximum = rs.getInt(1);
                    // Now get the key's current value
                    int expectedNextNumber = maximum+1;
                    int currentNextNumber = preview ? guidMgr.peekNextNumber(nnkey) : guidMgr.fixNextNumber(nnkey,expectedNextNumber);
                    if (currentNextNumber<expectedNextNumber)
                    {
                        String message = "Next number value " + currentNextNumber
                              + " was less " + "than the calculated next number "
                              + expectedNextNumber;
                        
                        if(!preview)
                           logSuccess(nnkey, "Fixed "+message);
                        else{
                           logPreview(nnkey,message);
                            }
                    } else if (currentNextNumber>expectedNextNumber)
                    {
                        // This can be normal behavior if an id is requested but not yet saved
                        // We will not decrease the next number but will save as a new block
                        logDebug(nnkey, "Next number value " + currentNextNumber
                                + " was more " + "than the calculated next number "
                                + expectedNextNumber);
                    } else 
                    {
                       logDebug(nnkey, "Next number value " + currentNextNumber
                             + " was equal to  " + "the calculated next number "
                             + expectedNextNumber +" number updated");
                    }

                } else {
                    logWarn(nnkey,
                            "Unable to calculate maximum value for key " +
                                    "that is associated with table "
                                    + table);
                }
                rs.close();
                stmax.close();

            }

        } catch (Exception e) {
            logFailure(table, "Problem updating: " + e.getLocalizedMessage());
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (SQLException e) {/* ignored */}
            if (stcur != null) try {
                stcur.close();
            } catch (SQLException e) {/* ignored */}
            if (upnext != null) try {
                upnext.close();
            } catch (SQLException e) {/* ignored */}
            if (innext != null) try {
                innext.close();
            } catch (SQLException e) {/* ignored */}
            if (stmax != null) try {
                stmax.close();
            } catch (SQLException e) {/* ignored */}
            if (c != null) try {
                c.close();
            } catch (SQLException e) {/* ignored */}

        }
    }

    @Override
    public String getOperation() {
        return "Fix the next number";
    }
}
