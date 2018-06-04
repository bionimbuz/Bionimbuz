package br.unb.cic.bionimbuz.services.sched.policy.impl;

import java.net.DatagramSocket;
import java.lang.String;
import java.lang.Throwable;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ProcessBuilder;
import java.util.Vector;

import br.unb.cic.bionimbuz.services.sched.model.ScheduledMachines;
import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbuz.utils.Pair;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.model.Job;


//import ByteUtils.*;

public abstract class CppSched extends SchedPolicy
{
	DatagramSocket socket;
	SocketAddress cppAddr;
	boolean debug=false;
	protected abstract String GetSchedPolicy();
	protected ConcurrentHashMap<String, PluginInfo> cloudMap;
	public void Debug()
	{
		if(debug)
		{
			System.out.println("Java Aqui: ");
			Thread.dumpStack();
		}
	}
	public CppSched()
	{
		try
		{
			Random randomGenerator= new Random();
			do
			{
				int portaEscolhida= randomGenerator.nextInt()%30000+31024;
				System.out.println("Porta escolhida: " + portaEscolhida);
				socket = new DatagramSocket(portaEscolhida);
				Debug();
			}
			while(GetPort()== -1);
			Debug();
			long key= randomGenerator.nextLong();
			System.out.println("Porta escolhida: " + GetPort());
			System.out.println("Número sorteado: " + key);
			Debug();
//			Runtime r = Runtime.getRuntime();
			List<String> lista= new Vector<String>();
			lista.add("/home/xicobionimbuz/Git/Bionimbuz/src/main/java/br/unb/cic/bionimbuz/services/sched/policy/impl/Cpp/Escalonador.out");
			lista.add(String.valueOf(GetPort() ) );
			lista.add(String.valueOf(key));
			lista.add(" &");
			ProcessBuilder pb= new ProcessBuilder(lista);
			pb.inheritIO();
			Process p = pb.start();
//			Process p = r.exec("/home/xicobionimbuz/Git/Bionimbuz/src/main/java/br/unb/cic/bionimbuz/services/sched/policy/impl/Cpp/Escalonador.out "+ GetPort() + " " + key + " &");//my_command &
			DatagramPacket pkt= new DatagramPacket(new byte[65000], 65000);
			Debug();
			long numReceived;
			do
			{
				Debug();
				socket.receive(pkt);
				Debug();
//				numReceived= ByteUtils.bytesStringToLong(pkt.getData() );
				numReceived= Long.parseLong((new String(pkt.getData(), StandardCharsets.US_ASCII )).trim(), 10);
				if(numReceived != key)
				{
					System.out.println("wrong key. Expecting " + key + ",  got " + numReceived );
				}
			}
			while(numReceived != key);
			Debug();
			cppAddr = pkt.getSocketAddress();
			Debug();
			socket.send(new DatagramPacket(("Ack!").getBytes("US-ASCII"), ("Ack!").getBytes("US-ASCII").length, cppAddr));
			Debug();
			
			//enviar escalonador desejado
			String schedType= "SCHED= "+ GetSchedPolicy();
			socket.send(new DatagramPacket((schedType).getBytes("US-ASCII"), schedType.getBytes("US-ASCII").length, cppAddr));
			if(Receive("[SchedTypeAwnser]").contains("Fail"))
			{
				Debug();
//				throw(new String("Deu ruim"));
			}
			System.out.println("Escalonador gerado!");
			
			
		}
		catch (Throwable e)
		{
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public int GetPort()
	{
		return socket.getLocalPort();
	}
/*	public static void main(String[] args)
	{
		CppSched sched= new CppSched();
		System.out.println("Yeah!");
		
	}*/
	protected String Receive(String begin)
	{
		DatagramPacket pkt= new DatagramPacket(new byte[65000], 65000);
		String received;
		boolean success;
		do
		{
			Debug();
			try {
				socket.receive(pkt);
			}
			catch(Throwable a) {
				Debug();
				a.printStackTrace();
			}
			received= pkt.getData().toString().trim();
			success= (new String(pkt.getData(), StandardCharsets.US_ASCII).trim()).startsWith(begin);
			if(!success)
			{
				System.out.println("wrong key. Expecting " + begin + ",  got " + pkt.getData().toString() );
			}
		}
		while(false == success);
		return received;
	}
	
	public HashMap<Job,ScheduledMachines> schedule(List<Job> jobs){
		String message= "SCHEDULE\rJOBS=" + jobs.size();
		message+= '\r';
		for(int i=0; i < jobs.size(); i++){
			message+= jobs.get(i).Serialize();
			message+= '\r';
		}
		message+= "PLUGININFOS=" + cloudMap.entrySet().size();
		message+= '\r';
		for (Map.Entry<String, PluginInfo> entry : cloudMap.entrySet()) {
			message+="key="+entry.getKey();
			message+= '\r';
			message+="value="+entry.getValue().Serialize();
			message+= '\r';
		}
		try {
			socket.send(new DatagramPacket((message).getBytes("US-ASCII"), (message).getBytes("US-ASCII").length, cppAddr));
		}
		catch(Throwable a) {
			Debug();
			a.printStackTrace();
		}
		Debug();

		HashMap<Job, ScheduledMachines> resultMap= new HashMap<Job, ScheduledMachines>();
		
		String result= Receive("Results=");
		StringTokenizer tokenizer= new StringTokenizer(result, "\r", false);
		int resultSize= Integer.parseInt(tokenizer.nextToken().substring("Results=".length()));
		for(int i=0; i < resultSize; i++){
			String token= tokenizer.nextToken();
			StringTokenizer localTokenizer= new StringTokenizer(token, "\n", false);
			String jobId= localTokenizer.nextToken();
			String pluginInfoId= localTokenizer.nextToken();
			int whereToRun= Integer.parseInt(localTokenizer.nextToken());
			Job scheduledJob= FindJob(jobId, jobs);
			PluginInfo scheduledPluginInfo= null;
			for(Map.Entry<String, PluginInfo> entry : cloudMap.entrySet()) {
				if(entry.getValue().getId()== pluginInfoId) {
					scheduledPluginInfo= entry.getValue();
					break;
				}
			}
			if(1 == whereToRun) {
				resultMap.put(scheduledJob, new ScheduledMachines());
				resultMap.get(scheduledJob).cpu.add(scheduledPluginInfo);
			}else if(2 == whereToRun) {
				resultMap.put(scheduledJob, new ScheduledMachines());
				resultMap.get(scheduledJob).gpu.add(scheduledPluginInfo);
			}
			else {
				
			}
		}
		
		
		return resultMap;
	}
	protected Job FindJob(String jobId, List<Job> jobs){
		for(int i=0; i < jobs.size(); i++){
			if(jobs.get(i).getId() == jobId){
				return jobs.get(i);
			}
		}
		throw new Error("Deu ruim");
	}
    @Override
    public List<PluginTask> relocate(Collection<Pair<Job, PluginTask>> taskPairs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelJobEvent(PluginTask task) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void jobDone(PluginTask task) {
        // nothing to do so far
    }

	
}
