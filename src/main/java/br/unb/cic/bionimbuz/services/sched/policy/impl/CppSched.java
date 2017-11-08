package br.unb.cic.bionimbuz.services.sched.policy.impl;

import java.net.DatagramSocket;
import java.lang.String;
import java.lang.Throwable;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
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
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("./Escalonador.out "+ GetPort() + " " + key + " > /home/francisco/Escalonador.log 2>&1 &");//my_command > output.log 2>&1 &
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
			String schedType= GetSchedPolicy();
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
		do
		{
			Debug();
			socket.receive(pkt);
			received= pkt.getData().toString().trim();
			if(!received.startsWith(begin))
			{
				System.out.println("wrong key. Expecting " + begin + ",  got " + pkt.getData().toString() );
			}
		}
		while(!received.startsWith(begin));
		return received;
	}
	
	public HashMap<Job,PluginInfo> schedule(List<Job> jobs){
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
		socket.send(new DatagramPacket((message).getBytes("US-ASCII"), (message).getBytes("US-ASCII").length, cppAddr));
		Debug();
		String result= Receive("Results=");
		StringTokenizer tokenizer= new StringTokenizer(result, "\r", false);
		int resultSize= Integer.parseInt(tokenizer.nextToken().substring("Results=".length()));
		for(int i=0; i < resultSize; i++){
			String token= tokenizer.nextToken();
			StringTokenizer localTokenizer= new StringTokenizer(token, "\n", false);
			
		}
		
		HashMap<Job, PluginInfo> resultMap= new HashMap<Job, PluginInfo>();
		
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

}
