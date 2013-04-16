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

import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import ch.raffael.contracts.NonNegative;
import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Nullable;

import static ch.raffael.contracts.processor.util.Identifiers.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class ClassName {

    public static final ClassName BOOLEAN = new ClassName("boolean", 0, 'Z');
    public static final ClassName BYTE = new ClassName("byte", 0, 'B');
    public static final ClassName SHORT = new ClassName("short", 0, 'S');
    public static final ClassName INT = new ClassName("int", 0, 'I');
    public static final ClassName LONG = new ClassName("long", 0, 'J');
    public static final ClassName CHAR = new ClassName("char", 0, 'C');
    public static final ClassName FLOAT = new ClassName("float", 0, 'F');
    public static final ClassName DOUBLE = new ClassName("double", 0, 'D');
    public static final ClassName VOID = new ClassName("void", 0, 'V');

    public static final ClassName BOOLEANW = new ClassName("java/lang/Boolean", 0);
    public static final ClassName BYTEW = new ClassName("java/lang/Byte", 0);
    public static final ClassName SHORTW = new ClassName("java/lang/Short", 0);
    public static final ClassName INTW = new ClassName("java/lang/Integer", 0);
    public static final ClassName LONGW = new ClassName("java/lang/Long", 0);
    public static final ClassName CHARW = new ClassName("java/lang/Character", 0);
    public static final ClassName FLOATW = new ClassName("java/lang/Float", 0);
    public static final ClassName DOUBLEW = new ClassName("java/lang/Double", 0);
    public static final ClassName VOIDW = new ClassName("java/lang/Void", 0);

    public static final ClassName OBJECT = new ClassName("java/lang/Object", 0);
    public static final ClassName STRING = new ClassName("java/lang/String", 0);
    public static final ClassName SERIALIZABLE = new ClassName("java/io/Serializable", 0);
    public static final ClassName CLONEABLE = new ClassName("java/lang/Cloneable", 0);

    private static final Splitter INAME_SPLITTER = Splitter.on('/');
    private static final Splitter BNAME_SPLITTER = Splitter.on('.');

    private static final Map<Character, ClassName> BY_CODE = ImmutableMap.<Character, ClassName>builder()
            .put('Z', BOOLEAN)
            .put('B', BYTE)
            .put('S', SHORT)
            .put('I', INT)
            .put('J', LONG)
            .put('C', CHAR)
            .put('F', FLOAT)
            .put('D', DOUBLE)
            .put('V', VOID)
            .build();
    private static final Map<String, ClassName> BY_NAME = ImmutableMap.<String, ClassName>builder()
            .put("boolean", BOOLEAN)
            .put("byte", BYTE)
            .put("short", SHORT)
            .put("int", INT)
            .put("long", LONG)
            .put("char", CHAR)
            .put("float", FLOAT)
            .put("double", DOUBLE)
            .put("void", VOID)
            .build();

    private final char code;
    private final String internalName;
    private final int arrayDepth;

    private ClassName(String internalName, int arrayDepth, char code) {
        this.internalName = internalName;
        this.arrayDepth = arrayDepth;
        this.code = code;
    }

    private ClassName(String internalName, int arrayDepth) {
        this(internalName, arrayDepth, 'L');
    }

    @Override
    public String toString() {
        if ( arrayDepth > 0 ) {
            return internalName + "[" + arrayDepth + "]";
        }
        else {
            return internalName;
        }
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        ClassName that = (ClassName)o;
        return arrayDepth == that.arrayDepth && internalName.equals(that.internalName);
    }

    @Override
    public int hashCode() {
        return 31 * internalName.hashCode() + arrayDepth;
    }

    @NotNull
    public static ClassName forBinary(@NotNull String name) throws IllegalClassNameException {
        return internalOrBinary(name, "Binary", BNAME_SPLITTER);
    }

    @NotNull
    public static ClassName forInternal(@NotNull String name) throws IllegalClassNameException {
        return internalOrBinary(name, "Internal", INAME_SPLITTER);
    }

    @NotNull
    public static ClassName forDescriptor(@NotNull String name) throws IllegalClassNameException {
        if ( name.isEmpty() ) {
            throw new IllegalClassNameException("Descriptor: " + name);
        }
        if ( name.charAt(0) == '[' ) {
            // for arrays, we can use exactly the same code as for internal names
            return internalOrBinary(name, "Descriptor", INAME_SPLITTER);
        }
        if ( name.charAt(0) == 'L' ) {
            String cname = extractClass(name);
            if ( cname == null ) {
                throw new IllegalClassNameException("Descriptor: " + name);
            }
            return new ClassName(internalClassName(cname, "Descriptor", INAME_SPLITTER, name), 0);
        }
        else {
            if ( name.length() != 1 ) {
                throw new IllegalClassNameException("Descriptor: " + name);
            }
            ClassName result = BY_CODE.get(name.charAt(0));
            if ( result == null ) {
                throw new IllegalClassNameException("Descriptor: " + name);
            }
            return result;
        }
    }

    @NotNull
    private static ClassName internalOrBinary(@NotNull String name, @NotNull String type, @NotNull Splitter splitter) throws IllegalClassNameException {
        int arrayDepth = 0;
        for ( int i = 0; i < name.length(); i++ ) {
            if ( name.charAt(i) == '[' ) {
                arrayDepth++;
            }
        }
        String n = name.substring(arrayDepth);
        if ( n.isEmpty() ) {
            throw new IllegalClassNameException(type + ": " + name);
        }
        if ( arrayDepth == 0 ) {
            return lookup(internalClassName(n, type, splitter, name), 0);
        }
        else if ( n.charAt(0) == 'L' ) {
            String cname = extractClass(n);
            if ( cname == null ) {
                throw new IllegalClassNameException(type + ": " + name);
            }
            return new ClassName(internalClassName(cname, type, splitter, name), arrayDepth);
        }
        else {
            if ( n.length() != 1 ) {
                throw new IllegalClassNameException(type + ": " + name);
            }
            ClassName cname = BY_CODE.get(n.charAt(0));
            if ( cname == null || cname == VOID ) {
                throw new IllegalClassNameException(type + ": " + name);
            }
            return cname.withArrayDepth(arrayDepth);
        }
    }

    private static String internalClassName(String name, String type, Splitter splitter, String originalName) throws IllegalClassNameException {
        StringBuilder buf = new StringBuilder(name.length());
        for ( String seg : splitter.split(name) ) {
            if ( !isValidIdentifier(seg) ) {
                throw new IllegalClassNameException(type + ": " + originalName);
            }
            if ( buf.length() > 0 ) {
                buf.append('/');
            }
            buf.append(seg);
        }
        return buf.toString();
    }

    @Nullable
    private static String extractClass(@NotNull String lname) {
        if ( lname.length() < 3 ) {
            return null;
        }
        if ( lname.charAt(0) != 'L' || lname.charAt(lname.length() - 1) != ';' ) {
            return null;
        }
        return lname.substring(1, lname.length() - 1);
    }

    private static ClassName lookup(String name, int arrayDepth) {
        ClassName n = BY_NAME.get(name);
        if ( n == null ) {
            return new ClassName(name, arrayDepth);
        }
        else if ( arrayDepth > 0 ) {
            return n.withArrayDepth(arrayDepth);
        }
        else {
            return n;
        }
    }

    @NotNull
    public String getName() {
        return internalName;
    }

    @NonNegative
    public int getArrayDepth() {
        return arrayDepth;
    }

    public boolean isPrimitive() {
        return arrayDepth <= 0 && code != 'L';
    }

    public char getCode() {
        return code;
    }

    @NotNull
    public ClassName withArrayDepth(@NonNegative int arrayDepth) {
        if ( arrayDepth == this.arrayDepth ) {
            return this;
        }
        else {
            return new ClassName(internalName, arrayDepth);
        }
    }

    @NotNull
    public String toBinary() {
        if ( arrayDepth > 0 ) {
            if ( isPrimitive() ) {
                return arrayStr(String.valueOf(code));
            }
            else {
                return arrayStr("L" + internalName.replace('/', '.') + ";");
            }
        }
        else {
            return internalName.replace('/', '.');
        }
    }

    @NotNull
    public String toInternal() {
        if ( arrayDepth > 0 ) {
            return toDescriptor();
        }
        else {
            return internalName;
        }
    }

    @NotNull
    public String toDescriptor() {
        if ( arrayDepth > 0 ) {
            if ( isPrimitive() ) {
                return arrayStr(String.valueOf(code));
            }
            else {
                return arrayStr("L" + internalName + ";");
            }
        }
        else {
            if ( isPrimitive() ) {
                return String.valueOf(code);
            }
            else {
                return "L" + internalName + ";";
            }
        }
    }

    private String arrayStr(String name) {
        StringBuilder buf = new StringBuilder(name.length() + arrayDepth);
        for ( int i = 0; i < arrayDepth; i++ ) {
            buf.append('[');
        }
        return buf.append(name).toString();
    }

}
