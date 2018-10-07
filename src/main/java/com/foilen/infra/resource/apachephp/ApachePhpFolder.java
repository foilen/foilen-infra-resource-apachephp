/*
    Foilen Infra Resource ApachePhp
    https://github.com/foilen/foilen-infra-resource-apachephp
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;
import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * This is the details of the folder structure.
 */
public class ApachePhpFolder extends AbstractIPResource {

    public static final String RESOURCE_TYPE = "Apache and PHP - Folder";

    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_FOLDER = "folder";
    public static final String PROPERTY_ALIAS = "alias";

    // Details
    private String uid;
    private String folder;
    private String alias;

    public ApachePhpFolder() {
        uid = SecureRandomTools.randomBase64String(10);
    }

    public ApachePhpFolder(String folder, String alias) {
        uid = SecureRandomTools.randomBase64String(10);
        this.folder = folder;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public String getFolder() {
        return folder;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return folder + " -> " + alias;
    }

    @Override
    public String getResourceName() {
        return uid;
    }

    public String getUid() {
        return uid;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
