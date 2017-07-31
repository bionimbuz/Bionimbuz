/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest;

import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import com.google.inject.AbstractModule;

/**
 *
 * @author Vinicius
 */
public class RestResourceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JobController.class).to(JobController.class);
        bind(SlaController.class).to(SlaController.class);
    }
    
}
