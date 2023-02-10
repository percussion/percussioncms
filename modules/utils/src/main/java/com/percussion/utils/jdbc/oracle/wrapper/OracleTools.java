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

package com.percussion.utils.jdbc.oracle.wrapper;

import com.percussion.util.PSSqlHelper;
import oracle.jdbc.OraclePreparedStatement;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;

import static com.percussion.util.PSSqlHelper.NUMBER_TABLE_DESCRIPTOR_NAME;

public class OracleTools {
    /**
     * Set the data for the specified collection for "oracle:thin" driver
     * using the <code>OraclePreparedStatement.setARRAY</code> method. This
     * method throws <code>IllegalArgumentException</code> for drivers other
     * than Oracle or if any object in the collection is not a number.
     *
     * @param stmt The PreparedStatement, may not be <code>null</code>.
     * @param bindStart The index of the parameter to bind, should be greater
     * than <code>0</code>
     * @param coll The colelction containing the values to bind.  May not be
     * <code>null</code>.
     * @param dataType The jdbc datatype, must be
     * <code>java.sql.Types.Array</code>
     *
     * @throws IllegalArgumentException if any parameter is invalid or if the
     * driver is not "oracle:thin", or if the collection contains object which
     * are not instances of <code>java.lang.Number</code> or whose
     * <code>toString()</code> does not return a number.
     *
     * @throws SQLException if database error occurs.
     */
    public static void setDataFromCollection(PreparedStatement stmt,
                                      int bindStart, Collection coll, int dataType) throws SQLException
    {
        if (stmt == null)
            throw new IllegalArgumentException("stmt may not be null");
        if (bindStart < 1)
            throw new IllegalArgumentException("bindStart must be greater than 0");
        if (coll == null)
            throw new IllegalArgumentException("coll may not be null");
        if (dataType != Types.ARRAY)
            throw new IllegalArgumentException("dataType must be Types.ARRAY");

        // has to use the original (Oracle) object to handle to collection data
        OraclePreparedStatement oraStmt = (OraclePreparedStatement) PSSqlHelper.getOracleStatement(stmt);
        if (oraStmt == null)
        {
            throw new IllegalArgumentException(
                    "expected OraclePreparedStatement but got "
                            + stmt.getClass().getName());
        }

        if (coll.isEmpty())
        {
            // if the collection is empty, then there is no way to know which
            // descriptor to use - numeric or string
            stmt.setNull(bindStart, dataType);
        }
        else
        {
            Number[] arr = new Number[coll.size()];
            Iterator it = coll.iterator();
            int i = 0;
            try
            {
                while (it.hasNext())
                {
                    Object obj = it.next();
                    if (obj != null)
                    {
                        if (obj instanceof Number)
                        {
                            arr[i] = (Number)obj;
                        }
                        else
                        {
                            String str = obj.toString();
                            if (str.indexOf(".") == -1)
                            {
                                arr[i] = new Long(str);
                            }
                            else
                            {
                                arr[i] = new Float(str);
                            }
                        }
                        i++;
                    }
                }
            }
            catch (NumberFormatException nfe)
            {
                throw new IllegalArgumentException(
                        "Collection contains non-numeric object. " +
                                nfe.getLocalizedMessage());
            }

            ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
                    NUMBER_TABLE_DESCRIPTOR_NAME, oraStmt.getConnection());
            ARRAY oraArr = new ARRAY(desc, oraStmt.getConnection(), arr);
            oraStmt.setARRAY(bindStart, oraArr);
        }
    }
}
