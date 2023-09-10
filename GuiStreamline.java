/*
 Author: Jose Falconi
 Email: jfalconi@ucsd.edu
 CS8B Login: cs8bwaaw
 Date: 3/6/19
 File: GuiStreamline.java
 Sources of Help: piazza, tutors, PSA6 writeup
 
 This file is meant to create an interface, using  GUI's to play
 our game Streamline.
 It is the solution to CSE8B PSA6.
 */

//Keep these lines. Tells java what classes it needs.
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.animation.*;
import javafx.animation.PathTransition.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import javafx.util.Duration;

/**
 * This class has methods that create a visualization of our game Streamline
 * and it has methods that allow for the scene to reset, update, and applys
 * backend logic of the game.
 */
public class GuiStreamline extends Application {
    static final double SCENE_WIDTH = 500;
    static final double SCENE_HEIGHT = 600;
    static final String TITLE = "CSE 8b Streamline GUI";
    static final String USAGE = 
        "Usage: \n" + 
        "> java GuiStreamline               - to start a game with default" +
        " size 6*5 and random obstacles\n" + 
        "> java GuiStreamline <filename>    - to start a game by reading g" +
        "ame state from the specified file\n" +
        "> java GuiStreamline <directory>   - to start a game by reading a" +
        "ll game states from files in\n" +
        "                                     the specified directory and " +
        "playing them in order\n";

    static final Color TRAIL_COLOR = Color.PALEVIOLETRED;
    static final Color GOAL_COLOR = Color.MEDIUMAQUAMARINE;
    static final Color OBSTACLE_COLOR = Color.DIMGRAY;
    //Chars represent obstacles, and trails
    private static final char TRAIL_CHAR = '.';
    private static final char OBSTACLE_CHAR = 'X';
    private static final char EMPTY_SPACE = ' ';
    // Trail radius will be set to this fraction of the size of a board square.
    static final double TRAIL_RADIUS_FRACTION = 0.1;

    // Squares will be resized to this fraction of the size of a board square.
    static final double SQUARE_FRACTION = 0.8;

    Scene mainScene;
    Group levelGroup;                   // For obstacles and trails
    Group rootGroup;                    // Parent group for everything else
    Player playerRect;                  // GUI representation of the player
    RoundedSquare goalRect;             // GUI representation of the goal

    Shape[][] grid;                     // Same dimensions as the game board

    Streamline game;                    // The current level
    ArrayList<Streamline> nextGames;    // Future levels

    MyKeyHandler myKeyHandler;          // for keyboard input

    /**
     * getter for Board Width
     * @return width of board, type int
     */
    // returns the width of the board for the current level
    public int getBoardWidth() {
        return game.currentState.board[0].length;
    }
    
    /**
     * getter for Board Height
     * @return height of the board, type int
     */
    // the height of the board for the current level
    public int getBoardHeight() {
        return game.currentState.board.length;
    }

    /**
     * getter for size of Square
     * @return square size, type int
     */
    // Find a size for a single square of the board that will fit nicely
    // in the current scene size.
    public double getSquareSize() {
        /* For example, given a scene size of 1000 by 600 and a board size
           of 5 by 6, we have room for each square to be 200x100. Since we
           want squares not rectangles, return the minimum which is 100 
           in this example. */
        double squareWidth = mainScene.getWidth() / getBoardWidth();
        double squareHeight = mainScene.getHeight() / getBoardHeight();
        if( squareWidth < squareHeight ){
            return squareWidth;
        }
        return squareHeight;
    }

