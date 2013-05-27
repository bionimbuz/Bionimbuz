package br.unb.cic.bionimbus.avro.rpc;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/21/13
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class BioNimbusException extends Exception {
    public BioNimbusException(Exception e) {
        super(e);
    }
}
