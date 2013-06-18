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
import ch.raffael.contracts.processor.cel.parser.AstBuilder
import ch.raffael.contracts.processor.cel.parser.CelLexer
import ch.raffael.contracts.processor.cel.parser.CelParser
import ch.raffael.contracts.processor.cel.specutil.Parser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNSimulator

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Parser {

    def methodMissing(String name, argsObj) {
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
        List<RecognitionException> errors = []
        ANTLRErrorListener errorListener = new BaseErrorListener() {
            @Override
            void syntaxError(Recognizer<?, ? extends ATNSimulator> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errors << e
            }
        }
        CelLexer lexer = new CelLexer(new ANTLRInputStream(source));
        CelParser parser = new CelParser(new CommonTokenStream(lexer));
        lexer.addErrorListener(errorListener)
        parser.addErrorListener(errorListener)
        AstBuilder builder = new AstBuilder()
        builder.install(parser)
        Object ctx = parser.invokeMethod(rule, args.drop(1))
        if ( errors ) {
            throw new CelErrorsException(errors)
        }
        else {
            return ctx.node
        }
    }
}