    /**
     * Resets the grid back to default with all transparent trail dots
     * at every empty space, colored trail dots at trail chars, and obstacles
     * at obstacle chars.
     * @param none
     * @return none, changes grid
     */
    // Destroy and recreate grid and all trail and obstacle shapes.
    // Assumes the dimensions of the board may have changed.
    public void resetGrid() {
        levelGroup.getChildren().clear();
        grid = new Shape[getBoardHeight()][getBoardWidth()];
        for(int row = 0; row < getBoardHeight(); row++){
            for(int col = 0; col < getBoardWidth(); col++){
                //gets location of on grid by using col and row
                double[] shapeLocation = boardIdxToScenePos(col,row);
                //Adds obstacle
                if(game.currentState.board[row][col] == OBSTACLE_CHAR){
                    Shape obstacle = new RoundedSquare(shapeLocation[0],
                            shapeLocation[1], SQUARE_FRACTION * 
                            getSquareSize());
                    grid[row][col] = obstacle;
                }
                //Adds transparent trail dots
                else if (game.currentState.board[row][col] == EMPTY_SPACE){
                    Shape trailCircle = new Circle(shapeLocation[0],
                            shapeLocation[1],TRAIL_RADIUS_FRACTION
                            *getSquareSize(),null);
                    grid[row][col] = trailCircle;
                }
                //Adds colored trail dots
                else if(game.currentState.board[row][col] == TRAIL_CHAR){
                    Shape trailCircle = new Circle(shapeLocation[0],
                            shapeLocation[1],TRAIL_RADIUS_FRACTION
                            * getSquareSize(),TRAIL_COLOR);
                    grid[row][col] = trailCircle;
                }
                //Adds objects to group class
                levelGroup.getChildren().add(grid[row][col]);
            }
        }
        // Hints: Empty out levelGroup and recreate grid.
        // Also makes sure grid is the right size, in case the size of the
        // board changed.

    }
    /**
     * updates the Trail color from transparent to colored.
     * @param none
     * @return none, changes the trail objects color once they are a trail and
     * not an empty space on the gamestate board.
     */
    // Sets the fill color of all trail Circles making them visible or not
    // depending on if that board position equals TRAIL_CHAR.
    public void updateTrailColors() {
        for(int row = 0; row < getBoardHeight(); row++){
            for(int col = 0; col < getBoardWidth(); col++){
                if(game.currentState.board[row][col] == TRAIL_CHAR){
                    grid[row][col].setFill(TRAIL_COLOR);
                }
                if(game.currentState.board[row][col] == EMPTY_SPACE){
                    grid[row][col].setFill(null);
                }
            }
        }
    }
    /** 
     * Coverts the given board column and row into scene coordinates.
     * Gives the center of the corresponding tile.
     * 
     * @param boardCol a board column to be converted to a scene x
     * @param boardRow a board row to be converted to a scene y
     * @return scene coordinates as length 2 array where index 0 is x
     */
    static final double MIDDLE_OFFSET = 0.5;
    public double[] boardIdxToScenePos (int boardCol, int boardRow) {
        double sceneX = ((boardCol + MIDDLE_OFFSET) * 
                (mainScene.getWidth() - 1)) / getBoardWidth();
        double sceneY = ((boardRow + MIDDLE_OFFSET) * 
                (mainScene.getHeight() - 1)) / getBoardHeight();
        return new double[]{sceneX, sceneY};
    }

    /**
     * Updates the player location when its col and row has been changed
     * @param fromCol,fromRow,toCol,toRow types int
     * @return none, updates the location of the player object and updates the
     * trail colors
     */
    // Makes trail markers visible and changes player position.
    // To be called when the user moved the player and the GUI needs to be 
    // updated to show the new position.
    // Parameters are the old position, new position, and whether it was an
    // undo movement.
    public void onPlayerMoved(int fromCol, int fromRow, int toCol, int toRow, 
            boolean isUndo)
    {
        // If the position is the same, just return
        if (fromCol == toCol && fromRow == toRow) {
            return;
        }
        //Changes colors of trail dots
        updateTrailColors();
        double[] playerLocation = boardIdxToScenePos(toCol,toRow);
        playerRect.setCenterX(playerLocation[0]);
        playerRect.setCenterY(playerLocation[1]);
        //ends game if level is passed
        if(game.currentState.levelPassed == true){
            onLevelFinished();
            return;
        }

    }
    
