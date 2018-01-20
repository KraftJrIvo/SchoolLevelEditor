/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;

import java.awt.Component;
import java.awt.Image;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import tank.Grid.Point;

/**
 *
 * @author IVO
 */
public class GameLevel {
    private Component parent;
    private ObjectsChooserPanel objectsChooserPanel = null;
    private ObjectsChooserPanel attributesChooserPanel = null;
    private String name = "Untitled1";
    //private int width = 32;
    //private int height = 32;
    public int width = 16;
    public int height = 16;
    public int chunkWidth = 4;
    public int chunkHeight = 8;
    public int tileWidth = 32, tileHeight = 16;
    public String roomName = "";
    public String roomAmbientName = "";
    boolean platformMode = false;
    private boolean changed = false;
    public int[][] content0 = null;
    public int[][] content1 = null;
    public int[][] content2 = null;
    //public int[][] content3 = null;

    public ArrayList<ArrayList<Integer>> roomsCoords;
    public ArrayList<String> roomsNames;
    public ArrayList<String> roomsAmbientNames;
    public ArrayList<ArrayList<ArrayList<Boolean>>> roomsWalls;
    int currentRoomId = -1;

    public int[][][] values = null;
    int curType=0;
    int curAngle=0;
    int curXOffset=0;
    int curYOffset=0;
    int curObjectWidth=0;
    int curObjectHeight=0;

    private String backgroundPath = null;
    private Image backgroundImage = null;
    private int tempPlatformDecorationPaletteSize = -1;
    private String versionString = "";
    private ArrayDeque<Platform> platforms = new ArrayDeque<Platform>();
    private ArrayDeque<ImageForPalette> platformPaletteImages = new ArrayDeque<ImageForPalette>();
    ImageForPalette[] platformPaletteImageArray = null;
    public int worldWidth = 5, worldHeight = 3, coordX = 0, coordY = 0, coordZ = 0;
    public int nextLevelX = 0, nextLevelY = 0;

    public static class ImageForPalette {
        public Image image = null;
        public String path = "";
    }

