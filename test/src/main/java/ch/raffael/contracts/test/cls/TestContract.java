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

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class TestContract {

    public static void test(Object obj) {
        if ( obj == null ) {
            RuntimeException exception =  new NullPointerException("(TestClass.java:12): Returning null");
            StackTraceElement[] stackTrace = exception.getStackTrace();
            StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length - 1];
            System.arraycopy(stackTrace, 1, newStackTrace, 0, newStackTrace.length);
            newStackTrace[0] = new StackTraceElement("ch.raffael.util.contracts.test.cls.TestClass", "notNull", "TestClass.java", 13);
            exception.setStackTrace(newStackTrace);
            throw exception;
        }
    }

}
