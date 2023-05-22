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
