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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.security;

import com.github.javafaker.Faker;
import com.ibm.icu.text.Normalizer2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.encoder.Encode;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.DB2Codec;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.codecs.OracleCodec;
import org.owasp.esapi.errors.EncodingException;
import org.owasp.esapi.reference.DefaultEncoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecureStringUtils {

    private static final Logger log = LogManager.getLogger(SecureStringUtils.class);

    public enum DatabaseType{
        MYSQL,
        ORACLE,
        DB2,
        MSSQL,
        DERBY
    }

    /**
     * Checks if the supplied date is a valid date.
     * @param dt the date
     * @return true if the date is valid, false if it is not
     */
    public static boolean isValidDate(String dt){
        try {
                LocalDate.parse(dt);
            } catch (DateTimeParseException e) {
                return false;
            }
            return true;
    }

    /**
     * Checks if the supplied time is a valie time.
     * @param t the time
     * @return true if the time is valid, false if not
     */
    public static boolean isValidTime(String t){
        try{
            LocalTime.parse(t);
        }catch(DateTimeParseException e){
            return false;
        }
        return true;
    }

    /**
     * Will return an instance of secure random.  Will attempt to return a StrongSecureRandom first
     * but will return a standard SecureRandom if Strong is unavailable.  May return null if
     * secure random cannot be initialized.
     *
     * @return
     */
    public static SecureRandom getSecureRandom(){
        SecureRandom ret = new SecureRandom();
        return ret;
    }

    /**
     * Static list of reserved SQL words to be used for validating table and column names.
     */
    private static final List<String> SQLKEYWORDS = new ArrayList<>();

    static {
        SQLKEYWORDS.addAll(Arrays.asList("ADD", "EXTERNAL", "PROCEDURE", "ALL", "FETCH", "PUBLIC", "ALTER", "FILE", "RAISERROR",
                "AND", "FILLFACTOR", "READ", "ANY", "FOR", "READTEXT", "AS", "FOREIGN", "RECONFIGURE", "ASC",
                "FREETEXT", "REFERENCES", "AUTHORIZATION", "FREETEXTTABLE", "REPLICATION", "BACKUP", "FROM", "RESTORE",
                "BEGIN", "FULL", "RESTRICT", "BETWEEN", "FUNCTION", "RETURN", "BREAK", "GOTO", "REVERT", "BROWSE",
                "GRANT", "REVOKE", "BULK", "GROUP", "RIGHT", "BY", "HAVING", "ROLLBACK", "CASCADE", "HOLDLOCK",
                "ROWCOUNT", "CASE", "IDENTITY", "ROWGUIDCOL", "CHECK", "IDENTITY_INSERT", "RULE", "CHECKPOINT",
                "IDENTITYCOL", "SAVE", "CLOSE", "IF", "SCHEMA", "CLUSTERED", "IN", "SECURITYAUDIT", "COALESCE",
                "INDEX", "SELECT", "COLLATE", "INNER", "SEMANTICKEYPHRASETABLE", "COLUMN", "INSERT",
                "SEMANTICSIMILARITYDETAILSTABLE", "COMMIT", "INTERSECT", "SEMANTICSIMILARITYTABLE", "COMPUTE", "INTO",
                "SESSION_USER", "CONSTRAINT", "IS", "SET", "CONTAINS", "JOIN", "SETUSER", "CONTAINSTABLE", "KEY",
                "SHUTDOWN", "CONTINUE", "KILL", "SOME", "CONVERT", "LEFT", "STATISTICS", "CREATE", "LIKE",
                "SYSTEM_USER", "CROSS", "LINENO", "TABLE", "CURRENT", "LOAD", "TABLESAMPLE", "CURRENT_DATE", "MERGE",
                "TEXTSIZE", "CURRENT_TIME", "NATIONAL", "THEN", "CURRENT_TIMESTAMP", "NOCHECK", "TO", "CURRENT_USER",
                "NONCLUSTERED", "TOP", "CURSOR", "NOT", "TRAN", "DATABASE", "NULL", "TRANSACTION", "DBCC", "NULLIF",
                "TRIGGER", "DEALLOCATE", "OF", "TRUNCATE", "DECLARE", "OFF", "TRY_CONVERT", "DEFAULT", "OFFSETS",
                "TSEQUAL", "DELETE", "ON", "UNION", "DENY", "OPEN", "UNIQUE", "DESC", "OPENDATASOURCE", "UNPIVOT",
                "DISK", "OPENQUERY", "UPDATE", "DISTINCT", "OPENROWSET", "UPDATETEXT", "DISTRIBUTED", "OPENXML", "USE",
                "DOUBLE", "OPTION", "USER", "DROP", "OR", "VALUES", "DUMP", "ORDER", "VARYING", "ELSE", "OUTER",
                "VIEW", "END", "OVER", "WAITFOR", "ERRLVL", "PERCENT", "WHEN", "ESCAPE", "PIVOT", "WHERE", "EXCEPT",
                "PLAN", "WHILE", "EXEC", "PRECISION", "WITH", "EXECUTE", "PRIMARY", "WITHIN GROUP", "EXISTS", "PRINT",
                "WRITETEXT", "EXIT", "PROC", "ABSOLUTE", "OVERLAPS", "ACTION", "PAD", "ADA", "PARTIAL", "PASCAL",
                "EXTRACT", "POSITION", "ALLOCATE", "FALSE", "PREPARE", "FIRST", "PRESERVE", "FLOAT", "ARE", "PRIOR",
                "PRIVILEGES", "FORTRAN", "ASSERTION", "FOUND", "AT", "REAL", "AVG", "GLOBAL", "RELATIVE", "GO", "BIT",
                "BIT_LENGTH", "BOTH", "ROWS", "HOUR", "CASCADED", "SCROLL", "IMMEDIATE", "SECOND", "CAST", "SECTION",
                "CATALOG", "INCLUDE", "CHAR", "SESSION", "CHAR_LENGTH", "INDICATOR", "CHARACTER", "INITIALLY",
                "CHARACTER_LENGTH", "SIZE", "INPUT", "SMALLINT", "INSENSITIVE", "SPACE", "INT", "SQL", "COLLATION",
                "INTEGER", "SQLCA", "SQLCODE", "INTERVAL", "SQLERROR", "CONNECT", "SQLSTATE", "CONNECTION",
                "SQLWARNING", "ISOLATION", "SUBSTRING", "CONSTRAINTS", "SUM", "LANGUAGE", "CORRESPONDING", "LAST",
                "TEMPORARY", "COUNT", "LEADING", "TIME", "LEVEL", "TIMESTAMP", "TIMEZONE_HOUR", "LOCAL",
                "TIMEZONE_MINUTE", "LOWER", "MATCH", "TRAILING", "MAX", "MIN", "TRANSLATE", "DATE", "MINUTE",
                "TRANSLATION", "DAY", "MODULE", "TRIM", "MONTH", "TRUE", "DEC", "NAMES", "DECIMAL", "NATURAL",
                "UNKNOWN", "NCHAR", "DEFERRABLE", "NEXT", "UPPER", "DEFERRED", "NO", "USAGE", "NONE", "USING",
                "DESCRIBE", "VALUE", "DESCRIPTOR", "DIAGNOSTICS", "NUMERIC", "VARCHAR", "DISCONNECT", "OCTET_LENGTH",
                "DOMAIN", "ONLY", "WHENEVER", "WORK", "END-EXEC", "WRITE", "YEAR", "OUTPUT", "ZONE", "EXCEPTION",
                "HOST", "RELEASE", "ADMIN", "IGNORE", "RESULT", "AFTER", "RETURNS", "AGGREGATE", "ROLE", "ALIAS",
                "INITIALIZE", "ROLLUP", "ROUTINE", "INOUT", "ROW", "ARRAY", "ASENSITIVE", "SAVEPOINT", "ASYMMETRIC",
                "INTERSECTION", "SCOPE", "SEARCH", "ATOMIC", "BEFORE", "ITERATE", "BINARY", "SENSITIVE", "LARGE",
                "SEQUENCE", "BLOB", "BOOLEAN", "LATERAL", "SETS", "SIMILAR", "BREADTH", "LESS", "CALL", "CALLED",
                "LIKE_REGEX", "CARDINALITY", "LIMIT", "SPECIFIC", "LN", "SPECIFICTYPE", "LOCALTIME", "SQLEXCEPTION",
                "LOCALTIMESTAMP", "LOCATOR", "CLASS", "MAP", "START", "CLOB", "STATE", "MEMBER", "STATEMENT",
                "COLLECT", "METHOD", "STATIC", "COMPLETION", "STDDEV_POP", "CONDITION", "MOD", "STDDEV_SAMP",
                "MODIFIES", "STRUCTURE", "MODIFY", "SUBMULTISET", "SUBSTRING_REGEX", "CONSTRUCTOR", "SYMMETRIC",
                "CORR", "MULTISET", "SYSTEM", "COVAR_POP", "TERMINATE", "COVAR_SAMP", "THAN", "CUBE", "NCLOB",
                "CUME_DIST", "NEW", "CURRENT_CATALOG", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH",
                "CURRENT_ROLE", "NORMALIZE", "TRANSLATE_REGEX", "CURRENT_SCHEMA", "CURRENT_TRANSFORM_GROUP_FOR_TYPE",
                "OBJECT", "TREAT", "CYCLE", "OCCURRENCES_REGEX", "DATA", "OLD", "UESCAPE", "UNDER", "OPERATION",
                "ORDINALITY", "UNNEST", "OUT", "OVERLAY", "DEPTH", "VAR_POP", "DEREF", "PARAMETER", "VAR_SAMP",
                "PARAMETERS", "VARIABLE", "DESTROY", "PARTITION", "DESTRUCTOR", "PATH", "WIDTH_BUCKET",
                "DETERMINISTIC", "POSTFIX", "WITHOUT", "DICTIONARY", "PREFIX", "WINDOW", "PREORDER", "WITHIN",
                "PERCENT_RANK", "DYNAMIC", "PERCENTILE_CONT", "XMLAGG", "EACH", "PERCENTILE_DISC", "XMLATTRIBUTES",
                "ELEMENT", "POSITION_REGEX", "XMLBINARY", "XMLCAST", "EQUALS", "XMLCOMMENT", "EVERY", "XMLCONCAT",
                "RANGE", "XMLDOCUMENT", "READS", "XMLELEMENT", "FILTER", "XMLEXISTS", "RECURSIVE", "XMLFOREST", "REF",
                "XMLITERATE", "REFERENCING", "XMLNAMESPACES", "FREE", "REGR_AVGX", "XMLPARSE", "FULLTEXTTABLE",
                "REGR_AVGY", "XMLPI", "FUSION", "REGR_COUNT", "XMLQUERY", "GENERAL", "REGR_INTERCEPT", "XMLSERIALIZE",
                "GET", "REGR_R2", "XMLTABLE", "REGR_SLOPE", "XMLTEXT", "REGR_SXX", "XMLVALIDATE", "GROUPING",
                "REGR_SXY", "HOLD", "REGR_SYY", "COLUMN_NAME", "COLUMNS", "COMMAND_FUNCTION", "COMMAND_FUNCTION_CODE",
                "COMMENT", "COMMITTED", "COMPRESS", "CONDITION_NUMBER", "CONNECTION_NAME", "CONSTRAINT_CATALOG",
                "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA", "CONVERSION", "COPY", "CREATEDB", "CREATEROLE", "CREATEUSER",
                "CSV", "CURSOR_NAME", "DATABASES", "DATETIME", "DATETIME_INTERVAL_CODE", "DATETIME_INTERVAL_PRECISION",
                "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DAYOFMONTH", "DAYOFWEEK", "DAYOFYEAR",
                "DEFAULTS", "DEFINED", "DEFINER", "DEGREE", "DELAY_KEY_WRITE", "DELAYED", "DELIMITER", "DELIMITERS",
                "DENSE_RANK", "DERIVED", "DISABLE", "DISPATCH", "DISTINCTROW", "DIV", "DO", "DUAL", "DUMMY",
                "DYNAMIC_FUNCTION", "DYNAMIC_FUNCTION_CODE", "ELSEIF", "ENABLE", "ENCLOSED", "ENCODING", "ENCRYPTED",
                "ENUM", "ESCAPED", "EXCLUDE", "EXCLUDING", "EXCLUSIVE", "EXISTING", "EXP", "EXPLAIN", "FIELDS",
                "FINAL", "FLOAT4", "FLOAT8", "FLOOR", "FLUSH", "FOLLOWING", "FORCE", "FORWARD", "FREEZE", "FULLTEXT",
                "G", "GENERATED", "GRANTED", "GRANTS", "GREATEST", "HANDLER", "HEADER", "HEAP", "HIERARCHY",
                "HIGH_PRIORITY", "HOSTS", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IDENTIFIED", "ILIKE",
                "IMMUTABLE", "IMPLEMENTATION", "IMPLICIT", "INCLUDING", "INCREMENT", "INFILE", "INFIX", "INHERIT",
                "INHERITS", "INITIAL", "INSERT_ID", "INSTANCE", "INSTANTIABLE", "INSTEAD", "INT1", "INT2", "INT3",
                "INT4", "INT8", "INVOKER", "ISAM", "ISNULL", "K", "KEY_MEMBER", "KEY_TYPE", "KEYS", "LANCOMPILER",
                "LAST_INSERT_ID", "LEAST", "LEAVE", "LENGTH", "LINES", "LISTEN", "LOCATION", "LOCK", "LOGIN", "LOGS",
                "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "M", "MATCHED", "MAX_ROWS", "MAXEXTENTS",
                "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MESSAGE_LENGTH", "MESSAGE_OCTET_LENGTH",
                "MESSAGE_TEXT", "MIDDLEINT", "MIN_ROWS", "MINUS", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MINVALUE",
                "MLSLABEL", "MODE", "MONTHNAME", "MORE", "MOVE", "MUMPS", "MYISAM", "NAME", "NESTING",
                "NO_WRITE_TO_BINLOG", "NOAUDIT", "NOCOMPRESS", "NOCREATEDB", "NOCREATEROLE", "NOCREATEUSER",
                "NOINHERIT", "NOLOGIN", "NORMALIZED", "NOSUPERUSER", "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT",
                "NULLABLE", "NULLS", "NUMBER", "OCTETS", "OFFLINE", "OFFSET", "OIDS", "ONLINE", "OPERATOR", "OPTIMIZE",
                "OPTIONALLY", "OPTIONS", "ORDERING", "OTHERS", "OUTFILE", "OVERRIDING", "OWNER", "PACK_KEYS",
                "PARAMETER_MODE", "PARAMETER_NAME", "PARAMETER_ORDINAL_POSITION", "PARAMETER_SPECIFIC_CATALOG",
                "PARAMETER_SPECIFIC_NAME", "PARAMETER_SPECIFIC_SCHEMA", "PASSWORD", "PCTFREE", "PLACING", "PLI",
                "POWER", "PRECEDING", "PREPARED", "PROCEDURAL", "PROCESS", "PROCESSLIST", "PURGE", "QUOTE", "RAID0",
                "RANK", "RAW", "RECHECK", "REGEXP", "REINDEX", "RELOAD", "RENAME", "REPEAT", "REPEATABLE", "REPLACE",
                "REQUIRE", "RESET", "RESIGNAL", "RESOURCE", "RESTART", "RETURNED_CARDINALITY", "RETURNED_LENGTH",
                "RETURNED_OCTET_LENGTH", "RETURNED_SQLSTATE", "RLIKE", "ROUTINE_CATALOG", "ROUTINE_NAME",
                "ROUTINE_SCHEMA", "ROW_COUNT", "ROW_NUMBER", "ROWID", "ROWNUM", "SCALE", "SCHEMA_NAME", "SCHEMAS",
                "SCOPE_CATALOG", "SCOPE_NAME", "SCOPE_SCHEMA", "SECOND_MICROSECOND", "SECURITY", "SELF", "SEPARATOR",
                "SERIALIZABLE", "SERVER_NAME", "SETOF", "SHARE", "SHOW", "SIGNAL", "SIMPLE", "SONAME", "SOURCE",
                "SPATIAL", "SPECIFIC_NAME", "SQL_BIG_RESULT", "SQL_BIG_SELECTS", "SQL_BIG_TABLES",
                "SQL_CALC_FOUND_ROWS", "SQL_LOG_OFF", "SQL_LOG_UPDATE", "SQL_LOW_PRIORITY_UPDATES", "SQL_SELECT_LIMIT",
                "SQL_SMALL_RESULT", "SQL_WARNINGS", "SQRT", "SSL", "STABLE", "STARTING", "STATUS", "STDIN", "STDOUT",
                "STORAGE", "STRAIGHT_JOIN", "STRICT", "STRING", "STYLE", "SUBCLASS_ORIGIN", "SUBLIST", "SUCCESSFUL",
                "SUPERUSER", "SYNONYM", "SYSDATE", "SYSID", "TABLE_NAME", "TABLES", "TABLESPACE", "TEMP", "TEMPLATE",
                "TERMINATED", "TEXT", "TIES", "TINYBLOB", "TINYINT", "TINYTEXT", "TOAST", "TOP_LEVEL_COUNT",
                "TRANSACTION_ACTIVE", "TRANSACTIONS_COMMITTED", "TRANSACTIONS_ROLLED_BACK", "TRANSFORM", "TRANSFORMS",
                "TRIGGER_CATALOG", "TRIGGER_NAME", "TRIGGER_SCHEMA", "TRUSTED", "TYPE", "UID", "UNBOUNDED",
                "UNCOMMITTED", "UNDO", "UNENCRYPTED", "UNLISTEN", "UNLOCK", "UNNAMED", "UNSIGNED", "UNTIL",
                "USER_DEFINED_TYPE_CATALOG", "USER_DEFINED_TYPE_CODE", "USER_DEFINED_TYPE_NAME",
                "USER_DEFINED_TYPE_SCHEMA", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VACUUM", "VALID", "VALIDATE",
                "VALIDATOR", "VARBINARY", "VARCHAR2", "VARCHARACTER", "VARIABLES", "VERBOSE", "VOLATILE", "X509",
                "XOR", "YEAR_MONTH", "ZEROFILL", "A", "ABORT", "ABS", "ACCESS", "ALSO", "ALWAYS", "ANALYSE", "ANALYZE",
                "ASSIGNMENT", "ATTRIBUTE", "ATTRIBUTES", "AUDIT", "AUTO_INCREMENT", "AVG_ROW_LENGTH", "BACKWARD",
                "BERNOULLI", "BIGINT", "BITVAR", "BOOL", "C", "CACHE", "CATALOG_NAME", "CEIL", "CEILING", "CHAIN",
                "CHANGE", "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME", "CHARACTER_SET_SCHEMA", "CHARACTERISTICS",
                "CHARACTERS", "CHECKED", "CHECKSUM", "CLASS_ORIGIN", "CLUSTER", "COBOL", "COLLATION_CATALOG",
                "COLLATION_NAME", "COLLATION_SCHEMA"));
    }

    public static String[] getSqlReservedKeywords(){
        return (String[])Arrays.copyOf(SQLKEYWORDS.toArray(),SQLKEYWORDS.size());
    }

    /**
     * Validates a table or column name.
     *
     * @param name
     * @return
     */
    public static boolean isValidTableOrColumnName(String name)
    {
        boolean isValid = StringUtils.isAlphanumeric(name);
        isValid = isValid && StringUtils.isAsciiPrintable(name);
        isValid = isValid && name.length() <= 128;
        isValid = isValid && !StringUtils.isNumeric(StringUtils.left(name, 1));
        isValid = isValid && !isSQLReservedKeyWord(name);

        // can't have single lowercase followed by upper case due to hibernate bug
        isValid = isValid && !(name.length() > 1 && Character.isLowerCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1)));

        return isValid;
    }

    /**
     * Validates against a list of known reserved SQL keywords.
     * @param name
     * @return
     */
    public static boolean isSQLReservedKeyWord(String name)
    {
        return SQLKEYWORDS.contains(name.toUpperCase());
    }

    /**
     * Given the supplied string that is expected to be a url,
     * remove any linefeeds from it, normalize it, and strip any
     * protocol that is not http or https.
     * @param s The cleansed url string or ""
     * @return An http or https url or or an empty string
     */
    public static String stripNonHttpProtocols(String s){
        String ret="";
        if(s != null) {
            //normalize the string
            String temp = s;
            temp = normalize(temp, true);
            int ipos = temp.indexOf(":");
            String prot;
            if (ipos > 0) {
                prot = temp.substring(0, ipos);
                if (prot.equals("HTTP") || prot.equals("HTTPS")) {
                    ret = stripAllLineBreaks(
                            normalize(s, false));
                }
            } else {
                ret = "";
            }
        }
        return ret;
    }
    public static String normalize(String s, boolean toUpper){
        if(toUpper)
            return Normalizer2.getNFKDInstance().normalize(s).toUpperCase();
        else
            return Normalizer2.getNFKDInstance().normalize(s);
    }


    /**
     * Utility to remove parameters from header.
     * @param str
     * @return
     */
    public static String stripAllLineBreaks(String str) {
        String ret = str;

        if(ret == null)
            return "";

        if(isURLEncoded(str)){
            try {

                // A valid http header will be url encoded, make sure no line feeds are encoded
                ret = URLDecoder.decode(str, StandardCharsets.UTF_8.name());

                ret = URLEncoder.encode(ret,StandardCharsets.UTF_8.name());
            }catch (UnsupportedEncodingException | IllegalArgumentException e) {

                ret  = ret;
            }
        }

        //Either not encoded or just sanity check the encoded string.
        ret = ret;

        return ret.replaceAll("%0d|%0a|\\R+","");
    }

    /**
     * Convenience method for sanitizing HTTP request parameters.
     * @param rp A string provided in an HTTP request
     * @return A sanitized string
     */
    public static String srp(String rp){
       return stripAllLineBreaks(rp);
    }

    public static boolean isURLEncoded(String s){
        boolean ret = false;
        try{
            String s1 = URLDecoder.decode(s, StandardCharsets.UTF_8.name());
            String s2  = URLEncoder.encode(s1, StandardCharsets.UTF_8.name());

            ret = s1.equals(s2);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        return ret;
    }
    /**
     * Utility to sanitize a string for use in a file system path under a specified path.
     *
     * @param str
     * @return The sanitized string
     */
    public static String sanitizeStringForFileUnderPath(String containingPath, String str){
        //TODO: Implement me!
        throw new RuntimeException("Not Implemented!");
    }

    /**
     * Utility to sanitize a string for use in a file system path
     *
     * @param str
     * @return The sanitized string
     */
    public static String sanitizeStringForFileSystem(String str){
        //TODO: Implement me!
        throw new RuntimeException("Not Implemented!");
    }

    public static String sanitizeStringForSQLStatement(String str){
        return str.replace("'", "\'");
    }
    /**
     * Utility to sanitize a string for use in a SQL statement.
     * @param str User provided string
     * @param dbType The type of database that should be used for encoding if an ESAPI Codec is available for the DB.
     * @return The sanitized string, will use {@link #sanitizeStringForFileSystem(String)} if no ESAPI codec is available.
     */
    public static String sanitizeStringForSQLStatement(String str, DatabaseType dbType){
        Codec codec;
        switch(dbType){
            case DERBY:
            case DB2:
                codec = new DB2Codec();
                break;
            case MYSQL:
                codec = new MySQLCodec(MySQLCodec.Mode.STANDARD);
                break;
            case ORACLE:
                codec = new OracleCodec();
                break;
            default:
                codec = null;
        }
        if(codec == null) {
            return sanitizeStringForSQLStatement(str);
        }else{

            return DefaultEncoder.getInstance().encodeForSQL(codec,str);
        }
    }

    /**
     * Takes a path provided as input and makes sure that it is:
     *
     *  1. URI Decoded
     *  2. Normalized for /'s
     *  3. Pointed at a resource under the web application root of / (/Rhythmyx/../../../../ would fail)
     *
     * @param path
     * @return the decoded and checked "safe" path or null if the path is invalid
     */
    public static String cleanWildPath(String[] resourcePaths, String path, String remoteIP){
        String ret = null;

        if(path ==null)
            return ret;

        try {
            //Url decode path
            ret = ESAPI.encoder().decodeFromURL(path);

            //Normalize backslashes to slashes
            ret = ret.replace("\\","/");

            //Normalize doubleslashes
            while(ret.contains("//")) {
                ret = ret.replace("//", "/");
            }

            //Remove /Rhythmyx/ if present
            while(ret.contains("/Rhythmyx/")) {
                ret = ret.replace("/Rhythmyx/", "/");
            }

            String[] dotdots = ret.split("\\.\\./");
            for(String s : resourcePaths){
                String[] slashes = {};
                if(ret.startsWith(s)) {
                    slashes = s.split("/");

                    if (dotdots.length > slashes.length) {
                        log.warn("Security filter blocked suspicious path: {} from client ip: {}",ret, remoteIP);
                        ret = null;
                        break;
                    }
                }
            }

            //if we didn't match a resource we still need to check for /../ - there should be no dot dots
            if(ret != null && ret.startsWith("/..")){
                log.warn("Security filter blocked suspicious path: {} from client ip: {}",ret, remoteIP);
                ret= null;
            }


        } catch (EncodingException e) {
            log.warn("Error decoding wild path {}. Error: {}", path, e.getMessage());
            log.debug(e);
        }

        return ret;
    }
    /**
     * Sanitizes a user provided string for use in HTML
     * @param str a user provided string
     * @return The sanitized string
     */
    public static String sanitizeStringForHTML(String str){
       return Encode.forHtml(str);
    }

    public static String generateRandomPassword(){
        Faker f = Faker.instance(getSecureRandom());

        char[] password = f.lorem().characters(6, 20, true, true).toCharArray();
        char[] special = new char[]{'@', '$', '%', '^', '&', '*'};
        for (int i = 0; i < f.random().nextInt(6); i++) {
            password[f.random().nextInt(password.length)] = special[f.random().nextInt(special.length)];
        }
        return new String(password);

    }

    /**
     * Will strip the leading / from arg if present.
     * @param arg a String, can be null
     * @return will return the supplied arg without a leading / if present or just return arg
     */
    public static String stripLeadingSlash(String arg){
        if(arg != null && arg.startsWith("/")){
            return arg.substring(1);
        }else{
            return arg;
        }
    }

    public static boolean isChildOfFilePath(final Path parent, final Path child) {
        final Path absoluteParent = parent.toAbsolutePath().normalize();
        final Path absoluteChild = child.toAbsolutePath().normalize();

        if (absoluteParent.getNameCount() >= absoluteChild.getNameCount()) {
            return false;
        }

        final Path immediateParent = absoluteChild.getParent();
        if (immediateParent == null) {
            return false;
        }

        return isSameFileAs(absoluteParent, immediateParent) || isChildOfFilePath(absoluteParent, immediateParent);
    }

    public static boolean isSameFileAs(final Path path, final Path path2) {
        try {
            return Files.isSameFile(path, path2);
        }
        catch (final IOException ioe) {
            return path.toAbsolutePath().normalize().equals(path2.toAbsolutePath().normalize());
        }
    }
}
