/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;


import java.awt.*;

import java.awt.event.*;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import tank.Grid.Point;
import tank.ObjectsChooserPanel.Rect;

/**
 *
 * @author IVO
 */
public class EditorLevelPanel extends JPanel
        implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    BufferedImage backgroundBufferedImage = null;
    BufferedImage gameLevelBufferedImage0 = null;
    BufferedImage gameLevelBufferedImage1 = null;
    BufferedImage gameLevelBufferedImage2 = null;
    BufferedImage gameLevelBufferedImage3 = null;
    BufferedImage platformsBufferedImage = null;
    BufferedImage topBufferedImage = null;
    public GameLevel gameLevel =  new GameLevel(this);
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private int mouseButton;
    private Grid grid = new Grid();
    private boolean gridIsVisible = true;
    private boolean backgroundIsVisible = true;
    private int mouseX;
    private int mouseY;
    private Point[][] platformKeys = null;
    private java.awt.Frame parentFrame;
    int currentLayer = 1;

    final int SPAWN_POINT_BLOCK_ID = 0;
    final int FLOOR_BLOCK_ID = 1;
    final int WALL_BLOCK_ID = 2;
    final int SCENERY_BLOCK_ID = 3;
    final int STATIC_OBJECT_BLOCK_ID = 4;
    final int DYNAMIC_OBJECT_BLOCK_ID = 5;
    final int RECTANGLE_DEATH_TRIGGER_BLOCK_ID = 6;
    final int TRIANGLE_DEATH_TRIGGER_BLOCK_ID = 7;
    final int CIRCLE_DEATH_TRIGGER_BLOCK_ID = 8;
    final int POINT_DEATH_TRIGGER_BLOCK_ID = 9;
    final int SAVE_POINT_BLOCK_ID = 10;
    final int PLATFORM_BLOCK_ID = 11;
    final int CLIMBABLE_BLOCK_ID = 12;
    final int SIGN_BLOCK_ID = 13;
    final int FLOATING_PLATFORM_BLOCK_ID = 14;
    final int IN_Z_LAYER_BLOCK_ID = 15;
    final int OUT_Z_LAYER_BLOCK_ID = 16;
    final int PORTAL_BLOCK_ID = 17;
    final int HORIZONTAL_PLATFORM_BLOCK_ID = 18;
    final int VERTICAL_PLATFORM_BLOCK_ID = 19;
    final int WATER_BLOCK_ID = 20;
    final int GOO_BLOCK_ID = 21;
    final int CHARACTER_ID = 200;

    final int VALUES_ID = 0;
    final int VALUES_ANGLE = 1;
    final int VALUES_X_OFFSET = 2;
    final int VALUES_Y_OFFSET = 3;

    Grid.Point curCell = null;
