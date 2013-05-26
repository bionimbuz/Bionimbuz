package br.unb.cic.bionimbus.p2p;

import junit.framework.TestCase;


public class ChordRingTest extends TestCase {


    public void testRingSize() {
        ChordRing ring = new ChordRing(new PeerNode(new ID(4)));
        ring.add(new PeerNode(new ID(8)));
        ring.add(new PeerNode(new ID(39)));
        System.out.println(ring);
        System.out.println(ring.printRawTable());
    }

}
