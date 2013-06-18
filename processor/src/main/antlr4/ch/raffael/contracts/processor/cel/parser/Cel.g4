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

@parser::header {
    import java.util.List;
    import java.util.LinkedList;
    import ch.raffael.contracts.processor.cel.Position;
    import ch.raffael.contracts.processor.cel.ast.*;
}

@lexer::header {
    import ch.raffael.contracts.processor.cel.ast.*;
}

clause returns [Clause node]
    :   FINALLY? ifExpression EOF
    ;

ifExpression returns [AstNode node]
    :   ( IF PAREN_OPEN condition=expression PAREN_CLOSE )? expression
    ;

expression returns [AstNode node]
    :   unary
    |   expression (MUL|DIV|MOD) expression
    |   expression (ADD|SUB) expression
    |   expression (RIGHT_SHIFT|URIGHT_SHIFT|LEFT_SHIFT) expression
    |   expression INSTANCEOF typeref
    |   expression (LT|LE|GE|GT) expression
    |   expression (EQ|NE) expression
    |   expression BITWISE_XOR expression
    |   expression BITWISE_OR expression
    |   expression BITWISE_AND expression
    |   expression LOGICAL_AND expression
    |   expression LOGICAL_OR expression
    |   expression CONDITIONAL expression COLON expression
    ;
	
unary returns [AstNode node]
    :	ADD unary
    |	SUB unary
    |	unaryNoPosNeg
    ;
unaryNoPosNeg returns [AstNode node]
    :	BITWISE_NOT unary
    |	LOGICAL_NOT unary
    |	cast
    |	factor
    ;
cast returns [AstNode node]
    :	PAREN_OPEN primitiveType PAREN_CLOSE unary
    |	PAREN_OPEN typeref PAREN_CLOSE unaryNoPosNeg
    ;
	
factor returns [AstNode node]
    :
    primary (selector)*
    ;
primary returns [AstNode node]
    : PAREN_OPEN expression PAREN_CLOSE
    | methodCall
    | ID
    | literal
    | functionCall
    | typeref ACCESS CLASS
    | classref? ACCESS THIS
    // typeref, e.g. int.class or int[].class
    ;
	
selector
    : ACCESS ID
    | ACCESS methodCall
    | INDEX_OPEN expression INDEX_CLOSE
    ;
	
literal returns [AstNode node]
    : STRING
    | CHAR
    | INT
    | FLOAT
    | TRUE
    | FALSE
    | NULL
    ;
	
methodCall
    : ID PAREN_OPEN
      ( first=expression (COMMA expression)*)?
      PAREN_CLOSE
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
	: ID
	  (ACCESS ID)*
	;
primitiveType
	:	TINT | TLONG | TSHORT | TBYTE | TDOUBLE | TFLOAT | TCHAR | TBOOLEAN
	;

IF		: 'if';
FINALLY 	: 'finally';
TRUE		: 'true';
FALSE		: 'false';
NULL		: 'null';
CLASS		: 'class';

TINT		: 'int';
TLONG		: 'long';
TSHORT		: 'short';
TBYTE		: 'byte';
TBOOLEAN	: 'boolean';
TFLOAT		: 'float';
TDOUBLE		: 'double';
TCHAR		: 'char';
TVOID		: 'void';

THIS		: 'this';
SUPER		: 'super';

OLD		: '@old';
THROWN		: '@thrown';
EQUAL		: '@equals';
PARAM		: '@param';
ARG		: '@arg';
RESULT		: '@result';
EACH		: '@each';
REGEX		: '@regex';

CONDITIONAL	: '?';

LOGICAL_OR	: '||';
LOGICAL_AND	: '&&';

BITWISE_OR	: '|';
BITWISE_XOR	: '^';
BITWISE_AND	: '&';

EQ		: '==';
NE		: '!=';
GT		: '>';
GE		: '>=';
LT		: '<';
LE		: '<=';
INSTANCEOF	: 'instanceof';

ADD		: '+';
SUB		: '-';
MUL		: '*';
DIV		: '/';
MOD		: '%';

LOGICAL_NOT	: '!';
BITWISE_NOT	: '~';

LEFT_SHIFT	: '<<';
RIGHT_SHIFT	: '>>';
URIGHT_SHIFT	: '>>>';

PAREN_OPEN	: '(';
PAREN_CLOSE	: ')';
INDEX_OPEN	: '[';
INDEX_CLOSE	: ']';

ACCESS		: '.';
COMMA		: ',';
COLON		: ':';
LAMBDA		: '->';

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
	) -> skip
    ;
COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~('\n'|'\r')* -> skip;
