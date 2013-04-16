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
package ch.raffael.contracts.processor.ct;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class FileClassSource implements ClassSource {

    private final Path path;

    public FileClassSource(@NotNull Path path) {
        this.path = path.toAbsolutePath();

    }

    @Override
    @Nullable
    public URL findClass(ClassName name) throws IOException {
        Path file = path.resolve(name.toInternal().replaceAll("/", path.getFileSystem().getSeparator() + ".class"));
        if ( Files.isRegularFile(file) ) {
            return file.toUri().toURL();
        }
        else {
            return null;
        }
    }

    @NotNull
    public Path getPath() {
        return path;
    }
}
