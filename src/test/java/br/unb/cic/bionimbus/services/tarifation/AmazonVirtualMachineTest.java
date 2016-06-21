/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.services.tarifation.Amazon.AmazonVirtualMachine;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author fritz
 */
public class AmazonVirtualMachineTest {
    
     @Test
     public void AmazonVirtualMachineTest() {
         AmazonVirtualMachine vm = new AmazonVirtualMachine("0.05", "us", 0, "linux", "n1.small",
                                                            0, "21/06/2016", 0,"21/06/2016", true, 0, true);
         assertEquals("Pricing Error!","0.05", vm.getPrice());
         assertEquals("Location Error!","us", vm.getRegion());
         assertEquals("ID Error!",0, vm.getId());
         assertEquals("OS Error!","linux", vm.getOs());
         assertEquals("Model Error!","n1.small", vm.getName());
         assertEquals("Upfront Error!",0, vm.getUpfront());
         assertEquals("Update Error!","21/06/2016", vm.getUpdated_at());
         assertEquals("Term Error!",0, vm.getTerm());
         assertEquals("Created Error!","21/06/2016", vm.getCreated_at());
         assertEquals("Latest Error!",true, vm.isLatest());
         assertEquals("Price Error!","0/HOUR", vm.getPrice());
         assertEquals("Ebsoptimized Error!",true, vm.isEbsoptimized());
     }
}
