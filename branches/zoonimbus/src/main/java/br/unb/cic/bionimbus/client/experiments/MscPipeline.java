package br.unb.cic.bionimbus.client.experiments;

import java.util.ArrayList;
import java.util.List;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginFile;

public class MscPipeline implements Pipeline {

    private int stage = -1;

    private final PluginFile input;

    private List<JobInfo> jobs = new ArrayList<JobInfo>();

    private List<String> outputFiles = new ArrayList<String>();

    public MscPipeline(PluginFile file) {
        input = file;
        outputFiles.add(input.getName() + "-align.sam");
        outputFiles.add(input.getName() + "-align.bed");
        outputFiles.add(input.getName() + "-interv.out");
        outputFiles.add(input.getName() + "-coverage.out");

        // bowtie
        JobInfo job = new JobInfo();
        job.setArgs("%I1%I2%O1");
//        job.setId(null);
        job.setServiceId(1111);
        job.addInput(input.getId(), input.getSize());
        // AQUI VOCE TEM QUE COLOCAR O ID DO ARQUIVO s1.fa
        job.addInput("s1.fa", new Long(114503237));
        job.addOutput(outputFiles.get(0));
        jobs.add(job);

        // sam2bed
        job = new JobInfo();
        job.setArgs("%I1%O1");
//        job.setId(null);
        job.setServiceId(1112);
        job.addOutput(outputFiles.get(1));
        jobs.add(job);

        // genome2interval
        job = new JobInfo();
        job.setArgs("%I110000%O1");
//        job.setId(null);
        job.setServiceId(1113);
        // AQUI VOCE TEM QUE COLOCAR O ID DO ARQUIVO human.genome
        job.addInput("human.hg19.genome", new Long(365));
        job.addOutput(outputFiles.get(2));
        jobs.add(job);

        // coverageBed
        job = new JobInfo();
        job.setArgs("%I1%I2%O1");
//        job.setId(null);
        job.setServiceId(1114);
        job.addOutput(outputFiles.get(3));
        jobs.add(job);
    }

    @Override
    public String getCurrentOutput() {
        if (stage == -1)
            return null;
        return outputFiles.get(stage);
    }

    @Override
    public JobInfo firstJob() {
        return nextJob(null);
    }

    @Override
    public JobInfo nextJob(PluginFile pluginFile) {
        stage++;
        JobInfo job = null;
        if (stage < jobs.size()) {
            /*if (stage == 1) {
				// adicionamos saida do bowtie na entrada do genome2interval
				JobInfo auxJob = jobs.get(2);
				auxJob.addInput(pluginFile.getId(), pluginFile.getSize());
			}*/
            if (stage == 2) {
                // adicionamos saida do sam2bed na entrada do coverageBed
                JobInfo auxJob = jobs.get(3);
                auxJob.addInput(pluginFile.getId(), pluginFile.getSize());
            }
            job = jobs.get(stage);
            if (pluginFile != null && stage != 2)
                // adicionamos a saida de um passo como entrada do seguinte
                // menos a saida do sam2bed, que nao e' entrada para o passo seguinte (genome2interval),
                // mas somente para o alem desse (coverageBed).
                job.addInput(pluginFile.getId(), pluginFile.getSize());
        }
        return job;
    }

    @Override
    public String getInput() {
        return input.getPath();
    }
}
