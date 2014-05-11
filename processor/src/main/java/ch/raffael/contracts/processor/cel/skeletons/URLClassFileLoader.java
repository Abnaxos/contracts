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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.google.common.io.ByteStreams;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class URLClassFileLoader implements ClassFileLoader {

    private final Locator locator;

    public URLClassFileLoader(Locator locator) {
        this.locator = locator;
    }

    @Override
    public byte[] loadClassFile(String className) throws IOException {
        try {
            URL url = locator.locateClassFile(className);
            if ( url == null ) {
                return null;
            }
            try ( InputStream input = url.openStream() ) {
                return ByteStreams.toByteArray(input);
            }
        }
        catch ( FileNotFoundException e ) {
            return null;
        }
    }

    @FunctionalInterface
    public static interface Locator {
        URL locateClassFile(String className) throws IOException;
    }

}
