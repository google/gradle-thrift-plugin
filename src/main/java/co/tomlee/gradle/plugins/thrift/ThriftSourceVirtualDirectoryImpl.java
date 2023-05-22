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
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import static org.gradle.api.reflect.TypeOf.typeOf;
import org.gradle.util.ConfigureUtil;

public class ThriftSourceVirtualDirectoryImpl implements ThriftSourceVirtualDirectory, HasPublicType {

    private static final String[] filters = {"**/*.thrift"};

    private final SourceDirectorySet thrift;

    public ThriftSourceVirtualDirectoryImpl(String parentDisplayName, ObjectFactory objectFactory) {
        final String displayName = String.format("%s Thrift source", parentDisplayName);
        this.thrift = objectFactory.sourceDirectorySet(parentDisplayName + ".thrift", displayName);
        this.thrift.getFilter().include(filters);
    }

    @Override
    public SourceDirectorySet getThrift() {
        return thrift;
    }

    @Override
    public ThriftSourceVirtualDirectory thrift(Closure closure) {
        ConfigureUtil.configure(closure, getThrift());
        return this;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return typeOf(ThriftSourceVirtualDirectory.class);
    }
}
