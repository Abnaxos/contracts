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

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.WeakHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javassist.ClassPool;

import ch.raffael.contracts.ContractViolationError;
import ch.raffael.contracts.NotNull;
import ch.raffael.contracts.internal.ContractsContext;
import ch.raffael.contracts.internal.Log;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@SuppressWarnings("ConstantConditions")
public final class ContractsManager implements ContractsManagerMBean {

    private static final ContractsManager INSTANCE = new ContractsManager();

    private final Log log = Log.getInstance();
    private Instrumentation instrumentation = null;
    private final WeakHashMap<ClassLoader, ClassPool> classPools = new WeakHashMap<ClassLoader, ClassPool>();

    private ContractsManager() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            server.registerMBean(this, ObjectName.getInstance(MBEAN_NAME));
            log.info("Registered MBean %s", MBEAN_NAME);
        }
        catch ( Exception e ) {
            log.error("Error registering MBean %s", e, MBEAN_NAME);
        }
    }

    public static void premain(String args, Instrumentation instrumentation) {
        getInstance().instrumentation = instrumentation;
        instrumentation.addTransformer(new Transformer());
    }

    public static ContractsManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void enable(@NotNull String name) {
        if ( name == null ) throw new NullPointerException("name");
        ContractsContext.getContext(name).enable();
    }

    @Override
    public void disable(@NotNull String name) {
        if ( name == null ) throw new NullPointerException("name");
        ContractsContext.getContext(name).disable();
    }

    @Override
    public void isEnabled(@NotNull String name) {
        if ( name == null ) throw new NullPointerException("name");
        ContractsContext.getContext(name).isEnabled();
    }

    public void reportViolation(@NotNull ContractsContext context, @NotNull ContractViolationError violation) {
        if ( context == null ) throw new NullPointerException("context");
        if ( violation == null ) throw new NullPointerException("violation");
        // FIXME: not implemented
    }

    @NotNull
    ClassPool getClassPool(@NotNull ClassLoader loader) {
        return null;
    }

}
