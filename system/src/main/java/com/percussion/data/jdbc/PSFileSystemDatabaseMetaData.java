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

package com.percussion.data.jdbc;

import com.percussion.data.PSResultSet;
import com.percussion.data.PSResultSetColumnMetaData;
import com.percussion.data.PSResultSetMetaData;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSFileFilter;
import com.percussion.utils.tools.PSPatternMatcher;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;


/**
 * The PSFileSystemDatabaseMetaData class provides access to database
 * meta data for the File System driver.
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSFileSystemDatabaseMetaData implements DatabaseMetaData {

   /**
    * @author   chadloder
    *
    * @version 1.7 1999/04/21
    *
    * Constructs a new, empty meta data object.
    *
    */
   PSFileSystemDatabaseMetaData()
   {}

   /**
    * @author   chadloder
    *
    * @version 1.7 1999/04/21
    *
    * Constructs a new meta data object owned by the given connection.
    *
    * @param   con   The connection.
    *
    */
   PSFileSystemDatabaseMetaData(PSFileSystemConnection con)
   {
      m_conn = con;
   }

   /**
    * Can all the procedures returned by getProcedures be called by the
    * current user? Procedures are not currently supported, thus
    * <code>false</code> is always returned.
    *
    * @return   always returns <code>false</code>
    * @throws   SQLException   if an error occurs
    */
   public boolean allProceduresAreCallable()
      throws SQLException
   {
      return false;
   }

   /**sor
    * Can all the tables returned by getTables be SELECTed by the
    * current user? This always returns false as file permissions may
    * prevent reading.
    *
    * @return   always returns <code>false</code>
    * @throws   SQLException   if an error occurs
    */
   public boolean allTablesAreSelectable()
      throws SQLException
   {
      return false;
   }

   /**
    * What's the URL of this connection?
    *
    * @return   the URL of this connection
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getURL()
      throws SQLException
   {
      return m_conn.getURL();
   }

   /**
    * What's our user name as known to the connection?
    *
    * @return   our connection user name
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getUserName()
      throws SQLException
   {
      return m_conn.getUserName();
   }

   /**
    * Is the database in read-only mode?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean isReadOnly()
      throws SQLException
   {
      return m_conn.isReadOnly();
   }

   /**
    * Are NULL values sorted high? This driver treats NULL values as being
    * empty, thus they are always sorted low.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean nullsAreSortedHigh()
      throws SQLException
   {
      return false;
   }

   /**
    * Are NULL values sorted low? This driver treats NULL values as being
    * empty, thus they are always sorted low.
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean nullsAreSortedLow()
      throws SQLException
   {
      return true;
   }

   /**
    * Are NULL values sorted at the start regardless of sort order? This
    * driver treats them as low values, and they are sorted accordingly.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean nullsAreSortedAtStart()
      throws SQLException
   {
      return false;
   }

   /**
    * Are NULL values sorted at the end regardless of sort order? This
    * driver treats them as low values, and they are sorted accordingly.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean nullsAreSortedAtEnd()
      throws SQLException
   {
      return false;
   }

   /**
    * What's the name of this database product?
    *
    * @return   database product name
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getDatabaseProductName()
      throws SQLException
   {
      return m_conn.getDriver().getName();
   }

   /**
    * What's the version of this database product?
    *
    * @return   database version
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getDatabaseProductVersion()
      throws SQLException
   {
   /* concat PSFileSystemDriver's getMajorVersion + "." + getMinorVersion
    * (convert method return values to String)
    */
      return m_conn.getDriver().getMajorVersion() + "." +
         m_conn.getDriver().getMajorVersion();
   }

   /**
    * What's the name of this JDBC driver?
    *
    * @return   JDBC driver name
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getDriverName()
      throws SQLException
   {
      return m_conn.getDriver().getName();
   }

   /**
    * What's the version of this JDBC driver?
    *
    * @return   JDBC driver version
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getDriverVersion()
      throws SQLException
   {
   /* concat PSFileSystemDriver's getMajorVersion + "." + getMinorVersion
    * (convert method return values to String)
    */
      return "" + m_conn.getDriver().getMajorVersion()
         + "." + m_conn.getDriver().getMinorVersion();
   }

   /**
    * What's the JDBC driver's major version number?
    *
    * @return   JDBC driver major version
    */
   public int getDriverMajorVersion()
   {
      // return PSFileSystemDriver's getMajorVersion
      return m_conn.getDriver().getMajorVersion();
   }

   /**
    * What's the JDBC driver's minor version number?
    *
    * @return   JDBC driver minor version
    */
   public int getDriverMinorVersion()
   {
      // return PSFileSystemDriver's getMinorVersion
      return m_conn.getDriver().getMinorVersion();
   }

   /**
    * Does the database store tables in a local file? At this time,
    * only the local file system is supported, thus <code>true</code> is
    * always returned
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean usesLocalFiles()
      throws SQLException
   {
      return true;
   }

   /**
    * Does the database use a file for each table? <code>false</code> is
    * always returned as a directory may represent a table. When this is
    * the case, the files stored in the directory are considered to be
    * the rows of the table.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean usesLocalFilePerTable()
      throws SQLException
   {
      return false;
   }

   /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case sensitive and as a result store them in mixed case? A
    * JDBC-Compliant driver will always return false.
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsMixedCaseIdentifiers()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in upper case?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean storesUpperCaseIdentifiers()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in lower case?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean storesLowerCaseIdentifiers()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in mixed case?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean storesMixedCaseIdentifiers()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Does the database treat mixed case quoted SQL identifiers as
    * case sensitive and as a result store them in mixed case?
    * A JDBC-Compliant driver will always return true.
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsMixedCaseQuotedIdentifiers()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in upper case?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean storesUpperCaseQuotedIdentifiers()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in lower case?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean storesLowerCaseQuotedIdentifiers()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in mixed case?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean storesMixedCaseQuotedIdentifiers()
      throws SQLException
   {
      return false;
   }

   /**
    * What's the string used to quote SQL identifiers? This
    * driver always uses a double quote character.
    *
    * @return   the quoting string
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getIdentifierQuoteString()
      throws SQLException
   {
      return "\"";
   }

   /**
    * Get a comma separated list of all a database's SQL keywords that
    * are NOT also SQL92 keywords. This driver has no additional keywords.
    *
    * @return   the list
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getSQLKeywords()
      throws SQLException
   {
      return "";
   }

   /**
    * Get a comma separated list of math functions. This driver does not
    * support math functions.
    *
    * @return   the list
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getNumericFunctions()
      throws SQLException
   {
      return "";
   }

   /**
    * Get a comma separated list of string functions. This driver does
    * not support string functions.
    *
    * @return   the list
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getStringFunctions()
      throws SQLException
   {
      return "";
   }

   /**
    * Get a comma separated list of system functions. This driver does
    * not support system functions.
    *
    * @return   the list
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getSystemFunctions()
      throws SQLException
   {
      return "";
   }

   /**
    * Get a comma separated list of time and date functions. This driver
    * does not supoprt time and date functions.
    *
    * @return   the list
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getTimeDateFunctions()
      throws SQLException
   {
      return "";
   }

   /**
    * This is the string that can be used to escape '_' or '%' in the
    * string pattern style catalog search parameters. Backslash is always
    * returned by this driver.
    * <p>
    * The '_' character represents any single character.
    * <p>
    * The '%' character represents any sequence of zero or more characters.
    *
    * @return   the string used to escape wildcard
    *                            characters
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getSearchStringEscape()
      throws SQLException
   {
      return "\\";
   }

   /** ???
    * Get all the "extra" characters that can be used in unquoted
    * identifier names (those beyond a-z, A-Z, 0-9 and _).
    *
    * @return   the string containing the extra characters
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getExtraNameCharacters()
      throws SQLException
   {
      return null;
   }

   /**
    * Is "ALTER TABLE" with add column supported?
    * <p>
    * DDL is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsAlterTableWithAddColumn()
      throws SQLException
   {
      return false;
   }

   /**
    * Is "ALTER TABLE" with drop column supported?
    * <p>
    * DDL is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsAlterTableWithDropColumn()
      throws SQLException
   {
      return false;
   }

   /**
    * Is column aliasing supported?
    * <p>
    * If so, the SQL AS clause can be used to provide names for computed
    * columns or to provide alias names for columns as required. A
    * JDBC-Compliant driver always returns true.
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsColumnAliasing()
      throws SQLException
   {
      return true;
   }

   /**
    * Are concatenations between NULL and non-NULL values NULL? A
    * JDBC-Compliant driver always returns true.
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean nullPlusNonNullIsNull()
      throws SQLException
   {
      return true;
   }

   /**
    * Is the CONVERT function between SQL types supported?
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsConvert()
      throws SQLException
   {
      return false;
   }

   /**
    * Is CONVERT between the given SQL types supported?
    * <p>
    * CONVERT is not currently supported by this driver.
    *
    * @param   fromType       the type to convert from
    * @param   toType         the type to convert to
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsConvert(int fromType, int toType)
      throws SQLException
   {
      return false;
   }

   /**
    * Are table correlation names supported? A JDBC-Compliant driver
    * always returns true.
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsTableCorrelationNames()
      throws SQLException
   {
      return true;
   }

   /**
    * If table correlation names are supported, are they restricted to
    * be different from the names of the tables?
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsDifferentTableCorrelationNames()
      throws SQLException
   {
      return false;
   }

   /**
    * Are expressions in "ORDER BY" lists supported?
    * <p>
    * Expressions in "ORDER BY" are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsExpressionsInOrderBy()
      throws SQLException
   {
      return false;
   }

   /**
    * Can an "ORDER BY" clause use columns not in the SELECT?
    * <p>
    * Only SELECTed columns are currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsOrderByUnrelated()
      throws SQLException
   {
      return false;
   }

   /**
    * Is some form of "GROUP BY" clause supported?
    * <p>
    * "GROUP BY" is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsGroupBy()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a "GROUP BY" clause use columns not in the SELECT?
    * <p>
    * "GROUP BY" is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsGroupByUnrelated()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a "GROUP BY" clause add columns not in the SELECT provided it
    * specifies all the columns in the SELECT?
    * <p>
    * "GROUP BY" is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsGroupByBeyondSelect()
      throws SQLException
   {
      return false;
   }

   /**
    * Is the escape character in "LIKE" clauses supported? A
    * JDBC-Compliant driver always returns true.
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsLikeEscapeClause()
      throws SQLException
   {
      return true;
   }

   /**
    * Are multiple ResultSets from a single execute supported?
    * <p>
    * Only a single ResultSet per execute is supported by this driver.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsMultipleResultSets()
      throws SQLException
   {
      return false;
   }

   /**
    * Can we have multiple transactions open at once
    * (on different connections)?
    * <p>
    * Multiple transactions can be open as long as they are accessing
    * different files. Each file can only be used by a single transaction,
    * even when separate connections are being used.
    *
    * @return   <code>false</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsMultipleTransactions()
      throws SQLException
   {
      return false;
   }

   /**
    * Can columns be defined as non-nullable? A JDBC-Compliant driver
    * always returns true.
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsNonNullableColumns()
      throws SQLException
   {
      return true;
   }

   /**
    * Is the ODBC Minimum SQL grammar supported? All JDBC-Compliant
    * drivers must return true.
    * <p>
    * ODBC Minimal SQL grammar is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsMinimumSQLGrammar()
      throws SQLException
   {
      return false;
   }

   /**
    * Is the ODBC Core SQL grammar supported?
    * <p>
    * ODBC Core SQL grammar is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsCoreSQLGrammar()
      throws SQLException
   {
      return false;
   }

   /**
    * Is the ODBC Extended SQL grammar supported?
    * <p>
    * ODBC Extended SQL grammar is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsExtendedSQLGrammar()
      throws SQLException
   {
      return false;
   }

   /**
    * Is the ANSI92 entry level SQL grammar supported? All
    * JDBC-Compliant drivers must return true.
    * <p>
    * ANSI92 entry level SQL grammar is not currently supported by
    * this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsANSI92EntryLevelSQL()
      throws SQLException
   {
      return false;
   }

   /**
    * Is the ANSI92 intermediate SQL grammar supported?
    * <p>
    * ANSI92 intermediate SQL grammar is not currently supported by
    * this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsANSI92IntermediateSQL()
      throws SQLException
   {
      return false;
   }

   /**
    * Is the ANSI92 full SQL grammar supported?
    * <p>
    * ANSI92 full SQL grammar is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsANSI92FullSQL()
      throws SQLException
   {
      return false;
   }

   /**
    * Is the SQL Integrity Enhancement Facility supported?
    * <p>
    * The SQL Integrity Enhancement Facility is used to define
    * primary keys, foreign keys, CHECK constraint clauses and
    * DEFAULT clauses.
    * <p>
    * This is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsIntegrityEnhancementFacility()
      throws SQLException
   {
      return false;
   }

   /**
    * Is some form of outer join supported?
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsOuterJoins()
      throws SQLException
   {
      return true;
   }

   /**
    * Are full nested outer joins supported?
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsFullOuterJoins()
      throws SQLException
   {
      return true;
   }

   /**
    * Is there limited support for outer joins? (This will be true if
    * supportFullOuterJoins is true.)
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsLimitedOuterJoins()
      throws SQLException
   {
      return false;
   }

   /**
    * What's the database vendor's preferred term for "schema"?
    * <p>
    * Schemas are not currently supported by this driver.
    *
    * @return   the vendor term
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getSchemaTerm()
      throws SQLException
   {
      return "";
   }

   /**
    * What's the database vendor's preferred term for "procedure"?
    * <p>
    * Procedures are not currently supported by this driver.
    *
    * @return   the vendor term
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getProcedureTerm()
      throws SQLException
   {
      return "";
   }

   /**
    * What's the database vendor's preferred term for "catalog"?
    * <p>
    * Drives and directories are treated as catalogs by this driver.
    *
    * @return   the vendor term
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getCatalogTerm()
      throws SQLException
   {
      return "directory";
   }

   /**
    * Does a catalog appear at the start of a qualified table name?
    * (Otherwise it appears at the end)
    *
    * @return   <code>true</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean isCatalogAtStart()
      throws SQLException
   {
      return true;
   }

   /**
    * What's the separator between catalog and table name? The OS specific
    * path separator is returned by this driver.
    *
    * @return   the separator string
    * @throws   SQLException   if an error occurs
    */
   public java.lang.String getCatalogSeparator()
      throws SQLException
   {
      return java.io.File.pathSeparator;
   }

   /**
    * Can a schema name be used in a data manipulation statement?
    * <p>
    * Schemas are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSchemasInDataManipulation()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a schema name be used in a procedure call statement?
    * <p>
    * Schemas are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSchemasInProcedureCalls()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a schema name be used in a table definition statement?
    * <p>
    * Schemas are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSchemasInTableDefinitions()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a schema name be used in an index definition statement?
    * <p>
    * Schemas are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSchemasInIndexDefinitions()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a schema name be used in a privilege definition statement?
    * <p>
    * Schemas are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSchemasInPrivilegeDefinitions()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a catalog name be used in a data manipulation statement?
    *
    * @return   <code>true</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsCatalogsInDataManipulation()
      throws SQLException
   {
      return true;
   }

   /**
    * Can a catalog name be used in a procedure call statement?
    * <p>
    * Procedures are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsCatalogsInProcedureCalls()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a catalog name be used in a table definition statement?
    * <p>
    * DDL is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsCatalogsInTableDefinitions()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a catalog name be used in an index definition statement?
    * <p>
    * Indexes are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsCatalogsInIndexDefinitions()
      throws SQLException
   {
      return false;
   }

   /**
    * Can a catalog name be used in a privilege definition statement?
    * <p>
    * Privilege definitions are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsCatalogsInPrivilegeDefinitions()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Is positioned DELETE supported?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsPositionedDelete()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Is positioned UPDATE supported?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsPositionedUpdate()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Is SELECT for UPDATE supported?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSelectForUpdate()
      throws SQLException
   {
      return false;
   }

   /**
    * Are stored procedure calls using the stored procedure escape
    * syntax supported?
    * <p>
    * Stored procedures are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsStoredProcedures()
      throws SQLException
   {
      return false;
   }

   /**
    * Are subqueries in comparison expressions supported? A
    * JDBC-Compliant driver always returns true.
    * <p>
    * Subqueries are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSubqueriesInComparisons()
      throws SQLException
   {
      return false;
   }

   /**
    * Are subqueries in 'exists' expressions supported? A
    * JDBC-Compliant driver always returns true.
    * <p>
    * Subqueries are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSubqueriesInExists()
      throws SQLException
   {
      return false;
   }

   /**
    * Are subqueries in 'in' statements supported? A
    * JDBC-Compliant driver always returns true.
    * <p>
    * Subqueries are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSubqueriesInIns()
      throws SQLException
   {
      return false;
   }

   /**
    * Are subqueries in quantified expressions supported? A
    * JDBC-Compliant driver always returns true.
    * <p>
    * Subqueries are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsSubqueriesInQuantifieds()
      throws SQLException
   {
      return false;
   }

   /**
    * Are correlated subqueries supported? A
    * JDBC-Compliant driver always returns true.
    * <p>
    * Subqueries are not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsCorrelatedSubqueries()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Is SQL UNION supported?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsUnion()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Is SQL UNION ALL supported?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsUnionAll()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Can cursors remain open across commits?
    *
    * @return   <code>true</code> if cursors always remain
    *                            open; <code>false</code> if they might not
    *                            remain open
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsOpenCursorsAcrossCommit()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Can cursors remain open across rollbacks?
    *
    * @return   <code>true</code> if cursors always remain
    *                            open; <code>false</code> if they might not
    *                            remain open
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsOpenCursorsAcrossRollback()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Can statements remain open across commits?
    *
    * @return   <code>true</code> if statements always
    *                            remain open; <code>false</code> if they
    *                            might not remain open
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsOpenStatementsAcrossCommit()
      throws SQLException
   {
      return false;
   }

   /** ???
    * Can statements remain open across rollbacks?
    *
    * @return   <code>true</code> if statements always
    *                            remain open; <code>false</code> if they
    *                            might not remain open
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsOpenStatementsAcrossRollback()
      throws SQLException
   {
      return false;
   }

   /**
    * How many hex characters can you have in an inline binary literal?
    * There is no limit imposed by this driver.
    *
    * @return   max literal length
    * @throws   SQLException   if an error occurs
    */
   public int getMaxBinaryLiteralLength()
      throws SQLException
   {
      return Integer.MAX_VALUE;
   }

   /**
    * What's the max length for a character literal?
    * There is no limit imposed by this driver.
    *
    * @return   max literal length
    * @throws   SQLException   if an error occurs
    */
   public int getMaxCharLiteralLength()
      throws SQLException
   {
      return Integer.MAX_VALUE;
   }

   /** ???
    * What's the limit on column name length?
    *
    * @return   max column name length
    * @throws   SQLException   if an error occurs
    */
   public int getMaxColumnNameLength()
      throws SQLException
   {
      return 0;
   }

   /**
    * What's the maximum number of columns in a "GROUP BY" clause?
    * <p>
    * "GROUP BY" is not currently supported by this driver.
    *
    * @return   0 is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public int getMaxColumnsInGroupBy()
      throws SQLException
   {
      return 0;
   }

   /**
    * What's the maximum number of columns allowed in an index?
    * <p>
    * Indexes are not currently supported by this driver.
    *
    * @return   0 is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public int getMaxColumnsInIndex()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum number of columns in an "ORDER BY" clause?
    *
    * @return   max columns
    * @throws   SQLException   if an error occurs
    */
   public int getMaxColumnsInOrderBy()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum number of columns in a "SELECT"?
    *
    * @return   max columns
    * @throws   SQLException   if an error occurs
    */
   public int getMaxColumnsInSelect()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum number of columns in a table?
    *
    * @return   max columns
    * @throws   SQLException   if an error occurs
    */
   public int getMaxColumnsInTable()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * How many active connections can we have at a time to this database?
    *
    * @return   max connections
    * @throws   SQLException   if an error occurs
    */
   public int getMaxConnections()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum cursor name length?
    *
    * @return   max cursor name length in bytes
    * @throws   SQLException   if an error occurs
    */
   public int getMaxCursorNameLength()
      throws SQLException
   {
      return 0;
   }

   /**
    * What's the maximum length of an index (in bytes)?
    * <p>
    * Indexes are not currently supported by this driver.
    *
    * @return   0 is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public int getMaxIndexLength()
      throws SQLException
   {
      return 0;
   }

   /**
    * What's the maximum length allowed for a schema name?
    * <p>
    * Schemas are not currently supported by this driver.
    *
    * @return   0 is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public int getMaxSchemaNameLength()
      throws SQLException
   {
      return 0;
   }

   /**
    * What's the maximum length of a procedure name?
    * <p>
    * Procedures are not currently supported by this driver.
    *
    * @return   0 is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public int getMaxProcedureNameLength()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum length of a catalog name?
    *
    * @return   max name length in bytes
    * @throws   SQLException   if an error occurs
    */
   public int getMaxCatalogNameLength()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum length of a single row?
    *
    * @return   max row size in bytes
    * @throws   SQLException   if an error occurs
    */
   public int getMaxRowSize()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * Did getMaxRowSize() include LONGVARCHAR and LONGVARBINARY blobs?
    *
    * @return   <code>true</code> if so
    * @throws   SQLException   if an error occurs
    */
   public boolean doesMaxRowSizeIncludeBlobs()
      throws SQLException
   {
      return false;
   }

   /** ???
    * What's the maximum length of a SQL statement?
    *
    * @return   max length in bytes
    * @throws   SQLException   if an error occurs
    */
   public int getMaxStatementLength()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * How many active statements can we have open at one time to this
    * database?
    *
    * @return   the maximum
    * @throws   SQLException   if an error occurs
    */
   public int getMaxStatements()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum length of a table name?
    *
    * @return   max name length in bytes
    * @throws   SQLException   if an error occurs
    */
   public int getMaxTableNameLength()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum number of tables in a SELECT?
    *
    * @return   the maximum
    * @throws   SQLException   if an error occurs
    */
   public int getMaxTablesInSelect()
      throws SQLException
   {
      return 0;
   }

   /** ???
    * What's the maximum length of a user name?
    *
    * @return   max name length in bytes
    * @throws   SQLException   if an error occurs
    */
   public int getMaxUserNameLength()
      throws SQLException
   {
      return 0;
   }

   /**
    * What's the database's default transaction isolation level?
    * The values are defined in java.sql.Connection.
    * <p>
    * Transaction isolation is not currently supported by this driver.
    *
    * @return   TRANSACTION_NONE is always returned
    *
    * @throws   SQLException   if an error occurs
    * @see   java.sql.Connection
    */
   public int getDefaultTransactionIsolation()
      throws SQLException
   {
      return java.sql.Connection.TRANSACTION_NONE;
   }

   /**
    * Are transactions supported? If not, commit is a noop and the
    * isolation level is TRANSACTION_NONE.
    * <p>
    * Transaction isolation is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsTransactions()
      throws SQLException
   {
      return false;
   }

   /**
    * Does the database support the given transaction isolation level?
    * <p>
    * Transaction isolation is not currently supported by this driver.
    *
    * @param   level          the values are defined in
    *                            java.sql.Connection
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    * @see   java.sql.Connection
    */
   public boolean supportsTransactionIsolationLevel(int level)
      throws SQLException
   {
      return false;
   }

   /**
    * Are both data definition and data manipulation statements
    * within a transaction supported?
    * <p>
    * DDL is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsDataDefinitionAndDataManipulationTransactions()
      throws SQLException
   {
      return false;
   }

   /**
    * Are only data manipulation statements within a transaction supported?
    *
    * @return   <code>true</code> is always returned
    * @throws   SQLException   if an error occurs
    */
   public boolean supportsDataManipulationTransactionsOnly()
      throws SQLException
   {
      return true;
   }

   /**
    * Does a data definition statement within a transaction force the
    * transaction to commit?
    * <p>
    * DDL is not currently supported by this driver.
    *
    * @return   <code>false</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean dataDefinitionCausesTransactionCommit()
      throws SQLException
   {
      return false;
   }

   /**
    * Is a data definition statement within a transaction ignored?
    * <p>
    * DDL is not currently supported by this driver (thus always ignored).
    *
    * @return   <code>true</code> is always returned
    *
    * @throws   SQLException   if an error occurs
    */
   public boolean dataDefinitionIgnoredInTransactions()
      throws SQLException
   {
      return true;
   }

   /**
    * This is not currently supported.
    *
    * @param   catalog               a catalog name; "" retrieves those
    *                               without a catalog; null means drop
    *                               catalog name from the selection criteria
    * @param   schemaPattern         a schema name pattern; "" retrieves
    *                               those without a schema
    * @param   procedureNamePattern a procedure name pattern
    *
    * @return   ResultSet - each row is a procedure
    *                               description
    * @throws   SQLException      if an error occurs
    * @see   #getSearchStringEscape
    */
   public java.sql.ResultSet getProcedures(
      java.lang.String catalog,
      java.lang.String schemaPattern,
      java.lang.String procedureNamePattern)
      throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * This is not currently supported.
    *
    * @param   catalog               a catalog name; "" retrieves those
    *                               without a catalog; null means drop
    *                               catalog name from the selection criteria
    * @param   schemaPattern         a schema name pattern; "" retrieves
    *                               those without a schema
    * @param   procedureNamePattern a procedure name pattern
    * @param   columnNamePattern    a column name pattern
    *
    * @return   ResultSet - each row is a stored
    *                               procedure parameter or column description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getSearchStringEscape
    */
   public java.sql.ResultSet getProcedureColumns(
      java.lang.String catalog,
      java.lang.String schemaPattern,
      java.lang.String procedureNamePattern,
      java.lang.String columnNamePattern)
      throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * Get a listing of the files and directories (tables) available in
    * a drive or directory (catalog).
    * <p>
    * Only table descriptions matching the catalog, schema, table name
    * and type criteria are returned. They are ordered by TABLE_TYPE,
    * TABLE_SCHEM and TABLE_NAME.
    * <p>
    * Each table description has the following columns:
    * <ol>
    * <li><b>TABLE_CAT</b> String => table catalog (may be null)</li>
    * <li><b>TABLE_SCHEM</b> String => table schema (may be null)</li>
    * <li><b>TABLE_NAME</b> String => table name</li>
    * <li><b>TABLE_TYPE</b> String => table type. Typical types are "TABLE",
    *     "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY",
    *     "ALIAS", "SYNONYM".</li>
    * <li><b>REMARKS</b> String => explanatory comment on the table</li>
    * </ol>
    * Note: Some databases may not return information for all tables.
    *
    * @param   catalog               the directory to be searched
    *
    * @param   schemaPattern         must be ""
    *
    * @param   tableNamePattern      a file name pattern. Use % in lieu of *
    *                               and _ in lieu of ?
    *
    * @param   types                a list of table types to include;
    *                               <code>null</code> returns all types
    *
    * @return   ResultSet - each row is a table description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getSearchStringEscape
    */
   public java.sql.ResultSet getTables(
      java.lang.String catalog,
      java.lang.String schemaPattern,
      java.lang.String tableNamePattern,
      java.lang.String types[])
      throws SQLException
   {
      PSPatternMatcher filt =
         PSPatternMatcher.FileWildcardMatcher(
         tableNamePattern.replace('%', '*').replace('_','?'));

      File startDir = null;

      try
      {
         startDir = PSFileSystemDriver.getPhysicalPath(
                  catalog, // the catalog
                  m_conn.getUserSession(), // the effective user session
                  PSAclEntry.AACE_DESIGN_READ // the required permissions
                  );
      }
      catch (IllegalArgumentException e)
      {
         throw new SQLException("Invalid catalog: " + catalog + ": " +
                                e.getLocalizedMessage());
      }
      catch (PSAuthorizationException authex)
      {
         // we did not have the permissions
         throw new SQLException("Access denied to " + catalog + ": " +
                                authex.getMessage());
      }

      // this will return null if the directory does not exist
      File[] fileList =
         startDir.listFiles((java.io.FileFilter)(new PSFileFilter(filt)));

      // build the column ArrayLists
      String tableTypeDir = "DIRECTORY", tableTypeFile = "FILE" ;
      String remarks = "";

      int size = (fileList == null) ? 0 : fileList.length;

      ArrayList alFileList = new  ArrayList(size),
                tableNames = new ArrayList(size),
                tableSchemas = new ArrayList(size),
                tableTypes = new ArrayList(size),
                tableCatalogs = new ArrayList(size),
                tableRemarks = new ArrayList(size);

      boolean includeDirectories = false;
      boolean includeFiles = false;

      if ((types == null) || (types.length == 0))
      {
         includeDirectories = true;
         includeFiles = true;
      }
      else
      {
         for (int i = 0; i < types.length; i++)
         {
            String curType = types[i];
            if (tableTypeFile.equalsIgnoreCase(curType))
               includeFiles = true;
            else if (tableTypeDir.equalsIgnoreCase(curType))
               includeDirectories = true;
         }
      }

      //compare file objects by their names
      Comparator c = new Comparator()
      {
         public int compare(Object left, Object right)
         {
            File fLeft = (File)left;
            File fRight = (File)right;

            return fLeft.getName().compareTo(fRight.getName());
         }
      };

      //put an array of File objects into an ArrayList for further sorting
      for (int i = 0; i < size; i++)
      {
         alFileList.add(fileList[i]);
      }

      // sort by table name
      Collections.sort(alFileList, c);

      Iterator it = alFileList.iterator();

      while (it.hasNext())
      {
         File f = (File)it.next();

         if (f.isDirectory()) {
            if (!includeDirectories)   // not interested in directories
               continue;

            tableTypes.add(tableTypeDir);
         }
         else {
            if (!includeFiles)         // not interested in files
               continue;

            tableTypes.add(tableTypeFile);
         }

         tableCatalogs.add(catalog);
         tableSchemas.add(null);
         tableNames.add(f.getName());
         tableRemarks.add(remarks);
      }

      // build the column name -> number mapping
      HashMap columnNames = new HashMap();
      columnNames.put("TABLE_CAT", new Integer(1));
      columnNames.put("TABLE_SCHEM", new Integer(2));
      columnNames.put("TABLE_NAME", new Integer(3));
      columnNames.put("TABLE_TYPE", new Integer(4));
      columnNames.put("REMARKS", new Integer(5));

      // put it all in the result set and return it
      return new PSResultSet(new ArrayList[] { tableCatalogs, tableSchemas,
                                               tableNames, tableTypes,
                                               tableRemarks },
                             columnNames,
                             ms_getTablesRSMeta);
   }

   /**
    * This is not currently supported.
    *
    * @return   ResultSet - each row has a single
    *                               String column that is a schema name
    *
    * @throws   SQLException      if an error occurs
    */
   public java.sql.ResultSet getSchemas()
      throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * Get the directories (catalog names) available in this database. If
    * "" was used as the database (catalog) when making the connection,
    * drive names or root directories will be returned. The results are
    * ordered by catalog name.
    * <p>
    * The catalog column is:
    * <ol>
    * <li><b>TABLE_CAT</b> String => catalog name</li>
    * </ol>
    *
    * @return   ResultSet - each row has a single
    *                               String column that is a catalog name
    *
    * @throws   SQLException      if an error occurs
    */
   public java.sql.ResultSet getCatalogs()
      throws SQLException
   {
      if (m_conn == null)
         throw new SQLException("no connection");

      // get a list of the directories under the catalog dir
      String cat = m_conn.getCatalog();

      boolean atRoot = false;

      // use current (E2) directory if catalog not specified
      if (cat == null || cat.length() == 0)
      {
         cat = ".";
         atRoot = true;
      }

      File catDir = null;
      try
      {
         if (atRoot)
            catDir = PSFileSystemDriver.getPhysicalPath(
               cat, // the catalog
               m_conn.getUserSession(), // the effective user session
               PSAclEntry.AACE_DESIGN_READ // the required permissions
               );
         else
            catDir = new File(".");
      }
      catch (IllegalArgumentException e)
      {
         throw new SQLException("Invalid catalog: " + cat + ": " + e.getLocalizedMessage());
      }
      catch (PSAuthorizationException authex)
      {
         // we did not have the permissions
         throw new SQLException("Access denied to " + cat + ": " + authex.getMessage());
      }

      File[] dirs = catDir.listFiles((java.io.FileFilter)
         (new PSFileFilter(PSFileFilter.IS_DIRECTORY)));

      ArrayList v = new ArrayList(dirs.length);

      for (int i = 0; i < dirs.length; i++)
      {
         if (atRoot)
            v.add(dirs[i].getName()); // avoid "./" in path name
         else
            v.add(dirs[i].getPath());
      }

      Collections.sort(v, new java.util.Comparator()
      {
         public int compare(Object left, Object right)
         {
            String leftStr = (String)left, rightStr = (String)right;
            return leftStr.compareTo(rightStr);
         }
      }
      );

      java.util.HashMap columnNames = new java.util.HashMap();
      columnNames.put("TABLE_CAT", new Integer(1));
      return new PSResultSet(new ArrayList[] { v }, columnNames,
         ms_getCatalogsRSMeta);
   }

   /**
    * Get the table types available in this database. The results are
    * ordered by table type.
    * <p>
    * The table type is:
    * <ol>
    * <li><b>TABLE_TYPE</b> String => table type. Typical types are
    *     "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
    *     "LOCAL TEMPORARY", "ALIAS", "SYNONYM".</li>
    * </ol>
    *
    * @return   ResultSet - each row has a single
    *                               String column that is a table type
    *
    * @throws   SQLException      if an error occurs
    */
   public java.sql.ResultSet getTableTypes()
      throws SQLException
   {
      ArrayList v = new ArrayList();
      v.add("DIRECTORY");
      v.add("FILE");
      java.util.HashMap columnNames = new java.util.HashMap();
      columnNames.put("TABLE_TYPE", new Integer(1));
      return new PSResultSet(new ArrayList[] { v }, columnNames,
         ms_getTableTypesRSMeta);
   }

   /**
    * Get a description of table columns available in a catalog. The file
    * system driver always returns the same set of columns:
    * <table border="2">
    * <tr><th>Name</th><th>Data Type</th><th>Description</th></tr>
    * <tr><td>path</td>
    *     <td>java.lang.String</td>
    *     <td>the path (directory) containing this file</td></tr>
    * <tr><td>name</td>
    *     <td>java.lang.String</td>
    *     <td>the name of the file, without any path information</td></tr>
    * <tr><td>fullname</td>
    *     <td>java.lang.String</td>
    *     <td>the full name of the file (includes path and name)</td></tr>
    * <tr><td>modified</td>
    *     <td>java.util.Date</td>
    *     <td>the date and time of the last modification to the file</td></tr>
    * <tr><td>length</td>
    *     <td>long</td>
    *     <td>the length of the file's contents</td></tr>
    * <tr><td>contents</td>
    *     <td>???</td>
    *     <td>the contents of the file</td></tr>
    * </table>
    * Additional attributes exist for files such as read-only, hidden, etc.
    * Retrieving attributes is not available through Java, so we will not
    * support it. Also, Java does not support retrieving the file creation
    * time or last accessed time. This too will not be supported.
    * <p>
    * Only column descriptions matching the catalog, schema, table
    * and column name criteria are returned. They are ordered by
    * TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION.
    * <p>
    * Each column description has the following columns:
    * <ol>
    * <li><b>TABLE_CAT</b> String => table catalog (may be null)</li>
    * <li><b>TABLE_SCHEM</b> String => table schema (may be null)</li>
    * <li><b>TABLE_NAME</b> String => table name</li>
    * <li><b>COLUMN_NAME</b> String => column name</li>
    * <li><b>DATA_TYPE</b> short => SQL type from java.sql.Types</li>
    * <li><b>TYPE_NAME</b> String => Data source dependent type name</li>
    * <li><b>COLUMN_SIZE</b> int => column size. For char or date types
    *     this is the maximum number of characters, for numeric or
    *     decimal types this is precision.</li>
    * <li><b>BUFFER_LENGTH</b> is not used.</li>
    * <li><b>DECIMAL_DIGITS</b> int => the number of fractional digits</li>
    * <li><b>NUM_PREC_RADIX</b> int => Radix (typically either 10 or 2)</li>
    * <li><b>NULLABLE</b> int => is NULL allowed?
    *    <ul>
    *    <li><b>columnNoNulls</b> - might not allow NULL values</li>
    *    <li><b>columnNullable</b> - definitely allows NULL values</li>
    *    <li><b>columnNullableUnknown</b> - nullability unknown</li>
    *    </ul></li>
    * <li><b>REMARKS</b> String => comment describing column (may be null)</li>
    * <li><b>COLUMN_DEF</b> String => default value (may be null)</li>
    * <li><b>SQL_DATA_TYPE</b> int => unused</li>
    * <li><b>SQL_DATETIME_SUB</b> int => unused</li>
    * <li><b>CHAR_OCTET_LENGTH</b> int => for char types the maximum number
    *     of bytes in the column</li>
    * <li><b>ORDINAL_POSITION</b> int => index of column in table (starting
    *     at 1)</li>
    * <li><b>IS_NULLABLE</b> String => "NO" means column definitely does
    *     not allow NULL values; "YES" means the column might allow NULL
    *     values. An empty string means nobody knows.</li>
    * </ol>
    *
    * @param   catalog               the directory to be searched
    *
    * @param   schemaPattern         must be ""
    *
    * @param   tableNamePattern      a file name pattern. always ignored
    *
    * @param   columnNamePattern    a column name pattern
    *
    * @return   ResultSet - each row is a column
    *                               description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getSearchStringEscape
    */
   public java.sql.ResultSet getColumns(
      java.lang.String catalog,
      java.lang.String schemaPattern,
      java.lang.String tableNamePattern,
      java.lang.String columnNamePattern)
      throws SQLException
   {
      // build a file wildcard comparison object that will be case
      // sensitive or case insensitive depending on what platform the
      // server is running on. To do so, we turn the SQL LIKE pattern
      // into a file wildcard by replacing % with * and replacing
      // _ with ? and then build a file wildcard matcher out of the
      // resulting pattern string
      PSPatternMatcher columnNameFilt =
         PSPatternMatcher.SQLPatternMatcher(columnNamePattern);

      // table catalog
      ArrayList table_cat = new ArrayList();

      // String => table schema (may be null)
      ArrayList table_schem = new ArrayList();

      // String => table name
      ArrayList table_name = new ArrayList();

      //String => column name
      ArrayList column_name = new ArrayList();

      // short => SQL type from java.sql.Types
      ArrayList data_type = new ArrayList();

      // String => Data source dependent type name
      ArrayList type_name = new ArrayList();

      /* int => column size. For char or date types this is the maximum
       * number of characters, for numeric or decimal types this is
       * precision.
       */
      ArrayList column_size = new ArrayList();

      // is not used
      ArrayList buffer_length = new ArrayList();

      // int => the number of fractional digits
      ArrayList decimal_digits = new ArrayList();

      // int => Radix (typically either 10 or 2)
      ArrayList num_prec_radix = new ArrayList();

      /* int => is NULL allowed?
       * columnNoNulls - might not allow NULL values
       * columnNullable - definitely allows NULL values
       * columnNullableUnknown - nullability unknown
       */
      ArrayList nullable = new ArrayList();

      // String => comment describing column (may be null)
      ArrayList remarks = new ArrayList();

      // String => default value (may be null)
      ArrayList column_def = new ArrayList();

      // int => unused
      ArrayList sql_data_type = new ArrayList();

      // int => unused
      ArrayList sql_datetime_sub = new ArrayList();

      // int => for char types the maximum number of bytes in the column
      ArrayList char_octet_length = new ArrayList();

      // int => index of column in table (starting at 1)
      ArrayList ordinal_position = new ArrayList();

      /* String => "NO" means column definitely doesn't allow NULL values;
       * "YES" means the column might allow NULL values. An empty string
       * means nobody knows.
       */
      ArrayList is_nullable = new ArrayList();

      Integer zero = new Integer(0);

      /* this is not a Phase 1 thing (contents not supported)
       if (columnNameFilt.doesMatchPattern("contents"))
       {
       table_schem.add(null);
       table_name.add(null);
       column_name.add("fullname");
       data_type.add(new Integer(java.sql.Types.VARCHAR));
       type_name.add("java.lang.String");
       column_size.add(new Integer(1024));
       buffer_length.add(zero);
       decimal_digits.add(zero);
       num_prec_radix.add(zero);
       nullable.add(new Integer(columnNoNulls));
       remarks.add(null);
       column_def.add(null);
       sql_data_type.add(zero);
       sql_datetime_sub.add(zero);
       char_octet_length.add(new Integer(2048));
       ordinal_position.add(new Integer(5));
       is_nullable.add("");
       }
       */
      if (columnNameFilt.doesMatchPattern("fullname"))
      {
         table_cat.add(catalog);
         table_schem.add(null);
         table_name.add(null);
         column_name.add("fullname");
         data_type.add(new Integer(java.sql.Types.VARCHAR));
         type_name.add("java.lang.String");
         column_size.add(new Integer(1024));
         buffer_length.add(zero);
         decimal_digits.add(zero);
         num_prec_radix.add(zero);
         nullable.add(new Integer(columnNoNulls));
         remarks.add(null);
         column_def.add(null);
         sql_data_type.add(zero);
         sql_datetime_sub.add(zero);
         char_octet_length.add(new Integer(2048));
         ordinal_position.add(new Integer(3));
         is_nullable.add("");
      }
      if (columnNameFilt.doesMatchPattern("length"))
      {
         table_cat.add(catalog);
         table_schem.add(null);
         table_name.add(null);
         column_name.add("length");
         data_type.add(new Integer(java.sql.Types.BIGINT));
         type_name.add("long");
         // there are 19 digits in 2^64, so precision is 19
         column_size.add(new Integer(19));
         buffer_length.add(zero);
         decimal_digits.add(zero);
         num_prec_radix.add(zero);
         nullable.add(new Integer(columnNoNulls));
         remarks.add(null);
         column_def.add(null);
         sql_data_type.add(zero);
         sql_datetime_sub.add(zero);
         char_octet_length.add(new Integer(8));
         ordinal_position.add(new Integer(5));
         is_nullable.add("");
      }
      if (columnNameFilt.doesMatchPattern("modified"))
      {
         table_cat.add(catalog);
         table_schem.add(null);
         table_name.add(null);
         column_name.add("modified");
         data_type.add(new Integer(java.sql.Types.DATE));
         type_name.add("java.util.Date");
         // there are 19 digits in 2^64, so precision is 19
         column_size.add(new Integer(19));
         buffer_length.add(zero);
         decimal_digits.add(zero);
         num_prec_radix.add(zero);
         nullable.add(new Integer(columnNoNulls));
         remarks.add(null);
         column_def.add(null);
         sql_data_type.add(zero);
         sql_datetime_sub.add(zero);
         char_octet_length.add(new Integer(8));
         ordinal_position.add(new Integer(4));
         is_nullable.add("");
      }
      if (columnNameFilt.doesMatchPattern("name"))
      {
         table_cat.add(catalog);
         table_schem.add(null);
         table_name.add(null);
         column_name.add("name");
         data_type.add(new Integer(java.sql.Types.VARCHAR));
         type_name.add("java.lang.String");
         column_size.add(new Integer(1024));
         buffer_length.add(zero);
         decimal_digits.add(zero);
         num_prec_radix.add(zero);
         nullable.add(new Integer(columnNoNulls));
         remarks.add(null);
         column_def.add(null);
         sql_data_type.add(zero);
         sql_datetime_sub.add(zero);
         char_octet_length.add(new Integer(2048));
         ordinal_position.add(new Integer(2));
         is_nullable.add("");
      }
      if (columnNameFilt.doesMatchPattern("path"))
      {
         table_cat.add(catalog);
         table_schem.add(null);
         table_name.add(null);
         column_name.add("path");
         data_type.add(new Integer(java.sql.Types.VARCHAR));
         type_name.add("java.lang.String");
         column_size.add(new Integer(1024));
         buffer_length.add(zero);
         decimal_digits.add(zero);
         num_prec_radix.add(zero);
         nullable.add(new Integer(columnNoNulls));
         remarks.add(null);
         column_def.add(null);
         sql_data_type.add(zero);
         sql_datetime_sub.add(zero);
         char_octet_length.add(new Integer(2048));
         ordinal_position.add(new Integer(1));
         is_nullable.add("");
      }

      java.util.HashMap columnNames = new java.util.HashMap();
      columnNames.put("TABLE_CAT", new Integer(1));
      columnNames.put("TABLE_SCHEM", new Integer(2));
      columnNames.put("TABLE_NAME", new Integer(3));
      columnNames.put("COLUMN_NAME", new Integer(4));
      columnNames.put("DATA_TYPE", new Integer(5));
      columnNames.put("TYPE_NAME", new Integer(6));
      columnNames.put("COLUMN_SIZE", new Integer(7));
      columnNames.put("BUFFER_LENGTH", new Integer(8));
      columnNames.put("DECIMAL_DIGITS", new Integer(9));
      columnNames.put("NUM_PREC_RADIX", new Integer(10));
      columnNames.put("NULLABLE", new Integer(11));
      columnNames.put("REMARKS", new Integer(12));
      columnNames.put("COLUMN_DEF", new Integer(13));
      columnNames.put("SQL_DATA_TYPE", new Integer(14));
      columnNames.put("SQL_DATETIME_SUB", new Integer(15));
      columnNames.put("CHAR_OCTET_LENGTH", new Integer(16));
      columnNames.put("ORDINAL_POSITION", new Integer(17));
      columnNames.put("IS_NULLABLE", new Integer(18));

      return new PSResultSet(new ArrayList[] { table_cat, table_schem, table_name,
         column_name, data_type, type_name, column_size, buffer_length,
         decimal_digits, num_prec_radix, nullable, remarks, column_def,
         sql_data_type, sql_datetime_sub, char_octet_length,
         ordinal_position, is_nullable }, columnNames, ms_getColumnsRSMeta);
   }

   /**
    * Get a description of the access rights for a table's columns.
    * <p>
    * Only privileges matching the column name criteria are returned.
    * They are ordered by COLUMN_NAME and PRIVILEGE.
    * <p>
    * Each privilige description has the following columns:
    * <ol>
    * <li><b>TABLE_CAT</b> String => table catalog (may be null)</li>
    * <li><b>TABLE_SCHEM</b> String => table schema (may be null)</li>
    * <li><b>TABLE_NAME</b> String => table name</li>
    * <li><b>COLUMN_NAME</b> String => column name</li>
    * <li><b>GRANTOR</b> => grantor of access (may be null)</li>
    * <li><b>GRANTEE</b> String => grantee of access</li>
    * <li><b>PRIVILEGE</b> String => name of access (SELECT, INSERT,
    *     UPDATE, REFRENCES, ...)</li>
    * <li><b>IS_GRANTABLE</b> String => "YES" if grantee is permitted to
    *     grant to others; "NO" if not; null if unknown</li>
    * </ol>
    *
    * @param   catalog               the directory containing the file
    *
    * @param   schema               must be ""
    *
    * @param   table                a file name
    *
    * @param   columnNamePattern    a column name pattern
    *
    * @return   ResultSet - each row is a column
    *                               privelege description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getSearchStringEscape
    */
   public java.sql.ResultSet getColumnPrivileges(
                                                 java.lang.String catalog,
                                                 java.lang.String schema,
                                                 java.lang.String table,
                                                 java.lang.String columnNamePattern)
                                                 throws SQLException
   {
      return null;
   }

   /**
    * Get a description of the access rights for each table available in
    * a catalog. Note that a table privilege applies to one or more
    * columns in the table. It would be wrong to assume that this
    * priviledge applies to all columns (this may be true for some
    * systems but is not true for all.)
    * <p>
    * Only privileges matching the schema and table name criteria are
    * returned. They are ordered by TABLE_SCHEM, TABLE_NAME, and PRIVILEGE.
    * <p>
    * Each privilige description has the following columns:
    * <ol>
    * <li><b>TABLE_CAT</b> String => table catalog (may be null)</li>
    * <li><b>TABLE_SCHEM</b> String => table schema (may be null)</li>
    * <li><b>TABLE_NAME</b> String => table name</li>
    * <li><b>GRANTOR</b> => grantor of access (may be null)</li>
    * <li><b>GRANTEE</b> String => grantee of access</li>
    * <li><b>PRIVILEGE</b> String => name of access (SELECT, INSERT,
    *     UPDATE, REFRENCES, ...)</li>
    * <li><b>IS_GRANTABLE</b> String => "YES" if grantee is permitted to
    *     grant to others; "NO" if not; null if unknown</lu>
    * </ol>
    *
    * @param   catalog               the directory to be searched
    *
    * @param   schemaPattern         must be ""
    *
    * @param   tableNamePattern      a file name pattern. Use % in lieu of *
    *                               and _ in lieu of ?
    *
    * @return   ResultSet - each row is a table
    *                               privelege description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getSearchStringEscape
    */
   public java.sql.ResultSet getTablePrivileges(
                                                java.lang.String catalog,
                                                java.lang.String schemaPattern,
                                                java.lang.String tableNamePattern)
                                                throws SQLException
   {
      return null;
   }

   /**
    * Get a description of a table's optimal set of columns that uniquely
    * identifies a row. They are ordered by SCOPE.
    * <p>
    * Each column description has the following columns:
    * <ol>
    * <li>SCOPE short => actual scope of result
    *    <ul>
    *    <li>bestRowTemporary - very temporary, while using row</li>
    *    <li>bestRowTransaction - valid for remainder of current transaction</li>
    *    <li>bestRowSession - valid for remainder of current session</li>
    *    </ul></li>
    * <li>COLUMN_NAME String => column name</li>
    * <li>DATA_TYPE short => SQL data type from java.sql.Types</li>
    * <li>TYPE_NAME String => Data source dependent type name</li>
    * <li>COLUMN_SIZE int => precision</li>
    * <li>BUFFER_LENGTH int => not used</li>
    * <li>DECIMAL_DIGITS short => scale</li>
    * <li>PSEUDO_COLUMN short => is this a pseudo column like an Oracle ROWID
    *    <ul>
    *    <li>bestRowUnknown - may or may not be pseudo column</li>
    *    <li>bestRowNotPseudo - is NOT a pseudo column</li>
    *    <li>bestRowPseudo - is a pseudo column
    *    </ul></li>
    * </ol>
    *
    * @param   catalog               the directory containing the file
    *
    * @param   schema               must be ""
    *
    * @param   table                a file name
    *
    * @param   scope                the scope of interest; use same
    *                               values as SCOPE
    *
    * @param   nullable             include columns that are nullable?
    *
    * @return   ResultSet - each row is a column
    *                               description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getSearchStringEscape
    */
   public java.sql.ResultSet getBestRowIdentifier(
                                                  java.lang.String catalog,
                                                  java.lang.String schema,
                                                  java.lang.String table,
                                                  int scope,
                                                  boolean nullable)
                                                  throws SQLException
   {
      return null;
   }


   /**
    * Get a description of a table's columns that are automatically updated
    * when any value in a row is updated. They are unordered.
    * <p>
    * Each column description has the following columns:
    * <ol>
    * <li><b>SCOPE</b> short => is not used</li>
    * <li><b>COLUMN_NAME</b> String => column name</li>
    * <li><b>DATA_TYPE</b> short => SQL data type from java.sql.Types</li>
    * <li><b>TYPE_NAME</b> String => Data source dependent type name</li>
    * <li><b>COLUMN_SIZE</b> int => precision</li>
    * <li><b>BUFFER_LENGTH</b> int => length of column value in bytes</li>
    * <li><b>DECIMAL_DIGITS</b> short => scale</li>
    * <li><b>PSEUDO_COLUMN</b> short => is this a pseudo column like
    *        an Oracle ROWID</li>
    *    <ul>
    *    <li><b>versionColumnUnknown</b> - may or may not be pseudo
    *           column</li>
    *    <li><b>versionColumnNotPseudo</b> - is NOT a pseudo column</li>
    *    <li><b>versionColumnPseudo</b> - is a pseudo column</li>
    *    </ul></li>
    * </ol>
    *
    * @param   catalog               the directory containing the file
    *
    * @param   schema               must be ""
    *
    * @param   table                a file name
    *
    * @return   ResultSet - each row is a column
    *                               description
    *
    * @throws   SQLException      if an error occurs
    */
   public java.sql.ResultSet getVersionColumns(
                                               java.lang.String catalog,
                                               java.lang.String schema,
                                               java.lang.String table)
                                               throws SQLException
   {
      return null;
   }

   /**
    * Get a description of a table's primary key columns. They are
    * ordered by COLUMN_NAME.
    * <p>
    * Each primary key column description has the following columns:
    * <ol>
    * <li><b>TABLE_CAT</b> String => table catalog (may be null)</li>
    * <li><b>TABLE_SCHEM</b> String => table schema (may be null)</li>
    * <li><b>TABLE_NAME</b> String => table name</li>
    * <li><b>COLUMN_NAME</b> String => column name</li>
    * <li><b>KEY_SEQ</b> short => sequence number within primary key</li>
    * <li><b>PK_NAME</b> String => primary key name (may be null)</li>
    * </ol>
    *
    * @param   catalog               the directory containing the file
    *
    * @param   schema               must be ""
    *
    * @param   table                a file name
    *
    * @return   ResultSet - each row is a primary key
    *                               column description
    *
    * @throws   SQLException      if an error occurs
    */
   public java.sql.ResultSet getPrimaryKeys(
                                            java.lang.String catalog,
                                            java.lang.String schema,
                                            java.lang.String table)
                                            throws SQLException
   {
      ArrayList table_cat = new ArrayList();
      ArrayList table_schem = new ArrayList();
      ArrayList table_name = new ArrayList();
      ArrayList column_name = new ArrayList();
      ArrayList key_seq = new ArrayList();
      ArrayList pk_name = new ArrayList();

      table_cat.add(catalog);
      table_schem.add(schema);
      table_name.add(table);
      column_name.add("fullname");
      key_seq.add(new Integer(1));
      pk_name.add(null);

      java.util.HashMap columnNames = new java.util.HashMap();

      columnNames.put("TABLE_CAT", new Integer(1));
      columnNames.put("TABLE_SCHEM", new Integer(2));
      columnNames.put("TABLE_NAME", new Integer(3));
      columnNames.put("COLUMN_NAME", new Integer(4));
      columnNames.put("KEY_SEQ", new Integer(5));
      columnNames.put("PK_NAME", new Integer(6));

      return new PSResultSet(new ArrayList[] { table_cat, table_schem,
         table_name, column_name, key_seq, pk_name }, columnNames,
         ms_getPrimaryKeysRSMeta);
   }

   /**
    * FOREIGN KEYS ARE NOT SUPPORTED BY THE FILE SYSTEM DRIVER.
    *
    * Get a description of the primary key columns that are referenced by
    * a table's foreign key columns (the primary keys imported by a table).
    * They are ordered by PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME, and
    * KEY_SEQ.
    * <p>
    * Each primary key column description has the following columns:
    * <ol>
    * <li><b>PKTABLE_CAT</b> String => primary key table catalog being
    *        imported (may be null)</li>
    * <li><b>PKTABLE_SCHEM</b> String => primary key table schema being
    *        imported (may be null)</li>
    * <li><b>PKTABLE_NAME</b> String => primary key table name being
    *        imported</li>
    * <li><b>PKCOLUMN_NAME</b> String => primary key column name being
    *        imported</li>
    * <li><b>FKTABLE_CAT</b> String => foreign key table catalog (may be
    *        null)</li>
    * <li><b>FKTABLE_SCHEM</b> String => foreign key table schema (may be
    *        null)</li>
    * <li><b>FKTABLE_NAME</b> String => foreign key table name</li>
    * <li><b>FKCOLUMN_NAME</b> String => foreign key column name</li>
    * <li><b>KEY_SEQ</b> short => sequence number within foreign key</li>
    * <li><b>UPDATE_RULE</b> short => What happens to foreign key when
    *        primary is updated:
    *    <ul>
    *    <li><b>importedNoAction</b> - do not allow update of primary key
    *           if it has been imported</li>
    *    <li><b>importedKeyCascade</b> - change imported key to agree with
    *           primary key update</li>
    *    <li><b>importedKeySetNull</b> - change imported key to NULL if
    *           its primary key has been updated</li>
    *    <li><b>importedKeySetDefault</b> - change imported key to default
    *           values if its primary key has been updated</li>
    *    <li><b>importedKeyRestrict</b> - same as importedKeyNoAction
    *           (for ODBC 2.x compatibility)</li>
    *    </ul></li>
    * <li><b>DELETE_RULE</b> short => What happens to the foreign key
    *        when primary is deleted.
    *    <ul>
    *    <li><b>importedKeyNoAction</b> - do not allow delete of primary
    *           key if it has been imported</li>
    *    <li><b>importedKeyCascade</b> - delete rows that import a
    *           deleted key</li>
    *    <li><b>importedKeySetNull</b> - change imported key to NULL if
    *           its primary key has been deleted</li>
    *    <li><b>importedKeyRestrict</b> - same as importedKeyNoAction
    *           (for ODBC 2.x compatibility)</li>
    *    <li><b>importedKeySetDefault</b> - change imported key to
    *           default if its primary key has been deleted</li>
    *    </ul></li>
    * <li><b>FK_NAME</b> String => foreign key name (may be null)</li>
    * <li><b>PK_NAME</b> String => primary key name (may be null)</li>
    * <li><b>DEFERRABILITY</b> short => can the evaluation of foreign
    *        key constraints be deferred until commit
    *    <ul>
    *    <li><b>importedKeyInitiallyDeferred</b> - see SQL92 for
    *           definition</li>
    *    <li><b>importedKeyInitiallyImmediate</b> - see SQL92 for
    *           definition</li>
    *    <li><b>importedKeyNotDeferrable</b> - see SQL92 for definition</li>
    *    </ul></li>
    * </ol>
    *
    * @param   catalog               the directory containing the file
    *
    * @param   schema               must be ""
    *
    * @param   table                a file name
    *
    * @return   ResultSet - each row is a primary key
    *                               column description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getExportedKeys
    */
   public java.sql.ResultSet getImportedKeys(
                                             java.lang.String catalog,
                                             java.lang.String schema,
                                             java.lang.String table)
                                             throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * FOREIGN KEYS ARE NOT SUPPORTED BY THE FILE SYSTEM DRIVER.
    *
    * Get a description of the foreign key columns that reference a
    * table's primary key columns (the foreign keys exported by a
    * table). They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
    * FKTABLE_NAME, and KEY_SEQ.
    * <p>
    * Each foreign key column description has the following columns:
    * <ol>
    * <li><b>PKTABLE_CAT</b> String => primary key table catalog
    *        (may be null)</li>
    * <li><b>PKTABLE_SCHEM</b> String => primary key table schema
    *        (may be null)</li>
    * <li><b>PKTABLE_NAME</b> String => primary key table name</li>
    * <li><b>PKCOLUMN_NAME</b> String => primary key column name</li>
    * <li><b>FKTABLE_CAT</b> String => foreign key table catalog
    *        (may be null) being exported (may be null)</li>
    * <li><b>FKTABLE_SCHEM</b> String => foreign key table schema
    *        (may be null) being exported (may be null)</li>
    * <li><b>FKTABLE_NAME</b> String => foreign key table name being
    *        exported</li>
    * <li><b>FKCOLUMN_NAME</b> String => foreign key column name being
    *        exported</li>
    * <li><b>KEY_SEQ</b> short => sequence number within foreign key</li>
    * <li><b>UPDATE_RULE</b> short => What happens to foreign key when
    *        primary is updated:
    *    <ul>
    *    <li><b>importedNoAction</b> - do not allow update of primary key
    *           if it has been imported</li>
    *    <li><b>importedKeyCascade</b> - change imported key to agree
    *           with primary key update</li>
    *    <li><b>importedKeySetNull</b> - change imported key to NULL
    *           if its primary key has been updated</li>
    *    <li><b>importedKeySetDefault</b> - change imported key to
    *           default values if its primary key has been updated</li>
    *    <li><b>importedKeyRestrict</b> - same as importedKeyNoAction
    *           (for ODBC 2.x compatibility)</li>
    *    </ul></li>
    * <li><b>DELETE_RULE</b> short => What happens to the foreign key
    *        when primary is deleted.
    *    <ul>
    *    <li><b>importedKeyNoAction</b> - do not allow delete of primary
    *           key if it has been imported</li>
    *    <li><b>importedKeyCascade</b> - delete rows that import a
    *           deleted key</li>
    *    <li><b>importedKeySetNull</b> - change imported key to NULL
    *           if its primary key has been deleted</li>
    *    <li><b>importedKeyRestrict</b> - same as importedKeyNoAction
    *           (for ODBC 2.x compatibility)</li>
    *    <li><b>importedKeySetDefault</b> - change imported key to
    *           default if its primary key has been deleted</li>
    *    </ul></li>
    * <li><b>FK_NAME</b> String => foreign key name (may be null)</li>
    * <li><b>PK_NAME</b> String => primary key name (may be null)</li>
    * <li><b>DEFERRABILITY</b> short => can the evaluation of foreign
    *        key constraints be deferred until commit
    *    <ul>
    *    <li><b>importedKeyInitiallyDeferred</b> - see SQL92 for
    *           definition</li>
    *    <li><b>importedKeyInitiallyImmediate</b> - see SQL92 for
    *           definition</li>
    *    <li><b>importedKeyNotDeferrable</b> - see SQL92 for
    *           definition</li>
    *    </ul></li>
    * </ol>
    *
    * @param   catalog               the directory containing the file
    *
    * @param   schema               must be ""
    *
    * @param   table                a file name
    *
    * @return   ResultSet - each row is a foreign key
    *                               column description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getImportedKeys
    */
   public java.sql.ResultSet getExportedKeys(
                                             java.lang.String catalog,
                                             java.lang.String schema,
                                             java.lang.String table)
                                             throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * FOREIGN KEYS ARE NOT SUPPORTED BY THE FILE SYSTEM DRIVER.
    *
    * Get a description of the foreign key columns in the foreign key
    * table that reference the primary key columns of the primary key
    * table (describe how one table imports another's key.) This should
    * normally return a single foreign key/primary key pair (most
    * tables only import a foreign key from a table once.) They are
    * ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and KEY_SEQ.
    * <p>
    * Each foreign key column description has the following columns:
    * <ol>
    * <li><b>PKTABLE_CAT</b> String => primary key table catalog
    *        (may be null)</li>
    * <li><b>PKTABLE_SCHEM</b> String => primary key table schema
    *        (may be null)</li>
    * <li><b>PKTABLE_NAME</b> String => primary key table name</li>
    * <li><b>PKCOLUMN_NAME</b> String => primary key column name</li>
    * <li><b>FKTABLE_CAT</b> String => foreign key table catalog
    *        (may be null) being exported (may be null)</li>
    * <li><b>FKTABLE_SCHEM</b> String => foreign key table schema
    *        (may be null) being exported (may be null)</li>
    * <li><b>FKTABLE_NAME</b> String => foreign key table name being
    *        exported</li>
    * <li><b>FKCOLUMN_NAME</b> String => foreign key column name being
    *        exported</li>
    * <li><b>KEY_SEQ</b> short => sequence number within foreign key</li>
    * <li><b>UPDATE_RULE</b> short => What happens to foreign key when
    *        primary is updated:
    *    <ul>
    *    <li><b>importedNoAction</b> - do not allow update of primary key
    *           if it has been imported</li>
    *    <li><b>importedKeyCascade</b> - change imported key to agree
    *           with primary key update</li>
    *    <li><b>importedKeySetNull</b> - change imported key to NULL if
    *           its primary key has been updated</li>
    *    <li><b>importedKeySetDefault</b> - change imported key to
    *           default values if its primary key has been updated</li>
    *    <li><b>importedKeyRestrict</b> - same as importedKeyNoAction
    *           (for ODBC 2.x compatibility)</li>
    *    </ul></li>
    * <li><b>DELETE_RULE</b> short => What happens to the foreign key
    *        when primary is deleted.
    *    <ul>
    *    <li><b>importedKeyNoAction</b> - do not allow delete of primary
    *           key if it has been imported</li>
    *    <li><b>importedKeyCascade</b> - delete rows that import a
    *           deleted key</li>
    *    <li><b>importedKeySetNull</b> - change imported key to NULL
    *           if its primary key has been deleted</li>
    *    <li><b>importedKeyRestrict</b> - same as importedKeyNoAction
    *           (for ODBC 2.x compatibility)</li>
    *    <li><b>importedKeySetDefault</b> - change imported key to
    *           default if its primary key has been deleted</li>
    *    </ul></li>
    * <li><b>FK_NAME</b> String => foreign key name (may be null)</li>
    * <li><b>PK_NAME</b> String => primary key name (may be null)</li>
    * <li><b>DEFERRABILITY</b> short => can the evaluation of foreign
    *        key constraints be deferred until commit
    *    <ul>
    *    <li><b>importedKeyInitiallyDeferred</b> - see SQL92 for
    *           definition</li>
    *    <li><b>importedKeyInitiallyImmediate</b> - see SQL92 for
    *           definition</li>
    *    <li><b>importedKeyNotDeferrable</b> - see SQL92 for definition</li>
    *    </ul></li>
    * </ol>
    *
    * @param   primaryCatalog       the directory containing the primary file
    *
    * @param   primarySchema         must be ""
    *
    * @param   primaryTable         a file name that exports the key
    *
    * @param   foreignCatalog       the directory containing the foreign file
    *
    * @param   foreignSchema         must be ""
    *
    * @param   foreignTable         a file name that imports the key
    *
    * @return   ResultSet - each row is a foreign key
    *                               column description
    *
    * @throws   SQLException      if an error occurs
    *
    * @see   #getImportedKeys
    */
   public java.sql.ResultSet getCrossReference(
                                               java.lang.String primaryCatalog,
                                               java.lang.String primarySchema,
                                               java.lang.String primaryTable,
                                               java.lang.String foreignCatalog,
                                               java.lang.String foreignSchema,
                                               java.lang.String foreignTable)
                                               throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * Get a description of all the standard SQL types supported by this
    * database. They are ordered by DATA_TYPE and then by how closely the
    * data type maps to the corresponding JDBC SQL type.
    * <p>
    * Each type description has the following columns:
    * <ol>
    * <li><b>TYPE_NAME</b> String => Type name</li>
    * <li><b>DATA_TYPE</b> short => SQL data type from java.sql.Types</li>
    * <li><b>PRECISION</b> int => maximum precision</li>
    * <li><b>LITERAL_PREFIX</b> String => prefix used to quote a literal
    *        (may be null)</li>
    * <li><b>LITERAL_SUFFIX</b> String => suffix used to quote a literal
    *        (may be null)</li>
    * <li><b>CREATE_PARAMS</b> String => parameters used in creating the
    *        type (may be null)</li>
    * <li><b>NULLABLE</b> short => can you use NULL for this type?
    *    <ul>
    *    <li><b>typeNoNulls</b> - does not allow NULL values</li>
    *    <li><b>typeNullable</b> - allows NULL values</li>
    *    <li><b>typeNullableUnknown</b> - nullability unknown</li>
    *    </ul></li>
    * <li><b>CASE_SENSITIVE</b> boolean=> is it case sensitive?</li>
    * <li><b>SEARCHABLE</b> short => can you use "WHERE" based on
    *        this type:
    *    <ul>
    *    <li><b>typePredNone</b> - No support</li>
    *    <li><b>typePredChar</b> - Only supported with WHERE .. LIKE</li>
    *    <li><b>typePredBasic</b> - Supported except for WHERE .. LIKE</li>
    *    <li><b>typeSearchable</b> - Supported for all WHERE .. </li>
    *    </ul></li>
    * <li><b>UNSIGNED_ATTRIBUTE</b> boolean => is it unsigned?</li>
    * <li><b>FIXED_PREC_SCALE</b> boolean => can it be a money value?</li>
    * <li><b>AUTO_INCREMENT</b> boolean => can it be used for an
    *        auto-increment value?</li>
    * <li><b>LOCAL_TYPE_NAME</b> String => localized version of type name
    *        (may be null)</li>
    * <li><b>MINIMUM_SCALE</b> short => minimum scale supported</li>
    * <li><b>MAXIMUM_SCALE</b> short => maximum scale supported</li>
    * <li><b>SQL_DATA_TYPE</b> int => unused</li>
    * <li><b>SQL_DATETIME_SUB</b> int => unused</li>
    * <li><b>NUM_PREC_RADIX</b> int => usually 2 or 10</li>
    * </ol>
    *
    * @return   ResultSet - each row is a SQL type
    *                               description
    *
    * @throws   SQLException      if an error occurs
    */
   public java.sql.ResultSet getTypeInfo()
   throws SQLException
   {
      return null;
   }

   /**
    * INDEXES ARE NOT SUPPORTED BY THE FILE SYSTEM DRIVER.
    *
    * Get a description of a table's indices and statistics. They are
    * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
    * <p>
    * Each index column description has the following columns:
    * <ol>
    * <li><b>TABLE_CAT</b> String => table catalog (may be null)</li>
    * <li><b>TABLE_SCHEM</b> String => table schema (may be null)</li>
    * <li><b>TABLE_NAME</b> String => table name</li>
    * <li><b>NON_UNIQUE</b> boolean => Can index values be non-unique?
    *        false when TYPE is tableIndexStatistic</li>
    * <li><b>INDEX_QUALIFIER</b> String => index catalog (may be null);
    *        null when TYPE is tableIndexStatistic</li>
    * <li><b>INDEX_NAME</b> String => index name; null when TYPE is
    *        tableIndexStatistic</li>
    * <li><b>TYPE</b> short => index type:
    *    <ul>
    *    <li><b>tableIndexStatistic</b> - this identifies table
    *           statistics that are returned in conjuction with a
    *           table's index descriptions</li>
    *    <li><b>tableIndexClustered</b> - this is a clustered index</li>
    *    <li><b>tableIndexHashed</b> - this is a hashed index</li>
    *    <li><b>tableIndexOther</b> - this is some other style of index</li>
    *    </ul></li>
    * <li><b>ORDINAL_POSITION</b> short => column sequence number within
    *        index; zero when TYPE is tableIndexStatistic</li>
    * <li><b>COLUMN_NAME</b> String => column name; null when TYPE is
    *        tableIndexStatistic</li>
    * <li><b>ASC_OR_DESC</b> String => column sort sequence, "A" =>
    *        ascending, "D" => descending, may be null if sort sequence
    *        is not supported; null when TYPE is tableIndexStatistic</li>
    * <li><b>CARDINALITY</b> int => When TYPE is tableIndexStatistic,
    *        then this is the number of rows in the table; otherwise,
    *        it is the number of unique values in the index.</li>
    * <li><b>PAGES</b> int => When TYPE is tableIndexStatisic then
    *        this is the number of pages used for the table, otherwise
    *        it is the number of pages used for the current index.</li>
    * <li><b>FILTER_CONDITION</b> String => Filter condition, if any.
    *        (may be null)</li>
    * </ol>
    *
    * @param   catalog               the directory containing the file
    *
    * @param   schema               must be ""
    *
    * @param   table                a file name
    *
    * @param   unique               when <code>true</code>, return only
    *                               indices for unique values; when
    *                               <code>false</code>, return indices
    *                               regardless of whether unique or not
    *
    * @param   approximate          when <code>true</code>, result is
    *                               allowed to reflect approximate or out
    *                               of data values; when <code>false</code>,
    *                               results are requested to be accurate
    *
    * @return   ResultSet - each row is an index
    *                               column description
    *
    * @throws   SQLException      if an error occurs
    */
   public java.sql.ResultSet getIndexInfo(
                                          java.lang.String catalog,
                                          java.lang.String schema,
                                          java.lang.String table,
                                          boolean unique,
                                          boolean approximate)
                                          throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * JDBC 2.0 Does the database support the given result set type?
    *
    * @param   type            defined in java.sql.ResultSet
    *
    * @return   <code>true</code> if so;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean supportsResultSetType(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Does the database support the concurrency type in combination
    * with the given result set type?
    *
    * @param   type               defined in java.sql.ResultSet
    *
    * @param   concurrency       type defined in java.sql.ResultSet
    *
    * @return   <code>true</code> if so;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean supportsResultSetConcurrency( int type,
                                               int concurrency)
                                               throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether a result set's own updates are visible.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if updates are
    *                            visible for the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean ownUpdatesAreVisible(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether a result set's own deletes are visible.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if deletes are
    *                            visible for the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean ownDeletesAreVisible(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether a result set's own inserts are visible.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if inserts are
    *                            visible for the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean ownInsertsAreVisible(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether updates made by others are visible.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if updates made by
    *                            others are visible for the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean othersUpdatesAreVisible(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether deletes made by others are visible.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if deletes made by
    *                            others are visible for the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean othersDeletesAreVisible(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether inserts made by others are visible.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if inserts made by
    *                            others are visible for the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean othersInsertsAreVisible(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether or not a visible row update can be
    * detected by calling the method ResultSet.rowUpdated.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if changes are
    *                            detected by the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean updatesAreDetected(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether or not a visible row delete can be
    * detected by calling the method ResultSet.rowDeleted.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if changes are
    *                            detected by the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean deletesAreDetected(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether or not a visible row insert can be
    * detected by calling the method ResultSet.rowInsertd.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if changes are
    *                            detected by the result set type;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean insertsAreDetected(int type)
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Indicates whether the driver supports batch updates.
    *
    * @param   type               set type, i.e. ResultSet.TYPE_XXX
    *
    * @return   <code>true</code> if the driver
    *                            supports batch updates;
    *                            <code>false</code> otherwise
    *
    * @throws   SQLException   if a database access error occurs
    */
   public boolean supportsBatchUpdates()
   throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Gets a description of the user-defined types defined in a
    * particular schema. Schema-specific UDTs may have type JAVA_OBJECT,
    * STRUCT, or DISTINCT.
    * <p>
    * Only types matching the catalog, schema, type name and type criteria
    * are returned. They are ordered by DATA_TYPE, TYPE_SCHEM and TYPE_NAME.
    * The type name parameter may be a fully-qualified name. In this case,
    * the catalog and schemaPattern parameters are ignored.
    * <p>
    * Each type description has the following columns:
    * <p>
    * <ol>
    * <li><b>TYPE_CAT</b> String => the type's catalog (may be null)</li>
    * <li><b>TYPE_SCHEM</b> String => type's schema (may be null)</li>
    * <li><b>TYPE_NAME</b> String => type name</li>
    * <li><b>CLASS_NAME</b> String => Java class name</li>
    * <li><b>DATA_TYPE</b> String => type value defined in java.sql.Types.
    *     One of JAVA_OBJECT, STRUCT, or DISTINCT</li>
    * <li><b>REMARKS</b> String => explanatory comment on the type</li>
    * </ol>
    * <EM>Note:</EM> UDTs are not supported, thus an empty result set
    * is always returned.
    *
    * @param   catalog            a catalog name; "" retrieves those without
    *                            a catalog; <code>null</code> means drop
    *                            catalog name from the selection criteria
    *
    * @param   schemaPattern      a schema name pattern; "" retrieves those
    *                            without a schema
    *
    * @param   typeNamePattern   a type name pattern; may be a
    *                            fully-qualified name
    *
    * @param   types             a list of user-named types to include
    *                            (JAVA_OBJECT, STRUCT, or DISTINCT);
    *                            <code>null</code> returns all types
    *
    * @return   ResultSet - each row is a type description
    *
    * @throws   SQLException   if a database access error occurs
    */
   public java.sql.ResultSet getUDTs(  java.lang.String catalog,
                                     java.lang.String schemaPattern,
                                     java.lang.String typeNamePattern,
                                     int[] types)
                                     throws SQLException
   {
      return new PSResultSet();
   }

   /**
    * JDBC 2.0 Retrieves the connection that produced this metadata object.
    *
    * @return   the connection that produced this metadata object
    */
   public java.sql.Connection getConnection()
      throws SQLException
   {
      return m_conn;
   }

   /**
    *  The connection that produced this object
    */
   protected PSFileSystemConnection m_conn;

   /**
    * The result set meta data objects for the different kinds of
    * results
    */
   protected static final PSResultSetMetaData ms_getCatalogsRSMeta
      = new PSResultSetMetaData();
   protected static final PSResultSetMetaData ms_getTablesRSMeta
      = new PSResultSetMetaData();
   protected static final PSResultSetMetaData ms_getTableTypesRSMeta
      = new PSResultSetMetaData();
   protected static final PSResultSetMetaData ms_getColumnsRSMeta
      = new PSResultSetMetaData();
   protected static final PSResultSetMetaData ms_getPrimaryKeysRSMeta
      = new PSResultSetMetaData();

   static
   {
      // result set meta data for getCatalogs
      ms_getCatalogsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_CAT",
         java.sql.Types.VARCHAR,
         255)
      );

      // result set meta data for getTables
      ms_getTablesRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_CAT",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getTablesRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_SCHEM",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getTablesRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_NAME",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getTablesRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_TYPE",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getTablesRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("REMARKS",
         java.sql.Types.VARCHAR,
         255)
      );

      // result set meta data for getTableTypes
      ms_getTableTypesRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_TYPE",
         java.sql.Types.VARCHAR,
         255)
      );

      // result set meta data for getColumns
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_CAT",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_SCHEM",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_NAME",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("COLUMN_NAME",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("DATA_TYPE",
         java.sql.Types.SMALLINT,
         2)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TYPE_NAME",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("COLUMN_SIZE",
         java.sql.Types.INTEGER,
         4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("BUFFER_LENGTH",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("DECIMAL_DIGITS",
         java.sql.Types.INTEGER,
         4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("NUM_PREC_RADIX",
         java.sql.Types.INTEGER,
         4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("NULLABLE",
         java.sql.Types.INTEGER,
         4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("REMARKS",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("COLUMN_DEF",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("SQL_DATA_TYPE",
         java.sql.Types.INTEGER,
         4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("SQL_DATETIME_SUB",
         java.sql.Types.INTEGER,
         4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("CHAR_OCTET_LENGTH",
         java.sql.Types.INTEGER,
         4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("ORDINAL_POSITION",
         java.sql.Types.INTEGER,
       4)
      );
      ms_getColumnsRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("IS_NULLABLE",
         java.sql.Types.VARCHAR,
         255)
      );

      // result set meta data for getPrimaryKeys
      ms_getPrimaryKeysRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_CAT",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getPrimaryKeysRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_SCHEM",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getPrimaryKeysRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("TABLE_NAME",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getPrimaryKeysRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("COLUMN_NAME",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getPrimaryKeysRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("KEY_SEQ",
         java.sql.Types.VARCHAR,
         255)
      );
      ms_getPrimaryKeysRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("PK_NAME",
         java.sql.Types.VARCHAR,
         255)
      );

   } // end static init

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
   public ResultSet getAttributes(
      String catalog,
      String schemaPattern,
      String typeNamePattern,
      String attributeNamePattern)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getDatabaseMajorVersion()
    */
   public int getDatabaseMajorVersion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getDatabaseMinorVersion()
    */
   public int getDatabaseMinorVersion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getJDBCMajorVersion()
    */
   public int getJDBCMajorVersion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getJDBCMinorVersion()
    */
   public int getJDBCMinorVersion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getResultSetHoldability()
    */
   public int getResultSetHoldability() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getSQLStateType()
    */
   public int getSQLStateType() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String, java.lang.String, java.lang.String)
    */
   public ResultSet getSuperTables(
      String catalog,
      String schemaPattern,
      String tableNamePattern)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String, java.lang.String, java.lang.String)
    */
   public ResultSet getSuperTypes(
      String catalog,
      String schemaPattern,
      String typeNamePattern)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#locatorsUpdateCopy()
    */
   public boolean locatorsUpdateCopy() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
    */
   public boolean supportsGetGeneratedKeys() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#supportsMultipleOpenResults()
    */
   public boolean supportsMultipleOpenResults() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#supportsNamedParameters()
    */
   public boolean supportsNamedParameters() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
    */
   public boolean supportsResultSetHoldability(int holdability)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#supportsSavepoints()
    */
   public boolean supportsSavepoints() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.DatabaseMetaData#supportsStatementPooling()
    */
   public boolean supportsStatementPooling() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean autoCommitFailureClosesAllResultSets() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public ResultSet getClientInfoProperties() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public ResultSet getFunctionColumns(String catalog, String schemaPattern,
         String functionNamePattern, String columnNamePattern)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public ResultSet getFunctions(String catalog, String schemaPattern,
         String functionNamePattern) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public RowIdLifetime getRowIdLifetime() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public ResultSet getSchemas(String catalog, String schemaPattern)
         throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
         String columnNamePattern) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public boolean generatedKeyAlwaysReturned() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

}
