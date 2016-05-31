package unal.informacion.teoria.recorder;

import java.util.HashMap;

public class User {
    private String name;
    private double[] audioName;
    private HashMap<String, double[] > commands;

    public User() {
        commands = new HashMap<>();
        audioName = new double[20];
    }

    public double[] getAudioName() {
        return audioName;
    }

    public void setAudioName(double[] audioName) {
        this.audioName = audioName;
    }

    public HashMap<String, double[]> getCommands() {
        return commands;
    }

    public double[] getCommand(String cmd) {
        return commands.get(cmd);
    }

    public void setCommands(HashMap<String, double[]> commands) {
        this.commands = commands;
    }

    public void setCommand(String key, double[] commands) {
        this.commands.put(key, commands);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
