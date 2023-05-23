// Copyright (c) 2023 Google LLC
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
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
