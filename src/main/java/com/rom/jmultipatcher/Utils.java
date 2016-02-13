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

import com.rom.jmultipatcher.gui.javafx.PatchType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import org.apache.commons.io.FileUtils;

public enum Utils {

    INSTANCE;

    public static void copyFile(final String sourcefile, final String targetfile, final boolean addNameSignature) {
        final Path sourcePath = Paths.get(sourcefile);
        final Path targetPath = Paths.get(targetfile);
        try {
            Files.copy(sourcePath, targetPath, COPY_ATTRIBUTES, REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static void checkFilePermissions(final String filepath, final boolean canRead, final boolean canWrite) {
        final Path path = Paths.get(filepath);
        final File file = path.toFile();
        if (canRead && !file.canRead()) {
            throw new IllegalArgumentException("Can't read file" + filepath);
        }
        if (canWrite && !file.canWrite()) {
            throw new IllegalArgumentException("Can't write to file" + filepath);
        }
    }

    public static char byteToAscii(final byte byteToAscii) {
        return (char) byteToAscii;
    }

    public static String bytearrayToString(final byte[] data) {
        final StringBuilder sbuilder = new StringBuilder(data.length);
        for (int i = 0; i < data.length; ++i) {
            if (data[i] < 0) {
                throw new IllegalArgumentException();
            }
            sbuilder.append((char) data[i]);
        }
        return sbuilder.toString();
    }

    public static long getCRC32(final String filepath, final int bytesBeforeEnd) throws IOException {
        final CRC32 sum_control = new CRC32();
        final byte[] fileAsByteArray = FileUtils.readFileToByteArray(new File(filepath));
        final byte[] copyOfRange = Arrays.copyOfRange(fileAsByteArray, 0, fileAsByteArray.length - bytesBeforeEnd);
        sum_control.update(copyOfRange);
        return sum_control.getValue();
    }

    public static String addPatchedTextToFilePath(final String absoluteTargetfilePath) {
        return absoluteTargetfilePath.replaceAll("\\.(\\w+)$", ".patched.$1");
    }

    public static String addPatchTextToFilePath(final String absoluteTargetfilePath, PatchType patchType) {
        return absoluteTargetfilePath.replaceAll("\\.(\\w+)$", "." + patchType.getPatchType());
    }

    public static byte[] byteArrayfromInt(final int dataSize) {

        ByteBuffer intBB = ByteBuffer.allocate(4);
        return intBB.putInt(dataSize).array();
    }

    public void byteArrayToFile(final String path, final byte[] byteArr) {
        FileOutputStream foStream = null;
        try {
            foStream = new FileOutputStream(path);

            foStream.write(byteArr);
            foStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static int getUnsignedByte(final byte byteToUnsign, final int shift) {
        return (byteToUnsign & 0xFF) << shift;
    }

    public static byte[] concatByteArray(final byte[] one, final byte[] two, int size, int offset) {
        byte[] combined = new byte[size];
        System.arraycopy(one, 0, combined, offset, one.length);
        System.arraycopy(two, 0, combined, one.length + offset, two.length);
        return combined;
    }

}
