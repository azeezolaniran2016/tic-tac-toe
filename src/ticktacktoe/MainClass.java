/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ticktacktoe;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author AZYSS
 */
public class MainClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            //
        }
            MainFrame ttt = new MainFrame();
            Toolkit tk = Toolkit.getDefaultToolkit() ;
            int scrnWidth = tk.getScreenSize().width ;
            int scrnHeight = tk.getScreenSize().height ;
            int locX = (int)( (scrnWidth/2) - (ttt.getWidth()/2) )  ;
            int locY = (int)( (scrnHeight/2) - (ttt.getHeight()/2) )  ;
            ttt.setLocation(locX, locY);
            ttt.setVisible(true);
    }
    
    
    
    
    public static void playNotification(final String path){
        if(!MainFrame.soundsOn)
            return ;
        try{
        //Runnable run = new Runnable(){
            URL url = MainClass.class.getResource(path);
            AudioClip clip = Applet.newAudioClip(url);
           // @Override
           // public void run() {
                clip.play();
           // }
        //};
        //Thread t = new Thread(run);
        //t.start();
        }catch(Exception ex){
            System.out.println("Exception while playing clip");
        }
    }
    
}
