/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest;

import br.unb.cic.bionimbus.controller.ControllerManager;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vinicius
 */
public class RestResourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerManager.class);

    private final JobController jobController;

    private final CloudMessageService cms;

    @Inject
    public RestResourceManager(JobController jobController, CloudMessageService cms) {
        // Verify if it is null
        Preconditions.checkNotNull(jobController);
        Preconditions.checkNotNull(cms);

        // Injects it
        this.jobController = jobController;
        this.cms = cms;

    }

}
