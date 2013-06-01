package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import org.apache.avro.AvroRemoteException;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/24/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class BioProtoImpl implements BioProto {

    public boolean ping() throws AvroRemoteException {
        return true;
    }
}
