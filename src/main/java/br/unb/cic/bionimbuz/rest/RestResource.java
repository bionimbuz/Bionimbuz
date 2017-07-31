/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest;

import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;

/**
 * Interface for a REST resource. It shall be started and handle incoming
 * requests
 *
 * @author Vinicius
 */
public interface RestResource {

    public ResponseInfo handleIncoming(RequestInfo request);
}
