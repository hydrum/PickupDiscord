package de.gost0r.pickupbot.pickup;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gametype {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static File CONFIGS_DIRECTORY = new File("configs/");

    private String name;
    private boolean active;

    private int teamSize;

    private final List<String> config;

    public Gametype(String name, int teamSize, boolean active) {
        this.setName(name);
        this.setActive(active);
        this.setTeamSize(teamSize);

        config = new ArrayList<String>();
        this.loadGameConfig();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getActive() {
        return active;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getConfig() {
        return config;
    }

    public void addConfig(String configString) {
        if (!config.contains(configString)) {
            this.config.add(configString);
        }
    }

    public void removeConfig(String configString) {
        config.remove(configString);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Gametype) {
            Gametype gt = (Gametype) o;
            return gt.name.equals(this.name);
        }
        return false;
    }

    public void loadGameConfig() {
        // Read file gametype.cfg
        File[] contents = CONFIGS_DIRECTORY.listFiles();
        boolean config_found = false;

        try {
            for (File f : contents) {
                String configName = this.getName();
                if (configName.startsWith("SCRIM")) {
                    configName = configName.split(" ")[1];
                }
                if (f.getName().contentEquals(configName + ".cfg")) {

                    BufferedReader br = new BufferedReader(new FileReader(CONFIGS_DIRECTORY.getPath() + "/" + f.getName()));

                    String line = br.readLine();
                    while (line != null) {
                        // Avoid commentary and empty lines
                        if (line.isEmpty() || line.charAt(0) == '/' && line.charAt(1) == '/') {
                            if (!line.contains("rconpassword")) {
                                line = br.readLine();
                            }
                            continue;
                        }

                        String ParameterWithoutCommentary = StringUtils.substringBefore(line, "//");
                        ParameterWithoutCommentary = StringUtils.normalizeSpace(ParameterWithoutCommentary);

                        this.addConfig(ParameterWithoutCommentary);

                        line = br.readLine();
                    }

                    br.close();

                    config_found = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error while openning gametype config file : " + this.getName(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading gametype config file : " + this.getName(), e);
        }

        if (!config_found) {
            LOGGER.log(Level.SEVERE, "Configuration file not found for gametype : " + this.getName());
        }

    }

    public boolean isTeamGamemode() {
        return this.getName().equalsIgnoreCase("SCRIM TS") || this.getName().equalsIgnoreCase("SCRIM CTF") || this.getName().equalsIgnoreCase("2v2");
    }

}
