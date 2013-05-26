package br.unb.cic.bionimbus.client.shell;

public interface Command {

    public String execute(String... params) throws Exception;

    public String usage();

    public String getName();

    public void setOriginalParamLine(String param);


}
