//
// Grammer for JSR-170's version of the SQL SELECT statement. This grammer is
// derived from the Oracle 7 grammer from the ANTRL website.
header {
	package com.percussion.services.contentmgr.impl.query;
	
	import com.percussion.services.contentmgr.data.PSQuery;
	import com.percussion.services.contentmgr.impl.query.nodes.*;
	import com.percussion.utils.types.PSPair;
	import javax.jcr.query.Query;
	import java.util.List;
	import java.util.ArrayList;
}

class SqlParser extends Parser;

options {
    exportVocab = Sql;
    k = 4;
    buildAST = false;
}

start_rule returns [PSQuery query]
		{
			List<PSQueryNodeIdentifier> sl = null;
			List<PSQueryNodeIdentifier> tl = null;
			IPSQueryNode w = null;
			List<PSPair<PSQueryNodeIdentifier,PSQuery.SortOrder>> o = null;
			query = new PSQuery(Query.SQL);
		}
		:
      "select" sl=select_list "from" tl=type_reference_list
      ( "where" w=where_condition )? ( ( order_clause ) => o=order_clause )?
      	{
      		query.setProjection(sl);
     		query.setTypeConstraints(tl);
			query.setWhere(w);
     		query.setSortFields(o);
     	}      

    ;

select_list returns [List<PSQueryNodeIdentifier> ids]
		{
			PSQueryNodeIdentifier p = null;
			ids = new ArrayList<PSQueryNodeIdentifier>();
		}
		:
      ( 
      	( property ) => p=property 
	      	{
	      		ids.add(p);
	      	}
      	( COMMA p=property 
	        {
	      		ids.add(p);
	      	}
	      )*
        | 
        ASTERISK 
	         {
	        		ids.add(new PSQueryNodeIdentifier("*"));
	         }     	
        )
    ;

type_reference_list returns [List<PSQueryNodeIdentifier> ids]
		{
			PSQueryNodeIdentifier tn = null;
			ids = new ArrayList<PSQueryNodeIdentifier>();
		}
		:
        tn=type_name 
        		{
        			ids.add(tn);
        		}
        ( COMMA tn=type_name 
	        {
        		ids.add(tn);
        	}
        )*
    ;

where_condition returns [IPSQueryNode wnode]	
	{
		wnode = null;
	}
	:  wnode=condition   ;

property returns [PSQueryNodeIdentifier id]
	{ 
		String n = null, p = null; 
		id = null;
	}
	:
      ident:IDENTIFIER 
      {
      	id = new PSQueryNodeIdentifier(ident.getText());
      }
    ;
    
type_name returns [PSQueryNodeIdentifier tn]
	{ 
		String ns = null;
		tn = null;
	}
	:
	id:IDENTIFIER
	{
		tn = new PSQueryNodeIdentifier(id.getText());
	}
	;
	

condition returns [IPSQueryNode lnode] 
	{
		IPSQueryNode rnode = null;
		lnode = null;
	}
	: lnode=logical_term
	( "or" rnode=logical_term
		{
			lnode = new PSQueryNodeConjunction(lnode, rnode, IPSQueryNode.Op.OR);
		}
	)* ;

logical_term returns [IPSQueryNode lnode]
	{
		IPSQueryNode rnode = null;
		lnode = null;
	}
   : lnode=logical_factor 
   ( "and" rnode=logical_factor 
   	{
			lnode = new PSQueryNodeConjunction(lnode, rnode, IPSQueryNode.Op.AND);
		}
   )*
    ;

