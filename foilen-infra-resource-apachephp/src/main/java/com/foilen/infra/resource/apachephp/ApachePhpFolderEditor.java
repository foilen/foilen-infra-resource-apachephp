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
import com.foilen.smalltools.tools.DirectoryTools;

public class ApachePhpFolderEditor extends SimpleResourceEditor<ApachePhpFolder> {

    public static final String EDITOR_NAME = "Apache PHP Folder";

    @Override
    protected void getDefinition(SimpleResourceEditorDefinition simpleResourceEditorDefinition) {

        simpleResourceEditorDefinition.addInputText(ApachePhpFolder.PROPERTY_FOLDER, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(DirectoryTools::cleanupDots);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

        simpleResourceEditorDefinition.addInputText(ApachePhpFolder.PROPERTY_ALIAS, fieldConfig -> {
            fieldConfig.addFormator(CommonFormatting::trimSpacesAround);
            fieldConfig.addFormator(DirectoryTools::cleanupDots);
            fieldConfig.addValidator(CommonValidation::validateNotNullOrEmpty);
        });

    }

    @Override
    public Class<ApachePhpFolder> getForResourceType() {
        return ApachePhpFolder.class;
    }

}
