/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.services.tarifation;

import br.unb.cic.bionimbus.services.tarifation.Amazon.AmazonStorage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fritz
 */
public class AmazonStorageTest {

    @Test
    public void AmazonStorageTest() {
        
        AmazonStorage as = new AmazonStorage(0, "us", "testkind", 0, "gb", "today", "today");
        assertEquals("ID Error!", "0", as.getId());
        assertEquals("Region Error!", "us", as.getRegion());
        assertEquals("Kind Error!", "testkind", as.getKind());
        assertEquals("Price Error!", "0.0/GB", as.getPrice());
        assertEquals("Unit Error!", "gb", as.getPriceUnit());
        assertEquals("Created Error!", "today", as.getCreatedAt());
        assertEquals("Updated Error!", "today", as.getUpdatedAt());
    }
}
