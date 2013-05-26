package br.unb.cic.bionimbus.p2p;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ChordRing {

    public static final int SHA1_BIT_SIZE = 160;

    private static final Logger LOG = LoggerFactory.getLogger(ChordRing.class);

    private final int m;

    private final PeerNode[] finger;
    private final ID id;
    private final PeerNode peer;

//	private PeerNode predecessor;

    public ChordRing(PeerNode thisNode) {
        this(thisNode, SHA1_BIT_SIZE);
        LOG.debug(String.format("Starting chord ring for peer %s", id));
    }

    public ChordRing(PeerNode thisNode, int bitsize) {

        id = thisNode.getId();
        peer = thisNode;
        peer.start();
        m = bitsize;
        finger = new PeerNode[m];

    }

    public synchronized PeerNode successor(ID key) {
        ID successor = successor().getId();
        if (key.gt(id) && key.lte(successor)) {
            return successor();
        } else {
            PeerNode n = getClosestPrecedingNode(key);
            return n.retrieveSuccessor(key);
        }
    }

    public synchronized PeerNode getClosestPrecedingNode(ID key) {

        for (int i = finger.length; i >= 0; i--) {
            if (finger[i] != null) {
                ID f = finger[i].getId();
                if (f.gt(id) && f.lt(key)) {
                    return finger[i];
                }
            }
        }
        return peer;
    }

    public synchronized PeerNode successor() {
        return finger[0];
    }

    public synchronized int size() {
        int count = 0;
        for (int i = 0; i < finger.length; i++) {
            if (finger[i] != null) {
                count++;
            }
        }
        return count;
    }

    public synchronized void add(PeerNode peerNode) {

        ID candidate = peerNode.getId();

        for (int i = 0; i < finger.length; i++) {

            ID temp = id.add(ID.pow(i)).mod(m);

            if (candidate.gte(temp)) {
                if (finger[i] == null) {
                    finger[i] = peerNode;
                    peerNode.start();
                }
            } else {
                break;
            }
        }

        //System.out.println("chord ring: " + peers());
    }

    public synchronized void remove(PeerNode peerNode) {
        for (int i = 0; i < finger.length; i++) {
            if (finger[i].equals(peerNode)) {
                finger[i] = null;
            }
        }
    }

    public synchronized Collection<PeerNode> peers() {
        final SortedSet<PeerNode> peers = new TreeSet<PeerNode>();
        for (PeerNode p : finger) {
            if (p != null) {
                peers.add(p);
            }
        }
        return ImmutableSortedSet.copyOf(peers);
    }

    public synchronized String printRawTable() {
        return peers().toString();
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table for ID " + id + ":");
        final SortedSet<ID> ids = new TreeSet<ID>();
        for (PeerNode p : finger) {
            if (p != null)
                ids.add(p.getId());
        }
        sb.append(ids.toString());
        return sb.toString();
    }
}
