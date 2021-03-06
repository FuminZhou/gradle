/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.api.internal.tasks.compile.incremental.recomp;

import org.gradle.api.Action;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.internal.tasks.compile.incremental.classpath.ClasspathSnapshot;
import org.gradle.api.internal.tasks.compile.incremental.classpath.ClasspathSnapshotProvider;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.File;
import java.util.Collection;

public class CurrentCompilation {
    private final IncrementalTaskInputs inputs;
    private final JavaCompileSpec spec;
    private final ClasspathSnapshotProvider classpathSnapshotProvider;

    public CurrentCompilation(IncrementalTaskInputs inputs, JavaCompileSpec spec, ClasspathSnapshotProvider classpathSnapshotProvider) {

        this.inputs = inputs;
        this.spec = spec;
        this.classpathSnapshotProvider = classpathSnapshotProvider;
    }

    public ClasspathSnapshot getClasspathSnapshot() {
        return classpathSnapshotProvider.getClasspathSnapshot(spec.getCompileClasspath());
    }

    public Collection<File> getAnnotationProcessorPath() {
        return spec.getAnnotationProcessorPath();
    }

    public void visitChanges(Action<InputFileDetails> action) {
        inputs.outOfDate(action);
        inputs.removed(action);
    }
}
