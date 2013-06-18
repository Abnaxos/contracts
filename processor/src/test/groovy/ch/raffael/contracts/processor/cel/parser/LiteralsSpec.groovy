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
package ch.raffael.contracts.processor.cel.parser

import ch.raffael.contracts.processor.cel.Position
import ch.raffael.contracts.processor.cel.ast.Nodes
import ch.raffael.contracts.util.NeedsWork
import org.antlr.v4.runtime.CommonToken
import spock.lang.Specification

import static ch.raffael.contracts.processor.cel.ast.Literal.Kind.*

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class LiteralsSpec extends Specification {

    private final static POS = new Position(1, 0)

    def "Integer literals"() {
      expect:
        Literals.integer(new CommonToken(CelLexer.INT, str)) == node

      where:
        str                    | node
        '123'                  | Nodes.literal(POS, INT, 123)
        '0x123'                | Nodes.literal(POS, INT, 0x123)
        '0X123'                | Nodes.literal(POS, INT, 0x123)
        '0123'                 | Nodes.literal(POS, INT, 0123)

        '123l'                 | Nodes.literal(POS, LONG, 123L)
        '0x123L'               | Nodes.literal(POS, LONG, 0x123L)
        '0X123l'               | Nodes.literal(POS, LONG, 0x123L)
        '0123L'                | Nodes.literal(POS, LONG, 0123L)

        '2147483647'           | Nodes.literal(POS, INT, Integer.MAX_VALUE)
        '2147483648'           | Nodes.blank(POS)

        '9223372036854775807L' | Nodes.literal(POS, LONG, Long.MAX_VALUE)
        '9223372036854775808L' | Nodes.blank(POS)
    }

    def "Escape sequences"() {
      expect:
        Literals.string(new CommonToken(CelLexer.STRING, '"' + esc + '"'), STRING) == Nodes.literal(POS, STRING, str)

      where:
        esc       | str
        '\\n'     | '\n'
        '\\r'     | '\r'
        '\\b'     | '\b'
        '\\f'     | '\f'
        '\\\\'    | '\\'
        '\\"'     | '"'
        '\\\''    | '\''
        '\\t'     | '\t'

        '\\uabcd' | '\uabcd'

        '\\400'   | '\400' // => ' 0'
        '\\377'   | '\377'
        '\\0'     | '\0'
    }

    @NeedsWork(description = "Check for all those hundreds of ways of writing float literals")
    def "Float and Double literals"() {
      expect:
        Literals.floatingPoint(new CommonToken(CelLexer.FLOAT, str)) == node

      where:
        str        | node
        '0d'       | Nodes.literal(POS, DOUBLE, 0.doubleValue())
        '0f'       | Nodes.literal(POS, FLOAT, 0.floatValue())
        '.0'       | Nodes.literal(POS, DOUBLE, 0.doubleValue())
        '.0d'      | Nodes.literal(POS, DOUBLE, 0.doubleValue())
        '.0f'      | Nodes.literal(POS, FLOAT, 0.floatValue())
        '0.0'      | Nodes.literal(POS, DOUBLE, 0.doubleValue())
        '0.0d'     | Nodes.literal(POS, DOUBLE, 0.doubleValue())
        '0.0f'     | Nodes.literal(POS, FLOAT, 0.floatValue())
        '0xa.bP3'  | Nodes.literal(POS, DOUBLE, Double.valueOf('0xa.bP3'))
        '0xa.bP3d' | Nodes.literal(POS, DOUBLE, Double.valueOf('0xa.bP3'))
        '0xa.bP3f' | Nodes.literal(POS, FLOAT, Float.valueOf('0xa.bP3'))
    }

}
