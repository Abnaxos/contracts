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
package ch.raffael.contracts.test.cls;

import ch.raffael.contracts.processor.pmap.ParameterMap;

import static ch.raffael.contracts.processor.pmap.ParameterMap.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@ParameterMap(
        methods = {
                @Method(descriptor = "equals(Ljava/lang/Object;)Z",
                        parameterNames = { "other" })
        },
        innerClasses = {
                @InnerClass(
                        name = "MyInner",
                        methods = {
                                @Method(descriptor = "wait(J)V",
                                        parameterNames = { "timeout" })
                        }
                ),
                @InnerClass(
                        name = "MyInner.InnerOfInner",
                        methods = {
                                @Method(descriptor = "hashCode()I", parameterNames = {})
                        }
                )
        }
)
public class PMapTest {

}
