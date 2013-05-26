package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;

public class AsyncCommand implements Command {

    private SimpleShell shell;

    public AsyncCommand(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                shell.print("Hello world");
            }

        });

        t.start();
        return "";
    }

    @Override
    public String usage() {
        return "example";
    }

    @Override
    public String getName() {
        return "example";
    }

    @Override
    public void setOriginalParamLine(String param) {
        // TODO Auto-generated method stub

    }

}
