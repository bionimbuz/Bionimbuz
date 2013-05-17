package br.unb.cic.bionimbus.plugin.hadoop.utils;

import java.io.InputStream;

public class RunningJobIdParser extends StreamReaderParserThread {
	public RunningJobIdParser(InputStream in) {
		super(in);
	}

	@Override
	public String parse(String line) {
		if (line == null) return null;
		if (line.contains("Running job:")) {
			return line.substring(line.lastIndexOf(" ") + 1);
		}
		return null;
	}

}
