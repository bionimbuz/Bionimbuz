package br.unb.cic.bionimbus.p2p;

public abstract class P2PAbstractListener {

//    private final P2PService p2p;
//
//    public P2PAbstractListener(P2PService p2p) {
//        this.p2p = p2p;
//        if (p2p != null)
//            p2p.addListener(this);
//    }
//
//    protected P2PService getP2P() {
//        return p2p;
//    }

//    @Override
//    public void onEvent(P2PEvent event) {
//        if (event.getType().equals(P2PEventType.FILE)) {
//            processFile((P2PFileEvent) event);
//        } else if (event.getType().equals(P2PEventType.MESSAGE)) {
//            processMessage((P2PMessageEvent) event);
//        }
//    }

//    private void processFile(P2PFileEvent event) {
//        recvFile(event.getFile(), event.getParms());
//    }
//
//    private void processMessage(P2PMessageEvent event) {
//        Message message = event.getMessage();
//        if (message == null)
//            return;
//
//        PeerNode peer = null;
//        if (message instanceof AbstractMessage) {
//            peer = ((AbstractMessage) message).getPeer();
//        }

//        if (message instanceof InfoReqMessage) {
//            recvInfoReq(peer.getHost());
//        } else if (message instanceof InfoRespMessage) {
//            recvInfoResp(peer.getHost(), ((InfoRespMessage) message).getPluginInfo());
//        } else if (message instanceof StartReqMessage) {
//            recvStartReq(peer.getHost(), ((StartReqMessage) message).getJobInfo());
//        } else if (message instanceof StartRespMessage) {
//            recvStartResp(peer.getHost(), ((StartRespMessage) message).getJobId(),
//                    ((StartRespMessage) message).getPluginTask());
//        } else if (message instanceof EndMessage) {
//            recvEnd(peer.getHost(), ((EndMessage) message).getTask());
//        } else if (message instanceof StatusReqMessage) {
//            recvStatusReq(peer.getHost(), ((StatusReqMessage) message).getTaskId());
//        } else if (message instanceof StatusRespMessage) {
//            recvStatusResp(peer.getHost(), ((StatusRespMessage) message).getPluginTask());
//        } else if (message instanceof StoreReqMessage) {
//            recvStoreReq(peer.getHost(), ((StoreReqMessage) message).getFileInfo(),
//                    ((StoreReqMessage) message).getTaskId());
//        } else if (message instanceof StoreRespMessage) {
//            recvStoreResp(peer.getHost(), ((StoreRespMessage) message).getPluginInfo(),
//                    ((StoreRespMessage) message).getFileInfo(), ((StoreRespMessage) message).getTaskId());
//        } else if (message instanceof StoreAckMessage) {
//            recvStoreAck(peer.getHost(), ((StoreAckMessage) message).getPluginFile());
//        } else if (message instanceof GetReqMessage) {
//            recvGetReq(peer.getHost(), ((GetReqMessage) message).getFileId(),
//                    ((GetReqMessage) message).getTaskId());
//        } else if (message instanceof GetRespMessage) {
//            recvGetResp(peer.getHost(), ((GetRespMessage) message).getPluginInfo(),
//                    ((GetRespMessage) message).getPluginFile(), ((GetRespMessage) message).getTaskId());
//        } else if (message instanceof CloudReqMessage) {
//            recvCloudReq(peer.getHost());
//        } else if (message instanceof CloudRespMessage) {
//            recvCloudResp(peer.getHost(), ((CloudRespMessage) message).values());
//        } else if (message instanceof SchedReqMessage) {
//            recvSchedReq(peer.getHost(), ((SchedReqMessage) message).values());
//        } else if (message instanceof SchedRespMessage) {
//            recvSchedResp(peer.getHost(), ((SchedRespMessage) message).getJobId(),
//                    ((SchedRespMessage) message).getPluginInfo());
//        } else if (message instanceof JobReqMessage) {
//            recvJobReq(peer.getHost(), ((JobReqMessage) message).values());
//        } else if (message instanceof JobRespMessage) {
//            recvJobResp(peer.getHost(), ((JobRespMessage) message).getJobInfo());
//        } else if (message instanceof ErrorMessage) {
//            recvError(null, ((ErrorMessage) message).getError());
//        } else if (message instanceof PingReqMessage) {
//            recvPingReq(peer.getHost(), ((PingReqMessage) message).getTimestamp());
//        } else if (message instanceof PingRespMessage) {
//            recvPingResp(peer.getHost(), ((PingRespMessage) message).getTimestamp());
//        } else if (message instanceof ListReqMessage) {
//            recvListReq(peer.getHost());
//        } else if (message instanceof ListRespMessage) {
//            recvListResp(peer.getHost(), ((ListRespMessage) message).values());
//        } else if (message instanceof PrepReqMessage) {
//            recvPrepReq(peer.getHost(), ((PrepReqMessage) message).getPluginFile(),
//                    ((PrepReqMessage) message).getTaskId());
//        } else if (message instanceof PrepRespMessage) {
//            recvPrepResp(peer.getHost(), ((PrepRespMessage) message).getPluginInfo(),
//                    ((PrepRespMessage) message).getPluginFile(), ((PrepRespMessage) message).getTaskId());
//        } else if (message instanceof CancelReqMessage) {
//            recvCancelReq(peer.getHost(), ((CancelReqMessage) message).getTaskId());
//        } else if (message instanceof CancelRespMessage) {
//            recvCancelResp(peer.getHost(), ((CancelRespMessage) message).getPluginTask());
//        } else if (message instanceof JobCancelReqMessage) {
//            recvJobCancelReq(peer.getHost(), ((JobCancelReqMessage) message).getJobId());
//        } else if (message instanceof JobCancelRespMessage) {
//            recvJobCancelResp(peer.getHost(), ((JobCancelRespMessage) message).getJobId());
//        }
//    }

//    protected abstract void recvFile(File file, Map<String, String> parms);
//
//    protected abstract void recvInfoReq(Host origin);
//
//    protected abstract void recvInfoResp(Host origin, PluginInfo info);
//
//    protected abstract void recvStartReq(Host origin, JobInfo job);
//
//    protected abstract void recvStartResp(Host origin, String jobId, PluginTask task);
//
//    protected abstract void recvEnd(Host origin, PluginTask task);
//
//    protected abstract void recvStatusReq(Host origin, String taskId);
//
//    protected abstract void recvStatusResp(Host origin, PluginTask task);
//
//    protected abstract void recvStoreReq(Host origin, FileInfo file, String taskId);
//
//    protected abstract void recvStoreResp(Host origin, PluginInfo plugin, FileInfo file, String taskId);
//
//    protected abstract void recvStoreAck(Host origin, PluginFile file);
//
//    protected abstract void recvGetReq(Host origin, String fileId, String taskId);
//
//    protected abstract void recvGetResp(Host origin, PluginInfo plugin, PluginFile file, String taskId);
//
//    protected abstract void recvCloudReq(Host origin);
//
//    protected abstract void recvCloudResp(Host origin, Collection<PluginInfo> plugins);
//
//    protected abstract void recvSchedReq(Host origin, Collection<JobInfo> job);
//
//    protected abstract void recvSchedResp(Host origin, String jobId, PluginInfo plugin);
//
//    protected abstract void recvJobReq(Host origin, Collection<JobInfo> job);
//
//    protected abstract void recvJobResp(Host origin, JobInfo job);
//
//    protected abstract void recvError(Host origin, String error);
//
//    protected abstract void recvPingReq(Host origin, long timestamp);
//
//    protected abstract void recvPingResp(Host origin, long timestamp);
//
//    protected abstract void recvListReq(Host origin);
//
//    protected abstract void recvListResp(Host origin, Collection<PluginFile> files);
//
//    protected abstract void recvPrepReq(Host origin, PluginFile file, String taskId);
//
//    protected abstract void recvPrepResp(Host origin, PluginInfo plugin, PluginFile file, String taskId);
//
//    protected abstract void recvCancelReq(Host origin, String taskId);
//
//    protected abstract void recvCancelResp(Host origin, PluginTask task);
//
//    protected abstract void recvJobCancelReq(Host origin, String jobId);
//
//    protected abstract void recvJobCancelResp(Host origin, String jobId);
}
