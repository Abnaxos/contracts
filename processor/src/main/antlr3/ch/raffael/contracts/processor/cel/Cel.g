/*
 * Copyright 2012-2013 Raffael Herzog
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar Cel;

options {
	//output = AST;
	backtrack = true;
//	memoize = true;
//	k = 2;
}

tokens {
	IF		= 'if';
	FINALLY 	= 'finally';
	TRUE		= 'true';
	FALSE		= 'false';
	NULL		= 'null';
	CLASS		= 'class';
	
	TINT		= 'int';
	TLONG		= 'long';
	TSHORT		= 'short';
	TBYTE		= 'byte';
	TBOOLEAN	= 'boolean';
	TFLOAT		= 'float';
	TDOUBLE		= 'double';
	TCHAR		= 'char';
	TVOID		= 'void';
	
	THIS		= 'this';
	SUPER		= 'super';
	
	OLD		= '@old';
	THROWN		= '@thrown';
	EQUALS		= '@equals';
	PARAM		= '@param';
	ARG		= '@arg';
	RESULT		= '@result';
	EACH		= '@each';
	REGEX		= '@regex';
	
	CONDITIONAL	= '?';
	
	LOGICAL_OR	= '||';
	LOGICAL_AND	= '&&';
	
	BITWISE_OR	= '|';
	BITWISE_XOR	= '^';
	BITWISE_AND	= '&';
	
	EQ		= '==';
	NE		= '!=';
	GT		= '>';
	GE		= '>=';
	LT		= '<';
	LE		= '<=';
	INSTANCEOF	= 'instanceof';
	
	ADD		= '+';
	SUB		= '-';
	MUL		= '*';
	DIV		= '/';
	MOD		= '%';
	
	LOGICAL_NOT	= '!';
	BITWISE_NOT	= '~';
	
	LEFT_SHIFT	= '<<';
	RIGHT_SHIFT	= '>>';
	URIGHT_SHIFT	= '>>>';

	PAREN_OPEN	= '(';
	PAREN_CLOSE	= ')';
	INDEX_OPEN	= '[';
	INDEX_CLOSE	= ']';
	
	ACCESS		= '.';
	COMMA		= ',';
	COLON		= ':';
	LAMBDA		= '->';
}

@parser::header {
package ch.raffael.contracts.processor.cel;

import java.util.List;
import java.util.LinkedList;
import ch.raffael.contracts.processor.cel.ast.*;
}

@lexer::header {
package ch.raffael.contracts.processor.cel;
}

@members {

	private Assertion assertion;
	
	public void init(Assertion assertion) {
		this.assertion = assertion;
	}
	
	public Assertion getAssertion() {
		return assertion;
	}

	BlankNode blank() {
		return Nodes.blank(input.LT(1));
	}

}

assertion returns [Assertion node]
	:	FINALLY?
		ifExpression
		{ Nodes.assertion($FINALLY!=null ? new Position($FINALLY.getLine(), $FINALLY.getCharPositionInLine()) : $ifExpression.node.getPosition(), $ifExpression.node, $FINALLY!=null); }
		EOF
	;

/*ifExpression
	:	(IF PAREN_OPEN! expression PAREN_CLOSE!)* expression
	;*/

ifExpression returns [AstNode node=blank()]
	:	tok=IF PAREN_OPEN cond=expression PAREN_CLOSE expr=expression
			{$node=Nodes.ifExpression($tok, $cond.node, $expr.node);}
	|	simple=expression {$node=$simple.node;}
	;

expression returns [AstNode node=blank()]
	:	logicalOr {$node=$logicalOr.node;}
		( tok=CONDITIONAL t=expression COLON f=expression {$node=Nodes.conditionalOp($tok, $node, $t.node, $f.node);})?
	;

logicalOr returns [AstNode node=blank()]
	:	first=logicalAnd {$node=$first.node;}
		( LOGICAL_OR next=logicalAnd {$node=Nodes.logicalOp($LOGICAL_OR, $node, $next.node);} )*
	;
logicalAnd returns [AstNode node=blank()]
	:	first=bitwiseOr {$node=$first.node;}
		( LOGICAL_AND next=bitwiseOr {$node=Nodes.logicalOp($LOGICAL_AND, $node, $next.node);} )*
	;
bitwiseOr returns [AstNode node=blank()]
	:	first=bitwiseXor {$node=$first.node;}
		( BITWISE_OR next=bitwiseXor {$node=Nodes.bitwiseOp($BITWISE_OR, $node, $next.node);} )*
	;
bitwiseXor returns [AstNode node=blank()]
	:	first=bitwiseAnd {$node=$first.node;}
		( BITWISE_XOR next=bitwiseAnd {$node=Nodes.bitwiseOp($BITWISE_XOR, $node, $next.node);} )*
	;
bitwiseAnd returns [AstNode node=blank()]
	:	equality ( BITWISE_AND equality )*
	;

equality returns [AstNode node=blank()]
	:	( first=relational {$node=$first.node;}
		| first=instanceOf {$node=$first.node;}
		)
		( op=(EQ|NE)
			(next=relational
			|next=instanceOf) {$node=Nodes.equalityOp($op, $node, $next.node);} )?
	;
