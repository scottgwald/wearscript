package com.dappervision.wearscript.takeTwo;

import com.coremedia.iso.boxes.Container;
import com.dappervision.wearscript.Log;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CompositeFile {
    private ArrayList<FileEntry> files;
    private boolean isVideo;
    private boolean tailIsFinished = true;

    public CompositeFile(boolean isVideo) {
        files = new ArrayList<FileEntry>();
        this.isVideo = isVideo;
    }

    public void addFile(String filePath, long fileDuration) {
        if (files.isEmpty()) {
            files.add(new FileEntry(filePath,0,fileDuration));
        } else {
            if (!tailIsFinished) {
                throw new IllegalStateException("Cannot Add Fragment: Recording in tail");
            }
            FileEntry lastFile = this.files.get(files.size()-1);
            files.add(new FileEntry(filePath
                    ,lastFile
                    .getStartTime()+ lastFile.
                    getFileDuration(), fileDuration));
        }
        this.tailIsFinished = false;
    }

    public void setTailDuration(long duration) {
        this.files.get(this.files.size()-1).setFileDuration(duration);
        this.tailIsFinished = true;
    }

    public boolean flattenFile(){
        if (files.size() <=1 && !tailIsFinished) {
            return false;
        } else {
            ArrayList<FileEntry> toMerge = new ArrayList<FileEntry>();
            for (int i = 0 ; i<this.files.size()-1 ; i++ ) {
                toMerge.add(this.files.get(i));
            }
            String mergedFileName = generateMergedFileName(toMerge.get(0).getFilePath(),
                    toMerge.get(toMerge.size() - 1).getFilePath());

            boolean merged = false;
            if (isVideo) {
                merged = this.flattenVideo(toMerge,mergedFileName);
            } else {

            }
            FileEntry lastFile = this.files.get(this.files.size()-2);
            if (merged) {
                for (int i=0;i< toMerge.size();i++) {
                    this.files.remove(0);
                }
                this.files.add(0, new FileEntry(mergedFileName, 0, lastFile
                        .getStartTime() + lastFile.getFileDuration()));
            }
            return true;
        }

    }

    public FileEntry getTail() {
        return files.get(files.size() - 1);
    }

    private String generateMergedFileName (String firstMerge , String lastMerge) {
        String[] first = firstMerge.split("/");
        String firstName = first[first.length-1];
        String[] second = lastMerge.split("/");
        String secondName = second[second.length-1];
        String[] trueName = firstName.split("-");
        String finalFirst = trueName[0];
        String[] removeExtension = finalFirst.split("\\.");
        finalFirst= removeExtension[0];
        finalFirst += "-"+secondName;
        String result = "";
        for (int i = 0; i< first.length-1; i++) {
            result+=first[i];
            result+="/";
        }
        result+=finalFirst;
        return result;
    }
    private boolean flattenVideo (List<FileEntry> toMerge, String fileName) {
        ArrayList<Movie> movies = new ArrayList<Movie>();
        try {

            for (FileEntry f : toMerge) {
                movies.add(MovieCreator.build(f.getFilePath()));
            }

            List<Track> videoTracks = new LinkedList<Track>();
            List<Track> audioTracks = new LinkedList<Track>();

            for (Movie m : movies) {
                for (Track t :m.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                }
            }

            Movie result = new Movie();
            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks.
                        toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack((videoTracks.
                        toArray(new Track[videoTracks.size()]))));
            }
            Container out = new DefaultMp4Builder().build(result);

            FileChannel fc = new RandomAccessFile(String.format(fileName),"rw").getChannel();
            out.writeContainer(fc);
            fc.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public FileTimeTuple getFileFromTime(long mSecsFromBeginning) {
        if (mSecsFromBeginning < 0) {
            throw new IllegalArgumentException("Can't access negative values");
        }

        FileTimeTuple target = null;
        for (int i = 0 ; i < files.size()-1; i ++) {
            if (files.get(i).getStartTime() <= mSecsFromBeginning &&
                    files.get(i+1).getStartTime() > mSecsFromBeginning) {
                target = new FileTimeTuple(files.get(i).getFilePath(),
                        mSecsFromBeginning- files.get(i).getStartTime());
                break;
            }
        }

        if (target == null) {
            target = new FileTimeTuple(this.files.get(this.files.size()-1).getFilePath(),
                    mSecsFromBeginning -this.files.get(this.files.size()-1).getStartTime());
        }
        return target;
    }

    public FileTimeTuple getFileFromJump(long mSecsJump, long mSecsInFile, String filePath) {
        FileEntry target = getFileEntry(filePath);
        if (target == null) {
            throw new IllegalArgumentException("File " + filePath + " is not an entry of CompositeFile");
        }
        long relativeJump = mSecsInFile + mSecsJump + target.getStartTime();
        return getFileFromTime(relativeJump);
    }

    /**
     * Returns the time relative to the beginning of the first file, given an arbitrary time in an
     * arbitrary file.
     * @param tuple The FileTimeTuple containing the file and time within that file
     * @return The time in absolute form, relative to the beginning of the first file
     */
    public long getTime(FileTimeTuple tuple) {
        return tuple.getTimeInFile() + getFileEntry(tuple.getFilePath()).getStartTime();
    }

    public FileEntry getFileEntry(String filePath) {
        FileEntry target = null;
        for (FileEntry f : files) {
            if (f.getFilePath().equals(filePath)) {
                target = f;
            }
        }
        return target;
    }

    public void print() {
        for (FileEntry f : files) {
            Log.d("print file", f.getFilePath() + " : "+ Long.toString(f.getStartTime()));
        }
    }

    public int size() {
        return files.size();
    }
}
