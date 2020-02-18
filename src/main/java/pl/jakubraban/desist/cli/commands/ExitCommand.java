package pl.jakubraban.desist.cli.commands;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine.Command;
import pl.jakubraban.desist.DesistSessionSpec;

@Command(name = "exit", description = "Exit Desist")
public class ExitCommand implements Runnable {

    public ExitCommand(DesistSessionSpec sessionSpec) { }

    @Override
    public void run() {
        System.exit(0);
        AnsiConsole.systemUninstall();
    }

}