logical_factor returns [IPSQueryNode lnode]
	{
		IPSQueryNode rnode = null;
		IPSQueryNode arg = null;
		IPSQueryNode.Op op = null;
		PSQueryNodeIdentifier id = null;
		List params = null;
		lnode = null;
	}
   :
   	OPEN_PAREN lnode=condition CLOSE_PAREN
   	|
   	(property OPEN_PAREN) => id=property OPEN_PAREN params=parameter_list CLOSE_PAREN
	{
		lnode = new PSQueryNodeFunction(id.getName(), params);
	}
   	|
   	(argument "is") => arg=argument "is" (n:"not")? "null"
   	{
   		if (n == null)
   		{
   			lnode = new PSQueryNodeComparison(arg, new PSQueryNodeValue(null),
   				IPSQueryNode.Op.EQ);
   		}
   		else
   		{
 			lnode = new PSQueryNodeComparison(arg, new PSQueryNodeValue(null),
 				IPSQueryNode.Op.NE);
   		}
   	}
   	|
    arg=argument op=operator (rnode=argument) 
    {
   		lnode = new PSQueryNodeComparison(arg, rnode, op);
   	}
   	|
   	"not" rnode=logical_factor 
   	{
   		lnode = new PSQueryNodeConjunction(null, (IPSQueryNode) rnode, IPSQueryNode.Op.NOT);
   	}
    ;
    
argument returns [IPSQueryNode rval=null]: rval=property | rval=value;    

parameter_list returns [List rval] 
	{
		Object p = null;
		rval = new ArrayList();
	}
	:
	p=parameter 
	{
		rval.add(p);
	}
	( COMMA p=parameter
	{
		rval.add(p);
	}
	)*
	;
	
parameter returns [Object rval]
   {
   		rval = null;
   }
   :
   rval=value | rval=property;

value returns [IPSQueryNode v]
	{
		v = null;
	}
	:
	qs:QUOTED_STRING 
	{
		String temp = qs.getText();
		// Strip outer quotes of any kind
		if (temp.startsWith("'") && temp.endsWith("'"))
		{
			temp = temp.substring(1, temp.length() - 1);
		}
		v = new PSQueryNodeValue(temp);
	}
	| var:VARIABLE
	{
		v = new PSQueryNodeVariable(var.getText());
	}
	| 
	(PLUS | m:MINUS)? n:NUMBER
	{
		String nstr = n.getText();
		if (m != null) 
		{
			nstr = "-" + nstr;
		}
		if (nstr.indexOf('.') >= 0)
		{
			v = new PSQueryNodeValue(new Double(nstr));
		}
		else
		{
			v = new PSQueryNodeValue(new Long(nstr));
		}
	}
	;

operator returns [IPSQueryNode.Op op]
	{
		op = null;
	}
   : 
   EQ
   { op = IPSQueryNode.Op.EQ; }
   | LT 
   { op = IPSQueryNode.Op.LT; }
   | GT 
   { op = IPSQueryNode.Op.GT; }
   | NOT_EQ 
   { op = IPSQueryNode.Op.NE; }
   | LE 
   { op = IPSQueryNode.Op.LE; }
   | GE 
   { op = IPSQueryNode.Op.GE; }
   | "like" 
   { op = IPSQueryNode.Op.LIKE; }
   ;  

order_clause returns [List<PSPair<PSQueryNodeIdentifier,PSQuery.SortOrder>> solist]
	{
		PSPair<PSQueryNodeIdentifier,PSQuery.SortOrder> adef = null;
		solist = new ArrayList<PSPair<PSQueryNodeIdentifier,PSQuery.SortOrder>>();
	}
    :
    "order" "by" adef=sorted_def 
    {
    	solist.add(adef);
    }
    ( COMMA adef=sorted_def 
 	{
 		solist.add(adef);
 	}
    )*
    ;
	
sorted_def returns [PSPair<PSQueryNodeIdentifier,PSQuery.SortOrder> rval]
   {
   		PSQueryNodeIdentifier p = null;
   		boolean asc = true;
   		rval = null;
   }
    :
    p=property ( "asc"
	   | "desc" 
	   {
	   		asc = false;
	   }
	   )? 
    {
	rval = 
		new PSPair<PSQueryNodeIdentifier,PSQuery.SortOrder>(p, 
			asc ? PSQuery.SortOrder.ASC : PSQuery.SortOrder.DSC);
    }
    ;
	
//
// Lexer
//

