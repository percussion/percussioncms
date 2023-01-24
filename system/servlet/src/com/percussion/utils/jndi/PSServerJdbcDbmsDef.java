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

package com.percussion.utils.jndi;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

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
