package org.gitblocks.gossip;

import org.apache.commons.cli.*;
import org.gitblocks.gossip.incoming.IncomingConnectionManager;
import org.gitblocks.gossip.outgoing.OutgoingConnectionManager;

public class Starter {

    public static final int PORT = 9002;
    private static boolean debug = false;
    private String name;

    // TODO: this is similar to singleton pattern. Refactor this to a better approach.
    private static MessageRouter messageRouter;

    // can only read the debug flag.
    public static boolean isDebug() {
        return debug;
    }

    public static void main(String[] args ){

        //initLogger();

        // parse command line arguments
        Options options = new Options();

        Option remoteNodeOpt = new Option("n", "remoteNodes", true,
                "list of node ip addresses:port numbers separated by comma");
        remoteNodeOpt.setRequired(false);
        options.addOption(remoteNodeOpt);

        Option portOpt = new Option("p", "port", true,
                "default port is 9002. If you want to override the port use the -p option");
        portOpt.setRequired(false);
        options.addOption(portOpt);

        Option debugOpt = new Option("d", "debug", false,
                "turn on debug messages");
        debugOpt.setRequired(false);
        options.addOption(debugOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("remoteNode", options);
            System.exit(1);
        }

        String remoteNodesStr = cmd.getOptionValue("remoteNodes");
        String portStr = cmd.getOptionValue("port");
        debug = cmd.hasOption("debug");

        String remoteNodes = remoteNodesStr;
        int port = PORT;


        if( portStr != null && !portStr.isEmpty() ){
            port = Integer.parseInt(portStr);
        }


        System.out.println("Localserver: 127.0.0.1 port:"+ port);
        if( remoteNodes != null ){
            System.out.println("RemoteHost: "+ remoteNodes );
        }

        // Generate a name for the "node"
        String nodeName = RandomString.generateString(5);

        // By Default, Start the server
        IncomingConnectionManager incoming = new IncomingConnectionManager(nodeName);


        // if a remote host ip address is provided, connect to that address
        OutgoingConnectionManager outgoing = new OutgoingConnectionManager(nodeName, port, remoteNodes);

        messageRouter = new MessageRouter(incoming, outgoing);

        incoming.start(port);
        outgoing.start();

        incoming.waitForCompletion();
    }

    public static MessageRouter getMessageRouter(){
        return messageRouter;
    }

    public static void initLogger(){

//        // Initialize logger
//        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
//
//        AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
//        FilterComponentBuilder flow = builder.newFilter("MarkerFilter", Filter.Result.ACCEPT, Filter.Result.DENY);
//        flow.addAttribute("marker", "FLOW");
//
//        console.add(flow);
//
//        AppenderComponentBuilder file = builder.newAppender("log", "File");
//        file.addAttribute("fileName", "node.log");
//
//        builder.add(console);
//        builder.add(file);
//
//        AppenderComponentBuilder rollingFile = builder.newAppender("rolling", "RollingFile");
//        rollingFile.addAttribute("fileName", "rolling.log");
//        rollingFile.addAttribute("filePattern", "rolling-%d{MM-dd-yy}.log.gz");
//
//
//        ComponentBuilder triggeringPolicies = builder.newComponent("Policies")
//                .addComponent(builder.newComponent("CronTriggeringPolicy")
//                        .addAttribute("schedule", "0 0 0 * * ?"))
//                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
//                        .addAttribute("size", "100M"));
//
//        rollingFile.addComponent(triggeringPolicies);
//
//
//        builder.add(rollingFile);
//
//        LayoutComponentBuilder standard
//                = builder.newLayout("PatternLayout");
//        standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
//
//        console.add(standard);
//        file.add(standard);
//        rollingFile.add(standard);
//
//        RootLoggerComponentBuilder rootLogger
//                = builder.newRootLogger(Level.ERROR);
//        rootLogger.add(builder.newAppenderRef("stdout"));
//
//        builder.add(rootLogger);
//
//        LoggerComponentBuilder logger = builder.newLogger("com", Level.DEBUG);
//        logger.add(builder.newAppenderRef("log"));
//        logger.addAttribute("additivity", false);
//
//        builder.add(logger);
//
//
//        Configurator.initialize(builder.build());
    }
}
