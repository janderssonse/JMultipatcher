/* JMultiPatcher - yet another rompatcher utility
 Copyright © 2014 Josef Andersson <josef.andersson@fripost.org>

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
package com.rom.jmultipatcher;

import com.rom.jmultipatcher.model.CrcModel;
import com.rom.jmultipatcher.model.FileManagerModel;
import com.rom.jmultipatcher.model.SizeModel;
import com.rom.jmultipatcher.patchformat.IPatcher;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable {

    private static final Logger LOG = Logger.getLogger(MainController.class.getName());

    //  @FXML
    // private Parent root;
    @FXML
    private TextField patchfileTextfield;
    @FXML
    private TextField sourcefileTextfield;
    @FXML
    private TextField outputfileTextfield;
    @FXML
    private Text actionText;
    @FXML
    private ChoiceBox patchTypeChBox;
    @FXML
    private Label patchfileLabel;
    @FXML
    private Label romfileLabel;
    @FXML
    private Label outputLabel;

    @FXML
    private Button romfileButton;
    @FXML
    private Button patchfileButton;

    @FXML
    private ToggleGroup patchActionChoiceToogleGroup;

    private final FileChooser fileChooser;
    private Stage stage;
    private File directoryTracker;

    private PatchActionType patchActionType = PatchActionType.APPLY;

    public MainController() {
        fileChooser = new FileChooser();

    }

    @FXML
    public void patchButtonOnClick(final ActionEvent event) {

        final IPatcher patcher = PatcherFactory.INSTANCE.buildPatcher(getSelectedPatchType());

        try {
            final FileManagerModel fileManager = new FileManagerModel(new CrcModel(), new SizeModel());
            fileManager.setPatchfilepath(patchfileTextfield.getText());
            fileManager.setSourcefilepath(sourcefileTextfield.getText());
            fileManager.setTargetfilepath(outputfileTextfield.getText());

            if (isValidFileTextfields()) {
                if (patchActionType == PatchActionType.APPLY) {
                    patcher.applyPatch(fileManager);
                } else {
                    patcher.createPatch(fileManager);
                }
                actionText.setFill(Color.DARKGREEN);
                actionText.setText("Patching done!");
            }
        } catch (IllegalArgumentException ex) {
            LOG.info(ex.getMessage());
            actionText.setFill(Color.DARKRED);
            actionText.setText(ex.getMessage());
        } catch (Exception ex) { //NOPMD //Will change to catching unhandlede exceptions feature in Javafx 8.9
            LOG.severe(ex.getMessage());
            actionText.setFill(Color.DARKRED);
            actionText.setText("Something went very wrong, please see logfile!");
        }

    }

    @FXML
    public void selectPatchfileOnClick(final ActionEvent event) {
        final List<String> filetypes = new ArrayList<>();
        for (PatchType patchType : PatchType.values()) {
            filetypes.add(patchType.getPatchType());
        }

        configureFileChooser(fileChooser, filetypes, "Select patch file!");
        directoryTracker = fileChooser.showOpenDialog(stage);
        if (directoryTracker != null) {
            if (patchActionType == PatchActionType.APPLY) {
                String extension = getSelectedFileextension(directoryTracker.getName());
                patchTypeChBox.setValue(extension);
            }
            setTextField(directoryTracker, patchfileTextfield);
            clearStatusText();
        }
    }

    private void setTextField(final File file, final TextField textField) {
        //desktop.open(file);
        textField.setText(file.getAbsolutePath());
    }

    @FXML
    public void selectRomToPatchOnClick(final ActionEvent event) {

        final List<String> filetypes = Arrays.asList(new String[]{"*"});
        configureFileChooser(fileChooser, filetypes, "Select a rom you would like to patch");
        directoryTracker = fileChooser.showOpenDialog(stage);
        if (directoryTracker != null) {
            setTextField(directoryTracker, sourcefileTextfield);
            if (patchActionType == PatchActionType.APPLY) {
                outputfileTextfield.setText(Utils.addPatchedTextToFilePath(sourcefileTextfield.getText()));
            } else {
                outputfileTextfield.setText(Utils.addPatchTextToFilePath(sourcefileTextfield.getText(), getSelectedPatchType()));

            }
            clearStatusText();
        }

    }

    private void configureFileChooser(final FileChooser fileChooser, final List<String> filetypes, final String selectText) {
        fileChooser.setTitle(selectText);
        File startDirectory = null;

        if (directoryTracker != null) {
            startDirectory = directoryTracker.getParentFile();
        }

        if (startDirectory == null) {
            startDirectory = new File(System.getProperty("user.home"));
        }

        fileChooser.setInitialDirectory(startDirectory);
        fileChooser.getExtensionFilters().clear();

        String selectedPatchType = patchTypeChBox.getSelectionModel().getSelectedItem().toString();

        if (filetypes.contains(selectedPatchType)) {
            //we want the selected patchtype to be pre-choosen, and in JavaFX 2.2 I couldnt find a property to set it,
            int indexOf = filetypes.indexOf(selectedPatchType);
            filetypes.remove(indexOf);
            filetypes.add(0, selectedPatchType);
        }

        if (patchActionType == PatchActionType.APPLY) {
            List<String> extensions = new ArrayList<>();
            for (final String filetype : filetypes) {
                extensions.add("*." + filetype);
            }

            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Patch Files", extensions) //NOPMD
            );
        }

    }

    void setStage(final Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        List<String> patchTypeList = new ArrayList<>();

        ObservableList<String> patchTypes = FXCollections.observableList(patchTypeList);
        for (PatchType patchType : PatchType.values()) {
            patchTypes.add(patchType.getPatchType());
        }
        patchTypeChBox.setItems(patchTypes);
        patchTypeChBox.setMinWidth(100);
        patchTypeChBox.getSelectionModel().selectFirst();

        patchTypeChBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue ov, final String t, final String t1) {
                if (!t.equalsIgnoreCase(t1)) {
                    resetFields();
                }
            }
        });

        patchActionChoiceToogleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
                ObservableMap<Object, Object> properties = patchActionChoiceToogleGroup.getSelectedToggle().getProperties();
                RadioButton r = (RadioButton) patchActionChoiceToogleGroup.getSelectedToggle();

                String dis = r.getText();

                if (dis.equals(PatchActionType.CREATE.getPatchType())) {
                    patchActionType = PatchActionType.CREATE;
                } else {
                    patchActionType = PatchActionType.APPLY;

                }

                setTextfieldAfterPatchActionType();
            }

            private void setTextfieldAfterPatchActionType() {
                if (patchActionType == PatchActionType.CREATE) {
                    patchfileLabel.setText("Patched rom:");
                    romfileLabel.setText("Untouched rom:");
                    outputLabel.setText("Output Patctfile:");
                    patchfileButton.setText("Patched rom");
                    romfileButton.setText("untocuhed rom sel");
                } else {
                    patchfileLabel.setText("Patch file:");
                    romfileLabel.setText("Rom file:");
                    outputLabel.setText("Output file:");
                    patchfileButton.setText("Select a patchfile");
                    romfileButton.setText("Selet untouched");
                }
            }
        });
    }

    private String getSelectedFileextension(final String filename) {
        for (PatchType patchType : PatchType.values()) {
            if (filename.toLowerCase().endsWith("." + patchType.getPatchType())) {
                return patchType.getPatchType();
            }
        }
        return "";
    }

    @FXML
    public void resetFields() {
        patchfileTextfield.clear();
        sourcefileTextfield.clear();
        outputfileTextfield.clear();
        clearStatusText();
    }

    private boolean isValidFileTextfields() {
        return !patchfileTextfield.getText().isEmpty() && !sourcefileTextfield.getText().isEmpty() && !outputfileTextfield.getText().isEmpty();
    }

    private PatchType getSelectedPatchType() {
         String toUpperCase = patchTypeChBox.getSelectionModel().getSelectedItem().toString().toUpperCase();
        return PatchType.valueOf(toUpperCase);
    }

    private void clearStatusText() {
        actionText.setText("");
    }
}
