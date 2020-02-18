package pl.jakubraban.desist.cli.commands;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;
import pl.jakubraban.desist.DesistSessionSpec;
import pl.jakubraban.desist.LockManager;
import pl.jakubraban.desist.PasswordGenerator;
import pl.jakubraban.desist.cli.CommandLineTable;
import pl.jakubraban.desist.cli.CommandLineUtils;
import pl.jakubraban.desist.dao.LockDao;
import pl.jakubraban.desist.exceptions.LockException;
import pl.jakubraban.desist.exceptions.LockRemovalException;
import pl.jakubraban.desist.model.Lock;

import static picocli.CommandLine.Help.Ansi.AUTO;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;

@Command(name = "lock", description = "Manipulate available locks", synopsisSubcommandLabel = "COMMAND", mixinStandardHelpOptions = true)
public class LockCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    private DesistSessionSpec sessionSpec;
    private LockDao locks;
    private LockManager lockManager;

    public LockCommand(DesistSessionSpec sessionSpec) {
        this.sessionSpec = sessionSpec;
        this.locks = new LockDao();
        this.lockManager = new LockManager(sessionSpec);
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

        try {
            if (plainTextPassword == null && !random) {
                System.out.println("Specify a password or add --random parameter to the command");
                return;
            }
            String passwordUsed = plainTextPassword;
            if (random) {
                passwordUsed = PasswordGenerator.generateRandomPassword(passwordLength);
            }
            lockManager.createLock(lockIdentifier, passwordUsed);
            System.out.println("Lock for " + lockIdentifier + " was created.");
            System.out.println("Set this password:");
            System.out.println(AUTO.string("@|bg(red),black " + passwordUsed + "|@"));
            System.out.println("for this service, then use lock activate command to activate the lock.");
        } catch (LoginException | LockException e) {
            System.out.println("Failed to create lock");
            System.out.println(e.getMessage());
        }
    }

    @Command(name = "activate", mixinStandardHelpOptions = true, description = "Activate specified lock for a given duration")
    public void activate(@Parameters(index = "1", paramLabel = "duration", description = "Duration in seconds, minutes, hours of days of this lock," +
                                " e.g. 30s, 10m, 12h, 3d. The number must be an integer.") String durationString,
                         @Parameters(index = "0", paramLabel = "lock-id", description = "Identifier of a lock") String lockIdentifier,
                         @Option(names = {"-a", "--again"}, defaultValue = "false", description = "Use to reactivate an opened or expired lock again with the same password") boolean again) {

        try {
            Duration lockDuration = parseDuration(durationString);
            var lock = lockManager.activateLock(lockIdentifier, lockDuration);
            CommandLineUtils.cls();
            System.out.println("\n" + lockIdentifier + " lock is activated until " + lock.getFormattedExpiryDate());
        } catch (LoginException | LockException e) {
            System.out.println("Failed to activate lock");
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Duration value you specified was not correct");
        } catch (IllegalArgumentException e) {
            System.out.println("Duration unit you specified was not correct");
        }

    }

    @Command(name = "open", mixinStandardHelpOptions = true, description = "Open an expired lock and get the password")
    public void open(@Parameters(index = "0", paramLabel = "lock-id", description = "Identifier of a lock") String lockIdentifier,
                     @Option(names = {"-c", "--copy"}, description = "Copy password to clipboard") boolean copy,
                     @Option(names = {"-n", "--no-print"}, description = "Don't print the password to console") boolean noPrint) {

        try {
            String plainTextPassword = lockManager.openLock(lockIdentifier);
            if (copy) {
                copyToClipboard(plainTextPassword);
                System.out.println("Password to " + lockIdentifier + " lock was copied to clipboard");
            }
            if (!noPrint) System.out.println(AUTO.string("Your " + lockIdentifier + " password is" + "\n@|bg(red),black " + plainTextPassword + "|@"));
            if (!copy && noPrint) System.out.println("Please use --no-print option only with --copy option");
        } catch (LockException | LoginException e) {
            System.out.println("Failed to open lock");
            System.out.println(e.getMessage());
        }
    }

    @Command(name = "extend", mixinStandardHelpOptions = true, description = "Delay expiry moment of an active lock by a given duration")
    public void extend(@Parameters(index = "0", paramLabel = "lock-id", description = "Identifier of a lock") String lockIdentifier,
                       @Parameters(index = "1", paramLabel = "duration", description = "Duration in seconds, minutes, hours of days of this lock," +
                               " e.g. 30s, 10m, 12h, 3d. The number must be an integer.") String durationString) {

        try {
            Duration lockDuration = parseDuration(durationString);
            Lock lock = lockManager.extendLock(lockIdentifier, lockDuration);
            System.out.println(lockIdentifier + " lock was extended and now expires on " + lock.getFormattedExpiryDate());
        } catch (LoginException | LockException e) {
            System.out.println("Failed to activate lock");
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Duration value you specified was not correct");
        } catch (IllegalArgumentException e) {
            System.out.println("Duration unit you specified was not correct");
        }
    }

    @Command(name = "remove", mixinStandardHelpOptions = true, description = "Remove a lock")
    public void remove(@Parameters(index = "0", arity = "1..*", paramLabel = "lock-id", description = "Identifier of a lock") String[] lockIdentifiers,
                       @Option(names = {"-f", "--force"}, description = "Remove a lock even if it's active or not yet opened") boolean forceRemove) {

        String deletedIdentifier = "";
        try {
            for (String identifier : lockIdentifiers) {
                deletedIdentifier = identifier;
                if (forceRemove) {
                    lockManager.forceRemoveLock(identifier);
                } else {
                    lockManager.removeLock(identifier);
                }
            }
        } catch (LockRemovalException e) {
            System.out.println("Failed to remove lock: " + deletedIdentifier);
            System.out.println("Attempted to remove active or not yet opened lock");
            System.out.println("Use lock remove --force to remove such a lock");
        } catch (LockException | LoginException e) {
            System.out.println("Failed to remove lock");
            System.out.println(e.getMessage());
        }
    }

    @Command(name = "status", mixinStandardHelpOptions = true, description = "Show all locks and their status")
    public void status() {
        try {
            CommandLineTable table = new CommandLineTable();
            table.setShowVerticalLines(true);
            String[] tableHeaders = {"Identifier", "Status", "Active since", "Expires on"};
            table.setHeaders(tableHeaders);
            for (Lock lock : lockManager.status()) {
                table.addRow(lock.getLockIdentifier(), lock.getStatusSummary(), lock.getFormattedActivationDate(), lock.getFormattedExpiryDate());
            }
            System.out.println();
            table.print();
        } catch (LoginException e) {
            System.out.println("Operation failed");
            System.out.println(e.getMessage());
        }
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

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new StringSelection(text),
                        null
                );
    }

}
