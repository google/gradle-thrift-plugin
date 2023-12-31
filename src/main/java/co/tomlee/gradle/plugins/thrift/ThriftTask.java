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
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.gradle.api.file.FileTree;

public class ThriftTask extends SourceTask {

    private File thrift;

    private final NamedDomainObjectContainer<Generator> generators = getProject().container(Generator.class);

    private final ArrayList<File> include = new ArrayList<>();

    private SourceDirectorySet source;

    private File out = getProject().file("build/generated-src/thrift");

    private boolean recurse = true;

    private boolean verbose = false;

    private boolean strict = false;

    private boolean debug = false;

    @TaskAction
    public void invokeThrift(final IncrementalTaskInputs inputs) throws Exception {
        final ArrayList<File> inputFiles = new ArrayList<>();
        if (inputs.isIncremental()) {
            inputs.outOfDate(new Action<InputFileDetails>() {
                @Override
                public void execute(InputFileDetails inputFileDetails) {
                    if (inputFileDetails.isAdded() || inputFileDetails.isModified()) {
                        inputFiles.add(inputFileDetails.getFile());
                    }
                }
            });
        } else {
            inputFiles.addAll(getSource().getFiles());
        }

        for (final Generator generator : generators) {
            for (final File file : inputFiles) {
                final File out = generatorOutputDirectory(generator);

                final List<String> command = buildCommand(generator, out, file.getAbsolutePath());
                getProject().getLogger().info("Running thrift: " + command);
                if (!out.isDirectory()) {
                    if (!out.mkdirs()) {
                        throw new GradleException("Could not create thrift output directory: " + out);
                    }
                }
                final CountDownLatch latch = new CountDownLatch(2);

                try {
                    final Process p = new ProcessBuilder(command).start();
                    new SlurpThread(latch, p.getInputStream(), System.out).start();
                    new SlurpThread(latch, p.getErrorStream(), System.err).start();

                    if (p.waitFor() != 0) {
                        throw new GradleException(getExecutable() + " command failed");
                    }
                    latch.await();
                } catch (GradleException e) {
                    throw e;
                } catch (Exception e) {
                    throw new GradleException("Unexpected error while executing thrift: " + e.getMessage(), e);
                }
            }
        }
    }

    @OutputDirectories
    public Set<File> getOutputDirectories() {
        final HashSet<File> files = new HashSet<>();
        boolean useSharedOutputDir = false;
        for (final Generator generator : generators) {
            useSharedOutputDir |= generator.getOut() == null;
            files.add(generatorOutputDirectory(generator));
        }
        if (useSharedOutputDir) {
            files.add(this.out);
        }
        return files;
    }

    @InputFiles
    @SkipWhenEmpty
    @Override
    @PathSensitive(value = PathSensitivity.RELATIVE)
    public SourceDirectorySet getSource() {
        return source;
    }

    @Override
    public void setSource(Object source) {
        super.setSource(source);
        if (source instanceof SourceDirectorySet)
            this.source = (SourceDirectorySet) source;
    }

    @Override
    public void setSource(FileTree source) {
        setSource((Object) source);
    }

    @Input
    public Map<String, Generator> getGenerators() {
        return generators.getAsMap();
    }

    @Input
    public List<File> getInclude() {
        return include;
    }

    @Input
    public boolean isRecurse() {
        return recurse;
    }

    @Input
    public boolean isVerbose() {
        return verbose;
    }

    @Input
    public boolean isStrict() {
        return strict;
    }

    @Input
    public boolean isDebug() {
        return debug;
    }

    @Input
    public String getExecutable() {
        return this.thrift != null ? this.thrift.getAbsolutePath() : "thrift";
    }

    public void out(Object dir) {
        this.out = getProject().file(dir);
    }

    public void recurse(boolean recurse) {
        this.recurse = recurse;
    }

    public void verbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void strict(boolean strict) {
        this.strict = strict;
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    public void path(Object file) {
        include.add(getProject().file(file));
    }

    public void generators(Closure c) {
        generators.configure(c);
    }

    public void executable(final Object executable) {
        this.thrift = getProject().file(executable);
    }

    public List<String> buildCommand(final Generator generator, File out, String fileName) {
        final String thrift = getExecutable();
        final List<String> command = new ArrayList<>(Arrays.asList(thrift, "-out", out.getAbsolutePath()));
        final List<String> options = generator.getOptions();
        final String arguments = options.isEmpty() ? "" : ":" + join(",", options);

        command.add("--gen");
        command.add(generator.getName() + arguments);
        for (final File include : this.include) {
            command.add("-I");
            command.add(include.getAbsolutePath());
        }
        if (recurse)
            command.add("-recurse");
        if (verbose)
            command.add("-verbose");
        if (strict)
            command.add("-strict");
        if (debug)
            command.add("-debug");
        command.add(fileName);
        return command;
    }

    private static String join(final String sep, final List<String> arg) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arg.size(); i++) {
            sb.append(arg.get(i));
            if (i < arg.size() - 1) {
                sb.append(sep);
            }
        }
        return sb.toString();
    }

    private File generatorOutputDirectory(final Generator generator) {
        if (generator.getOut() != null) {
            return getProject().file(generator.getOut());
        } else {
            return this.out;
        }
    }

    private final class SlurpThread extends Thread {

        private final CountDownLatch latch;
        private final InputStream in;
        private final PrintStream out;

        public SlurpThread(final CountDownLatch latch, final InputStream in, final PrintStream out) {
            setDaemon(true);

            this.latch = latch;
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                final InputStreamReader reader = new InputStreamReader(in);
                final char[] buf = new char[8 * 1024];
                for (;;) {
                    try {
                        if (reader.read(buf) <= 0) {
                            break;
                        }
                        out.print(buf);
                    } catch (IOException e) {
                        getLogger().error("Failed to read from input stream", e);
                        break;
                    }
                }
            } finally {
                latch.countDown();
            }
        }
    }
}
