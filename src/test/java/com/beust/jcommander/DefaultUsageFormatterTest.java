package com.beust.jcommander;

import com.beust.jcommander.args.*;
import com.beust.jcommander.internal.Maps;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ResourceBundle;
import java.util.*;

@Test
public class DefaultUsageFormatterTest {

    /**
     * Returns a resource bundle for the en_US locale, this is to prevent needing an exhaustive list of locales
     * to allow the related tests to run on foreign machines.
     *
     * @return a hard-coded resource bundle for the en_US locale
     */
    public static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle("MessageBundle", new Locale("en", "US"));
    }

    private enum TestEnum1 {
        A, B, C, D
    }

    private enum TestEnum2 {
    }

    @Test
    public void testUsage() {
        class MainParameters {
            @Parameter(names = {"-a", "--a", "--a-parameter"}, description = "a parameter")
            public int a;
        }
        @Parameters(commandNames = "one", commandDescription = "one command")
        class OneCommand {
            @Parameter(names = {"-b", "--b", "--b-parameter"}, description = "b parameter")
            public int b;
        }
        @Parameters(commandNames = "two", commandDescription = "two command")
        class TwoCommand {
            @Parameter(names = {"-c", "--c", "--c-parameter"}, description = "c parameter")
            public int c;
        }
        JCommander jc = JCommander.newBuilder()
                .addObject(new MainParameters())
                .addCommand(new OneCommand())
                .addCommand(new TwoCommand())
                .build();
        StringBuilder output = new StringBuilder();
        jc.setConsole(new StringBuilderConsole(output));
        jc.usage();
        String expected = """
            Usage: <main class> [options] [command] [command options]
            
              Options:
                -a, --a, --a-parameter
                  a parameter
                  Default: 0
            
              Commands:
                one      one command
                  Usage: one [options]
            
                    Options:
                      -b, --b, --b-parameter
                        b parameter
                        Default: 0
            
                two      two command
                  Usage: two [options]
            
                    Options:
                      -c, --c, --c-parameter
                        c parameter
                        Default: 0
            """;
        Assert.assertEquals(output.toString(), expected);
    }

    @Test
    public void testOutputFormat() {
        class ArgsTemplate {
            @Parameter(names = {"--a", "-a"})
            public int a;
            @Parameter(names = {"--b", "-b"})
            public int b = 2;
            @Parameter(names = {"--c", "-c"}, description = "sets c")
            public int c;
            @Parameter(names = {"--d", "-d"}, description = "sets d")
            public int d = 2;
            @Parameter(names = {"--e"})
            public TestEnum1 e;
            @Parameter(names = {"--f"})
            public TestEnum1 f = TestEnum1.A;
            @Parameter(names = {"--g"}, description = "sets g")
            public TestEnum1 g;
            @Parameter(names = {"--h"}, description = "sets h")
            public TestEnum1 h = TestEnum1.A;
            @Parameter(names = {"-i"})
            public TestEnum2 i;
            @Parameter(names = {"-k"}, description = "sets k")
            public TestEnum2 k;
        }

        // setup
        StringBuilder sb = new StringBuilder();
        JCommander jc = JCommander.newBuilder()
                .addObject(new ArgsTemplate())
                .build();

        // action
        jc.getUsageFormatter().usage(sb);

        // verify
        String expected = """
            Usage: <main class> [options]
            
              Options:
                --a, -a
                  Default: 0
                --b, -b
                  Default: 2
                --c, -c
                  sets c
                  Default: 0
                --d, -d
                  sets d
                  Default: 2
                --e
                  Options: [A, B, C, D]
                --f
                  Options: [A, B, C, D]
                  Default: A
                --g
                  sets g
                  Possible Values: [A, B, C, D]
                --h
                  sets h
                  Default: A
                  Possible Values: [A, B, C, D]
                -i
                  Options: []
                -k
                  sets k
                  Possible Values: []
            """;
        Assert.assertEquals(sb.toString(), expected);
    }

    @Test
    public void testPlaceholder() {
        class ArgsTemplate {
            @Parameter(names = {"-i"}, placeholder = "<filename>")
            public String inputFilename;
        }

        // setup
        StringBuilder sb = new StringBuilder();
        JCommander jc = JCommander.newBuilder()
                .addObject(new ArgsTemplate())
                .build();

        // action
        jc.getUsageFormatter().usage(sb);

        // verify
        String expected = """
            Usage: <main class> [options]
            
              Options:
                -i <filename>
            
            """;
        Assert.assertEquals(sb.toString(), expected);
    }

    @Test
    public void testLongMainParameterDescription() {
        //setup
        JCommander jc = JCommander.newBuilder()
                .addObject(new ArgsLongMainParameterDescription())
                .build();
        StringBuilder sb = new StringBuilder();

        //action
        jc.getUsageFormatter().usage(sb);

        //verify
        for (String line : sb.toString().split("\n")) {
            Assert.assertTrue(line.length() <= jc.getColumnSize(), "line length < column size");
        }
    }

    @Test
    public void testLongCommandDescription() throws Exception {
        //setup
        JCommander jc = JCommander.newBuilder()
                .addCommand(new ArgsLongCommandDescription())
                .build();
        StringBuilder sb = new StringBuilder();

        //action
        jc.getUsageFormatter().usage(sb);

        //verify
        for (String line : sb.toString().split("\n")) {
            Assert.assertTrue(line.length() <= jc.getColumnSize(), "line length < column size");
        }
    }

    @Test
    public void testDescriptionWrappingLongWord() {
        //setup
        StringBuilder sb = new StringBuilder();
        final JCommander jc = JCommander.newBuilder()
                .addObject(new ArgsLongDescription())
                .build();

        //action
        jc.getUsageFormatter().usage(sb);

        //verify
        for (String line : sb.toString().split("\n")) {
            Assert.assertTrue(line.length() <= jc.getColumnSize(), "line length < column size");
        }
    }

    @Test
    public void programName() {
        JCommander jcommander = new JCommander();
        String programName = "main";
        jcommander.setProgramName(programName);
        StringBuilder sb = new StringBuilder();
        jcommander.getUsageFormatter().usage(sb);

        Assert.assertTrue(sb.toString().contains(programName));
        Assert.assertEquals(jcommander.getProgramName(), programName);
    }

    @Test
    public void dontShowOptionUsageIfThereAreNoOptions() {
        class CommandTemplate {
            @Parameter
            List<String> parameters = new ArrayList<>();
        }

        CommandTemplate template = new CommandTemplate();
        JCommander jcommander = JCommander.newBuilder()
                .addObject(template)
                .build();
        jcommander.setProgramName("main");
        StringBuilder sb = new StringBuilder();
        jcommander.getUsageFormatter().usage(sb);
        Assert.assertEquals(sb.toString().indexOf("options"), -1);
    }

    @Test
    public void annotationsAndDynamicParameters() {
        class DSimple {
            @DynamicParameter(names = "-D", description = "Dynamic parameters go here")
            public Map<String, String> params = Maps.newHashMap();

            @DynamicParameter(names = "-A", assignment = "@")
            public Map<String, String> params2 = Maps.newHashMap();
        }

        JCommander jc = JCommander.newBuilder()
                .addObject(new DSimple())
                .build();
        jc.getUsageFormatter().usage(new StringBuilder());
    }

    /**
     * Getting the description of a nonexistent command should throw an exception.
     */
    @Test(expectedExceptions = ParameterException.class)
    public void nonexistentCommandShouldThrow() {
        String[] argv = {};
        JCommander jc = JCommander.newBuilder().addObject(new Object()).build();
        jc.parse(argv);
        jc.getUsageFormatter().getCommandDescription("foo");
    }

    @Test
    public void i18MissingKeyForCommand() {
        JCommander jc = JCommander.newBuilder()
                .addObject(new ArgsHelp())
                .resourceBundle(getResourceBundle())
                .build();
        jc.addCommand(new ArgsLongCommandDescription());
        StringBuilder sb = new StringBuilder();
        jc.getUsageFormatter().usage(sb);
        String usage = sb.toString();
        Assert.assertTrue(usage.contains("text"));
    }

    @Test
    public void noParseConstructor() {
        JCommander jCommander = JCommander.newBuilder()
                .addObject(new ArgsMainParameter1())
                .build();
        jCommander.getUsageFormatter().usage(new StringBuilder());
        // Before fix, this parse would throw an exception, because it calls createDescription, which
        // was already called by usage(), and can only be called once.
        jCommander.parse();
    }

    /**
     * Test a use case where there are required parameters, but you still want
     * to interrogate the options which are specified.
     */
    @Test
    public void usageWithRequiredArgsAndResourceBundle() {
        ArgsHelp argsHelp = new ArgsHelp();
        JCommander jc = JCommander.newBuilder()
                .addObject(new Object[] {argsHelp, new ArgsRequired()})
                .resourceBundle(getResourceBundle())
                .build();
        // Should be able to display usage without triggering validation
        jc.getUsageFormatter().usage(new StringBuilder());
        try {
            jc.parse("-h");
            Assert.fail("Should have thrown a required parameter exception");
        } catch (ParameterException e) {
            Assert.assertTrue(e.getMessage().contains("are required"));
        }
        Assert.assertTrue(argsHelp.help);
    }

    @Test
    public void usageShouldNotChange() {
        JCommander jc = JCommander.newBuilder().addObject(new Args1()).build();
        jc.parse("-log", "1");
        StringBuilder sb = new StringBuilder();
        jc.getUsageFormatter().usage(sb);
        String expected = sb.toString();

        sb = new StringBuilder();
        jc.getUsageFormatter().usage(sb);
        String actual = sb.toString();
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "This used to run out of memory")
    public void oom() {
        JCommander jc = JCommander.newBuilder()
                .addObject(new ArgsOutOfMemory())
                .build();
        jc.getUsageFormatter().usage(new StringBuilder());
    }

    @Test
    public void doNotDisplayHelpDefaultValue() {
        class Arg {
            @Parameter(names = "--help", help = true)
            public boolean help = false;
        }
        Arg args = new Arg();
        String[] argv = {"--help"};
        JCommander jc = JCommander.newBuilder().addObject(args).build();
        jc.parse(argv);

        StringBuilder sb = new StringBuilder();

        jc.getUsageFormatter().usage(sb);

        Assert.assertFalse(sb.toString().contains("Default"));
    }

    @Test
    public void usageCommandsUnderUsage() {
        class Arg {
        }
        @Parameters(commandDescription = "command a")
        class ArgCommandA {
            @Parameter(description = "command a parameters")
            List<String> parameters;
        }
        @Parameters(commandDescription = "command b")
        class ArgCommandB {
            @Parameter(description = "command b parameters")
            List<String> parameters;
        }

        Arg a = new Arg();

        JCommander c = JCommander.newBuilder()
                .addObject(a)
                .build();
        c.addCommand("a", new ArgCommandA());
        c.addCommand("b", new ArgCommandB());

        StringBuilder sb = new StringBuilder();
        c.getUsageFormatter().usage(sb);
        Assert.assertTrue(sb.toString().contains("[command options]\n\n  Commands:"));
    }

    @Test
    public void usageWithEmptyLine() {
        class Arg {
        }
        @Parameters(commandDescription = "command a")
        class ArgCommandA {
            @Parameter(description = "command a parameters")
            List<String> parameters;
        }
        @Parameters(commandDescription = "command b")
        class ArgCommandB {
            @Parameter(description = "command b parameters")
            List<String> parameters;
        }

        Arg a = new Arg();

        JCommander c = JCommander.newBuilder()
                .addObject(a)
                .build();
        c.addCommand("a", new ArgCommandA());
        c.addCommand("b", new ArgCommandB());

        StringBuilder sb = new StringBuilder();
        c.getUsageFormatter().usage(sb);
        String expected = """
            Usage: <main class> [command] [command options]
            
              Commands:
                a      command a
                  Usage: a command a parameters
            
                b      command b
                  Usage: b command b parameters
            """;
        Assert.assertEquals(sb.toString(), expected);
    }

    @Test
    public void usageWithSubCommands() {
        class Arg {
        }
        @Parameters(commandDescription = "command a")
        class ArgCommandA {
            @Parameter(description = "command a parameters")
            List<String> parameters;
        }
        @Parameters(commandDescription = "command b")
        class ArgCommandB {
            @Parameter(description = "command b parameters")
            List<String> parameters;
        }

        Arg a = new Arg();

        JCommander c = JCommander.newBuilder()
                .addObject(a)
                .build();
        c.setColumnSize(100);
        c.addCommand("a", new ArgCommandA());

        // b is a sub-command of a
        JCommander aCommand = c.getCommands().get("a");
        aCommand.addCommand("b", new ArgCommandB());

        StringBuilder sb = new StringBuilder();
        c.getUsageFormatter().usage(sb);
        Assert.assertTrue(sb.toString().contains("command a parameters\n\n        Commands:"));
        Assert.assertTrue(sb.toString().contains("command b\n            Usage:"));
    }

    @Test
    public void emptyStringAsDefault() {
        class Arg {
            @Parameter(names = "-x")
            String s = "";
        }
        Arg a = new Arg();
        StringBuilder sb = new StringBuilder();
        JCommander c = JCommander.newBuilder()
                .addObject(a)
                .build();
        c.getUsageFormatter().usage(sb);
        Assert.assertTrue(sb.toString().contains("Default: <empty string>"));
    }

    @Test
    public void dontShowNullForMissingCommandDescription() {
        // given
        class Command {}
        JCommander jc = JCommander.newBuilder().addCommand("testCommand", new Command()).build();
        StringBuilder sb = new StringBuilder();

        // when
        jc.getUsageFormatter().usage(sb);

        // then
        Assert.assertFalse(sb.toString().contains("null"));
    }
}
