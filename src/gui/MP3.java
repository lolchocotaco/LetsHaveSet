package gui;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import javazoom.jl.player.Player;


public class MP3 {
    private String filename;
    private Player player; 
    public boolean isPlaying;
    private final  String audioPath [] = {"sounds/Buzzer.mp3", "sounds/Ding.mp3", "sounds/lobbyMusic.mp3"};
    
    // constructor that takes the name of an MP3 file
    public MP3(int type){
    	try{
         FileInputStream fis     = new FileInputStream(audioPath[type]);
         BufferedInputStream bis = new BufferedInputStream(fis);
         player = new Player(bis);
         isPlaying= false;
         }catch (Exception e) {
             System.out.println("Problem playing file " + filename);
         }
   }

    public void close() { if (player != null) player.close(); }

    // play the MP3 file to the sound card
    public void play() {
        // run in new thread to play in background
    	isPlaying = true;
        new Thread() {
            public void run() {
                try { player.play(); player.close(); isPlaying = false;}
                catch (Exception e) {}
            }
        }.start();
    }
    
    
    
    public void loopPlay() {
    	isPlaying = true;
        // run in new thread to play in background
        new Thread() {
            public void run() {
                try { 
                	while(true){player.play(); }
                } catch (Exception e) {}
            }
        }.start();
    }
}
