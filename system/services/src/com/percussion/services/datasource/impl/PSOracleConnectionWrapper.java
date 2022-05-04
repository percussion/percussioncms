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

package com.percussion.services.datasource.impl;

import oracle.jdbc.OracleConnectionWrapper;
import oracle.jdbc.driver.OracleConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Wrapper;


public class PSOracleConnectionWrapper extends OracleConnectionWrapper {

    private final Connection delegate;

    public PSOracleConnectionWrapper(Wrapper delegate) throws SQLException {
        super(delegate.unwrap(OracleConnection.class));
        this.delegate = (Connection) delegate;
    }


    @Override
    public void close() throws SQLException {
        delegate.close();
    }
}