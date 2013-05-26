package br.biofoco.p2p.dht.core;

import junit.framework.TestCase;
import br.unb.cic.bionimbus.p2p.dht.IDFactory;
import br.unb.cic.bionimbus.p2p.dht.PeerNode;
import br.unb.cic.bionimbus.p2p.dht.walker.PeerView;

public class PeerViewTest extends TestCase {

    private PeerView p;

    public void setUp() {
        p = new PeerView();
    }

    public void tearDown() {
        p.clear();
        p = null;
    }

    public void testInvertedInsertion() {

        PeerNode peer = new PeerNode(IDFactory.fromString("3"));
        p.add(peer);

        peer = new PeerNode(IDFactory.fromString("2"));
        p.add(peer);

        peer = new PeerNode(IDFactory.fromString("1"));
        p.add(peer);

        assertEquals("[1, 2, 3]", p.keys().toString());
    }

    public void testDuplicateInsertion() {

        PeerNode peer = new PeerNode(IDFactory.fromString("1"));
        p.add(peer);
        p.add(peer);

        assertEquals(1, p.size());
    }

    public void testInsertAndRemove() {
        PeerNode peer = new PeerNode(IDFactory.fromString("1"));
        p.add(peer);

        assertEquals(1, p.size());

        p.remove(peer);

        assertEquals(0, p.size());
    }

}
