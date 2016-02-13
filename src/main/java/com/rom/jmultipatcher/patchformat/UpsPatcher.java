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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.CyclomaticComplexity")
public class UpsPatcher implements IPatcher {

    private final static String UPS_HEADERSIGNATURE = "UPS1";
    // private static final Logger logger = Logger.getLogger(UpsPatcher.class.getName());

    @Override
    public void applyPatch(final FileManagerModel fileManager) {

        try (RandomAccessFile sourcefile = new RandomAccessFile(fileManager.getSourcefilepath(), "r"); RandomAccessFile patchfile = new RandomAccessFile(fileManager.getPatchfilepath(), "r");) {

            fileManager.setPatchfile(patchfile);
            fileManager.setSourcefile(sourcefile);

            checkPrerequisite(fileManager);

            //find crc's of the patch!! //as maybe were doing a swap later use atomicint,,
            fileManager.getPatchfile().seek(fileManager.getPatchfile().length() - 12);
            fileManager.getCrcContainer().setSourceCrc(fileManager.getPatchfile().readInt() & 0xFFFFFFFFL);
            fileManager.getCrcContainer().setTargetCrc(fileManager.getPatchfile().readInt() & 0xFFFFFFFFL);
            fileManager.getCrcContainer().setPatchCrc(fileManager.getPatchfile().readInt() & 0xFFFFFFFFL);

            validatePatchfile(fileManager);

            fileManager.getSizeContainer().setSourceSize(variableLengthDecode(fileManager.getPatchfile()));
            fileManager.getSizeContainer().setTargetSize(variableLengthDecode(fileManager.getPatchfile()));

            //    final long crcSourcefile = Utils.getCRC32(fileManager.getSourcefilepath(), 0);
            sourcefile.seek(0);

            //  final long crcUpsX = toUpsCrc(fileManager.getSourcefileCrcFromPatchfile());
            //   final long crcUpsY = toUpsCrc(fileManager.getTargetfileCrcFromPatchfile());
            Utils.copyFile(fileManager.getSourcefilepath(), fileManager.getTargetfilepath(), false);

            writeTargetfile(fileManager);
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("Couldn't find file", ex);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Something went wrong with file IO", ex);
        }
    }