    /**
     * Changes the gamestate if a certain keyCode is inserted
     * @param keycode type KeyCode
     * @return none, updates the board
     */
    // To be called when a key is pressed
    void handleKeyCode(KeyCode keyCode) {

        /*TODO*/
        int prevCol = game.currentState.playerCol;
        int prevRow = game.currentState.playerRow;
        switch (keyCode) {
            /*TODO*/
            case UP:
                game.recordAndMove(Direction.UP); 
                break;
            case LEFT:
                game.recordAndMove(Direction.LEFT);
                break;
            case DOWN:
                game.recordAndMove(Direction.DOWN);
                break;
            case RIGHT:
                game.recordAndMove(Direction.RIGHT);
                break;
            case U:
                game.undo();
                break;
            case O:
                game.saveToFile();
                break;
            case Q:
                System.exit(0);

            default:
                System.out.println("Possible commands:\n w - up\n " + 
                        "a - left\n s - down\n d - right\n u - undo\n " + 
                        "q - quit level");
                break;
        }
        
        onPlayerMoved(prevCol,prevRow,game.currentState.playerCol,
                game.currentState.playerRow,game.currentState.levelPassed);
        // Call onPlayerMoved() to update the GUI to reflect the player's 
        // movement (if any)
    }

    // This nested class handles keyboard input and calls handleKeyCode()
    /** 
     * This class is meant to override the handle method that intakes user
     * input
     */
    class MyKeyHandler implements EventHandler<KeyEvent> {
        @Override
            /**
             * Gets the user input and calls handleKeyCode using the input as
             * its paramater
             * @param e type KeyEvent
             * @return none
             */
            public void handle(KeyEvent e) {
                handleKeyCode(e.getCode());
            }
    }

    /**
     * Resets the grid back to normal and initializes the goalRect and
     * playerRect objects
     * @param none
     * @return none, updates the player and goal objects after resetting the
     * grid
     */
    // To be called whenever the UI needs to be completely redone to reflect
    // a new level
    public void onLevelLoaded() {
        resetGrid();

        double squareSize = getSquareSize() * SQUARE_FRACTION;

        // Update the player position
        double[] playerPos = boardIdxToScenePos(
                game.currentState.playerCol, game.currentState.playerRow
                );
        playerRect.setSize(squareSize);
        playerRect.setCenterX(playerPos[0]);
        playerRect.setCenterY(playerPos[1]);

        //update the goal position
        double[] goalPos = boardIdxToScenePos(
                game.currentState.goalCol, game.currentState.goalRow);
        goalRect.setSize(squareSize);
        goalRect.setCenterX(goalPos[0]);
        goalRect.setCenterY(goalPos[1]);
    }

    // Called when the player reaches the goal. Shows the winning animation
    // and loads the next level if there is one.
    static final double SCALE_TIME = 175;  // milliseconds for scale animation
    static final double FADE_TIME = 250;   // milliseconds for fade animation
    static final double DOUBLE_MULTIPLIER = 2;
    /**
     * Creates the animation that signifies the level was passed
     * @param none
     * @return none, it ends the game
     */
    public void onLevelFinished() {
        // Clone the goal rectangle and scale it up until it covers the screen

        // Clone the goal rectangle
        Rectangle animatedGoal = new Rectangle(
                goalRect.getX(),
                goalRect.getY(),
                goalRect.getWidth(),
                goalRect.getHeight()
                );
        animatedGoal.setFill(goalRect.getFill());

        // Add the clone to the scene
        List<Node> children = rootGroup.getChildren();
        children.add(children.indexOf(goalRect), animatedGoal);

        // Create the scale animation
        ScaleTransition st = new ScaleTransition(
                Duration.millis(SCALE_TIME), animatedGoal
                );
        st.setInterpolator(Interpolator.EASE_IN);

        // Scale enough to eventually cover the entire scene
        st.setByX(DOUBLE_MULTIPLIER * 
                mainScene.getWidth() / animatedGoal.getWidth());
        st.setByY(DOUBLE_MULTIPLIER * 
                mainScene.getHeight() / animatedGoal.getHeight());

        /*
         * This will be called after the scale animation finishes.
         * If there is no next level, quit. Otherwise switch to it and
         * fade out the animated cloned goal to reveal the new level.
         */
        st.setOnFinished(e1 -> {

                /* TODO: check if there is no next game and if so, quit */
                if(nextGames.isEmpty() == true){
                    System.exit(0);
                }else{
                    game = nextGames.get(0);
                    nextGames.remove(0);
                }
                /* TODO: update the instances variables game and nextGames 
                   to switch to the next level */

                // Update UI to the next level, but it won't be visible yet
                // because it's covered by the animated cloned goal
                onLevelLoaded();

                /* TODO: use a FadeTransition on animatedGoal, with FADE_TIME as
                   the duration. Use setOnFinished() to schedule code to
                   run after this animation is finished. When the animation
                   finishes, remove animatedGoal from rootGroup. */
                FadeTransition fade = new FadeTransition(
                    Duration.millis(FADE_TIME),goalRect);
                fade.play();
                rootGroup.getChildren().remove(animatedGoal);
                });

        // Start the scale animation
        st.play();
    }

