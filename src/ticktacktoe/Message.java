/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ticktacktoe;

import java.io.Serializable;

/**
 *
 * @author AZYSS
 */
public class Message implements Serializable{
    //private int messageType ;
    private int move ;
    private int messageCommand ;
    private String messageBody ;
    
    //constants declaration
    static final int MOVE_COMMAND = 0 ;
    static final int ACCEPT_CHALLENGE_COMMAND = 1 ;
    static final int REFUSE_CHALLENGE_COMMAND = 2 ;
    static final int MESSAGE_COMMAND = 3 ;
    static final int CHALLENGE_COMMAND = 4 ;
    static final int PLAY_AGAIN_ACCEPTED_COMMAND = 5 ;
    static final int PLAY_AGAIN_REFUSED_COMMAND = 6 ;
    
    public Message (int mCmd, int mve, String msgBody) {
        //this.messageType = msgType ;
        this.move = mve ;
        this.messageCommand = mCmd ;
        this.messageBody = msgBody ;
    }
    
    /*int getMessagegType(){
        return this.messageType ;
    }*/
    
    int getMove(){
        return this.move ;
    }
    
    int getMesageCommand(){
        return this.messageCommand ;
    }
    
    String getMessageBody(){
        return this.messageBody ;
    }
    
}
