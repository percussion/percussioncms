header 
{
package com.percussion.soln.segment.rx;
import java.util.List;
import java.util.Vector;
}

class SegmentAliasFieldParser extends Parser;
{
	private List<String> aliases = new Vector<String>(); 
	public List<String> getAliases() { return this.aliases; }
}
startRule
	: aliasList
    ;


aliasList
	: aliasRule (aliasRule)*
	;
    
aliasRule
	: a: QUOTED { aliases.add(a.getText()); }
	| b: UNQUOTED { aliases.add(b.getText()); }
	;
	
class SegmentAliasFieldLexer extends Lexer;

options { k = 2; }
	
QUOTED	: '"' ( options {greedy=false;} : . )* '"' 
	  {
	  	// Strip the surrounding quotes
	  	String txt = getText(); 
	  	setText(txt.substring(1, txt.length() -1)); 
	  };

// one-or-more letters followed by a newline
UNQUOTED	:	(~('\r' | '\n' | '\t' | ' ' | '"'))+;

SEPARATOR
    :   '\r' '\n'   // DOS
    |   '\n'        // UNIX
    |   '\t'
    |   (' ')+  { $setType(Token.SKIP); }
    ;