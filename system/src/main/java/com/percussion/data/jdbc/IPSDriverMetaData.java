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

package com.percussion.data.jdbc;

import java.sql.SQLException;


/**
 * The IPSDriverMetaData interface defines the mechanism by which driver
 * information can be cataloged. In particular, the names of servers
 * available through the driver.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSDriverMetaData {
   /**
    * Get the server names available through this driver.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>SERVER_NAME</B> String => server name</LI>
    * </OL>
    *
    * @return     a result set containing one server per row
    *
    * @exception  SQLException   if an error occurs accessing the servers
    */
   public java.sql.ResultSet getServers() throws SQLException;
}

