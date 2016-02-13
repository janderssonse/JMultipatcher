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

import com.rom.jmultipatcher.Utils;
import com.rom.jmultipatcher.model.FileManagerModel;
import com.rom.jmultipatcher.model.IPSPatch;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IpsPatcher implements IPatcher {

    private final static String IPS_EOF = "EOF";
    private final static String IPS_HEADERSIGNATURE = "PATCH";
    // private static final Logger logger = Logger.getLogger(IpsPatcher.class.getName());

    @Override
    public void applyPatch(final FileManagerModel fileManager) {
        try {
            Utils.copyFile(fileManager.getSourcefilepath(), fileManager.getTargetfilepath(), true);

            try (RandomAccessFile patchfile = new RandomAccessFile(fileManager.getPatchfilepath(), "r"); RandomAccessFile targetfile = new RandomAccessFile(fileManager.getTargetfilepath(), "rw");) {

                fileManager.setPatchfile(patchfile);
                fileManager.setTargetfile(targetfile);
                Utils.checkFilePermissions(fileManager.getPatchfilepath(), true, false);
                Utils.checkFilePermissions(fileManager.getSourcefilepath(), true, false);
                Utils.checkFilePermissions(fileManager.getTargetfilepath(), true, true);

                final byte[] offset = new byte[3];
                final byte[] dataSize = new byte[2];

                validatePatchfile(fileManager);

                for (fileManager.getPatchfile().read(offset); !eofMarker(offset); fileManager.getPatchfile().read(offset)) {

                    final int offsetAsInt = getOffsetAsInt(offset);
                    if (fileManager.getTargetfile().length() < offsetAsInt) {
                        throw new IllegalArgumentException("IPS-offset tried to patch outside length of targetfile." + System.lineSeparator() + "Offset:" + offsetAsInt + " targetfile: " + fileManager.getTargetfile());
                    }

                    fileManager.getPatchfile().read(dataSize);
                    final int dataSizeAsInt = getDataSizeAsInt(dataSize);
                    targetfile.seek(offsetAsInt);

                    if (dataSizeAsInt == 0) {
                        writeRLERecord(fileManager);
                    } else {
                        final byte data[] = new byte[dataSizeAsInt]; //NOPMD
                        fileManager.getPatchfile().read(data);
                        targetfile.write(data);
                    }

                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
//TO-DO: implement signaturepathc

    /* Check head */
    public static void validatePatchfile(final FileManagerModel fileManager) throws IOException {
        final byte[] headsignature = new byte[5];
        fileManager.getPatchfile().read(headsignature);
        final String sig = Utils.bytearrayToString(headsignature);
        if (!IPS_HEADERSIGNATURE.equals(sig)) {
            throw new IllegalArgumentException("Couldn't read headersignature of IPS. " + System.lineSeparator() + " Found signature: bytes " + headsignature + " bytesToAscii" + sig);
        }
    }

    private boolean eofMarker(final byte chs[]) {
        return IPS_EOF.equals(new String(chs));
    }

    private void writeRLERecord(final FileManagerModel fileManager) throws IOException {
        final byte[] rleSize = new byte[2];
        fileManager.getPatchfile().read(rleSize);
        final int rleNrOfBytes = getDataSizeAsInt(rleSize);
        final int val = Utils.getUnsignedByte(fileManager.getPatchfile().readByte(), 0);

        for (int i = 0; i < rleNrOfBytes; i++) {
            fileManager.getTargetfile().write(val);
        }
    }

    public static int getOffsetAsInt(final byte[] offset) {
        return Utils.getUnsignedByte(offset[0], 16) + Utils.getUnsignedByte(offset[1], 8) + Utils.getUnsignedByte(offset[2], 0);
    }

    public static int getDataSizeAsInt(final byte[] dataSize) {
        return Utils.getUnsignedByte(dataSize[0], 8) + Utils.getUnsignedByte(dataSize[1], 0);
    }

   

    @Override
    public void createPatch(FileManagerModel fileManager) {
        try {
            IPSPatch patch = new IPSPatch();
            RandomAccessFile sourcefile = new RandomAccessFile(fileManager.getSourcefilepath(), "r");
            RandomAccessFile targetfile = new RandomAccessFile(fileManager.getTargetfilepath(), "r");
            fileManager.setSourcefile(sourcefile);
            fileManager.setTargetfile(targetfile);

            sourcefile.seek(0);
            targetfile.seek(0);

            long length = fileManager.getSourcefile().length();
            for (int fileBytePos = 0; fileBytePos < length; fileBytePos++) {
                // Difference found.  Find where they stop being different.
                if (sourcefile.read() != targetfile.read()) {
                           
                    int endingOffset = fileBytePos;
                    do {
                        endingOffset = ++endingOffset;
                    } while (fileBytePos < length && sourcefile.read() != targetfile.read());//todo: safer check

                    int recNrOfBytes = endingOffset - fileBytePos;
                    ByteBuffer startingOffsetBB = ByteBuffer.allocate(4);
                    startingOffsetBB.putInt(fileBytePos);
                    ByteBuffer recSizeBB = ByteBuffer.allocate(recNrOfBytes);

                    fileManager.getSourcefile().seek(fileBytePos);
                    fileManager.getSourcefile().read(recSizeBB.array());

                    patch.addRecord(startingOffsetBB.array(), recSizeBB.array());

                    //reset pointers to start looking for next record
                    fileBytePos = fileBytePos + recNrOfBytes;
                    fileManager.getSourcefile().seek(fileBytePos + recNrOfBytes);
                    fileManager.getTargetfile().seek(fileBytePos + recNrOfBytes);
                }
            }
            
            ;
            Files.write(Paths.get(fileManager.getPatchfilepath()), patch.toByteArray());

        } catch (IOException ex) {
            Logger.getLogger(IpsPatcher.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
