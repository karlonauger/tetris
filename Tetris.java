/*******************************************************************
 * GLACTIC WAR, Chapter 14
 *******************************************************************/
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
/*******************************************************************
 * Primary class for game
 *******************************************************************/
public class Tetris extends Game {
    //these must be static because they are passed to a constructor
    static int FRAMERATE = 60;
    static int SCREENWIDTH = 816;
    static int SCREENHEIGHT = 616;//748;
    static int PLAYWIDTH = 336;//408;
    static int PLAYHEIGHT = 616;//748;
    //misc global constants
    final int PICE_X = 28;//34;
    final int PICE_Y = 28;//34;
    final int START_X = PLAYWIDTH/2;
    final int START_Y = PICE_Y*3;//102;

    boolean[][] Grid = new boolean[12][22];
    AnimatedSprite[] CurrentSprites = new AnimatedSprite[4];
    AnimatedSprite[] NextSprites = new AnimatedSprite[4];
    AnimatedSprite[] KeepSprites = new AnimatedSprite[4];

    //sprite types
    final int SPRITE_SQUARE_BLOCK  = 1;
    final int SPRITE_L_RIGHT_BLOCK = 2;
    final int SPRITE_L_LEFT_BLOCK  = 3;
    final int SPRITE_T_BLOCK       = 4;
    final int SPRITE_LINE_BLOCK    = 5;
    final int SPRITE_S_BLOCK       = 6;
    final int SPRITE_Z_BLOCK       = 7;

    final int SPRITE_BORDER=0;
    final int SPRITE_KEEP=12;
    //game states
    final int GAME_MENU = 0;
    final int GAME_RUNNING = 1;
    final int GAME_OVER = 2;
    final int GAME_PAUSED = 3;
    //various toggles
    boolean showBounds = false;
    boolean collisionTesting = true;
    boolean showGrid = false;
    boolean keep = true;
    boolean kepedPiece = false;
    //define the images used in the game
    ImageEntity background;
    ImageEntity red;
    ImageEntity blue;
    ImageEntity green;
    ImageEntity purple;
    ImageEntity orange;
    ImageEntity gray;
    ImageEntity lightblue;
    ImageEntity darkred;
    //health/shield meters and score
    int linesCleared = 1;
    int level = 1;
    int speed = 1;
    int lines = 0;
    int count = FRAMERATE/speed;
    int drop = 0;
    int softDrop = 0;
    Point2D center = new Point2D(START_X, START_Y);
    Point2D centerNext = new Point2D(START_X, START_Y);
    Point2D centerKeep = new Point2D(START_X, START_Y);
    int score = 0;
    int scoreCount = 0;
    int highscore = 0;
    int gameState = GAME_MENU;
    //create a random number generator
    Random rand = new Random();
    //traking variables for key imput
    boolean keyP, keyUp, keyLeft, keyRight, keyDown, keySpace;
    //sound effects and music
    //MidiSequence music = new MidiSequence();
    //SoundClip shoot = new SoundClip();
    //SoundClip explosion = new SoundClip();
    
/*******************************************************************
 * constructor
 *******************************************************************/
     public Tetris(){
        //call base Game class contructor
        super(FRAMERATE, SCREENWIDTH, SCREENHEIGHT);
     }
/*******************************************************************
 * gameStartup event passed by game engine
 *******************************************************************/
    public void gameStartup() {
        //load sound and music
        //music.load("music.mid");
        //shoot.load("shoot.au");
        //explosion.load("explode.au");
        //load the squares
        red = new ImageEntity(this);
        red.load("Red.png");
        orange = new ImageEntity(this);
        orange.load("Orange.png");
        blue = new ImageEntity(this);
        blue.load("Blue.png");
        green = new ImageEntity(this);
        green.load("Green.png");
        gray = new ImageEntity(this);
        gray.load("Gray.png");
        purple = new ImageEntity(this);
        purple.load("Purple.png");
        darkred = new ImageEntity(this);
        darkred.load("DarkRed.png");
        lightblue = new ImageEntity(this);
        lightblue.load("LightBlue.png");
        //load the background image
        background = new ImageEntity(this);
        background.load("black.png");
        //start off in pause mode
        pauseGame();
    }
/*******************************************************************
 * game resetGame
 *******************************************************************/
    private void resetGame(){
        //restart the music soundtrack
        //music.setLooping(true);
        //music.play();
        //wipe out the sprite list to start over
        sprites().clear();
        //clear grid
        for (int y = 0; y< 22; y++){
            for (int x = 0; x<12; x++){
                Grid[x][y] = false;
            }
        }
        //display border verticles
        for (int y = 0; y< PLAYHEIGHT; y+=PICE_Y){
            for (int x = 0; x<PLAYWIDTH-PICE_X+3; x+=PLAYWIDTH-PICE_X){
                AnimatedSprite pice = new AnimatedSprite(this, graphics());
                pice.setImage(gray.getImage());
                pice.setPosition(new Point2D(x,y));
                pice.setVelocity(new Point2D(0, 0));
                pice.setFrameWidth(PICE_X);
                pice.setFrameHeight(PICE_Y);
                pice.setSpriteType(SPRITE_BORDER);
                //add the new piece to the sprite list
                sprites().add(pice);
                Grid[(int) pice.position().X()/PICE_X][(int) pice.position().Y()/PICE_Y] = true;
            }
        }

        //display border horizontals
        for (int x = PICE_X; x< PLAYWIDTH-PICE_X; x+=PICE_X){
            for (int y = 0; y<PLAYHEIGHT-PICE_Y+3; y+=PLAYHEIGHT-PICE_Y){
                AnimatedSprite pice = new AnimatedSprite(this, graphics());
                pice.setImage(gray.getImage());
                pice.setPosition(new Point2D(x,y));
                pice.setVelocity(new Point2D(0, 0));
                pice.setFrameWidth(PICE_X);
                pice.setFrameHeight(PICE_Y);
                pice.setSpriteType(SPRITE_BORDER);
                //add the new piece to the sprite list
                sprites().add(pice);
                Grid[(int) pice.position().X()/PICE_X][(int) pice.position().Y()/PICE_Y] = true;
            }
        }

        //create random piece sprite
        createPiece(0);
        redyPiece();
        createPiece(0);
        //reset variables
        score = 0;
        level = 1;
        lines = 0;
        speed = 1;
        count = FRAMERATE/speed;
        kepedPiece = false;
    }
/*******************************************************************
 * game TimedUpdate event passed by game engine
 *******************************************************************/
    void gameTimedUpdate() {
        checkInput();
    }
/*******************************************************************
 * gameRefreshScreen event passed by game engine
 *******************************************************************/
    void gameDrawBackground() {
        Graphics2D g2d = graphics();
        //draw the background
        g2d.drawImage(background.getImage(),0,0,SCREENWIDTH,SCREENHEIGHT,this);
        //what is the game state?
    }

