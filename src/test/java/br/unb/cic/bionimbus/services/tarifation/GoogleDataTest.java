/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.services.tarifation.Google.GoogleCloud;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel Fritz Sluzala
 */
public class GoogleDataTest {

    public static void main(String[] args) {
        GoogleCloud gc = new GoogleCloud();
        System.out.println(gc.GoogleComputeEngineInstances("N1.STANDARD-4.PREEMPTIBLE", "").toString(4));
    }
}
