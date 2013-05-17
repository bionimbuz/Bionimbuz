package br.unb.cic.bionimbus.plugin.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskState;

public class HadoopGetInfo implements Callable<PluginInfo> {

	private static final String nameNode = "http://localhost:50070/dfshealth.jsp";
	private static final String jobTracker = "http://localhost:50030/jobtracker.jsp";
	private static final String nodes = "<a href=\"machines.jsp?type=active\">";
	private static final String tasks = "http://localhost:50030/jobtasks.jsp?jobid=%s&type=map&pagenum=1";
	private static final String serviceDir = "services";

	public static void getTaskInfo(PluginTask task) throws Exception {
		
		// O estado da task eh pending enquanto ela nao obter um Local Id.
		task.setState(PluginTaskState.PENDING);
		
		// Se o local id estiver nulo quer dizer que a task ainda nem executou no hadoop
		if (task.getJobInfo().getLocalId() == null) return;
		
		// O estado da task e waiting ate que prove-se o contrario
		task.setState(PluginTaskState.WAITING);
		
		URL url = new URL(String.format(tasks, task.getJobInfo().getLocalId()));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String s = "";

		while ((s = br.readLine()) != null) {
			Pattern pattern = Pattern.compile("<td>(\\d+\\.\\d+)%<table", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(s);

			while (matcher.find()) {
				double percent = Double.parseDouble(matcher.group(1));
				
				// DEBUG
				// System.out.println(percent);
				if (percent > 0.0) {
					task.setState(PluginTaskState.RUNNING);
					return;
				}
			}
		}
		
		br.close();
		conn.disconnect();
	}
	
	private void getNameNodeInfo(PluginInfo info) throws Exception {
		
		URL url = new URL(nameNode);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String s = "";

		while ((s = br.readLine()) != null) {
			if (s.contains("Configured Capacity")) {
				int i = 0;
				StringTokenizer st = new StringTokenizer(s, "<>");

				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					String[] split = token.split(" ");
					float value;
					String unit;
					if (i == 7) {
						value = Float.parseFloat(split[1]);
						unit = split[2];
						if (unit.equals("TB")) {
							float k = 1024.0f;
							value *= (k*k*k*k);
						} else if (unit.equals("GB"))
							value *= (1024*1024*1024);
						else if (unit.equals("MB"))
							value *= (1024*1024);
						else if (unit.equals("MB"))
							value *= 1024;
						info.setFsSize(value);
					} else if (i == 31) {
						value = Float.parseFloat(split[1]);
						unit = split[2];
						if (unit.equals("TB")) {
							float k = 1024.0f;
							value *= (k*k*k*k);
						} else if (unit.equals("GB"))
							value *= (1024*1024*1024);
						else if (unit.equals("MB"))
							value *= (1024*1024);
						else if (unit.equals("MB"))
							value *= 1024;
						info.setFsFreeSize(value);
					}
					i++;
					if (i > 31)
						break;
				}
			}
		}

		br.close();
		conn.disconnect();
	}

	private void getJobTrackerInfo(PluginInfo info) throws Exception {
		URL url = new URL(jobTracker);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String s = "";

		while ((s = br.readLine()) != null) {
			int index = -1;

			if ((index = s.indexOf(nodes)) > 0) {
				index += nodes.length();
				String[] tokens = s.substring(index).split("<");
				info.setNumNodes(Integer.parseInt(tokens[0]));
				info.setNumCores(Integer.parseInt(tokens[11].substring(tokens[11].indexOf('>') + 1)));
				info.setNumOccupied(Integer.parseInt(tokens[3].substring(tokens[3].indexOf('>') + 1)));
			}
		}

		br.close();
		conn.disconnect();
	}
	
	private void loadServices(PluginInfo info) throws Exception {
		List<PluginService> list = new CopyOnWriteArrayList<PluginService>();
		System.out.println("serviceDir = " + serviceDir);
		File dir = new File(serviceDir);
		
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile() && file.canRead() && file.getName().endsWith(".json")) {
					ObjectMapper mapper = new ObjectMapper();
					PluginService service = mapper.readValue(file, PluginService.class);
					list.add(service);
				}
			}
		}
		info.setServices(list);
	}

	@Override
	public PluginInfo call() throws Exception {
		PluginInfo info = new PluginInfo();
		getNameNodeInfo(info);
		getJobTrackerInfo(info);
		loadServices(info);
		return info;
	}

}
