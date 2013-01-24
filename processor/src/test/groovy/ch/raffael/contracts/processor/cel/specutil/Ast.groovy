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
package ch.raffael.contracts.processor.cel.specutil

import ch.raffael.contracts.processor.cel.Position
import ch.raffael.contracts.processor.cel.ast.*
import ch.raffael.util.common.UnreachableCodeException
import org.codehaus.groovy.runtime.StackTraceUtils

/**
 * A simple helper class that provides a more concise way of building an AST.
 * Just syntactic sugar, nothing more.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Ast {

    static BlankNode blank() {
        Nodes.blank(where())
    }

    static Assertion assertion(AstNode expression, boolean isFinally=false) {
        Nodes.assertion(where(), expression, isFinally)
    }

    static IfExpression ifExpr(AstNode condition, AstNode expression) {
        Nodes.ifExpression(where(), condition, expression)
    }

    static ConditionalOp conditional(AstNode condition, AstNode onTrue, AstNode onFalse) {
        Nodes.conditionalOp(where(), condition, onTrue, onFalse)
    }

    static LogicalOp logOr(AstNode left, AstNode right) {
        Nodes.logicalOp(where(), LogicalOp.Kind.OR, left, right)
    }

    static LogicalOp logAnd(AstNode left, AstNode right) {
        Nodes.logicalOp(where(), LogicalOp.Kind.AND, left, right)
    }

    static BitwiseOp bitOr(AstNode left, AstNode right) {
        Nodes.bitwiseOp(where(), BitwiseOp.Kind.OR, left, right)
    }

    static BitwiseOp bitXor(AstNode left, AstNode right) {
        Nodes.bitwiseOp(where(), BitwiseOp.Kind.XOR, left, right)
    }

    static BitwiseOp bitAnd(AstNode left, AstNode right) {
        Nodes.bitwiseOp(where(), BitwiseOp.Kind.AND, left, right)
    }

    static EqualityOp eq(AstNode left, AstNode right) {
        Nodes.equalityOp(where(), EqualityOp.Kind.EQUAL, left, right)
    }

    static EqualityOp ne(AstNode left, AstNode right) {
        Nodes.equalityOp(where(), EqualityOp.Kind.NOT_EQUAL, left, right)
    }

    static RelationalOp gt(AstNode left, AstNode right) {
        Nodes.relationalOp(where(), RelationalOp.Kind.GREATER_THAN, left, right)
    }

    static RelationalOp ge(AstNode left, AstNode right) {
        Nodes.relationalOp(where(), RelationalOp.Kind.GREATER_OR_EQUAL, left, right)
    }

    static RelationalOp lt(AstNode left, AstNode right) {
        Nodes.relationalOp(where(), RelationalOp.Kind.LESS_THAN, left, right)
    }

    static RelationalOp le(AstNode left, AstNode right) {
        Nodes.relationalOp(where(), RelationalOp.Kind.LESS_OR_EQUAL, left, right)
    }

    static ShiftOp shiftLeft(AstNode left, AstNode right) {
        Nodes.shiftOp(where(), ShiftOp.Kind.LEFT, left, right)
    }

    static ShiftOp shiftRight(AstNode left, AstNode right) {
        Nodes.shiftOp(where(), ShiftOp.Kind.RIGHT, left, right)
    }

    static ShiftOp ushiftRight(AstNode left, AstNode right) {
        Nodes.shiftOp(where(), ShiftOp.Kind.UNSIGNED_RIGHT, left, right)
    }

    static ArithmeticOp add(AstNode left, AstNode right) {
        Nodes.arithmeticOp(where(), ArithmeticOp.Kind.ADD, left, right)
    }

    static ArithmeticOp sub(AstNode left, AstNode right) {
        Nodes.arithmeticOp(where(), ArithmeticOp.Kind.SUB, left, right)
    }

    static ArithmeticOp mul(AstNode left, AstNode right) {
        Nodes.arithmeticOp(where(), ArithmeticOp.Kind.MUL, left, right)
    }

    static ArithmeticOp div(AstNode left, AstNode right) {
        Nodes.arithmeticOp(where(), ArithmeticOp.Kind.DIV, left, right)
    }

    static ArithmeticOp mod(AstNode left, AstNode right) {
        Nodes.arithmeticOp(where(), ArithmeticOp.Kind.MOD, left, right)
    }

    static UnaryOp pos(AstNode expr) {
        Nodes.unaryOp(where(), UnaryOp.Kind.POS, expr)
    }

    static UnaryOp neg(AstNode expr) {
        Nodes.unaryOp(where(), UnaryOp.Kind.NEG, expr)
    }

    static UnaryOp bitNot(AstNode expr) {
        Nodes.unaryOp(where(), UnaryOp.Kind.BITWISE_NOT, expr)
    }

    static UnaryOp logNot(AstNode expr) {
        Nodes.unaryOp(where(), UnaryOp.Kind.LOGICAL_NOT, expr)
    }

    static IdReference idRef(AstNode source = null, String id) {
        Nodes.idReference(where(), source, id)
    }

    static MethodCall method(AstNode source = null, String methodName, AstNode... args) {
        Nodes.methodCall(where(), source, methodName, args as List)
    }

    static ArrayAccess array(AstNode source, AstNode index) {
        Nodes.arrayAccess(where(), source, index)
    }

    private static Position where() {
        def stackTrace = StackTraceUtils.sanitize(new Exception()).getStackTrace()
        for ( elem in stackTrace ) {
            if ( !(elem.className ==~ "${Ast.class.name}(\\\$.*)?") ) {
                return new Position(elem.lineNumber, 0)
            }
        }
        throw new UnreachableCodeException()
    }

}
