/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;

import tank.ObjectsChooserPanel.Rect;

/**
 *
 * @author IVO
 */
public class Grid {
    private int width, cellWidth = 10;
    private int height, cellHeight = 10;
    private int panelWidth;
    private int panelHeight;
    private int[] lineX = null;
    private int[] lineY = null;
    private boolean toCenter = true;
    private float maxWidth = 1000;

    public Grid() {}

    public Grid(boolean toCenter) {
        this.toCenter = toCenter;
    }

    public static class Point {
        public int x;
        public int y;

        public Point() {
            x = 0;
            y = 0;
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private void initSize(int w, int h) {
        width = w;
        height = h;
        lineX = new int[width + 1];
        lineY = new int[height + 1];
    }

    private void recalc(){
        double kPanel, kLevel, kScale;
        int xStep, yStep;
        int i, t, levelWidth, levelHeight;
        int idealWidth = width /** 10*/ * cellWidth;
        int idealHeight = height /** 10*/ * cellHeight;
        int xOffset = 0, yOffset = 0;

        kPanel = (panelHeight == 0) ? 0 : (double)panelWidth / (double)panelHeight;
        kLevel = (height == 0) ? 0 : (double) width / (double)height;

        int levelWidthNoOne = panelWidth - 1;
        int levelHeightNoOne = panelHeight - 1;

        if ((kPanel > kLevel) || (kPanel >= 1 && kLevel < 1)) {
            kScale = (double)panelHeight / (double) idealHeight;
            levelWidth = (int)(idealWidth * kScale);
            levelHeight = levelHeightNoOne;
        } else {
            kScale = (double)panelWidth / (double) idealWidth;
            levelWidth = levelWidthNoOne;
            levelHeight = (int)(idealHeight * kScale);
        }

        if (toCenter) {
            xOffset = (int)((double)(panelWidth-levelWidth)/2.0);
            yOffset = (int)((double)(panelHeight-levelHeight)/2.0);
        }

        for(i=0; i<=height; ++i){
            for(t=0; t<=width; ++t){
                xStep = (int)((double)levelWidth/(double)width*(double)t);
                yStep = (int)((double)levelHeight/(double)height*(double)i);
                lineY[i] = yOffset + yStep;
                lineX[t] = xOffset + xStep;
            }
        }
    }

    public Point getCell(int mouseX, int mouseY) {
        Point point = new Point(-1, -1);
        if (mouseX < lineX[0] || mouseX > lineX[width]) {
            return point;
        }
        if (mouseY < lineY[0] || mouseY > lineY[height]) {
            return point;
        }
        int i;
        for (i = 1; i < lineX.length; ++i) {
            if (mouseX < lineX[i]) {
                point.x = i - 1;
                break;
            }
        }
        for (i = 1; i < lineY.length; ++i) {
            if (mouseY < lineY[i]) {
                point.y = i - 1;
                break;
            }
        }
        return point;
    }

    /**
     * by cells
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * by cells
     * @return
     */
    public int getHeight() {
        return height;
    }

    public void setSize(int w, int h, int panelWidth, int panelHeight) {
        if (w < 1 || h < 1) return;
        if (panelWidth < 1 || panelHeight < 1) return;
        this.panelWidth = Math.min(panelWidth, (int)maxWidth);
        this.panelHeight = Math.min(panelHeight, (int)maxWidth);
        initSize(w, h);
        recalc();
    }

    public void setCellSize(int cw, int ch, int panelWidth, int panelHeight) {
        cellWidth = cw;
        cellHeight = ch;
        recalc();
    }

    public void setSize(int w, int h) {
        initSize(w, h);
        recalc();
    }

    public int getLineX(int x) {
        if (x < 0 || x > width || width == 0) return -1;
        return lineX[x];
    }

    public int getLineY(int y) {
        if (y < 0 || y > height || height == 0) return -1;
        return lineY[y];
    }

    public boolean isNull() {
        return (lineX == null || lineY == null || width == 0 || height == 0);
    }

    public int getOffsetX() {
        if (lineX == null) return 0;
        return lineX[0];
    }

    public int getOffsetY() {
        if (lineY == null) return 0;
        return lineY[0];
    }

    public int getGridScreenWidth() {
        if (lineX == null) return 0;
        return lineX[lineX.length-1]-lineX[0];
    }

    public int getGridScreenHeight() {
        if (lineY == null) return 0;
        return lineY[lineY.length-1]-lineY[0];
    }

    public int getApproximatelyCellScreenSize() {
        if (lineX == null) return 0;
        return lineX[1]-lineX[0];
    }

    public int getCellMediumWidth() {
        return lineX[1] - lineX[0];
    }

    public int getCellMediumHeight() {
        return lineY[1] - lineY[0];
    }


    /**
     * get cell rct
     * @param cell coords x
     * @param y
     * @return
     */
    public Rect getCellRect(int x, int y) {
        if (x < 0 || x >= width || width == 0) return null;
        if (y < 0 || y >= height || height == 0) return null;
        Rect rect = new Rect();
        rect.left = lineX[x];
        rect.top = lineY[y];
        rect.right = lineX[x+1];
        rect.bottom = lineY[y+1];
        return rect;
    }

    public Rect getCellRectByScreenPos(int x, int y) {
        Rect rect = new Rect();
        int w = getCellMediumWidth();
        int h = getCellMediumHeight();
        rect.left = x - w / 2;
        rect.top = y - h / 2;
        rect.right = rect.left + w;
        rect.bottom = rect.top + h;
        return rect;

    }

    public Point getCellCenter(int x, int y) {
        if (x < 0 || x >= width || width == 0) return null;
        if (y < 0 || y >= height || height == 0) return null;
        Point pos = new Point();
        pos.x = lineX[x]+(lineX[x+1]-lineX[x])/2;
        pos.y = lineY[y]+(lineY[y+1]-lineY[y])/2;
        return pos;
    }
}
