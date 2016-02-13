package com.rom.jmultipatcher.gui.cmd;

import com.rom.jmultipatcher.gui.javafx.PatchType;
import com.rom.jmultipatcher.gui.javafx.PatcherFactory;
import com.rom.jmultipatcher.model.CrcModel;
import com.rom.jmultipatcher.model.FileManagerModel;
import com.rom.jmultipatcher.model.SizeModel;
import com.rom.jmultipatcher.patchformat.IPatcher;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

public class CommandLineMain {

    private final String SOURCE_ROM_PATH = "sourcerompath";
    private final String TARGET_ROM_PATH = "targetrompath";
    private final String PATH_ROM_PATH = "patchpath";
    private final String MODE = "mode";

    public static void main(String[] args) {
        CommandLineMain m = new CommandLineMain();
        try {
            CommandLine cmd = m.getParser(args);
            m.run(cmd);
        } catch (ParseException ex) {
            Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run(CommandLine cmd) {
        if (validateArguments(cmd)) {
           
            final FileManagerModel fileManager = new FileManagerModel(new CrcModel(), new SizeModel());
            fileManager.setPatchfilepath(cmd.getOptionValue(PATH_ROM_PATH, ""));
            fileManager.setSourcefilepath(cmd.getOptionValue(SOURCE_ROM_PATH, ""));
            fileManager.setTargetfilepath(cmd.getOptionValue(TARGET_ROM_PATH, ""));

            String extension = FilenameUtils.getExtension(cmd.getOptionValue(PATH_ROM_PATH, ""));
            PatchType patchType = PatchType.valueOf(extension.toUpperCase());
            IPatcher patcher = PatcherFactory.INSTANCE.buildPatcher(patchType);

            if (cmd.getOptionValue(MODE).equalsIgnoreCase("apply")) {
                patcher.applyPatch(fileManager);
            } else {
                patcher.createPatch(fileManager);
            }
        } else {
            System.out.println("Correct you arguments! "
                    + " sourcerompath: " + cmd.getOptionValue(SOURCE_ROM_PATH)
                    + " targetrompath: " + cmd.getOptionValue(TARGET_ROM_PATH)
                    + " patchpath: " + cmd.getOptionValue(PATH_ROM_PATH)
                    + " mode: " + cmd.getOptionValue(MODE));
        }
    }
    
    private boolean validateArguments(CommandLine cmd){
       return cmd.hasOption(SOURCE_ROM_PATH)
                && cmd.hasOption(TARGET_ROM_PATH)
                && cmd.hasOption(PATH_ROM_PATH)
                && cmd.hasOption(MODE);
    }

    private CommandLine getParser(String[] args) throws ParseException {
        return new DefaultParser().parse(constructOptions(), args);
    }

    private Options constructOptions() {
        final Options posixOptions = new Options();
        posixOptions.addOption("s", SOURCE_ROM_PATH, true, "The source rom path.");
        posixOptions.addOption("t", TARGET_ROM_PATH, true, "The target rom path.");
        posixOptions.addOption("p", PATH_ROM_PATH, true, "The path to the patchfile.");
        posixOptions.addOption("m", MODE, true, "The path to the patchfile.");

        return posixOptions;
    }
}
