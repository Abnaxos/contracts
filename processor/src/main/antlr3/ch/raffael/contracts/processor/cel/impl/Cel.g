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
	EQUAL		= '@equals';
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
package ch.raffael.contracts.processor.cel.impl;

import java.util.List;
import java.util.LinkedList;
import ch.raffael.contracts.processor.cel.Position;
import ch.raffael.contracts.processor.cel.ast.*;
}

@lexer::header {
package ch.raffael.contracts.processor.cel.impl;
}

@members {

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
	:	first=equality {$node=$first.node;}
		( BITWISE_AND next=equality {$node=Nodes.bitwiseOp($BITWISE_AND, $node, $next.node);})*
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
	|	factor {$node=$factor.node;}
	;
cast	:	paren=PAREN_OPEN primitiveType PAREN_CLOSE unary
	|	paren=PAREN_OPEN typeref PAREN_CLOSE unaryNoPosNeg
	;
	
factor returns [AstNode node=blank()]
	:
	primary {$node=$primary.node;} (selector[$node] {$node=$selector.node;})*
	;
primary returns [AstNode node=blank()]
	: PAREN_OPEN expression PAREN_CLOSE {$node=$expression.node;}
	| methodCall[null] {$node=$methodCall.node;}
	| ID {$node=Nodes.idReference($ID, $ID.text);}
	| literal {$node=$literal.node;}
	| functionCall
	| typeref ACCESS CLASS
	| classref? ACCESS THIS
	// typeref, e.g. int.class or int[].class
	;
	
selector [AstNode source] returns [AstNode node=blank()]
	: ACCESS ID {$node=Nodes.idReference($ID, source, $ID.text);}
	| ACCESS methodCall[source] {$node=$methodCall.node;}
	| INDEX_OPEN expression INDEX_CLOSE {$node=Nodes.arrayAccess($INDEX_OPEN, source, $expression.node);}
	;
	
literal returns [AstNode node=blank()]
	: STRING {$node=Literals.string($STRING, Literal.Kind.STRING);}
	| CHAR {$node=Literals.string($CHAR, Literal.Kind.CHAR);}
	| INT {$node=Literals.integer($INT);}
	| FLOAT {$node=Literals.floatingPoint($FLOAT);}
	| TRUE {$node=Nodes.literal($TRUE, Literal.Kind.BOOLEAN, true);}
	| FALSE {$node=Nodes.literal($FALSE, Literal.Kind.BOOLEAN, false);}
	| NULL {$node=Nodes.literal($NULL, Literal.Kind.NULL, null);}
	;
	
methodCall [AstNode source] returns [AstNode node=blank()]
@init{
List<AstNode> args = new ArrayList<>();
}
	: ID PAREN_OPEN
	  ( first=expression {args.add($first.node);} (COMMA next=expression {args.add($next.node);})*)?
	  PAREN_CLOSE {$node=Nodes.methodCall($ID, source, $ID.text, args);}
	;
	
functionCall
	: OLD PAREN_OPEN expression PAREN_CLOSE
	| THROWN PAREN_OPEN classref? PAREN_CLOSE
	| paramFunction
	| RESULT PAREN_OPEN PAREN_CLOSE
	| EQUAL PAREN_OPEN expression COMMA expression PAREN_CLOSE
	| EACH PAREN_OPEN ID COLON expression LAMBDA ifExpression PAREN_CLOSE
	| REGEX PAREN_OPEN STRING (COMMA ID)* PAREN_CLOSE
	;
paramFunction
	:	(fun=PARAM|fun=ARG) PAREN_OPEN (((ADD|SUB)? INT) | ID)? PAREN_CLOSE
	;

typeref
	:
	( ID (ACCESS ID)*
	| primitiveType
	) (INDEX_OPEN INDEX_CLOSE)*
	;
classref returns [String name=""]
@init{
StringBuilder buf = new StringBuilder();
}
	: first=ID {buf.append($first.text);}
	  (ACCESS next=ID {buf.append('.').append($next.text);})*
	  {name=buf.toString();}
	;
primitiveType
	:	TINT | TLONG | TSHORT | TBYTE | TDOUBLE | TFLOAT | TCHAR | TBOOLEAN
	;

ID	:	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'$')*;

INT	:
	( '0'
	| ('1'..'9') DIGIT*
	| '0' OCT_DIGIT+
	| HEX_PREFIX HEX_DIGIT+
	) ('l'|'L')?
	;

FLOAT
    :
    ( (('0'..'9')+ '.' ('0'..'9')* EXPONENT?
      |   '.' ('0'..'9')+ EXPONENT?
      |   ('0'..'9')+ EXPONENT)
    | (HEX_PREFIX (HEX_DIGIT)* 
        (    () 
        |    ('.' (HEX_DIGIT)* ) 
        )
        ( 'p' | 'P' )
        ( '+' | '-' )?
        ( '0' .. '9' )+
    ))
    ('d'|'D'|'f'|'F')?
    ;
    
fragment HEX_PREFIX
	: '0' ('x'|'X')
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
	|	'0'..'3' OCT_DIGIT OCT_DIGIT
	|	OCT_DIGIT OCT_DIGIT
	|	OCT_DIGIT
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
WS:	( ' '
	| '\t'
	| '\r'
	| '\n'
	) {$channel=HIDDEN;}
    ;
COMMENT: '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;};
LINE_COMMENT: '//' ~('\n'|'\r')* { $channel = HIDDEN; };
