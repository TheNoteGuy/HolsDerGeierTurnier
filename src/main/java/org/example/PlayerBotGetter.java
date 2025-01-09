package org.example;

import lombok.Getter;
import org.example.framework.HolsDerGeierSpieler;
import java.io.*;
import java.net.*;
import java.util.*;

public class PlayerBotGetter {
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String BOTS_BASE_PATH = "src/main/java/org/example/bots/round";  // Changed to include "round"
    private static final String TEMP_COMPILE_DIR = "temp_compile";
    private static final Random RANDOM = new Random();

    @Getter private HolsDerGeierSpieler spieler1;
    @Getter private HolsDerGeierSpieler spieler2;
    @Getter private int round = 1;

    private final URLClassLoader classLoader;

    public PlayerBotGetter() {
        try {
            File compileDir = new File(PROJECT_ROOT, TEMP_COMPILE_DIR);
            compileDir.mkdirs();
            classLoader = new URLClassLoader(
                    new URL[]{compileDir.toURI().toURL()},
                    this.getClass().getClassLoader()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize class loader", e);
        }
    }

    private List<Class<? extends HolsDerGeierSpieler>> findValidBotsInRound(int roundNum) {
        List<Class<? extends HolsDerGeierSpieler>> result = new ArrayList<>();
        String roundPath = PROJECT_ROOT + "/" + BOTS_BASE_PATH + roundNum;  // Now forms "bots/round1" etc.
        File roundDir = new File(roundPath);

        System.out.println("Scanning directory: " + roundPath);

        if (!roundDir.exists() || !roundDir.isDirectory()) {
            System.out.println("Directory not found: " + roundPath);
            return result;
        }

        File[] javaFiles = roundDir.listFiles((dir, name) -> name.endsWith(".java"));
        if (javaFiles == null) {
            System.out.println("No files found in: " + roundPath);
            return result;
        }

        String basePackage = "org.example.bots.round" + roundNum + ".";
        for (File file : javaFiles) {
            try {
                String className = file.getName().replace(".java", "");
                String fullClassName = basePackage + className;

                Class<?> loadedClass = classLoader.loadClass(fullClassName);

                if (HolsDerGeierSpieler.class.isAssignableFrom(loadedClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends HolsDerGeierSpieler> botClass =
                            (Class<? extends HolsDerGeierSpieler>) loadedClass;
                    result.add(botClass);
                }
            } catch (Exception e) {
                System.out.println("Could not load class from file: " + file.getName());
            }
        }

        System.out.println("Found " + result.size() + " bots in round " + roundNum);
        return result;
    }

    public void getter() {
        spieler1 = null;
        spieler2 = null;

        for (int r = 1; r <= 10; r++) {
            List<Class<? extends HolsDerGeierSpieler>> validBots = findValidBotsInRound(r);
            if (validBots.size() >= 2) {
                round = r;
                selectBotsFromList(validBots);
                return;
            }
        }
        throw new IllegalStateException("Not enough valid bots found in any round");
    }

    private void selectBotsFromList(List<Class<? extends HolsDerGeierSpieler>> botClasses) {
        try {
            int size = botClasses.size();
            int index1 = RANDOM.nextInt(size);
            int index2;
            do {
                index2 = RANDOM.nextInt(size);
            } while (index1 == index2);

            spieler1 = botClasses.get(index1).getDeclaredConstructor().newInstance();
            spieler2 = botClasses.get(index2).getDeclaredConstructor().newInstance();

            System.out.println("Selected from round " + round + ":");
            System.out.println("Player 1: " + spieler1.getClass().getSimpleName());
            System.out.println("Player 2: " + spieler2.getClass().getSimpleName());

        } catch (Exception e) {
            throw new RuntimeException("Bot creation failed: " + e.getMessage());
        }
    }

    public boolean isCurrentRoundEmpty() {
        return findValidBotsInRound(round).size() < 2;
    }

    public boolean isTournamentComplete() {
        for (int r = 10; r > 0; r--) {
            int botsCount = findValidBotsInRound(r).size();
            if (botsCount > 0) {
                return botsCount == 1;
            }
        }
        return true;
    }

    public void forceRescan() {
        spieler1 = null;
        spieler2 = null;
        round = 1;


        System.out.println("Forcing rescan of bot directories...");
        getter();
    }
}