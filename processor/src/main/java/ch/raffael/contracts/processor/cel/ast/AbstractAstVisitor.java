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
package ch.raffael.contracts.processor.cel.ast;

/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public abstract class AbstractAstVisitor implements AstVisitor {

    protected AbstractAstVisitor() {
    }

    @Override
    public void visit(Clause clause) {
        clause.children().forEach(c -> {c.accept(this);});
    }

    @Override
    public void visit(Imply imply) {
        imply.getCondition().accept(this);
        imply.getImplies().accept(this);
    }

    @Override
    public void visit(ConditionalOp conditionalOp) {
        conditionalOp.getCondition().accept(this);
        conditionalOp.getOnTrue().accept(this);
        conditionalOp.getOnFalse().accept(this);
    }

    @Override
    public void visit(LogicalOp logicalOp) {
        logicalOp.getLeft().accept(this);
        logicalOp.getRight().accept(this);
    }

    @Override
    public void visit(BitwiseOp bitwiseOp) {
        bitwiseOp.getLeft().accept(this);
        bitwiseOp.getRight().accept(this);
    }

    @Override
    public void visit(EqualityOp equalityOp) {
        equalityOp.getLeft().accept(this);
        equalityOp.getRight().accept(this);
    }

    @Override
    public void visit(RelationalOp relationalOp) {
        relationalOp.getLeft().accept(this);
        relationalOp.getRight().accept(this);
    }

    @Override
    public void visit(ShiftOp shiftOp) {
        shiftOp.getLeft().accept(this);
        shiftOp.getRight().accept(this);
    }

    @Override
    public void visit(ArithmeticOp arithmeticOp) {
        arithmeticOp.getLeft().accept(this);
        arithmeticOp.getRight().accept(this);
    }

    @Override
    public void visit(UnaryOp unaryOp) {
        unaryOp.getExpression().accept(this);
    }

    @Override
    public void visit(IdReference idReference) {
        if ( idReference.getSource() != null ) {
            idReference.getSource().accept(this);
        }
    }

    @Override
    public void visit(MethodCall methodCall) {
        if ( methodCall.getSource() != null ) {
            methodCall.getSource().accept(this);
        }
        for ( AstNode arg : methodCall.getArguments() ) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        arrayAccess.getSource().accept(this);
    }

    @Override
    public void visit(Literal literal) {
    }

    @Override
    public void visit(BlankNode blankNode) {
    }

}
