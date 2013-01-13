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
package ch.raffael.contracts.processor.cel;

import javax.annotation.concurrent.Immutable;

import ch.raffael.contracts.NonNegative;
import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Positive;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Immutable
public final class Position implements Comparable<Position> {

    private final int line;
    private final int charInLine;

    public Position(@Positive int line, @NonNegative int charInLine) {
        this.line = line;
        this.charInLine = charInLine;
    }

    @Override
    public String toString() {
        return line + ":" + charInLine;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        Position that = (Position)o;
        return line == that.line && charInLine == that.charInLine;
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + charInLine;
        return result;
    }

    @Positive
    public int getLine() {
        return line;
    }

    @NonNegative
    public int getCharInLine() {
        return charInLine;
    }

    @NotNull
    public Position baseOn(Position base) {
        if ( line == 1 ) {
            return new Position(line, charInLine + base.charInLine);
        }
        else {
            return new Position(line + base.line - 1, charInLine);
        }
    }

    @Override
    public int compareTo(Position that) {
        if ( this.line > that.line ) {
            return 1;
        }
        else if ( this.line < that.line ) {
            return -1;
        }
        else if ( this.charInLine > that.charInLine ) {
            return 1;
        }
        else if ( this.charInLine < that.charInLine ) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
