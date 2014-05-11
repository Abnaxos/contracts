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
    :   pre=topLevelExpr (IMPLY post+=topLevelExpr)*
    |   directPost=IMPLY post+=topLevelExpr
    ;

topLevelExpr returns [AstNode node]
    : expression
    | throwExpression
    | finallyExpression
    ;

throwExpression returns[Throw node]
    :   THROW classRef ID? (COLON expression)?
    ;

finallyExpression returns [Finally node]
    :   FINALLY expression
    ;

parenExpression returns [AstNode node]
    :   expression (IMPLY parenExpression)?
    ;

expression returns [AstNode node]
    :   unary
    |   expression (MUL|DIV|MOD) expression
    |   expression (ADD|SUB) expression
    |   expression (RIGHT_SHIFT|URIGHT_SHIFT|LEFT_SHIFT) expression
    |   expression INSTANCEOF typeRef
    |   expression (LT|LE|GE|GT) expression
    |   expression (EQ|NE|IDENTICAL) expression
    |   RETURN expression
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
    |	PAREN_OPEN typeRef PAREN_CLOSE unaryNoPosNeg
    ;
	
factor returns [AstNode node]
    :
    primary (selector)*
    ;
primary returns [AstNode node]
    : PAREN_OPEN parenExpression PAREN_CLOSE
    | INDEX_OPEN expression INDEX_CLOSE // old
    | methodCall
    | ID
    | literal
    | ARGREF
    | RETURN
    | regex
    | typeRef ACCESS CLASS
    | (typeRef ACCESS)? THIS
    ;
	
selector
    : ACCESS ID
    | ACCESS methodCall
    | ACCESS FOR CONDITIONAL? PAREN_OPEN typeRef? ID COLON expression PAREN_CLOSE
    | INDEX_OPEN expression INDEX_CLOSE
    ;

regex
    : REGEX ( PAREN_OPEN (flags+=ID (COMMA flags+=ID)*)? PAREN_CLOSE )?
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
      ( expression (COMMA expression)*)?
      PAREN_CLOSE
    ;
	
/*functionCall
    : OLD PAREN_OPEN expression PAREN_CLOSE                                 #OldFunction
    | THROWN PAREN_OPEN typeref? PAREN_CLOSE                                #ThrownFunction
    | (PARAM|ARG) PAREN_OPEN (((ADD|SUB)? INT) | ID)? PAREN_CLOSE           #ParamFunction
    | RESULT PAREN_OPEN PAREN_CLOSE                                         #ResultFunction
    | EQUAL PAREN_OPEN expression COMMA expression PAREN_CLOSE              #EqualFunction
    | EACH PAREN_OPEN expression COMMA ID THEN ifExpression PAREN_CLOSE     #EachFunction
    | REGEX PAREN_OPEN STRING (COMMA ID)* PAREN_CLOSE                       #RegexFunction
    ;*/

typeRef
    :
    ( classRef
    | primitiveType
    ) (INDEX_OPEN INDEX_CLOSE)*
    ;
classRef returns [String className]
    : ID (ACCESS ID)*
    ;
primitiveType
    :	TINT | TLONG | TSHORT | TBYTE | TDOUBLE | TFLOAT | TCHAR | TBOOLEAN
    ;

TRUE            : 'true';
FALSE           : 'false';
NULL            : 'null';
CLASS           : 'class';

TINT            : 'int';
TLONG           : 'long';
TSHORT          : 'short';
TBYTE           : 'byte';
TBOOLEAN        : 'boolean';
TFLOAT          : 'float';
TDOUBLE         : 'double';
TCHAR           : 'char';
TVOID           : 'void';

THIS            : 'this';
SUPER           : 'super';

THROW           : 'throw' | 'throws';
FINALLY         : 'finally';
RETURN          : 'return';

CONDITIONAL     : '?';

LOGICAL_OR      : '||';
LOGICAL_AND     : '&&';

BITWISE_OR      : '|';
BITWISE_XOR     : '^';
BITWISE_AND     : '&';

EQ              : '==';
NE              : '!=';
GT              : '>';
GE              : '>=';
LT              : '<';
LE              : '<=';
INSTANCEOF      : 'instanceof';
IDENTICAL       : '===';

ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : '/';
MOD             : '%';

LOGICAL_NOT     : '!';
BITWISE_NOT     : '~';

LEFT_SHIFT      : '<<';
RIGHT_SHIFT     : '>>';
URIGHT_SHIFT    : '>>>';

PAREN_OPEN      : '(';
PAREN_CLOSE     : ')';
INDEX_OPEN      : '[';
INDEX_CLOSE     : ']';

ACCESS          : '.';
COMMA           : ',';
COLON           : ':';
IMPLY           : '->';

FOR             : 'for';

// some java keywords that aren't used in Cel
JAVA_KEYWORD
    : 'case'
    | 'const'
    | 'do'
    | 'else'
    | 'final'
    | 'goto'
    | 'if'
    | 'switch'
    | 'while'
    ;

ID
    : ID_START ID_PART*
    ;
fragment ID_START
    :   [a-zA-Z$_] // these are the "java letters" below 0xFF
    |   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierStart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment ID_PART
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
    |   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

ARGREF
    : '#'
    ( ('+'|'-')? DIGIT+
    | '#'
    );

INT :
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
    : STRING_VALUE
    ;

REGEX
    : '$' STRING_VALUE
    ;

fragment STRING_VALUE
    : '\"' ( ESCAPE_SEQUENCE | ~('\"'|'\\') )* '\"'
    | '\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') )* '\''
    ;
fragment ESCAPE_SEQUENCE
    : '\\'
    ( 'n'
    | 'r'
    | 't'
    | 'b'
    | 'f'
    | '"'
    | '\''
    | '\\'
    | 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    | '0'..'3' OCT_DIGIT OCT_DIGIT
    | OCT_DIGIT OCT_DIGIT
    | OCT_DIGIT
    )
    ;

fragment DIGIT
    : '0'..'9'
    ;
fragment OCT_DIGIT
    :	'0'..'7'
    ;
fragment HEX_DIGIT
    :	'0'..'9' | 'a'..'f' | 'A'..'F'
    ;

CHAR: '\'' ( ESCAPE_SEQUENCE | ~'\'' ) '\'' ('c'|'C')
    ;

fragment EXPONENT
    : ('e'|'E') ('+'|'-')? ('0'..'9')+
    ;

// whitespaces
WS: ( ' '
    | '\t'
    | '\r'
    | '\n'
    ) -> skip
    ;
COMMENT
    : '/*' .*? '*/' -> skip
    ;
LINE_COMMENT
    : '//' ~('\n'|'\r')* -> skip
    ;
