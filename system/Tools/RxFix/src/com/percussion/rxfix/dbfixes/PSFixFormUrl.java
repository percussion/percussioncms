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

public class PSFixFormUrl extends PSFixDBBase implements IPSFix {

    private boolean fixSuccess = false;
    /**
     * Ctor for the class
     *
     * @throws SQLException
     * @throws NamingException
     */
    public PSFixFormUrl() throws NamingException, SQLException {
        super();
    }
    @Override
    public void fix(boolean preview)
            throws Exception {
        super.fix(preview);
        fixContentFormUrl();
    }
    /**
     * Fixes form url by changing all "/perc-form-processor/forms" to
     * "/perc-form-processor/form/" in the RENDEDERDFORM column of the
     * CT_PERCFORMASSET table.
     *
     * @throws SQLException if any error occurs during DB access.
     */
    private void fixContentFormUrl() throws Exception
    {

        logInfo(null,"Finding all forms");
        PSStringTemplate ms_allForms = new PSStringTemplate("Select CONTENTID, REVISIONID, NAME, RENDEREDFORM FROM "
                +" {schema}.CT_PERCFORMASSET"
                + " WHERE RENDEREDFORM LIKE '%\"/perc-form-processor/form%' ");
        try (Connection dbConnection = PSConnectionHelper.getDbConnection()) {
            try (PreparedStatement st = PSPreparedStatement.getPreparedStatement(
                    dbConnection,
                    ms_allForms.expand(m_defDict))) {

                ResultSet results = st.executeQuery();
                while (results.next()) {
                    int contentid = results.getInt("CONTENTID");
                    int revisionid = results.getInt("REVISIONID");
                    String name = results.getString("NAME");
                    String renderedForm = results.getString("RENDEREDFORM");

                    logInfo(null, "Rendered form: " + renderedForm);

                    // Change all "/perc-form-processor/forms" to
                    // "/perc-form-processor/form/"
                    renderedForm = renderedForm.replaceAll(
                            "action=\"/perc-form-processor/forms/\"",
                            "action=\"/perc-form-processor/forms/form/\"");

                    renderedForm = renderedForm.replaceAll(
                            "action=\"/perc-form-processor/form/\"",
                            "action=\"/perc-form-processor/forms/form/\"");

                    logInfo(null, "updating 'RENDEREDFORM' column in 'CT_PERCFORMASSET' for contentid = "
                            + contentid
                            + " and revision = "
                            + revisionid
                            + " and name = "
                            + name);

                    logInfo(null, "Rendered form after upgrade: " + renderedForm);

                    PSStringTemplate ms_updateForms = new PSStringTemplate("UPDATE  {schema}.CT_PERCFORMASSET  SET RENDEREDFORM = ?"
                            + " WHERE CONTENTID = ?" + " and REVISIONID = ?");
                    PreparedStatement ps2 = dbConnection.prepareStatement(ms_updateForms.expand(m_defDict));
                    ps2.setString(1, renderedForm);
                    ps2.setInt(2, contentid);
                    ps2.setInt(3, revisionid);
                    ps2.executeUpdate();
                }
            }
            log(PSFixResult.Status.SUCCESS,null,"Forms Urls Fixed");
            fixSuccess=true;
        }

    }
    public boolean removeStartupOnSuccess(){
        return fixSuccess;
    }

    @Override
    public String getOperation() {
        return "Fix FormUrls";
    }
}