class SqlLexer extends Lexer;

options {
    exportVocab = Sql;
    testLiterals = false;
    k = 2;
    caseSensitive = false;
    caseSensitiveLiterals = false;
    charVocabulary = '\3' .. '\377';
}

//
// Literals were put here in the lexer to get them to be case insensitive.
//

// identifier 
//     ::= 
//     ( "letter" { "letter" | "digit" | "underline" | "dollar" | "sharp" } ) 
//     | "quote" { "any character" } "quote" 

IDENTIFIER options { testLiterals=true; }
    : 
        'a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | ':' | '_' | '$' | '#' )*
    ;
    
VARIABLE 
    : 
        ':' ( 'a' .. 'z' | '0' .. '9' | '_' | '$' | '#' )*
    ;

// quoted_string 
//    ::= "'" { "any_character" } "'" 

QUOTED_STRING
      : '\'' ( ~'\'' )* '\'' 
    ;


SEMI : ';';
COMMA : ',' ;
ASTERISK : '*' ;
AT_SIGN : '@' ;
PLUS : '+' ;
MINUS : '-' ;
OPEN_PAREN : '(' ;
CLOSE_PAREN : ')' ;

DIVIDE : '/' ;

VERTBAR : '|' ;

EQ : '=' ;

// Why did I do this?  Isn't this handle by just increasing the look ahead?
NOT_EQ :
            '<' { _ttype = LT; }
                (       ( '>' { _ttype = NOT_EQ; } )
                    |   ( '=' { _ttype = LE; } ) )?
        | "!=" | "^="
    ;
GT : '>' ( '=' { _ttype = GE; } )? ;

// Number code from Java, simplified for SQL
NUMBER
	{boolean isDecimal=false;}
	:	'.'	((('0'..'9')+ (EXPONENT)? (FLOAT_SUFFIX)?))?

	|	(	'0' (	//float or double with leading zero
			(('0'..'9')+ ('.'|EXPONENT|FLOAT_SUFFIX)) => ('0'..'9')+
			)?
		  |	('1'..'9') ('0'..'9')*  {isDecimal=true;} // non-zero decimal
		)
		(	// only check to see if it's a float if looks like decimal so far
			{isDecimal}?
			(	'.' ('0'..'9')* (EXPONENT)? (FLOAT_SUFFIX)?
			|	EXPONENT (FLOAT_SUFFIX)?
			|	FLOAT_SUFFIX
			)
		)?
	;
	
// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
	:	('e') ('+'|'-')? ('0'..'9')+
	;


protected
FLOAT_SUFFIX
	:	'f'|'d'
	;

// Not sure exactly what the purpose of a double quote is.  It has cropped up
// around column names and aliases.  Maybe that means you could have
// table names, column names, or even aliases with spaces.  If so, they should
// no longer be skipped and added the rules.
DOUBLE_QUOTE : '"' { $setType(Token.SKIP); } ;

WS  :   (   ' '
        |   '\t'
        |   '\r' '\n' { newline(); }
        |   '\n'      { newline(); }
        |   '\r'      { newline(); }
        )
        {$setType(Token.SKIP);} //ignore this token
    ;

// Taken right from antlr-2.7.1/examples/java/java/java.g ...
// multiple-line comments
ML_COMMENT
	:	"/*"
		(	/*	'\r' '\n' can be matched in one alternative or by matching
				'\r' in one iteration and '\n' in another.  I am trying to
				handle any flavor of newline that comes in, but the language
				that allows both "\r\n" and "\r" and "\n" to all be valid
				newline is ambiguous.  Consequently, the resulting grammar
				must be ambiguous.  I'm shutting this warning off.
			 */
			options {
				generateAmbigWarnings=false;
			}
		:
			{ LA(2)!='/' }? '*'
		|	'\r' '\n'		{newline();}
		|	'\r'			{newline();}
		|	'\n'			{newline();}
		|	~('*'|'\n'|'\r')
		)*
		"*/"
		{$setType(Token.SKIP);}
	;

