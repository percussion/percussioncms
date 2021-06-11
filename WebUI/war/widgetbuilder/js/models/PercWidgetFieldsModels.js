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

/**
 * Widget definition model.
 */
(function($)
{
    WidgetBuilderApp.Field = Backbone.Model.extend({
		defaults: {
			"name": "",
			"label": "",
			"type": "TEXT"
		},
        validate:function(oldName){
            var errors = [];
            var nameVal = this.get("name").trim();
            //is empty check
            if(nameVal.length < 1 || !WidgetBuilderApp.startsWithAlphaRegEx.test(nameVal) || nameVal.length < 2)
                errors.push({name:"name",message:"This is a required field, must start with a letter and must contain at least two characters."});
            else if($.inArray(nameVal.toUpperCase(),sqlWords) !== -1)
            {
            	errors.push({name:"name",message: nameVal + " is a SQL Reserved Keyword. Please choose another value. See <a href='https://help.percussion.com/percussion-cm1/developers/widget-builder/widget-builder-reserved-field-names/index' title='Widget Builder Reserved Words' target='_blank'>the Help website</a> for a complete list of reserved keywords."});
            }
            else
            {
                var char1 = nameVal[0];
                var char2 = nameVal[1];
                if(char1 == char1.toLowerCase() && char2 == char2.toUpperCase())
                    errors.push({name:"name",message:"The field name cannot start with a lowercase letter followed by an uppercase letter."});
            }
            
            var isDupe = false;
            $.each(WidgetBuilderApp.fieldsList.models, function () {
                if(nameVal!=oldName && this.get("name") == nameVal)
                    isDupe = true;
            }, this);
            if(isDupe)
                errors.push({name:"name",message:"The field name must be unique."});

            var labelVal = this.get("label").trim();
            if(labelVal.length < 1)
                errors.push({name:"label",message:"This is a required field."});
            return errors;
        }
	});

    WidgetBuilderApp.FieldCollection = Backbone.Collection.extend({
        model:WidgetBuilderApp.Field,
        comparator: function(model) {
            return model.get('ordinal');
        }
    });
    
    /*
     * This list of SQL reserved Keywords is also found server side in PSWidgetBuilderFieldsValidator please
     * also update server side if there are updates done to this list
     */
    var sqlWords = ["ADD", "EXTERNAL", "PROCEDURE", "ALL", "FETCH", "PUBLIC", "ALTER", "FILE", "RAISERROR",
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
                    "COLLATION_NAME", "COLLATION_SCHEMA"];

})(jQuery);