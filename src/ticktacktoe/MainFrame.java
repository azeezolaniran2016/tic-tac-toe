/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ticktacktoe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author AZYSS
 */
public class MainFrame extends javax.swing.JFrame {
    private ArrayList<PlayView> playViewsList ;
    private BoardPanel boardPanel ;
    private JPopupMenu playerSelectionPopup ;
    private JLabel exibitionLabel ;
    private ArrayList<Integer> playerOneMoveList;
    private ArrayList<Integer> playerTwoMoveList ;
    private String playerOne ;
    private String playerTwo ;
    private int playCount ;
    private MouseClickAction mouseClickAction;
    private boolean onePlayer ;
    private boolean twoPlayer ;
    private boolean onlinePlay ;
    private boolean playerOneTurn ;
    //private int playerOneWins ;
    //private int playerTwoWins ;
    //private boolean draw ;
    private StatsBoard statsBoard ;
    private JPopupMenu statsBoardPopup ;
    protected static boolean soundsOn = true  ;
    private boolean onlineGameInitiator ;
    //thread stuff.
    private static ExecutorService executorService ;
    private final static int WIN = 1 ;
    private final static int DRAW = 0 ;
    private final static int NULL = 2 ;
    
    private static final String WELCOME_TEXT = "TIC-TAC-TOE "
            + "Designed and Implemented by OlawaleAzyss.." ;
    
    private static ServerSocket serverSocket ;
    private static Socket socket ;
    private static ObjectOutputStream outputStream ;
    private static ObjectInputStream inputStream ;
    private final static int LISTEN_PORT = 6008 ;
    
    
    
    /**
     * 
     */
    public MainFrame(){
        initComponents();
        ImageIcon iIcon = new ImageIcon(MainFrame.class.getResource("/res/pngs/frameIcon.png"));// load the frame icon
        this.setIconImage(iIcon.getImage());// set the frame icon
        this.statsBoard = new StatsBoard();
        this.statsBoardPopup = new JPopupMenu();
        this.statsBoardPopup.add(this.statsBoard);
        
        executorService = Executors.newCachedThreadPool() ;
        //playerOneTurn = true ;
        this.mouseClickAction = new MouseClickAction();
        //this.playCount = 0;
        //this.playerOne = "";
        //this.playerTwo = "" ;
        this.playerOneMoveList = new ArrayList();
        this.playerTwoMoveList = new ArrayList();
        //this.reloadPlayersMoves();
        
        
        this.playerSelectionPopup = new JPopupMenu();
        this.playerSelectionPopup.addPopupMenuListener(new PopupMenuListener(){

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                //System.out.println("Component shown"); 
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                //System.out.println("popupMenuBecoming invisible"); 
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                MainFrame.this.startOnePlayerGameButton.setEnabled(false);
                MainFrame.this.gameTypeButtonGroup.clearSelection();
            }
        });
        
