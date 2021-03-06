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

package org.gradle.internal.fingerprint;

import org.gradle.api.internal.changedetection.state.NormalizedFileSnapshot;
import org.gradle.api.internal.changedetection.state.mirror.PhysicalDirectorySnapshot;
import org.gradle.api.internal.changedetection.state.mirror.PhysicalMissingSnapshot;
import org.gradle.caching.internal.BuildCacheHasher;
import org.gradle.internal.file.FileType;
import org.gradle.internal.hash.HashCode;

public class IgnoredPathFingerprint implements NormalizedFileSnapshot {

    public static final IgnoredPathFingerprint DIRECTORY = new IgnoredPathFingerprint(FileType.Directory, PhysicalDirectorySnapshot.SIGNATURE);
    private static final IgnoredPathFingerprint MISSING_FILE = new IgnoredPathFingerprint(FileType.Missing, PhysicalMissingSnapshot.SIGNATURE);

    private final FileType type;
    private final HashCode normalizedContentHash;

    public static IgnoredPathFingerprint create(FileType type, HashCode contentHash) {
        switch (type) {
            case Directory:
                return DIRECTORY;
            case Missing:
                return MISSING_FILE;
            case RegularFile:
                return new IgnoredPathFingerprint(FileType.RegularFile, contentHash);
            default:
                throw new IllegalStateException();
        }
    }

    private IgnoredPathFingerprint(FileType type, HashCode normalizedContentHash) {
        this.type = type;
        this.normalizedContentHash = normalizedContentHash;
    }

    @Override
    public String getNormalizedPath() {
        return "";
    }

    @Override
    public HashCode getNormalizedContentHash() {
        return normalizedContentHash;
    }

    @Override
    public FileType getType() {
        return type;
    }

    @Override
    public int compareTo(NormalizedFileSnapshot o) {
        if (!(o instanceof IgnoredPathFingerprint)) {
            return -1;
        }
        return normalizedContentHash.compareTo(o.getNormalizedContentHash());
    }

    @Override
    public void appendToHasher(BuildCacheHasher hasher) {
        hasher.putHash(normalizedContentHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IgnoredPathFingerprint that = (IgnoredPathFingerprint) o;
        return normalizedContentHash.equals(that.normalizedContentHash);
    }

    @Override
    public int hashCode() {
        return normalizedContentHash.hashCode();
    }

    @Override
    public String toString() {
        return String.format("IGNORED / %s", getType() == FileType.Directory ? "DIR" : getType() == FileType.Missing ? "MISSING" : normalizedContentHash);
    }
}
