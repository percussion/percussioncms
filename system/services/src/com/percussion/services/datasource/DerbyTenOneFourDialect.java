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

