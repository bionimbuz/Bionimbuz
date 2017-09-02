/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD),
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.unb.cic.bionimbuz.plugin.linux;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import br.unb.cic.bionimbuz.constants.SystemConstants;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginService;

public class LinuxGetInfo implements Callable<PluginInfo> {

	public static final String PATH = "files";
	public static final String CORES = "dstat -cf";
	public static final String CPUMHZ = "grep -m 1 MHz /proc/cpuinfo";
	public static final String CPUGHZ = "grep -m 1 GHz /proc/cpuinfo";
	public static final String MEMTOTAL = "grep -m 1 MemTotal /proc/meminfo";
	public static final String MEMFREE = "grep -m 1 MemFree /proc/meminfo";
	private final PluginInfo pluginInfo = new PluginInfo();

	public LinuxGetInfo() {
		super();
	}

	@Override
	public PluginInfo call() {
		try {
			this.getCpuInfo();
			this.getMemoryInfo();
			this.getDiskInfo();
			this.getServices();
			this.getUptime();
		} catch (final Exception ex) {
			Logger.getLogger(LinuxGetInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		return this.pluginInfo;
	}

	/**
	 * Obtem as informações do processador do recurso e realiza o setter dessa
	 * informações na classe pluginInfo. Número de cores, frequencia do
	 * processador(GHz) e quantidade de cores ocupados.
	 */
	private void getCpuInfo() {
		final int nCpus = Runtime.getRuntime().availableProcessors();
		this.pluginInfo.setNumCores(nCpus);
		// TODO: correct numNodes
		this.pluginInfo.setNumNodes(1);
		this.pluginInfo.setNumOccupied(this.getCoresOccupied(nCpus));
		String cpuInfo = this.execCommand(CPUMHZ);
		this.pluginInfo.setCurrentFrequencyCore(
				(new Double(cpuInfo.substring(cpuInfo.indexOf(":") + 1, cpuInfo.length()).trim())) / 100000);
		cpuInfo = this.execCommand(CPUGHZ);
		final Double freq = new Double(cpuInfo.substring(cpuInfo.indexOf("@") + 1, cpuInfo.length() - 3).trim())
				* 1000000000;
		this.pluginInfo.setFactoryFrequencyCore(freq);
	}

	/**
	 * Retorna o número de cores ocupados no recuso caso seu processamento estaja
	 * acima de 70 porcento.
	 *
	 * @return número de cores ocupados
	 */
	private int getCoresOccupied(int numCpu) {
		int nCpuOccupied = 0;
		try {
			InputStreamReader read;
			BufferedReader buffer;
			final List<Integer> linesCPU = new ArrayList<>(numCpu);
			String[] columns, lines;
			String line;
			int count = 0;
			int i = 0;
			final Process p = Runtime.getRuntime().exec(CORES);
			read = new InputStreamReader(p.getInputStream());
			buffer = new BufferedReader(read);
			// magic number numCpu+(4)
			while ((line = buffer.readLine()) != null && count < (numCpu + 4)) {
				if (count >= 3) {
					columns = line.trim().split(":");
					for (int j = 0; j < columns.length && j < numCpu; j++) {
						lines = columns[j].trim().replaceAll("[\\[\\]]", "").replace("  ", "").replace("0;0m", "")
								.split(" ");
						lines = Arrays.stream(lines).filter(value -> value != null && !value.isEmpty())
								.toArray(size -> new String[size]);
						if (i != 0) {
							linesCPU.set(j, new Integer(lines[0]) + linesCPU.get(j));
						} else {
							linesCPU.add(j, new Integer(lines[0]));
						}
					}
					i++;
				}
				count++;
			}
			// finaliza a execução
			p.destroy();
			for (i = 0; i < numCpu && i < linesCPU.size(); i++) {
				if ((linesCPU.get(i) / 3) > 80) {
					nCpuOccupied++;
				}
			}
		} catch (final IOException ex) {
			Logger.getLogger(LinuxGetInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		return nCpuOccupied;
	}

	/**
	 * Obtem as informações da memória RAM do recurso e realiza o setter dessa
	 * informações na classe pluginInfo. Memória total e memória livre em
	 * GigaBytes(Retorno da eexcução em KB / 1024²).
	 */
	private void getMemoryInfo() {
		String mem = this.execCommand(MEMTOTAL);
		this.pluginInfo
				.setMemoryTotal((new Double(mem.substring(mem.indexOf(":") + 1, mem.length() - 2).trim()) / 1048576));
		mem = this.execCommand(MEMFREE);
		this.pluginInfo
				.setMemoryFree((new Double(mem.substring(mem.indexOf(":") + 1, mem.length() - 2).trim()) / 1048576));
	}

	/**
	 * Obtem as informações do espaço e, disco do recurso e realiza o setter dessa
	 * informações na classe pluginInfo. Espaço total e espaço livre.
	 */
	private void getDiskInfo() {
		final File path = new File(PATH);
		for (final File root : File.listRoots()) {
			if (path.getAbsolutePath().contains(root.getAbsolutePath())) {
				this.pluginInfo.setFsFreeSize((float) root.getFreeSpace());
				this.pluginInfo.setFsSize((float) root.getTotalSpace());
				return;
			}
		}
	}

	private void getServices() throws Exception {
		final List<PluginService> list = new CopyOnWriteArrayList<>();
		final File dir = new File(SystemConstants.FOLDER_SERVICE);
		if (dir.isDirectory()) {
			for (final File file : dir.listFiles()) {
				if (file.isFile() && file.canRead() && file.getName().endsWith(".json")) {
					final ObjectMapper mapper = new ObjectMapper();
					final PluginService service = mapper.readValue(file, PluginService.class);
					list.add(service);
				}
			}
		}
		this.pluginInfo.setServices(list);
	}

	private void getUptime() {
		this.pluginInfo.setUptime(System.currentTimeMillis());
	}

	/**
	 * Retorna os valores da execução do comando informado pelo parâmetro
	 *
	 * @param Command
	 *            comando a ser executado
	 * @return string resultado da execução
	 */
	private String execCommand(String Command) {
		String line = null;
		InputStreamReader read;
		try {
			read = new InputStreamReader(Runtime.getRuntime().exec(Command).getInputStream());
			final BufferedReader buffer = new BufferedReader(read);
			line = buffer.readLine();
		} catch (final IOException ex) {
			Logger.getLogger(LinuxGetInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		return line;
	}
}
