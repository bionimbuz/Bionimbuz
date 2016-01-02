/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest;

import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import com.google.inject.AbstractModule;

/**
 *
 * @author Vinicius
 */
public class RestResourceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JobController.class).to(JobController.class);
    }
    
}
