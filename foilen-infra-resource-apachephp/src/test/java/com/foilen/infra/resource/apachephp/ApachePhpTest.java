/*
    Foilen Infra Resource ApachePhp
    https://github.com/foilen/foilen-infra-resource-apachephp
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import java.util.Collections;

import org.junit.Test;

import com.foilen.infra.plugin.core.system.fake.junits.AbstractIPPluginTest;
import com.foilen.infra.plugin.core.system.junits.JunitsHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.apachephp.ApachePhp;
import com.foilen.infra.resource.apachephp.ApachePhpEditor;
import com.foilen.infra.resource.apachephp.ApachePhpFolder;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;

public class ApachePhpTest extends AbstractIPPluginTest {

    @Test
    public void test_basic() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser unixUser = new UnixUser(2000, "myphp", "/home/myphp", null, null);
        Website website = new Website("myphp");
        website.setDomainNames(Collections.singleton("myphp.example.com"));

        ApachePhp apachePhp = new ApachePhp();
        apachePhp.setName("myphp");
        apachePhp.setBasePath("/home/myphp/www");
        apachePhp.setResourceEditorName(ApachePhpEditor.EDITOR_NAME);

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(apachePhp);
        changes.resourceAdd(website);

        // Create links
        changes.linkAdd(apachePhp, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(apachePhp, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, apachePhp);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpTest_test_basic-state.json", getClass(), true);
    }

    @Test
    public void test_withFolders() {

        // Create resources
        Machine machine = new Machine("h1.example.com", "192.168.0.200");
        UnixUser unixUser = new UnixUser(2000, "myphp", "/home/myphp", null, null);
        Website website = new Website("myphp");
        website.setDomainNames(Collections.singleton("myphp.example.com"));

        ApachePhp apachePhp = new ApachePhp();
        apachePhp.setName("myphp");
        apachePhp.setBasePath("/home/myphp/www");
        apachePhp.setResourceEditorName(ApachePhpEditor.EDITOR_NAME);

        ApachePhpFolder apachePhpFolder = new ApachePhpFolder("/home/myphp/other", "/other");
        apachePhpFolder.setUid("the_uid");

        ChangesContext changes = new ChangesContext(getCommonServicesContext().getResourceService());
        changes.resourceAdd(machine);
        changes.resourceAdd(unixUser);
        changes.resourceAdd(apachePhp);
        changes.resourceAdd(website);
        changes.resourceAdd(apachePhpFolder);

        // Create links
        changes.linkAdd(apachePhp, LinkTypeConstants.RUN_AS, unixUser);
        changes.linkAdd(apachePhp, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(apachePhp, LinkTypeConstants.USES, apachePhpFolder);
        changes.linkAdd(website, LinkTypeConstants.INSTALLED_ON, machine);
        changes.linkAdd(website, LinkTypeConstants.POINTS_TO, apachePhp);

        // Execute
        getInternalServicesContext().getInternalChangeService().changesExecute(changes);

        // Assert
        JunitsHelper.assertState(getCommonServicesContext(), getInternalServicesContext(), "ApachePhpTest_test_withFolders-state.json", getClass(), true);
    }

}
