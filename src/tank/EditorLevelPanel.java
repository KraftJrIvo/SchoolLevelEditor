/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;


import java.awt.*;

import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

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
        implements MouseListener, MouseMotionListener, KeyListener {
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

    public void  setObjectsChooserPanel(ObjectsChooserPanel panel) {
        gameLevel.setObjectsChooserPanel(panel);
    }

    private int getOffset(int layer, int texHeight) {
        if (layer == 0) {
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

    public void loadLevel(String fileName) {
        gameLevel.load(fileName);
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

    public void drawLayer(Graphics g, ImageObserver io, int n) {
        for (int i = 0; i < getLevelWidth(); ++i) {
            for (int t = 0; t < getLevelHeight(); ++t) {
                if ((gameLevel.content2[i][t] != -1 && n == 2) || (gameLevel.content1[i][t] != -1 && n == 1) || (gameLevel.content0[i][t] != -1 && n == 0) || (gameLevel.content3[i][t] != -1 && n == 3)) {
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
                    int imageTrueWidth = (int)(image.getWidth(this)*cell.width()/gameLevel.tileWidth);
                    int imageTrueHeight = (int)(image.getHeight(this)*cell.height()/gameLevel.tileHeight);
                    //g.drawImage(image, cell.left, cell.bottom-imageTrueHeight+getOffset(n), cell.width(), imageTrueHeight, io);
                    if (n != 3) {
                        if (imageIndex < gameLevel.getObjectsChooserPanel().imagesCount) {
                            g.drawImage(image, cell.left+cell.width()/2-imageTrueWidth/2, cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight), imageTrueWidth, imageTrueHeight, io);
                        } else {
                            imageTrueHeight = (int)(cell.width() * test1);
                            boolean left = (i > 0 && ((gameLevel.content2[i - 1][t] == imageIndex && n == 2) || (gameLevel.content1[i - 1][t] == imageIndex && n == 1) || (gameLevel.content0[i-1][t] == imageIndex && n == 0) || (gameLevel.content3[i-1][t] == imageIndex && n == 3)));
                            boolean right = (i < getLevelWidth()-1 && ((gameLevel.content2[i+1][t] == imageIndex && n == 2) || (gameLevel.content1[i+1][t] == imageIndex && n == 1) || (gameLevel.content0[i+1][t] == imageIndex && n == 0) || (gameLevel.content3[i+1][t] == imageIndex && n == 3)));
                            boolean up = (t > 0 && ((gameLevel.content2[i][t-1] == imageIndex && n == 2) || (gameLevel.content1[i][t-1] == imageIndex && n == 1) || (gameLevel.content0[i][t-1] == imageIndex && n == 0) || (gameLevel.content3[i][t-1] == imageIndex && n == 3)));
                            boolean down = (t < getLevelHeight()-1 && ((gameLevel.content2[i][t+1] == imageIndex && n == 2) || (gameLevel.content1[i][t+1] == imageIndex && n == 1) || (gameLevel.content0[i][t+1] == imageIndex && n == 0) || (gameLevel.content3[i][t+1] == imageIndex && n == 3)));
                            int tileX = getRightTile(left, right, up, down).x;
                            int tileY = getRightTile(left, right, up, down).y;
                            if (getTileInverted(left, right, up, down)) {
                                g.drawImage(image, cell.left, cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight), cell.left+cell.width(), cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight)+imageTrueHeight,
                                        width*tileX+width, height*tileY, width*tileX, height*tileY+height, this);
                            } else {
                                g.drawImage(image, cell.left, cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight), cell.left+cell.width(), cell.bottom - imageTrueHeight + getOffset(n, imageTrueHeight)+imageTrueHeight,
                                        width*tileX, height*tileY, width*tileX+width, height*tileY+height, this);
                            }
                        }
                    } else {
                        g.setColor(new Color(1.0f, 0, 1.0f, 0.25f));
                        g.setFont(g.getFont().deriveFont(40.0f));
                        g.drawString(getTileString(gameLevel.content3[i][t]), cell.left, cell.bottom);
                    }
                }
            }
        }
        g.setColor(Color.WHITE);
    }

    private String getTileString(int i) {
        switch (i) {
            case 0: return "!S!";
            case 1: return "FLR";
            case 2: return "WAL";
            case 3: return "SCE";
            case 4: return "STA";
            case 5: return "DYN";
        }
        return "";
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
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        mouseButton = e.getButton();
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
        if (mouseButton == MouseEvent.BUTTON1
                && gameLevel.getObjectsChooserPanel().getSelectedObject() != -1) {
            if (fillCell(e.getX(), e.getY(), gameLevel.getObjectsChooserPanel().getSelectedObject())) {
                repaint();
            }
        } else if (mouseButton == MouseEvent.BUTTON3) {
            if (fillCell(e.getX(), e.getY(), -1)) {
                repaint();
            }
        } else if (mouseButton == MouseEvent.BUTTON2) {
                shiftTiles(1);
        }
    }

    public void mouseMoved(MouseEvent e) {
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
        if (e.getKeyCode() == KeyEvent.VK_PLUS) {
            shiftTiles(1);
        } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            shiftTiles(-1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
