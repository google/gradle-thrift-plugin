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
package org.anarres.gradle.plugin.thrift;

import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
@RunWith(Parameterized.class)
public class ThriftPluginApplyTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThriftPluginApplyTest.class);

    @Nonnull
    private static Object[] A(Object... in) {
        return in;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> parameters() throws Exception {
        return Arrays.asList(
                // A("2.12"),   // No longer works.
                // A("2.14"),
                // A("3.0"),
                // A("3.2.1"),  // No longer works.
                // A("4.10.3"), // No longer works.
                A("5.6"),
                A("6.1.1"),
                A("6.4.1")
        );
    }

    private final String gradleVersion;
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    public File testProjectBuildFile;

    @Before
    public void setUp() throws Exception {
        testProjectBuildFile = testProjectDir.newFile("build.gradle");
    }

    public ThriftPluginApplyTest(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

    @Test
    public void testApply() throws Exception {
        String text = "plugins { id 'com.google.gradle.thrift' }\n";
        Files.write(text, testProjectBuildFile, StandardCharsets.UTF_8);

        GradleRunner runner = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withPluginClasspath()
                .withDebug(true)
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("tasks");
        LOG.info("Building...\n\n");
        // System.out.println("ClassPath is " + runner.getPluginClasspath());
        BuildResult result = runner.build();
        LOG.info("Output:\n\n" + result.getOutput() + "\n\n");
    }

}
