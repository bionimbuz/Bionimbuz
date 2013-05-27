package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.CancelRespMessage;
import br.unb.cic.bionimbus.avro.gen.CloudRespMessage;
import org.apache.avro.AvroRemoteException;

/**
 * Created with IntelliJ IDEA.
 * User: edward
 * Date: 5/24/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class BioProtoImpl implements BioProto {


//    @Override
//    public BidResponse bid(BidRequest bidRequest) throws AvroRemoteException, BidderError {
//       return BidResponse.newBuilder().setMaxBidMicroCpm(10).setCreativeSnippet("Hi").build();
//    }
//
//    @Override
//    public void notify(Notification notification) {
//        System.out.println(notification.toString());
//    }

    @Override
    public CancelRespMessage cancelTask(CharSequence taskId) throws AvroRemoteException {
        throw new AvroRemoteException("Nao implementado");
    }

    @Override
    public boolean ping() throws AvroRemoteException {
        return true;
    }

    @Override
    public CloudRespMessage cloudRequest() throws AvroRemoteException {
        throw new AvroRemoteException("Nao implementado");
    }
}
