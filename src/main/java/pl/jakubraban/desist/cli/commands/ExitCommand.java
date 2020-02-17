package pl.jakubraban.desist.cli.commands;

import picocli.CommandLine.*;
import pl.jakubraban.desist.DesistSessionSpec;

import java.util.concurrent.Callable;

@Command(name = "exit", description = "Exit Desist")
public class ExitCommand implements Callable<Integer> {

    public ExitCommand(DesistSessionSpec sessionSpec) { }

    @Override
    public Integer call() {
        System.exit(0);
        return 0;
    }

}
