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
package ch.raffael.contracts.agent;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;

import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.Nullable;
import ch.raffael.contracts.Require;
import ch.raffael.contracts.internal.Log;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Transformer implements ClassFileTransformer {

    private final Log log = Log.getInstance();
    private volatile ContractsManager contractsManager = null;

    Transformer() {
    }

    @Override
    @Nullable
    public byte[] transform(@NotNull ClassLoader loader,
                            @NotNull String className,
                            @Require("classBeingRedefined==null") Class<?> classBeingRedefined,
                            @Nullable ProtectionDomain protectionDomain,
                            @NotNull byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if ( contractsManager == null ) {
            // synchronization: we don't care, getInstance() will always return the same
            contractsManager = ContractsManager.getInstance();
        }
        try {
            ClassPool pool = contractsManager.getClassPool(loader);
            CtClass ctClass = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
            boolean modified = false;
            if ( ctClass.getName().equals("ch.raffael.util.contracts.internal.ContractsContext") ) {
                modified = true;
                log.debug("Adding violation reporting to ContractsContext");
                CtField mgrField = new CtField(pool.get(ContractsManager.class.getName()), "MANAGER", ctClass);
                mgrField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
                ctClass.addField(mgrField, ContractsManager.class.getName() + ".getInstance()");
                CtMethod violation = ctClass.getDeclaredMethod("violation", new CtClass[] { pool.get("ch.raffael.util.contracts.ContractViolation") });
                violation.insertBefore("MANAGER.reportViolation(this, $1);");
            }
            if ( modified ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Instrumented class %s", ctClass.getName());
                }
                return ctClass.toBytecode();
            }
            else {
                return null;
            }
        }
        catch ( Exception e ) {
            log.error("Error instrumenting class %s", e, className);
            return null;
        }
    }
}
