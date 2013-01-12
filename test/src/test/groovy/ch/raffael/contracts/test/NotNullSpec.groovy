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
package ch.raffael.contracts.test

import spock.lang.Specification

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("GroovyPointlessArithmetic")
class NotNullSpec extends Specification {

    ///**
    // * I'm currently using this one to check some details out with the debugger.
    // * Let it fail (or not).
    // */
    //@Ignore
    //def "Various experimentsbv"() {
    //  given:
    //    def loader = new TestClassLoader(getClass().getClassLoader())
    //
    //  when:
    //    //def cls2 = Class.forName("ch.raffael.util.contracts.test.cls.TestClass\$Inner", false, loader).newInstance();
    //    //println cls2
    //    def cls = Class.forName("ch.raffael.util.contracts.test.cls.TestClass", false, loader)
    //    cls.newInstance().notNull(null)
    //
    //  then:
    //    true
    //    //def e = thrown(NullPointerException)
    //    //e.printStackTrace()
    //}
    //
    ////def "@NotNull method does not throw NullPointerException when not returning null"() {
    ////  given:
    ////    def loader = new TestClassLoader(getClass().getClassLoader())
    ////
    ////  when:
    ////    def cls = Class.forName("ch.raffael.util.contracts.test.cls.TestClass", false, loader)
    ////    cls.newInstance().notNull("Foo")
    ////
    ////  then:
    ////    true
    ////}

}
