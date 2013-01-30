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
package ch.raffael.contracts.processor.cel.specutil

import ch.raffael.contracts.processor.cel.CelErrorsException
import ch.raffael.contracts.processor.cel.ast.AstNode
import ch.raffael.contracts.processor.cel.impl.CelLexer
import ch.raffael.contracts.processor.cel.impl.CelParser
import org.antlr.runtime.ANTLRStringStream
import org.antlr.runtime.CommonTokenStream
import org.antlr.runtime.RecognitionException

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Parser {

    static methodMissing(String name, argsObj) {
        Object[] args = (Object[])argsObj
        String source
        if ( args.length == 1 ) {
            source = args[0]
        }
        else if ( args.length == 0 ) {
            source = TrackFeatureExtension.current.name
        }
        else {
            throw new MissingMethodException(name, getClass(), args)
        }
        return Parser.parse(name, source)
    }

    static AstNode parse(String rule, String source, Object... args) {
        String name
        final List<RecognitionException> errors = new LinkedList<>();
        CelLexer lexer = new CelLexer(new ANTLRStringStream(source)) {
            @Override
            public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
                errors.add(e);
            }
        };
        CelParser parser = new CelParser(new CommonTokenStream(lexer)) {
            @Override
            public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
                errors.add(e);
            }
        };
        AstNode result = parser.invokeMethod(rule, args.drop(1)) as AstNode
        if ( errors ) {
            throw new CelErrorsException(errors)
        }
        else {
            return result
        }
    }
}
