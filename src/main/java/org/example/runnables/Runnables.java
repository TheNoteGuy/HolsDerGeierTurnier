package org.example.runnables;

import lombok.Setter;
import org.example.Main;
import org.example.framework.GeierInfo;
import org.example.framework.HolsDerGeier;

import javax.swing.*;

public class Runnables {
    @Setter
    private HolsDerGeier hdg = Main.hdg;

    private final Runnable addPlayer1 = () -> hdg.addPlayer1(hdg.getPlayerBotGetter().getSpieler1());
    private final Runnable addPlayer2 = () -> hdg.addPlayer2(hdg.getPlayerBotGetter().getSpieler2());
    private final Runnable hardReset = () -> {
        hdg.fullReset();
        hdg.getPlayerBotGetter().forceRescan();
    };
    private final Runnable runGame = this::executeGame;

    public Runnable getAddPlayer1() { return addPlayer1; }
    public Runnable getAddPlayer2() { return addPlayer2; }
    public Runnable getHardReset() { return hardReset; }
    public Runnable getRunGame() { return runGame; }

    private void executeGame() {
        HolsDerGeier.timeStart1 = System.currentTimeMillis();
        int numberOfGames;

        try {
            numberOfGames = Integer.parseInt(Main.numberOfGames.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number of games.");
            return;
        }

        new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (HolsDerGeier.players[0] == null || HolsDerGeier.players[1] == null) {
                        String message = (Math.random() < 0.01) ?
                                "MAYBE YOU SHOULD ADD SOME PLAYERS FIRST BEFORE ASKING ME TO START A GAME YOU DUMBASS!" :
                                "Please add players first";
                        JOptionPane.showMessageDialog(null, message);
                        return null;
                    }

                    hdg.newGame(numberOfGames);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            protected void done() {
                long duration = System.currentTimeMillis() - HolsDerGeier.timeStart1;
                String result = String.format("Game over! Score: %s %s vs %s %s Time: %dms",
                        HolsDerGeier.players[0].getClass().getAnnotation(GeierInfo.class).name(),
                        Main.counter1.getText(),
                        HolsDerGeier.players[1].getClass().getAnnotation(GeierInfo.class).name(),
                        Main.counter2.getText(),
                        duration);

                JOptionPane.showMessageDialog(null, result);
                //System.out.println(Main.getGameOutcomes());
                System.out.println(result);
                Main.getGameOutcomes().clear();
            }
        }.execute();
    }
}