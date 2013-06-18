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
package ch.raffael.contracts.processor.cel;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import ch.raffael.contracts.processor.cel.ast.Clause;
import ch.raffael.contracts.processor.cel.parser.AstBuilder;
import ch.raffael.contracts.processor.cel.parser.CelLexer;
import ch.raffael.contracts.processor.cel.parser.CelParser;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Compiler {

    private final List<CelError> errors = new LinkedList<>();
    private final Location sourceLocation;
    private final String expression;

    public Compiler(Location sourceLocation, String expression) {
        this.sourceLocation = sourceLocation;
        this.expression = expression;
    }

    public Clause parse() throws ParseException {
        ANTLRErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                addANTLRError(e, msg);
            }
        };
        CelLexer lexer = new CelLexer(new ANTLRInputStream(expression));
        lexer.addErrorListener(errorListener);
        CelParser parser = new CelParser(new CommonTokenStream(lexer));
        parser.addErrorListener(errorListener);
        try {
            Clause ast = new AstBuilder().install(parser).clause().node;
            if ( ast == null ) {
                if ( errors.isEmpty() ) {
                    throw new IllegalStateException("No AST returned, but no errors reported");
                }
                throw new ParseException(errors);
            }
            return ast;
        }
        catch ( RecognitionException e ) {
            // should not happen
            addANTLRError(e, "Unexpected: " + e.toString());
            throw new ParseException(errors);
        }
    }

    private void addANTLRError(RecognitionException e, String msg) {
        errors.add(new CelError(new Position(e.getOffendingToken().getLine(), e.getOffendingToken().getCharPositionInLine()), msg));
    }

}
