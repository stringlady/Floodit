import java.util.*;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents a single square of the game area
class Cell {

  // In logical coordinates, with the origin at the top-left corner of the screen
  //int x;
  //int y;
  static int CELL_SIZE = 25;
  Color color;
  boolean flooded;

  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  
  Cell(Color color, boolean flooded) {
    this.color = color;
    this.flooded = false;
    this.left = null;
    this.right = null;
    this.top = null;
    this.bottom = null;
  }
  
  // draws the cell
  WorldImage draw() {
    WorldImage c = new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, this.color);
    return c;
  }
  
  // adds this cell and its adjacent flooded, non-null cells to a given worklist queue
  void workload(Deque<Cell> worklist) {
    if (this.flooded) {
      if (! worklist.contains(this)) {
        worklist.add(this);
      }
      
      if (this.right != null && ! worklist.contains(this.right) && this.right.flooded) {
        worklist.add(this.right);
      }
      
      if (this.bottom != null && ! worklist.contains(this.bottom) && this.bottom.flooded) {
        worklist.add(this.bottom);
      }
      
      if (this.left != null && ! worklist.contains(this.left) && this.left.flooded) {
        worklist.add(this.left);
      }
      
      if (this.top != null && ! worklist.contains(this.top) && this.top.flooded) {
        worklist.add(this.top);
      }
    }
  }
  
  
  // updates this cells adjacent cells to flooded if they match the given color
  void flood(Color color) {
    if (this.flooded) {
      if (this.left != null && color.equals(this.left.color) && ! this.left.flooded) {
        this.left.flooded = true;
      }
      if (this.top != null && color.equals(this.top.color)  && ! this.top.flooded) {
        this.top.flooded = true;
      }
      if (this.right != null && color.equals(this.right.color)  && ! this.right.flooded) {
        this.right.flooded = true;
      }
      if (this.bottom != null && color.equals(this.bottom.color)  && ! this.bottom.flooded) {
        this.bottom.flooded = true;
      }
    }
  }  
}


// The configured world of the flood it game
class FloodItWorld extends World {

  // All the cells of the game
  Cell[][] board; // changed board to 2D array as I felt it would be a better design decision
  
  static int BOARD_SIZE = 22; // set size of game
  static ArrayList<Color> differentColors = new ArrayList<>(
      Arrays.asList(Color.red, Color.green, Color.blue, Color.pink, Color.magenta,
          Color.cyan, Color.yellow)); // array list of different colors
  
  Random rand;
  Color floodColor;
  Deque<Cell> worklist;
  Boolean flooding;
  Integer clicks;
  Integer maxClicks;
  Integer time;
  
  FloodItWorld() {
    this.board = new Cell[BOARD_SIZE][BOARD_SIZE];
    this.rand = new Random(); // random value
    this.floodColor = null;
    this.worklist = new LinkedList<Cell>();
    this.flooding = false;
    this.clicks = 0;
    this.maxClicks = BOARD_SIZE + Math.floorDiv(BOARD_SIZE, 2);
    this.time = 0;
    init();
  }
  
  FloodItWorld(Random rand) {
    this.board = new Cell[BOARD_SIZE][BOARD_SIZE];
    this.rand = rand;
    this.floodColor = null;
    this.worklist = new LinkedList<Cell>();
    this.flooding = false;
    this.clicks = 0;
    this.maxClicks = BOARD_SIZE + Math.floorDiv(BOARD_SIZE, 2);
    this.time = 0;
    init();
  }
  
