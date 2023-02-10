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

package com.percussion.delivery.rdbms;

import java.sql.Types;

import org.hibernate.dialect.SQLServer2012Dialect;

/**
 * Custom Dialect Class used to redefine native types for hibernate using MSSql Server 2008
 * 
 * @author federicoromanelli
 *
 */
public class PSUnicodeSQLServerDialect extends SQLServer2012Dialect
{
        /**
         * Initializes a new instance of the {@link SQLServer2012Dialect} class.
         * 
         * Note: the mapping for the values used in registerColumnType method are the same
         * as the ones described in the following file:
         * "\system\Tools\TableFactory\src\com\percussion\tablefactory\PSJdbcDataTypeMaps.xml"
         */
        public PSUnicodeSQLServerDialect() {
            super();
            
            // Register the native data types to use by Hibernate
            // Make sure to use the values included in PSJdbcDataTypeMaps.xml
            registerColumnType(Types.BIT, "BIT");
            registerColumnType(Types.TINYINT, "TINYINT");
            registerColumnType(Types.SMALLINT, "SMALLINT");            
            registerColumnType(Types.INTEGER, "INT");
            registerColumnType(Types.BIGINT, "BIGINT");
            registerColumnType(Types.FLOAT, "REAL");
            registerColumnType(Types.REAL, "REAL");
            registerColumnType(Types.DOUBLE, "FLOAT(53)");
            registerColumnType(Types.NUMERIC, "NUMERIC(18,0)");
            registerColumnType(Types.DECIMAL, "DECIMAL(18,0)");
            registerColumnType(Types.CHAR, "NCHAR($l)");
            registerColumnType(Types.VARCHAR, "NVARCHAR($l)");
            registerColumnType(Types.LONGVARCHAR, "NTEXT");
            registerColumnType(Types.DATE, "DATETIME");
            registerColumnType(Types.TIME, "DATETIME");
            registerColumnType(Types.TIMESTAMP, "DATETIME");
            registerColumnType(Types.BINARY, "BINARY");
            registerColumnType(Types.VARBINARY, "VARBINARY");
            registerColumnType(Types.LONGVARBINARY, "IMAGE");
            registerColumnType(Types.BLOB, "IMAGE");
            registerColumnType(Types.CLOB, "NTEXT");
        }
}