    /** 
     * Performs file IO to populate game and nextGames using filenames from
     * command line arguments.
     */
    public void loadLevels() {
        game = null;
        nextGames = new ArrayList<Streamline>();

        List<String> args = getParameters().getRaw();
        if (args.size() == 0) {
            System.out.println("Starting a default-sized random game...");
            game = new Streamline();
            return;
        }

        // at this point args.length == 1

        File file = new File(args.get(0));
        if (!file.exists()) {
            System.out.printf("File %s does not exist. Exiting...", 
                    args.get(0));
            return;
        }

        // if is not a directory, read from the file and start the game
        if (!file.isDirectory()) {
            System.out.printf("Loading single game from file %s...\n", 
                    args.get(0));
            game = new Streamline(args.get(0));
            return;
        }

        // file is a directory, walk the directory and load from all files
        File[] subfiles = file.listFiles();
        Arrays.sort(subfiles);
        for (int i=0; i<subfiles.length; i++) {
            File subfile = subfiles[i];

            // in case there's a directory in there, skip
            if (subfile.isDirectory()) continue;

            // assume all files are properly formatted games, 
            // create a new game for each file, and add it to nextGames
            System.out.printf("Loading game %d/%d from file %s...\n",
                    i+1, subfiles.length, subfile.toString());
            nextGames.add(new Streamline(subfile.toString()));
        }

        // Switch to the first level
        game = nextGames.get(0);
        nextGames.remove(0);
    }

    /**
     * The main entry point for all JavaFX Applications
     * Initializes instance variables, creates the scene, and sets up the UI
     */
    @Override
        public void start(Stage primaryStage) throws Exception {
            // Populate game and nextGames
            loadLevels();

            // Initialize the scene and our groups
            rootGroup = new Group();
            mainScene = new Scene(rootGroup, SCENE_WIDTH, SCENE_HEIGHT, 
                    Color.GAINSBORO);
            levelGroup = new Group();
            rootGroup.getChildren().add(levelGroup);

            //TODO: initialize goalRect and playerRect, add them to rootGroup,
            //      call onLevelLoaded(), and set up keyboard input handling
            double[] goalPosition = boardIdxToScenePos(
                    game.currentState.goalCol, game.currentState.goalRow);
            goalRect = new RoundedSquare();
            goalRect.setFill(GOAL_COLOR);
            goalRect.setCenterX(goalPosition[0]);
            goalRect.setCenterY(goalPosition[1]);
            goalRect.setSize(getSquareSize());
            rootGroup.getChildren().add(goalRect);
            
            double[] playerPosition = boardIdxToScenePos(
                    game.currentState.playerCol,game.currentState.playerRow);
            playerRect = new Player();
            playerRect.setCenterX(playerPosition[0]);
            playerRect.setCenterY(playerPosition[1]);
            playerRect.setSize(getSquareSize());
            rootGroup.getChildren().add(playerRect);
            onLevelLoaded();
            // Make the scene visible
            primaryStage.setTitle(TITLE);
            primaryStage.setScene(mainScene);
            primaryStage.setResizable(false);
            primaryStage.show();

            myKeyHandler = new MyKeyHandler();
            mainScene.setOnKeyPressed(myKeyHandler);
        }

    /** 
     * Execution begins here, but at this point we don't have a UI yet
     * The only thing to do is call launch() which will eventually result in
     * start() above being called.
     */
    public static void main(String[] args) {
        if (args.length != 0 && args.length != 1) {
            System.out.print(USAGE);
            return;
        }

        launch(args);
    }
}