  // initialize the board of cells
  void init() {
    // loops where i represents the y axis and j the x axis
    // purpose: loop to create a new cell with a random color in accordance to board size
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        board[i][j] = new Cell(differentColors.get(rand.nextInt(differentColors.size())), false);
      }
    }
    
    // purpose: loop to connect each cell with its adjacent cells
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        if (j - 1 >= 0) {
          board[i][j].left = board[i][j - 1];
        }
        if (j + 1 < board[i].length) {
          board[i][j].right = board[i][j + 1];
        }
        if (i - 1 >= 0) {
          board[i][j].top = board[i - 1][j];
        }
        if (i + 1 < board.length) {
          board[i][j].bottom = board[i + 1][j];
        }
      }
    } 
    
    this.board[0][0].flooded = true;
    this.floodColor = this.board[0][0].color;
    this.clicks = 0;
    this.time = 0;
  }
  
  // creates the scene for the flood it world
  public WorldScene makeScene() {
    WorldScene world = new WorldScene(FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE, 
        (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE));
    
    // purpose: loop to draw and place each cell into the world scene
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        world.placeImageXY(
            this.board[i][j].draw().movePinhole(-0.5 * Cell.CELL_SIZE, 
            -0.5 * Cell.CELL_SIZE), j * Cell.CELL_SIZE, i * Cell.CELL_SIZE);
      }
    }
    
    world.placeImageXY(
        new TextImage(this.clicks.toString() + "/" 
        + this.maxClicks.toString(), 24, FontStyle.BOLD, Color.BLACK),
        50, 10);
    
    world.placeImageXY(
        new TextImage("Time: " 
        + String.valueOf(Math.floorDiv(this.time, 28)), 24, FontStyle.BOLD, Color.BLACK),
        world.width - 75, 10);
    
    return world;
  }
  
  
  public WorldEnd worldEnds() {
    WorldScene gameover = new WorldScene(
        FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE, 
        FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE);
    gameover.placeImageXY(
        new TextImage("GAME OVER", 50 ,FontStyle.BOLD, Color.RED),
        gameover.width / 2, gameover.height / 2);
    
    WorldScene win = new WorldScene(
        FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE, 
        FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE);
    win.placeImageXY(
        new TextImage("WINNER", 50 ,FontStyle.BOLD, Color.RED),
        win.width / 2, gameover.height / 2);
    
    if (this.clicks == this.maxClicks) {
      return new WorldEnd(true, gameover);
    } else if (this.fullFlood()) {
      return new WorldEnd(true, win);
    } else {
      return new WorldEnd(false, this.makeScene());
    }
  }
  
  // checks if board is fully flooded
  public boolean fullFlood() {
    boolean result = true;
    int i = 0;
    int j = 0;
    
    while (result && i < BOARD_SIZE && j <= BOARD_SIZE) {
      if (! this.board[i][j].color.equals(this.floodColor)) {
        result = false;
      }
      if (j < BOARD_SIZE) {
        j++;
      } else if (j >= BOARD_SIZE) {
        i++;
        j = 0;
      }
    }
    return result;  
  }
  
  // key event for the the game: if r is prested the game will reset
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      init();
      new WorldEnd(false, this.makeScene());
    }
  }
  
  // on click event that controls what color the scene will flood to
  public void onMouseClicked(Posn pos) {
    int row = Math.floorDiv(pos.y, Cell.CELL_SIZE);
    int col = Math.floorDiv(pos.x, Cell.CELL_SIZE);
    
    this.floodColor = this.board[row][col].color;

    
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        this.board[i][j].flood(this.board[0][0].color);
        this.board[i][j].workload(worklist);
      }
    }
    this.flooding = true;
    this.clicks++;
  }
  
  // updates the flooding cells if game is currently flooding
  public void onTick() {
    if (this.flooding) {
      if (this.worklist.size() == 0) {
        this.flooding = false;
      } else {
        worklist.pollFirst().color = this.floodColor;
      }
    }
    this.time++;
  }
}


// examples of flood it world
class ExamplesFloodIt {
  FloodItWorld board1 = new FloodItWorld();
  FloodItWorld board2 = new FloodItWorld(new Random(10));
  Cell c1 = new Cell(Color.blue, true);
  Cell c2 = new Cell(Color.green, false);
  
  // test draw method
  boolean testDraw(Tester t) {
    return t.checkExpect(c1.draw(), new RectangleImage(25, 25, OutlineMode.SOLID, Color.blue));
  }
  
  // test make scene method
  boolean testMakeScene(Tester t) {
    WorldScene world = new WorldScene(550, 550);
    for (int i = 0; i < board1.board.length; i++) {
      for ( int j = 0; j < board1.board[i].length; j++) {
        world.placeImageXY(
            board1.board[i][j].draw()
            .movePinhole(-0.5 * 25, -0.5 * 25), j * 25, i * 25);
      }
    }
    
    world.placeImageXY(
        new TextImage("0/33", 24, FontStyle.BOLD, Color.BLACK),
        50, 10);
    
    world.placeImageXY(
        new TextImage("Time: 0", 24, FontStyle.BOLD, Color.BLACK),
        world.width - 75, 10);
    
    return t.checkExpect(board1.makeScene(), world);
  }
  
  // test connection a cell to the left
  boolean testConnectionLeft(Tester t) {
    return t.checkExpect(board1.board[0][0].left, null)
        && t.checkExpect(board1.board[2][4].left, board1.board[2][3])
        && t.checkExpect(board1.board[21][0].left, null);
  }
  
  // test connection a cell to the right
  boolean testConnectionRight(Tester t) {
    return t.checkExpect(board1.board[0][21].right, null)
        && t.checkExpect(board1.board[5][1].right, board1.board[5][2])
        && t.checkExpect(board1.board[21][21].right, null);
  }
  
  // test connection a cell to the top
  boolean testConnectionTop(Tester t) {
    return t.checkExpect(board1.board[0][0].top, null)
        && t.checkExpect(board1.board[3][7].top, board1.board[2][7])
        && t.checkExpect(board1.board[0][21].top, null);
  }
  
  // test connection a cell to the bottom
  boolean testConnectionBottom(Tester t) {
    return t.checkExpect(board1.board[21][0].bottom, null)
        && t.checkExpect(board1.board[12][20].bottom, board1.board[13][20])
        && t.checkExpect(board1.board[21][21].bottom, null);
  }
  
  // testing that randomness
  boolean testRandomness(Tester t) {
    return t.checkExpect(board1.rand, board2.rand);
  }
  
  // test big bang
  void testBigBang(Tester t) {
    FloodItWorld game = new FloodItWorld();
    game.bigBang(FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE, 
        FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE, 1.0 / 28.0);
  }
}