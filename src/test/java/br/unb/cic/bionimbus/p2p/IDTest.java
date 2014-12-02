package br.unb.cic.bionimbus.p2p;

import junit.framework.TestCase;


public class IDTest extends TestCase {

    public void testIdOps() {

        System.out.println(new ID(19).mod(3));
    }

}
