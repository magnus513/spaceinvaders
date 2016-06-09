package se.magnus.spaceinvaders;

import javax.microedition.lcdui.game.TiledLayer;

/**
 * @author magnus
 */
public class Field extends TiledLayer
{

    //private static final int WIDTH_IN_TILES = 12;
    //private static final int HEIGHT_IN_TILES = 12;
    //private static final int TILE_WIDTH = 16;
    //private static final int TILE_HEIGHT = 16;
    //private static final int WIDTH_IN_TILES = 176;
    //private static final int HEIGHT_IN_TILES = 220;
    private static final int BACKGROUND_WIDTH = 176;
    private static final int BACKGROUND_HEIGHT = 220;

    private static int[][] cellTiles =
        {{-3, -2, -3, -1, -2, -1, -3, -1, -2, -3, -1, -2},
         {-2,  3,  4,  3,  1,  2,  3,  2,  1,  5,  2, -3},
         {-1,  2,  1,  2,  3,  4,  5,  3,  2,  4,  3, -1},
         {-2,  1,  4,  9,  9,  9,  9,  4,  5,  2,  1, -2},
         {-3,  3,  5,  9, 10, 10, 10,  2,  1,  3,  5, -1},
         {-2,  2,  3,  9, 10, 10, 10,  5,  4,  2,  1, -3},
         {-1,  4,  2,  9,  9,  9,  9,  3,  1,  3,  2, -2},
         {-3,  2,  5,  1,  3,  1,  4,  2,  5,  4,  3, -3},
         {-2,  1,  4,  2,  5,  2,  3,  4,  2,  1,  2, -1},
         {-1,  5,  1,  4,  3,  4,  1,  2,  3,  4,  1, -2},
         {-3,  2,  4,  5,  2,  3,  2,  4,  1,  2,  3, -3},
         {-2, -3, -2, -1, -2, -1, -3, -2, -1, -3, -1, -2}};

    private static int[][] waterFrames = {{6, 7, 8}, {7, 8, 6}, {8, 6, 7}};
    private static int[][] background = {{1}};

    public Field()
    {
        //super(WIDTH_IN_TILES, HEIGHT_IN_TILES, Misc.getInstance().createImage("/image/field.png"), TILE_WIDTH, TILE_HEIGHT);
        super(1, 1, Misc.getInstance().getLargeBackgroundImage(), Misc.getInstance().getLargeBackgroundImage().getWidth(), Misc.getInstance().getLargeBackgroundImage().getHeight());

        //createAnimatedTile(waterFrames[0][0]);      // tile -1
        //createAnimatedTile(waterFrames[1][0]);      // tile -2
        //createAnimatedTile(waterFrames[2][0]);      // tile -3
        /*
        for(int row = 0; row < HEIGHT_IN_TILES; ++row)
        {
            for (int column = 0; column < WIDTH_IN_TILES; ++column)
            {
                setCell(column, row, cellTiles[row][column]);
            }
        }
        */
        setCell(0, 0, background[0][0]);
    }

    public boolean containsImpassableArea(int x, int y, int width, int height)
    {
        return false;
    }

}