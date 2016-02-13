/* JMultiPatcher - yet another rompatcher utility
 Copyright © 2014 - 2016 Josef Andersson <josef.andersson@fripost.org>

 This file is part of JMultiPatcher.

 JMultiPatcher is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 any later version.

 JMultiPatcher is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with JMultiPatcher.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.rom.jmultipatcher.gui.javafx;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

//todo: add commandline feature of batch patching
public class JMultipatcherMain extends Application {

    private static final Logger LOG = Logger.getLogger(JMultipatcherMain.class.getName());
    private static final String LOGFILE = "jmultipatcher.log";
    private static final int LOGFILE_SIZE = 1_048_576;

    @Override
    public void start(final Stage stage) throws IOException {

        setupLogfile();
        setupStage(stage);
    }

    private void setupStage(final Stage stage) throws IOException {
        stage.setTitle("JMultipatcher");
        
        final FXMLLoader loader = new FXMLLoader(JMultipatcherMain.class.getResource("/fxml/JMultiPatcher.fxml"));
        GridPane gridpane = (GridPane) loader.load();

        final MainController controller = (MainController) loader.getController();
        controller.setStage(stage);
        final Scene scene = new Scene(gridpane);
        stage.setScene(scene);
        stage.show();
    }

    private void setupLogfile() throws SecurityException, IOException {
        FileHandler filehandler = new FileHandler(LOGFILE, LOGFILE_SIZE, 1);
        LOG.addHandler(filehandler);
    }
}
