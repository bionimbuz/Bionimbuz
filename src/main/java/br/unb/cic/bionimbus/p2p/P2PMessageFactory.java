package br.unb.cic.bionimbus.p2p;

public class P2PMessageFactory { //extends MessageFactory {

//    public P2PMessageFactory() {
//        super("p2p");
//    }
//
//    @Override
//    public Message getMessage(int id, byte[] buffer) {
//
//        Message message = null;
//        P2PMessageType type = P2PMessageType.of(id);
//
//        switch (type) {
//            case INFOREQ:
//                message = new InfoReqMessage();
//                break;
//            case INFORESP:
//                message = new InfoRespMessage();
//                break;
//            case STARTREQ:
//                message = new StartReqMessage();
//                break;
//            case STARTRESP:
//                message = new StartRespMessage();
//                break;
//            case END:
//                message = new EndMessage();
//                break;
//            case STATUSREQ:
//                message = new StatusReqMessage();
//                break;
//            case STATUSRESP:
//                message = new StatusRespMessage();
//                break;
//            case CANCELREQ:
//                message = new CancelReqMessage();
//                break;
//            case CANCELRESP:
//                message = new CancelRespMessage();
//                break;
//            case STOREREQ:
//                message = new StoreReqMessage();
//                break;
//            case STORERESP:
//                message = new StoreRespMessage();
//                break;
//            case STOREACK:
//                message = new StoreAckMessage();
//                break;
//            case LISTREQ:
//                message = new ListReqMessage();
//                break;
//            case LISTRESP:
//                message = new ListRespMessage();
//                break;
//            case GETREQ:
//                message = new GetReqMessage();
//                break;
//            case GETRESP:
//                message = new GetRespMessage();
//                break;
//            case PREPREQ:
//                message = new PrepReqMessage();
//                break;
//            case PREPRESP:
//                message = new PrepRespMessage();
//                break;
//            case CLOUDREQ:
//                message = new CloudReqMessage();
//                break;
//            case CLOUDRESP:
//                message = new CloudRespMessage();
//                break;
//            case SCHEDREQ:
//                message = new SchedReqMessage();
//                break;
//            case SCHEDRESP:
//                message = new SchedRespMessage();
//                break;
//            case JOBREQ:
//                message = new JobReqMessage();
//                break;
//            case JOBRESP:
//                message = new JobRespMessage();
//                break;
//            case JOBCANCELREQ:
//                message = new JobCancelReqMessage();
//                break;
//            case JOBCANCELRESP:
//                message = new JobCancelRespMessage();
//                break;
//            case ERROR:
//                message = buildErrorMessage(buffer);
//                break;
//            case PINGREQ:
//                message = new PingReqMessage();
//                break;
//            case PINGRESP:
//                message = new PingRespMessage();
//                break;
//        }
//
//        try {
//            message.deserialize(buffer);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            message = null;
//        }
//
//        return message;
//    }
//
//    private Message buildErrorMessage(byte[] buffer) {
//        Message message = null;
//
//        try {
//            switch (ErrorMessage.deserializeErrorType(buffer)) {
//                case INFO:
//                    message = new InfoErrorMessage();
//                    message.deserialize(buffer);
//                    break;
//                case SCHED:
//                    message = new SchedErrorMessage();
//                    message.deserialize(buffer);
//                    break;
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            message = null;
//        }
//
//        return message;
//    }

}
