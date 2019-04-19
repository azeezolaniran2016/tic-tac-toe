/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ticktacktoe;

import java.util.ArrayList;

/**
 *
 * @author AZYSS
 */
public class StaticMethods {
    
    /**
     * Method to calculate the lowest of two integers
     * @param v1 integer value 1
     * @param v2 integer value 2
     * @return lowest between v1 and v2
     */
    public static int getLowestInt(int v1, int v2){
        int lowest ;
        lowest = v1 ;
        if(v1 > v2)
            lowest = v2 ;
        //return the lowest value
        return lowest ;
    }
    
    /**
     * Method to test three successful plays and determine if they are a win or not
     * @param list
     * @return true to indicate a win or false
     */
    public static boolean isWin(ArrayList<Integer> list){
        
        if(list.contains(0) && list.contains(1) && list.contains(2)){
            return true ;
        }
        if(list.contains(3) && list.contains(4) && list.contains(5)){
            return true ;
        }
        if(list.contains(6) && list.contains(7) && list.contains(8)){
            return true ;
        }
        if(list.contains(0) && list.contains(3) && list.contains(6)){
            return true ;
        }
        if(list.contains(1) && list.contains(4) && list.contains(7)){
            return true ;
        }
        if(list.contains(2) && list.contains(5) && list.contains(8)){
            return true ;
        }
        if(list.contains(2) && list.contains(4) && list.contains(6)){
            return true ;
        }
        if(list.contains(0) && list.contains(4) && list.contains(8)){
            return true ;
        }
        
        return false ;
    }
}
