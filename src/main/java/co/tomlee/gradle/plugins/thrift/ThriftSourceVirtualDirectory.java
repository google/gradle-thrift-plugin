/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.tomlee.gradle.plugins.thrift;

import groovy.lang.Closure;
import javax.annotation.Nonnull;
import org.gradle.api.file.SourceDirectorySet;

public interface ThriftSourceVirtualDirectory {

    @Nonnull
    public SourceDirectorySet getThrift();

    public ThriftSourceVirtualDirectory thrift(Closure closure);
}
