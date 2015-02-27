/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import tank.GameLevel.Platform;
import tank.Grid.Point;
import tank.ObjectsChooserPanel.Rect;

/**
 *
 * @author IVO
 */
public class EditorLevelPanel extends JPanel
        implements MouseListener, MouseMotionListener {
    BufferedImage backgroundBufferedImage = null;
    BufferedImage gameLevelBufferedImage0 = null;
    BufferedImage gameLevelBufferedImage1 = null;
    BufferedImage gameLevelBufferedImage2 = null;
    BufferedImage platformsBufferedImage = null;
    BufferedImage topBufferedImage = null;
    public GameLevel gameLevel =  new GameLevel(this);
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private int mouseButton;
    private Grid grid = new Grid();
    private Image platformStartImage = null;
    private Image platformFinishImage = null;
    private int platformKeyRadius;
    private boolean gridIsVisible = true;
    private boolean backgroundIsVisible = true;
    private GameLevel.Platform tempPlatform = new GameLevel.Platform();
    private boolean platformCreation = false;
    private int mouseX;
    private int mouseY;
    private Point[][] platformKeys = null;
    private Color[] platformColors = new Color[8];
    private java.awt.Frame parentFrame;
    int currentLayer = 1;

    public void  setObjectsChooserPanel(ObjectsChooserPanel panel) {
        panel.setOnChooser(new ObjectsChooserPanel.ChooserListener() {
            public void onChoose(int i) {
                cancelPlatformCreation();
            }
        });
        gameLevel.setObjectsChooserPanel(panel);
    }

    private int getOffset(int layer) {
        if (layer == 0) {
            return grid.getCellRect(0, 0).height()/4;
        } else if (layer == 1) {
            return 0;
        } else {
            return -grid.getCellRect(0, 0).height();
        }
    }

    public void saveLevel(String fileName) {
        gameLevel.save(fileName);
    }

    public void addLevel(String fileName) {
        gameLevel.add(fileName);
    }

    public void loadLevel(String fileName) {
        gameLevel.load(fileName);
        cancelPlatformCreation();
        gridSizeChanged();
/*        if (gameLevel.getBackground() != null) {
            backgroundImage = ObjectsChooserPanel.getImageAtPath(this, gameLevel.getBackground());
        } else {
            backgroundImage = null;
        }*/
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
        newPlatformKeys(gameLevel.getWidth(), gameLevel.getHeight());
        setPlatformKeys();
        platformKeyRadius = grid.getApproximatelyCellScreenSize()/6;
    }

    private void drawPlatformKey(Graphics2D g2, int col, int row) {
        if (col < 0 || col >= grid.getWidth() || row < 0 || row >= grid.getHeight()) return;
        int currentKey = platformKeys[col][row].x;
        if (currentKey == Platform.PLATFORM_START) {
            drawImage(g2, platformStartImage, col, row);
        } else if (currentKey == Platform.PLATFORM_KEY) {
            //drawImage(g2, platformKeyImage, col, row);
            Point pos;
            pos = grid.getCellCenter(col, row);
            int circleSize = platformKeyRadius*2;
            g2.fillOval(pos.x-platformKeyRadius, pos.y-platformKeyRadius, circleSize, circleSize);
        } else if (currentKey == Platform.PLATFORM_FINISH) {
            drawImage(g2, platformFinishImage, col, row);
        }
    }

    private void invalidatePlatformCreation() {
        platformCreation = false;
        setPlatformKeys();
        invalidateGameLevel();
        invalidateTop();
        repaint();
    }

    private void newPlatformKeys(int width, int height) {
        platformKeys = new Point[width][height];
        int x, y;
        for (x = 0; x < width; ++x) {
            for (y = 0; y < height; ++y) {
                platformKeys[x][y] = new Point();
            }
        }
    }

    private void clearPlatformKeys(int width, int height) {
        int x, y;
        for (y = 0; y < height; ++y) {
            for (x = 0; x < width; ++x) {
                platformKeys[x][y].x = 0;
                platformKeys[x][y].y = 0;
            }
        }
    }

    private void setPlatformKeys() {
        if (tempPlatform.posArray == null && gameLevel.getPlatformCount() == 0) return;
        clearPlatformKeys(grid.getWidth(), grid.getHeight());
        Platform currentPlatform;
        int ix = 0;
        Iterator<Platform> platformIterator = gameLevel.getPlatformIterator();
        while (platformIterator.hasNext()) {
            currentPlatform = platformIterator.next();
            if (currentPlatform.posArray == null) continue;
            setPlatformKeys(currentPlatform, ix);
            ix++;
        }
        if (platformCreation) setPlatformKeys(tempPlatform, ix);
    }

    private void setPlatformKeys(Platform platform, int ix) {
        if (platform.posArray == null) return;
        Iterator<Point> pointIterator;
        Point pos;
        int currentKey = 0;
        pointIterator = platform.posArray.iterator();
        while (pointIterator.hasNext()) {
            pos = pointIterator.next();
            if (currentKey == 0) currentKey = Platform.PLATFORM_START;
            else if(currentKey == Platform.PLATFORM_START) {
                if (!pointIterator.hasNext())
                    currentKey = Platform.PLATFORM_FINISH;
                else
                    currentKey = Platform.PLATFORM_KEY;
            }
            else if(!pointIterator.hasNext()) currentKey = Platform.PLATFORM_FINISH;
            if (platformKeys[pos.x][pos.y].x == Platform.PLATFORM_START) continue;
            setPlatformKey (pos.x, pos.y, currentKey, ix);
        }
    }

    private int getCurrentTempPlatformKey() {
        if (tempPlatform.posArray == null) return 0;
        if (tempPlatform.posArray.size() == 1) return Platform.PLATFORM_START;
        return Platform.PLATFORM_KEY;
    }

    private void setPlatformKey(int x, int y, int key, int ix) {
        if (key == 0) return;
        if (x < 0 || x >= grid.getWidth() || y < 0 || y >= grid.getHeight()) return;
        if (platformKeys[x][y].x == Platform.PLATFORM_START) return;
        if ((platformKeys[x][y].x == 0)
                || (platformKeys[x][y].x == Platform.PLATFORM_KEY
                && key != Platform.PLATFORM_KEY)
                || (platformKeys[x][y].x == Platform.PLATFORM_FINISH
                && key == Platform.PLATFORM_START))
            platformKeys[x][y].x = key;
            platformKeys[x][y].y = ix;
    }

    private void drawPlatform(Graphics g) {
        Point centerPoint = grid.getCell(mouseX, mouseY);
        if (centerPoint.x < 0 || centerPoint.y < 0) return;
        Point leftPoint = new Point();
        Point rightPoint = new Point();
        if ((tempPlatform.getSize() % 2) == 0) {
            leftPoint.x = centerPoint.x - (tempPlatform.getSize() / 2 - 1);
        } else {
            leftPoint.x = centerPoint.x - tempPlatform.getSize() / 2;
        }
        leftPoint.y = rightPoint.y = centerPoint.y;
        rightPoint.x = Math.min(grid.getWidth() - 1, leftPoint.x + tempPlatform.getSize() - 1);
        leftPoint.x = Math.max(0, leftPoint.x);
        Rect leftCellRect = grid.getCellRect(leftPoint.x, leftPoint.y);
        Rect rightCellRect = grid.getCellRect(rightPoint.x, rightPoint.y);
        if (leftCellRect != null && rightCellRect != null) {
            g.setColor(Color.black);
            g.fillRect(leftCellRect.left, leftCellRect.top,
                    rightCellRect.right - leftCellRect.left, leftCellRect.height());
        }
    }


    private void drawPlatformSymbol(Graphics g) {
        if (tempPlatform == null) return;
        Point centerPoint = grid.getCell(mouseX, mouseY);
        if (centerPoint.x < 0 || centerPoint.y < 0) return;
        Rect centerCellRect = grid.getCellRect(centerPoint.x, centerPoint.y);
        if (centerCellRect != null) {
            if (tempPlatform.posArray == null)
                g.drawImage(platformStartImage, centerCellRect.left, centerCellRect.top,
                    centerCellRect.width(), centerCellRect.height(), this);
            else
                g.drawImage(platformFinishImage, centerCellRect.left, centerCellRect.top,
                    centerCellRect.width(), centerCellRect.height(), this);
        }
    }

    private void invalidateAll() {
        invalidateBackgroud();
        invalidateGameLevel();
        invalidatePlatforms();
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
        g2.drawImage(image, grid.getLineX(col), grid.getLineY(row)+cellRect.height()-imageTrueHeight+offsetY, cellRect.width(), imageTrueHeight,  this);
    }

    private boolean fillCell(int x, int y, int item) {
        Grid.Point cell = grid.getCell(x, y);
        selectedRow = cell.y;
        selectedColumn = cell.x;
        if (selectedRow < 0 || selectedColumn < 0) return false;
        int oldValue = gameLevel.getCell(selectedColumn, selectedRow, currentLayer);
        gameLevel.setCell(selectedColumn, selectedRow, currentLayer, item);

        // repaint cell
        /*if (oldValue == item) return true;

        Rect cellRect = grid.getCellRect(cell.x, cell.y);
        if (cellRect == null) return false;
        Graphics2D g2;
        int offseY = getOffset(currentLayer);
        if (currentLayer == 0) {
            g2 = gameLevelBufferedImage0.createGraphics();
        } else if (currentLayer == 1) {
            g2 = gameLevelBufferedImage1.createGraphics();
        } else {
            g2 = gameLevelBufferedImage2.createGraphics();
        }
        if (oldValue > -1) {
            g2.setBackground(new Color(0x00000000, true));
            g2.clearRect(cellRect.left, 0, cellRect.width(), cellRect.height()*getLevelHeight());
        }
        //int intm;
        for (int i = 0; i < getLevelHeight(); ++i) {
            if (currentLayer == 0) {
                item = gameLevel.content0[cell.x][i];
            } else if (currentLayer == 1) {
                item = gameLevel.content1[cell.x][i];
            } else {
                item = gameLevel.content2[cell.x][i];
            }
            cellRect = grid.getCellRect(cell.x, i);
            if (item != -1) {
                Image image = gameLevel.getObjectsChooserPanel().getImage(item);
                ImageObserver io = new ImageObserver() {
                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        return false;
                    }
                };
                int imageWidth = image.getWidth(io);
                int imageHeight = image.getHeight(io);
                int imageTrueHeight = cellRect.width() * (imageHeight / imageWidth);
                //g2.drawImage(image, cellRect.left, cellRect.bottom - imageTrueHeight+offseY, cellRect.width(), imageTrueHeight, this);
            }
        }
        drawPlatformKey(g2, selectedColumn, selectedRow);*/
        return true;
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public EditorLevelPanel(java.awt.Frame parent) {
        parentFrame = parent;
        addMouseListener(this);
        addMouseMotionListener(this);
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pressedEscape");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "pressedEnter");


        platformStartImage = ObjectsChooserPanel.getImage(this, "plat_start.png");
        platformFinishImage = ObjectsChooserPanel.getImage(this, "plat_finish.png");

        platformColors[0] = Color.RED;
        platformColors[1] = Color.BLUE;
        platformColors[2] = Color.GREEN;
        platformColors[3] = Color.ORANGE;
        platformColors[4] = Color.MAGENTA;
        platformColors[5] = Color.DARK_GRAY;
        platformColors[6] = Color.BLACK;
        platformColors[7] = new Color(0xff990000);
    }

    public void cancelPlatformCreation() {
        if (!platformCreation || tempPlatform.posArray == null) return;
        tempPlatform.posArray.clear();
        invalidatePlatformCreation();
    }

    public void applyPlatform() {
        if (!platformCreation || tempPlatform.posArray == null
                || tempPlatform.posArray.size() <= 1) return;
        gameLevel.addPlatform(tempPlatform);
        tempPlatform = new GameLevel.Platform();
        invalidatePlatforms();
        invalidatePlatformCreation();
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

    public void setAreaCoords(int width, int height) {
        gameLevel.setAreaCoords(width, height);
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

    public void drawLayer(Graphics g, ImageObserver io, int n) {
        for (int i = 0; i < getLevelWidth(); ++i) {
            for (int t = 0; t < getLevelHeight(); ++t) {
                if ((gameLevel.content2[i][t] != -1 && n == 2) || (gameLevel.content1[i][t] != -1 && n == 1) || (gameLevel.content0[i][t] != -1 && n == 0)) {
                    Rect cell = grid.getCellRect(i, t);
                    int imageIndex = gameLevel.getCell(i, t, n);
                    Image image = gameLevel.getObjectsChooserPanel().getImage(imageIndex);
                    int width, height;
                    if (imageIndex < gameLevel.getObjectsChooserPanel().imagesCount) {
                        width = image.getWidth(io);
                        height = image.getHeight(io);
                    } else {
                        width = image.getWidth(io)/3;
                        height = image.getHeight(io)/4;
                    }
                    float test1 = (float)height/(float)width;
                    int imageTrueHeight = (int)(cell.width() * test1);
                    //g.drawImage(image, cell.left, cell.bottom-imageTrueHeight+getOffset(n), cell.width(), imageTrueHeight, io);
                    if (imageIndex < gameLevel.getObjectsChooserPanel().imagesCount) {
                        g.drawImage(image, cell.left, cell.bottom - imageTrueHeight + getOffset(n), cell.width(), imageTrueHeight, io);
                    } else {
                        boolean left = (i > 0 && ((gameLevel.content2[i-1][t] == imageIndex && n == 2) || (gameLevel.content1[i-1][t] == imageIndex && n == 1) || (gameLevel.content0[i-1][t] == imageIndex && n == 0)));
                        boolean right = (i < getLevelWidth()-1 && ((gameLevel.content2[i+1][t] == imageIndex && n == 2) || (gameLevel.content1[i+1][t] == imageIndex && n == 1) || (gameLevel.content0[i+1][t] == imageIndex && n == 0)));
                        boolean up = (t > 0 && ((gameLevel.content2[i][t-1] == imageIndex && n == 2) || (gameLevel.content1[i][t-1] == imageIndex && n == 1) || (gameLevel.content0[i][t-1] == imageIndex && n == 0)));
                        boolean down = (t < getLevelHeight()-1 && ((gameLevel.content2[i][t+1] == imageIndex && n == 2) || (gameLevel.content1[i][t+1] == imageIndex && n == 1) || (gameLevel.content0[i][t+1] == imageIndex && n == 0)));
                        int tileX = getRightTile(left, right, up, down).x;
                        int tileY = getRightTile(left, right, up, down).y;
                        if (getTileInverted(left, right, up, down)) {
                            g.drawImage(image, cell.left, cell.bottom - imageTrueHeight + getOffset(n), cell.left+cell.width(), cell.bottom - imageTrueHeight + getOffset(n)+imageTrueHeight,
                                    width*tileX+width, height*tileY, width*tileX, height*tileY+height, this);
                        } else {
                            g.drawImage(image, cell.left, cell.bottom - imageTrueHeight + getOffset(n), cell.left+cell.width(), cell.bottom - imageTrueHeight + getOffset(n)+imageTrueHeight,
                                    width*tileX, height*tileY, width*tileX+width, height*tileY+height, this);
                        }
                }
                }
            }
        }

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
        if (gridIsVisible) drawGrid(g);
        if (platformCreation) drawPlatform(g);
        if (gameLevelBufferedImage0!=null) g.drawImage(gameLevelBufferedImage0, 0, 0, gameLevelBufferedImage0.getWidth(),
                    gameLevelBufferedImage0.getHeight(), this);
        if (gameLevelBufferedImage1!=null) g.drawImage(gameLevelBufferedImage1, 0, 0, gameLevelBufferedImage1.getWidth(),
                    gameLevelBufferedImage1.getHeight(), this);
        if (gameLevelBufferedImage2!=null) g.drawImage(gameLevelBufferedImage2, 0, 0, gameLevelBufferedImage2.getWidth(),
                    gameLevelBufferedImage2.getHeight(), this);
        ImageObserver io = new ImageObserver() {
            @Override
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return false;
            }
        };

        drawLayer(g, io, 0);
        drawLayer(g, io, 1);
        drawLayer(g, io, 2);
        if (platformCreation) {
            if (tempPlatform.posArray != null && tempPlatform.posArray.size()>0) {
                Point pos, lastPos, currentCell, currentPos;
                pos = tempPlatform.posArray.getLast();
                lastPos = grid.getCellCenter(pos.x, pos.y);
                currentCell = grid.getCell(mouseX, mouseY);
                if (currentCell.x >= 0 || currentCell.y >= 0) {
                    currentPos = grid.getCellCenter(currentCell.x, currentCell.y);
                    if (currentPos != null) {
                        g.setColor(platformColors[gameLevel.getPlatformCount()%8]);
                        g.drawLine(currentPos.x, currentPos.y, lastPos.x, lastPos.y);
                    }
                }
            }
            drawPlatformSymbol(g);
        }
        if (gameLevel.getPlatformCount() > 0)
            g.drawImage(platformsBufferedImage, 0, 0, platformsBufferedImage.getWidth(),
                    platformsBufferedImage.getHeight(), this);
        if (platformCreation && topBufferedImage != null)
            g.drawImage(topBufferedImage, 0, 0, topBufferedImage.getWidth(),
                    topBufferedImage.getHeight(), this);
        drawPlatformsLines(g);
    }

    private void drawPlatformsLines(Graphics g) {
        if (tempPlatform.posArray == null && gameLevel.getPlatformCount() == 0) return;
        Platform currentPlatform;
        int currentPlatformIx = 0;
        Iterator<Platform> platformIterator = gameLevel.getPlatformIterator();
        while (platformIterator.hasNext()) {
            currentPlatform = platformIterator.next();
            g.setColor(platformColors[currentPlatformIx%8]);
            currentPlatformIx++;
            drawPlatformLines(g, currentPlatform);
        }
        g.setColor(platformColors[currentPlatformIx%8]);
        if (platformCreation) drawPlatformLines(g, tempPlatform);
    }

    private void drawPlatformLines(Graphics g, Platform platform) {
        Point pos, center, prev_center = new Point();
        if (platform.posArray != null) {
            Iterator<Point> iterator = platform.posArray.iterator();
            boolean first = true;
            while(iterator.hasNext()) {
                pos = iterator.next();
                center = grid.getCellCenter(pos.x, pos.y);
                if (!first) {
                    g.drawLine(prev_center.x, prev_center.y, center.x, center.y);
                } else first = false;
                prev_center = center;
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        mouseButton = e.getButton();
        if (platformCreation) {
            Point cellPos = grid.getCell(mouseX, mouseY);
            if (cellPos.x >= 0 && cellPos.y >= 0) {
                Point pos;

                if (tempPlatform.posArray == null) tempPlatform.posArray = new ArrayDeque<Point>();
                Iterator<Point> iterator = tempPlatform.posArray.iterator();
                while(iterator.hasNext()) {
                    pos = iterator.next();
                    if (pos.x == cellPos.x && pos.y == cellPos.y) return;
                }
                pos = new Point();
                pos.x = cellPos.x;
                pos.y = cellPos.y;
                tempPlatform.posArray.add(pos);
                setPlatformKey(pos.x, pos.y, getCurrentTempPlatformKey(), gameLevel.getPlatformCount());
                invalidateGameLevel();
                invalidateTop();
                repaint();
                return;
            }
        }
        mouseEvent(e);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        mouseEvent(e);
    }

    private void mouseEvent(MouseEvent e) {
        if (platformCreation) return;
        if (mouseButton == MouseEvent.BUTTON1
                && gameLevel.getObjectsChooserPanel().getSelectedObject() != -1) {
            if (fillCell(e.getX(), e.getY(), gameLevel.getObjectsChooserPanel().getSelectedObject())) {
                repaint();
            }
        } else if (mouseButton == MouseEvent.BUTTON3) {
            if (fillCell(e.getX(), e.getY(), -1)) {
                repaint();
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (platformCreation) {
            mouseX = e.getX();
            mouseY = e.getY();
            invalidateTop();
            repaint();
        }
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

    public void createPlatform(int size, int speed, int delay) {
        tempPlatform.setSize(size);
        tempPlatform.movingSpeed = speed;
        tempPlatform.delay = delay;
        platformCreation = true;
        invalidateTop();
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
        } else {
            gameLevelBufferedImage = gameLevelBufferedImage2;
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
        } else {
            gameLevelBufferedImage2 = gameLevelBufferedImage;
        }
    }

    public void invalidatePlatforms() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        if (checkSizeToChange(platformsBufferedImage)) {
            platformsBufferedImage = getGraphicsConfiguration().createCompatibleImage(
                getWidth(), getHeight(), Transparency.TRANSLUCENT
            );
        }
        Graphics2D g2 = setTransparentBack(platformsBufferedImage);
        Iterator<Platform> platformIterator = gameLevel.getPlatformIterator();
        Platform currentPlatform;
        while (platformIterator.hasNext()) {
            currentPlatform = platformIterator.next();
            //drawPlatformDecoration(g2, currentPlatform);
        }

/*        g2.setColor(Color.black);
        int i, t;
        int imageIndex;
        Image image;
        int currentPlatformIx = 0;
        for (i=0; i<getLevelHeight(); ++i){
            for (t=0; t<getLevelWidth(); ++t){
                g2.setColor(platformColors[platformKeys[t][i].y%8]);
                imageIndex = gameLevel.getCell(t, i);
                if (imageIndex >= 0) {
                    image = gameLevel.getObjectsChooserPanel().getImage(imageIndex);
                    drawImage(g2, image, t, i);
                }
                drawPlatformKey(g2, t, i);
            }
            currentPlatformIx++;
        }*/
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

        if (platformCreation) {
            // draw caption
            float fontSize = (float)getWidth() * 0.03f;
            Font gFont = new Font("Tahoma", Font.BOLD, (int)fontSize);
            g2.setColor(Color.orange);
            g2.setFont(gFont);
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout tl;
            if (tempPlatform.posArray != null && tempPlatform.posArray.size() > 0)
                tl = new TextLayout("Set next point. (ESC - cancel, ENTER - apply)", g2.getFont(), frc);
            else
                tl = new TextLayout("Set start point. (ESC - cancel)", g2.getFont(), frc);
            float xx = (float)((getWidth()-tl.getBounds().getWidth())/2);
            Shape shape = tl.getOutline(null);
            BasicStroke sroke = new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2.setStroke(sroke);
            AffineTransform Tx = AffineTransform.getTranslateInstance(xx, tl.getAscent()+10);
            shape = Tx.createTransformedShape(shape);
            Color paint = new Color(0x55000000);
            Paint oldPaint = g2.getPaint();
            g2.setPaint(paint);
            g2.draw(shape);
            g2.setPaint(oldPaint);
            tl.draw(g2, xx, tl.getAscent()+10);
            g2.dispose();
        }
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }


}
