package org.geoserver.cluster.hazelcast;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CatalogVisitor;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourcePool;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geotools.data.DataStore;

/**
 * Proxy for {@link CatalogInfo} concrete interfaces used to notify the local cluster member of
 * objects removed on another member.
 * <p>
 * The only methods proxied are {@link CatalogInfo#getId() getId()}, {@code getName()} for the ones
 * that have such a method, and {@link CatalogInfo#accept(CatalogVisitor)}.
 * <p>
 * {@code accept(CatalogVisitor)} is crucial for the {@link ResourcePool} catalog listener to be
 * able of disposing locally cached resources such as {@link DataStore} instances.
 *
 */
class RemovedObjectProxy implements InvocationHandler {

    private final String id;

    private final String name;

    private final Class<? extends Info> infoInterface;

    public RemovedObjectProxy(String id, String name, Class<? extends Info> infoInterface) {
        checkNotNull(id, "id");
        checkNotNull(name, "name");
        checkNotNull(infoInterface, "infoInterface");
        checkArgument(infoInterface.isInterface(), "%s is not an interface", infoInterface);
        this.id = id;
        this.name = name;
        this.infoInterface = infoInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getid".equalsIgnoreCase(method.getName())) {
            return id;
        }
        if ("getname".equalsIgnoreCase(method.getName())) {
            return name;
        }
        if ("accept".equals(method.getName())) {
            proxyVisitory(proxy, method, (CatalogVisitor) args[0]);
        }
        Class<?> returnType = method.getReturnType();
        if (List.class.isAssignableFrom(returnType)) {
            return Collections.EMPTY_LIST;
        }
        return null;
    }

    // void accept( CatalogVisitor visitor );
    private void proxyVisitory(Object proxy, Method method, CatalogVisitor catalogVisitor) {
        if (NamespaceInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((NamespaceInfo) proxy);
        } else if (WorkspaceInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((WorkspaceInfo) proxy);
        } else if (CoverageInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((CoverageInfo) proxy);
        } else if (FeatureTypeInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((FeatureTypeInfo) proxy);
        } else if (WMSLayerInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((WMSLayerInfo) proxy);
        } else if (CoverageStoreInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((CoverageStoreInfo) proxy);
        } else if (DataStoreInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((DataStoreInfo) proxy);
        } else if (WMSStoreInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((WMSStoreInfo) proxy);
        } else if (StyleInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((StyleInfo) proxy);
        } else if (LayerInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((LayerInfo) proxy);
        } else if (LayerGroupInfo.class.equals(infoInterface)) {
            catalogVisitor.visit((LayerGroupInfo) proxy);
        } else if (Catalog.class.equals(infoInterface)) {
            catalogVisitor.visit((Catalog) proxy);
        }
    }

    @Override
    public String toString() {
        return "RemovedObjectProxy[(" + infoInterface.getSimpleName() + "[id=" + id + ", name="
                + name + "]]";
    }

}
