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
package com.rom.jmultipatcher.patchformat;

import com.rom.jmultipatcher.model.FileManagerModel;
import com.rom.jmultipatcher.model.CrcModel;
import com.rom.jmultipatcher.model.SizeModel;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UpsPatcherTest {

    /* Integration-like test with real file
     UPS input in HEX:
     UPS1               filesizeX   filesizeY       rel.offset and data     rel. offset and data    crc32X          crc32Y          crc32Z
     /* 55 50 53 31     19 9F       19 9F           82 03 00                88 04 05 00             5E 81 BE F5     35 6E 76 12     8B BC 4B 47   */
    @Test
    public void testUpsPatchNoSwap() throws Exception {

        String patchfilePath = filepathAsString(File.separator + "upspatches" + File.separator + "variableLE.ups");
        String sourcefilePath = filepathAsString(File.separator + "roms" + File.separator + "zeros.rom");
        String outputFilePatch = sourcefilePath.replace(File.separator + "roms" + File.separator + "zeros.rom", File.separator + "output" + File.separator + "zeros.patched.rom");

        IPatcher instance = new UpsPatcher();
        FileManagerModel fileManager = new FileManagerModel(new CrcModel(), new SizeModel());
        fileManager.setPatchfilepath(patchfilePath);
        fileManager.setSourcefilepath(sourcefilePath);
        fileManager.setTargetfilepath(outputFilePatch);
        instance.applyPatch(fileManager);

        try (RandomAccessFile patchedFile = new RandomAccessFile(outputFilePatch, "r");) {
            patchedFile.seek(2);
            assertEquals(patchedFile.readByte(), 3);
            patchedFile.seek(12);
            assertEquals(patchedFile.readByte(), 4);
            assertEquals(patchedFile.readByte(), 5);
        }

    }

    private String filepathAsString(String relativeTestfilePath) {
        Path resourcePath = null;
        try {
            URL resourceUrl = getClass().getResource(relativeTestfilePath);
            if (resourceUrl == null) {
                throw new IllegalArgumentException("Couldn't find/read file");
            }
            resourcePath = Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException ex) {
            System.out.println("Unable to find testfile with relativePath" + relativeTestfilePath);
        }
        return resourcePath == null ? "" : resourcePath.toString();
    }

}
