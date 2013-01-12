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
package ch.raffael.contracts.internal

import spock.lang.Specification

import static ch.raffael.contracts.internal.ContractsContext.getContext

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings(["GroovyPointlessArithmetic", "GroovyAccessibility"])
class ContractsPolicySpec extends Specification {

    def "Creation of children"() {
      given:
        def name = '$' + UUID.randomUUID().toString().replaceAll('-', '.\\$')
        def prevSize = ContractsContext.CONTEXTS.size()

      when: "Get a policy"
        def p = getContext(name)
      and: "Get the same policy a second time"
        def p2 = getContext(name)

      then:
        p != null
        p == p2
        ContractsContext.CONTEXTS.size() == prevSize + 5
    }

    def "Invalid names throw IllegalArgumentException"() {
      when:
        getContext(name)

      then:
        def iae = thrown(IllegalArgumentException)
        iae.message.startsWith('Illegal policy name:')

      where:
        name << [
                '',
                '.',
                ' ',
                'foo.3bar',
                '.foo.bar',
                'foo.bar.',
                'foo..bar'
        ]
    }

    def "Use class name as policy name"() {
      when:
        def p = getContext(ContractsPolicySpec)

      then:
        p.name == ContractsPolicySpec.name
    }

    def "Use outer class for inner classes"() {
      when:
        def p = getContext(ContractsPolicySpec.Inner)

      then:
        p.name == ContractsPolicySpec.name
    }

    def "Use package name as policy name"() {
      when:
        def p = getContext(ContractsPolicySpec.getPackage())

      then:
        p.name == ContractsPolicySpec.getPackage().getName()
    }

    private class Inner {}

}
