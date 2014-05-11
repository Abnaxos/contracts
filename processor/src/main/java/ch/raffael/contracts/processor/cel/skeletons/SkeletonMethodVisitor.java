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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Opcodes.ASM5;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class SkeletonMethodVisitor extends MethodVisitor {

    private static final Type SKELETON_EXCEPTION = Type.getType(SkeletonInvocationError.class);
    private static final Method SKELETON_EXCEPTION_CONSTRUCTOR = Method.getMethod("void <init>()");

    private final GeneratorAdapter gen;
    private int lineNumber = -1;

    protected SkeletonMethodVisitor(String className, GeneratorAdapter mv) {
        super(ASM5, mv);
        this.gen = mv;
    }

    public SkeletonMethodVisitor(MethodVisitor methodVisitor, int access, String name, String desc, String className) {
        super(ASM5, new GeneratorAdapter(methodVisitor, access, name, desc));
        gen = (GeneratorAdapter)mv;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        gen.newInstance(SKELETON_EXCEPTION);
        gen.dup();
        gen.invokeConstructor(SKELETON_EXCEPTION, SKELETON_EXCEPTION_CONSTRUCTOR);
        gen.throwException();
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
    }

    @Override
    public void visitInsn(int opcode) {
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
    }

    @Override
    public void visitLabel(org.objectweb.asm.Label label) {
    }

    @Override
    public void visitLdcInsn(Object cst) {
    }

    @Override
    public void visitIincInsn(int var, int increment) {
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, org.objectweb.asm.Label... labels) {
    }

    @Override
    public void visitLookupSwitchInsn(org.objectweb.asm.Label dflt, int[] keys, Label[] labels) {
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return null;
    }

    @Override
    public void visitTryCatchBlock(org.objectweb.asm.Label start, org.objectweb.asm.Label end, org.objectweb.asm.Label
            handler, String type) {
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return null;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, org.objectweb.asm.Label
            start, org.objectweb.asm.Label end, int index) {
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, org.objectweb.asm.Label[] start, org.objectweb.asm.Label[] end, int[] index, String desc, boolean visible) {
        return null;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        if ( lineNumber < 0 ) {
            lineNumber = line;
        }
        else {
            lineNumber = Math.min(lineNumber, line);
        }
    }

    int getLineNumber() {
        return lineNumber;
    }

}