    public static class Platform {
        private byte[][] decorationArray = null;
        private int size = 0;
        public int movingSpeed;
        public int delay;
        public ArrayDeque<Point> posArray = null;
        public static final int PLATFORM_START = 1;
        public static final int PLATFORM_KEY = 2;
        public static final int PLATFORM_FINISH = 3;
        public byte getDecoration(int column, int row) {
            if (decorationArray == null) return -1;
            if (column < 0 || row < 0) return -1;
            if (column >= decorationArray.length) return -1;
            if (row >= decorationArray[0].length) return -1;
            return decorationArray[column][row];
        }
        public void setDecor(int column, int row, byte value) {
            if (decorationArray == null) return;
            if (column < 0 || row < 0) return;
            if (column >= decorationArray.length) return;
            if (row >= decorationArray[0].length) return;
            decorationArray[column][row] = value;
        }
        public void setSize(int newSize) {
            if (newSize < 1 || newSize > 10) return;
            size = newSize;
            decorationArray = new byte[size+2][3];
            int x, y;
            for (x = 0; x < getDecorationWidth(); ++x) {
                for (y = 0; y < 3; ++y) {
                    decorationArray[x][y] = -1;
                }
            }
        }
        public void saveDecoration(ZipOutputStream zos) {
            if (decorationArray == null) return;
            try {
                for (int x = 0; x < decorationArray.length; ++x) {
                    zos.write(decorationArray[x]);
                }
            } catch (IOException ex) {
                Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public void loadDecoration(InputStream iStream) {
            decorationArray = new byte[size+2][3];
            try {
                for (int x = 0; x < decorationArray.length; ++x) {
                    iStream.read(decorationArray[x]);
                }
            } catch (IOException ex) {
                Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public int getDecorationWidth() {
            return size+2;
        }
        public int getSize() {
            return size;
        }
        public void copyDecoration(Platform from) {
            if (from == null) return;
            setSize(from.getSize());
            int x, y;
            for (x=0; x < getDecorationWidth(); ++x) {
                for (y=0; y < 3; ++y) {
                    decorationArray[x][y] = from.getDecoration(x, y);
                }
            }
        }
    }

    public void setChunkSize(int width, int height) {
        chunkWidth = width;
        chunkHeight = height;
    }

    public void setWorldSize(int width, int height) {
        worldWidth = width;
        worldHeight = height;
    }

    public void setAreaCoords(int width, int height, int z) {
        coordX = width;
        coordY = height;
        coordZ = z;
    }

    public void fillFloor(int type) {
        for (int y=0; y<getHeight(); ++y){
            for (int x=0; x<getWidth(); ++x){
                content0[x][y] = type;
                if (values[x][y][0] == -1) values[x][y][0] = 1;
            }
        }
    }

    public Image getPlatformDecorationImage(Platform platform, int x, int y) {
        if (platform == null) return null;
        if (platformPaletteImageArray == null) return null;
        byte id = platform.getDecoration(x, y);
        if (id < 0 || id >= platformPaletteImageArray.length) return null;
        return platformPaletteImageArray[id].image;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public void  setObjectsChooserPanel(ObjectsChooserPanel panel) {
        objectsChooserPanel = panel;
    }
    public void  setAttributesChooserPanel(ObjectsChooserPanel panel) {
        attributesChooserPanel = panel;
    }

    public ObjectsChooserPanel getObjectsChooserPanel() {
        return objectsChooserPanel;
    }

    public boolean isSaved() {
        return !changed;
    }

    public GameLevel(Component component) {
        parent = component;
        roomsCoords = new ArrayList<ArrayList<Integer>>();
        roomsNames = new ArrayList<String>();
        roomsAmbientNames = new ArrayList<String>();
        roomsWalls = new ArrayList<ArrayList<ArrayList<Boolean>>>();
        setSize(width, height);
    }

    public void setSize(int w, int h) {
        width = Math.max(1, w);
        height = Math.max(1, h);
        content0  = new int[getWidth()][getHeight()];
        content1  = new int[getWidth()][getHeight()];
        content2  = new int[getWidth()][getHeight()];
        values = new int[getWidth()][getHeight()][6];
        //content3  = new int[getWidth()][getHeight()];
        int x, y;
        for (y=0; y<getHeight(); ++y){
            for (x=0; x<getWidth(); ++x){
                content0[x][y] = -1;
                content1[x][y] = -1;
                content2[x][y] = -1;
                //content3[x][y] = -1;
                for (int i=0; i<6; ++i){
                    if (i == 0) values[x][y][i] = -1;
                    else values[x][y][i] = 0;
                }
            }
        }
        changed = false;
        platforms.clear();
    }

    public void setValues(int tw, int th, int pw, int ph, boolean pm) {
        boolean ok = true;
        if (tileWidth != tw || tileHeight != th) {
            ok = false;
        }
        tileWidth = Math.max(1, tw);
        tileHeight = Math.max(1, th);
        platformMode = pm;
        if (!ok) {
            content0  = new int[getWidth()][getHeight()];
            content1  = new int[getWidth()][getHeight()];
            content2  = new int[getWidth()][getHeight()];
            values = new int[getWidth()][getHeight()][6];
            int x, y;
            for (y=0; y<getHeight(); ++y){
                for (x=0; x<getWidth(); ++x){
                    content0[x][y] = -1;
                    content1[x][y] = -1;
                    content2[x][y] = -1;
                    for (int i=0; i<6; ++i){
                        if (i == 0) values[x][y][i] = -1;
                        else values[x][y][i] = 0;
                    }
                }
            }
            changed = false;
        }
    }

    public void ClearPlatform() {
        platforms.clear();
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public int getCell(int x, int y, int layer) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return -1;
        if (layer == 0) {
            return content0[x][y];
        } else if (layer == 1) {
            return content1[x][y];
        } else if (layer == 2) {
            return content2[x][y];
        } else {
            return values[x][y][0];
        }
    }

    public void setCell(int x, int y, int layer, int item) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return;

        if (layer == 0) {
            content0[x][y] = item;
            if (content1[x][y] == -1) {
                if (item == -1) {
                    values[x][y][0] = -1;
                }
                else {
                    values[x][y][0] = 1;
                }
            }
        } else if (layer == 1) {
            content1[x][y] = item;
            if (item == -1) {
                if (content0[x][y] == -1) {
                    values[x][y][0] = -1;
                }
                else {
                    values[x][y][0] = 1;
                }
            }
            else {
                values[x][y][0] = 2;
            }
        } else if (layer == 2) {
            content2[x][y] = item;
        } else {
            values[x][y][0] = item;
            if (item == -1) {
                values[x][y][1] = 0;
                values[x][y][2] = 0;
                values[x][y][3] = 0;
                values[x][y][4] = 0;
                values[x][y][5] = 0;
            } else {
                values[x][y][1] = curAngle;
                values[x][y][2] = curXOffset;
                values[x][y][3] = curYOffset;
                values[x][y][4] = curObjectWidth;
                values[x][y][5] = curObjectHeight;
                if (item == 1) {
                } else if (item == 2) {
                }
            }
        }
        changed = true;
        if (currentRoomId != -1) {
            if (values[x][y][0] != -1) {
                if (values[x][y][0] == 1) {
                    roomsWalls.get(currentRoomId).get(y).set(x, false);
                } else if (values[x][y][0] == 2) {
                    roomsWalls.get(currentRoomId).get(y).set(x, true);
                }
            } else {
                roomsWalls.get(currentRoomId).get(y).set(x, null);
            }
        }
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    public void add(String fileName) {

        roomName = roomsNames.get(currentRoomId);
        roomAmbientName = roomsAmbientNames.get(currentRoomId);
        coordX = roomsCoords.get(currentRoomId).get(0);
        coordY = roomsCoords.get(currentRoomId).get(1);
        coordZ = roomsCoords.get(currentRoomId).get(2);
        try {
            RandomAccessFile fos = new RandomAccessFile(fileName, "rw");
            //fos.skipBytes((int)fos.length()-1);
            String nameTemp;
            long read = 1;
            int namesCount = fos.read();
            //getObjectsChooserPanel().newNames.clear();
            int nameSize;
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff = new byte[nameSize];
                fos.read(buff);
                read += nameSize + 1;
                //getObjectsChooserPanel().newNames.add(new String(buff));
            }
            //getObjectsChooserPanel().updateTiles();
            byte[] buff = new byte[(int)(fos.length() - read)];
            fos.read(buff);
            fos.seek(0);
            fos.write(getObjectsChooserPanel().names.size());
            for (int i =0; i < getObjectsChooserPanel().names.size(); ++i) {
                fos.write(getObjectsChooserPanel().names.get(i).length());
                fos.write(getObjectsChooserPanel().names.get(i).getBytes(), 0, getObjectsChooserPanel().names.get(i).length());
            }
            fos.write(buff);
            fos.seek(0);
            namesCount = fos.read();
            getObjectsChooserPanel().newNames.clear();
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff2 = new byte[nameSize];
                fos.read(buff2);
                read += nameSize + 1;
                getObjectsChooserPanel().newNames.add(new String(buff2));
            }
            getObjectsChooserPanel().updateTiles();
            int size = fos.read();
            fos.skipBytes(size + 7);
            int x=999, y=999, z=999, w=999, h=999;
            boolean found = false;
            read += size + 8;
            do {
                if (read >= fos.length()) break;
                w = fos.read();
                h = fos.read();
                x = fos.read();
                y = fos.read();
                z = fos.read();
                int roomNameSize = fos.read();
                byte[] buff2 = new byte[roomNameSize];
                fos.read(buff2);
                //String roomName = new String(buff2);
                int roomAmbientNameSize = fos.read();
                byte[] buff3 = new byte[roomAmbientNameSize];
                fos.read(buff3);
                //String roomAmbientName = new String(buff3);
                if (x != coordX || y != coordY || z != coordZ) {
                    read += 5 + roomNameSize + 1 + roomAmbientNameSize + 1;
                    fos.skipBytes(w*h*9);
                    read += w*h*9;
                } else {
                    width = w;
                    height = h;
                    found = true;
                    break;
                }
            } while (read < fos.length());

            if (!found) {
                fos.write(width);
                fos.write(height);
                fos.write(coordX);
                fos.write(coordY);
                fos.write(coordZ);
                fos.write(roomName.length());
                fos.write(roomName.getBytes(), 0, roomName.getBytes().length);
                fos.write(roomAmbientName.length());
                fos.write(roomAmbientName.getBytes(), 0, roomAmbientName.getBytes().length);
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content0[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content1[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content2[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(values[x][y][0]);
                    fos.write(values[x][y][1]);
                    fos.write(values[x][y][2]);
                    fos.write(values[x][y][3]);
                    fos.write(values[x][y][4]);
                    fos.write(values[x][y][5]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shift(String fileName) {

        try {
            RandomAccessFile fos = new RandomAccessFile(fileName, "rw");
            //fos.skipBytes((int)fos.length()-1);
            String nameTemp;
            int namesCount = fos.read();
            getObjectsChooserPanel().newNames.clear();
            int nameSize;
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff = new byte[nameSize];
                fos.read(buff);
                getObjectsChooserPanel().newNames.add(new String(buff));
            }
            getObjectsChooserPanel().updateTiles();
            int size = fos.read();
            fos.skipBytes(size + 7);
            int x=999, y=999, z=999, w=999, h=999;
            boolean found = false;
            long read = size + 8;
            do {
                if (w == -1) break;
                w = fos.read();
                h = fos.read();
                x = fos.read();
                y = fos.read();
                z = fos.read();
                width = w;
                height = h;
                int c;
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        if (c < 250 && ((curAngle == 1 && c >= objectsChooserPanel.imagesCount) || (curAngle == 2 && c >= objectsChooserPanel.imagesCount + objectsChooserPanel.tilesetsCount) || curAngle == 0)) {
                            fos.write(c+curType);
                        } else {
                            fos.write(c);
                        }
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer() - 1);
                        if (c < 250 && ((curAngle == 1 && c >= objectsChooserPanel.imagesCount) || (curAngle == 2 && c >= objectsChooserPanel.imagesCount + objectsChooserPanel.tilesetsCount) || curAngle == 0)) {
                            fos.write(c+curType);
                        } else {
                            fos.write(c);
                        }
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer() - 1);
                        if (c < 250 && ((curAngle == 1 && c >= objectsChooserPanel.imagesCount) || (curAngle == 2 && c >= objectsChooserPanel.imagesCount + objectsChooserPanel.tilesetsCount) || curAngle == 0)) {
                            fos.write(c+curType);
                        } else {
                            fos.write(c);
                        }
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                    }
                }
            } while (read < fos.length());

            //fos.write(254);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(String fileName) {

        try {
            RandomAccessFile fos = new RandomAccessFile(fileName, "rw");
            //fos.skipBytes((int)fos.length()-1);
            String nameTemp;
            int namesCount = fos.read();
            getObjectsChooserPanel().newNames.clear();
            int nameSize;
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff = new byte[nameSize];
                fos.read(buff);
                getObjectsChooserPanel().newNames.add(new String(buff));
            }
            getObjectsChooserPanel().updateTiles();
            int size = fos.read();
            fos.skipBytes(size + 7);
            int x=999, y=999, z=999, w=999, h=999;
            boolean found = false;
            long read = size + 8;
            do {
                if (read >= fos.length()) break;
                w = fos.read();
                h = fos.read();
                x = fos.read();
                y = fos.read();
                z = fos.read();
                if (x != coordX || y != coordY || z != coordZ) {
                    read += 5;
                    fos.skipBytes(w*h*9);
                    read += w*h*9;
                } else {
                    width = w;
                    height = h;
                    found = true;
                    break;
                }
            } while (read < fos.length());

            if (found) {
                fos.seek(fos.getFilePointer()-5);
                fos.write(w);
                fos.write(h);
                fos.write(-10);
                fos.write(-10);
                fos.write(-10);
            }

            //fos.write(254);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFileUsingIO(File sourceFile, File destinationFile) throws IOException {
        InputStream inputStreamData = null;
        OutputStream outputStreamData = null;

        try {
            inputStreamData = new BufferedInputStream(new FileInputStream(sourceFile));
            outputStreamData = new BufferedOutputStream(new FileOutputStream(destinationFile));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStreamData.read(buffer)) > 0) {
                outputStreamData.write(buffer, 0, length);
            }

        } finally {
            inputStreamData.close();
            outputStreamData.close();
        }
    }

    public void save(String fileName) {
        //File f2 = new File(fileName.replace("world", "_world"));
        File file = new File(fileName);
        if (!file.exists() || currentRoomId == -1) {
            return;
        }
        File yourFile = new File(fileName.replace("world1", "wworld1"));
        try {
            yourFile.createNewFile(); // if file already exists will do nothing
            FileOutputStream oFile = new FileOutputStream(yourFile, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f1 = new File(fileName);
        try {
            copyFileUsingIO(f1, yourFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        ZipEntry ze = null;
        try {
            fos = new FileOutputStream(fileName);
            //zos = new ZipOutputStream(fos);
            //ze = new ZipEntry("Content.tlv");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        //if (ze == null) return;
        /*try {
            zos.putNextEntry(ze);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        int prevRoomId = currentRoomId;
        String str = "Tank Game v1.4\n";
        try {
            fos.write(getObjectsChooserPanel().names.size());
            for (int i =0; i < getObjectsChooserPanel().names.size(); ++i) {
                fos.write(getObjectsChooserPanel().names.get(i).length());
                fos.write(getObjectsChooserPanel().names.get(i).getBytes(), 0, getObjectsChooserPanel().names.get(i).length());
            }
            //zos.write(str.getBytes(), 0, str.getBytes().length);
            fos.write(name.length());
            fos.write(name.getBytes(), 0, name.getBytes().length);
            int pm;
            if (!platformMode) pm = 0;
            else pm = 1;
            fos.write(pm);
            fos.write(worldWidth);
            fos.write(worldHeight);
            fos.write(tileWidth);
            fos.write(tileHeight);
            fos.write(chunkWidth);
            fos.write(chunkHeight);
            for (int i = -1; i < roomsCoords.size(); ++i) {
                if (i == currentRoomId) continue;
                if (i != -1) {
                    roomName = roomsNames.get(i);
                    roomAmbientName = roomsAmbientNames.get(i);
                    coordX = roomsCoords.get(i).get(0);
                    coordY = roomsCoords.get(i).get(1);
                    coordZ = roomsCoords.get(i).get(2);
                    load(fileName.replace("world1", "wworld1"), false, false);
                    this.fileName.replace("wworld1", "world1");
                } else {
                    roomName = roomsNames.get(currentRoomId);
                    roomAmbientName = roomsAmbientNames.get(currentRoomId);
                }
                fos.write(width);
                fos.write(height);
                fos.write(coordX);
                fos.write(coordY);
                fos.write(coordZ);
                fos.write(roomName.length());
                fos.write(roomName.getBytes(), 0, roomName.getBytes().length);
                fos.write(roomAmbientName.length());
                fos.write(roomAmbientName.getBytes(), 0, roomAmbientName.getBytes().length);
                int x, y;
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        fos.write(content0[x][y]);
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        fos.write(content1[x][y]);
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        fos.write(content2[x][y]);
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        fos.write(values[x][y][0]);
                        fos.write(values[x][y][1]);
                        fos.write(values[x][y][2]);
                        fos.write(values[x][y][3]);
                        fos.write(values[x][y][4]);
                        fos.write(values[x][y][5]);
                    }
                }
            }
            currentRoomId = prevRoomId;
            roomName = roomsNames.get(currentRoomId);
            roomAmbientName = roomsAmbientNames.get(currentRoomId);
            coordX = roomsCoords.get(currentRoomId).get(0);
            coordY = roomsCoords.get(currentRoomId).get(1);
            coordZ = roomsCoords.get(currentRoomId).get(2);
            //load(, false, false);
            ((EditorLevelPanel)parent).reloadLevel(fileName);
            //boolean b = yourFile.delete();
            yourFile.deleteOnExit();
            /*((EditorLevelPanel)parent).invalidateAll();
            ((EditorLevelPanel)parent).repaint();
            (LevelEditorFrame)((EditorLevelPanel)parent).parentFrame)).updateFieldsAfterLoad();*/
            //fos.write(254);
            //zos.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        //saveImage(zos, fileName, true, backgroundImage, backgroundPath, 0, backgroundBufferAndName);
        //backgroundPath = fileName;
        //ImageForPalette ifp;
        //ObjectsChooserPanel.ImageBufferAndName ibn;
        try {
            //zos.close();
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void saveImage(ZipOutputStream zos, String fileName, boolean isBackground, Image image, String imagePath, int id,
            ObjectsChooserPanel.ImageBufferAndName imageBufferAndName) {
        if ((imagePath == null || image == null) && imageBufferAndName == null) return;
        if (imageBufferAndName != null)
            ObjectsChooserPanel.writeImageBufferToZipOutputStream(zos, imageBufferAndName, id);
        else if(getExtension(imagePath).compareTo(".tlz") == 0) {
            if (isBackground) {
                ObjectsChooserPanel.writeImageFromZipToZipOutputStream(zos,
                        imagePath, "background", true);
            } else {
                ObjectsChooserPanel.writeImageFromZipToZipOutputStream(zos,
                        imagePath, "platform_decoration/image"+String.format("%03d", id), true);
            }
        } else {
            String ext = getExtension(imagePath);
            int buffSize = 1024;
            byte[] buff = new byte[buffSize];
            File imageFile = new File(imagePath);
            FileInputStream imageFIS = null;
            if (imageFile.exists()) {
                try {
                    imageFIS = new FileInputStream(imageFile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
                    imageFIS = null;
                }
            }
            if (imageFIS != null) {
                ZipEntry ze;
                if (isBackground)
                    ze = new ZipEntry("background"+File.separator+"image"+ext);
                else {
                    ze = new ZipEntry("platform_decoration/image"+String.format("%03d"+ext, id));
                }
                int buffRead;
                try {
                    zos.putNextEntry(ze);
                    while ((buffRead = imageFIS.read(buff)) > 0) {
                        zos.write(buff, 0, buffRead);
                    }
                    zos.closeEntry();
                } catch (IOException ex) {
                    Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static String getExtension(String filePath) {
        String ext;
        int dot = filePath.lastIndexOf(".");
        if (dot >= 0) {
            ext = filePath.substring(dot, filePath.length());
        } else {
            ext = "";
        }
        return ext;
    }

    public String fileName = "";

    public void load(String fileName, boolean fullLoad, boolean updateTiles) {
        this.fileName = fileName;
       /* if (changed) {
            int ret = JOptionPane.showConfirmDialog(parent, "Эта, как его, всё потрёте, дядя. Трём?", "Эта, предупреждениё", JOptionPane.YES_NO_OPTION );
            if (ret == JOptionPane.NO_OPTION) return;
        }*/
        /*if (!fileName.toLowerCase().endsWith(".tlv")) {
            fileName += ".tlv";
        }*/
        File file = new File(fileName);
        if (!file.exists()) {
            JOptionPane.showConfirmDialog(parent, "Эта, нету такова файла!", "Э...", JOptionPane.OK_CANCEL_OPTION );
            return;
        }
        if (!file.canRead()) {
            JOptionPane.showConfirmDialog(parent, "Нельзя читать энтот файл, дядя. Понял?", "Фига", JOptionPane.OK_CANCEL_OPTION );
            return;
        }
        loadTLW(file, fullLoad, updateTiles);
    }


    private boolean loadTLW(File file, boolean fullLoad, boolean updateTiles) {
        FileInputStream fInput = null;
        try {
            fInput = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fInput == null) {
            return true;
        }
        try {
            if (fullLoad) {
                if (fullLoadFromInputStream(fInput, file.length())) {
                    fInput.close();
                    return true;
                }
            } else {
                if (loadFromInputStream(fInput, file.length(), updateTiles)) {
                    fInput.close();
                    return true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        return false;
    }

    private boolean fullLoadFromInputStream(InputStream fInput, long length) throws IOException {
        tempPlatformDecorationPaletteSize = -1;
        platforms.clear();

        roomsNames = new ArrayList<String>();
        roomsAmbientNames = new ArrayList<String>();
        roomsCoords = new ArrayList<ArrayList<Integer>>();
        roomsWalls = new ArrayList<ArrayList<ArrayList<Boolean>>>();

        String nameTemp;
        int namesCount = fInput.read();
        getObjectsChooserPanel().newNames.clear();
        int nameSize;
        for (int i =0; i < namesCount; ++i) {
            nameSize = fInput.read();
            byte[] buff = new byte[nameSize];
            fInput.read(buff);
            getObjectsChooserPanel().newNames.add(new String(buff));
        }
        getObjectsChooserPanel().updateTiles();
        int size;
        size = fInput.read();
        byte[] buff = new byte[size];
        fInput.read(buff);
        name = new String(buff);
        int tmp = fInput.read();
        if (tmp == 0) platformMode = false;
        else platformMode = true;
        worldWidth = fInput.read();
        worldHeight = fInput.read();
        tileWidth = fInput.read();
        tileHeight = fInput.read();
        chunkWidth = fInput.read();
        chunkHeight = fInput.read();
        int x=999, y=999, z=999, w=999, h=999;
        boolean found = false;
        long read = 6+name.length();
        do {
            if (read >= length) break;
            w = fInput.read();
            h = fInput.read();
            x = fInput.read();
            y = fInput.read();
            z = fInput.read();
            //read += 5;
            int roomNameSize = fInput.read();
            if (roomNameSize < 0) break;
            byte[] buff2 = new byte[roomNameSize];
            fInput.read(buff2);
            String roomName = new String(buff2);
            int roomAmbientNameSize = fInput.read();
            byte[] buff3 = new byte[roomAmbientNameSize];
            fInput.read(buff3);
            String roomAmbientName = new String(buff3);
            read += 5 + roomNameSize + 1 + roomAmbientNameSize + 1;
            boolean bad = false;
            for (int i = 0; i < roomsCoords.size(); ++i) {
                if (roomsCoords.get(i).get(0) == x && roomsCoords.get(i).get(1) == y && roomsCoords.get(i).get(2) == z) {
                    bad = true;
                    break;
                }
            }
            if (bad || x > 200 || y > 200 || z > 200) {
                fInput.skip(w*h*9);
                read+=w*h*9;
            } else {
                roomsCoords.add(new ArrayList<Integer>());
                roomsNames.add(roomName);
                roomsAmbientNames.add(roomAmbientName);
                roomsCoords.get(roomsCoords.size()-1).add(x);
                roomsCoords.get(roomsCoords.size()-1).add(y);
                roomsCoords.get(roomsCoords.size()-1).add(z);
                roomsWalls.add(new ArrayList<ArrayList<Boolean>>());
                /*for (y = 0; y < h; ++y) {
                    roomsWalls.get(roomsWalls.size()-1).add(new ArrayList<Boolean>());
                    for (x = 0; x < w; ++x) {
                        int sym = fInput.read();
                        read++;
                        if (sym == 255) {
                            roomsWalls.get(roomsWalls.size()-1).get(roomsWalls.get(roomsWalls.size()-1).size()-1).add(null);
                        } else {
                            roomsWalls.get(roomsWalls.size()-1).get(roomsWalls.get(roomsWalls.size()-1).size()-1).add(false);
                        }
                    }
                }
                for (y = 0; y < h; ++y) {
                    for (x = 0; x < w; ++x) {
                        int sym = fInput.read();
                        read++;
                        if (sym != 255) {
                            roomsWalls.get(roomsWalls.size()-1).get(y).set(x, true);
                        }
                    }
                }*/
                fInput.skip(w*h*3);
                read+=w*h*3;
                for (y = 0; y < h; ++y) {
                    roomsWalls.get(roomsWalls.size()-1).add(new ArrayList<Boolean>());
                    for (x = 0; x < w; ++x) {
                        int sym = fInput.read();
                        read++;
                        if (sym == 2) {
                            roomsWalls.get(roomsWalls.size()-1).get(roomsWalls.get(roomsWalls.size()-1).size()-1).add(true);
                        }else if (sym == 255) {
                            roomsWalls.get(roomsWalls.size()-1).get(roomsWalls.get(roomsWalls.size()-1).size()-1).add(null);
                        } else {
                            roomsWalls.get(roomsWalls.size()-1).get(roomsWalls.get(roomsWalls.size()-1).size()-1).add(false);
                        }
                        fInput.skip(5);
                        read+=5;
                    }
                }
            }
            //fInput.skip(w*h*5);
            //read+=w*h*5;
            if (roomsWalls.get(roomsWalls.size()-1).size() == 0) {
                roomsWalls.remove(roomsWalls.size()-1);
                roomsCoords.remove(roomsCoords.size()-1);
                break;
            }
        } while (read < length);

        return true;
    }

    private boolean loadFromInputStream(InputStream fInput, long length, boolean updateTiles) throws IOException {
        tempPlatformDecorationPaletteSize = -1;
        platforms.clear();

        String nameTemp;
        int namesCount = fInput.read();
        if (updateTiles) getObjectsChooserPanel().newNames.clear();
        int nameSize;
        for (int i =0; i < namesCount; ++i) {
            nameSize = fInput.read();
            byte[] buff = new byte[nameSize];
            fInput.read(buff);
            if (updateTiles) getObjectsChooserPanel().newNames.add(new String(buff));
        }
        if (updateTiles) getObjectsChooserPanel().updateTiles();
        int size;
        size = fInput.read();
        byte[] buff = new byte[size];
        fInput.read(buff);
        name = new String(buff);
        int tmp = fInput.read();
        if (tmp == 0) platformMode = false;
        else platformMode = true;
        worldWidth = fInput.read();
        worldHeight = fInput.read();
        tileWidth = fInput.read();
        tileHeight = fInput.read();
        chunkWidth = fInput.read();
        chunkHeight = fInput.read();
        int x=999, y=999, z=999, w=999, h=999;
        boolean found = false;
        long read = 6+name.length();
        do {
            if (read >= length) break;
            w = fInput.read();
            h = fInput.read();
            x = fInput.read();
            y = fInput.read();
            z = fInput.read();
            int roomNameSize = fInput.read();
            if (roomNameSize < 0) break;
            byte[] buff2 = new byte[roomNameSize];
            fInput.read(buff2);
            //String roomName = new String(buff2);
            int roomAmbientNameSize = fInput.read();
            byte[] buff3 = new byte[roomAmbientNameSize];
            fInput.read(buff3);
            //String roomAmbientName = new String(buff3);
            read += 5 + roomNameSize + 1 + roomAmbientNameSize + 1;
            if (x != coordX || y != coordY || z != coordZ) {
                fInput.skip(w*h*9);
                read += w*h*9;
            } else {
                found = true;
                break;
            }
        } while (read < length);

        if (found) {
            if (w == 999) {
                width = fInput.read();
            } else {
                width = w;
            }
            if (h == 999) {
                height = fInput.read();
            } else {
                height = h;
            }
            if (x == 999) {
                coordX = fInput.read();
            } else {
                coordX = x;
            }
            if (y == 999) {
                coordY = fInput.read();
            } else {
                coordY = y;
            }
            if (z == 999) {
                coordZ = fInput.read();
            } else {
                coordZ = z;
            }

            /*int endChecker = 0;
            while ((coordX != nextLevelX || coordY != nextLevelY)) {
                fInput.skip(width*height*4);
                endChecker = fInput.read();
                if (endChecker == 254) {
                    return false;
                } else {
                    coordX = endChecker;
                }
                //coordX = fInput.read();
                coordY = fInput.read();
            }*/
            content0 = new int[width][height];
            content1 = new int[width][height];
            content2 = new int[width][height];
            values = new int[width][height][6];
            //setValues(tileWidth, tileHeight, 0, 0, false);
            changed = false;
            int sym;
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) content0[x][y] = -1;
                    else content0[x][y] = sym;
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) content1[x][y] = -1;
                    else content1[x][y] = sym;
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) content2[x][y] = -1;
                    else content2[x][y] = sym;
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) values[x][y][0] = -1;
                    else values[x][y][0] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][1] = 0;
                    else values[x][y][1] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][2] = 0;
                    else values[x][y][2] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][3] = 0;
                    else values[x][y][3] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][4] = 0;
                    else values[x][y][4] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][5] = 0;
                    else values[x][y][5] = sym;
                }
            }
            for (int zzz = 0; zzz < roomsCoords.size(); ++zzz) {
                ArrayList<Integer> coord = roomsCoords.get(zzz);
                if (coord.get(0) ==coordX && coord.get(1) == coordY && coord.get(2) == coordZ) {
                    currentRoomId = zzz;
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public void setBackground(String name) {
        backgroundPath = name;
        backgroundImage = ObjectsChooserPanel.getImageAtPath((JPanel)parent, name);
    }

    /*public String getBackground() {
        return backgroundPath;
    }*/

    public void addPlatform(Platform platform) {
        changed = true;
        platforms.add(platform);
    }

    public int getPlatformCount() {
        return platforms.size();
    }

    public Iterator<Platform> getPlatformIterator() {
        return platforms.iterator();
    }



    public void addImageForPlatformPalette(Image newImage, String imagePath) {
        ImageForPalette image = new ImageForPalette();
        image.image = newImage;
        image.path = imagePath;
        platformPaletteImages.add(image);
        platformPaletteImageArray = platformPaletteImages.toArray(new ImageForPalette[0]);
    }

    public ImageForPalette getPlatformPaletteImage(int id) {
        if (id < 0 || id >= platformPaletteImageArray.length) return null;
        return platformPaletteImageArray[id];
    }

    public int getPlatformPaletteSize() {
        if (platformPaletteImageArray == null) return 0;
        return platformPaletteImageArray.length;
    }

    public void deleteImageFromPlatformPalette(int id){
        if (id == -1 || platformPaletteImages == null) return;
        platformPaletteImages.clear();
        int i;
        for (i = 0; i < platformPaletteImageArray.length; ++i){
            if (i != id)
                platformPaletteImages.add(platformPaletteImageArray[i]);
        }
        platformPaletteImageArray = platformPaletteImages.toArray(new ImageForPalette[0]);
        Iterator<Platform> platformIterator = platforms.iterator();
        Platform platform;
        int x, y, imgColumnsCount;
        byte item;
        while (platformIterator.hasNext()) {
            platform = platformIterator.next();
            imgColumnsCount = platform.getDecorationWidth();
            for (x = 0; x < imgColumnsCount; ++x) {
                for (y = 0; y < 3; ++y) {
                    item = platform.getDecoration(x, y);
                    if (item == id) {
                        platform.setDecor(x, y, (byte)-1);
                    } else if (item > id) {
                        platform.setDecor(x, y, (byte)(item-1));
                    }
                }
            }
        }
        if (parent instanceof EditorLevelPanel) {
            ((EditorLevelPanel)parent).repaint();
        }
    }
}
