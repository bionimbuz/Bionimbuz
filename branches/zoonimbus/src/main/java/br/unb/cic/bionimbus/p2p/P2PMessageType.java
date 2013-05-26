package br.unb.cic.bionimbus.p2p;


public enum P2PMessageType {

    INFOREQ(0x1),
    INFORESP(0x2),
    STARTREQ(0x3),
    STARTRESP(0x4),
    END(0x5),
    STATUSREQ(0x6),
    STATUSRESP(0x7),
    STOREREQ(0x8),
    STORERESP(0xA),
    GETREQ(0xB),
    GETRESP(0xC),
    CLOUDREQ(0xD),
    CLOUDRESP(0xE),
    SCHEDREQ(0xF),
    SCHEDRESP(0x11),
    JOBREQ(0x12),
    JOBRESP(0X13),
    ERROR(0x14),
    PINGREQ(0x15),
    PINGRESP(0x16),
    STOREACK(0x17),
    LISTREQ(0x18),
    LISTRESP(0x19),
    PREPREQ(0x1A),
    PREPRESP(0x1B),
    CANCELREQ(0x1C),
    CANCELRESP(0x1D),
    JOBCANCELREQ(0x1E),
    JOBCANCELRESP(0x1F);

    private final int code;

    private P2PMessageType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static P2PMessageType of(int code) {
        for (P2PMessageType type : values()) {
            if (type.code() == code)
                return type;
        }

        throw new IllegalArgumentException("code " + code + "is not registered!");
    }
}
