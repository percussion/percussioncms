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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * The PSDatabasePoolDatabaseMetaData class is used as a wrapper on top of
 * a java.sql.DatabaseMetaData implementation. We've found that various
 * methods, such as getTables require the parameters to be setup just right
 * or else the call will not return the appropriate data. To avoid these
 * problems, we've created this wrapper which is used to perform the
 * appropriate parameter fixup.
 * <P>
 * This class is used to fix bug id Rx-99-11-0029
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSDatabasePoolDatabaseMetaData implements DatabaseMetaData
{
   public PSDatabasePoolDatabaseMetaData(DatabaseMetaData md)
   {
      super();
      m_md = md;

      m_flags = 0;
      try {
         if (m_md.supportsCatalogsInDataManipulation())
            m_flags |= FLAGS_SUPPORTS_CATALOG;
      } catch (java.sql.SQLException e) {
         // we will ignore these for now
      }

      try {
         if (m_md.supportsSchemasInDataManipulation())
            m_flags |= FLAGS_SUPPORTS_SCHEMA;
      } catch (java.sql.SQLException e) {
         // we will ignore these for now
      }

      try {
         if (m_md.storesUpperCaseIdentifiers())
            m_flags |= FLAGS_STORES_UPPERCASE;
         else if (m_md.storesLowerCaseIdentifiers())
            m_flags |= FLAGS_STORES_LOWERCASE;
      } catch (java.sql.SQLException e) {
         // we will ignore these for now
      }
   }

   //----------------------------------------------------------------------
   // First, a variety of minor information about the target database.

   /**
    * Can all the procedures returned by getProcedures be called by the
    * current user?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean allProceduresAreCallable() throws SQLException
   {
      return m_md.allProceduresAreCallable();
   }

   /**
    * Can all the tables returned by getTable be SELECTed by the
    * current user?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean allTablesAreSelectable() throws SQLException
   {
      return m_md.allTablesAreSelectable();
   }

   /**
    * What's the url for this database?
    *
    * @return the url or null if it cannot be generated
    * @exception SQLException if a database access error occurs
    */
   public String getURL() throws SQLException
   {
      return m_md.getURL();
   }

   /**
    * What's our user name as known to the database?
    *
    * @return our database user name
    * @exception SQLException if a database access error occurs
    */
   public String getUserName() throws SQLException
   {
      return m_md.getUserName();
   }

   /**
    * Is the database in read-only mode?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean isReadOnly() throws SQLException
   {
      return m_md.isReadOnly();
   }

   /**
    * Are NULL values sorted high?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean nullsAreSortedHigh() throws SQLException
   {
      return m_md.nullsAreSortedHigh();
   }

   /**
    * Are NULL values sorted low?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean nullsAreSortedLow() throws SQLException
   {
      return m_md.nullsAreSortedLow();
   }

   /**
    * Are NULL values sorted at the start regardless of sort order?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean nullsAreSortedAtStart() throws SQLException
   {
      return m_md.nullsAreSortedAtStart();
   }

   /**
    * Are NULL values sorted at the end regardless of sort order?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean nullsAreSortedAtEnd() throws SQLException
   {
      return m_md.nullsAreSortedAtEnd();
   }

   /**
    * What's the name of this database product?
    *
    * @return database product name
    * @exception SQLException if a database access error occurs
    */
   public String getDatabaseProductName() throws SQLException
   {
      return m_md.getDatabaseProductName();
   }

   /**
    * What's the version of this database product?
    *
    * @return database version
    * @exception SQLException if a database access error occurs
    */
   public String getDatabaseProductVersion() throws SQLException
   {
      return m_md.getDatabaseProductVersion();
   }

   /**
    * What's the name of this JDBC driver?
    *
    * @return JDBC driver name
    * @exception SQLException if a database access error occurs
    */
   public String getDriverName() throws SQLException
   {
      return m_md.getDriverName();
   }

   /**
    * What's the version of this JDBC driver?
    *
    * @return JDBC driver version
    * @exception SQLException if a database access error occurs
    */
   public String getDriverVersion() throws SQLException
   {
      return m_md.getDriverVersion();
   }

   /**
    * What's this JDBC driver's major version number?
    *
    * @return JDBC driver major version
    */
   public int getDriverMajorVersion()
   {
      return m_md.getDriverMajorVersion();
   }

   /**
    * What's this JDBC driver's minor version number?
    *
    * @return JDBC driver minor version number
    */
   public int getDriverMinorVersion()
   {
      return m_md.getDriverMinorVersion();
   }

   /**
    * Does the database store tables in a local file?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean usesLocalFiles() throws SQLException
   {
      return m_md.usesLocalFiles();
   }

   /**
    * Does the database use a file for each table?
    *
    * @return true if the database uses a local file for each table
    * @exception SQLException if a database access error occurs
    */
   public boolean usesLocalFilePerTable() throws SQLException
   {
      return m_md.usesLocalFilePerTable();
   }

   /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case sensitive and as a result store them in mixed case?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver will always return false.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsMixedCaseIdentifiers() throws SQLException
   {
      return m_md.supportsMixedCaseIdentifiers();
   }

   /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in upper case?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean storesUpperCaseIdentifiers() throws SQLException
   {
      return m_md.storesUpperCaseIdentifiers();
   }

   /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in lower case?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean storesLowerCaseIdentifiers() throws SQLException
   {
      return m_md.storesLowerCaseIdentifiers();
   }

   /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in mixed case?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean storesMixedCaseIdentifiers() throws SQLException
   {
      return m_md.storesMixedCaseIdentifiers();
   }

   /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case sensitive and as a result store them in mixed case?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver will always return true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
   {
      return m_md.supportsMixedCaseQuotedIdentifiers();
   }

   /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in upper case?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
   {
      return m_md.storesUpperCaseQuotedIdentifiers();
   }

   /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in lower case?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
   {
      return m_md.storesLowerCaseQuotedIdentifiers();
   }

   /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in mixed case?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
   {
      return m_md.storesMixedCaseQuotedIdentifiers();
   }

   /**
    * What's the string used to quote SQL identifiers?
    * This returns a space " " if identifier quoting isn't supported.
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> 
    * driver always uses a double quote character.
    *
    * @return the quoting string
    * @exception SQLException if a database access error occurs
    */
   public String getIdentifierQuoteString() throws SQLException
   {
      return m_md.getIdentifierQuoteString();
   }

   /**
    * Gets a comma-separated list of all a database's SQL keywords
    * that are NOT also SQL92 keywords.
    *
    * @return the list 
    * @exception SQLException if a database access error occurs
    */
   public String getSQLKeywords() throws SQLException
   {
      return m_md.getSQLKeywords();
   }

   /**
    * Gets a comma-separated list of math functions.  These are the 
    * X/Open CLI math function names used in the JDBC function escape 
    * clause.
    *
    * @return the list
    * @exception SQLException if a database access error occurs
    */
   public String getNumericFunctions() throws SQLException
   {
      return m_md.getNumericFunctions();
   }

   /**
    * Gets a comma-separated list of string functions.  These are the 
    * X/Open CLI string function names used in the JDBC function escape 
    * clause.
    *
    * @return the list
    * @exception SQLException if a database access error occurs
    */
   public String getStringFunctions() throws SQLException
   {
      return m_md.getStringFunctions();
   }

   /**
    * Gets a comma-separated list of system functions.  These are the 
    * X/Open CLI system function names used in the JDBC function escape 
    * clause.
    *
    * @return the list
    * @exception SQLException if a database access error occurs
    */
   public String getSystemFunctions() throws SQLException
   {
      return m_md.getSystemFunctions();
   }

   /**
    * Gets a comma-separated list of time and date functions.
    *
    * @return the list
    * @exception SQLException if a database access error occurs
    */
   public String getTimeDateFunctions() throws SQLException
   {
      return m_md.getTimeDateFunctions();
   }

   /**
    * Gets the string that can be used to escape wildcard characters.
    * This is the string that can be used to escape '_' or '%' in
    * the string pattern style catalog search parameters.
    *
    * <P>The '_' character represents any single character.
    * <P>The '%' character represents any sequence of zero or 
    * more characters.
    *
    * @return the string used to escape wildcard characters
    * @exception SQLException if a database access error occurs
    */
   public String getSearchStringEscape() throws SQLException
   {
      return m_md.getSearchStringEscape();
   }

   /**
    * Gets all the "extra" characters that can be used in unquoted
    * identifier names (those beyond a-z, A-Z, 0-9 and _).
    *
    * @return the string containing the extra characters 
    * @exception SQLException if a database access error occurs
    */
   public String getExtraNameCharacters() throws SQLException
   {
      return m_md.getExtraNameCharacters();
   }

   //--------------------------------------------------------------------
   // Functions describing which features are supported.

   /**
    * Is "ALTER TABLE" with add column supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsAlterTableWithAddColumn() throws SQLException
   {
      return m_md.supportsAlterTableWithAddColumn();
   }

   /**
    * Is "ALTER TABLE" with drop column supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsAlterTableWithDropColumn() throws SQLException
   {
      return m_md.supportsAlterTableWithDropColumn();
   }

   /**
    * Is column aliasing supported? 
    *
    * <P>If so, the SQL AS clause can be used to provide names for
    * computed columns or to provide alias names for columns as
    * required.
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsColumnAliasing() throws SQLException
   {
      return m_md.supportsColumnAliasing();
   }

   /**
    * Are concatenations between NULL and non-NULL values NULL?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean nullPlusNonNullIsNull() throws SQLException
   {
      return m_md.nullPlusNonNullIsNull();
   }

   /**
    * Is the CONVERT function between SQL types supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsConvert() throws SQLException
   {
      return m_md.supportsConvert();
   }

   /**
    * Is CONVERT between the given SQL types supported?
    *
    * @param fromType the type to convert from
    * @param toType the type to convert to    
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsConvert(int fromType, int toType) throws SQLException
   {
      return m_md.supportsConvert(fromType, toType);
   }

   /**
    * Are table correlation names supported?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsTableCorrelationNames() throws SQLException
   {
      return m_md.supportsTableCorrelationNames();
   }

   /**
    * If table correlation names are supported, are they restricted
    * to be different from the names of the tables?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsDifferentTableCorrelationNames() throws SQLException
   {
      return m_md.supportsDifferentTableCorrelationNames();
   }

   /**
    * Are expressions in "ORDER BY" lists supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsExpressionsInOrderBy() throws SQLException
   {
      return m_md.supportsExpressionsInOrderBy();
   }

   /**
    * Can an "ORDER BY" clause use columns not in the SELECT statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsOrderByUnrelated() throws SQLException
   {
      return m_md.supportsOrderByUnrelated();
   }

   /**
    * Is some form of "GROUP BY" clause supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsGroupBy() throws SQLException
   {
      return m_md.supportsGroupBy();
   }

   /**
    * Can a "GROUP BY" clause use columns not in the SELECT?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsGroupByUnrelated() throws SQLException
   {
      return m_md.supportsGroupByUnrelated();
   }

   /**
    * Can a "GROUP BY" clause add columns not in the SELECT
    * provided it specifies all the columns in the SELECT?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsGroupByBeyondSelect() throws SQLException
   {
      return m_md.supportsGroupByBeyondSelect();
   }

   /**
    * Is the escape character in "LIKE" clauses supported?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsLikeEscapeClause() throws SQLException
   {
      return m_md.supportsLikeEscapeClause();
   }

   /**
    * Are multiple ResultSets from a single execute supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsMultipleResultSets() throws SQLException
   {
      return m_md.supportsMultipleResultSets();
   }

   /**
    * Can we have multiple transactions open at once (on different
    * connections)?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsMultipleTransactions() throws SQLException
   {
      return m_md.supportsMultipleTransactions();
   }

   /**
    * Can columns be defined as non-nullable?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsNonNullableColumns() throws SQLException
   {
      return m_md.supportsNonNullableColumns();
   }

   /**
    * Is the ODBC Minimum SQL grammar supported?
    *
    * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsMinimumSQLGrammar() throws SQLException
   {
      return m_md.supportsMinimumSQLGrammar();
   }

   /**
    * Is the ODBC Core SQL grammar supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsCoreSQLGrammar() throws SQLException
   {
      return m_md.supportsCoreSQLGrammar();
   }

   /**
    * Is the ODBC Extended SQL grammar supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsExtendedSQLGrammar() throws SQLException
   {
      return m_md.supportsExtendedSQLGrammar();
   }

   /**
    * Is the ANSI92 entry level SQL grammar supported?
    *
    * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsANSI92EntryLevelSQL() throws SQLException
   {
      return m_md.supportsANSI92EntryLevelSQL();
   }

   /**
    * Is the ANSI92 intermediate SQL grammar supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsANSI92IntermediateSQL() throws SQLException
   {
      return m_md.supportsANSI92IntermediateSQL();
   }

   /**
    * Is the ANSI92 full SQL grammar supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsANSI92FullSQL() throws SQLException
   {
      return m_md.supportsANSI92FullSQL();
   }

   /**
    * Is the SQL Integrity Enhancement Facility supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsIntegrityEnhancementFacility() throws SQLException
   {
      return m_md.supportsIntegrityEnhancementFacility();
   }

   /**
    * Is some form of outer join supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsOuterJoins() throws SQLException
   {
      return m_md.supportsOuterJoins();
   }

   /**
    * Are full nested outer joins supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsFullOuterJoins() throws SQLException
   {
      return m_md.supportsFullOuterJoins();
   }

   /**
    * Is there limited support for outer joins?  (This will be true
    * if supportFullOuterJoins is true.)
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsLimitedOuterJoins() throws SQLException
   {
      return m_md.supportsLimitedOuterJoins();
   }

   /**
    * What's the database vendor's preferred term for "schema"?
    *
    * @return the vendor term
    * @exception SQLException if a database access error occurs
    */
   public String getSchemaTerm() throws SQLException
   {
      return m_md.getSchemaTerm();
   }

   /**
    * What's the database vendor's preferred term for "procedure"?
    *
    * @return the vendor term
    * @exception SQLException if a database access error occurs
    */
   public String getProcedureTerm() throws SQLException
   {
      return m_md.getProcedureTerm();
   }

   /**
    * What's the database vendor's preferred term for "catalog"?
    *
    * @return the vendor term
    * @exception SQLException if a database access error occurs
    */
   public String getCatalogTerm() throws SQLException
   {
      return m_md.getCatalogTerm();
   }

   /**
    * Does a catalog appear at the start of a qualified table name?
    * (Otherwise it appears at the end)
    *
    * @return true if it appears at the start 
    * @exception SQLException if a database access error occurs
    */
   public boolean isCatalogAtStart() throws SQLException
   {
      return m_md.isCatalogAtStart();
   }

   /**
    * What's the separator between catalog and table name?
    *
    * @return the separator string
    * @exception SQLException if a database access error occurs
    */
   public String getCatalogSeparator() throws SQLException
   {
      return m_md.getCatalogSeparator();
   }

   /**
    * Can a schema name be used in a data manipulation statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSchemasInDataManipulation() throws SQLException
   {
      return m_md.supportsSchemasInDataManipulation();
   }

   /**
    * Can a schema name be used in a procedure call statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSchemasInProcedureCalls() throws SQLException
   {
      return m_md.supportsSchemasInProcedureCalls();
   }

   /**
    * Can a schema name be used in a table definition statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSchemasInTableDefinitions() throws SQLException
   {
      return m_md.supportsSchemasInTableDefinitions();
   }

   /**
    * Can a schema name be used in an index definition statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSchemasInIndexDefinitions() throws SQLException
   {
      return m_md.supportsSchemasInIndexDefinitions();
   }

   /**
    * Can a schema name be used in a privilege definition statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
   {
      return m_md.supportsSchemasInPrivilegeDefinitions();
   }

   /**
    * Can a catalog name be used in a data manipulation statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsCatalogsInDataManipulation() throws SQLException
   {
      return m_md.supportsCatalogsInDataManipulation();
   }

   /**
    * Can a catalog name be used in a procedure call statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsCatalogsInProcedureCalls() throws SQLException
   {
      return m_md.supportsCatalogsInProcedureCalls();
   }

   /**
    * Can a catalog name be used in a table definition statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsCatalogsInTableDefinitions() throws SQLException
   {
      return m_md.supportsCatalogsInTableDefinitions();
   }

   /**
    * Can a catalog name be used in an index definition statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsCatalogsInIndexDefinitions() throws SQLException
   {
      return m_md.supportsCatalogsInIndexDefinitions();
   }

   /**
    * Can a catalog name be used in a privilege definition statement?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
   {
      return m_md.supportsCatalogsInPrivilegeDefinitions();
   }


   /**
    * Is positioned DELETE supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsPositionedDelete() throws SQLException
   {
      return m_md.supportsPositionedDelete();
   }

   /**
    * Is positioned UPDATE supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsPositionedUpdate() throws SQLException
   {
      return m_md.supportsPositionedUpdate();
   }

   /**
    * Is SELECT for UPDATE supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSelectForUpdate() throws SQLException
   {
      return m_md.supportsSelectForUpdate();
   }

   /**
    * Are stored procedure calls using the stored procedure escape
    * syntax supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsStoredProcedures() throws SQLException
   {
      return m_md.supportsStoredProcedures();
   }

   /**
    * Are subqueries in comparison expressions supported?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSubqueriesInComparisons() throws SQLException
   {
      return m_md.supportsSubqueriesInComparisons();
   }

   /**
    * Are subqueries in 'exists' expressions supported?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSubqueriesInExists() throws SQLException
   {
      return m_md.supportsSubqueriesInExists();
   }

   /**
    * Are subqueries in 'in' statements supported?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSubqueriesInIns() throws SQLException
   {
      return m_md.supportsSubqueriesInIns();
   }

   /**
    * Are subqueries in quantified expressions supported?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsSubqueriesInQuantifieds() throws SQLException
   {
      return m_md.supportsSubqueriesInQuantifieds();
   }

   /**
    * Are correlated subqueries supported?
    *
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsCorrelatedSubqueries() throws SQLException
   {
      return m_md.supportsCorrelatedSubqueries();
   }

   /**
    * Is SQL UNION supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsUnion() throws SQLException
   {
      return m_md.supportsUnion();
   }

   /**
    * Is SQL UNION ALL supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsUnionAll() throws SQLException
   {
      return m_md.supportsUnionAll();
   }

   /**
    * Can cursors remain open across commits? 
    * 
    * @return <code>true</code> if cursors always remain open;
    *      <code>false</code> if they might not remain open
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsOpenCursorsAcrossCommit() throws SQLException
   {
      return m_md.supportsOpenCursorsAcrossCommit();
   }

   /**
    * Can cursors remain open across rollbacks?
    * 
    * @return <code>true</code> if cursors always remain open;
    *      <code>false</code> if they might not remain open
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsOpenCursorsAcrossRollback() throws SQLException
   {
      return m_md.supportsOpenCursorsAcrossRollback();
   }

   /**
    * Can statements remain open across commits?
    * 
    * @return <code>true</code> if statements always remain open;
    *      <code>false</code> if they might not remain open
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsOpenStatementsAcrossCommit() throws SQLException
   {
      return m_md.supportsOpenStatementsAcrossCommit();
   }

   /**
    * Can statements remain open across rollbacks?
    * 
    * @return <code>true</code> if statements always remain open;
    *      <code>false</code> if they might not remain open
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsOpenStatementsAcrossRollback() throws SQLException
   {
      return m_md.supportsOpenStatementsAcrossRollback();
   }

   

   //----------------------------------------------------------------------
   // The following group of methods exposes various limitations 
   // based on the target database with the current driver.
   // Unless otherwise specified, a result of zero means there is no
   // limit, or the limit is not known.
   
   /**
    * How many hex characters can you have in an inline binary literal?
    *
    * @return max binary literal length in hex characters;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxBinaryLiteralLength() throws SQLException
   {
      return m_md.getMaxBinaryLiteralLength();
   }

   /**
    * What's the max length for a character literal?
    *
    * @return max literal length;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxCharLiteralLength() throws SQLException
   {
      return m_md.getMaxCharLiteralLength();
   }

   /**
    * What's the limit on column name length?
    *
    * @return max column name length;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxColumnNameLength() throws SQLException
   {
      return m_md.getMaxColumnNameLength();
   }

   /**
    * What's the maximum number of columns in a "GROUP BY" clause?
    *
    * @return max number of columns;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxColumnsInGroupBy() throws SQLException
   {
      return m_md.getMaxColumnsInGroupBy();
   }

   /**
    * What's the maximum number of columns allowed in an index?
    *
    * @return max number of columns;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxColumnsInIndex() throws SQLException
   {
      return m_md.getMaxColumnsInIndex();
   }

   /**
    * What's the maximum number of columns in an "ORDER BY" clause?
    *
    * @return max number of columns;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxColumnsInOrderBy() throws SQLException
   {
      return m_md.getMaxColumnsInOrderBy();
   }

   /**
    * What's the maximum number of columns in a "SELECT" list?
    *
    * @return max number of columns;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxColumnsInSelect() throws SQLException
   {
      return m_md.getMaxColumnsInSelect();
   }

   /**
    * What's the maximum number of columns in a table?
    *
    * @return max number of columns;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxColumnsInTable() throws SQLException
   {
      return m_md.getMaxColumnsInTable();
   }

   /**
    * How many active connections can we have at a time to this database?
    *
    * @return max number of active connections;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxConnections() throws SQLException
   {
      return m_md.getMaxConnections();
   }

   /**
    * What's the maximum cursor name length?
    *
    * @return max cursor name length in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxCursorNameLength() throws SQLException
   {
      return m_md.getMaxCursorNameLength();
   }

   /**
    * What's the maximum length of an index (in bytes)?   
    *
    * @return max index length in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxIndexLength() throws SQLException
   {
      return m_md.getMaxIndexLength();
   }

   /**
    * What's the maximum length allowed for a schema name?
    *
    * @return max name length in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxSchemaNameLength() throws SQLException
   {
      return m_md.getMaxSchemaNameLength();
   }

   /**
    * What's the maximum length of a procedure name?
    *
    * @return max name length in bytes; 
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxProcedureNameLength() throws SQLException
   {
      return m_md.getMaxProcedureNameLength();
   }

   /**
    * What's the maximum length of a catalog name?
    *
    * @return max name length in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxCatalogNameLength() throws SQLException
   {
      return m_md.getMaxCatalogNameLength();
   }

   /**
    * What's the maximum length of a single row?
    *
    * @return max row size in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxRowSize() throws SQLException
   {
      return m_md.getMaxRowSize();
   }

   /**
    * Did getMaxRowSize() include LONGVARCHAR and LONGVARBINARY
    * blobs?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
   {
      return m_md.doesMaxRowSizeIncludeBlobs();
   }

   /**
    * What's the maximum length of a SQL statement?
    *
    * @return max length in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxStatementLength() throws SQLException
   {
      return m_md.getMaxStatementLength();
   }

   /**
    * How many active statements can we have open at one time to this
    * database?
    *
    * @return the maximum number of statements that can be open at one time;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxStatements() throws SQLException
   {
      return m_md.getMaxStatements();
   }

   /**
    * What's the maximum length of a table name?
    *
    * @return max name length in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxTableNameLength() throws SQLException
   {
      return m_md.getMaxTableNameLength();
   }

   /**
    * What's the maximum number of tables in a SELECT statement?
    *
    * @return the maximum number of tables allowed in a SELECT statement;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxTablesInSelect() throws SQLException
   {
      return m_md.getMaxTablesInSelect();
   }

   /**
    * What's the maximum length of a user name?
    *
    * @return max user name length  in bytes;
    *     a result of zero means that there is no limit or the limit is not known
    * @exception SQLException if a database access error occurs
    */
   public int getMaxUserNameLength() throws SQLException
   {
      return m_md.getMaxUserNameLength();
   }

   //----------------------------------------------------------------------

   /**
    * What's the database's default transaction isolation level?  The
    * values are defined in <code>java.sql.Connection</code>.
    *
    * @return the default isolation level 
    * @exception SQLException if a database access error occurs
    * @see Connection
    */
   public int getDefaultTransactionIsolation() throws SQLException
   {
      return m_md.getDefaultTransactionIsolation();
   }

   /**
    * Are transactions supported? If not, invoking the method
    * <code>commit</code> is a noop and the
    * isolation level is TRANSACTION_NONE.
    *
    * @return <code>true</code> if transactions are supported; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsTransactions() throws SQLException
   {
      return m_md.supportsTransactions();
   }

   /**
    * Does this database support the given transaction isolation level?
    *
    * @param level the values are defined in <code>java.sql.Connection</code>
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    * @see Connection
    */
   public boolean supportsTransactionIsolationLevel(int level)
                     throws SQLException
   {
      return m_md.supportsTransactionIsolationLevel(level);
   }

   /**
    * Are both data definition and data manipulation statements
    * within a transaction supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsDataDefinitionAndDataManipulationTransactions()
                      throws SQLException
   {
      return m_md.supportsDataDefinitionAndDataManipulationTransactions();
   }
   /**
    * Are only data manipulation statements within a transaction
    * supported?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean supportsDataManipulationTransactionsOnly()
                     throws SQLException
   {
      return m_md.supportsDataManipulationTransactionsOnly();
   }
   /**
    * Does a data definition statement within a transaction force the
    * transaction to commit?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean dataDefinitionCausesTransactionCommit()
                     throws SQLException
   {
      return m_md.dataDefinitionCausesTransactionCommit();
   }
   /**
    * Is a data definition statement within a transaction ignored?
    *
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    */
   public boolean dataDefinitionIgnoredInTransactions()
                     throws SQLException
   {
      return m_md.dataDefinitionIgnoredInTransactions();
   }


   /**
    * Gets a description of the stored procedures available in a
    * catalog.
    *
    * <P>Only procedure descriptions matching the schema and
    * procedure name criteria are returned.  They are ordered by
    * PROCEDURE_SCHEM, and PROCEDURE_NAME.
    *
    * <P>Each procedure description has the the following columns:
    *  <OL>
    *   <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
    *   <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
    *   <LI><B>PROCEDURE_NAME</B> String => procedure name
    *  <LI> reserved for future use
    *  <LI> reserved for future use
    *  <LI> reserved for future use
    *   <LI><B>REMARKS</B> String => explanatory comment on the procedure
    *   <LI><B>PROCEDURE_TYPE</B> short => kind of procedure:
    *     <UL>
    *     <LI> procedureResultUnknown - May return a result
    *     <LI> procedureNoResult - Does not return a result
    *     <LI> procedureReturnsResult - Returns a result
    *     </UL>
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schemaPattern a schema name pattern; "" retrieves those
    * without a schema
    * @param procedureNamePattern a procedure name pattern 
    * @return ResultSet - each row is a procedure description 
    * @exception SQLException if a database access error occurs
    * @see #getSearchStringEscape 
    */
   public ResultSet getProcedures(String catalog, String schemaPattern,
         String procedureNamePattern) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schemaPattern = getFixedupSchema(schemaPattern);
      procedureNamePattern = getFixedupIdentifier(procedureNamePattern);

      return m_md.getProcedures(catalog, schemaPattern, procedureNamePattern);
   }

   /**
    * Gets a description of a catalog's stored procedure parameters
    * and result columns.
    *
    * <P>Only descriptions matching the schema, procedure and
    * parameter name criteria are returned.  They are ordered by
    * PROCEDURE_SCHEM and PROCEDURE_NAME. Within this, the return value,
    * if any, is first. Next are the parameter descriptions in call
    * order. The column descriptions follow in column number order.
    *
    * <P>Each row in the ResultSet is a parameter description or
    * column description with the following fields:
    *  <OL>
    *   <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
    *   <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
    *   <LI><B>PROCEDURE_NAME</B> String => procedure name
    *   <LI><B>COLUMN_NAME</B> String => column/parameter name 
    *   <LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
    *     <UL>
    *     <LI> procedureColumnUnknown - nobody knows
    *     <LI> procedureColumnIn - IN parameter
    *     <LI> procedureColumnInOut - INOUT parameter
    *     <LI> procedureColumnOut - OUT parameter
    *     <LI> procedureColumnReturn - procedure return value
    *     <LI> procedureColumnResult - result column in ResultSet
    *     </UL>
    *  <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
    *   <LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
    *  type name is fully qualified
    *   <LI><B>PRECISION</B> int => precision
    *   <LI><B>LENGTH</B> int => length in bytes of data
    *   <LI><B>SCALE</B> short => scale
    *   <LI><B>RADIX</B> short => radix
    *   <LI><B>NULLABLE</B> short => can it contain NULL?
    *     <UL>
    *     <LI> procedureNoNulls - does not allow NULL values
    *     <LI> procedureNullable - allows NULL values
    *     <LI> procedureNullableUnknown - nullability unknown
    *     </UL>
    *   <LI><B>REMARKS</B> String => comment describing parameter/column
    *  </OL>
    *
    * <P><B>Note:</B> Some databases may not return the column
    * descriptions for a procedure. Additional columns beyond
    * REMARKS can be defined by the database.
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schemaPattern a schema name pattern; "" retrieves those
    * without a schema 
    * @param procedureNamePattern a procedure name pattern 
    * @param columnNamePattern a column name pattern 
    * @return ResultSet - each row describes a stored procedure parameter or 
    *     column
    * @exception SQLException if a database access error occurs
    * @see #getSearchStringEscape 
    */
   public ResultSet getProcedureColumns(String catalog,
         String schemaPattern,
         String procedureNamePattern, 
         String columnNamePattern) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schemaPattern = getFixedupSchema(schemaPattern);
      procedureNamePattern = getFixedupIdentifier(procedureNamePattern);
      columnNamePattern = getFixedupIdentifier(columnNamePattern);

      return m_md.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
   }

   /**
    * Gets a description of tables available in a catalog.
    *
    * <P>Only table descriptions matching the catalog, schema, table
    * name and type criteria are returned.  They are ordered by
    * TABLE_TYPE, TABLE_SCHEM and TABLE_NAME.
    *
    * <P>Each table description has the following columns:
    *  <OL>
    *   <LI><B>TABLE_CAT</B> String => table catalog (may be null)
    *   <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
    *   <LI><B>TABLE_NAME</B> String => table name
    *   <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
    *         "VIEW",   "SYSTEM TABLE", "GLOBAL TEMPORARY", 
    *         "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
    *   <LI><B>REMARKS</B> String => explanatory comment on the table
    *  </OL>
    *
    * <P><B>Note:</B> Some databases may not return information for
    * all tables.
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schemaPattern a schema name pattern; "" retrieves those
    * without a schema
    * @param tableNamePattern a table name pattern 
    * @param types a list of table types to include; null returns all types 
    * @return ResultSet - each row is a table description
    * @exception SQLException if a database access error occurs
    * @see #getSearchStringEscape 
    */
   public ResultSet getTables(String catalog, String schemaPattern,
      String tableNamePattern, String types[]) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schemaPattern = getFixedupSchema(schemaPattern);
      tableNamePattern = getFixedupIdentifier(tableNamePattern);

      return m_md.getTables(catalog, schemaPattern, tableNamePattern, types);
   }

   /**
    * Gets the schema names available in this database.  The results
    * are ordered by schema name.
    *
    * <P>The schema column is:
    *  <OL>
    *   <LI><B>TABLE_SCHEM</B> String => schema name
    *  </OL>
    *
    * @return ResultSet - each row has a single String column that is a
    * schema name 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getSchemas() throws SQLException
   {
      return m_md.getSchemas();
   }

   /**
    * Gets the catalog names available in this database.  The results
    * are ordered by catalog name.
    *
    * <P>The catalog column is:
    *  <OL>
    *   <LI><B>TABLE_CAT</B> String => catalog name
    *  </OL>
    *
    * @return ResultSet - each row has a single String column that is a
    * catalog name 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getCatalogs() throws SQLException
   {
      return m_md.getCatalogs();
   }

   /**
    * Gets the table types available in this database.  The results
    * are ordered by table type.
    *
    * <P>The table type is:
    *  <OL>
    *   <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
    *         "VIEW",   "SYSTEM TABLE", "GLOBAL TEMPORARY", 
    *         "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
    *  </OL>
    *
    * @return ResultSet - each row has a single String column that is a
    * table type 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getTableTypes() throws SQLException
   {
      return m_md.getTableTypes();
   }

   /**
    * Gets a description of table columns available in 
    * the specified catalog.
    *
    * <P>Only column descriptions matching the catalog, schema, table
    * and column name criteria are returned.  They are ordered by
    * TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION.
    *
    * <P>Each column description has the following columns:
    *  <OL>
    *   <LI><B>TABLE_CAT</B> String => table catalog (may be null)
    *   <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
    *   <LI><B>TABLE_NAME</B> String => table name
    *   <LI><B>COLUMN_NAME</B> String => column name
    *   <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
    *   <LI><B>TYPE_NAME</B> String => Data source dependent type name,
    *  for a UDT the type name is fully qualified
    *   <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
    *      types this is the maximum number of characters, for numeric or
    *      decimal types this is precision.
    *   <LI><B>BUFFER_LENGTH</B> is not used.
    *   <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
    *   <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
    *   <LI><B>NULLABLE</B> int => is NULL allowed?
    *     <UL>
    *     <LI> columnNoNulls - might not allow NULL values
    *     <LI> columnNullable - definitely allows NULL values
    *     <LI> columnNullableUnknown - nullability unknown
    *     </UL>
    *   <LI><B>REMARKS</B> String => comment describing column (may be null)
    *    <LI><B>COLUMN_DEF</B> String => default value (may be null)
    *   <LI><B>SQL_DATA_TYPE</B> int => unused
    *   <LI><B>SQL_DATETIME_SUB</B> int => unused
    *   <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the 
    *      maximum number of bytes in the column
    *   <LI><B>ORDINAL_POSITION</B> int   => index of column in table 
    *     (starting at 1)
    *   <LI><B>IS_NULLABLE</B> String => "NO" means column definitely 
    *     does not allow NULL values; "YES" means the column might 
    *     allow NULL values.  An empty string means nobody knows.
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schemaPattern a schema name pattern; "" retrieves those
    * without a schema
    * @param tableNamePattern a table name pattern 
    * @param columnNamePattern a column name pattern 
    * @return ResultSet - each row is a column description
    * @exception SQLException if a database access error occurs
    * @see #getSearchStringEscape 
    */
   public ResultSet getColumns(String catalog, String schemaPattern,
      String tableNamePattern, String columnNamePattern)
               throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schemaPattern = getFixedupSchema(schemaPattern);
      tableNamePattern = getFixedupIdentifier(tableNamePattern);
      columnNamePattern = getFixedupIdentifier(columnNamePattern);

      return m_md.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
   }

   /**
    * Gets a description of the access rights for a table's columns.
    *
    * <P>Only privileges matching the column name criteria are
    * returned.  They are ordered by COLUMN_NAME and PRIVILEGE.
    *
    * <P>Each privilige description has the following columns:
    *  <OL>
    *   <LI><B>TABLE_CAT</B> String => table catalog (may be null)
    *   <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
    *   <LI><B>TABLE_NAME</B> String => table name
    *   <LI><B>COLUMN_NAME</B> String => column name
    *   <LI><B>GRANTOR</B> => grantor of access (may be null)
    *   <LI><B>GRANTEE</B> String => grantee of access
    *   <LI><B>PRIVILEGE</B> String => name of access (SELECT, 
    *     INSERT, UPDATE, REFRENCES, ...)
    *   <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted 
    *     to grant to others; "NO" if not; null if unknown 
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schema a schema name; "" retrieves those without a schema
    * @param table a table name
    * @param columnNamePattern a column name pattern 
    * @return ResultSet - each row is a column privilege description
    * @exception SQLException if a database access error occurs
    * @see #getSearchStringEscape 
    */
   public ResultSet getColumnPrivileges(String catalog, String schema,
      String table, String columnNamePattern) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schema = getFixedupSchema(schema);
      table = getFixedupIdentifier(table);
      columnNamePattern = getFixedupIdentifier(columnNamePattern);

      return m_md.getColumnPrivileges(catalog, schema, table, columnNamePattern);
   }

   /**
    * Gets a description of the access rights for each table available
    * in a catalog. Note that a table privilege applies to one or
    * more columns in the table. It would be wrong to assume that
    * this priviledge applies to all columns (this may be true for
    * some systems but is not true for all.)
    *
    * <P>Only privileges matching the schema and table name
    * criteria are returned.  They are ordered by TABLE_SCHEM,
    * TABLE_NAME, and PRIVILEGE.
    *
    * <P>Each privilige description has the following columns:
    *  <OL>
    *   <LI><B>TABLE_CAT</B> String => table catalog (may be null)
    *   <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
    *   <LI><B>TABLE_NAME</B> String => table name
    *   <LI><B>GRANTOR</B> => grantor of access (may be null)
    *   <LI><B>GRANTEE</B> String => grantee of access
    *   <LI><B>PRIVILEGE</B> String => name of access (SELECT, 
    *     INSERT, UPDATE, REFRENCES, ...)
    *   <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted 
    *     to grant to others; "NO" if not; null if unknown 
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schemaPattern a schema name pattern; "" retrieves those
    * without a schema
    * @param tableNamePattern a table name pattern 
    * @return ResultSet - each row is a table privilege description
    * @exception SQLException if a database access error occurs
    * @see #getSearchStringEscape 
    */
   public ResultSet getTablePrivileges(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schemaPattern = getFixedupSchema(schemaPattern);
      tableNamePattern = getFixedupIdentifier(tableNamePattern);

      return m_md.getTablePrivileges(catalog, schemaPattern, tableNamePattern);
   }

   /**
    * Gets a description of a table's optimal set of columns that
    * uniquely identifies a row. They are ordered by SCOPE.
    *
    * <P>Each column description has the following columns:
    *  <OL>
    *   <LI><B>SCOPE</B> short => actual scope of result
    *     <UL>
    *     <LI> bestRowTemporary - very temporary, while using row
    *     <LI> bestRowTransaction - valid for remainder of current transaction
    *     <LI> bestRowSession - valid for remainder of current session
    *     </UL>
    *   <LI><B>COLUMN_NAME</B> String => column name
    *   <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
    *   <LI><B>TYPE_NAME</B> String => Data source dependent type name,
    *  for a UDT the type name is fully qualified
    *   <LI><B>COLUMN_SIZE</B> int => precision
    *   <LI><B>BUFFER_LENGTH</B> int => not used
    *   <LI><B>DECIMAL_DIGITS</B> short    => scale
    *   <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column 
    *     like an Oracle ROWID
    *     <UL>
    *     <LI> bestRowUnknown - may or may not be pseudo column
    *     <LI> bestRowNotPseudo - is NOT a pseudo column
    *     <LI> bestRowPseudo - is a pseudo column
    *     </UL>
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schema a schema name; "" retrieves those without a schema
    * @param table a table name
    * @param scope the scope of interest; use same values as SCOPE
    * @param nullable include columns that are nullable?
    * @return ResultSet - each row is a column description 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getBestRowIdentifier(String catalog, String schema,
      String table, int scope, boolean nullable) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schema = getFixedupSchema(schema);
      table = getFixedupIdentifier(table);

      return m_md.getBestRowIdentifier(catalog, schema, table, scope, nullable);
   }
   
   /**
    * Gets a description of a table's columns that are automatically
    * updated when any value in a row is updated.  They are
    * unordered.
    *
    * <P>Each column description has the following columns:
    *  <OL>
    *   <LI><B>SCOPE</B> short => is not used
    *   <LI><B>COLUMN_NAME</B> String => column name
    *   <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
    *   <LI><B>TYPE_NAME</B> String => Data source dependent type name
    *   <LI><B>COLUMN_SIZE</B> int => precision
    *   <LI><B>BUFFER_LENGTH</B> int => length of column value in bytes
    *   <LI><B>DECIMAL_DIGITS</B> short    => scale
    *   <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column 
    *     like an Oracle ROWID
    *     <UL>
    *     <LI> versionColumnUnknown - may or may not be pseudo column
    *     <LI> versionColumnNotPseudo - is NOT a pseudo column
    *     <LI> versionColumnPseudo - is a pseudo column
    *     </UL>
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schema a schema name; "" retrieves those without a schema
    * @param table a table name
    * @return ResultSet - each row is a column description 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getVersionColumns(String catalog, String schema,
            String table) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schema = getFixedupSchema(schema);
      table = getFixedupIdentifier(table);

      return m_md.getVersionColumns(catalog, schema, table);
   }
   
   /**
    * Gets a description of a table's primary key columns.  They
    * are ordered by COLUMN_NAME.
    *
    * <P>Each primary key column description has the following columns:
    *  <OL>
    *   <LI><B>TABLE_CAT</B> String => table catalog (may be null)
    *   <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
    *   <LI><B>TABLE_NAME</B> String => table name
    *   <LI><B>COLUMN_NAME</B> String => column name
    *   <LI><B>KEY_SEQ</B> short => sequence number within primary key
    *   <LI><B>PK_NAME</B> String => primary key name (may be null)
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schema a schema name; "" retrieves those
    * without a schema
    * @param table a table name
    * @return ResultSet - each row is a primary key column description 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getPrimaryKeys(String catalog, String schema,
            String table) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schema = getFixedupSchema(schema);
      table = getFixedupIdentifier(table);

      return m_md.getPrimaryKeys(catalog, schema, table);
   }

   /**
    * Gets a description of the primary key columns that are
    * referenced by a table's foreign key columns (the primary keys
    * imported by a table).  They are ordered by PKTABLE_CAT,
    * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
    *
    * <P>Each primary key column description has the following columns:
    *  <OL>
    *   <LI><B>PKTABLE_CAT</B> String => primary key table catalog 
    *     being imported (may be null)
    *   <LI><B>PKTABLE_SCHEM</B> String => primary key table schema
    *     being imported (may be null)
    *   <LI><B>PKTABLE_NAME</B> String => primary key table name
    *     being imported
    *   <LI><B>PKCOLUMN_NAME</B> String => primary key column name
    *     being imported
    *   <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
    *   <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
    *   <LI><B>FKTABLE_NAME</B> String => foreign key table name
    *   <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
    *   <LI><B>KEY_SEQ</B> short => sequence number within foreign key
    *   <LI><B>UPDATE_RULE</B> short => What happens to 
    *      foreign key when primary is updated:
    *     <UL>
    *     <LI> importedNoAction - do not allow update of primary 
    *            key if it has been imported
    *     <LI> importedKeyCascade - change imported key to agree 
    *            with primary key update
    *     <LI> importedKeySetNull - change imported key to NULL if 
    *            its primary key has been updated
    *     <LI> importedKeySetDefault - change imported key to default values 
    *            if its primary key has been updated
    *     <LI> importedKeyRestrict - same as importedKeyNoAction 
    *                         (for ODBC 2.x compatibility)
    *     </UL>
    *   <LI><B>DELETE_RULE</B> short => What happens to 
    *     the foreign key when primary is deleted.
    *     <UL>
    *     <LI> importedKeyNoAction - do not allow delete of primary 
    *            key if it has been imported
    *     <LI> importedKeyCascade - delete rows that import a deleted key
    *     <LI> importedKeySetNull - change imported key to NULL if 
    *            its primary key has been deleted
    *     <LI> importedKeyRestrict - same as importedKeyNoAction 
    *                         (for ODBC 2.x compatibility)
    *     <LI> importedKeySetDefault - change imported key to default if 
    *            its primary key has been deleted
    *     </UL>
    *   <LI><B>FK_NAME</B> String => foreign key name (may be null)
    *   <LI><B>PK_NAME</B> String => primary key name (may be null)
    *   <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key 
    *     constraints be deferred until commit
    *     <UL>
    *     <LI> importedKeyInitiallyDeferred - see SQL92 for definition
    *     <LI> importedKeyInitiallyImmediate - see SQL92 for definition 
    *     <LI> importedKeyNotDeferrable - see SQL92 for definition 
    *     </UL>
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schema a schema name; "" retrieves those
    * without a schema
    * @param table a table name
    * @return ResultSet - each row is a primary key column description 
    * @exception SQLException if a database access error occurs
    * @see #getExportedKeys 
    */
   public ResultSet getImportedKeys(String catalog, String schema,
            String table) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schema = getFixedupSchema(schema);
      table = getFixedupIdentifier(table);

      return m_md.getImportedKeys(catalog, schema, table);
   }

   /**
    * Gets a description of the foreign key columns that reference a
    * table's primary key columns (the foreign keys exported by a
    * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
    * FKTABLE_NAME, and KEY_SEQ.
    *
    * <P>Each foreign key column description has the following columns:
    *  <OL>
    *   <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
    *   <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
    *   <LI><B>PKTABLE_NAME</B> String => primary key table name
    *   <LI><B>PKCOLUMN_NAME</B> String => primary key column name
    *   <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
    *     being exported (may be null)
    *   <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
    *     being exported (may be null)
    *   <LI><B>FKTABLE_NAME</B> String => foreign key table name
    *     being exported
    *   <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
    *     being exported
    *   <LI><B>KEY_SEQ</B> short => sequence number within foreign key
    *   <LI><B>UPDATE_RULE</B> short => What happens to 
    *      foreign key when primary is updated:
    *     <UL>
    *     <LI> importedNoAction - do not allow update of primary 
    *            key if it has been imported
    *     <LI> importedKeyCascade - change imported key to agree 
    *            with primary key update
    *     <LI> importedKeySetNull - change imported key to NULL if 
    *            its primary key has been updated
    *     <LI> importedKeySetDefault - change imported key to default values 
    *            if its primary key has been updated
    *     <LI> importedKeyRestrict - same as importedKeyNoAction 
    *                         (for ODBC 2.x compatibility)
    *     </UL>
    *   <LI><B>DELETE_RULE</B> short => What happens to 
    *     the foreign key when primary is deleted.
    *     <UL>
    *     <LI> importedKeyNoAction - do not allow delete of primary 
    *            key if it has been imported
    *     <LI> importedKeyCascade - delete rows that import a deleted key
    *     <LI> importedKeySetNull - change imported key to NULL if 
    *            its primary key has been deleted
    *     <LI> importedKeyRestrict - same as importedKeyNoAction 
    *                         (for ODBC 2.x compatibility)
    *     <LI> importedKeySetDefault - change imported key to default if 
    *            its primary key has been deleted
    *     </UL>
    *   <LI><B>FK_NAME</B> String => foreign key name (may be null)
    *   <LI><B>PK_NAME</B> String => primary key name (may be null)
    *   <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key 
    *     constraints be deferred until commit
    *     <UL>
    *     <LI> importedKeyInitiallyDeferred - see SQL92 for definition
    *     <LI> importedKeyInitiallyImmediate - see SQL92 for definition 
    *     <LI> importedKeyNotDeferrable - see SQL92 for definition 
    *     </UL>
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schema a schema name; "" retrieves those
    * without a schema
    * @param table a table name
    * @return ResultSet - each row is a foreign key column description 
    * @exception SQLException if a database access error occurs
    * @see #getImportedKeys 
    */
   public ResultSet getExportedKeys(String catalog, String schema,
            String table) throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schema = getFixedupSchema(schema);
      table = getFixedupIdentifier(table);

      return m_md.getExportedKeys(catalog, schema, table);
   }

   /**
    * Gets a description of the foreign key columns in the foreign key
    * table that reference the primary key columns of the primary key
    * table (describe how one table imports another's key.) This
    * should normally return a single foreign key/primary key pair
    * (most tables only import a foreign key from a table once.)  They
    * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
    * KEY_SEQ.
    *
    * <P>Each foreign key column description has the following columns:
    *  <OL>
    *   <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
    *   <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
    *   <LI><B>PKTABLE_NAME</B> String => primary key table name
    *   <LI><B>PKCOLUMN_NAME</B> String => primary key column name
    *   <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
    *     being exported (may be null)
    *   <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
    *     being exported (may be null)
    *   <LI><B>FKTABLE_NAME</B> String => foreign key table name
    *     being exported
    *   <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
    *     being exported
    *   <LI><B>KEY_SEQ</B> short => sequence number within foreign key
    *   <LI><B>UPDATE_RULE</B> short => What happens to 
    *      foreign key when primary is updated:
    *     <UL>
    *     <LI> importedNoAction - do not allow update of primary 
    *            key if it has been imported
    *     <LI> importedKeyCascade - change imported key to agree 
    *            with primary key update
    *     <LI> importedKeySetNull - change imported key to NULL if 
    *            its primary key has been updated
    *     <LI> importedKeySetDefault - change imported key to default values 
    *            if its primary key has been updated
    *     <LI> importedKeyRestrict - same as importedKeyNoAction 
    *                         (for ODBC 2.x compatibility)
    *     </UL>
    *   <LI><B>DELETE_RULE</B> short => What happens to 
    *     the foreign key when primary is deleted.
    *     <UL>
    *     <LI> importedKeyNoAction - do not allow delete of primary 
    *            key if it has been imported
    *     <LI> importedKeyCascade - delete rows that import a deleted key
    *     <LI> importedKeySetNull - change imported key to NULL if 
    *            its primary key has been deleted
    *     <LI> importedKeyRestrict - same as importedKeyNoAction 
    *                         (for ODBC 2.x compatibility)
    *     <LI> importedKeySetDefault - change imported key to default if 
    *            its primary key has been deleted
    *     </UL>
    *   <LI><B>FK_NAME</B> String => foreign key name (may be null)
    *   <LI><B>PK_NAME</B> String => primary key name (may be null)
    *   <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key 
    *     constraints be deferred until commit
    *     <UL>
    *     <LI> importedKeyInitiallyDeferred - see SQL92 for definition
    *     <LI> importedKeyInitiallyImmediate - see SQL92 for definition 
    *     <LI> importedKeyNotDeferrable - see SQL92 for definition 
    *     </UL>
    *  </OL>
    *
    * @param primaryCatalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param primarySchema a schema name; "" retrieves those
    * without a schema
    * @param primaryTable the table name that exports the key
    * @param foreignCatalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param foreignSchema a schema name; "" retrieves those
    * without a schema
    * @param foreignTable the table name that imports the key
    * @return ResultSet - each row is a foreign key column description 
    * @exception SQLException if a database access error occurs
    * @see #getImportedKeys 
    */
   public ResultSet getCrossReference(
      String primaryCatalog, String primarySchema, String primaryTable,
      String foreignCatalog, String foreignSchema, String foreignTable
      ) throws SQLException
   {
      foreignCatalog = getFixedupCatalog(foreignCatalog);
      foreignSchema = getFixedupSchema(foreignSchema);
      foreignTable = getFixedupIdentifier(foreignTable);

      foreignCatalog = getFixedupCatalog(foreignCatalog);
      foreignSchema = getFixedupSchema(foreignSchema);
      foreignTable = getFixedupIdentifier(foreignTable);

      return m_md.getCrossReference(
         primaryCatalog, primarySchema, primaryTable,
         foreignCatalog, foreignSchema, foreignTable);
   }

   /**
    * Gets a description of all the standard SQL types supported by
    * this database. They are ordered by DATA_TYPE and then by how
    * closely the data type maps to the corresponding JDBC SQL type.
    *
    * <P>Each type description has the following columns:
    *  <OL>
    *   <LI><B>TYPE_NAME</B> String => Type name
    *   <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
    *   <LI><B>PRECISION</B> int => maximum precision
    *   <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal 
    *     (may be null)
    *   <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal 
         (may be null)
    *   <LI><B>CREATE_PARAMS</B> String => parameters used in creating 
    *     the type (may be null)
    *   <LI><B>NULLABLE</B> short => can you use NULL for this type?
    *     <UL>
    *     <LI> typeNoNulls - does not allow NULL values
    *     <LI> typeNullable - allows NULL values
    *     <LI> typeNullableUnknown - nullability unknown
    *     </UL>
    *   <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive?
    *   <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
    *     <UL>
    *     <LI> typePredNone - No support
    *     <LI> typePredChar - Only supported with WHERE .. LIKE
    *     <LI> typePredBasic - Supported except for WHERE .. LIKE
    *     <LI> typeSearchable - Supported for all WHERE ..
    *     </UL>
    *   <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned?
    *   <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value?
    *   <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an 
    *     auto-increment value?
    *   <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name 
    *     (may be null)
    *   <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
    *   <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
    *   <LI><B>SQL_DATA_TYPE</B> int => unused
    *   <LI><B>SQL_DATETIME_SUB</B> int => unused
    *   <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
    *  </OL>
    *
    * @return ResultSet - each row is a SQL type description 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getTypeInfo() throws SQLException
   {
      return m_md.getTypeInfo();
   }
   
   /**
    * Gets a description of a table's indices and statistics. They are
    * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
    *
    * <P>Each index column description has the following columns:
    *  <OL>
    *   <LI><B>TABLE_CAT</B> String => table catalog (may be null)
    *   <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
    *   <LI><B>TABLE_NAME</B> String => table name
    *   <LI><B>NON_UNIQUE</B> boolean => Can index values be non-unique? 
    *     false when TYPE is tableIndexStatistic
    *   <LI><B>INDEX_QUALIFIER</B> String => index catalog (may be null); 
    *     null when TYPE is tableIndexStatistic
    *   <LI><B>INDEX_NAME</B> String => index name; null when TYPE is 
    *     tableIndexStatistic
    *   <LI><B>TYPE</B> short => index type:
    *     <UL>
    *     <LI> tableIndexStatistic - this identifies table statistics that are
    *         returned in conjuction with a table's index descriptions
    *     <LI> tableIndexClustered - this is a clustered index
    *     <LI> tableIndexHashed - this is a hashed index
    *     <LI> tableIndexOther - this is some other style of index
    *     </UL>
    *   <LI><B>ORDINAL_POSITION</B> short => column sequence number 
    *     within index; zero when TYPE is tableIndexStatistic
    *   <LI><B>COLUMN_NAME</B> String => column name; null when TYPE is 
    *     tableIndexStatistic
    *   <LI><B>ASC_OR_DESC</B> String => column sort sequence, "A" => ascending, 
    *     "D" => descending, may be null if sort sequence is not supported; 
    *     null when TYPE is tableIndexStatistic   
    *   <LI><B>CARDINALITY</B> int => When TYPE is tableIndexStatistic, then 
    *     this is the number of rows in the table; otherwise, it is the 
    *     number of unique values in the index.
    *   <LI><B>PAGES</B> int => When TYPE is  tableIndexStatisic then 
    *     this is the number of pages used for the table, otherwise it 
    *     is the number of pages used for the current index.
    *   <LI><B>FILTER_CONDITION</B> String => Filter condition, if any.  
    *     (may be null)
    *  </OL>
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schema a schema name; "" retrieves those without a schema
    * @param table a table name  
    * @param unique when true, return only indices for unique values; 
    *    when false, return indices regardless of whether unique or not 
    * @param approximate when true, result is allowed to reflect approximate 
    *    or out of data values; when false, results are requested to be 
    *    accurate
    * @return ResultSet - each row is an index column description 
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getIndexInfo(String catalog, String schema, String table,
         boolean unique, boolean approximate)
               throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schema = getFixedupSchema(schema);
      table = getFixedupIdentifier(table);

      return m_md.getIndexInfo(catalog, schema, table, unique, approximate);
   }


   //--------------------------JDBC 2.0-----------------------------

   /**
    * JDBC 2.0
    *
    * Does the database support the given result set type?
    *
    * @param type defined in <code>java.sql.ResultSet</code>
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    * @see Connection
    */
   public boolean supportsResultSetType(int type) throws SQLException
   {
      return m_md.supportsResultSetType(type);
   }

   /**
    * JDBC 2.0
    *
    * Does the database support the concurrency type in combination
    * with the given result set type?
    *
    * @param type defined in <code>java.sql.ResultSet</code>
    * @param concurrency type defined in <code>java.sql.ResultSet</code>
    * @return <code>true</code> if so; <code>false</code> otherwise 
    * @exception SQLException if a database access error occurs
    * @see Connection
    */
   public boolean supportsResultSetConcurrency(int type, int concurrency)
     throws SQLException
   {
      return m_md.supportsResultSetConcurrency(type, concurrency);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether a result set's own updates are visible.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if updates are visible for the result set type;
    *      <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean ownUpdatesAreVisible(int type) throws SQLException
   {
      return m_md.ownUpdatesAreVisible(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether a result set's own deletes are visible.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if deletes are visible for the result set type;
    *      <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean ownDeletesAreVisible(int type) throws SQLException
   {
      return m_md.ownDeletesAreVisible(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether a result set's own inserts are visible.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if inserts are visible for the result set type;
    *      <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean ownInsertsAreVisible(int type) throws SQLException
   {
      return m_md.ownInsertsAreVisible(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether updates made by others are visible.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if updates made by others
    * are visible for the result set type;
    *      <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean othersUpdatesAreVisible(int type) throws SQLException
   {
      return m_md.othersUpdatesAreVisible(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether deletes made by others are visible.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if deletes made by others
    * are visible for the result set type;
    *      <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean othersDeletesAreVisible(int type) throws SQLException
   {
      return m_md.othersDeletesAreVisible(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether inserts made by others are visible.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return true if updates are visible for the result set type
    * @return <code>true</code> if inserts made by others
    * are visible for the result set type;
    *      <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean othersInsertsAreVisible(int type) throws SQLException
   {
      return m_md.othersInsertsAreVisible(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether or not a visible row update can be detected by 
    * calling the method <code>ResultSet.rowUpdated</code>.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if changes are detected by the result set type;
    *       <code>false</code> otherwise
    * @exception SQLException if a database access error occurs
    */
   public boolean updatesAreDetected(int type) throws SQLException
   {
      return m_md.updatesAreDetected(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether or not a visible row delete can be detected by 
    * calling ResultSet.rowDeleted().  If deletesAreDetected()
    * returns false, then deleted rows are removed from the result set.
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return true if changes are detected by the resultset type
    * @exception SQLException if a database access error occurs
    */
   public boolean deletesAreDetected(int type) throws SQLException
   {
      return m_md.deletesAreDetected(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether or not a visible row insert can be detected
    * by calling ResultSet.rowInserted().
    *
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return true if changes are detected by the resultset type
    * @exception SQLException if a database access error occurs
    */
   public boolean insertsAreDetected(int type) throws SQLException
   {
      return m_md.insertsAreDetected(type);
   }

   /**
    * JDBC 2.0
    *
    * Indicates whether the driver supports batch updates.
    * @return true if the driver supports batch updates; false otherwise
    */
   public boolean supportsBatchUpdates() throws SQLException
   {
      return m_md.supportsBatchUpdates();
   }

   /**
    * JDBC 2.0
    *
    * Gets a description of the user-defined types defined in a particular
    * schema.  Schema-specific UDTs may have type JAVA_OBJECT, STRUCT, 
    * or DISTINCT.
    *
    * <P>Only types matching the catalog, schema, type name and type  
    * criteria are returned.  They are ordered by DATA_TYPE, TYPE_SCHEM 
    * and TYPE_NAME.  The type name parameter may be a fully-qualified 
    * name.  In this case, the catalog and schemaPattern parameters are
    * ignored.
    *
    * <P>Each type description has the following columns:
    *  <OL>
    *   <LI><B>TYPE_CAT</B> String => the type's catalog (may be null)
    *   <LI><B>TYPE_SCHEM</B> String => type's schema (may be null)
    *   <LI><B>TYPE_NAME</B> String => type name
    *  <LI><B>CLASS_NAME</B> String => Java class name
    *   <LI><B>DATA_TYPE</B> String => type value defined in java.sql.Types.  
    *  One of JAVA_OBJECT, STRUCT, or DISTINCT
    *   <LI><B>REMARKS</B> String => explanatory comment on the type
    *  </OL>
    *
    * <P><B>Note:</B> If the driver does not support UDTs, an empty
    * result set is returned.
    *
    * @param catalog a catalog name; "" retrieves those without a
    * catalog; null means drop catalog name from the selection criteria
    * @param schemaPattern a schema name pattern; "" retrieves those
    * without a schema
    * @param typeNamePattern a type name pattern; may be a fully-qualified
    * name
    * @param types a list of user-named types to include (JAVA_OBJECT, 
    * STRUCT, or DISTINCT); null returns all types 
    * @return ResultSet - each row is a type description
    * @exception SQLException if a database access error occurs
    */
   public ResultSet getUDTs(String catalog, String schemaPattern, 
           String typeNamePattern, int[] types) 
     throws SQLException
   {
      catalog = getFixedupCatalog(catalog);
      schemaPattern = getFixedupSchema(schemaPattern);
      typeNamePattern = getFixedupIdentifier(typeNamePattern);

      return m_md.getUDTs(catalog, schemaPattern, typeNamePattern, types);
   }

   /**
    * JDBC 2.0
    * Retrieves the connection that produced this metadata object.
    *
    * @return the connection that produced this metadata object
    */
   public Connection getConnection() throws SQLException
   {
      return m_md.getConnection();
   }


   public String getFixedupCatalog(String catalog)
   {
      if ((m_flags & FLAGS_SUPPORTS_CATALOG) == 0)
         catalog = null;
      else if ((catalog != null) && (catalog.length() == 0))
         catalog = null;

      if (catalog != null)
      {
         if ((m_flags & FLAGS_STORES_UPPERCASE) != 0)
            catalog = catalog.toUpperCase();
         else if ((m_flags & FLAGS_STORES_LOWERCASE) != 0)
            catalog = catalog.toLowerCase();
      }

      return catalog;
   }

   public String getFixedupSchema(String schema)
   {
      if ((m_flags & FLAGS_SUPPORTS_SCHEMA) == 0)
         schema = null;
      else if ((schema != null) && (schema.length() == 0))
         schema = null;

      if (schema != null)
      {
         if ((m_flags & FLAGS_STORES_UPPERCASE) != 0)
            schema = schema.toUpperCase();
         else if ((m_flags & FLAGS_STORES_LOWERCASE) != 0)
            schema = schema.toLowerCase();
      }

      return schema;
   }

   public String getFixedupIdentifier(String identifier)
   {
      if (identifier != null)
      {
         if ((m_flags & FLAGS_STORES_UPPERCASE) != 0)
            identifier = identifier.toUpperCase();
         else if ((m_flags & FLAGS_STORES_LOWERCASE) != 0)
            identifier = identifier.toLowerCase();
      }

      return identifier;
   }


   private static final int FLAGS_SUPPORTS_CATALOG   = 0x0001;
   private static final int FLAGS_SUPPORTS_SCHEMA   = 0x0002;
   private static final int FLAGS_STORES_LOWERCASE   = 0x0010;
   private static final int FLAGS_STORES_UPPERCASE   = 0x0020;

   private int m_flags = 0;
   private DatabaseMetaData m_md;
   
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
