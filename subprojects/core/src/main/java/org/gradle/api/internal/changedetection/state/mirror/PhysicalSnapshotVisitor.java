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

package org.gradle.api.internal.changedetection.state.mirror;

/**
 * Visitor for {@link PhysicalSnapshot}.
 */
public interface PhysicalSnapshotVisitor {

    /**
     * Called before visiting the contents of a directory.
     *
     * @return Whether the subtree should be visited.
     */
    boolean preVisitDirectory(PhysicalSnapshot directorySnapshot);

    /**
     * Called for each regular/missing file.
     */
    void visit(PhysicalSnapshot fileSnapshot);

    /**
     * Called when leaving a directory.
     */
    void postVisitDirectory();
}
