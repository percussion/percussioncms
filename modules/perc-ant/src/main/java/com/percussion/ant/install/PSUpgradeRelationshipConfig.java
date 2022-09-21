/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.ant.install;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSExceptionUtils;
import com.percussion.install.IPSUpgradeModule;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSProperties;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PSUpgradeRelationshipConfig extends PSAction {

    Logger logger = LogManager.getLogger(PSUpgradeSiteConfig.class);

    /**
     * The name of the configuration table.
     */
    private final static String CONFIG_TABLE = "PSX_RXCONFIGURATIONS";


    // see base class
    @Override
    public void execute()
    {
        String strRootDir = null;
        PSProperties props = null;
        PSJdbcDbmsDef dbmsDef = null;

        try
        {
            strRootDir = getRootDir();

            if (!(strRootDir.endsWith(File.separator)))
                strRootDir += File.separator;

            props = new PSProperties(strRootDir + IPSUpgradeModule.REPOSITORY_PROPFILEPATH);
            props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
            dbmsDef = new PSJdbcDbmsDef(props);
            try(Connection conn = RxLogTables.createConnection(props)) {

                if (conn == null) {
                    PSLogger.logError(
                            "PSUpgradeRelationshipConfig#execute : Could not establish connection with database");
                    PSLogger.logError(
                            "PSUpgradeRelationshipConfig#execute : Table modifications aborted");

                    return;
                }

                updateRelationshipTypeConfig(conn, dbmsDef);

            }
        }
        catch(Exception e)
        {
            PSLogger.logError("PSUpgradeRelationshipConfig : " + e.getMessage());
        }
    }

    private void updateRelationshipTypeConfig(
            final Connection conn, final PSJdbcDbmsDef dbmsDef)
    {

        PSLogger.logInfo("Adding missing Relationship configs ");

        String qualTableName = PSSqlHelper.qualifyTableName(CONFIG_TABLE,
                dbmsDef.getDataBase(), dbmsDef.getSchema(),
                dbmsDef.getDriver());

        String configXml = null;

        String configXmlSql = "SELECT CONFIGURATION FROM " + qualTableName + " WHERE NAME='relationships'";

        try (PreparedStatement stmt = conn.prepareStatement(configXmlSql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs != null && rs.next()){

                configXml = rs.getString(1);
            }

        } catch (SQLException e) {
            PSLogger.logError("PSUpgradeRelationshipConfig : Failed ERROR: " + PSExceptionUtils.getMessageForLog(e));
        }

        if(StringUtils.isEmpty(configXml)){
            PSLogger.logInfo("PSUpgradeRelationshipConfig : configxml for relationships is null. Thus nothing to update");
            return;
        }

        boolean changed = false;

        if(!configXml.contains(PSRelationshipConfig.TYPE_RECYCLED_CONTENT)){
            configXml = configXml.replace("</PSXRelationshipConfigSet>",getRecycleContentConfig() + "\n</PSXRelationshipConfigSet>");
            changed = true;
        }

        if(!configXml.contains(PSRelationshipConfig.TYPE_LOCAL_CONTENT)){
            configXml = configXml.replace("</PSXRelationshipConfigSet>",getLocalContentConfig() + "\n</PSXRelationshipConfigSet>");
            changed = true;
        }

        if(!configXml.contains(PSRelationshipConfig.TYPE_WIDGET_ASSEMBLY)){
            configXml = configXml.replace("</PSXRelationshipConfigSet>",getWidgetAssemblyConfig() + "\n</PSXRelationshipConfigSet>");
            changed = true;
        }

        if(!configXml.contains(PSRelationshipConfig.TYPE_WIDGET_CONTENT)){
            configXml = configXml.replace("</PSXRelationshipConfigSet>",getWidgetContentConfig() + "\n</PSXRelationshipConfigSet>");
            changed = true;
        }

        if(changed) {

            String updateConfigXML = "UPDATE " + qualTableName + " SET CONFIGURATION='" + configXml + "' WHERE NAME='relationships'";

            try {
                String driver = dbmsDef.getDriver();
                if (PSSqlHelper.isOracle(driver) ||
                        driver.equals(PSJdbcUtils.DB2))
                    conn.setAutoCommit(false);

                try (PreparedStatement stmt = conn.prepareStatement(updateConfigXML)) {
                    stmt.executeUpdate();
                }
                if (PSSqlHelper.isOracle(driver) ||
                        driver.equals(PSJdbcUtils.DB2)) {
                    conn.setAutoCommit(true);
                    conn.commit();
                }
                PSLogger.logInfo("PSUpgradeRelationshipConfig : Updated Relationship Config");
            } catch (SQLException e) {
                PSLogger.logError("PSUpgradeRelationshipConfig : Update Failed ERROR: " + PSExceptionUtils.getMessageForLog(e));
            }
        }else{
            PSLogger.logInfo("PSUpgradeRelationshipConfig : Nothing to update. It's up to date");
        }

    }

    private String getRecycleContentConfig(){
        return "<PSXRelationshipConfig category=\"rs_recycled\" id=\"8\" label=\"Recycled Content\" name=\"RecycledContent\" type=\"system\">\n" +
                "    <PSXCloneOverrideFieldList id=\"0\"/>\n" +
                "    <PSXPropertySet>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_allowcloning\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies whether or not this relationship can be cloned.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_islocaldependency\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Specifies if this relationship must be packaged and deployed with the owner. If\n" +
                "                checked, the relationship must be deployed, if not, it is optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_skippromotion\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies if this relationship should be skipped when an item is promoted.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_usecommunityfilter\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies if this relationship will be filtered by community id.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_usedependentrevision\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Defines whether or not to use the dependent revision as part of the dependent\n" +
                "                locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_useownerrevision\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_useserverid\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER\n" +
                "                will be used, otherwise the current user will be used.</Description>\n" +
                "      </PSXProperty>\n" +
                "    </PSXPropertySet>\n" +
                "    <ProcessChecks>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_cloneshallow\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>2</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_clonedeep\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"0\">\n" +
                "              <variable>\n" +
                "                <PSXOriginatingRelationshipProperty id=\"0\">\n" +
                "                  <name>name</name>\n" +
                "                </PSXOriginatingRelationshipProperty>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>Translation-Mandatory</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "    </ProcessChecks>\n" +
                "    <Explanation/>\n" +
                "  </PSXRelationshipConfig>";
    }

    private String getWidgetContentConfig(){
        return "<PSXRelationshipConfig category=\"rs_widget\" id=\"115\" label=\"Widget Content\" name=\"Widget-Content\" type=\"system\">\n" +
                "    <PSXCloneOverrideFieldList id=\"0\"/>\n" +
                "    <PSXPropertySet>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_allowcloning\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies whether or not this relationship can be cloned.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_islocaldependency\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies if this relationship must be packaged and deployed with the owner.  If checked, the relationship must be deployed, if not, it is optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_skippromotion\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Specifies if this relationship should be skipped when an item is promoted.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_usedependentrevision\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_useownerrevision\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_useserverid\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>\n" +
                "      </PSXProperty>\n" +
                "    </PSXPropertySet>\n" +
                "    <UserPropertySet>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_inlinerelationship\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>Marks a relationship as an inline link relationship. Its value is the field name and possibly the row id. If this property is missing (null or empty), the relationship is not treated as an inline link relationship.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_folderid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The folder id used, optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_siteid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The site id used, optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_variantid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The variant used.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_slotid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The slot used.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_sortrank\">\n" +
                "        <Value type=\"String\">0</Value>\n" +
                "        <Description>The sorting rank.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_widgetname\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The widget name used.</Description>\n" +
                "      </PSXProperty>\n" +
                "    </UserPropertySet>\n" +
                "    <ProcessChecks>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_cloneshallow\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_clonedeep\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>2</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "    </ProcessChecks>\n" +
                "    <Explanation>The relationship configuration for storing the relationship between widgets and assets.</Explanation>\n" +
                "  </PSXRelationshipConfig>";
    }

    private String getWidgetAssemblyConfig(){
        return "<PSXRelationshipConfig category=\"rs_widget\" id=\"113\" label=\"Widget Assembly\" name=\"Widget-Assembly\" type=\"system\">\n" +
                "    <PSXCloneOverrideFieldList id=\"0\"/>\n" +
                "    <PSXPropertySet>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_allowcloning\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies whether or not this relationship can be cloned.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_islocaldependency\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies if this relationship must be packaged and deployed with the owner.  If checked, the relationship must be deployed, if not, it is optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_skippromotion\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Specifies if this relationship should be skipped when an item is promoted.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_usedependentrevision\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_useownerrevision\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_useserverid\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>\n" +
                "      </PSXProperty>\n" +
                "    </PSXPropertySet>\n" +
                "    <UserPropertySet>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_inlinerelationship\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>Marks a relationship as an inline link relationship. Its value is the field name and possibly the row id. If this property is missing (null or empty), the relationship is not treated as an inline link relationship.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_folderid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The folder id used, optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_siteid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The site id used, optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_variantid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The variant used.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_slotid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The slot used.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_sortrank\">\n" +
                "        <Value type=\"String\">0</Value>\n" +
                "        <Description>The sorting rank.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_widgetname\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The widget name used.</Description>\n" +
                "      </PSXProperty>\n" +
                "    </UserPropertySet>\n" +
                "    <ProcessChecks>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_cloneshallow\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_clonedeep\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>2</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "    </ProcessChecks>\n" +
                "    <Explanation>The relatin ship configuration for storing the widget relationships with the other objects like pages and templates.</Explanation>\n" +
                "  </PSXRelationshipConfig>";
    }

    private String getLocalContentConfig(){
        return " <PSXRelationshipConfig category=\"rs_activeassembly\" id=\"111\" label=\"Local Content\" name=\"LocalContent\" type=\"system\">\n" +
                "    <PSXCloneOverrideFieldList id=\"0\"/>\n" +
                "    <PSXPropertySet>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_allowcloning\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies whether or not this relationship can be cloned.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_islocaldependency\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies if this relationship must be packaged and deployed with the owner.  If checked, the relationship must be deployed, if not, it is optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_skippromotion\">\n" +
                "        <Value type=\"Boolean\">no</Value>\n" +
                "        <Description>Specifies if this relationship should be skipped when an item is promoted.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_usedependentrevision\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Defines whether or not to use the dependent revision as part of the dependent locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_useownerrevision\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Defines whether or not to use the owner revision as part of the owner locator.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"no\" name=\"rs_useserverid\">\n" +
                "        <Value type=\"Boolean\">yes</Value>\n" +
                "        <Description>Specifies the user to be used while executing effects. If checked the user RXSERVER will be used, otherwise the current user will be used.</Description>\n" +
                "      </PSXProperty>\n" +
                "    </PSXPropertySet>\n" +
                "    <UserPropertySet>\n" +
                "      <PSXProperty locked=\"yes\" name=\"rs_inlinerelationship\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>Marks a relationship as an inline link relationship. Its value is the field name and possibly the row id. If this property is missing (null or empty), the relationship is not treated as an inline link relationship.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_folderid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The folder id used, optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_siteid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The site id used, optional.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_variantid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The variant used.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_slotid\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The slot used.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_sortrank\">\n" +
                "        <Value type=\"String\">1</Value>\n" +
                "        <Description>The sorting rank.</Description>\n" +
                "      </PSXProperty>\n" +
                "      <PSXProperty locked=\"yes\" name=\"sys_widgetname\">\n" +
                "        <Value type=\"String\"/>\n" +
                "        <Description>The widget name used.</Description>\n" +
                "      </PSXProperty>\n" +
                "    </UserPropertySet>\n" +
                "    <ProcessChecks>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_cloneshallow\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>2</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "      <PSXProcessCheck context=\"relationship\" name=\"rs_clonedeep\" sequence=\"1\">\n" +
                "        <Conditions>\n" +
                "          <PSXRule boolean=\"and\">\n" +
                "            <PSXConditional id=\"1\">\n" +
                "              <variable>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>1</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </variable>\n" +
                "              <operator>=</operator>\n" +
                "              <value>\n" +
                "                <PSXTextLiteral id=\"0\">\n" +
                "                  <text>2</text>\n" +
                "                </PSXTextLiteral>\n" +
                "              </value>\n" +
                "              <boolean>AND</boolean>\n" +
                "            </PSXConditional>\n" +
                "          </PSXRule>\n" +
                "        </Conditions>\n" +
                "      </PSXProcessCheck>\n" +
                "    </ProcessChecks>\n" +
                "    <Explanation/>\n" +
                "  </PSXRelationshipConfig>";
    }
}
