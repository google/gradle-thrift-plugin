package co.tomlee.gradle.plugins.thrift;

import groovy.lang.Closure;
import javax.annotation.Nonnull;
import org.gradle.api.file.SourceDirectorySet;

public interface ThriftSourceVirtualDirectory {

    @Nonnull
    public SourceDirectorySet getThrift();

    public ThriftSourceVirtualDirectory thrift(Closure closure);
}
