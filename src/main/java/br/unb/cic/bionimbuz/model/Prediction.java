/*
 *  BionimbuzClient is a federated cloud platform.
 * Copyright (C) 2017 Developted in Laboratory of Bioinformatics and Data (LaBiD), 
Department of Computer Science, University of Brasilia, Brazil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author biolabid2
 */
public class Prediction {

    private String id;
    private String idService;
    private Long timeService;
    private Instance instance;
    private Double custoService;

    public Prediction() {
    }

    public Prediction(String idService, Long timeService, Instance instance, Double custoService) {
        this.idService = idService;
        this.timeService = timeService;
        this.instance = instance;
        this.custoService = custoService;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimeService() {
        return timeService;
    }

    public void setTimeService(Long timeService) {
        this.timeService = timeService;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

 
    public Double getCustoService() {
        return custoService;
    }

    public void setCustoService(Double custoServiço) {
        this.custoService = custoServiço;
    }
    
    public String getIdService() {
        return idService;
    }

    public void setIdService(String idService) {
        this.idService = idService;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(Prediction.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
