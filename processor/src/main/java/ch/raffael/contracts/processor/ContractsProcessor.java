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
package ch.raffael.contracts.processor;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import ch.raffael.contracts.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class ContractsProcessor {

    public static final String CONTRACTS_CLASS_SUFFIX = "$$ch$raffael$contracts";

    public static final String CTOR_NAME = "<init>";

    private static final BiMap<String, String> BINARY_TO_DESCRIPTOR =
            ImmutableBiMap.<String, String>builder()
                    .put("int", "I")
                    .put("long", "J")
                    .put("short", "S")
                    .put("byte", "B")
                    .put("double", "D")
                    .put("float", "F")
                    .put("char", "C")
                    .put("boolean", "Z")
                    .put("void", "V")
                    .build();

    private ContractsProcessor() {
    }

    @NotNull
    public static String toInternalName(@NotNull String binaryName) {
        return binaryName.replace('.', '/');
    }

    @NotNull
    public static String toDescriptor(@NotNull String internalName) {
        String descriptor = BINARY_TO_DESCRIPTOR.get(internalName);
        if ( descriptor != null ) {
            return descriptor;
        }
        else {
            return "L" + internalName + ";";
        }
    }

    @NotNull
    public static String getContractsClassName(@NotNull String binaryName) {
        return binaryName + CONTRACTS_CLASS_SUFFIX;
    }

}
