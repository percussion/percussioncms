/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.rdbms;

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
