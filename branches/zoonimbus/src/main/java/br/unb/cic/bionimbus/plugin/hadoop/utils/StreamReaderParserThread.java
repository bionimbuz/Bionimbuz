package br.unb.cic.bionimbus.plugin.hadoop.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

public abstract class StreamReaderParserThread implements Runnable {
    private InputStream in;
    private String result;
    private boolean stop = false;

    public StreamReaderParserThread(InputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
            String line;
            while (!this.stop) {
                line = br.readLine();
                this.result = parse(line);
                if (StringUtils.isNotEmpty(this.result)) break;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler linha.");
        }
    }

    public void stop() {
        this.stop = true;
    }

    public String getResult() {
        return this.result;
    }

    public abstract String parse(String line);
}