relational returns [AstNode node=blank()]
	:	first=shift {$node=$first.node;}
		( op=(GE|GT|LT|LE) next=shift {$node=Nodes.relationalOp($op, $node, $next.node);})?
	;
instanceOf returns [AstNode node=blank()]
	:	val=shift INSTANCEOF type=typeref
	;

shift returns [AstNode node=blank()]
	:	first=addition {$node=$first.node;}
		( op=(LEFT_SHIFT|RIGHT_SHIFT|URIGHT_SHIFT) next=addition {$node=Nodes.shiftOp($op, $node, $next.node);})*
	;

addition returns [AstNode node=blank()]
	:	first=multiplication {$node=$first.node;}
		( op=(ADD|SUB) next=multiplication {$node=Nodes.arithmeticOp($op, $node, $next.node);} )*
	;
multiplication returns [AstNode node=blank()]
	:	first=unary {$node=$first.node;}
		( op=(MUL|DIV|MOD) next=unary {$node=Nodes.arithmeticOp($op, $node, $next.node);} )*
	;
	
unary returns [AstNode node=blank()]
	:	ADD pos=unary {$node=Nodes.unaryOp($ADD, $pos.node);}
	|	SUB neg=unary {$node=Nodes.unaryOp($SUB, $neg.node);}
	|	unaryNoPosNeg {$node=$unaryNoPosNeg.node;}
	;
unaryNoPosNeg returns [AstNode node=blank()]
	:	BITWISE_NOT bnot=unary {$node=Nodes.unaryOp($BITWISE_NOT, $bnot.node);}
	|	LOGICAL_NOT lnot=unary {$node=Nodes.unaryOp($LOGICAL_NOT, $lnot.node);}
	|	cast
	|	factor postfix*
	;
cast	:	paren=PAREN_OPEN primitiveType PAREN_CLOSE unary
	|	paren=PAREN_OPEN typeref PAREN_CLOSE unaryNoPosNeg
	;

postfix:	ACCESS member=ID
	|	ACCESS method=ID PAREN_OPEN argList? PAREN_CLOSE
	|	index=INDEX_OPEN expression INDEX_CLOSE
	;
	
factor	:
	(	reference
	|	INT|FLOAT|STRING|CHAR
	|	TRUE|FALSE|NULL|THIS|SUPER
	|	( typeref | primitiveType | TVOID ) ACCESS CLASS
	|	call
	|	function
	|	(PAREN_OPEN expression PAREN_CLOSE)
	);
reference
	// A reference to unknown; this is used later to determine how to interpret this:
	// It could be a local variable, a field, a static field, a package/class
	// See JLS7 §6.5.2
	:	id=ID
	;
call	:	method=ID PAREN_OPEN argList? PAREN_CLOSE;

function:	OLD PAREN_OPEN expression PAREN_CLOSE
	|	THROWN PAREN_OPEN classref? PAREN_CLOSE
	|	paramFunction
	|	RESULT PAREN_OPEN PAREN_CLOSE
	|	EQUALS PAREN_OPEN expression COMMA expression PAREN_CLOSE
	|	EACH PAREN_OPEN ID COLON expression LAMBDA ifExpression PAREN_CLOSE
	|	REGEX PAREN_OPEN STRING (COMMA ID)* PAREN_CLOSE
	;
paramFunction
	:	(fun=PARAM|fun=ARG) PAREN_OPEN (((ADD|SUB)? INT) | ID)? PAREN_CLOSE
	;
		
argList	:	expression ( COMMA expression )*
	;

typeref	:	classref array*
	|	primitiveType array+
	;
classref:	ID classDereference*;
classDereference
	:	ACCESS pkgOrCls=ID;
typerefFragment
	:	ID
	;
array	:	arr=INDEX_OPEN INDEX_CLOSE;
primitiveType
	:	TINT | TLONG | TSHORT | TBYTE | TDOUBLE | TFLOAT | TCHAR | TBOOLEAN
	;

ID	:	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'$')*;

INT	:
	( '0'
	| ('1'..'9') DIGIT*
	| '0' OCT_DIGIT+
	| '0' 'x' HEX_DIGIT+
	) ('l'|'L')?
	;

FLOAT
    :
    ( ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ) ('d'|'D'|'f'|'F')?
    ;

STRING
	:	'\"' ( ESCAPE_SEQUENCE | ~('\"'|'\\') )* '\"'
	|	'\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') )* '\''
	;

fragment ESCAPE_SEQUENCE
	:	'\\'
	(	'n'
	|	'r'
	|	't'
	|	'b'
	|	'f'
	|	'"'
	|	'\''
	|	'\\'
	|	'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	|	OCT_DIGIT OCT_DIGIT OCT_DIGIT?
	)
	;

fragment DIGIT	: '0'..'9' ;
fragment OCT_DIGIT
	:	'0'..'7';
fragment HEX_DIGIT
	:	'0'..'9' | 'a'..'f' | 'A'..'F';

CHAR:  '\'' ( ESCAPE_SEQUENCE | ~'\'' ) '\'' ('c'|'C')
    ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

// whitespaces
WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;
COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;
