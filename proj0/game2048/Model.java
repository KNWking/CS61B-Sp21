package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author KNWking
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;
        boolean[][] ifMerged = new boolean[4][4];

        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        if (side != Side.NORTH) {
            board.setViewingPerspective(side);
        }
        int len = board.size();


        /*  I think the following code should use a for loop to check
         *  until it encounters a boundary.
         *  This way, if the board expands further, there won't be a need for restructuring.
         *  I didn't write it that way because I was already tired.
         */

        // check row 2.
        int nowRow = 2;
        for (int nowCol = 0; nowCol < len; ++nowCol) {
            Tile tile = board.tile(nowCol, nowRow);
            if (tile != null) {
                if (aboveNullExist(nowCol, nowRow) || aboveNTileValueEqual(nowCol, nowRow, tile, 1)) {
                    changed = true;
                    boolean ifChangeScore = board.move(nowCol, nowRow + 1, tile);
                    if (ifChangeScore) {
                        score += board.tile(nowCol, nowRow + 1).value();
                        ifMerged[nowCol][nowRow + 1] = true;
                    }
                }
            }
        }
        //  check row 1.
        nowRow = 1;
        for (int nowCol = 0; nowCol < len; ++nowCol) {
            Tile tile = board.tile(nowCol, nowRow);
            if (tile != null) {
                if (aboveNullExist(nowCol, nowRow)) {
                    changed = true;
                    if (aboveNullExist(nowCol, nowRow + 1)) {
                        board.move(nowCol, nowRow + 2, tile);
                    } else if (aboveNTileValueEqual(nowCol, nowRow, tile, 2) && !ifMerged[nowCol][nowRow + 2]) {
                        board.move(nowCol, nowRow + 2, tile);
                        score += board.tile(nowCol, nowRow + 2).value();
                        ifMerged[nowCol][nowRow + 2] = true;
                    } else {
                        board.move(nowCol, nowRow + 1, tile);
                    }
                } else if (aboveNTileValueEqual(nowCol, nowRow, tile, 1) && !ifMerged[nowCol][nowRow + 1]) {
                    changed = true;
                    board.move(nowCol, nowRow + 1, tile);
                    score += board.tile(nowCol, nowRow + 1).value();
                    ifMerged[nowCol][nowRow + 1] = true;
                }
            }
        }

        // check row 0.
        nowRow = 0;
        for (int nowCol = 0; nowCol < len; ++nowCol) {
            Tile tile = board.tile(nowCol, nowRow);
            if (tile != null) {
                if (aboveNullExist(nowCol, nowRow)) {
                    changed = true;
                    if (aboveNullExist(nowCol, nowRow + 1)) {
                        if (aboveNullExist(nowCol, nowRow + 2)) {
                            board.move(nowCol, nowRow + 3, tile);
                        } else if (aboveNTileValueEqual(nowCol, nowRow, tile, 3) && !ifMerged[nowCol][nowRow + 3]) {
                            board.move(nowCol, nowRow + 3, tile);
                            score += board.tile(nowCol, nowRow + 3).value();
                            ifMerged[nowCol][nowRow + 3] = true;
                        } else {
                            board.move(nowCol, nowRow + 2, tile);
                        }
                    } else if (aboveNTileValueEqual(nowCol, nowRow, tile, 2) && !ifMerged[nowCol][nowRow + 2]) {
                        board.move(nowCol, nowRow + 2, tile);
                        score += board.tile(nowCol, nowRow + 2).value();
                        ifMerged[nowCol][nowRow + 2] = true;
                    } else {
                        board.move(nowCol, nowRow + 1, tile);
                    }
                } else if (aboveNTileValueEqual(nowCol, nowRow, tile, 1) && !ifMerged[nowCol][nowRow + 1]) {
                    changed = true;
                    board.move(nowCol, nowRow + 1, tile);
                    score += board.tile(nowCol, nowRow + 1).value();
                    ifMerged[nowCol][nowRow + 1] = true;
                }
            }
        }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        board.setViewingPerspective(Side.NORTH);
        return changed;
    }

    /** Return true if there is an empty tile above [row][col] exist. */
    private boolean aboveNullExist(int col, int row) {
        Tile aboveTile = board.tile(col, row + 1);
        return aboveTile == null;
    }

    /** Return true if the Nth above tile is equal with the given one. */
    private boolean aboveNTileValueEqual(int col, int row, Tile tile, int n) {
        int tileValue = tile.value();
        Tile aboveTile = board.tile(col, row + n);
        if (aboveTile != null && aboveTile.value() == tileValue) {
            return true;
        }
        return false;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        int len = b.size();
        for (int i = 0; i < len; ++i) {
            for (int j = 0; j < len; ++j) {
                Tile tile = b.tile(i, j);
                if (tile == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        int len = b.size();
        for (int i = 0; i < len; ++i) {
            for (int j = 0; j < len; ++j) {
                Tile tile = b.tile(i, j);
                if (tile != null && tile.value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        return atLeastOneEmpty(b) || twoAdjacentTilesSameValue(b);
    }

    /** Returns true if there is at least one empty space on the board. */
    private static boolean atLeastOneEmpty(Board b) {
        int len = b.size();
        for (int i = 0; i < len; ++i) {
            for (int j = 0; j < len; ++j) {
                Tile tile = b.tile(i, j);
                if (tile == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return true if there are two adjacent tiles with the same value. */
    private static boolean twoAdjacentTilesSameValue(Board b) {
        int len = b.size();
        for (int i = 0; i < len; ++i) {
            for (int j = 0; j < len; ++j) {
                Tile tile = b.tile(i, j);
                if (tile != null) {
                    int tileValue = tile.value();
                    /* Code below is unnecessary, since when you traverse the matrix,
                    *  you do it from left to right, and from top to bottom.
                    *  So actually when you access a cell in the matrix,
                    *  you have effectively already checked its left and upper neighbors.
                    *  if (i - 1 >= 0) {
                    *      Tile aboveTile = b.tile(i - 1, j);
                    *      if (aboveTile.value() == tileValue) {
                    *          return true;
                    *      }
                    *  }
                    *  if (j - 1 >= 0) {
                    *      Tile leftTile = b.tile(i, j - 1);
                    *      if (leftTile.value() == tileValue) {
                    *          return true;
                    *      }
                    *  }
                    */
                    if (i + 1 < len) {
                        Tile bottomTile = b.tile(i + 1, j);
                        if (bottomTile.value() == tileValue) {
                            return true;
                        }
                    }
                    if(j + 1 < len) {
                        Tile rightTile = b.tile(i, j + 1);
                        if (rightTile.value() == tileValue) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
