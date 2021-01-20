header {
	package com.percussion.services.contentmgr.impl.query;
	
	import com.percussion.services.contentmgr.data.PSQuery;
	import com.percussion.services.contentmgr.impl.query.nodes.*;
	import com.percussion.utils.types.PSPair;
	import javax.jcr.query.Query;
	import java.util.List;
	import java.util.ArrayList;
}

class XpathParser extends Parser;

options {
    exportVocab = Xpath;
    k = 4;
    buildAST = false;
}

tokens {
	ELEMENT="element";
}

start_rule returns [PSQuery query]
	{
		IPSQueryNode w = null;
		List<PSPair<PSQueryNodeIdentifier,PSQuery.SortOrder>> o = null;
		PSPair<PSQueryNodeIdentifier,String> eldata = null;
		List<String> path = new ArrayList<String>();
		String pp = null;
		query = new PSQuery(Query.XPATH);
		List al = null;
	}
	: 
	(pp=path_element
	{
		path.add(pp);
	}
	)*
	(eldata=element_rule (SLASH)?
	{
		query.getTypeConstraints().add(eldata.getFirst());
		path.add(eldata.getSecond());
	}
	)?
	( LSQ w=where_clause RSQ )? 
	(al=attribute_list
	{
		if (al != null)
		{
			query.getProjection().addAll(al);
		}
	}
	)?
	(o=order_clause)? 
	{
		StringBuilder jcrpath = new StringBuilder();
		boolean first = true;
		for(String pathel : path)
		{
			if (pathel.equals("//"))
			{
				jcrpath.append("/%/");
			}
			else if (pathel.equals("jcr:root"))
			{
				// Ignore
			}
			else if (pathel.equals("/"))
			{
				jcrpath.append("/");
			}
			else if (pathel.equals("**"))
			{
				jcrpath.append("%");
			}
			else
			{
				jcrpath.append(pathel);
			}
			first = false;
		}
		
		IPSQueryNode pathnode = null;
		if (jcrpath.length() > 0)
		{
			pathnode =
   			new PSQueryNodeComparison(new PSQueryNodeIdentifier("jcr:path"),
      			new PSQueryNodeValue(jcrpath.toString()),IPSQueryNode.Op.LIKE);
		}
		if (w == null)
		{
	   		query.setWhere(pathnode);
		}
		else if (pathnode != null)
		{
			query.setWhere(
				new PSQueryNodeConjunction(pathnode, w, IPSQueryNode.Op.AND));
		}
		else 
		{
			query.setWhere(w);
		}
		
		query.setSortFields(o);
		if (query.getProjection().size() == 0)
		{
			query.getProjection().add(new PSQueryNodeIdentifier("*"));
		}
		
		if (query.getTypeConstraints().size() == 0)
		{
			query.getTypeConstraints().add(new PSQueryNodeIdentifier("nt:base"));
		}
	}
	;
	
path_element returns [String rval=null]
	: 
	s:SLASH 
	{
		return "/";
	}	
	| 
	d:DSLASH
	{
		return "//";
	}
	|
	f:IDENTIFIER
	{
		return f.getText();
	}
	;

where_clause returns [IPSQueryNode n = null] : n = term;

full_expr returns [IPSQueryNode n=null]
	:
	n=expr
	|
	("not") => "not" n=expr
	{
		n = new PSQueryNodeConjunction(null, n, IPSQueryNode.Op.NOT);
	}	
	| OPEN_PAREN n=term CLOSE_PAREN
	;
	
factor returns [IPSQueryNode n=null] 
	{
		IPSQueryNode right = null;
	}
	: 
	n=full_expr 
	("and" right=full_expr
	{
		n = new PSQueryNodeConjunction(n, right, IPSQueryNode.Op.AND);
	}
	)*
	;

term returns [IPSQueryNode n = null]
	{
		IPSQueryNode right = null;
	}
	:
	n=factor
	("or" right=factor
	{
		n = new PSQueryNodeConjunction(n, right, IPSQueryNode.Op.OR);
	}
	)*
	;

expr returns [IPSQueryNode n = null] 
	{
		IPSQueryNode.Op o = null;
		IPSQueryNode v = null;
		PSQueryNodeIdentifier a = null;
		List<IPSQueryNode> params = null;
	}
	: 
	(fqn OPEN_PAREN) => 
		a=fqn OPEN_PAREN params=parameter_list CLOSE_PAREN
	{
		n = new PSQueryNodeFunction(a.getName(), params);
	}
	|
	(a=attribute (o=op v=value)? | o=op v=value a=attribute)
	{
		if (o != null)
		{
			n = new PSQueryNodeComparison(a, v, o);
		}
		else
		{
			n = new PSQueryNodeComparison(new PSQueryNodeValue(null),
				a, IPSQueryNode.Op.NE);
		}
	}
	;
	
