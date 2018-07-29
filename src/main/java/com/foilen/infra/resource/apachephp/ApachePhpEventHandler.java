/*
    Foilen Infra Resource ApachePhp
    https://github.com/foilen/foilen-infra-resource-apachephp
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.apachephp;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.AbstractFinalStateManagedResourcesEventHandler;
import com.foilen.infra.plugin.v1.core.eventhandler.FinalStateManagedResource;
import com.foilen.infra.plugin.v1.core.eventhandler.FinalStateManagedResourcesUpdateEventHandlerContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.website.Website;
import com.foilen.smalltools.tools.FreemarkerTools;

public class ApachePhpEventHandler extends AbstractFinalStateManagedResourcesEventHandler<ApachePhp> {

    @Override
    protected void commonHandlerExecute(CommonServicesContext services, FinalStateManagedResourcesUpdateEventHandlerContext<ApachePhp> context) {

        context.addManagedResourceTypes(Application.class);

        IPResourceService resourceService = services.getResourceService();

        ApachePhp apachePhp = context.getResource();

        // Get the links
        List<Machine> machines = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, LinkTypeConstants.INSTALLED_ON, Machine.class);
        List<UnixUser> unixUsers = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, LinkTypeConstants.RUN_AS, UnixUser.class);
        List<ApachePhpFolder> folders = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, LinkTypeConstants.USES, ApachePhpFolder.class);
        List<AttachablePart> attachedParts = resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(apachePhp, "ATTACHED", AttachablePart.class);

        // Validate links
        boolean proceed = true;
        if (machines.isEmpty()) {
            logger.info("No machine to install on. Skipping");
            proceed = false;
        }
        if (unixUsers.size() > 1) {
            logger.warn("Too many unix user to run as");
            throw new IllegalUpdateException("Must have a singe unix user to run as. Got " + unixUsers.size());
        }
        if (unixUsers.isEmpty()) {
            logger.info("No unix user to run as. Skipping");
            proceed = false;
        }

        if (proceed) {

            UnixUser unixUser = unixUsers.get(0);
            Long unixUserId = unixUser.getId();

            // Create an Application
            Application application = new Application();
            FinalStateManagedResource applicationFinalState = new FinalStateManagedResource();
            applicationFinalState.setManagedResource(application);
            context.addManagedResources(applicationFinalState);
            application.setName(apachePhp.getName());
            application.setDescription(apachePhp.getResourceDescription());

            IPApplicationDefinition applicationDefinition = new IPApplicationDefinition();
            application.setApplicationDefinition(applicationDefinition);
            applicationDefinition.setRunAs(unixUserId);

            // Sort by aliases ; deeper first
            Collections.sort(folders, (a, b) -> {
                int result = Integer.compare(b.getAlias().length(), a.getAlias().length()); // Longer first
                if (result == 0) {
                    result = a.getAlias().compareTo(b.getAlias());
                }
                return result;
            });

            applicationDefinition.setFrom("foilen/fcloud-docker-apache_php5:1.0.0");

            // Apache and PHP config
            IPApplicationDefinitionAssetsBundle assetsBundle = applicationDefinition.addAssetsBundle();
            assetsBundle.addAssetResource("/etc/apache2/ports.conf", "/com/foilen/infra/resource/apachephp/apache-ports.conf");
            assetsBundle.addAssetResource("/apache-start.sh", "/com/foilen/infra/resource/apachephp/apache-start.sh");
            assetsBundle.addAssetResource("/etc/php5/apache2/conf.d/99-fcloud.ini", "/com/foilen/infra/resource/apachephp/php.ini");

            // Site configuration
            Map<String, Object> model = new HashMap<>();
            applicationDefinition.addVolume(new IPApplicationDefinitionVolume(apachePhp.getBasePath(), "/base"));
            model.put("baseFolder", "/base");

            List<ApachePhpFolder> containerFolders = folders.stream() //
                    .map(it -> new ApachePhpFolder("/folders/" + it.getFolder().replaceAll("\\/", "_"), it.getAlias())) //
                    .collect(Collectors.toList());
            model.put("aliases", containerFolders);
            for (int i = 0; i < folders.size(); ++i) {
                applicationDefinition.addVolume(new IPApplicationDefinitionVolume(folders.get(i).getFolder(), containerFolders.get(i).getFolder()));
            }

            assetsBundle.addAssetContent("/etc/apache2/sites-enabled/000-default.conf", FreemarkerTools.processTemplate("/com/foilen/infra/resource/apachephp/apache-http-fs.ftl", model));

            applicationDefinition.addBuildStepCommand("chmod 644 /etc/apache2/ports.conf /etc/php5/apache2/conf.d/99-fcloud.ini ; chmod 755 /apache-start.sh");

            applicationDefinition.addVolume(new IPApplicationDefinitionVolume(null, "/var/lock/apache2", unixUserId, unixUserId, "755"));

            applicationDefinition.addContainerUserToChangeId("www-data", unixUserId);

            applicationDefinition.addBuildStepCommand("chmod -R 777 /var/log");
            applicationDefinition.addBuildStepCommand("chown www-data:www-data /var/run/apache2");
            applicationDefinition.addService("apache", "/apache-start.sh");

            applicationDefinition.addPortEndpoint(8080, DockerContainerEndpoints.HTTP_TCP);

            // Link machines
            applicationFinalState.addManagedLinksToType(LinkTypeConstants.INSTALLED_ON);
            machines.forEach(it -> applicationFinalState.addLinkTo(LinkTypeConstants.INSTALLED_ON, it));

            // Link unix user
            applicationFinalState.addManagedLinksToType(LinkTypeConstants.RUN_AS);
            unixUsers.forEach(it -> applicationFinalState.addLinkTo(LinkTypeConstants.RUN_AS, it));

            // Sync link websites
            List<Website> websitesFrom = resourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Website.class, LinkTypeConstants.POINTS_TO, apachePhp);
            applicationFinalState.addManagedLinksFromType(LinkTypeConstants.POINTS_TO);
            websitesFrom.forEach(it -> applicationFinalState.addLinkFrom(it, LinkTypeConstants.POINTS_TO));

            // Attach parts in a deterministic order
            logger.debug("attachedParts ; amount {}", attachedParts.size());
            attachedParts.stream() //
                    .sorted((a, b) -> a.getResourceName().compareTo(b.getResourceName())) //
                    .forEach(attachedPart -> {
                        logger.debug("Attaching {} with type {}", attachedPart.getResourceName(), attachedPart.getClass().getName());
                        attachedPart.attachTo(services, null, null, application, applicationDefinition);
                    });

        }

    }

    @Override
    public Class<ApachePhp> supportedClass() {
        return ApachePhp.class;
    }

}
