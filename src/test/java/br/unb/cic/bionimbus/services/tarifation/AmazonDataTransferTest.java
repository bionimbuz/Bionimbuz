/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.services.tarifation.Amazon.AmazonDataTransfer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fritz
 */
public class AmazonDataTransferTest {
    
    @Test
    public void AmazonDataTransferTest() {
        AmazonDataTransfer adt = new AmazonDataTransfer(0, "us", "kindtest", "tiertest", 0, "createdtest", "updatedtest");
        assertEquals("ID Error","0", adt.getId());
        assertEquals("Region Error!", "us", adt.getRegion());
        assertEquals("Kind Error!", "kindtest", adt.getKind());
        assertEquals("Tier Error!", "tiertest", adt.getTier());
        assertEquals("Price Error!", "0.0/OPERATION", adt.getPrice());
        assertEquals("Created Error!","createdtest", adt.getCreatedAt());
        assertEquals("Updated Error!","updatedtest", adt.getUpdatedAt());
    }
}