element_rule returns [PSPair<PSQueryNodeIdentifier,String> eldata]
    {
	   	PSQueryNodeIdentifier type = null;
      	eldata = new PSPair<PSQueryNodeIdentifier,String>();
    }
	:
	ELEMENT OPEN_PAREN (STAR | prop:IDENTIFIER) 
			COMMA (STAR | type=fqn) CLOSE_PAREN
	{
		if (prop == null)
		{
			eldata.setSecond("**");
		}
		else
		{
			eldata.setSecond(prop.getText());
		}
		if (type == null)
		{
			eldata.setFirst(new PSQueryNodeIdentifier("*"));
		}
		else
		{
			eldata.setFirst(type);
		}
	}
	;	
	
attribute_list returns [List rval]
   {
		PSQueryNodeIdentifier a = null;
   		rval = new ArrayList();
   }
   :
	a=attribute
	{
		rval.add(a);
	} 
	|
	(OPEN_PAREN a=attribute 
	{
		rval.add(a);
	}
	(OR a=attribute
	{
		rval.add(a);
	}
	)*
	CLOSE_PAREN)
	;
	
parameter_list returns [List rval] 
	{
		Object p = null;
		rval = new ArrayList();
	}
	:
	p=value 
	{
		rval.add(p);
	}
	( COMMA p=value
	{
		rval.add(p);
	}
	)*
	;
	
op returns [IPSQueryNode.Op op]
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
   | NE 
   { op = IPSQueryNode.Op.NE; }
   | LE 
   { op = IPSQueryNode.Op.LE; }
   | GE 
   { op = IPSQueryNode.Op.GE; }
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
    p=attribute ( "ascending"
    | "descending" 
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
	
value returns [IPSQueryNode rval=null]:
	rval=attribute 
	|
	qs:QUOTED_STRING 
	{
		String temp = qs.getText();
		// Strip outer quotes of any kind
		if (temp.startsWith("'") && temp.endsWith("'"))
		{
			temp = temp.substring(1, temp.length() - 1);
		}
		rval = new PSQueryNodeValue(temp);
	}
	| var:VARIABLE
	{
		rval = new PSQueryNodeVariable(var.getText());
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
			rval = new PSQueryNodeValue(new Double(nstr));
		}
		else
		{
			rval = new PSQueryNodeValue(new Long(nstr));
		}
	}
	;

attribute returns [PSQueryNodeIdentifier attr=null]
	:
	AT attr=fqn;

fqn returns [PSQueryNodeIdentifier id]
	{
		id = null;
	}
	: name:IDENTIFIER
	{
		id = new PSQueryNodeIdentifier(name.getText());
	}
	;

	
//
// Lexer
//

class XpathLexer extends Lexer;

options {
    exportVocab = Xpath;
    testLiterals = true;
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

IDENTIFIER
    : 	'a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | ':' | '_' | '$' | '#' )*
    ;
    
VARIABLE
    : 
        ':' ( 'a' .. 'z' | '0' .. '9' | '_' | '$' | '#' )*
    ;    

// quoted_string 
//    ::= "\'" { "any_character" } "\'" 

QUOTED_STRING
      : '\'' ( ~'\'' )* '\'' 
    ;

SLASH : '/' ('/' { _ttype = DSLASH; } )?;
OPEN_PAREN : '(' ;
CLOSE_PAREN : ')' ;
EQ : '=' ;
COMMA : ',';
BANG : '!' ( '=' { _ttype = NE; } )? ;

GT : '>' ( '=' { _ttype = GE; } )? ;
LT : '<' ( '=' { _ttype = LE; } )? ;
AT : '@';
LSQ : '[';
RSQ : ']';
STAR : '*';
OR : '|';

// match_string 
//    ::= "'" { "any_character" | "_" | "%" } "'" 

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
	
protected
N : '0' .. '9' ( '0' .. '9' )* ;

WS  :   (   ' '
        |   '\t'
        |   '\r' '\n' { newline(); }
        |   '\n'      { newline(); }
        |   '\r'      { newline(); }
        )
        {$setType(Token.SKIP);} //ignore this token
    ;

ML_COMMENT
	:	"(:"
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
		":)"
		{$setType(Token.SKIP);}
	;