    void gameRefreshScreen() {
        Graphics2D g2d = graphics();
        //what is the game state?
        if (gameState == GAME_MENU) {
            g2d.setFont(new Font("Verdana", Font.BOLD, 36));
            g2d.setColor(Color.BLACK);
            g2d.drawString("TETRIS",252,202);
            g2d.setColor(new Color(200,30,30));
            g2d.drawString("TETRIS", 250, 200);

            int x = 270, y = 15;
            g2d.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("CONTROLS:",x,++y*20);
            g2d.drawString("ROTATE(Clockwise) - UP Arrow, X Key",x+20,++y*20);
            g2d.drawString("ROTATE(Counter Clockwise) - Z Key", x+20,++y*20);
            g2d.drawString("MOVE LEFT - Left Arrow", x+20,++y*20);
            g2d.drawString("MOVE RIGHT - Right Arrow", x+20,++y*20);
            g2d.drawString("HARD DROP - Space Bar", x+20,++y*20);
            g2d.drawString("SOFT DROP - Down Arrow", x+20,++y*20);
            g2d.drawString("HOLD - C Key", x+20,++y*20);

            //g2d.setColor(Color.WHITE);
            //g2d.drawString("POWERUPS INCREASE FIREPOWER!", 240, 480);

            g2d.setFont(new Font("Ariel", Font.BOLD, 24));
            g2d.setColor(Color.ORANGE);
            g2d.drawString("Press ENTER to start", 280, 570);
        }
        else if (gameState == GAME_RUNNING) {
            //display the score
            g2d.setFont(new Font("Verdana", Font.BOLD, 24));
            g2d.setColor(Color.RED);
            g2d.drawString("SCORE", PLAYWIDTH+20, 20);
            g2d.drawString(""+score, PLAYWIDTH+20, 40);
            g2d.drawString("LINES", PLAYWIDTH+230, 20);
            g2d.drawString(""+lines, PLAYWIDTH+230, 40);
            //g2d.setColor(Color.RED);
            //g2d.drawString("HIGHSCORE", PLAYWIDTH+230, 20);
            //g2d.drawString(""+highscore, PLAYWIDTH+230, 40);
            //g2d.setColor(Color.GREEN);
            g2d.drawString("LEVEL", PLAYWIDTH+130, 20);
            g2d.drawString(""+level, PLAYWIDTH+130, 40);
            g2d.setColor(Color.GREEN);
            g2d.drawString("NEXT", PLAYWIDTH+40, 80); //80);
            g2d.drawString("HOLD", PLAYWIDTH+40, 195);//220);
            //display the grid
            if (showGrid) {
                g2d.setColor(Color.WHITE);
                for (int y = 0; y< 22; y++){
                    for (int x = 0; x<12; x++){
                        if (Grid[x][y])
                            g2d.drawString("1", 10+(x*PICE_X), 25+(y*PICE_Y));
                        if (!Grid[x][y])
                            g2d.drawString("0", 10+(x*PICE_X), 25+(y*PICE_Y));
                    }
                }
            }
        }
        else if (gameState == GAME_OVER){
            //display the score
            g2d.setFont(new Font("Verdana", Font.BOLD, 24));
            g2d.setColor(Color.RED);
            g2d.drawString("SCORE", PLAYWIDTH+20, 20);
            g2d.drawString(""+score, PLAYWIDTH+20, 40);
            g2d.drawString("LINES", PLAYWIDTH+230, 20);
            g2d.drawString(""+lines, PLAYWIDTH+230, 40);
            //g2d.setColor(Color.RED);
            //g2d.drawString("HIGHSCORE", PLAYWIDTH+230, 20);
            //g2d.drawString(""+highscore, PLAYWIDTH+230, 40);
            //g2d.setColor(Color.GREEN);
            g2d.drawString("LEVEL", PLAYWIDTH+130, 20);
            g2d.drawString(""+level, PLAYWIDTH+130, 40);
            g2d.setColor(Color.GREEN);
            g2d.drawString("NEXT", PLAYWIDTH+40, 80); //80);
            g2d.drawString("HOLD", PLAYWIDTH+40, 195);//220);

            g2d.setFont(new Font("Verdana", Font.BOLD, 36));
            g2d.setColor(new Color(200, 30, 30));
            g2d.drawString("GAME OVER", 270, 200);

            g2d.setFont(new Font("Arial", Font.CENTER_BASELINE, 24));
            g2d.setColor(Color.ORANGE);
            g2d.drawString("Press ENTER to restart", 260, 500);
            if (showGrid) {
                g2d.setColor(Color.WHITE);
                for (int y = 0; y< 22; y++){
                    for (int x = 0; x<12; x++){
                        if (Grid[x][y])
                            g2d.drawString("1", 10+(x*PICE_X), 25+(y*PICE_Y));
                        if (!Grid[x][y])
                            g2d.drawString("0", 10+(x*PICE_X), 25+(y*PICE_Y));
                    }
                }
            }
        }
        else if (gameState == GAME_PAUSED){
            //display the score
            g2d.setFont(new Font("Verdana", Font.BOLD, 24));
            g2d.setColor(Color.RED);
            g2d.drawString("SCORE", PLAYWIDTH+20, 20);
            g2d.drawString(""+score, PLAYWIDTH+20, 40);
            g2d.drawString("LINES", PLAYWIDTH+230, 20);
            g2d.drawString(""+lines, PLAYWIDTH+230, 40);
            //g2d.setColor(Color.RED);
            //g2d.drawString("HIGHSCORE", PLAYWIDTH+230, 20);
            //g2d.drawString(""+highscore, PLAYWIDTH+230, 40);
            //g2d.setColor(Color.GREEN);
            g2d.drawString("LEVEL", PLAYWIDTH+130, 20);
            g2d.drawString(""+level, PLAYWIDTH+130, 40);
            g2d.setColor(Color.GREEN);
            g2d.drawString("NEXT", PLAYWIDTH+40, 80);
            g2d.drawString("HOLD", PLAYWIDTH+40, 220);

            g2d.setFont(new Font("Verdana", Font.BOLD, 36));
            g2d.setColor(new Color(200, 30, 30));
            g2d.drawString("GAME PAUSED", 270, 200);

            g2d.setFont(new Font("Arial", Font.CENTER_BASELINE, 24));
            g2d.setColor(Color.ORANGE);
            g2d.drawString("Press P to resume", 260, 500);
            g2d.drawString("Press ENTER to restart", 260, 520);
            if (showGrid) {
                g2d.setColor(Color.WHITE);
                for (int y = 0; y< 22; y++){
                    for (int x = 0; x<12; x++){
                        if (Grid[x][y])
                            g2d.drawString("1", 10+(x*PICE_X), 25+(y*PICE_Y));
                        if (!Grid[x][y])
                            g2d.drawString("0", 10+(x*PICE_X), 25+(y*PICE_Y));
                    }
                }
            }
        }
     }
/*******************************************************************
 * gameShutdown event passed by game engine
 *******************************************************************/
    void gameShutdown(){
        //music.stop();
        //shoot.stop();
        //explosions.stop();
    }
/*******************************************************************
 * spriteUpdate event passed by game engine
 *******************************************************************/    
    public void spriteUpdate(AnimatedSprite sprite) {

    }
/*******************************************************************
 * spriteDraw event passed by game engine
 * called by the game class after each sprite is drawn
 * to give you a chance to manipulate the sprite
 *******************************************************************/
    public void spriteDraw(AnimatedSprite sprite) {
        if (showBounds) {
            if (sprite.collided())
                sprite.drawBounds(Color.RED);
            else
                sprite.drawBounds(Color.BLUE);
        }
    }
/*******************************************************************
 * spriteDying event passed by game engine
 * called after a sprite's age reaches its lifespan
 * at which oint it will be killed off, and then removed from
 * the linked list. you can cancel the purging process here.
 *******************************************************************/
    public void spriteDying(AnimatedSprite sprite) {
        //currenntly no need to revive any sprites
    }
/*******************************************************************
 * spriteCollision event passed by game engine
 *******************************************************************/
    public boolean spriteCollision(AnimatedSprite spr1, AnimatedSprite spr2) {
        return true;
        //jump out quickly if collisions are off
        //if (!collisionTesting) //return false;
        //figure out what type of sprite has collided
        //if (spr1.spriteType()==CURRENT_SPRITE) {
            //did bullet hit an asteroid?
            //if (spr2.spriteType()!=CURRENT_SPRITE) {
                //return true;
           // }
       // }
        //return false;
    }
    public void checkLines() {
        for (int y=20; y>1; y--){
            if (Grid[1][y]&&Grid[2][y]&&Grid[3][y]&&Grid[4][y]&&
                    Grid[5][y]&&Grid[6][y]&&Grid[7][y]&&
                    Grid[8][y]&&Grid[9][y]&&Grid[10][y]&&Grid[11][y]){
                for (int x=1;x<11;x++){
                    Grid[x][y]=false;
                }
                for(int n=0; n<sprites().size(); n++) {
                    AnimatedSprite spr = (AnimatedSprite) sprites().get(n);
                    if(spr.position().Y()==y*PICE_Y) {
                        if(spr.play()){
                            spr.setAlive(false);
                        }
                    }
                }
                screenDown(y);
                bumpScore(100*linesCleared);
                lines++;
                checkLines();
                linesCleared++;
            }
            else {
                linesCleared = 1;
            }
        }
    }
/*******************************************************************
 * gameKeyDown event passed by game engine
 *******************************************************************/
    public void gameKeyDown(int keyCode) {
        switch(keyCode) {
        case KeyEvent.VK_LEFT:
            keyLeft = true;
            moveLeft();
            break;
        case KeyEvent.VK_RIGHT:
            keyRight = true;
            moveRight();
            break;
        case KeyEvent.VK_UP:
            keyUp = true;
            break;
        case KeyEvent.VK_Z:
            break;
        case KeyEvent.VK_X:
            break;
        case KeyEvent.VK_SPACE:
            keySpace = true;
            break;
        case KeyEvent.VK_DOWN:
            keyDown = true;
            break;
        case KeyEvent.VK_C:
            if (keep){
                keep = false;
                keepPiece();
            }
            break;
        case KeyEvent.VK_G:
            //toggle grid
            showGrid = !showGrid;
            break;
        case KeyEvent.VK_ENTER:
            if(gameState == GAME_MENU){
                resetGame();
                resumeGame();
                gameState = GAME_RUNNING;
            }
            else if (gameState == GAME_OVER){
                resetGame();
                resumeGame();
                gameState = GAME_RUNNING;
            }
            else if (gameState == GAME_PAUSED){
                resetGame();
                resumeGame();
                gameState = GAME_RUNNING;
            }
            break;
        case KeyEvent.VK_P:
            if (gameState==GAME_RUNNING){
                pauseGame();
                gameState = GAME_PAUSED;
            }
            else if (gameState==GAME_PAUSED){
                resumeGame();
                gameState = GAME_RUNNING;
            }
            break;
        case KeyEvent.VK_ESCAPE:
            if (gameState==GAME_RUNNING){
                pauseGame();
                gameState = GAME_PAUSED;
            }
            break;
        }
    }
/*******************************************************************
 * gameKeyUp event passed by game engine
 *******************************************************************/
    public void gameKeyUp(int keyCode) {
        switch(keyCode) {
        case KeyEvent.VK_LEFT:
            keyLeft = false;
            break;
        case KeyEvent.VK_RIGHT:
            keyRight = false;
            break;
        case KeyEvent.VK_UP:
            keyUp = false;
            rotateRight();
            break;
        case KeyEvent.VK_DOWN:
            keyDown = false;
            break;
        case KeyEvent.VK_Z:
            rotateLeft();
            break;
        case KeyEvent.VK_X:
            rotateRight();
            break;
        case KeyEvent.VK_SPACE:
            keySpace = false;
            hardDrop();
            break;
        case KeyEvent.VK_P:
            keyP = false;
            break;
        }
    }
/*******************************************************************
 * mouse events passed by game engine
 * the game is not currently using mouse input
 *******************************************************************/
    public void gameMouseDown() {}
    public void gameMouseUp() {}
    public void gameMouseMove() {}

/*******************************************************************
 * create a random "big" asteroid
 *******************************************************************/
    public void createPiece(int i) {
        //create new piece sprites
        AnimatedSprite pice1 = new AnimatedSprite(this, graphics());
        AnimatedSprite pice2 = new AnimatedSprite(this, graphics());
        AnimatedSprite pice3 = new AnimatedSprite(this, graphics());
        AnimatedSprite pice4 = new AnimatedSprite(this, graphics());

        if (i==0) {i = rand.nextInt(7)+1;}

        switch(i) {
        case 1:
            //square
            pice1.setImage(blue.getImage());
            pice2.setImage(blue.getImage());
            pice3.setImage(blue.getImage());
            pice4.setImage(blue.getImage());
            //set positions
            pice1.setPosition(new Point2D(START_X,        START_Y));
            pice2.setPosition(new Point2D(START_X-PICE_X, START_Y));
            pice3.setPosition(new Point2D(START_X,        START_Y-PICE_Y));
            pice4.setPosition(new Point2D(START_X-PICE_X, START_Y-PICE_Y));

            centerNext = new Point2D(START_X, START_Y);

            pice1.setSpriteType(SPRITE_SQUARE_BLOCK);
            pice2.setSpriteType(SPRITE_SQUARE_BLOCK);
            pice3.setSpriteType(SPRITE_SQUARE_BLOCK);
            pice4.setSpriteType(SPRITE_SQUARE_BLOCK);

            break;
        case 2:
            // L right
            pice1.setImage(green.getImage());
            pice2.setImage(green.getImage());
            pice3.setImage(green.getImage());
            pice4.setImage(green.getImage());
            //set positions
            pice1.setPosition(new Point2D(START_X,        START_Y));
            pice2.setPosition(new Point2D(START_X-PICE_X, START_Y));
            pice3.setPosition(new Point2D(START_X+PICE_X, START_Y));
            pice4.setPosition(new Point2D(START_X+PICE_X, START_Y-PICE_Y));

            centerNext = new Point2D(START_X+PICE_X/2, START_Y+PICE_Y/2);

            pice1.setSpriteType(SPRITE_L_RIGHT_BLOCK);
            pice2.setSpriteType(SPRITE_L_RIGHT_BLOCK);
            pice3.setSpriteType(SPRITE_L_RIGHT_BLOCK);
            pice4.setSpriteType(SPRITE_L_RIGHT_BLOCK);

            break;
        case 3:
            // L left
            pice1.setImage(purple.getImage());
            pice2.setImage(purple.getImage());
            pice3.setImage(purple.getImage());
            pice4.setImage(purple.getImage());
            //set positions
            pice1.setPosition(new Point2D(START_X,        START_Y));
            pice2.setPosition(new Point2D(START_X-PICE_X, START_Y));
            pice3.setPosition(new Point2D(START_X+PICE_X, START_Y));
            pice4.setPosition(new Point2D(START_X-PICE_X, START_Y-PICE_Y));

            centerNext = new Point2D(START_X+PICE_X/2, START_Y+PICE_Y/2);

            pice1.setSpriteType(SPRITE_L_LEFT_BLOCK);
            pice2.setSpriteType(SPRITE_L_LEFT_BLOCK);
            pice3.setSpriteType(SPRITE_L_LEFT_BLOCK);
            pice4.setSpriteType(SPRITE_L_LEFT_BLOCK);

            break;
        case 4:
            // T block
            pice1.setImage(orange.getImage());
            pice2.setImage(orange.getImage());
            pice3.setImage(orange.getImage());
            pice4.setImage(orange.getImage());
            //set positions
            pice1.setPosition(new Point2D(START_X,        START_Y));
            pice2.setPosition(new Point2D(START_X-PICE_X, START_Y));
            pice3.setPosition(new Point2D(START_X+PICE_X, START_Y));
            pice4.setPosition(new Point2D(START_X       , START_Y-PICE_Y));

            centerNext = new Point2D(START_X+PICE_X/2, START_Y+PICE_Y/2);

            pice1.setSpriteType(SPRITE_T_BLOCK);
            pice2.setSpriteType(SPRITE_T_BLOCK);
            pice3.setSpriteType(SPRITE_T_BLOCK);
            pice4.setSpriteType(SPRITE_T_BLOCK);

            break;
        case 5:
            // line block
            pice1.setImage(red.getImage());
            pice2.setImage(red.getImage());
            pice3.setImage(red.getImage());
            pice4.setImage(red.getImage());
            //set positions
            pice1.setPosition(new Point2D(START_X,               START_Y));
            pice2.setPosition(new Point2D(START_X-PICE_X,        START_Y));
            pice3.setPosition(new Point2D(START_X-PICE_X-PICE_X, START_Y));
            pice4.setPosition(new Point2D(START_X+PICE_X       , START_Y));

            centerNext = new Point2D(START_X, START_Y);

            pice1.setSpriteType(SPRITE_LINE_BLOCK);
            pice2.setSpriteType(SPRITE_LINE_BLOCK);
            pice3.setSpriteType(SPRITE_LINE_BLOCK);
            pice4.setSpriteType(SPRITE_LINE_BLOCK);

            break;
        case 6:
            //S block
            pice1.setImage(lightblue.getImage());
            pice2.setImage(lightblue.getImage());
            pice3.setImage(lightblue.getImage());
            pice4.setImage(lightblue.getImage());
            //set positions
            pice1.setPosition(new Point2D(START_X,        START_Y));
            pice2.setPosition(new Point2D(START_X+PICE_X, START_Y-PICE_Y));
            pice3.setPosition(new Point2D(START_X-PICE_X, START_Y));
            pice4.setPosition(new Point2D(START_X,        START_Y-PICE_Y));

            centerNext = new Point2D(START_X+PICE_X/2, START_Y+PICE_Y/2);

            pice1.setSpriteType(SPRITE_S_BLOCK);
            pice2.setSpriteType(SPRITE_S_BLOCK);
            pice3.setSpriteType(SPRITE_S_BLOCK);
            pice4.setSpriteType(SPRITE_S_BLOCK);

            break;
        case 7:
            //Z block
            pice1.setImage(darkred.getImage());
            pice2.setImage(darkred.getImage());
            pice3.setImage(darkred.getImage());
            pice4.setImage(darkred.getImage());
            //set positions
            pice1.setPosition(new Point2D(START_X,        START_Y));
            pice2.setPosition(new Point2D(START_X-PICE_X, START_Y-PICE_Y));
            pice3.setPosition(new Point2D(START_X+PICE_X, START_Y));
            pice4.setPosition(new Point2D(START_X,        START_Y-PICE_Y));

            centerNext = new Point2D(START_X+PICE_X/2, START_Y+PICE_Y/2);

            pice1.setSpriteType(SPRITE_Z_BLOCK);
            pice2.setSpriteType(SPRITE_Z_BLOCK);
            pice3.setSpriteType(SPRITE_Z_BLOCK);
            pice4.setSpriteType(SPRITE_Z_BLOCK);

            break;
        }
        //set velocity
        pice1.setVelocity(new Point2D(0, 0));
        pice2.setVelocity(new Point2D(0, 0));
        pice3.setVelocity(new Point2D(0, 0));
        pice4.setVelocity(new Point2D(0, 0));
        //move far right
        pice1.setPosition(new Point2D((int)pice1.position().X()+(PICE_X*9), (int)pice1.position().Y()+(PICE_Y)));
        pice2.setPosition(new Point2D((int)pice2.position().X()+(PICE_X*9), (int)pice2.position().Y()+(PICE_Y)));
        pice3.setPosition(new Point2D((int)pice3.position().X()+(PICE_X*9), (int)pice3.position().Y()+(PICE_Y)));
        pice4.setPosition(new Point2D((int)pice4.position().X()+(PICE_X*9), (int)pice4.position().Y()+(PICE_Y)));

        pice1.setFrameWidth(START_X);
        pice2.setFrameWidth(START_X);
        pice3.setFrameWidth(START_X);
        pice4.setFrameWidth(START_X);
        pice1.setFrameHeight(PICE_Y);
        pice2.setFrameHeight(PICE_Y);
        pice3.setFrameHeight(PICE_Y);
        pice4.setFrameHeight(PICE_Y);

        pice1.setPlay(false);
        pice2.setPlay(false);
        pice3.setPlay(false);
        pice4.setPlay(false);
        //add the new pieces to the sprite list
        sprites().add(pice1);
        sprites().add(pice2);
        sprites().add(pice3);
        sprites().add(pice4);

        //add to next sprite list
        NextSprites[0] = pice1;
        NextSprites[1] = pice2;
        NextSprites[2] = pice3;
        NextSprites[3] = pice4;
    }
    public void redyPiece() {
        AnimatedSprite pice1 = NextSprites[0];
        AnimatedSprite pice2 = NextSprites[1];
        AnimatedSprite pice3 = NextSprites[2];
        AnimatedSprite pice4 = NextSprites[3];

        CurrentSprites[0] = pice1;
        CurrentSprites[1] = pice2;
        CurrentSprites[2] = pice3;
        CurrentSprites[3] = pice4;

        pice1.setPlay(true);
        pice2.setPlay(true);
        pice3.setPlay(true);
        pice4.setPlay(true);

        pice1.setPosition(new Point2D((int)pice1.position().X()-(PICE_X*9), (int)pice1.position().Y()-(PICE_Y)));
        pice2.setPosition(new Point2D((int)pice2.position().X()-(PICE_X*9), (int)pice2.position().Y()-(PICE_Y)));
        pice3.setPosition(new Point2D((int)pice3.position().X()-(PICE_X*9), (int)pice3.position().Y()-(PICE_Y)));
        pice4.setPosition(new Point2D((int)pice4.position().X()-(PICE_X*9), (int)pice4.position().Y()-(PICE_Y)));

        center = new Point2D((int)centerNext.X(), (int)centerNext.Y());

        if ((Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y])||
                (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y])||
                (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y])||
                (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y])){
                    gameState=GAME_OVER;
        }
    }
    public void screenDown(int y){
        for(int n=0; n<sprites().size(); n++) {
            AnimatedSprite spr = (AnimatedSprite) sprites().get(n);
            if(spr.play()&&spr.alive()&&((int)spr.position().Y())<y*PICE_Y){
                Grid[(int)spr.position().X()/PICE_X][(int)spr.position().Y()/PICE_Y]=false;
                spr.setPosition(new Point2D((int)spr.position().X(),(int)spr.position().Y()+PICE_Y));
            }
        }
        for(int n=0; n<sprites().size(); n++) {
            AnimatedSprite spr = (AnimatedSprite) sprites().get(n);
            if(spr.play()&&spr.alive()){
                Grid[(int)spr.position().X()/PICE_X][(int)spr.position().Y()/PICE_Y]=true;
            }
        }
    }
    public void keepPiece () {
        if (gameState!=GAME_RUNNING) return;
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];

        pice1.setAlive(false);
        pice2.setAlive(false);
        pice3.setAlive(false);
        pice4.setAlive(false);

        AnimatedSprite nextPice1 = NextSprites[0];
        AnimatedSprite nextPice2 = NextSprites[1];
        AnimatedSprite nextPice3 = NextSprites[2];
        AnimatedSprite nextPice4 = NextSprites[3];

        createPiece(pice1.spriteType());

        AnimatedSprite keepPice1 = NextSprites[0];
        AnimatedSprite keepPice2 = NextSprites[1];
        AnimatedSprite keepPice3 = NextSprites[2];
        AnimatedSprite keepPice4 = NextSprites[3];

        keepPice1.setPosition(new Point2D((int)keepPice1.position().X(), (int)keepPice1.position().Y()+(PICE_Y*4)));
        keepPice2.setPosition(new Point2D((int)keepPice2.position().X(), (int)keepPice2.position().Y()+(PICE_Y*4)));
        keepPice3.setPosition(new Point2D((int)keepPice3.position().X(), (int)keepPice3.position().Y()+(PICE_Y*4)));
        keepPice4.setPosition(new Point2D((int)keepPice4.position().X(), (int)keepPice4.position().Y()+(PICE_Y*4)));

        if (kepedPiece){
            AnimatedSprite currPice1 = KeepSprites[0];
            AnimatedSprite currPice2 = KeepSprites[1];
            AnimatedSprite currPice3 = KeepSprites[2];
            AnimatedSprite currPice4 = KeepSprites[3];

            createPiece(currPice1.spriteType());
            redyPiece();

            currPice1.setAlive(false);
            currPice2.setAlive(false);
            currPice3.setAlive(false);
            currPice4.setAlive(false);
        }
        NextSprites[0] = nextPice1;
        NextSprites[1] = nextPice2;
        NextSprites[2] = nextPice3;
        NextSprites[3] = nextPice4;

        KeepSprites[0] = keepPice1;
        KeepSprites[1] = keepPice2;
        KeepSprites[2] = keepPice3;
        KeepSprites[3] = keepPice4;

        if (!kepedPiece){
            redyPiece();
            createPiece(0);
            kepedPiece = true;
            keep = true;
        }
    }
