/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.compile.incremental.deps;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import org.gradle.internal.serialize.AbstractSerializer;
import org.gradle.internal.serialize.Decoder;
import org.gradle.internal.serialize.Encoder;
import org.gradle.internal.serialize.IntSetSerializer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassSetAnalysisData {
    final Map<String, DependentsSet> dependents;
    final Map<String, IntSet> classesToConstants;
    final Map<String, Set<String>> classesToChildren;
    final String fullRebuildCause;

    public ClassSetAnalysisData(Map<String, DependentsSet> dependents, Map<String, IntSet> classesToConstants, Map<String, Set<String>> classesToChildren, String fullRebuildCause) {
        this.dependents = dependents;
        this.classesToConstants = classesToConstants;
        this.classesToChildren = classesToChildren;
        this.fullRebuildCause = fullRebuildCause;
    }

    public DependentsSet getDependents(String className) {
        if (fullRebuildCause != null) {
            return DependentsSet.dependencyToAll(fullRebuildCause);
        }
        DependentsSet dependentsSet = dependents.get(className);
        return dependentsSet == null ? DependentsSet.empty() : dependentsSet;
    }

    public IntSet getConstants(String className) {
        IntSet integers = classesToConstants.get(className);
        if (integers == null) {
            return IntSets.EMPTY_SET;
        }
        return integers;
    }

    public Set<String> getChildren(String className) {
        Set<String> children = classesToChildren.get(className);
        return children == null ? Collections.<String>emptySet() : children;
    }

    public static class Serializer extends AbstractSerializer<ClassSetAnalysisData> {

        @Override
        public ClassSetAnalysisData read(Decoder decoder) throws Exception {
            Map<Integer, String> classNameMap = new HashMap<Integer, String>();

            int count = decoder.readSmallInt();
            ImmutableMap.Builder<String, DependentsSet> dependentsBuilder = ImmutableMap.builder();
            for (int i = 0; i < count; i++) {
                String className = readClassName(decoder, classNameMap);
                DependentsSet dependents = readDependentsSet(decoder, classNameMap);
                dependentsBuilder.put(className, dependents);
            }

            count = decoder.readSmallInt();
            ImmutableMap.Builder<String, IntSet> classesToConstantsBuilder = ImmutableMap.builder();
            for (int i = 0; i < count; i++) {
                String className = readClassName(decoder, classNameMap);
                IntSet constants = IntSetSerializer.INSTANCE.read(decoder);
                classesToConstantsBuilder.put(className, constants);
            }

            count = decoder.readSmallInt();
            ImmutableMap.Builder<String, Set<String>> classNameToChildren = ImmutableMap.builder();
            for (int i = 0; i < count; i++) {
                String parent = readClassName(decoder, classNameMap);
                int nameCount = decoder.readSmallInt();
                ImmutableSet.Builder<String> namesBuilder = ImmutableSet.builder();
                for (int j = 0; j < nameCount; j++) {
                    namesBuilder.add(readClassName(decoder, classNameMap));
                }
                classNameToChildren.put(parent, namesBuilder.build());
            }

            String fullRebuildCause = decoder.readNullableString();

            return new ClassSetAnalysisData(dependentsBuilder.build(), classesToConstantsBuilder.build(), classNameToChildren.build(), fullRebuildCause);
        }

        @Override
        public void write(Encoder encoder, ClassSetAnalysisData value) throws Exception {
            Map<String, Integer> classNameMap = new HashMap<String, Integer>();

            encoder.writeSmallInt(value.dependents.size());
            for (Map.Entry<String, DependentsSet> entry : value.dependents.entrySet()) {
                writeClassName(entry.getKey(), classNameMap, encoder);
                writeDependentSet(entry.getValue(), classNameMap, encoder);
            }

            encoder.writeSmallInt(value.classesToConstants.size());
            for (Map.Entry<String, IntSet> entry : value.classesToConstants.entrySet()) {
                writeClassName(entry.getKey(), classNameMap, encoder);
                IntSetSerializer.INSTANCE.write(encoder, entry.getValue());
            }

            encoder.writeSmallInt(value.classesToChildren.size());
            for (Map.Entry<String, Set<String>> entry : value.classesToChildren.entrySet()) {
                writeClassName(entry.getKey(), classNameMap, encoder);
                encoder.writeSmallInt(entry.getValue().size());
                for (String className : entry.getValue()) {
                    writeClassName(className, classNameMap, encoder);
                }
            }

            encoder.writeNullableString(value.fullRebuildCause);
        }

        private DependentsSet readDependentsSet(Decoder decoder, Map<Integer, String> classNameMap) throws IOException {
            byte b = decoder.readByte();
            if (b == 1) {
                return DependentsSet.dependencyToAll(decoder.readNullableString());
            }
            int count = decoder.readSmallInt();
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (int i = 0; i < count; i++) {
                builder.add(readClassName(decoder, classNameMap));
            }
            return DependentsSet.dependents(builder.build());
        }

        private void writeDependentSet(DependentsSet dependentsSet, Map<String, Integer> classNameMap, Encoder encoder) throws IOException {
            if (dependentsSet.isDependencyToAll()) {
                encoder.writeByte((byte) 1);
                encoder.writeNullableString(dependentsSet.getDescription());
            } else {
                encoder.writeByte((byte) 2);
                encoder.writeSmallInt(dependentsSet.getDependentClasses().size());
                for (String className : dependentsSet.getDependentClasses()) {
                    writeClassName(className, classNameMap, encoder);
                }
            }
        }

        private String readClassName(Decoder decoder, Map<Integer, String> classNameMap) throws IOException {
            int id = decoder.readSmallInt();
            if (id == 0) {
                id = decoder.readSmallInt();
                String className = decoder.readString();
                classNameMap.put(id, className);
                return className;
            }
            return classNameMap.get(id);
        }

        private void writeClassName(String className, Map<String, Integer> classIdMap, Encoder encoder) throws IOException {
            Integer id = classIdMap.get(className);
            if (id == null) {
                id = classIdMap.size() + 1;
                classIdMap.put(className, id);
                encoder.writeSmallInt(0);
                encoder.writeSmallInt(id);
                encoder.writeString(className);
            } else {
                encoder.writeSmallInt(id);
            }
        }
    }
}
