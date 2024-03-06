import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 75;
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    int topScore = 0;
    Timer timer;
    Random random;

    JButton restartButton;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.setLayout(new BorderLayout());
        this.addKeyListener(new MyKeyAdapter());

        restartButton = new JButton("Restart");
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> restartGame());
        this.add(restartButton, BorderLayout.SOUTH);
        this.remove(restartButton);

        startGame();
        loadTopScores();
    }

    private Integer[] topScores = new Integer[5];

    private void loadTopScores() {
        File file = new File("topScores.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (Scanner scanner = new Scanner(file)) {
            int index = 0;
            while (scanner.hasNextInt() && index < topScores.length) {
                topScores[index++] = scanner.nextInt();
            }
            Arrays.sort(topScores);
            for(int i = 0; i < topScores.length / 2; i++) {
                int temp = topScores[i];
                topScores[i] = topScores[topScores.length - i - 1];
                topScores[topScores.length - i - 1] = temp;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void saveTopScores() {
        try (PrintWriter out = new PrintWriter("topScores.txt")) {
            for (int score : topScores) {
                out.println(score);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateTopScores() {
        for (int i = 0; i < topScores.length; i++) {
            if (applesEaten > topScores[i]) {
                for (int j = topScores.length - 1; j > i; j--) {
                    topScores[j] = topScores[j - 1];
                }
                topScores[i] = applesEaten;
                break;
            }
        }
        Arrays.sort(topScores, Collections.reverseOrder());
        saveTopScores();
    }


    private void restartGame() {
        if (applesEaten > topScore) {
            topScore = applesEaten;
        }
        applesEaten = 0;
        bodyParts = 6;
        direction = 'R';
        running = true;

        for (int i = 0; i < bodyParts; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        this.removeAll();
        this.revalidate();
        this.repaint();
        updateTopScores();
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    public void scoreboardDraw(Graphics g){
        int startY = SCREEN_HEIGHT - 105;
        g.setColor(Color.GRAY);
        g.setFont(new Font("Roboto Condensed", Font.BOLD, 15));
        FontMetrics scoreboard = getFontMetrics(g.getFont());
        String topScoresTitle = "Top 5 Scores:";
        g.drawString(topScoresTitle, (SCREEN_WIDTH - scoreboard.stringWidth(topScoresTitle)) / 2, startY);
        int scoreGap = 20;
        for (int i = topScores.length - 1; i >= 0 ; i--) {
            g.drawString("Top " + (i + 1) + ": " + topScores[i], (SCREEN_WIDTH - (scoreboard.stringWidth(topScoresTitle) - 36)) / 2, startY + scoreGap * (i + 1));
        }
    }
    private void sessionTopScore(Graphics g) {
        g.setFont(new Font("Roboto Condensed", Font.BOLD, 20));
        FontMetrics metricsTopScore = getFontMetrics(g.getFont());
        String topScoreStr = "(Session) Top Score: " + topScore;
        g.drawString(topScoreStr, (SCREEN_WIDTH - metricsTopScore.stringWidth(topScoreStr)) / 2, g.getFont().getSize() * 4);

        scoreboardDraw(g);
    }
    public void draw(Graphics g) {
        if (running) {
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            g.setColor(Color.red);
            g.setFont(new Font("Roboto Condensed", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());

            g.setColor(Color.GRAY);
            sessionTopScore(g);
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }
        if (x[0] < 0) {
            running = false;
        }
        if (x[0] >= SCREEN_WIDTH) {
            running = false;
        }
        if (y[0] < 0) {
            running = false;
        }
        if (y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            updateTopScores();
            if (!isRestartButtonAdded()) {
                addRestartButton();
            }
        }
    }

    private boolean isRestartButtonAdded() {
        return Arrays.asList(this.getComponents()).contains(restartButton);
    }

    private void addRestartButton() {
        restartButton.setBackground(new Color(50, 50, 50));
        restartButton.setForeground(new Color(0, 255, 0));
        restartButton.setFont(new Font("Roboto Condensed", Font.BOLD, 20));
        restartButton.setText("RETRY");

        int buttonWidth = 120;
        int buttonHeight = 40;
        int xPosition = (SCREEN_WIDTH - buttonWidth) / 2;
        int yPosition = SCREEN_HEIGHT - buttonHeight - 240;

        restartButton.setBounds(xPosition, yPosition, buttonWidth, buttonHeight);
        restartButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(null);
        buttonPanel.add(restartButton);
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        this.removeAll();
        this.setLayout(null);
        this.add(buttonPanel);

        this.revalidate();
        this.repaint();
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Roboto Condensed", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());

        g.setColor(Color.red);
        g.setFont(new Font("Roboto Condensed", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        g.setColor(Color.LIGHT_GRAY);
        sessionTopScore(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        } else {
            this.add(restartButton, BorderLayout.SOUTH);
            this.revalidate();
            this.repaint();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
            }
        }
    }
}