        this.onlineOptionsPopupMenu.addPopupMenuListener(new PopupMenuListener(){

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                //System.out.println("Component shown"); 
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                //System.out.println("popupMenuBecoming invisible"); 
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                //MainFrame.this.startOnePlayerGameButton.setEnabled(false);
                MainFrame.this.gameTypeButtonGroup.clearSelection();
            }
        });
        //initialize the array with playviews
        /*
        this.playViewsArray = new PlayView[]{new PlayView(0),new PlayView(1), new PlayView(2)
                , new PlayView(3), new PlayView(4), new PlayView(5)
                , new PlayView(6), new PlayView(7), new PlayView(8)};*/
        //this.loadPlayViewsList();
        
        
        //add actionListener to the playViews
        //this.addMouseListeners();
        
        this.exibitionLabel = new JLabel(new ImageIcon(this.getClass().getResource("/res/gifs/tictac.gif")));
        //this.add(this.exibitionLabel, BorderLayout.CENTER);
        refreshGameView();
        this.pack();
        
        //start listening for challenges
        this.startListeningForChallenges();
    }
    
    private void refreshGameView(){
        if(this.onlinePlay){
            this.disconnectOnlinePlay();// disconnect online play
        }
        this.onlineGameInitiator = false ;
        onlineX_SelectionRadioButton.setEnabled(true);// enable one playerX seletion button
        onlineO_SelectionRadioButton.setEnabled(true);// enable one player O selection button
        this.playViewsList = new ArrayList(9);
        this.loadPlayViewsList(); // load playViews list
        this.removeMouseListeners(); //remove necessary mouse listeners
        this.addMouseListeners(); // add mouse listeners
        this.gameTypeButtonGroup.clearSelection(); //
        this.playerOneMoveList.clear(); // clear player one move list
        this.playerTwoMoveList.clear(); // clear player two move list
        this.playerSelectionButtonGroup.clearSelection(); // clear game type seletion
        this.startOnePlayerGameButton.setEnabled(false); // enable start one player game
        this.playCount = 0 ;//reset the play count
        this.onePlayerRadioButton.setForeground(Color.black);
        this.twoPlayerRadioButton.setForeground(Color.BLACK);
        this.onlinePlayerRadioButton.setForeground(Color.black);
        this.playerOne = "";
        this.playerTwo = "" ;
        this.onePlayer = false ;
        //this.draw = false ;
        this.twoPlayer = false ;
        this.playerOneTurn = false ;
        try{
            this.remove(this.boardPanel);
        }catch(NullPointerException npe){
        }
        this.add(this.exibitionLabel, BorderLayout.CENTER);
        this.feedBackTextArea.setText(MainFrame.WELCOME_TEXT);
        this.lockGameOptions(false);
        this.reloadPlayersMoves();
        //this.add(this.boardPanel = new BoardPanel(this.playViewsList));
        //this.onePlayer = true ;
        // online game stuff
        this.onlineGameOptionsButtonGroup.clearSelection();
        this.connectOnlineButton.setEnabled(false);
        this.onlinePlayLoadingConnectionLabel.setEnabled(false);
        this.onlineConnectionStatusLabel.setText("");
        this.onlinePlay = false ;
        
        this.repaint();
        //this.revalidate() ;
    }
    
    private void startListeningForChallenges(){
        executorService.execute(new Runnable(){

            @Override
            public void run() {
                //System.out.println("Listening for connections");
                try {
                    serverSocket = new ServerSocket(LISTEN_PORT, 0) ;
                } catch (IOException ex) {
                    System.out.println("IOException has been thrown in initializing in serverSocket") ;
                }
                //int cR = 0;
                while(true){// continue listening for a connection infinitely
                    try{
                        socket = serverSocket.accept(); // accept a connection
                        //System.out.println("Connections received count = " + (++cR)) ;
                        outputStream = new ObjectOutputStream(socket.getOutputStream()); // get the outputStream
                        outputStream.flush(); // flush object output stream ;
                        inputStream = new ObjectInputStream(socket.getInputStream()); // get the input stram
                       
                        do{
                            try{
                                Message msg = (Message)inputStream.readObject(); // recieve the challenge message
                                handleMessage(msg);// handle the challenge message
                            }catch(ClassNotFoundException ex){
                                System.out.println("Class not found ex thrown in startListeningForChallenges()");
                            }
                        }while(onlinePlay == true);//continue playing game or keep connection on
                        //System.out.println("Online Play loop Ended");
                        JOptionPane.showMessageDialog(MainFrame.this, "Online Refused/Disconnected.", "", JOptionPane.INFORMATION_MESSAGE);
                        refreshGameView();
                        
                    } catch (IOException ex) {
                        System.out.println("IOException thrown in startListeningForChallenges()\n" + ex.getMessage());
                        JOptionPane.showMessageDialog(MainFrame.this, "Connection to opponent lost", "Oops", JOptionPane.ERROR_MESSAGE);
                        refreshGameView();
                    }
                }//end of outer while ; 
            } 
        });
        
    }
    
    
    private void handleMessage(Message msg){
        switch(msg.getMesageCommand()){// requested to play a challenge
            case Message.CHALLENGE_COMMAND:{// handle a received challenge command
                int resp = JOptionPane.showConfirmDialog(this,
                        "Online Challenge request received."
                        , "Online Challenge Request",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); // prompt user to respond to a challenge request
                
                if(resp == JOptionPane.YES_OPTION){// user responds yes
                    try {
                        if(msg.getMessageBody().equalsIgnoreCase("X")){// set player one and two characters
                            playerTwo = "X";
                            playerOne = "O";
                        }else{
                            playerTwo = "O";
                            playerOne = "X";
                        }
                        //System.out.println("Player One = " + playerOne +
                        //        "\nPlayer Two = " + playerTwo + " For challenge acceptor");       
                        
                        outputStream.writeObject(new Message(Message.ACCEPT_CHALLENGE_COMMAND, -1, ""));
                        outputStream.flush();
                        feedBackTextArea.setText(feedBackTextArea.getText() + "\nOnline Challenge Accepted."
                                + "\n\nYou are player " + playerOne);
                        feedBackTextArea.setCaretPosition(feedBackTextArea.getText().length());
                        lockGameOptions(true);
                        this.onlineGameInitiator = false ;
                        onlinePlay = true ;
                        onlinePlayerRadioButton.setSelected(true);
                        this.remove(this.exibitionLabel);
                        this.add(this.boardPanel = new BoardPanel(this.playViewsList));
                        playerOneTurn = false ;
                        this.repaint();
                        this.revalidate();
                        //onlinePlayerRadioButton.setSelected(true);
                        //onlinePlayerRadioButton.setForeground(Color.BLUE);
                        //System.out.println("Accept challenge message written");
                    } catch (IOException ex) {
                        System.out.println("IOException while trying to write accept challenge message");
                    }
                }else{
                    Message rMsg = new Message(Message.REFUSE_CHALLENGE_COMMAND, -1, "") ;
                    try {
                        outputStream.writeObject(rMsg);// write challenge refused message
                        outputStream.flush();// flush output stream
                    } catch (IOException ex) {
                        System.out.println("IOException when trying to write refuse challenge message");
                    }
                }
                break ;
            }
            case Message.ACCEPT_CHALLENGE_COMMAND:{// opponent accepted the challenge
                lockGameOptions(true);
                this.onlineGameInitiator = true ;
                //System.out.println("Player One = " + playerOne +
                //                "\nPlayer Two = " + playerTwo + " for challenge requester.");
                onlinePlay = true ;
                onlinePlayerRadioButton.setSelected(true);
                feedBackTextArea.setText(feedBackTextArea.getText() + "\nOnline Challenge Request Accepted."
                        + "\n\nYou are player " + playerOne + ".");
                feedBackTextArea.setCaretPosition(feedBackTextArea.getText().length());
                this.remove(this.exibitionLabel);
                this.add(this.boardPanel = new BoardPanel(this.playViewsList));
                playerOneTurn = true ;
                this.repaint();
                this.revalidate();
                //lockGameOptions(true);
                //onlinePlayerRadioButton.setSelected(true);
                //onlinePlayerRadioButton.setForeground(Color.BLUE);
                //System.out.println("Accept challenge command recieved");
                break ;
            }
            case Message.REFUSE_CHALLENGE_COMMAND:{// handle online play response
                onlinePlay = false ;
                feedBackTextArea.setText(feedBackTextArea.getText() + "\nOnline Challenge Request Refused.");
                feedBackTextArea.setCaretPosition(feedBackTextArea.getText().length());
                //System.out.println("Refuse challenge command recieved");
                break ;
            }
            case Message.MOVE_COMMAND:{
                int move = msg.getMove() ;
                //System.out.println("Move Location Received = " + move);
                this.onlineOpponentPlay(move);
                playerOneTurn = true ;
                //System.out.println("Move command recieved");
                break ;
            }
            case(Message.PLAY_AGAIN_ACCEPTED_COMMAND):{
                //System.out.println("Play_Again command recieved");
                this.playOnlineGameAgain();
                break ;
            }
            case(Message.PLAY_AGAIN_REFUSED_COMMAND):{
                //System.out.println("Play again refused command received");
                break ;
            }
            case Message.MESSAGE_COMMAND:{
                System.out.println("Message command recieved");
                break ;
            }
        }
    }
    
    private void sendChallengeRequest(final String cpuName, final int lPort){
        executorService.execute(new Runnable(){

            @Override
            public void run() {
                try {
                    connectOnlineButton.setEnabled(false); // disable connectbutton
                    socket = new Socket(cpuName, lPort);//initialize the socket
                    //feedBackTextArea.setText("Connection established to " + socket.getInetAddress().getHostName());
                    outputStream = new ObjectOutputStream(socket.getOutputStream()); // get the output stream
                    outputStream.flush();// flush object output stream
                    inputStream = new ObjectInputStream(socket.getInputStream()); // get the input stream
                    //do all necessary initializations
                    Message msg = new Message(Message.CHALLENGE_COMMAND, -1, playerOne); //create challenge message and playerOne character
                    outputStream.writeObject(msg);//send challenge message
                    outputStream.flush();// flush output stream
                    feedBackTextArea.setText(feedBackTextArea.getText() + "\nWaiting for opponent to accept challenge.");
                    try {
                        //msg = (Message)inputStream.readObject();// receive challenge request response message
                        //handleMessage(msg);// handle the message
                        do{//check the first message which is a request response
                            try{
                                msg = (Message)inputStream.readObject();
                                handleMessage(msg);
                            }catch (IOException ex){
                                System.out.println("IOException thrown in handleMessage() call in "
                                        + "sendChallengeRequest | " + ex.getMessage());
                                onlinePlay = false;
                            }
                        }while(onlinePlay == true);//keep handling message as long as online play == true
                        // online play ended or refused
                    } catch (ClassNotFoundException ex) {
                        System.out.println("ClassNotFoundException occured in sendChallengeRequest()");
                    }
                    //System.out.println("Send challenge request loop ended");
                    JOptionPane.showMessageDialog(MainFrame.this, "Connection lost.", "Connection Error"
                            , JOptionPane.ERROR_MESSAGE);
                    refreshGameView();
                    
                } catch (IOException ex) {
                    System.out.println("IOException in sendChallengeRequest() \n " + ex.getMessage());
                    refreshGameView(); 
                    //connectOnlineButton.setEnabled(true);
                    //onlinePlayLoadingConnectionLabel.setEnabled(false);
                    //onlineConnectionStatusLabel.setForeground(Color.red);
                    //onlineX_SelectionRadioButton.setEnabled(true);
                    //onlineO_SelectionRadioButton.setEnabled(true);
                    //onlineConnectionStatusLabel.setText("Failed...");
                    JOptionPane.showMessageDialog(MainFrame.this, "Connection Failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    private void writePlayerMoveToStream(int move){
        try {
            outputStream.writeObject(new Message(Message.MOVE_COMMAND, move, ""));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not find network connection", "Connection Error", JOptionPane.ERROR_MESSAGE);
            writePlayerMoveToStream(move);
        }
    }
    
    private void loadPlayViewsList(){
        this.playViewsList.clear();
        for(int i = 0; i < 9; i++)
            this.playViewsList.add(new PlayView(i));
    }
    
    private void disconnectOnlinePlay(){
        try {
            this.onlinePlay = false ;
            this.connectOnlineButton.setEnabled(false);
            socket.close();
            onlineConnectionStatusLabel.setText("");
            onlinePlayLoadingConnectionLabel.setEnabled(false);
            //System.out.println("disconnected online play");
        } catch (IOException ex) {
            System.out.println("IOException in disconnectOnlinePlay " + ex.getMessage());
        }
    }
    
    private void reloadPlayersMoves(){
        //load all possible computer moves
        this.playerOneMoveList.clear();
        this.playerTwoMoveList.clear();
    }
    
    private void removeMouseListeners(){
        for(int i = 0; i < this.playViewsList.size(); i++){
            this.playViewsList.get(i).removeMouseListener(this.mouseClickAction);
        }
    }
    
    private void addMouseListeners(){
        for(int i = 0; i < this.playViewsList.size(); i++){
            this.playViewsList.get(i).addMouseListener(this.mouseClickAction);
        }
    }
    
    //method to lock the selection panel
    private void lockGameOptions(final boolean lock){
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                if(lock){
                onePlayerRadioButton.setEnabled(!lock);
                twoPlayerRadioButton.setEnabled(!lock);
                onlinePlayerRadioButton.setEnabled(!lock);
            }else{
                onePlayerRadioButton.setEnabled(!lock);
                twoPlayerRadioButton.setEnabled(!lock);
                onlinePlayerRadioButton.setEnabled(!lock);
        }}
        });
        
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gameTypeButtonGroup = new javax.swing.ButtonGroup();
        playerSelectionButtonGroup = new javax.swing.ButtonGroup();
        playerSelectionPanel = new javax.swing.JPanel();
        oSelectionRadioButton = new javax.swing.JRadioButton();
        xSelectionRadioButton = new javax.swing.JRadioButton();
        startOnePlayerGameButton = new javax.swing.JButton();
        aboutLabel = new javax.swing.JLabel();
        aboutPopup = new javax.swing.JPopupMenu();
        hintsLabel = new javax.swing.JLabel();
        onlinePlayOptionsPanel = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        connectOnlineButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cpuNameTextField = new javax.swing.JTextField();
        cpuPortTextField = new javax.swing.JTextField();
        onlinePlayLoadingConnectionLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        onlineX_SelectionRadioButton = new javax.swing.JRadioButton();
        onlineO_SelectionRadioButton = new javax.swing.JRadioButton();
        jSeparator4 = new javax.swing.JSeparator();
        onlineConnectionStatusLabel = new javax.swing.JLabel();
        onlineGameOptionsButtonGroup = new javax.swing.ButtonGroup();
        onlineOptionsPopupMenu = new javax.swing.JPopupMenu();
        feedBackPanel = new javax.swing.JPanel();
        gameTypeSelectionPanel = new javax.swing.JPanel();
        onePlayerRadioButton = new javax.swing.JRadioButton();
        twoPlayerRadioButton = new javax.swing.JRadioButton();
        onlinePlayerRadioButton = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        feedBackTextArea = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        helpMenu = new javax.swing.JMenu();
        scoreBoardMenu = new javax.swing.JMenuItem();
        hintsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        refreshMenuItem = new javax.swing.JMenuItem();
        connectionMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();

        gameTypeButtonGroup.add(this.onePlayerRadioButton);
        gameTypeButtonGroup.add(this.twoPlayerRadioButton);
        gameTypeButtonGroup.add(this.onlinePlayerRadioButton);

        this.playerSelectionButtonGroup.add(this.xSelectionRadioButton);this.playerSelectionButtonGroup.add(this.oSelectionRadioButton);

        playerSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "One Player ", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tempus Sans ITC", 0, 10), new java.awt.Color(0, 102, 102))); // NOI18N

        oSelectionRadioButton.setFont(new java.awt.Font("Tempus Sans ITC", 1, 24)); // NOI18N
        oSelectionRadioButton.setForeground(new java.awt.Color(0, 102, 102));
        oSelectionRadioButton.setText("O");
        oSelectionRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                oSelectionRadioButtonStateChanged(evt);
            }
        });

        xSelectionRadioButton.setFont(new java.awt.Font("Tempus Sans ITC", 1, 24)); // NOI18N
        xSelectionRadioButton.setForeground(new java.awt.Color(153, 0, 153));
        xSelectionRadioButton.setText("X");
        xSelectionRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                xSelectionRadioButtonStateChanged(evt);
            }
        });

        startOnePlayerGameButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        startOnePlayerGameButton.setText("START");
        startOnePlayerGameButton.setEnabled(false);
        startOnePlayerGameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startOnePlayerGameButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout playerSelectionPanelLayout = new javax.swing.GroupLayout(playerSelectionPanel);
        playerSelectionPanel.setLayout(playerSelectionPanelLayout);
        playerSelectionPanelLayout.setHorizontalGroup(
            playerSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(startOnePlayerGameButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(playerSelectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(playerSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xSelectionRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(oSelectionRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        playerSelectionPanelLayout.setVerticalGroup(
            playerSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(playerSelectionPanelLayout.createSequentialGroup()
                .addComponent(oSelectionRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xSelectionRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startOnePlayerGameButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        aboutLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/aboutTicTacToe.png"))); // NOI18N

        hintsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gifs/tttTip.gif"))); // NOI18N

        onlinePlayOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Online Game Settings", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tempus Sans ITC", 0, 10), new java.awt.Color(0, 102, 102))); // NOI18N
        onlinePlayOptionsPanel.setOpaque(false);

        connectOnlineButton.setText("Connect");
        connectOnlineButton.setEnabled(false);
        connectOnlineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectOnlineButtonActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("CPU Name : ");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("Port : ");

        cpuNameTextField.setForeground(new java.awt.Color(0, 0, 153));

        cpuPortTextField.setForeground(new java.awt.Color(0, 0, 153));
        cpuPortTextField.setText(String.valueOf(LISTEN_PORT));

        onlinePlayLoadingConnectionLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        onlinePlayLoadingConnectionLabel.setForeground(new java.awt.Color(0, 102, 102));
        onlinePlayLoadingConnectionLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gifs/spinner-20x20.gif"))); // NOI18N
        onlinePlayLoadingConnectionLabel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gifs/disabledIcon20x20.png"))); // NOI18N
        onlinePlayLoadingConnectionLabel.setEnabled(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select Player", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tempus Sans ITC", 0, 10), new java.awt.Color(0, 102, 102))); // NOI18N
        jPanel2.setOpaque(false);

        onlineX_SelectionRadioButton.setFont(new java.awt.Font("Tempus Sans ITC", 0, 14)); // NOI18N
        onlineX_SelectionRadioButton.setText("Player X");
        onlineX_SelectionRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                onlineX_SelectionRadioButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                onlineX_SelectionRadioButtonMouseExited(evt);
            }
        });
        onlineX_SelectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlineX_SelectionRadioButtonActionPerformed(evt);
            }
        });

        onlineO_SelectionRadioButton.setFont(new java.awt.Font("Tempus Sans ITC", 0, 14)); // NOI18N
        onlineO_SelectionRadioButton.setText("Player O");
        onlineO_SelectionRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                onlineO_SelectionRadioButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                onlineO_SelectionRadioButtonMouseExited(evt);
            }
        });
        onlineO_SelectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlineO_SelectionRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(onlineO_SelectionRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(onlineX_SelectionRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(onlineX_SelectionRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlineO_SelectionRadioButton))
        );

        onlineConnectionStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        onlineConnectionStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout onlinePlayOptionsPanelLayout = new javax.swing.GroupLayout(onlinePlayOptionsPanel);
        onlinePlayOptionsPanel.setLayout(onlinePlayOptionsPanelLayout);
        onlinePlayOptionsPanelLayout.setHorizontalGroup(
            onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(onlinePlayOptionsPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(onlinePlayOptionsPanelLayout.createSequentialGroup()
                        .addGroup(onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cpuNameTextField)
                            .addComponent(cpuPortTextField)))
                    .addGroup(onlinePlayOptionsPanelLayout.createSequentialGroup()
                        .addComponent(onlinePlayLoadingConnectionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(onlineConnectionStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectOnlineButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSeparator4)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        onlinePlayOptionsPanelLayout.setVerticalGroup(
            onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, onlinePlayOptionsPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cpuNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cpuPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(onlinePlayOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(onlineConnectionStatusLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(onlinePlayLoadingConnectionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(connectOnlineButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        onlineGameOptionsButtonGroup.add(this.onlineX_SelectionRadioButton);
        onlineGameOptionsButtonGroup.add(this.onlineO_SelectionRadioButton);

        this.onlineOptionsPopupMenu.add(this.onlinePlayOptionsPanel)
;
        onlineOptionsPopupMenu.setOpaque(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Tic Tac Toe [Test Version]");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        feedBackPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        gameTypeSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11), new java.awt.Color(0, 153, 255))); // NOI18N

        onePlayerRadioButton.setFont(new java.awt.Font("Tempus Sans ITC", 1, 14)); // NOI18N
        onePlayerRadioButton.setText("1 Player ");
        onePlayerRadioButton.setToolTipText("Play against computer");
        onePlayerRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                onePlayerRadioButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                onePlayerRadioButtonMouseExited(evt);
            }
        });
        onePlayerRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                onePlayerRadioButtonItemStateChanged(evt);
            }
        });

        twoPlayerRadioButton.setFont(new java.awt.Font("Tempus Sans ITC", 1, 14)); // NOI18N
        twoPlayerRadioButton.setText("2 Players");
        twoPlayerRadioButton.setToolTipText("Two human players");
        twoPlayerRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                twoPlayerRadioButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                twoPlayerRadioButtonMouseExited(evt);
            }
        });
        twoPlayerRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoPlayerRadioButtonActionPerformed(evt);
            }
        });

        onlinePlayerRadioButton.setFont(new java.awt.Font("Tempus Sans ITC", 1, 14)); // NOI18N
        onlinePlayerRadioButton.setText("Online   ");
        onlinePlayerRadioButton.setToolTipText("Play with a remote user");
        onlinePlayerRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                onlinePlayerRadioButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                onlinePlayerRadioButtonMouseExited(evt);
            }
        });
        onlinePlayerRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                onlinePlayerRadioButtonItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout gameTypeSelectionPanelLayout = new javax.swing.GroupLayout(gameTypeSelectionPanel);
        gameTypeSelectionPanel.setLayout(gameTypeSelectionPanelLayout);
        gameTypeSelectionPanelLayout.setHorizontalGroup(
            gameTypeSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(onePlayerRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(twoPlayerRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(onlinePlayerRadioButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        gameTypeSelectionPanelLayout.setVerticalGroup(
            gameTypeSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gameTypeSelectionPanelLayout.createSequentialGroup()
                .addComponent(onePlayerRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(twoPlayerRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlinePlayerRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "::Log::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 11), new java.awt.Color(0, 153, 255))); // NOI18N

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        feedBackTextArea.setEditable(false);
        feedBackTextArea.setColumns(20);
        feedBackTextArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        feedBackTextArea.setForeground(new java.awt.Color(51, 51, 255));
        feedBackTextArea.setLineWrap(true);
        feedBackTextArea.setRows(5);
        feedBackTextArea.setText(WELCOME_TEXT);
        feedBackTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(feedBackTextArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );

        javax.swing.GroupLayout feedBackPanelLayout = new javax.swing.GroupLayout(feedBackPanel);
        feedBackPanel.setLayout(feedBackPanelLayout);
        feedBackPanelLayout.setHorizontalGroup(
            feedBackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, feedBackPanelLayout.createSequentialGroup()
                .addComponent(gameTypeSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        feedBackPanelLayout.setVerticalGroup(
            feedBackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gameTypeSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getContentPane().add(feedBackPanel, java.awt.BorderLayout.PAGE_END);

        helpMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/helpIcon.png"))); // NOI18N
        helpMenu.setText("Help");

        scoreBoardMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/scoreIcon.png"))); // NOI18N
        scoreBoardMenu.setText("Score Board");
        scoreBoardMenu.setToolTipText("View score board");
        scoreBoardMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scoreBoardMenuActionPerformed(evt);
            }
        });
        helpMenu.add(scoreBoardMenu);

        hintsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/hintIcon2.png"))); // NOI18N
        hintsMenuItem.setText("Hint");
        hintsMenuItem.setToolTipText("View game hint");
        hintsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hintsMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(hintsMenuItem);
        helpMenu.add(jSeparator1);

        aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/infoIcon.png"))); // NOI18N
        aboutMenuItem.setText("About");
        aboutMenuItem.setToolTipText("About developer");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuBar1.add(helpMenu);

        optionsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/settingsIcon.png"))); // NOI18N
        optionsMenu.setText("Options");

        refreshMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/refreshIcon.png"))); // NOI18N
        refreshMenuItem.setText("Refresh");
        refreshMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(refreshMenuItem);

        connectionMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/chat15.jpg"))); // NOI18N
        connectionMenuItem.setText("Chat");
        connectionMenuItem.setEnabled(false);
        optionsMenu.add(connectionMenuItem);
        optionsMenu.add(jSeparator2);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pngs/exitIcon.png"))); // NOI18N
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(exitMenuItem);

        jMenuBar1.add(optionsMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onePlayerRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_onePlayerRadioButtonItemStateChanged
        // TODO add your handling code here:
        if(this.onePlayerRadioButton.isSelected()){
            this.playerSelectionPopup.add(this.playerSelectionPanel);
            this.playerSelectionPopup.show(this.gameTypeSelectionPanel, this.onePlayerRadioButton.getX(), this.onePlayerRadioButton.getY());
        }
    }//GEN-LAST:event_onePlayerRadioButtonItemStateChanged

    private void oSelectionRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_oSelectionRadioButtonStateChanged
        // TODO add your handling code here:
        if(this.oSelectionRadioButton.isSelected()){
            if(!this.startOnePlayerGameButton.isEnabled()){
                this.startOnePlayerGameButton.setEnabled(true);
                this.playerOne = "O";
                this.playerTwo = "X";
            }
            
        }
    }//GEN-LAST:event_oSelectionRadioButtonStateChanged

    private void xSelectionRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_xSelectionRadioButtonStateChanged
        // TODO add your handling code here:
        if(this.xSelectionRadioButton.isSelected()){
            if(!this.startOnePlayerGameButton.isEnabled()){
                this.startOnePlayerGameButton.setEnabled(true);
                this.playerOne = "X";
                this.playerTwo = "O";
            }
            
        }
    }//GEN-LAST:event_xSelectionRadioButtonStateChanged

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        // TODO add your handling code here:
        this.exitAction();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void refreshMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshMenuItemActionPerformed
        // TODO add your handling code here:
        this.refreshGameView();
    }//GEN-LAST:event_refreshMenuItemActionPerformed

    private void startOnePlayerGameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startOnePlayerGameButtonActionPerformed
        // TODO add your handling code here:
        this.playerSelectionPopup.setVisible(false);
        this.lockGameOptions(true);
        this.remove(this.exibitionLabel);
        this.add(this.boardPanel = new BoardPanel(this.playViewsList));
        this.feedBackTextArea.setText(this.feedBackTextArea.getText() + ""
                + "\nYou are Player" + this.playerOne + ".");
        this.onePlayer = true ;
        this.twoPlayer = false ;
        this.onlinePlay = false ;
        this.playerOneTurn = true ;
        this.onePlayerRadioButton.setForeground(Color.BLUE);
        this.repaint();
        this.revalidate();
    }//GEN-LAST:event_startOnePlayerGameButtonActionPerformed

    private void onePlayerRadioButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onePlayerRadioButtonMouseEntered
        // TODO add your handling code here:
        if(!this.onePlayerRadioButton.isEnabled())
            return ;
        this.onePlayerRadioButton.setForeground(Color.blue);
    }//GEN-LAST:event_onePlayerRadioButtonMouseEntered

    private void twoPlayerRadioButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_twoPlayerRadioButtonMouseExited
        // TODO add your handling code here:
        if(!this.twoPlayerRadioButton.isEnabled())
            return ;
        this.twoPlayerRadioButton.setForeground(Color.black);
    }//GEN-LAST:event_twoPlayerRadioButtonMouseExited

    private void twoPlayerRadioButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_twoPlayerRadioButtonMouseEntered
        // TODO add your handling code here:
        if(!this.twoPlayerRadioButton.isEnabled())
            return ;
        this.twoPlayerRadioButton.setForeground(Color.blue);
    }//GEN-LAST:event_twoPlayerRadioButtonMouseEntered

    private void onlinePlayerRadioButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onlinePlayerRadioButtonMouseEntered
        // TODO add your handling code here:
        if(!this.onlinePlayerRadioButton.isEnabled())
            return ;
        this.onlinePlayerRadioButton.setForeground(Color.BLUE);
    }//GEN-LAST:event_onlinePlayerRadioButtonMouseEntered

    private void onlinePlayerRadioButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onlinePlayerRadioButtonMouseExited
        // TODO add your handling code here:
        if(!this.onlinePlayerRadioButton.isEnabled())
            return ;
        this.onlinePlayerRadioButton.setForeground(Color.black);
    }//GEN-LAST:event_onlinePlayerRadioButtonMouseExited

    private void onePlayerRadioButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onePlayerRadioButtonMouseExited
        // TODO add your handling code here:
        if(!this.onePlayerRadioButton.isEnabled())
            return ;
        this.onePlayerRadioButton.setForeground(Color.black);
    }//GEN-LAST:event_onePlayerRadioButtonMouseExited

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        // TODO add your handling code here:
        this.aboutPopup.remove(this.hintsLabel);
        this.aboutPopup.add(this.aboutLabel);
        this.aboutPopup.show(this, this.aboutMenuItem.getX() + 30, this.aboutMenuItem.getY()+50);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void twoPlayerRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twoPlayerRadioButtonActionPerformed
        // TODO add your handling code here:
        if(this.twoPlayerRadioButton.isSelected()){
            this.twoPlayer = true ;
            this.onePlayer = false ;
            this.onlinePlay = false ;
            this.lockGameOptions(true);
            this.remove(this.exibitionLabel);
            this.add(this.boardPanel = new BoardPanel(this.playViewsList));
            playerOne = "X";
            playerTwo = "O" ;
            playerOneTurn = true ;
            this.repaint();
            this.revalidate();
            this.feedBackTextArea.setText("Player [ " + playerOne + " ] plays first");
        }
    }//GEN-LAST:event_twoPlayerRadioButtonActionPerformed

    private void hintsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintsMenuItemActionPerformed
        // TODO add your handling code here:
        //this.aboutPopup.remove(this.aboutLabel);
        //this.aboutPopup.add(this.hintsLabel);
        //this.aboutPopup.show(this, this.aboutMenuItem.getX() + 30, this.aboutMenuItem.getY()+50);
    }//GEN-LAST:event_hintsMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        exitAction();
    }//GEN-LAST:event_formWindowClosing

    private void scoreBoardMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scoreBoardMenuActionPerformed
        // TODO add your handling code here:
        this.statsBoardPopup.show(this, this.helpMenu.getX() - 7, this.aboutMenuItem.getY()+30);
    }//GEN-LAST:event_scoreBoardMenuActionPerformed

    
    private void connectOnlineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectOnlineButtonActionPerformed
        //connection action takes place here
        this.onlinePlayLoadingConnectionLabel.setEnabled(true);
        this.onlineConnectionStatusLabel.setForeground(Color.BLUE);
        onlineO_SelectionRadioButton.setEnabled(false);
        onlineX_SelectionRadioButton.setEnabled(false);
        this.onlineConnectionStatusLabel.setText("connecting...");
        this.sendChallengeRequest(this.cpuNameTextField.getText(), Integer.parseInt(this.cpuPortTextField.getText()));
    }//GEN-LAST:event_connectOnlineButtonActionPerformed

    private void onlineO_SelectionRadioButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onlineO_SelectionRadioButtonMouseExited
        // TODO add your handling code here:
        if(!onlineO_SelectionRadioButton.isEnabled())
            return ;
        this.onlineO_SelectionRadioButton.setForeground(Color.black);
    }//GEN-LAST:event_onlineO_SelectionRadioButtonMouseExited

    private void onlineO_SelectionRadioButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onlineO_SelectionRadioButtonMouseEntered
        // TODO add your handling code here:
        if(!onlineO_SelectionRadioButton.isEnabled())
            return ;
        this.onlineO_SelectionRadioButton.setForeground(Color.blue);
    }//GEN-LAST:event_onlineO_SelectionRadioButtonMouseEntered

    private void onlineX_SelectionRadioButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onlineX_SelectionRadioButtonMouseExited
        // TODO add your handling code here:
        if(!onlineX_SelectionRadioButton.isEnabled())
            return ;
        this.onlineX_SelectionRadioButton.setForeground(Color.black);
    }//GEN-LAST:event_onlineX_SelectionRadioButtonMouseExited

    private void onlineX_SelectionRadioButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onlineX_SelectionRadioButtonMouseEntered
        // TODO add your handling code here:
        if(!onlineX_SelectionRadioButton.isEnabled())
            return ;
        this.onlineX_SelectionRadioButton.setForeground(Color.blue);
    }//GEN-LAST:event_onlineX_SelectionRadioButtonMouseEntered

    private void onlineX_SelectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlineX_SelectionRadioButtonActionPerformed
        // TODO add your handling code here:
        if(onlineX_SelectionRadioButton.isSelected()){
            this.playerOne = "X";
            this.playerTwo = "O";
            this.connectOnlineButton.setEnabled(true);
            this.onlineX_SelectionRadioButton.setForeground(Color.BLUE);
            this.onlineO_SelectionRadioButton.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_onlineX_SelectionRadioButtonActionPerformed

    private void onlineO_SelectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlineO_SelectionRadioButtonActionPerformed
        // TODO add your handling code here:
        if(onlineO_SelectionRadioButton.isSelected()){
            this.playerOne = "O";
            this.playerTwo = "X";
            this.connectOnlineButton.setEnabled(true);
            this.onlineO_SelectionRadioButton.setForeground(Color.BLUE);
            this.onlineX_SelectionRadioButton.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_onlineO_SelectionRadioButtonActionPerformed

    private void onlinePlayerRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_onlinePlayerRadioButtonItemStateChanged
        // TODO add your handling code here:
        if(onlinePlay){
            this.onlinePlayerRadioButton.setForeground(Color.blue);
            return ;
        }
        if(this.onlinePlayerRadioButton.isSelected()){
            this.onlinePlayerRadioButton.setForeground(Color.blue);
            this.onlineOptionsPopupMenu.show(this.gameTypeSelectionPanel
                , this.onlinePlayerRadioButton.getX()
                , this.onlinePlayerRadioButton.getY() - 150);
        }else{
            this.onlinePlayerRadioButton.setForeground(Color.black);
        }
    }//GEN-LAST:event_onlinePlayerRadioButtonItemStateChanged

    
    
    
    private void playOnePlayerGameAgain(){
        this.playerOneTurn = false ;
        this.feedBackTextArea.setText("Opponent accepted to play again.");
        this.loadPlayViewsList();
        this.removeMouseListeners();
        this.addMouseListeners();
        this.playerOneMoveList.clear();
        this.playerTwoMoveList.clear();
        this.reloadPlayersMoves();
        this.remove(this.boardPanel);
        this.add(this.boardPanel = new BoardPanel(this.playViewsList));
        this.repaint();
        this.revalidate();
        
        if(this.playCount%2 == 0){//player goes first
            this.feedBackTextArea.setText("Your " + this.playerOne + " turn.");
            playerOneTurn = true ;
        }else{
            this.feedBackTextArea.setText("Player " + this.playerTwo + " turn.");
            this.computerPlay();
        }
    }
    
    private void playTwoPlayerGameAgain(){
        
        this.loadPlayViewsList();
        this.removeMouseListeners();
        this.addMouseListeners();
        this.playerOneMoveList.clear();
        this.playerTwoMoveList.clear();
        this.reloadPlayersMoves();
        this.remove(this.boardPanel);
        this.add(this.boardPanel = new BoardPanel(this.playViewsList));
        this.repaint();
        this.revalidate();
        
        if(this.playCount%2 == 0){//player goes first
            this.feedBackTextArea.setText("Player " + this.playerOne + " turn");
            this.playerOneTurn = true ;
        }else{
            this.feedBackTextArea.setText("Player " + this.playerTwo + " turn");
            this.playerOneTurn = false ;
        }
    }
    
    private void onlineOpponentPlay(final int move){
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                MainClass.playNotification("/res/wavs/teeClick.wav");
                playViewsList.get(move).drawPlay(playerTwo);
                playerTwoMoveList.add(playViewsList.get(move).getPosition());
                playViewsList.get(move).removeMouseListener(mouseClickAction);
                //playViewsList.remove(move);
                
                int testGame = isWin(playerTwoMoveList);
                if(testGame == WIN){//cpu wins
                    //++playerTwoWins ;
                    MainFrame.this.statsBoard.updateOnlineAwayPlayerScore();
                    MainClass.playNotification("/res/wavs/gong.wav");
                    gameWinAction(playerTwo, 3); 
                }else if( testGame == DRAW){//draw 
                    MainClass.playNotification("/res/wavs/boo.wav");
                    MainFrame.this.statsBoard.updateOnlineDrawScore();
                    gameDrawAction(3);
                }else{
                    MainFrame.this.feedBackTextArea.setText(feedBackTextArea.getText() +"\n"
                            + "Your turn.");
                    if(!playerOneTurn){
                        playerOneTurn = true ;
                    }
                }
            }
        });
    }
    
    private void computerPlay(){
        
        final int cpuMove = this.getCPU_NextMove(); // get cpu next move
        
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                playViewsList.get(cpuMove).drawPlay(playerTwo);
                MainClass.playNotification("/res/wavs/teeClick.wav");
                playerTwoMoveList.add(playViewsList.get(cpuMove).getPosition());
                playViewsList.get(cpuMove).removeMouseListener(mouseClickAction);
                playViewsList.remove(cpuMove);
                
                int testGame = isWin(playerTwoMoveList);
                if(testGame == WIN){//cpu wins
                    //++playerTwoWins ;
                    MainFrame.this.statsBoard.updateOnePlayerComputerScore();
                    MainClass.playNotification("/res/wavs/gong.wav");
                    gameWinAction("CPU", 1); 
                }else if( testGame == DRAW){//draw 
                    MainClass.playNotification("/res/wavs/boo.wav");
                    MainFrame.this.statsBoard.updateOnePlayerDrawScore();
                    gameDrawAction(1);
                }else{
                    if(!playerOneTurn){
                    playerOneTurn = true ;
                    MainFrame.this.feedBackTextArea.setText(feedBackTextArea.getText() +"\n"
                            + "Player " + playerOne + " turn.");
                    }
                }
            }
        });
    }
    
    private void gameWinAction(String winner, int gameType){
        ++this.playCount ;
        if(gameType == 1){//1 player game
            int opt = JOptionPane.showConfirmDialog(MainFrame.this.feedBackTextArea, winner + " wins this one. \n Play again ?", "", JOptionPane.YES_NO_OPTION);
            if(opt == JOptionPane.YES_OPTION){
                //play one player again
                playOnePlayerGameAgain();
            }else{
                refreshGameView();
            } 
        }else if(gameType == 2){//2 player game
            int opt = JOptionPane.showConfirmDialog(MainFrame.this.feedBackTextArea, "Player " + winner + ""
                                        + " Wins. \nPlay again ?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(opt == JOptionPane.YES_OPTION){
                //playAgain
                MainFrame.this.playTwoPlayerGameAgain();
            }else{
                MainFrame.this.refreshGameView();
            }
        }else if(gameType == 3){//online game
            int opt = JOptionPane.showConfirmDialog(MainFrame.this.feedBackTextArea, "Player " + winner + ""
                    + " Wins. \nPlay again ?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(opt == JOptionPane.YES_OPTION){
                //playAgain
                //MainFrame.this.playOnlineGameAgain();
                try {
                    MainFrame.outputStream.writeObject(new Message(Message.PLAY_AGAIN_ACCEPTED_COMMAND, -1, ""));
                    MainFrame.outputStream.flush();
                } catch (IOException ex) {
                    System.out.println("Could not write play again message in gameDrawAction()");
                }
            }else{
                MainFrame.this.refreshGameView();
            }
        }else{
            System.out.println("Unkown game type in gameWinAction Method");
        }   
    }
    
    private void gameDrawAction(int gameType){
        ++playCount ;
        if(gameType == 1){// 1 player game
            int opt = JOptionPane.showConfirmDialog(MainFrame.this.feedBackTextArea, "Game ends in a Draw. \n Play again ?", "", JOptionPane.YES_NO_OPTION);
            if(opt == JOptionPane.YES_OPTION){
                //play one player again
                playOnePlayerGameAgain();
            }else{
                refreshGameView();
            } 
        }else if(gameType == 2){//2 player game
            int opt = JOptionPane.showConfirmDialog(MainFrame.this.feedBackTextArea, "It's a Draw. \nPlay again ?", "Draw"
                            , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(opt == JOptionPane.YES_OPTION){
                //playAgain
                MainFrame.this.playTwoPlayerGameAgain();
            }else{
                MainFrame.this.refreshGameView();
            }
            
        }else if(gameType == 3){// online game 
            int opt = JOptionPane.showConfirmDialog(MainFrame.this.feedBackTextArea, "It's a Draw. \nPlay again ?", "Draw"
                            , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(opt == JOptionPane.YES_OPTION){
                //playAgain
                //MainFrame.this.playOnlineGameAgain();
                try {
                    MainFrame.outputStream.writeObject(new Message(Message.PLAY_AGAIN_ACCEPTED_COMMAND, -1, ""));
                    MainFrame.outputStream.flush();
                } catch (IOException ex) {
                    System.out.println("Could not write play again message in gameDrawAction()");
                }
            }else{
                MainFrame.this.refreshGameView();
            }
        }else{
            System.out.println("Unknown game type in gameDrawAction Method");
        }
        
    }
    
    private void playOnlineGameAgain(){
        //++playCount;
        this.loadPlayViewsList();
        this.removeMouseListeners();
        this.addMouseListeners();
        this.playerOneMoveList.clear();
        this.playerTwoMoveList.clear();
        this.reloadPlayersMoves();
        this.remove(this.boardPanel);
        this.add(this.boardPanel = new BoardPanel(this.playViewsList));
        this.repaint();
        this.revalidate();
        
        if(this.onlineGameInitiator){
            this.playerOneTurn = false ;
            onlineGameInitiator = false ;
            this.feedBackTextArea.setText("Opponent accepted to play again. \nOpponent goes first.");
        }else{
            this.playerOneTurn = true ;
            onlineGameInitiator = true ;
            this.feedBackTextArea.setText("Opponent accepted to play again. \nYou go first.");
            //wait for other player before you can make a move
        }
        //System.out.println("playOnlineGameAgain() Called");
    }
    
    private int getCPU_NextMove(){
        int randInt = 0 ;
        randInt = new Random().nextInt(this.playViewsList.size());
        return randInt ;  
    }
    
    private int isWin(ArrayList<Integer> list){
        
        if(list.contains(0) && list.contains(1) && list.contains(2)){
            return WIN ;
        }
        if(list.contains(3) && list.contains(4) && list.contains(5)){
            return WIN ;
        }
        if(list.contains(6) && list.contains(7) && list.contains(8)){
            return WIN ;
        }
        if(list.contains(0) && list.contains(3) && list.contains(6)){
            return WIN ;
        }
        if(list.contains(1) && list.contains(4) && list.contains(7)){
            return WIN ;
        }
        if(list.contains(2) && list.contains(5) && list.contains(8)){
            return WIN ;
        }
        if(list.contains(2) && list.contains(4) && list.contains(6)){
            return WIN ;
        }
        if(list.contains(0) && list.contains(4) && list.contains(8)){
            return WIN ;
        }
        if(checkForDraw()){//check for draw
            return DRAW ;
        }
        return NULL ;
    }
    
    private boolean checkForDraw(){
        if(this.playViewsList.isEmpty())
            return true ;
        else if(this.playerOneMoveList.size() == 5 || this.playerTwoMoveList.size() == 5 )
            return true ;
        else
            return false ;
    }
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aboutLabel;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPopupMenu aboutPopup;
    private javax.swing.JButton connectOnlineButton;
    private javax.swing.JMenuItem connectionMenuItem;
    private javax.swing.JTextField cpuNameTextField;
    private javax.swing.JTextField cpuPortTextField;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPanel feedBackPanel;
    private javax.swing.JTextArea feedBackTextArea;
    private javax.swing.ButtonGroup gameTypeButtonGroup;
    private javax.swing.JPanel gameTypeSelectionPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel hintsLabel;
    private javax.swing.JMenuItem hintsMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JRadioButton oSelectionRadioButton;
    private javax.swing.JRadioButton onePlayerRadioButton;
    private javax.swing.JLabel onlineConnectionStatusLabel;
    private javax.swing.ButtonGroup onlineGameOptionsButtonGroup;
    private javax.swing.JRadioButton onlineO_SelectionRadioButton;
    private javax.swing.JPopupMenu onlineOptionsPopupMenu;
    private javax.swing.JLabel onlinePlayLoadingConnectionLabel;
    private javax.swing.JPanel onlinePlayOptionsPanel;
    private javax.swing.JRadioButton onlinePlayerRadioButton;
    private javax.swing.JRadioButton onlineX_SelectionRadioButton;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.ButtonGroup playerSelectionButtonGroup;
    private javax.swing.JPanel playerSelectionPanel;
    private javax.swing.JMenuItem refreshMenuItem;
    private javax.swing.JMenuItem scoreBoardMenu;
    private javax.swing.JButton startOnePlayerGameButton;
    private javax.swing.JRadioButton twoPlayerRadioButton;
    private javax.swing.JRadioButton xSelectionRadioButton;
    // End of variables declaration//GEN-END:variables

    private void exitAction() {
        int response = JOptionPane.showConfirmDialog(this, "Exit Tic-Tac-Toe", "Exit Game", JOptionPane.YES_NO_OPTION);
        if(response == JOptionPane.YES_OPTION){
            System.exit(0) ;
        }
    }
    
    
    private class MouseClickAction extends MouseAdapter{

        @Override
        public void mouseClicked(MouseEvent mEvt) {
            if(mEvt.getClickCount() < 2 )
               return ; 
            if(onePlayer && (!playerOneTurn)){
                System.out.println("Wait for player one turn ");
                return ;
            }
            
            final PlayView tempPView = (PlayView)mEvt.getSource();
            final int pos = tempPView.getPosition() ;
            SwingUtilities.invokeLater(new Runnable(){ public void run(){
            switch (pos){
                case 0:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            //System.out.println("Online play move on " + tempPView.getPosition());
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }
                case 1:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }
                case 2:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }
                case 3:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2) ;
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }
                case 4:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }
                case 5:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }
                case 6:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }
                case 7:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                //++MainFrame.this.playerOneWins ;
                                gameWinAction(playerOne, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                }case 8:{
                    if(onePlayer){
                        tempPView.drawPlay(playerOne);
                        MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                        tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                        MainFrame.this.playViewsList.remove(tempPView);
                        MainClass.playNotification("/res/wavs/click.wav");
                    }
                    if(twoPlayer){
                        if(playerOneTurn){//player one move
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                //++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerX_Wins();
                                gameWinAction(playerOne, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerTwo + " turn.");
                                playerOneTurn = false ;
                            }
                        }else{//player two move
                            tempPView.drawPlay(playerTwo);
                            MainFrame.this.playerTwoMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/teeClick.wav");
                            int test =  isWin(playerTwoMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateTwoPlayerO_Wins();
                                gameWinAction(playerTwo, 2);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateTwoPlayerDrawScore();
                                gameDrawAction(2);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Player " + playerOne + " turn.");
                                playerOneTurn = true ;
                            }
                        }
                    }
                    if(onlinePlay){
                        //System.out.println("Online play move on " + tempPView.getPosition());
                        if(playerOneTurn){
                            tempPView.drawPlay(playerOne);
                            MainFrame.this.writePlayerMoveToStream(tempPView.getPosition());
                            MainFrame.this.playerOneMoveList.add(tempPView.getPosition());
                            tempPView.removeMouseListener(MainFrame.this.mouseClickAction);
                            //MainFrame.this.playViewsList.remove(tempPView);
                            MainClass.playNotification("/res/wavs/click.wav");
                            int test =  isWin(playerOneMoveList);
                            if(test == WIN){
                                //System.out.println("Player one wins ");
                                // ++MainFrame.this.playerOneWins ;
                                MainFrame.this.statsBoard.updateOnlineHomePlayerScore();
                                gameWinAction(playerOne, 3);
                            }else if(test == DRAW){
                                MainFrame.this.statsBoard.updateOnlineDrawScore();
                                gameDrawAction(3);
                            }else{
                                feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                                    + "Opponents turn.");
                                playerOneTurn = false ;
                            }
                        }
                    }
                    break ;
                } 
            }
            
            if(MainFrame.this.onePlayer){
                int testPlay = isWin(playerOneMoveList);
                if(testPlay == WIN){
                    //++playerOneWins;
                    MainFrame.this.statsBoard.updateOnePlayerHumanScore();
                    MainFrame.this.gameWinAction(playerOne, 1);
                }else if (testPlay == DRAW){
                    MainFrame.this.gameDrawAction(1);
                    MainFrame.this.statsBoard.updateOnePlayerDrawScore();
                }else{
                    MainFrame.this.computerPlay();
                    feedBackTextArea.setText(feedBackTextArea.getText() + "\n"
                            + "Player " + playerTwo + " turn.");
                    playerOneTurn = false ;
                }}
            };
            //if(MainFrame.this.getCPU_NextMove() == -1){//game is drawn if computer has no more moves to play
            //    int opt = JOptionPane.showConfirmDialog(MainFrame.this, "It's a draw !\n Play again ?", "Draw", JOptionPane.YES_NO_OPTION);
            //if(opt == JOptionPane.YES_OPTION){
                //play one player again
            //    playOnePlayerGameAgain();
            //}else{
            //    refreshGameView();
            //}
            //return ;
            //}
            });
        
        }
    }
}
