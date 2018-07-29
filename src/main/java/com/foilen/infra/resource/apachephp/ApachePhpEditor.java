/*
    Foilen Infra Resource ApachePhp
    https://github.com/foilen/foilen-infra-resource-apachephp
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditor;
import com.foilen.infra.plugin.v1.core.visual.editor.simpleresourceditor.SimpleResourceEditorDefinition;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonFormatting;
import com.foilen.infra.plugin.v1.core.visual.helper.CommonValidation;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.DirectoryTools;
import com.google.common.base.Strings;

public class ApachePhpEditor extends SimpleResourceEditor<ApachePhp> {

    public static final String EDITOR_NAME = "Apache PHP";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_NAME, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::toLowerCase);
            fieldConfig.addFormator(CommonFormatting::trimSpaces);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
            fieldConfig.addValidator(CommonValidation::validateAlphaNumLower);
        });

        simpleResourceEditorDefinition.addInputText(ApachePhp.PROPERTY_BASE_PATH, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(DirectoryTools::cleanupDots);
            fieldConfig.addFormator(path -> {
                if (!Strings.isNullOrEmpty(path)) {
                    if (path.charAt(0) != '/') {
                        return "/" + path;
                    }
                }
                return path;
            });
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addResource("unixUser", LinkTypeConstants.RUN_AS, UnixUser.class);
        simpleResourceEditorDefinition.addReverseResources("websitesFrom", Website.class, LinkTypeConstants.POINTS_TO);
        simpleResourceEditorDefinition.addResources("folders", LinkTypeConstants.USES, ApachePhpFolder.class);
        simpleResourceEditorDefinition.addResources("attachableParts", "ATTACHED", AttachablePart.class);
        simpleResourceEditorDefinition.addResources("machines", LinkTypeConstants.INSTALLED_ON, Machine.class);

    }

    @Override
    public Class<ApachePhp> getForResourceType() {
        return ApachePhp.class;
    }

}
