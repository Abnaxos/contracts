/*
 * Copyright 2012-2014 Raffael Herzog
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
package ch.raffael.contracts.processor.cel.skeletons;

import java.io.File;
import java.io.IOException;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ClassFileLoader {

    byte[] loadClassFile(String className) throws IOException;

    static String nameToPath(String name) {
        return nameToPath(name, '/');
    }

    static String nameToFilePath(String name) {
        return nameToPath(name, File.separatorChar);
    }

    static String nameToPath(String name, char separator) {
        return name.replace('.', separator) + ".class";
    }

}
