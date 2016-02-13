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
package com.rom.jmultipatcher.model;

import java.io.RandomAccessFile;
 

public class FileManagerModel {

    private String sourcefilepath;
    private String targetfilepath;
    private String patchfilepath;

    private RandomAccessFile sourcefile;
    private RandomAccessFile targetfile;
    private RandomAccessFile patchfile;

    private  CrcModel crcModel;
    private  SizeModel sizeModel;

    public FileManagerModel(final CrcModel crcModel, final SizeModel sizeModel) {
        this.crcModel = crcModel;
        this.sizeModel = sizeModel;
    }
  
    public String getSourcefilepath() {
        return sourcefilepath;
    }

    public void setSourcefilepath(final String sourcefilepath) {
        this.sourcefilepath = sourcefilepath;
    }

    public String getTargetfilepath() {
        return targetfilepath;
    }

    public void setTargetfilepath(final String targetfilepath) {
        this.targetfilepath = targetfilepath;
    }

    public String getPatchfilepath() {
        return patchfilepath;
    }

    public void setPatchfilepath(final String patchfilepath) {
        this.patchfilepath = patchfilepath;
    }

    public RandomAccessFile getSourcefile() {
        return sourcefile;
    }

    public void setSourcefile(final RandomAccessFile sourcefile) {
        this.sourcefile = sourcefile;
    }

    public RandomAccessFile getTargetfile() {
        return targetfile;
    }

    public void setTargetfile(final RandomAccessFile targetfile) {
        this.targetfile = targetfile;
    }

    public RandomAccessFile getPatchfile() {
        return patchfile;
    }

    public void setPatchfile(final RandomAccessFile patchfile) {
        this.patchfile = patchfile;
    }

 
    public CrcModel getCrcContainer() {
        return crcModel;
    }
 
    public SizeModel getSizeContainer() {
        return sizeModel;
    }

    

}
