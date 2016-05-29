package unal.informacion.teoria.recorder;

import java.util.ArrayList;

public class User {
    private String name;
    private double[] audioName;
    private double[] audioGo;
    private double[] audioStop;
    private double[] audioLeft;
    private double[] audioRight;

    public double[] getAudioName() {
        return audioName;
    }

    public void setAudioName(double[] audioName) {
        this.audioName = audioName;
    }

    public double[] getAudioGo() {
        return audioGo;
    }

    public void setAudioGo(double[] audioGo) {
        this.audioGo = audioGo;
    }

    public double[] getAudioStop() {
        return audioStop;
    }

    public void setAudioStop(double[] audioStop) {
        this.audioStop = audioStop;
    }

    public double[] getAudioLeft() {
        return audioLeft;
    }

    public void setAudioLeft(double[] audioLeft) {
        this.audioLeft = audioLeft;
    }

    public double[] getAudioRight() {
        return audioRight;
    }

    public void setAudioRight(double[] audioRight) {
        this.audioRight = audioRight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
