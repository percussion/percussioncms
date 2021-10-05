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

package com.percussion.deploy.server;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jndi.PSJndiObjectLocator;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;

public class PSServerJdbcDbmsDef extends PSJdbcDbmsDef {

    public PSServerJdbcDbmsDef()
    {

    }

    public PSServerJdbcDbmsDef(IPSConnectionInfo connInfo)
            throws NamingException, SQLException
    {
        m_connInfo = connInfo;
        m_connDetail = PSConnectionHelper.getConnectionDetail(connInfo);
        PSJndiObjectLocator loc = new PSJndiObjectLocator(
                m_connDetail.getDatasourceName());
        DataSource ds = loc.lookupDataSource();
        super.init(ds, m_connDetail.getDriver(), m_connDetail.getOrigin());
    }

    /**
     * Get connection info provided during construction.
     *
     * @return The info, may be <code>null</code>.
     * Use {@link #getConnectionDetail()} to determine if this object was
     * constructed with connection info.
     */
    public IPSConnectionInfo getConnectionInfo()
    {
        return m_connInfo;
    }

    /**
     * If constructed using an {@link IPSConnectionInfo} object, gets the
     * resolved connection details.
     *
     * @return The connection details, only <code>null</code> if not constructed
     * with an {@link IPSConnectionInfo} object.
     */
    public PSConnectionDetail getConnectionDetail()
    {
        return m_connDetail;
    }


    /**
     * Connection detail, not <code>null</code> if constructed with an
     * <code>IPSConnectionInfo</code> object.
     */
    private PSConnectionDetail m_connDetail = null;


    /**
     * Connection info if supplied during construction, may be <code>null</code>.
     */
    private IPSConnectionInfo m_connInfo = null;


}