/*
    case 0: return "!S!";
    case 1: return "FLR";
    case 2: return "WAL";
    case 3: return "SCE";
    case 4: return "STA";
    case 5: return "DYN";
    case 6: return "H";
    case 7: return "V";
    case 8: return "O";
    case 9: return ".";
    case 10: return "!?!";
*/

    public void  setObjectsChooserPanel(ObjectsChooserPanel panel) {
        gameLevel.setObjectsChooserPanel(panel);
    }
    public void  setAttributesChooserPanel(ObjectsChooserPanel panel) {
        gameLevel.setAttributesChooserPanel(panel);
    }

    private int getOffset(int layer, int texHeight) {
        if (layer == 0 && !gameLevel.platformMode) {
            return texHeight - grid.getCellRect(0, 0).height();
        } else {
            return 0;
        }
    }

    public void saveLevel(String fileName) {
        gameLevel.save(fileName);
    }

    public void addLevel(String fileName) {
        gameLevel.add(fileName);
    }

    public void removeLevel(String fileName) {
        gameLevel.remove(fileName);
    }

    public void shiftLevel(String fileName) {
        gameLevel.shift(fileName);
    }

    public void loadLevel(String fileName) {
        gameLevel.load(fileName, false, true);
        gridSizeChanged();
        invalidateAll();
        repaint();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        gridSizeChanged();
        invalidateAll();
    }

    private void gridSizeChanged() {
        grid.setSize(gameLevel.getWidth(), gameLevel.getHeight(), getWidth(), getHeight());
        grid.setCellSize(gameLevel.tileWidth, gameLevel.tileHeight, getWidth(), getHeight());

    }

    public void shiftTiles(int n) {
        for (int i = 0; i < getLevelWidth(); ++i) {
            for (int t = 0; t < getLevelHeight(); ++t) {
                if (gameLevel.content0[i][t] >= gameLevel.getObjectsChooserPanel().imagesCount-1) gameLevel.content0[i][t] += n;
                if (gameLevel.content1[i][t] >= gameLevel.getObjectsChooserPanel().imagesCount-1) gameLevel.content1[i][t] += n;
                if (gameLevel.content2[i][t] >= gameLevel.getObjectsChooserPanel().imagesCount-1) gameLevel.content2[i][t] += n;
            }
        }
        repaint();
    }

    private void invalidateAll() {
        invalidateBackgroud();
        invalidateGameLevel();
        invalidateTop();
    }

    private void drawGrid(Graphics g) {
        int i, t;
        int lastCol = -1, lastRow = -1;
        if (gridIsVisible) {
            if (backgroundIsVisible && gameLevel.getBackgroundImage() != null) g.setXORMode(Color.white);
            g.setColor(Color.black);
            for (i = getLevelHeight(); i >= 0; --i) {
                for (t = 0; t <= getLevelWidth(); ++t) {
                    if (lastRow != grid.getLineY(i)) {
                        g.drawLine(grid.getLineX(0), grid.getLineY(i), grid.getLineX(grid.getWidth()), grid.getLineY(i));
                        lastRow = grid.getLineY(i);
                    }
                    if (lastCol != grid.getLineX(t)) {
                        g.drawLine(grid.getLineX(t), grid.getLineY(0), grid.getLineX(t), grid.getLineY(grid.getHeight()));
                        lastCol = grid.getLineX(t);
                    }
                }
            }
            if (backgroundIsVisible && gameLevel.getBackgroundImage() != null) g.setPaintMode();
        }
    }



    private void drawImage(Graphics2D g2, Image image, int col, int row) {
        //int width = grid.getLineX(col+1)-grid.getLineX(col);
        //int height = grid.getLineY(row+1)-grid.getLineY(row);
        g2.drawImage(image, grid.getLineX(col), grid.getLineY(row), this);
    }

    private void drawImage(Graphics2D g2, Image image, int col, int row, int offsetY, int width, int height) {
        //int width = grid.getLineX(col+1)-grid.getLineX(col);
        //int height = grid.getLineY(row+1)-grid.getLineY(row);
        Rect cellRect = grid.getCellRect(col, row);
        int imageTrueHeight = cellRect.width() * (height/width);
        g2.drawImage(image, grid.getLineX(col), grid.getLineY(row) + cellRect.height() - imageTrueHeight + offsetY, cellRect.width(), imageTrueHeight, this);
    }

    private boolean fillCell(int x, int y, int item) {
        Grid.Point cell = grid.getCell(x, y);
        selectedRow = cell.y;
        selectedColumn = cell.x;
        if (selectedRow < 0 || selectedColumn < 0) return false;
        int oldValue = gameLevel.getCell(selectedColumn, selectedRow, currentLayer);
        gameLevel.setCell(selectedColumn, selectedRow, currentLayer, item);

        return true;
    }

    public EditorLevelPanel(java.awt.Frame parent) {
        parentFrame = parent;
        addMouseListener(this);
        addMouseMotionListener(this);
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pressedEscape");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "pressedEnter");
    }

    public int getLevelWidth() {
        return gameLevel.getWidth();
    }

    public int getLevelHeight() {
        return gameLevel.getHeight();
    }

    public void setLevelSize(int width, int height) {
        boolean ok = true;
        if (gameLevel.getWidth() != width || gameLevel.getHeight() != height) {
            ok = false;
        }
        if (!ok) {
            width = Math.min(width, 300);
            height = Math.min(height, 300);
            gameLevel.setSize(width, height);
            gridSizeChanged();
            invalidateAll();
            repaint();
        }
    }

    public void setWorldSize(int width, int height) {
        gameLevel.setWorldSize(width, height);
    }

    public void setAreaCoords(int width, int height, int z) {
        gameLevel.setAreaCoords(width, height, z);
    }

    public void setLevelValues(int tileWidth, int tileHeight, int playerWidth, int playerHeight, boolean platformMode) {
        boolean ok = true;
        if (gameLevel.tileWidth != tileWidth || gameLevel.tileHeight != tileHeight) {
            ok = false;
        }
        gameLevel.setValues(tileWidth, tileHeight, playerWidth, playerHeight, platformMode);
        if (!ok) {
            gridSizeChanged();
            invalidateAll();
            repaint();
        }
    }

    public boolean getTileInverted(boolean left, boolean right, boolean up, boolean down) {
        if ((up && down && left)||(up && left)||(left && down)||(left)) {
            return true;
        } return false;
    }

    public java.awt.Point getRightTile(boolean left, boolean right, boolean up, boolean down) {
        if (up && down && left && right) {
            return new java.awt.Point(2, 3);
        }

        if ((up && down && left) || (up && down && right)) {
            return new java.awt.Point(0, 3);
        }
        if (up && right && left) {
            return new java.awt.Point(2, 2);
        }
        if (right && down && left) {
            return new java.awt.Point(1, 3);
        }

        if (up && down) {
            return new java.awt.Point(0, 2);
        }
        if (left && right) {
            return new java.awt.Point(1, 2);
        }

        if ((up && left) || (up && right)) {
            return new java.awt.Point(1, 1);
        }
        if ((left && down) || (right && down)) {
            return new java.awt.Point(0, 1);
        }
        if (up) {
            return new java.awt.Point(2, 1);
        }
        if (down) {
            return new java.awt.Point(2, 0);
        }
        if (left || right) {
            return new java.awt.Point(1, 0);
        }
        return new java.awt.Point(0, 0);

    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 300);
    }

    public void setCurrentLayer(int layer) {
        currentLayer = layer;
    }

    public static BufferedImage toBufferedImage(Image img){
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        // Return the buffered image
        return bimage;
    }

    public static Image toImage(BufferedImage bimage){
        // Casting is enough to convert from BufferedImage to Image
        Image img = (Image) bimage;
        return img;
    }

    public static Image getEmptyImage(int width, int height){
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return toImage(img);
    }


    public static Image rotate(Image img, double angle)
    {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));

        int w = img.getWidth(null), h = img.getHeight(null);

        int neww = (int) Math.floor(w*cos + h*sin),
                newh = (int) Math.floor(h*cos + w*sin);

        BufferedImage bimg = toBufferedImage(getEmptyImage(neww, newh));
        Graphics2D g = bimg.createGraphics();

        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(Math.toRadians(angle), w/2, h/2);
        g.drawRenderedImage(toBufferedImage(img), null);
        g.dispose();

        return toImage(bimg);
    }

    public void drawLayer(Graphics g, ImageObserver io, int n) {
        for (int i = 0; i < getLevelWidth(); ++i) {
            for (int t = 0; t < getLevelHeight(); ++t) {
                if ((gameLevel.content2[i][t] != -1 && n == 2) || (gameLevel.content1[i][t] != -1 && n == 1) || (gameLevel.content0[i][t] != -1 && n == 0) || (gameLevel.values[i][t][0] != -1 && n == 3)) {
                    Rect cell = grid.getCellRect(i, t);
                    int imageIndex = gameLevel.getCell(i, t, n);
                    Image image;
                    if (n != 3) {
                        image = gameLevel.getObjectsChooserPanel().getImage(imageIndex);
                    } else {
                        image = gameLevel.getObjectsChooserPanel().getImage(0);
                    }
                    if (image == null) {
                        continue;
                    }
                    int width = 0, height = 0;
                    float test1 = 0;
                    if (n != 3) {
                        if (gameLevel.getObjectsChooserPanel().tileTypes.get(imageIndex) == 0) {
                            width = image.getWidth(io);
                            height = image.getHeight(io);
                        } else {
                            width = image.getWidth(io)/3;
                            height = image.getHeight(io)/4;
                        }
                        test1 = (float)height/(float)width;
                    }
                    int imageTrueWidth = (int)(image.getWidth(this)*cell.width()/gameLevel.tileWidth);
                    int imageTrueHeight = (int)(image.getHeight(this)*cell.height()/gameLevel.tileHeight);
                    //g.drawImage(image, cell.left, cell.bottom-imageTrueHeight+getOffset(n), cell.width(), imageTrueHeight, io);
                    if (n != 3) {
                        int XOffset = 0;
                        int YOffset = 0;
                        if (n == 2) {
                            //XOffset = gameLevel.values[i][t][VALUES_X_OFFSET]*(cell.width()/gameLevel.tileWidth);
                            //YOffset = gameLevel.values[i][t][VALUES_Y_OFFSET]*(cell.height()/gameLevel.tileHeight);
                        }
                        if (gameLevel.getObjectsChooserPanel().tileTypes.get(imageIndex) == 0) {
                            if (gameLevel.values[i][t][VALUES_ANGLE] == 1 && gameLevel.values[i][t][VALUES_ID] < 200) {
                                g.drawImage(rotate(image, 90), cell.left + XOffset, cell.bottom - cell.height()/2+imageTrueHeight/2 - imageTrueHeight + getOffset(n, imageTrueHeight) + YOffset, imageTrueHeight, imageTrueWidth, io);
                            } else if (gameLevel.values[i][t][VALUES_ANGLE] == 2) {
                                g.drawImage(rotate(image, 180), cell.left+cell.width()/2-imageTrueWidth/2 + XOffset, cell.top + getOffset(n, imageTrueHeight) + YOffset, imageTrueWidth, imageTrueHeight, io);
                            } else if (gameLevel.values[i][t][VALUES_ANGLE] == 3) {
                                g.drawImage(rotate(image, 270), cell.left+(cell.width()-imageTrueHeight) + XOffset, cell.bottom - cell.height()/2+imageTrueHeight/2 - imageTrueHeight + getOffset(n, imageTrueHeight) + YOffset, imageTrueHeight, imageTrueWidth, io);
                            } else {
                                g.drawImage(image, cell.left+cell.width()/2-imageTrueWidth/2 + XOffset, cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight) + YOffset, imageTrueWidth, imageTrueHeight, io);
                            }
                        } else if (gameLevel.getObjectsChooserPanel().tileTypes.get(imageIndex) == 1) {
                            imageTrueHeight = (int)(cell.width() * test1);
                            boolean left = (i == 0 || ((gameLevel.content2[i - 1][t] == imageIndex && n == 2) || (gameLevel.content1[i - 1][t] == imageIndex && n == 1) || (gameLevel.content0[i-1][t] == imageIndex && n == 0) || (gameLevel.values[i-1][t][0] == imageIndex && n == 3)));
                            boolean right = (i == getLevelWidth()-1 || ((gameLevel.content2[i+1][t] == imageIndex && n == 2) || (gameLevel.content1[i+1][t] == imageIndex && n == 1) || (gameLevel.content0[i+1][t] == imageIndex && n == 0) || (gameLevel.values[i+1][t][0] == imageIndex && n == 3)));
                            boolean up = (t == 0 || ((gameLevel.content2[i][t-1] == imageIndex && n == 2) || (gameLevel.content1[i][t-1] == imageIndex && n == 1) || (gameLevel.content0[i][t-1] == imageIndex && n == 0) || (gameLevel.values[i][t-1][0] == imageIndex && n == 3)));
                            boolean down = (t == getLevelHeight()-1 || ((gameLevel.content2[i][t+1] == imageIndex && n == 2) || (gameLevel.content1[i][t+1] == imageIndex && n == 1) || (gameLevel.content0[i][t+1] == imageIndex && n == 0) || (gameLevel.values[i][t+1][0] == imageIndex && n == 3)));
                            int tileX = getRightTile(left, right, up, down).x;
                            int tileY = getRightTile(left, right, up, down).y;
                            if (getTileInverted(left, right, up, down)) {
                                g.drawImage(image, cell.left, cell.bottom - imageTrueHeight/* + getOffset(n, imageTrueHeight)*/, cell.left+cell.width(), cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight)+imageTrueHeight,
                                        width*tileX+width, height*tileY, width*tileX, height*tileY+height, this);
                            } else {
                                g.drawImage(image, cell.left, cell.bottom - imageTrueHeight/* + getOffset(n, imageTrueHeight)*/, cell.left+cell.width(), cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight)+imageTrueHeight,
                                        width*tileX, height*tileY, width*tileX+width, height*tileY+height, this);
                            }
                        } else {
                            height = image.getHeight(io);
                            g.drawImage(image, cell.left, cell.bottom + getOffset(n, imageTrueHeight), cell.right, cell.top + getOffset(n, imageTrueHeight),
                                    0, 0, height, height, this);
                        }
                    } else {
                        g.setColor(getTileColor(gameLevel.values[i][t][VALUES_ID], gameLevel.values[i][t][VALUES_ANGLE]));
                        g.fillRect(cell.left, cell.top, cell.width(), cell.height());
                        g.setColor(new Color(0, 0, 0, 0.25f));
                        g.setFont(g.getFont().deriveFont(25.0f));
                        g.drawString(getTileString(gameLevel.values[i][t][VALUES_ID], gameLevel.values[i][t][VALUES_ANGLE]), cell.left, cell.bottom);
                    }
                }
            }
        }
        g.setColor(Color.WHITE);
    }

    /*final int PLATFORM_BLOCK_ID = 11;
    final int CLIMBABLE_BLOCK_ID = 12;
    final int SIGN_BLOCK_ID = 13;
    final int FLOATING_PLATFORM_BLOCK_ID = 15;
    final int IN_Z_LAYER_BLOCK_ID = 16;
    final int OUT_Z_LAYER_BLOCK_ID = 17;
    final int PORTAL_BLOCK_ID = 18;
    final int HORIZONTAL_PLATFORM_BLOCK_ID = 19;
    final int VERTICAL_PLATFORM_BLOCK_ID = 20;*/

    private String getTileString(int i, int angle) {
        switch (i) {
            case SPAWN_POINT_BLOCK_ID: return "!S!";
            case FLOOR_BLOCK_ID: return "";
            case WALL_BLOCK_ID: return "";
            case SCENERY_BLOCK_ID: return "SCE";
            case STATIC_OBJECT_BLOCK_ID: return "STA";
            case DYNAMIC_OBJECT_BLOCK_ID: return "DYN";
            case RECTANGLE_DEATH_TRIGGER_BLOCK_ID: return "H";
            case TRIANGLE_DEATH_TRIGGER_BLOCK_ID: return "V";
            case CIRCLE_DEATH_TRIGGER_BLOCK_ID: return "O";
            case POINT_DEATH_TRIGGER_BLOCK_ID: return ".";
            case SAVE_POINT_BLOCK_ID: return "!?!";
            case PLATFORM_BLOCK_ID: return "PLAT";
            case CLIMBABLE_BLOCK_ID: return "";
            case SIGN_BLOCK_ID: return "SGN";
            case FLOATING_PLATFORM_BLOCK_ID: return "FLT";
            case IN_Z_LAYER_BLOCK_ID: return "Z+";
            case OUT_Z_LAYER_BLOCK_ID: return "Z-";
            case PORTAL_BLOCK_ID: return "PRTL";
            case HORIZONTAL_PLATFORM_BLOCK_ID: return "VPLT";
            case VERTICAL_PLATFORM_BLOCK_ID: return "HPLT";
            case WATER_BLOCK_ID: return "WWW";
            case GOO_BLOCK_ID: return "GOO";
        }
        if (i >= 200) {
            if (angle == 0) {
                return "ch" + (i - 200);
            }
            return "it" + (i - 200);
        }
        return "";
    }

    private Color getTileColor(int i, int angle) {
        switch (i) {
            case SPAWN_POINT_BLOCK_ID: return new Color(1.0f, 0, 1.0f, 0.2f);
            case FLOOR_BLOCK_ID: return new Color(0.5f, 0.5f, 0.5f, 0.2f);
            case WALL_BLOCK_ID: return new Color(0, 0, 0, 0.2f);
            case SCENERY_BLOCK_ID: return new Color(0, 1.0f, 0, 0.2f);
            case STATIC_OBJECT_BLOCK_ID: return new Color(1.0f, 1.0f, 0, 0.2f);
            case DYNAMIC_OBJECT_BLOCK_ID: return new Color(0.8f, 0.3f, 0, 0.2f);
            case RECTANGLE_DEATH_TRIGGER_BLOCK_ID: return new Color(1.0f, 0, 0, 0.2f);
            case TRIANGLE_DEATH_TRIGGER_BLOCK_ID: return new Color(1.0f, 0, 0, 0.2f);
            case CIRCLE_DEATH_TRIGGER_BLOCK_ID: return new Color(1.0f, 0, 0, 0.2f);
            case POINT_DEATH_TRIGGER_BLOCK_ID: return new Color(1.0f, 0, 0, 0.2f);
            case SAVE_POINT_BLOCK_ID: return new Color(0, 0, 1.0f, 0.2f);
            case PLATFORM_BLOCK_ID: return new Color(0.5f, 0.5f, 1.0f, 0.2f);
            case CLIMBABLE_BLOCK_ID: return new Color(1.0f, 0.5f, 0.5f, 0.2f);
            case SIGN_BLOCK_ID: return new Color(0.3f, 0.3f, 0, 0.2f);
            case FLOATING_PLATFORM_BLOCK_ID: return new Color(0.5f, 0.5f, 1.0f, 0.2f);
            case IN_Z_LAYER_BLOCK_ID: return new Color(1.0f, 0, 1.0f, 0.2f);
            case OUT_Z_LAYER_BLOCK_ID: return new Color(1.0f, 0, 1.0f, 0.2f);
            case PORTAL_BLOCK_ID: return new Color(1.0f, 0, 1.0f, 0.2f);
            case HORIZONTAL_PLATFORM_BLOCK_ID: return new Color(0.5f, 0.5f, 1.0f, 0.2f);
            case VERTICAL_PLATFORM_BLOCK_ID: return new Color(0.5f, 0.5f, 1.0f, 0.2f);
            case WATER_BLOCK_ID: return new Color(0.2f, 0.2f, 1.0f, 0.2f);
            case GOO_BLOCK_ID: return new Color(0.2f, 1.0f, 0.2f, 0.2f);
        }
        if (i >= 200) {
            if (angle == 0) {
                return new Color(0.7f, 0.4f, 0.0f, 0);
            }
            return new Color(0.7f, 0.4f, 0.0f, 0);
        }
        return new Color(1.0f, 1.0f, 1.0f, 0);
    }

    @Override
    public void paint(Graphics g) {
        if (backgroundIsVisible && backgroundBufferedImage != null)
            g.drawImage(backgroundBufferedImage, 0, 0, backgroundBufferedImage.getWidth(),
                    backgroundBufferedImage.getHeight(), this);
        else {
            g.setColor(Color.lightGray);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        if (gridIsVisible) {
            drawWorldEditorPanel(g);
            drawGrid(g);
        }
        if (gameLevelBufferedImage0!=null) g.drawImage(gameLevelBufferedImage0, 0, 0, gameLevelBufferedImage0.getWidth(),
                    gameLevelBufferedImage0.getHeight(), this);
        if (gameLevelBufferedImage1!=null) g.drawImage(gameLevelBufferedImage1, 0, 0, gameLevelBufferedImage1.getWidth(),
                    gameLevelBufferedImage1.getHeight(), this);
        if (gameLevelBufferedImage2!=null) g.drawImage(gameLevelBufferedImage2, 0, 0, gameLevelBufferedImage2.getWidth(),
                    gameLevelBufferedImage2.getHeight(), this);
        if (gameLevelBufferedImage3!=null) g.drawImage(gameLevelBufferedImage3, 0, 0, gameLevelBufferedImage3.getWidth(),
                gameLevelBufferedImage3.getHeight(), this);
        ImageObserver io = new ImageObserver() {
            @Override
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return false;
            }
        };

        drawLayer(g, io, 0);
        drawLayer(g, io, 1);
        drawLayer(g, io, 2);
        if (currentLayer == 3) drawLayer(g, io, 3);

        if (gameLevel.getPlatformCount() > 0)
            g.drawImage(platformsBufferedImage, 0, 0, platformsBufferedImage.getWidth(),
                    platformsBufferedImage.getHeight(), this);
        if (selectedRow != -1 && selectedColumn != -1) {
            g.setColor(Color.BLACK);
            g.drawString("Layer1: " + gameLevel.content0[selectedColumn][selectedRow],10,10*2);
            g.drawString("Layer2: " + gameLevel.content1[selectedColumn][selectedRow],10,20*2);
            g.drawString("Layer3: " + gameLevel.content2[selectedColumn][selectedRow],10,30*2);
            g.drawString("Type: " + gameLevel.values[selectedColumn][selectedRow][0],10,40*2);
            g.drawString("X: " + gameLevel.values[selectedColumn][selectedRow][2],10,50*2);
            g.drawString("Y: " + gameLevel.values[selectedColumn][selectedRow][3],10,60*2);
            g.drawString("W: " + gameLevel.values[selectedColumn][selectedRow][4],10,70*2);
            g.drawString("H: " + gameLevel.values[selectedColumn][selectedRow][5],10,80*2);
            g.drawString("Orientation: " + gameLevel.values[selectedColumn][selectedRow][1],10,90*2);
            g.setColor(new Color(1.0f, 0, 0, 0.2f));
            if (curCell != null) {
                g.fillRect(grid.getLineX(curCell.x), grid.getLineY(curCell.y), grid.getLineX(curCell.x + 1) - grid.getLineX(curCell.x), grid.getLineY(curCell.y + 1) - grid.getLineY(curCell.y));
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public boolean mouseIsInGrid(int x, int y) {
        Rectangle rect = new Rectangle();
        rect.x = grid.getCellRect(0, 0).left;
        rect.y = grid.getCellRect(0, 0).top;
        rect.width = grid.getGridScreenWidth();
        rect.height = grid.getGridScreenHeight();
        return rect.contains(x, y);
    }

    public boolean mouseIsInEditorGrid(int x, int y) {
        Rectangle rect = new Rectangle();
        rect.width = (int)(realChunkSize * gameLevel.worldWidth);
        rect.x = getWidth()/2 - rect.width/2 + worldEditorPanelOffsetX;
        rect.height = (int)(realChunkSize * gameLevel.worldWidth);
        rect.y = grid.getLineY(0) + worldEditorPanelOffsetY - rect.height;
        return rect.contains(x, y);
    }

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private int worldEditorPanelOffsetX = 0;
    private int worldEditorPanelOffsetY = 0;
    private int worldEditorPanelOffsetScale = 10;
    private boolean lastClickInGrid = false;
    public int currentZLayer = 0;
    private int lastMovedMouseX = 0;
    private int lastMovedMouseY = 0;
    int realChunkSize = 0;
    public int selectedId = -1;

    private void drawWorldEditorPanel(Graphics g) {
        float scale = worldEditorPanelOffsetScale/10.0f;
        int chunkSize = 20;
        realChunkSize = (int)(chunkSize * scale);
        int w = (int)(realChunkSize * gameLevel.worldWidth);
        int h = (int)(realChunkSize * gameLevel.worldWidth);
        int x = getWidth()/2 - w/2 + worldEditorPanelOffsetX;
        int y = grid.getLineY(0) + worldEditorPanelOffsetY;
        for (int i = 0; i < gameLevel.worldWidth; ++i) {
            for (int j = 0; j < gameLevel.worldWidth; ++j) {
                if ((j + i)%2 == 0) g.setColor(new Color(0.1f, 0.1f, 0.1f));
                else g.setColor(new Color(0.0f, 0.0f, 0.0f));
                g.fillRect(x + i * realChunkSize, y - (j+1) * realChunkSize, realChunkSize, realChunkSize);
            }
        }
        for (int z = 0; z <= currentZLayer; ++z) {
            float colorScale = ((float)(gameLevel.worldHeight - Math.abs(z-currentZLayer)))/((float)gameLevel.worldHeight);
            g.setColor(new Color(0.5f * colorScale, 0.5f * colorScale, 0.5f * colorScale));
            selectedId = -1;
            int selectedXX = -1;
            int selectedYY = -1;
            int selectedWW = -1;
            int selectedHH = -1;
            for (int i = 0; i < gameLevel.roomsCoords.size(); ++i) {
                int xx = gameLevel.roomsCoords.get(i).get(0);
                int yy = gameLevel.roomsCoords.get(i).get(1) - 1;
                int zz = gameLevel.roomsCoords.get(i).get(2);
                int ww = gameLevel.roomsWalls.get(i).get(0).size();
                int hh = gameLevel.roomsWalls.get(i).size();
                if (zz == z) {
                    if (z == currentZLayer && lastMovedMouseX > x + xx * realChunkSize && lastMovedMouseX < x + xx * realChunkSize + realChunkSize*(ww/gameLevel.chunkWidth) &&
                            lastMovedMouseY < y - (yy+1) * realChunkSize && lastMovedMouseY > y - (yy+1) * realChunkSize - realChunkSize*(hh/gameLevel.chunkHeight)) {
                        g.setColor(new Color(0.5f * colorScale, 0.7f * colorScale, 0.5f * colorScale));
                        g.fillRect(x + xx * realChunkSize, y - (yy+1) * realChunkSize - realChunkSize*(hh/gameLevel.chunkHeight), realChunkSize*(ww/gameLevel.chunkWidth), realChunkSize*hh/gameLevel.chunkHeight);
                        selectedId = i;
                        selectedXX = xx;
                        selectedYY = yy;
                        selectedHH = hh;
                        selectedWW = ww;
                    } else {
                        g.setColor(new Color(0.5f * colorScale, 0.5f * colorScale, 0.5f * colorScale));
                        g.fillRect(x + xx * realChunkSize, y - (yy+1) * realChunkSize - realChunkSize*(hh/gameLevel.chunkHeight), realChunkSize*(ww/gameLevel.chunkWidth), realChunkSize*hh/gameLevel.chunkHeight);
                    }
                    float blockXSize = ((float)(realChunkSize)/((float)gameLevel.chunkWidth));
                    float blockYSize = ((float)(realChunkSize)/((float)gameLevel.chunkHeight));
                    if (z == currentZLayer) {
                        for (int xxx = 0; xxx < realChunkSize * ww/gameLevel.chunkWidth; ++xxx) {
                            for (int yyy = 0; yyy < realChunkSize * hh/gameLevel.chunkHeight; ++yyy) {
                                int xxxx = (int)((float)xxx / blockXSize);
                                int yyyy = (int)((float)yyy / blockYSize);
                                if (gameLevel.roomsWalls.get(i).get(yyyy).get(xxxx) != null) {
                                    if (gameLevel.roomsWalls.get(i).get(yyyy).get(xxxx)) {
                                        g.setColor(new Color(0.7f , 0.7f, 0.7f));
                                        g.fillRect(x + xx * realChunkSize + xxx, y - (yy+1) * realChunkSize + yyy - realChunkSize * hh/gameLevel.chunkHeight, 1, 1);
                                    }
                                } else {
                                    g.setColor(new Color(0.3f , 0.3f, 0.3f));
                                    g.fillRect(x + xx * realChunkSize + xxx, y - (yy+1) * realChunkSize + yyy - realChunkSize * hh/gameLevel.chunkHeight, 1, 1);
                                }
                            }
                        }
                    }
                    g.setColor(new Color(0.6f * colorScale, 0.6f * colorScale, 0.6f * colorScale));
                    g.drawRect(x + xx * realChunkSize, y - (yy+1) * realChunkSize - realChunkSize*(hh/gameLevel.chunkHeight), realChunkSize*(ww/gameLevel.chunkWidth), realChunkSize*hh/gameLevel.chunkHeight);
                }
            }
            if (selectedId != -1 && gameLevel.roomsCoords.get(selectedId).get(2) == currentZLayer) {
                gameLevel.coordX = gameLevel.roomsCoords.get(selectedId).get(0);
                gameLevel.coordY = gameLevel.roomsCoords.get(selectedId).get(1);
                gameLevel.coordZ = gameLevel.roomsCoords.get(selectedId).get(2);
                g.setColor(new Color(0.6f * colorScale, 0.8f * colorScale, 0.6f * colorScale));
                g.drawRect(x + selectedXX * realChunkSize, y - (selectedYY+1) * realChunkSize - realChunkSize*(selectedHH/gameLevel.chunkHeight), realChunkSize*(selectedWW/gameLevel.chunkWidth), realChunkSize*selectedHH/gameLevel.chunkHeight);
                g.setColor(new Color(1.0f, 0, 0));
                g.drawString(gameLevel.roomsNames.get(selectedId), lastMovedMouseX, lastMovedMouseY);
            } else {
                gameLevel.coordX = -1;
                gameLevel.coordY = -1;
                gameLevel.coordZ = -1;
            }
        }
        g.setColor(new Color(1.0f, 1.0f, 1.0f));
        if (clickedDotX != 0 && clickedDotY != 0) {
            g.drawRect(clickedDotX, clickedDotY, curDotX - clickedDotX, curDotY - clickedDotY);
        } else {
            g.drawRect(curDotX, curDotY, 1, 1);
        }
    }

    int clickedDotX = 0;
    int clickedDotY = 0;

    public void mousePressed(MouseEvent e) {
        mouseButton = e.getButton();
        if (mouseIsInGrid(e.getX(), e.getY())) {
            mouseEvent(e);
            lastClickInGrid = false;
        } else {
            if (mouseIsInEditorGrid(e.getX(), e.getY()) && curDotDist < 5) {
                clickedDotX = curDotX;
                clickedDotY = curDotY;
            }
            else if (mouseButton == MouseEvent.BUTTON1) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
            lastClickInGrid = true;
        }
    }

    public void reloadLevel(String fileName) {
        gameLevel.load(fileName, false, false);
        gridSizeChanged();
        invalidateAll();
        repaint();
        ((LevelEditorFrame)(parentFrame)).updateFieldsAfterLoad();
    }

    public void removeRoom(int id) {
        gameLevel.roomsCoords.remove(id);
        gameLevel.roomsWalls.remove(id);
        gameLevel.roomsNames.remove(id);
        gameLevel.roomsAmbientNames.remove(id);
        if (gameLevel.currentRoomId == id) {
            reloadLevel(gameLevel.fileName);
        }
    }

    private void addRoom(int x, int y, int w, int h) {
        gameLevel.coordX = x;
        gameLevel.coordY = y;
        gameLevel.coordZ = currentZLayer;
        gameLevel.width = w * gameLevel.chunkWidth;
        gameLevel.height = h * gameLevel.chunkHeight;
        gameLevel.roomsCoords.add(new ArrayList<Integer>());
        gameLevel.roomsCoords.get(gameLevel.roomsCoords.size()-1).add(x);
        gameLevel.roomsCoords.get(gameLevel.roomsCoords.size()-1).add(y);
        gameLevel.roomsCoords.get(gameLevel.roomsCoords.size()-1).add(currentZLayer);
        gameLevel.roomsNames.add("");
        gameLevel.roomsAmbientNames.add("");
        gameLevel.currentRoomId = gameLevel.roomsNames.size()-1;
        /*gameLevel.roomsWalls = new ArrayList<ArrayList<ArrayList<Boolean>>>();
        for (int i = 0; i < h * gameLevel.chunkHeight; ++i) {
            gameLevel.roomsWalls.add(new ArrayList<ArrayList<Boolean>>());
            for (int j = 0; j < w * gameLevel.chunkWidth; ++j) {
                gameLevel.roomsWalls.get(i).add(null);
            }
        }*/
        gameLevel.roomsWalls.add(new ArrayList<ArrayList<Boolean>>());
        for (y = 0; y < gameLevel.height; ++y) {
            gameLevel.roomsWalls.get(gameLevel.roomsWalls.size()-1).add(new ArrayList<Boolean>());
            for (x = 0; x < gameLevel.width; ++x) {
                gameLevel.roomsWalls.get(gameLevel.roomsWalls.size()-1).get(gameLevel.roomsWalls.get(gameLevel.roomsWalls.size()-1).size()-1).add(null);
            }
        }

        gameLevel.content0 = new int[gameLevel.width][gameLevel.height];
        gameLevel.content1 = new int[gameLevel.width][gameLevel.height];
        gameLevel.content2 = new int[gameLevel.width][gameLevel.height];
        gameLevel.values = new int[gameLevel.width][gameLevel.height][6];
        //setValues(tileWidth, tileHeight, 0, 0, false);
        for (y = 0; y < gameLevel.height; ++y) {
            for (x = 0; x < gameLevel.width; ++x) {
                gameLevel.content0[x][y] = -1;
            }
        }
        for (y = 0; y < gameLevel.height; ++y) {
            for (x = 0; x < gameLevel.width; ++x) {
                gameLevel.content1[x][y] = -1;
            }
        }
        for (y = 0; y < gameLevel.height; ++y) {
            for (x = 0; x < gameLevel.width; ++x) {
                gameLevel.content2[x][y] = -1;
            }
        }
        for (y = 0; y < gameLevel.height; ++y) {
            for (x = 0; x < gameLevel.width; ++x) {
                gameLevel.values[x][y][0] = -1;
                gameLevel.values[x][y][1] = 0;
                gameLevel.values[x][y][2] = 0;
                gameLevel.values[x][y][3] = 0;
                gameLevel.values[x][y][4] = 0;
                gameLevel.values[x][y][5] = 0;
            }
        }

        gridSizeChanged();
        invalidateAll();
        repaint();
        ((LevelEditorFrame)(parentFrame)).updateFieldsAfterLoad();
        gameLevel.save(gameLevel.fileName);
    }

    public void mouseReleased(MouseEvent e) {
        if (!mouseIsInGrid(e.getX(), e.getY())) {
            int ww = (Math.max(curDotX, clickedDotX) - Math.min(curDotX, clickedDotX))/realChunkSize;
            int hh = (Math.max(curDotY, clickedDotY) - Math.min(curDotY, clickedDotY))/realChunkSize;
            if (gameLevel.coordX != -1) {
                //gameLevel.save(gameLevel.fileName);
                reloadLevel(gameLevel.fileName);
            } else if (ww != 0 && hh != 0 && clickedDotX != 0 && clickedDotY != 0) {
                int w = (int)(realChunkSize * gameLevel.worldWidth);
                int h = (int)(realChunkSize * gameLevel.worldWidth);
                int x = getWidth()/2 - w/2 + worldEditorPanelOffsetX;
                int y = grid.getLineY(0) + worldEditorPanelOffsetY;
                int xx = (Math.min(curDotX, clickedDotX) - x)/realChunkSize;
                int yy =(y - Math.max(curDotY, clickedDotY))/realChunkSize;
                boolean intersects = false;
                Rectangle rect1 = new Rectangle(xx + 1, yy + 1, ww - 1, hh - 1);
                for (int  i = 0; i < gameLevel.roomsCoords.size(); ++i) {
                    if (gameLevel.roomsCoords.get(i).get(2) == currentZLayer) {
                        Rectangle rect2 = new Rectangle(gameLevel.roomsCoords.get(i).get(0) + 1, gameLevel.roomsCoords.get(i).get(1) + 1, gameLevel.roomsWalls.get(i).get(0).size() - 1, gameLevel.roomsWalls.get(i).size() - 1);
                        if (rect1.intersects(rect2) || rect1.contains(rect2)) {
                            intersects = true;
                            break;
                        }
                    }
                }
                if (!intersects) {
                    addRoom(xx, yy, ww, hh);
                }
                repaint();
            }
        }
        clickedDotX = 0;
        clickedDotY = 0;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        lastMovedMouseX = e.getX();
        lastMovedMouseY = e.getY();
        if (mouseIsInGrid(e.getX(), e.getY()) && !lastClickInGrid) {
            mouseEvent(e);
        } else {
            if (mouseIsInEditorGrid(e.getX(), e.getY())) {
                int w = (int)(realChunkSize * gameLevel.worldWidth);
                int x = getWidth()/2 - w/2 + worldEditorPanelOffsetX;
                int y = grid.getLineY(0) + worldEditorPanelOffsetY;
                int mx = e.getX() + realChunkSize/4;
                int my = e.getY() - realChunkSize/4;
                curDotX = mx - (mx - x) + ((mx - x) - ((mx - x) % realChunkSize));
                curDotY = my - (my - y) + ((my - y) - ((my - y) % realChunkSize));
            } else if (clickedDotX == 0 && clickedDotY == 0 && lastClickInGrid) {
                worldEditorPanelOffsetX += e.getX() - lastMouseX;
                worldEditorPanelOffsetY += e.getY() - lastMouseY;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
            repaint();
        }
    }

    private void mouseEvent(MouseEvent e) {
        if (mouseButton == MouseEvent.BUTTON1
                && gameLevel.getObjectsChooserPanel().getSelectedObject() != -1) {
            if (currentLayer != 3) {
                if (fillCell(e.getX(), e.getY(), gameLevel.getObjectsChooserPanel().getSelectedObject())) {
                    repaint();
                }
            } else {
                if (fillCell(e.getX(), e.getY(), gameLevel.curType)) {
                    repaint();
                }
            }
        } else if (mouseButton == MouseEvent.BUTTON3) {
            if (fillCell(e.getX(), e.getY(), -1)) {
                repaint();
            }
        } else if (mouseButton == MouseEvent.BUTTON2) {
            //shiftTiles(1);
            //gameLevel.fillFloor(gameLevel.getObjectsChooserPanel().getSelectedObject());
            gameLevel.curType = gameLevel.values[selectedColumn][selectedRow][0];
            gameLevel.curAngle = gameLevel.values[selectedColumn][selectedRow][1];
            gameLevel.curXOffset = gameLevel.values[selectedColumn][selectedRow][2];
            gameLevel.curYOffset = gameLevel.values[selectedColumn][selectedRow][3];
            gameLevel.curObjectWidth = gameLevel.values[selectedColumn][selectedRow][4];
            gameLevel.curObjectHeight = gameLevel.values[selectedColumn][selectedRow][5];
            ((LevelEditorFrame)parentFrame).updateFields();
            //repaint();
        }
    }

    int curDotX = 0;
    int curDotY = 0;
    int curDotDist = 100;

    public void mouseMoved(MouseEvent e) {
        curCell = grid.getCell(e.getX(), e.getY());
        selectedRow = curCell.y;
        selectedColumn = curCell.x;
        lastMovedMouseX = e.getX();
        lastMovedMouseY = e.getY();
        int w = (int)(realChunkSize * gameLevel.worldWidth);
        int x = getWidth()/2 - w/2 + worldEditorPanelOffsetX;
        int y = grid.getLineY(0) + worldEditorPanelOffsetY;
        if (!mouseIsInGrid(e.getX(), e.getY()) &&  mouseIsInEditorGrid(e.getX(), e.getY())) {
            int mx = e.getX() + realChunkSize/4;
            int my = e.getY() - realChunkSize/4;
            curDotX = mx - (mx - x) + ((mx - x) - ((mx - x) % realChunkSize));
            curDotY = my - (my - y) + ((my - y) - ((my - y) % realChunkSize));
            curDotDist = (int)Math.sqrt((e.getX()-curDotX)*(e.getX()-curDotX) + (e.getY()-curDotY)*(e.getY()-curDotY));
        }
        repaint();
    }

    public void setBackground(String name) {
        gameLevel.setBackground(name);
        //backgroundImage = ObjectsChooserPanel.getImageAtPath(this, name);
        invalidateBackgroud();
        repaint();
    }

    public void showGrid(boolean show) {
        if (gridIsVisible == show) return;
        gridIsVisible = show;
        repaint();
    }

    public void showBackgound(boolean show) {
        if (backgroundIsVisible == show) return;
        backgroundIsVisible = show;
        invalidateBackgroud();
        repaint();
    }


    private boolean checkSizeToChange(BufferedImage image) {
        if (image == null) return true;
        if (image.getWidth() != getWidth() || image.getHeight() != getHeight()) return true;
        return false;
    }

    public void invalidateBackgroud() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        if (checkSizeToChange(backgroundBufferedImage)) {
            backgroundBufferedImage = getGraphicsConfiguration().createCompatibleImage(
                getWidth(), getHeight()
            );
        }
        Graphics2D g2 = backgroundBufferedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2.setBackground(Color.lightGray);
        g2.clearRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.black);
        if (backgroundIsVisible && gameLevel.getBackgroundImage() != null)
            g2.drawImage(gameLevel.getBackgroundImage(), grid.getOffsetX(), grid.getOffsetY(), grid.getGridScreenWidth(), grid.getGridScreenHeight(), this);
        g2.dispose();
    }

    public void invalidateGameLevel() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        BufferedImage gameLevelBufferedImage;
        if (currentLayer == 0) {
            gameLevelBufferedImage = gameLevelBufferedImage0;
        } else if (currentLayer == 1) {
            gameLevelBufferedImage = gameLevelBufferedImage1;
        } else if (currentLayer == 2) {
            gameLevelBufferedImage = gameLevelBufferedImage2;
        } else {
            gameLevelBufferedImage = gameLevelBufferedImage3;
        }
        if (checkSizeToChange(gameLevelBufferedImage)) {
            gameLevelBufferedImage = getGraphicsConfiguration().createCompatibleImage(
                getWidth(), getHeight(), Transparency.TRANSLUCENT
            );
        }
        Graphics2D g2 = setTransparentBack(gameLevelBufferedImage);

        g2.setColor(Color.black);
        if (currentLayer == 0) {
            gameLevelBufferedImage0 = gameLevelBufferedImage;
        } else if (currentLayer == 1) {
            gameLevelBufferedImage1 = gameLevelBufferedImage;
        } else if (currentLayer == 2) {
            gameLevelBufferedImage2 = gameLevelBufferedImage;
        } else {
            gameLevelBufferedImage3 = gameLevelBufferedImage;
        }
    }


    private Graphics2D setTransparentBack(BufferedImage image) {
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2.setBackground(new Color(0x00000000, true));
        g2.clearRect(0, 0, getWidth(), getHeight());
        return g2;
    }

    public void invalidateTop() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        if (checkSizeToChange(topBufferedImage)) {
            topBufferedImage = getGraphicsConfiguration().createCompatibleImage(
                getWidth(), getHeight(), Transparency.TRANSLUCENT
            );
        }

        Graphics2D g2 = setTransparentBack(topBufferedImage);

    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        /*if (e.getKeyCode() == KeyEvent.VK_PLUS) {
            //shiftTiles(1);
            worldEditorPanelOffsetScale++;
        } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            //shiftTiles(-1);
            worldEditorPanelOffsetScale--;
        }
        repaint();*/
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //shiftTiles(-1);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (mouseIsInGrid(e.getX(), e.getY())) {
            gameLevel.curAngle++;
            if (gameLevel.curAngle > 3) gameLevel.curAngle = 0;
        } else {
            if (e.getWheelRotation() < 0)worldEditorPanelOffsetScale++;
            else worldEditorPanelOffsetScale--;
            if (worldEditorPanelOffsetScale < 0) worldEditorPanelOffsetScale = 0;
            repaint();
        }
    }
}
