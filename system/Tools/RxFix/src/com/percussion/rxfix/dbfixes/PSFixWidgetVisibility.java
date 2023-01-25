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

package com.percussion.rxfix.dbfixes;

import com.percussion.rxfix.IPSFix;
import com.percussion.rxfix.PSFixResult;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSConnectionHelper;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PSFixWidgetVisibility extends PSFixDBBase implements IPSFix {

    private boolean fixSuccess = false;
    private static final String LOGSTRING = "PSFixWidgetVisibility";
    /**
     * Ctor for the class
     *
     * @throws SQLException
     * @throws NamingException
     */
    public PSFixWidgetVisibility() throws NamingException, SQLException {
        super();
    }

    @Override
    public void fix(boolean preview)
            throws Exception {
        super.fix(preview);
        fixWidgetVisibility();
    }
    /**
     * Fixes Custom Widgets Type ContentType HIDEFROMMENU flag to mark them hidden from Devtools ContentExplorer New Menu
     *
     * @throws SQLException if any error occurs during DB access.
     */
    private void fixWidgetVisibility() throws Exception
    {

        logInfo(LOGSTRING,"Finding all widgets that needs to be updated");
        PSStringTemplate ms_allCustomWidgets = new PSStringTemplate("Select PREFIX, LABEL FROM "
                +" {schema}.PSX_WIDGETBUILDERDEFINITION");
        StringBuilder customWidgets = null;
        try (Connection dbConnection = PSConnectionHelper.getDbConnection()) {
            try (PreparedStatement st = PSPreparedStatement.getPreparedStatement(
                    dbConnection,
                    ms_allCustomWidgets.expand(m_defDict))) {

                ResultSet results = st.executeQuery();
                while (results.next()) {
                    String prefix = results.getString("PREFIX");
                    String label = results.getString("LABEL");
                    String widgetName = prefix+label;
                    widgetName = widgetName.toLowerCase();
                    logInfo(LOGSTRING, "Widget Found:  " + widgetName);
                    String query = "UPDATE  {schema}.CONTENTTYPES  SET HIDEFROMMENU = 1"
                            + " WHERE LOWER(CONTENTTYPENAME) = ? " + " and HIDEFROMMENU = 0";

                    logInfo(LOGSTRING, "Query:  " + query);
                    logInfo(LOGSTRING, "Param:  " + widgetName);
                    PSStringTemplate ms_updateContentType = new PSStringTemplate(query);
                    PreparedStatement ps2 = dbConnection.prepareStatement(ms_updateContentType.expand(m_defDict));
                    ps2.setString(1, widgetName);
                    ps2.executeUpdate();
                }

            }
            log(PSFixResult.Status.SUCCESS,null,"Updated Custom Widget ContentType HIDEFROMMENU flag Fixed");
            fixSuccess=true;
        }

    }
    public boolean removeStartupOnSuccess(){
        return fixSuccess;
    }

    @Override
    public String getOperation() {
        return "Fix Widgets Visibility";
    }
}
