package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.JobCancel;
import org.apache.avro.AvroRemoteException;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.*;

public class BioProtoImpl implements BioProto {

    public boolean ping() throws AvroRemoteException {
        return true;
    }

    @Override
    public List<String> listFiles() throws AvroRemoteException {
        return asList("file1", "file2", "file3");
    }

    @Override
    public List<String> listServices() throws AvroRemoteException {
        return asList("blast", "interpro", "bowtie");
    }

    @Override
    public String startJob(String jobID) throws AvroRemoteException {
        return "OK";
    }

    @Override
    public String cancelJob(String jobID) throws AvroRemoteException {
        return "OK";
    }

}
