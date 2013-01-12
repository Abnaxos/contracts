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
package ch.raffael.contracts.processor.util;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.google.common.io.NullOutputStream;

import ch.raffael.contracts.NotNull;
import ch.raffael.util.common.UnexpectedException;

import static com.google.common.base.Preconditions.*;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class Digester implements DataOutput {

    public static final String ALGORITHM = "SHA-1";

    private final DigestOutputStream digester = new DigestOutputStream(new NullOutputStream(), messageDigest());
    private final DataOutputStream data = new DataOutputStream(digester);

    @NotNull
    private static MessageDigest messageDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new UnexpectedException(e);
        }
    }

    private byte[] digest = null;

    @Override
    public void write(int b) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.write(b);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void write(@NotNull byte[] b) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.write(b);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.write(b, off, len);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeBoolean(boolean v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeBoolean(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeByte(int v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeByte(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeShort(int v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeShort(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeChar(int v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeChar(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeInt(int v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeInt(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeLong(long v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeLong(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeFloat(float v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeFloat(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeDouble(double v) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeDouble(v);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeBytes(@NotNull String s) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeBytes(s);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeChars(@NotNull String s) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeChars(s);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeUTF(@NotNull String s) {
        checkState(digest == null, "Digest already calculated");
        try {
            data.writeUTF(s);
        }
        catch ( IOException e ) {
            throw new UnexpectedException(e);
        }
    }

    @NotNull
    public byte[] digest() {
        if ( digest == null ) {
            try {
                data.close();
            }
            catch ( IOException e ) {
                throw new UnexpectedException(e);
            }
            digest = digester.getMessageDigest().digest();
        }
        return Arrays.copyOf(digest, digest.length);
    }

}
