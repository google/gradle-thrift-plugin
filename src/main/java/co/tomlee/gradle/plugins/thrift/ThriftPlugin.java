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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import javax.inject.Inject;
import java.io.File;
import org.gradle.api.model.ObjectFactory;

public class ThriftPlugin implements Plugin<Project> {

    private final ObjectFactory objectFactory;

    @Inject
    public ThriftPlugin(final ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaLibraryPlugin.class);

        configureConfigurations(project);
        configureSourceSets(project);
    }

    private void configureConfigurations(final Project project) {
        final Configuration thriftConfiguration = project.getConfigurations().create("thrift").setVisible(false);
        project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME).extendsFrom(thriftConfiguration);
    }

    private void configureSourceSets(final Project project) {
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                //
                // This logic borrowed from the antlr plugin.
                // 1. Add a new 'thrift' virtual directory mapping
                //
                final ThriftSourceVirtualDirectoryImpl thriftSourceSet
                        = new ThriftSourceVirtualDirectoryImpl(((DefaultSourceSet) sourceSet).getDisplayName(), objectFactory);
                new DslObject(sourceSet).getConvention().getPlugins().put("thrift", thriftSourceSet);
                final String srcDir = String.format("src/%s/thrift", sourceSet.getName());
                thriftSourceSet.getThrift().srcDir(srcDir);
                sourceSet.getAllSource().source(thriftSourceSet.getThrift());

                //
                // 2. Create a ThriftTask for this sourceSet
                //
                final String taskName = sourceSet.getTaskName("generate", "ThriftSource");
                final ThriftTask thriftTask = project.getTasks().create(taskName, ThriftTask.class);
                thriftTask.setDescription(String.format("Processes the %s Thrift IDLs.", sourceSet.getName()));

                //
                // 3. Set up convention mapping for default sources (allows user to not have to specify)
                //
                thriftTask.setSource(thriftSourceSet.getThrift());

                //
                // 4. Set up the thrift output directory (adding to javac inputs)
                //
                final String outputDirectoryName
                        = String.format("%s/generated-src/thrift/%s", project.getBuildDir(), sourceSet.getName());
                final File outputDirectory = new File(outputDirectoryName);
                thriftTask.out(outputDirectory);
                sourceSet.getJava().srcDir(outputDirectory);

                //
                // 5. Register the fact that thrit should run before compiling.
                //
                project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(taskName);
            }
        });
    }
}
