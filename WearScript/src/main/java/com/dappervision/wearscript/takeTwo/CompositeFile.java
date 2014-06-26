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
    private ArrayList<FileFragment> fragments;
    private boolean isVideo;
    private boolean tailIsFinished = true;

    public CompositeFile(boolean isVideo) {
        fragments = new ArrayList<FileFragment>();
        this.isVideo = isVideo;
    }

    public void addFragment (String filePath, long fileDuration) {
        if (fragments.isEmpty()) {
            fragments.add(new FileFragment(filePath,0,fileDuration));
        } else {
            if (!tailIsFinished) {
                throw new IllegalStateException("Cannot Add Fragment: Recording in tail");
            }
            FileFragment lastFragment = this.fragments.get(fragments.size()-1);
            fragments.add(new FileFragment(filePath
                    ,lastFragment
                    .getStartTime()+ lastFragment.
                    getFileDuration(), fileDuration));
            this.tailIsFinished = false;
        }
    }

    public void setTailDuration(long duration) {
        this.fragments.get(this.fragments.size()-1).setFileDuration(duration);
        this.tailIsFinished = true;
    }

    public boolean flattenFile(){
        if (fragments.size() <=1 && !tailIsFinished) {
            return false;
        } else {
            ArrayList<FileFragment> toMerge = new ArrayList<FileFragment>();
            for (int i = 0 ; i<this.fragments.size()-1 ; i++ ) {
                toMerge.add(this.fragments.get(i));
            }
            String mergedFileName = generateMergedFileName(toMerge.get(0).getFilePath(),
                    toMerge.get(toMerge.size() - 1).getFilePath());

            boolean merged = false;
            if (isVideo) {
                merged = this.flattenVideo(toMerge,mergedFileName);
            } else {

            }
            FileFragment lastFragment = this.fragments.get(this.fragments.size()-2);
            if (merged) {
                for (int i=0;i< toMerge.size();i++) {
                    this.fragments.remove(0);
                }
                this.fragments.add(0,new FileFragment(mergedFileName,0,lastFragment
                        .getStartTime()+lastFragment.getFileDuration()));
            }
            return true;
        }

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
    private boolean flattenVideo (List<FileFragment> toMerge, String fileName) {
        ArrayList<Movie> movies = new ArrayList<Movie>();
        try {

            for (FileFragment f : toMerge) {
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

    public FileTimeTuple getFragmentFromTime(long mSecsFromBeginning) {
        if (mSecsFromBeginning < 0) {
            throw new IllegalArgumentException("Can't access negative values");
        }

        FileTimeTuple target = null;
        for (int i = 0 ; i < fragments.size()-1; i ++) {
            if (fragments.get(i).getStartTime() <= mSecsFromBeginning &&
                    fragments.get(i+1).getStartTime() > mSecsFromBeginning) {
                target = new FileTimeTuple(fragments.get(i).getFilePath(),
                        mSecsFromBeginning-fragments.get(i).getStartTime());
                break;
            }
        }

        if (target == null) {
            target = new FileTimeTuple(this.fragments.get(this.fragments.size()-1).getFilePath(),
                    mSecsFromBeginning -this.fragments.get(this.fragments.size()-1).getStartTime());
        }
        return target;
    }

    public FileTimeTuple getFragmentFromJump(long mSecsJump , long mSecsInFile , String filePath) {
        FileFragment target = null;
        for (FileFragment f : fragments) {
            if (f.getFilePath().equals(filePath)) {
                target = f;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("File is not a fragment of CompositeFile");
        }
        long relativeJump = mSecsInFile + mSecsJump + target.getStartTime();
        return getFragmentFromTime(relativeJump);
    }

    public void print() {
        for (FileFragment f : fragments) {
            Log.d("print file", f.getFilePath() + " : "+ Long.toString(f.getStartTime()));
        }
    }


}
