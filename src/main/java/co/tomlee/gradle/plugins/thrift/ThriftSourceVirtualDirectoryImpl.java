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
