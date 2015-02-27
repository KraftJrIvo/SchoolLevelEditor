/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import tank.GameLevel.ImageForPalette;

/**
 *
 * @author IVO
 */
public class PlatformPalettePanel extends JPanel implements MouseListener, MouseMotionListener {
    private int imgColumnsCount = 20;
    private int imgLinesCount = 5;
//    private ArrayDeque<ImageForPalette> images = new ArrayDeque<ImageForPalette>();
//    ImageForPalette[] imageArray = null;
    private GameLevel gameLevel;
    private int selectedImage = -1;
    private Grid grid = new Grid(true);

    public int getSelectedImageID() {
        return selectedImage;
    }

    public interface ChooserListener {
        void onChoose(int i);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        grid.setSize(getWidth()/20, getHeight()/20, getWidth(), getHeight());
    }

    public ImageForPalette getImageForPalette(int id) {
        return gameLevel.getPlatformPaletteImage(id);
    }

    public void deleteSelectedItem(){
        gameLevel.deleteImageFromPlatformPalette(selectedImage);
        selectedImage = -1;
        repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    private void mouseEvent(MouseEvent e) {
        if (gameLevel.getPlatformPaletteSize() <= 0) return;
        int mouseX = e.getX();
        int mouseY = e.getY();
        Grid.Point cellCoord = grid.getCell(mouseX, mouseY);
        selectedImage = cellCoord.y * grid.getWidth() + cellCoord.x;
        if (selectedImage >= gameLevel.getPlatformPaletteSize())
            selectedImage = -1;
        repaint();
    }

    public void mousePressed(MouseEvent e) {
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

    public void mouseMoved(MouseEvent e) {
    }

    public static class Rect {
        public int left = 0;
        public int right = 0;
        public int top = 0;
        public int bottom = 0;
        public int width() {
            return right - left;
        }
        public int height() {
            return bottom - top;
        }
    }

    public PlatformPalettePanel(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
        grid.setSize(imgColumnsCount, imgLinesCount, getWidth(), getHeight());
        addMouseListener((MouseListener)this);
        addMouseMotionListener((MouseMotionListener)this);
    }

    @Override
    public void paint(Graphics g) {
        if (grid.isNull()) return;
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        int x, y;
        ObjectsChooserPanel.Rect rect;
        for (int i = 0; i<gameLevel.getPlatformPaletteSize(); ++i) {
            x = i % grid.getWidth();
            y = i / grid.getWidth();
            rect = grid.getCellRect(x, y);
            g.drawImage(gameLevel.getPlatformPaletteImage(i).image,
                    rect.left, rect.top, rect.width(), rect.height(), this);
        }
        g.setColor(Color.black);
        for (x = 0; x <= grid.getWidth(); ++x) {
            g.drawLine(grid.getLineX(x), grid.getLineY(0), grid.getLineX(x), grid.getLineY(grid.getHeight()));
        }
        for (y = 0; y <= grid.getHeight(); ++y) {
            g.drawLine(grid.getLineX(0), grid.getLineY(y), grid.getLineX(grid.getWidth()), grid.getLineY(y));
        }
        if (selectedImage > -1) {
            x = selectedImage%grid.getWidth();
            y = selectedImage/grid.getWidth();
            rect = grid.getCellRect(x, y);
            g.setColor(Color.red);
            g.drawRect(rect.left, rect.top, rect.width(), rect.height());
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 100);
    }
}