    private void writeTargetfile(final FileManagerModel fileManager) {
        //final String targetfilepath, final AtomicLong sizeSourcefileFromPatchfile, final AtomicLong sizeTargetfileFromPatchfile, final RandomAccessFile sourcefile, final RandomAccessFile patchfile, final long crcUpsY) {

        try (RandomAccessFile targetfile = new RandomAccessFile(fileManager.getTargetfilepath(), "rw");) {
            fileManager.setTargetfile(targetfile);

            Utils.checkFilePermissions(fileManager.getTargetfilepath(), true, true);

            //if fileY is larger...
           // for (long i = fileManager.getSourcefileSizeFromPatchfile(); i < fileManager.getTargetfileSizeFromPatchfile(); i++) {
             //   targetfile.writeByte(0x00);
            //}

            fileManager.getSourcefile().seek(0);
            fileManager.getTargetfile().seek(0);

            long relativeOffset = 0;
            while (fileManager.getPatchfile().getFilePointer() < fileManager.getPatchfile().length() - 12) {
                relativeOffset += variableLengthDecode(fileManager.getPatchfile());
                if (relativeOffset >= fileManager.getSizeContainer().getTargetSize()) {
                    continue;
                }

                fileManager.getSourcefile().seek(relativeOffset);
                fileManager.getTargetfile().seek(relativeOffset);
                for (long i = relativeOffset; i < fileManager.getSizeContainer().getTargetSize(); i++) {
                    final int fileZByte = Utils.getUnsignedByte(fileManager.getPatchfile().readByte(), 0);
                    relativeOffset++;
                    if (fileZByte == 0) {
                        break;
                    }
                    if (i < fileManager.getSizeContainer().getTargetSize()) {
                        final int fileXByte = i < fileManager.getSizeContainer().getSourceSize() ? Utils.getUnsignedByte(fileManager.getSourcefile().readByte(), 0) : 0x00;
                        targetfile.writeByte(fileZByte ^ fileXByte);
                    }
                }
            }
            crcCheckTargetfile(fileManager);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UpsPatcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UpsPatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crcCheckTargetfile(final FileManagerModel fileManager) throws IllegalArgumentException, IOException {
        //crccheck patchedfile
        fileManager.getTargetfile().seek(0);
        final long crcY = Utils.getCRC32(fileManager.getTargetfilepath(), 0);
        final long crcUpsY = toUpsCrc(fileManager.getCrcContainer().getTargetCrc());
        if (crcUpsY != crcY) {
            throw new IllegalArgumentException("CRC32 for written file doesn't seem to " + System.lineSeparator() + " match CRC32 read from patchfile.");
        }
    }

    //ported this code first, but it only seems to take care of if you mixed up input files.. we'll see,
   /* private void performSwap(final RandomAccessFile sourcefile, final AtomicLong sizeSourcefileFromPatchfile, final long crcSourcefile, final long crcUpsX, final AtomicLong sizeTargetfileFromPatchfile, final long crcUpsY, final AtomicLong crcSourceFileFromPatchfile, final AtomicLong crcTargetFileFromPatchfile) throws IllegalArgumentException, IOException {
     //in the special case that sizex == sizey && rcrcx == rcrcy,
     //no swap needs to be performed; checksum test will still work
     if (sourcefile.length() != sizeSourcefileFromPatchfile.get() || crcSourcefile != crcUpsX) {  //x^z->y ?
     if (sourcefile.length() == sizeTargetfileFromPatchfile.get() && crcSourcefile == crcUpsY) {
     //y^z->x
     swap(sizeSourcefileFromPatchfile, sizeTargetfileFromPatchfile);
     swap(crcSourceFileFromPatchfile, crcTargetFileFromPatchfile);
     } else {
     throw new IllegalArgumentException("Input file (the file you're applying the patch to)" + System.lineSeparator() + " and/or CRC32 invalid.");
     }
     }
     }*/
    
    
    protected void checkPrerequisite(final FileManagerModel fileManager) throws IOException {//final String xFilepath, final String zFilepath, long patchFileLength) {
        boolean canRead = true;
        boolean canWrite = false;
        Utils.checkFilePermissions(fileManager.getSourcefilepath(), canRead, canWrite);
        Utils.checkFilePermissions(fileManager.getPatchfilepath(), canRead, canWrite);

        if (fileManager.getPatchfile().length() < 20) {
            throw new IllegalArgumentException("Patch file invalid (file size too small)");
        }
    }

    public byte[] variableLengthEncode(final long offset) throws IOException {
        long offsetVal = offset;
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(104857600);//direct buffers only use virtual mem for unused buffering, so we can init a 100mb buffer without worries

        for (;;) {
            final long cleanedOffsetVal = offsetVal & 0x7f;//get the bits without MSB
            offsetVal >>= 7;
            if (offsetVal == 0) {
                byteBuffer.put((byte) (0x80 | cleanedOffsetVal));
                break;
            }
            byteBuffer.put((byte) cleanedOffsetVal);
            offsetVal--;
        }
        byte[] bytes = new byte[byteBuffer.position()];
        for (int i = 0; i < byteBuffer.position(); i++) {
            bytes[i] = byteBuffer.get(i);
        }
        return bytes;
    }

    private long variableLengthDecode(final RandomAccessFile file) throws IOException {
        long offset = 0, shift = 1;
        for (;;) {
            final int byteFromFile = file.readByte();
            offset += (byteFromFile & 0x7f) * shift; // dont use MSB..
            if ((byteFromFile & 0x80) > 0) { //if MSB is 1 no more bytes (octets) to get
                break;
            }
            shift <<= 7;
            offset += shift;
        }
        return offset;
    }

    public void validatePatchfile(final FileManagerModel fileManager) throws IOException {

        fileManager.getPatchfile().seek(0);
        final long crcZUps = toUpsCrc(fileManager.getCrcContainer().getPatchCrc());
        final long crcOfFileZ = Utils.getCRC32(fileManager.getPatchfilepath(), 4);
        if (crcZUps != crcOfFileZ) {
            throw new IllegalArgumentException("Patchfile CRC32 invalid. Path: " + fileManager.getPatchfilepath() + "FileCRC32: " + crcOfFileZ + "UPSCRC32: " + crcZUps);
        }
        fileManager.getPatchfile().seek(0);
        final byte[] headerSignature = new byte[4];
        fileManager.getPatchfile().read(headerSignature);
        final String header = Utils.bytearrayToString(headerSignature);
        if (!header.equals(UPS_HEADERSIGNATURE)) {
            throw new IllegalArgumentException("Patch header invalid. Instead found: " + header);
        }
    }

    /**
     * *
     * Here we handle that the upsformat is saved in little endianmode.and that
     * also clears the unused bits to stop any signedissues
     *
     * @param offset a 4-byte size array with data in upsformat (little endian)
     */
    public static long upsCrcToLong(final byte[] offset) {
        return (Utils.getUnsignedByte(offset[3], 24) + Utils.getUnsignedByte(offset[2], 16) + Utils.getUnsignedByte(offset[1], 8) + Utils.getUnsignedByte(offset[0], 0)) & 0xFFFFFFFFL;
    }

 

  /*  public void swap(final AtomicLong aLong, final AtomicLong bLong) {
        aLong.set(bLong.getAndSet(aLong.get()));
    }

    public void swap(final AtomicInteger aLong, final AtomicInteger bLong) {
        aLong.set(bLong.getAndSet(aLong.get()));
    }*/

    private long toUpsCrc(final long crc) {
        final byte[] data = ByteBuffer.allocate(4).putInt((int) crc).array();
        return upsCrcToLong(data);
    }

    @Override
    public void createPatch(FileManagerModel fileManager) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
