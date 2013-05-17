package br.unb.cic.bionimbus.client.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.shell.commands.SyncCommunication;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.JobReqMessage;
import br.unb.cic.bionimbus.p2p.messages.JobRespMessage;
import br.unb.cic.bionimbus.p2p.messages.ListReqMessage;
import br.unb.cic.bionimbus.p2p.messages.ListRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreRespMessage;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;

public class MscTool {
	
	private static final Logger LOG = LoggerFactory.getLogger(MscTool.class);

	private P2PService p2p;

	private SyncCommunication communication;

	private void initCommunication() throws IOException, InterruptedException {
		String configFile = System.getProperty("config.file", "conf/client.json");
		BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);

		this.p2p = new P2PService(config);
		this.p2p.start();
		while (p2p.getPeers().isEmpty())
			;

		LOG.info("client is connected.");
		//TimeUnit.SECONDS.sleep(10);
		this.communication = new SyncCommunication(p2p);
	}
	
	private List<String> readFileNames() throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader("inputfiles.txt"));
		String line;
		while ((line = br.readLine()) != null)
			list.add(line);
		return list;
	}

	private void uploadFile(String name) throws IOException, InterruptedException {
		File file = new File(name);
		FileInfo info = new FileInfo();
		info.setName(file.getName());
		info.setSize(file.length());

		LOG.info("uploading " + name + ": " + file.length() + " bytes");

		communication.sendReq(new StoreReqMessage(p2p.getPeerNode(), info, ""), P2PMessageType.STORERESP);
		StoreRespMessage resp = (StoreRespMessage) communication.getResp();
		PluginInfo pluginInfo = resp.getPluginInfo();

		LOG.info("uploading to plugin "+ pluginInfo.getId() + " at " + pluginInfo.getHost().getAddress());
		p2p.sendFile(pluginInfo.getHost(), resp.getFileInfo().getName());
	}

	public void uploadFiles() throws IOException, InterruptedException {
		List<String> fileNames = readFileNames();
		initCommunication();
		for (String name : fileNames) {
			uploadFile(name);
		}
	}

	private PluginFile getPluginFile(String file, Collection<PluginFile> cloudFiles) throws FileNotFoundException {
		for (PluginFile pluginFile : cloudFiles)
			if (pluginFile.getPath().equals(file))
				return pluginFile;
		throw new FileNotFoundException(file);
	}

	private List<Pipeline> getPipelines() throws IOException, InterruptedException {
		Collection<PluginFile> cloudFiles = listCloudFiles();
		List<Pipeline> list = new ArrayList<Pipeline>();
		for (String file : readFileNames()) {
			Pipeline pipeline = new MscPipeline(getPluginFile(file, cloudFiles));
			list.add(pipeline);
		}
		return list;
	}

	public void runJobs() throws IOException, InterruptedException {
		initCommunication();
		List<Pipeline> list = getPipelines();
		List<Pipeline> sending = new ArrayList<Pipeline>(list);


		while (!list.isEmpty()) {

			int count = 0;
			List<JobInfo> jobs = new ArrayList<JobInfo>();
			List<Pipeline> sendAux = new ArrayList<Pipeline>(sending);
			for (Pipeline pipeline : sendAux) {
				JobInfo job = pipeline.firstJob();
				if (job != null) {
					jobs.add(job);
					sending.remove(pipeline);
					if (++count >= 4) {
						sendJobs(jobs);
						TimeUnit.MINUTES.sleep(1);
						break;
					}
				}
			}
	
			if ((count > 0) && (count < 4))
				sendJobs(jobs);

			Collection<PluginFile> files = listCloudFiles();
			List<Pipeline> auxList = new ArrayList<Pipeline>(list);

			for (Pipeline pipeline : auxList) {
				String file = pipeline.getCurrentOutput();
				if (file == null)
					continue;
				for (PluginFile pluginFile : files) {
					if (!pluginFile.getPath().equals(file))
					  continue;
					JobInfo job = pipeline.nextJob(pluginFile);
					if (job != null) {
						List<JobInfo> jobList = new ArrayList<JobInfo>();
						jobList.add(job);
						sendJobs(jobList);
					} else {
						LOG.info("pipeline " + pipeline.getInput() + " finalized...");
						list.remove(pipeline);
					}
				}
			}

			TimeUnit.SECONDS.sleep(5);
		}
		
		LOG.info("test concluded!");
	}

	private Collection<PluginFile> listCloudFiles() throws InterruptedException {
		communication.sendReq(new ListReqMessage(p2p.getPeerNode()), P2PMessageType.LISTRESP);
		ListRespMessage listResp = (ListRespMessage) communication.getResp();
		return listResp.values();
	}

	private void sendJobs(List<JobInfo> jobs) throws InterruptedException {
		communication.sendReq(new JobReqMessage(p2p.getPeerNode(), jobs), P2PMessageType.JOBRESP);
		JobRespMessage resp = (JobRespMessage) communication.getResp();
		LOG.info("job " + resp.getJobInfo().getId() + " sent succesfully...");
	}

	public void printResult() {
		
	}

	public static void main(String[] args) {
		MscTool tool = new MscTool();
		try {
			//tool.uploadFiles();
			tool.runJobs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tool.printResult();
	}
}
