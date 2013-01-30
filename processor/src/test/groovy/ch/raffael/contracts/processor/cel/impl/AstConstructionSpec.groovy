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
package ch.raffael.contracts.processor.cel.impl

import ch.raffael.contracts.processor.cel.CelErrorsException
import ch.raffael.contracts.processor.cel.specutil.Parser
import ch.raffael.contracts.processor.cel.specutil.TrackFeature
import spock.lang.Specification

import static ch.raffael.contracts.processor.cel.specutil.Ast.*

/**
 * Specification for AST construction. <strong>NOTE:</strong> methodMissing() will
 * forward to the parser, i.e. you can invoke any parser rule by its name with the source
 * code to be parsed as first argument. If no source code is given, the name of the
 * current spec will be used. This is a little bit hacky, but as you can see, it saves
 * a LOT of typing. ;)
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@TrackFeature
class AstConstructionSpec extends Specification {

    def "Sanity check: Throws CelErrorsException when parse errors occur"() {
      when:
        this.assertion "a b (c d)"

      then:
        thrown(CelErrorsException)
    }

    def "a+b"() {
      expect:
        addition() == add(idRef('a'), idRef('b'))
    }

    def "a.c+b"() {
      expect:
        addition() == add(idRef(idRef('a'), 'c'), idRef('b'))
    }

    def "a.c(x)+b"() {
      expect:
        addition() == add(method(idRef('a'), 'c', idRef('x')), idRef('b'))
    }

    def methodMissing(String name, Object args) {
        Parser.methodMissing(name, args)
    }

}
