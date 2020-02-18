package pl.jakubraban.desist.cli.commands;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;
import pl.jakubraban.desist.DesistSessionSpec;
import pl.jakubraban.desist.PasswordGenerator;
import pl.jakubraban.desist.cli.CommandLineUtils;
import pl.jakubraban.desist.dao.LockDao;
import pl.jakubraban.desist.model.Lock;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Command(name = "lock", description = "Manipulate available locks", synopsisSubcommandLabel = "COMMAND", mixinStandardHelpOptions = true)
public class LockCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    private DesistSessionSpec sessionSpec;
    private LockDao locks;

    public LockCommand(DesistSessionSpec sessionSpec) {
        this.sessionSpec = sessionSpec;
        this.locks = new LockDao();
    }

    @Override
    public void run() {
        if (!sessionSpec.isUserLogged()) {
            System.out.println("Log in first to manipulate locks.");
            return;
        }
        throw new ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @Command(name = "create", description = "Create a new lock", mixinStandardHelpOptions = true)
    public void create(@Parameters(index = "0", paramLabel = "lock-id", description = "Identifier of a lock (e.g. service name you don't want to use") String lockIdentifier,
                       @Parameters(index = "1", paramLabel = "passwd", arity = "0..1", description = "Your password to the service you lock." +
                               " Skip this and add --random option to get a random password.") String plainTextPassword,
                       @Option(names = {"-r", "--random"}, description = "Generate a random password instead of providing one by yourself") boolean random,
                       @Option(names = {"-l", "--length"}, paramLabel = "passwd-length", description = "If --random is active, specify length of password (default 20)", defaultValue = "20") int passwordLength) {

        if (!sessionSpec.isUserLogged()) {
            System.out.println("Log in first to create a lock.");
            return;
        }
        if (plainTextPassword == null && !random) {
            System.out.println("Specify a password or add --random parameter to the command");
            return;
        }
        List<Lock> userLocks = locks.getLocksByUser(sessionSpec.getLoggedUser());
        for (Lock userLock : userLocks) {
            if (userLock.getLockIdentifier().equalsIgnoreCase(lockIdentifier)) {
                System.out.println("Lock of this name already exists; remove that lock first.");
                return;
            }
        }
        String passwordUsed = plainTextPassword;
        if (random) {
            passwordUsed = PasswordGenerator.generateRandomPassword(passwordLength);
        }
        Lock newLock = new Lock(sessionSpec.getLoggedUser(), lockIdentifier, passwordUsed);
        locks.save(newLock);

        System.out.println("Lock for " + lockIdentifier + "was created.");
        System.out.println("Set this password:");
        System.out.println(passwordUsed);
        System.out.println("for this service, then use lock activate command to activate the lock.");
    }

    @Command(name = "activate", mixinStandardHelpOptions = true, description = "Activate specified lock for a given duration")
    public void activate(@Parameters(index = "1", paramLabel = "duration", description = "Duration in seconds, minutes, hours of days of this lock," +
                                " e.g. 30s, 10m, 12h, 3d. The number must be an integer.") String durationString,
                         @Parameters(index = "0", paramLabel = "lock-id", description = "Identifier of a lock") String lockIdentifier) {

        if (!sessionSpec.isUserLogged()) {
            System.out.println("Log in first to activate a lock.");
            return;
        }
        Optional<Lock> activatedLock = locks.getLockByIdentifierAndUser(lockIdentifier, sessionSpec.getLoggedUser());
        Duration duration = parseDuration(durationString);
        activatedLock.ifPresentOrElse(lock -> {
                    lock.activate(duration);
                    locks.save(lock);
                    CommandLineUtils.cls();
                    System.out.println("\n" + lockIdentifier + " lock is activated until " + lock.getFormattedExpiryDate());
                },
                () -> System.out.println("Specified lock was not found."));
    }

    private Duration parseDuration(String durationString) {
        char durationUnit = durationString.charAt(durationString.length() - 1);
        String durationValueString = durationString.substring(0, durationString.length() - 1);
        int durationValue = Integer.parseInt(durationValueString);
        Duration result;
        switch (durationUnit) {
            case 's':
                result = Duration.ofSeconds(durationValue);
                break;
            case 'm':
                result = Duration.ofMinutes(durationValue);
                break;
            case 'h':
                result = Duration.ofHours(durationValue);
                break;
            case 'd':
                result = Duration.ofDays(durationValue);
                break;
            default:
                throw new IllegalArgumentException("The specified duration unit was not correct.");
        }
        return result;
    }

}
