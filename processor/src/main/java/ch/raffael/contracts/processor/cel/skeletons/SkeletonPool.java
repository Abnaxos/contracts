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

import java.util.Map;

import com.google.common.collect.MapMaker;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class SkeletonPool {

    private final Map<String, SkeletonHolder> skeletons = new MapMaker().concurrencyLevel(1).makeMap();

    private final SkeletonClassLoader loader;

    public SkeletonPool(ClassFileLoader loader) {
        this.loader = new SkeletonClassLoader(loader);
    }

    public Skeleton get(String className) throws ClassNotFoundException {
        return skeletons.computeIfAbsent(className, SkeletonHolder::new).get();
    }

    public Skeleton getIfPresent(String className) {
        try {
            return get(className);
        }
        catch ( ClassNotFoundException e ) {
            return null;
        }
    }

    public void clear(String className) {
        skeletons.remove(className);
    }

    public void clearAll() {
        skeletons.clear();
    }

    public boolean contains(Skeleton skeleton) {
        return skeleton.equals(getIfPresent(skeleton.className()));
    }

    private class SkeletonHolder {

        private final String className;
        private volatile Skeleton skeleton = null;

        private SkeletonHolder(String className) {
            this.className = className;
        }

        public Skeleton get() throws ClassNotFoundException {
            if ( skeleton == null ) {
                synchronized ( this ) {
                    if ( skeleton == null ) {
                        return new Skeleton(Class.forName(className, false, loader));
                    }
                }
            }
            return skeleton;
        }

    }

}
