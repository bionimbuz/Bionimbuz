package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;


import java.io.IOException;


/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/27/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RpcClient {

    public BioProto getProxy() throws IOException;

    public void close() throws Exception;
}
