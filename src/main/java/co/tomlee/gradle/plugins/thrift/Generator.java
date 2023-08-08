// Copyright (c) 2023 Google LLC
// Copyright (c) 2016-2020 Shevek
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Generator implements Serializable {

    private final String name;
    private final List<String> options = new ArrayList<>();
    private Object out;

    public Generator() {
        this.name = null;
    }

    public Generator(String name) {
        this.name = name;
    }

    public void option(String option) {
        options.add(option);
    }

    public void options(List<String> options) {
        this.options.addAll(options);
    }

    public void out(Object out) {
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public List<String> getOptions() {
        return options;
    }

    public Object getOut() {
        return out;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Generator) {
            final Generator otherGenerator = (Generator) other;
            return name.equals(((Generator) other).name)
                    && options.equals(((Generator) other).options)
                    && Objects.equals(out, ((Generator) other).out);
        }
        return false;
    }
}
