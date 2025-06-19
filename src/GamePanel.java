import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.io.*;
public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNITS_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_HEIGHT*SCREEN_HEIGHT)/UNITS_SIZE;
    static final int DELAY = 90;
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts=6;
    int eatenApples = 0;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean isRunning = false;
    int highScore = 0;
    final String HIGH_SCORE_FILE = "highscorefile.txt";
    JButton replayButton = new JButton("Replay");

    Timer timer;
    Random random;
    GamePanel(){
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
        this.setBackground(Color.DARK_GRAY);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }
    public void startGame(){
        newApple();
        loadHighScore();
        isRunning = true;
        timer = new Timer(DELAY,this);
        timer.start();

    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }
    public void draw(Graphics g){
        if(isRunning) {
            for (int i = 0; i < SCREEN_HEIGHT / UNITS_SIZE; i++) {
                g.drawLine(i * UNITS_SIZE, 0, i * UNITS_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNITS_SIZE, SCREEN_WIDTH, i * UNITS_SIZE);
            }
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNITS_SIZE, UNITS_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                g.setColor((i == 0) ? new Color(0, 100, 0) : Color.GREEN);
                g.fillRect(x[i], y[i], UNITS_SIZE, UNITS_SIZE);
            }

            g.setColor(Color.red);
            g.setFont(new Font("Ink Free",Font.BOLD,25));
            FontMetrics fontMetrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + eatenApples ,(SCREEN_WIDTH - fontMetrics.stringWidth("Score: " + eatenApples))/2 , SCREEN_HEIGHT/11);
        }
        else {
            gameOver(g);
        }
    }
    public void move(){
        for (int i = bodyParts; i > 0 ; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }
        switch (direction){
            case 'U' ->{
                y[0] = y[0] - UNITS_SIZE;
            }
            case 'D' ->{
                y[0] = y[0] + UNITS_SIZE;
            }
            case 'L' ->{
                x[0] = x[0] - UNITS_SIZE;
            }
            case 'R' ->{
                x[0] = x[0] + UNITS_SIZE;
            }
        }
    }
    public void checkApple(){
        if(appleX == x[0] && appleY == y[0]  ){
            bodyParts++;
            eatenApples++;
            newApple();
        }
    }
    public void checkCollusion(){
        for (int i = bodyParts; i > 0 ; i--) {
            if(x[0]==x[i] && y[0]==y[i]){
                isRunning = false;
                break;
            }
        }
        if( x[0] >= SCREEN_WIDTH || y[0] >= SCREEN_HEIGHT || x[0] < 0 || y[0] < 0 ){
            isRunning=false;
        }
        if(!isRunning){
            timer.stop();
        }
    }
    public void gameOver(Graphics g){
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free",Font.BOLD,75));
        FontMetrics fontMetrics = getFontMetrics(g.getFont());
        g.drawString("GAME OVER",(SCREEN_WIDTH - fontMetrics.stringWidth("GAME OVER"))/2 , SCREEN_HEIGHT/2);


// Draw Score
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics scoreMetrics = getFontMetrics(g.getFont());
        g.drawString("Score: " + eatenApples,
                (SCREEN_WIDTH - scoreMetrics.stringWidth("Score: " + eatenApples)) / 2,
                SCREEN_HEIGHT / 2 - 100);

// Draw High Score
        if(highScore < eatenApples){
            highScore = eatenApples;
            saveHighScore();
        }
        g.drawString("High Score: " + highScore,
                (SCREEN_WIDTH - scoreMetrics.stringWidth("High Score: " + highScore)) / 2,
                SCREEN_HEIGHT / 2 + 100);


        if (!this.getComponents().toString().contains("Replay")) {
            replayButton.setBounds((SCREEN_WIDTH - 150) / 2, SCREEN_HEIGHT / 2 + 150, 150, 50);
            replayButton.setFont(new Font("Ink Free", Font.BOLD, 20));
            replayButton.setFocusable(false);
            replayButton.addActionListener(e -> resetGame());
            this.setLayout(null);
            this.add(replayButton);
            this.repaint();
        }


    }

    public void newApple(){
        appleX = random.nextInt((int)(SCREEN_WIDTH/UNITS_SIZE))*UNITS_SIZE;
        appleY = random.nextInt((int)(SCREEN_HEIGHT/UNITS_SIZE))*UNITS_SIZE;
    }
    public void resetGame() {
        eatenApples = 0;
        bodyParts = 6;
        direction = 'R';
        for (int i = 0; i < x.length; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        this.remove(replayButton); // remove button
        isRunning = true;
        newApple();
        timer.start();
        repaint();
    }

    public void loadHighScore(){
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))){
            String line = reader.readLine();
            if(line!=null){
                highScore = Integer.parseInt(line);
            }
        }
        catch (IOException e) {
            System.out.println("No high score file found. Starting fresh.");
        }
    }

    public void saveHighScore(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(isRunning){
            move();
            checkApple();
            checkCollusion();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e){
            switch (e.getKeyCode()){
                case KeyEvent.VK_LEFT -> {if(direction !=  'R') direction = 'L';}

                case KeyEvent.VK_RIGHT ->{ if(direction != 'L') direction = 'R';}

                case KeyEvent.VK_UP ->{ if(direction != 'D') direction = 'U';}

                case KeyEvent.VK_DOWN ->{ if(direction != 'U') direction = 'D';}
            }
        }
    }
}
