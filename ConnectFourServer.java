import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;



public class ConnectFourServer {

   
    public static void main(String[] args) throws Exception {
        int port = 8901 , backlog = 5;
        String ip = "192.168.56.1";
        ServerSocket listener = new ServerSocket(port);

        System.out.println("Connect Four Server is Running");
        System.out.println("Adress: "+listener.getInetAddress());
        System.out.println("port: "+listener.getLocalSocketAddress());
        try {
            while (true) {
                Game game = new Game();
                Game.Player playerX = game.new Player(listener.accept(), "RED");
                Game.Player playerO = game.new Player(listener.accept(), "BLUE");
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerX);
                game.currentPlayer = playerX;
                playerX.start();
                playerO.start();
            }
        } finally {
            listener.close();
        }
    }
}

class Game {

    
    private Player[] board = {
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null,
        null, null, null, null, null, null,null, null};

    
    Player currentPlayer;

    
    public boolean isWinner() {
         // horizontalCheck 
        for (int j = 0 ; j< 9-4 ; j++){//column
            for (int i = 0 ; i < 48 ; i+=8){//row
                if (    board[i + j]!= null && board[i +j]== board[i +j+1] && board[i +j] == board[i+j+2] && board[i +j] ==  board[i+j+3]){
                return true;
                }
            }
        }
        
        for (int i = 0 ; i< 24 ; i+=8){
            for (int j = 0 ; j < 9 ; j++){
                if (    board[i  + j]!= null && board[i +j]== board[i+8 +j] && board[i +j] == board[i+(16) +j] && board[i +j] ==  board[i+(24) +j]){
                return true;
                }
            }
        }
     
        for (int i = 24 ; i< 48 ; i+=8){
            for (int j = 0 ; j <4 ; j++){
                if (    board[i + j]!= null && board[i +j]== board[(i-8) +j+1] && board[(i-8) +j+1] == board[i-16 +j+2] && board[(i-16) +j+2] ==  board[(i-24) +j+3]){
                return true;
                }
            }
        }
        
        for (int i = 24 ; i< 48 ; i+=8){
            for (int j = 3 ; j < 8; j++){
                if (    board[i  + j]!= null && board[i +j]== board[(i-8) +j-1 ] && board[(i-8) +j-1 ] == board[(i-16) +j-2] && board[(i-16) +j-2] ==  board[(i-24) +j-3]){
                return true;
                }
            }
        }
        
        return false;
    }

    
    public boolean boardFilledUp() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        return true;
    }

    
    public synchronized int legalMove(int location, Player player) {
        int minlocation = (location % 8)+8*5;
        for(int i = minlocation ; i >= location ; i-= 8)
            if (player == currentPlayer && board[i] == null) {
                board[i] = currentPlayer;
                currentPlayer = currentPlayer.opponent;
                currentPlayer.otherPlayerMoved(i);
                return i;
            }
        return -1;
    }

    
    class Player extends Thread {
        String mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

        
        public Player(Socket socket, String mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE Waiting for opponent to connect");
            } catch (Exception e) {
                System.out.println("Player died: " + e);
            }
        }

        
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        
        public void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
            output.println(
                isWinner() ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
        }

        
        public void run() {
            try {
                
                output.println("MESSAGE All players connected");

               
                if (mark.equals("RED")) {
                    output.println("MESSAGE Your move");
                }

              
                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        int validlocation = legalMove(location, this);
                        if (validlocation!= -1) {
                            output.println("VALID_MOVE"+validlocation);
                            output.println(isWinner() ? "VICTORY"
                                         : boardFilledUp() ? "TIE"
                                         : "");
                        } else {
                            output.println("MESSAGE ?");
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("Player died: " + e);
            } finally {
                try {socket.close();} catch (Exception e) {}
            }
        }
    }
}