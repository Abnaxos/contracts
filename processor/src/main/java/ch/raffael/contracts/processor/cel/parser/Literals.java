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
package ch.raffael.contracts.processor.cel.parser;

import org.antlr.v4.runtime.Token;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.processor.cel.CelError;
import ch.raffael.contracts.processor.cel.Position;
import ch.raffael.contracts.processor.cel.ast.AstNode;
import ch.raffael.contracts.processor.cel.ast.Literal;
import ch.raffael.contracts.processor.cel.ast.Nodes;
import ch.raffael.contracts.util.NeedsWork;


/**
 * Some helper methods for literals. The grammar is written in a way that the input *will*
 * be valid: The lexer wouldn't have recognised it, if it wasn't. That's why there are
 * only minimal error checks in here. If the lexer did something wrong, the code will fail
 * miserably.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class Literals {

    private Literals() {
    }

    @NeedsWork(
            description = "This can't handle Integer.MIN_VALUE/Long.MIN_VALUE, because the AST is NEG->IntLiteral (positive)")
    @NotNull
    static AstNode integer(Token tok) {
        String strValue = tok.getText();
        Literal.Kind kind = null;
        if ( Character.toUpperCase(strValue.charAt(strValue.length() - 1)) == 'L' ) {
            strValue = strValue.substring(0, strValue.length() - 1);
            try {
                return Nodes.literal(tok, Literal.Kind.LONG, Long.decode(strValue));
            }
            catch ( NumberFormatException e ) {
                // We know that it was a syntactically correct int literal, the lexer made
                // that sure. Therefore, we also know, what this exception means: The value
                // is out of range
                return error(tok, "Long literal out of range: " + tok.getText());
            }
        }
        else {
            try {
                return Nodes.literal(tok, Literal.Kind.INT, Integer.decode(strValue));
            }
            catch ( NumberFormatException e ) {
                // We know that it was a syntactically correct int literal, the lexer made
                // that sure. Therefore, we also know, what this exception means: The value
                // is out of range
                return error(tok, "Integer literal out of range: " + tok.getText());
            }
        }
    }

    static AstNode floatingPoint(Token tok) {
        String strValue = tok.getText();
        Literal.Kind kind = Literal.Kind.DOUBLE;
        if ( Character.toUpperCase(strValue.charAt(strValue.length() - 1)) == 'F' ) {
            kind = Literal.Kind.FLOAT;
            strValue = strValue.substring(0, strValue.length() - 1);
        }
        else if ( Character.toUpperCase(strValue.charAt(strValue.length() - 1)) == 'D' ) {
            strValue = strValue.substring(0, strValue.length() - 1);
        }
        if ( kind == Literal.Kind.DOUBLE ) {
            return Nodes.literal(tok, Literal.Kind.DOUBLE, Double.valueOf(strValue));
        }
        else {
            return Nodes.literal(tok, Literal.Kind.FLOAT, Float.valueOf(strValue));
        }
    }

    static AstNode string(Token tok, Literal.Kind kind) {
        String strValue = tok.getText();
        strValue = strValue.substring(1, strValue.length() - (kind == Literal.Kind.CHAR ? 2 : 1));
        StringBuilder buf = new StringBuilder(strValue.length());
        for ( int i = 0; i < strValue.length(); i++ ) {
            if ( strValue.charAt(i) == '\\' ) {
                i++;
                int unescaped;
                switch ( strValue.charAt(i) ) {
                    case '\\':
                        unescaped = '\\';
                        break;
                    case 'n':
                        unescaped = '\n';
                        break;
                    case 'r':
                        unescaped = '\r';
                        break;
                    case 't':
                        unescaped = '\t';
                        break;
                    case 'b':
                        unescaped = '\b';
                        break;
                    case 'f':
                        unescaped = '\f';
                        break;
                    case '\'':
                        unescaped = '\'';
                        break;
                    case '"':
                        unescaped = '"';
                        break;
                    case 'u':
                        unescaped = 0;
                        for ( int j = 0; j < 4; j++ ) {
                            unescaped = (unescaped << 4) | hex(strValue.charAt(i + j + 1));
                        }
                        i += 4;
                        break;
                    default:
                        // octal
                        unescaped = 0;
                        for ( int j = 0; j < 3 && i < strValue.length(); j++, i++ ) {
                            int digit = strValue.charAt(i) - '0';
                            if ( digit < 0 || digit > 7 ) {
                                i--;
                                break;
                            }
                            unescaped = (unescaped << 3) | digit;
                            if ( j == 1 && unescaped >= 32 ) {
                                // must have been \4xx => only two digits
                                break;
                            }
                        }
                }
                buf.append((char)unescaped);
            }
            else {
                buf.append(strValue.charAt(i));
            }
        }
        if ( kind == Literal.Kind.CHAR ) {
            assert buf.length() == 1;
            return Nodes.literal(tok, Literal.Kind.CHAR, buf.charAt(0));
        }
        else {
            return Nodes.literal(tok, Literal.Kind.STRING, buf.toString());
        }
    }

    private static int hex(char c) {
        c = Character.toUpperCase(c);
        if ( c >= '0' && c <= '9' ) {
            return c - '0';
        }
        else {
            return c - 'A' + 10;
        }
    }

    @NeedsWork(description = "Attach the error")
    private static AstNode error(Token tok, String msg) {
        AstNode node = Nodes.blank(tok);
        node.addError(new CelError(new Position(tok), msg));
        return node;
    }

}