/******************************************************************
 * process keys that have been pressed
 *******************************************************************/
    public void checkInput() {
        if (gameState!=GAME_RUNNING) return;
        if (keyDown) softDrop();
        if (!keyDown) drop();
    }
    public void drop() {
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];
        drop++;
        if (drop>count){
            drop = 0;
            pice1.setPosition(new Point2D((int)pice1.position().X(), (int)pice1.position().Y()+PICE_Y));
            pice2.setPosition(new Point2D((int)pice2.position().X(), (int)pice2.position().Y()+PICE_Y));
            pice3.setPosition(new Point2D((int)pice3.position().X(), (int)pice3.position().Y()+PICE_Y));
            pice4.setPosition(new Point2D((int)pice4.position().X(), (int)pice4.position().Y()+PICE_Y));

            center = new Point2D((int)center.X(), (int)center.Y()+PICE_Y);

            if ((Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y])||
                    (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y])||
                    (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y])||
                    (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y])){
                pice1.setPosition(new Point2D((int)pice1.position().X(), (int)pice1.position().Y()-PICE_Y));
                pice2.setPosition(new Point2D((int)pice2.position().X(), (int)pice2.position().Y()-PICE_Y));
                pice3.setPosition(new Point2D((int)pice3.position().X(), (int)pice3.position().Y()-PICE_Y));
                pice4.setPosition(new Point2D((int)pice4.position().X(), (int)pice4.position().Y()-PICE_Y));
                Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y]=true;
                Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y]=true;
                Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y]=true;
                Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y]=true;
                checkLines();
                redyPiece();
                createPiece(0);
                keep = true;
            }
        }
    }
    public void softDrop (){
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];
        softDrop++;
        if (softDrop>2){
            softDrop = 0;
            bumpScore(3);
            pice1.setPosition(new Point2D((int)pice1.position().X(), (int)pice1.position().Y()+PICE_Y));
            pice2.setPosition(new Point2D((int)pice2.position().X(), (int)pice2.position().Y()+PICE_Y));
            pice3.setPosition(new Point2D((int)pice3.position().X(), (int)pice3.position().Y()+PICE_Y));
            pice4.setPosition(new Point2D((int)pice4.position().X(), (int)pice4.position().Y()+PICE_Y));

            center = new Point2D((int)center.X(), (int)center.Y()+PICE_Y);

            if ((Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y])||
                    (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y])||
                    (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y])||
                    (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y])){
                pice1.setPosition(new Point2D((int)pice1.position().X(), (int)pice1.position().Y()-PICE_Y));
                pice2.setPosition(new Point2D((int)pice2.position().X(), (int)pice2.position().Y()-PICE_Y));
                pice3.setPosition(new Point2D((int)pice3.position().X(), (int)pice3.position().Y()-PICE_Y));
                pice4.setPosition(new Point2D((int)pice4.position().X(), (int)pice4.position().Y()-PICE_Y));
                Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y]=true;
                Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y]=true;
                Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y]=true;
                Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y]=true;
                checkLines();
                redyPiece();
                createPiece(0);
                keep = true;
            }
        }
    }
    public void hardDrop (){
        if (gameState!=GAME_RUNNING) return;
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];
        boolean end = false;
        while (!end){
            bumpScore(5);
            pice1.setPosition(new Point2D((int)pice1.position().X(), (int)pice1.position().Y()+PICE_Y));
            pice2.setPosition(new Point2D((int)pice2.position().X(), (int)pice2.position().Y()+PICE_Y));
            pice3.setPosition(new Point2D((int)pice3.position().X(), (int)pice3.position().Y()+PICE_Y));
            pice4.setPosition(new Point2D((int)pice4.position().X(), (int)pice4.position().Y()+PICE_Y));

            center = new Point2D((int)center.X(), (int)center.Y()+PICE_Y);

            if ((Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y])||
                    (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y])||
                    (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y])||
                    (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y])){
                pice1.setPosition(new Point2D((int)pice1.position().X(), (int)pice1.position().Y()-PICE_Y));
                pice2.setPosition(new Point2D((int)pice2.position().X(), (int)pice2.position().Y()-PICE_Y));
                pice3.setPosition(new Point2D((int)pice3.position().X(), (int)pice3.position().Y()-PICE_Y));
                pice4.setPosition(new Point2D((int)pice4.position().X(), (int)pice4.position().Y()-PICE_Y));
                Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y]=true;
                Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y]=true;
                Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y]=true;
                Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y]=true;
                checkLines();
                redyPiece();
                createPiece(0);
                end = true;
                keep = true;
            }
        }
    }
    public void moveRight (){
        if (gameState!=GAME_RUNNING) return;
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];

        pice1.setPosition(new Point2D((int)pice1.position().X()+PICE_X, (int)pice1.position().Y()));
        pice2.setPosition(new Point2D((int)pice2.position().X()+PICE_X, (int)pice2.position().Y()));
        pice3.setPosition(new Point2D((int)pice3.position().X()+PICE_X, (int)pice3.position().Y()));
        pice4.setPosition(new Point2D((int)pice4.position().X()+PICE_X, (int)pice4.position().Y()));

        center = new Point2D((int)center.X()+PICE_X, (int)center.Y());

        if (Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y]){moveLeft();return;}
        if (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y]){moveLeft();return;}
        if (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y]){moveLeft();return;}
        if (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y]){moveLeft();return;}
    }
    public void moveLeft (){
        if (gameState!=GAME_RUNNING) return;
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];

        pice1.setPosition(new Point2D((int)pice1.position().X()-PICE_X, (int)pice1.position().Y()));
        pice2.setPosition(new Point2D((int)pice2.position().X()-PICE_X, (int)pice2.position().Y()));
        pice3.setPosition(new Point2D((int)pice3.position().X()-PICE_X, (int)pice3.position().Y()));
        pice4.setPosition(new Point2D((int)pice4.position().X()-PICE_X, (int)pice4.position().Y()));

        center = new Point2D((int)center.X()-PICE_X, (int)center.Y());

        if (Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y]){moveRight();return;}
        if (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y]){moveRight();return;}
        if (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y]){moveRight();return;}
        if (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y]){moveRight();return;}
    }
    public void rotateRight (){
        if (gameState!=GAME_RUNNING) return;
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];

        pice1.setPosition(new Point2D((int)(center.X()+(center.Y()-pice1.position().Y())-PICE_X), (int)(center.Y()+(pice1.position().X()-center.X()))));
        pice2.setPosition(new Point2D((int)(center.X()+(center.Y()-pice2.position().Y())-PICE_X), (int)(center.Y()+(pice2.position().X()-center.X()))));
        pice3.setPosition(new Point2D((int)(center.X()+(center.Y()-pice3.position().Y())-PICE_X), (int)(center.Y()+(pice3.position().X()-center.X()))));
        pice4.setPosition(new Point2D((int)(center.X()+(center.Y()-pice4.position().Y())-PICE_X), (int)(center.Y()+(pice4.position().X()-center.X()))));

        if (Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y]){rotateLeft();return;}
        if (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y]){rotateLeft();return;}
        if (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y]){rotateLeft();return;}
        if (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y]){rotateLeft();return;}
    }
    public void rotateLeft (){
        if (gameState!=GAME_RUNNING) return;
        AnimatedSprite pice1 = CurrentSprites[0];
        AnimatedSprite pice2 = CurrentSprites[1];
        AnimatedSprite pice3 = CurrentSprites[2];
        AnimatedSprite pice4 = CurrentSprites[3];

        pice1.setPosition(new Point2D((int)(center.X()-(center.Y()-pice1.position().Y())), (int)(center.Y()-(pice1.position().X()-center.X())-PICE_X)));
        pice2.setPosition(new Point2D((int)(center.X()-(center.Y()-pice2.position().Y())), (int)(center.Y()-(pice2.position().X()-center.X())-PICE_X)));
        pice3.setPosition(new Point2D((int)(center.X()-(center.Y()-pice3.position().Y())), (int)(center.Y()-(pice3.position().X()-center.X())-PICE_X)));
        pice4.setPosition(new Point2D((int)(center.X()-(center.Y()-pice4.position().Y())), (int)(center.Y()-(pice4.position().X()-center.X())-PICE_X)));

        if (Grid[(int)pice1.position().X()/PICE_X][(int)pice1.position().Y()/PICE_Y]){rotateRight();return;}
        if (Grid[(int)pice2.position().X()/PICE_X][(int)pice2.position().Y()/PICE_Y]){rotateRight();return;}
        if (Grid[(int)pice3.position().X()/PICE_X][(int)pice3.position().Y()/PICE_Y]){rotateRight();return;}
        if (Grid[(int)pice4.position().X()/PICE_X][(int)pice4.position().Y()/PICE_Y]){rotateRight();return;}
    }
/*******************************************************************
 * keep track of high score
 *******************************************************************/
    public void bumpScore(int howmuch) {
        score += howmuch;
        scoreCount +=howmuch;
        if (score>highscore)
            highscore =score;
        if (scoreCount>=1500){
            scoreCount = 0;
            level++;
            speed++;
            count = FRAMERATE/speed;
        }
    }
}