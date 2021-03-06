/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.changedetection.state;

import net.rubygrapefruit.platform.ThreadSafe;
import org.gradle.api.internal.changedetection.state.mirror.FileSystemSnapshot;
import org.gradle.api.internal.changedetection.state.mirror.PhysicalSnapshot;

import javax.annotation.Nullable;

/**
 * Maintains an in-memory mirror of the state of the filesystem.
 *
 * This is intended to only be used by {@link DefaultFileSystemSnapshotter}. Use {@link FileSystemSnapshotter} instead.
 */
@ThreadSafe
public interface FileSystemMirror {
    @Nullable
    PhysicalSnapshot getFile(String absolutePath);

    void putFile(PhysicalSnapshot file);

    @Nullable
    Snapshot getContent(String absolutePath);

    void putContent(String absolutePath, Snapshot snapshot);

    @Nullable
    FileSystemSnapshot getDirectoryTree(String absolutePath);

    void putDirectory(String absolutePath, FileSystemSnapshot directory);
}
