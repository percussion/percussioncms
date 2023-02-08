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

package com.percussion.services.datasource;

import org.hibernate.dialect.DerbyTenSevenDialect;

import java.sql.Types;

public class DerbyTenOneFourDialect extends DerbyTenSevenDialect {

    public DerbyTenOneFourDialect(){
        super();

        //re-map nationalized types as they are unsupported by derby
        registerColumnType(Types.NCHAR,"char($l)");
        registerColumnType(Types.NVARCHAR,"varchar($l)");
        registerColumnType(Types.LONGNVARCHAR,"long varchar($l)");
        registerColumnType(Types.NCLOB,"clob($l)");
    }

    public String getCrossJoinSeparator() {
        return ", ";
    }

    /**
     * Does this dialect support Nationalized Types
     *
     * @return boolean
     */
    @Override
    public boolean supportsNationalizedTypes() {
        return false;
    }
}

