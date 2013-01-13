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

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import ch.raffael.contracts.processor.cel.ast.Assertion;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Compiler {

    private final List<CelError> errors = new LinkedList<>();
    private final Location sourceLocation;
    private final String expression;
    private Assertion ast;

    public Compiler(Location sourceLocation, String expression) {
        this.sourceLocation = sourceLocation;
        this.expression = expression;
    }

    public void parse() {
        CelLexer lexer = new CelLexer(new ANTLRStringStream(expression)) {
            @Override
            public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
                addANTLRError(e, getErrorMessage(e, tokenNames));
            }
        };
        CelParser parser = new CelParser(new CommonTokenStream(lexer)) {
            @Override
            public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
                addANTLRError(e, getErrorMessage(e, tokenNames));
            }
        };
        try {
            parser.assertion();
        }
        catch ( RecognitionException e ) {
            // should not happen
            addANTLRError(e, "Unexpected: " + e.toString());
        }
    }

    private void addANTLRError(RecognitionException e, String msg) {
        errors.add(new CelError(sourceLocation, expression, e.line, e.charPositionInLine, msg));
    }

}
