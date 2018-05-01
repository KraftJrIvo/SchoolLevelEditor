/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author IVO
 */


public class ObjectsChooserPanel extends JPanel implements MouseListener, MouseMotionListener {
    private int imgColumnsCount = 0;
    private int imgLinesCount = 0;
    private String[] imageNames = new String[10];
    //private Image[] images = new Image[10];
    ArrayList<Image> images, tilesets, animations;
    ArrayList<String> names;
    ArrayList<String> newNames;
    ArrayList<Integer> namesLengths;
    ArrayList<Rect> imagesRect;
    ArrayList<Integer> tileTypes;
    ArrayList<Integer> tileIndices;
    ArrayList<Integer> tileTileIndices;
    ArrayList<Integer> tilesetIndices;
    ArrayList<Point> tilesetTileSizes;
    //private Rect[] imagesRect = new Rect[10];
    private int selectedImage = 2;
    private Grid grid = new Grid(false);
    private ChooserListener listener = null;
    FileHandler fileHandler;
    String currentDir = "default";
    boolean loaded = false;
    int imagesCount = 0, tilesetsCount = 0, animationsCount = 0;
    public EditorLevelPanel lp;

    public interface ChooserListener {
        void onChoose(int i);
    }

    public void setOnChooser(ChooserListener listener) {
        this.listener = listener;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        grid.setSize(imgColumnsCount, imgLinesCount, getWidth(), getHeight());
        imagesRect.clear();
        calculateImagesRect();
    }



    public void mouseClicked(MouseEvent e) {
    }

    public int getSelectedObject() {
        return selectedImage;
    }

    public Image getImage(int index) {
        if (tileTypes.get(index) < 0) return null;
        if (tileTypes.get(index) == 2) return animations.get(tileIndices.get(index));
        if (tileTypes.get(index) == 1) return getRealTileSet(tileIndices.get(index));
        return images.get(tileIndices.get(index));
    }

    public Point getTileCoords(int index) {
        int tileId = 0;
        int iii = 0;
        while (iii < index) {
            if (tileTypes.get(iii) == 1) {
                tileId++;
            }
            iii++;
        }
        int tilesetWidth = tilesetTileSizes.get(tilesetIndices.indexOf(tileIndices.get(index))).x;
        int iy = tileTileIndices.get(tileId) / tilesetWidth;
        int ix = tileTileIndices.get(tileId) - iy * tilesetWidth;
        return new Point(ix, iy);
    }

    public Image getCurrentImage() {
        return getImage(selectedImage);
    }

    private void mouseEvent(MouseEvent e) {
        if (imagesRect == null) return;
        int oldSelectedImage = selectedImage;
        selectedImage = -1;
        for (int i = 0; i<imagesRect.size(); ++i) {
            if (e.getX() >= imagesRect.get(i).left && e.getX() < imagesRect.get(i).right
                    && e.getY() >= imagesRect.get(i).top && e.getY() < imagesRect.get(i).bottom) {
                selectedImage = i;
                if (listener != null) listener.onChoose(selectedImage);
                if (oldSelectedImage != selectedImage) repaint();
                return;
            }
        }
        if (oldSelectedImage != selectedImage) {
            repaint();
        }
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

    public ObjectsChooserPanel() {

    }

    public void collapseTilesets() {
        int numberAdded = 0;
        for (int i = 0; i < names.size(); ++i) {
            if (names.get(i) == null) {
                int tileId = 0;
                int iii = 0;
                while (iii < i) {
                    if (tileTypes.get(iii) == 1) {
                        tileId++;
                    }
                    iii++;
                }
                names.remove(i);
                namesLengths.remove(i);
                tilesets.remove(tileId);
                tileIndices.remove(i);
                tileTypes.remove(i);
                i--;
                //if (i == names.size()-1) break;
            }
        }
        numberAdded = 0;
    }
    public void expandTilesets() {
        int numberAdded = 0;
        ArrayList<Image> preSets = (ArrayList<Image>) tilesets.clone();
        for (int i = 0; i < tilesetIndices.size(); ++i) {
            int count = tilesetTileSizes.get(i).x * tilesetTileSizes.get(i).y - 1;
            for (int j = 0; j < count; ++j) {
                int imCount = 0;
                int iii = 0;
                while (iii < i){
                    if (tileTypes.get(iii) != 1) {
                        imCount++;
                    }
                    iii++;
                }
                preSets.add(tilesetIndices.get(i)+1+numberAdded, null);
                names.add(tilesetIndices.get(i)+imCount+1+numberAdded, null);
                namesLengths.add(tilesetIndices.get(i)+imCount+1+numberAdded,0);
                tileIndices.add(tilesetIndices.get(i)+imCount+1+numberAdded,tilesetIndices.get(i));
                tileTypes.add(tilesetIndices.get(i)+imCount+1+numberAdded,1);
                numberAdded++;
            }
        }
        tilesets = preSets;
    }

    public void init(String str, EditorLevelPanel lp) {
        imgColumnsCount = 24;
        imgLinesCount = 20;
        images = new ArrayList<Image>();
        tilesets = new ArrayList<Image>();
        animations = new ArrayList<Image>();
        imagesRect = new ArrayList<Rect>();
        names = new ArrayList<String>();
        namesLengths = new ArrayList<Integer>();
        newNames = new ArrayList<String>();
        tileTypes = new ArrayList<Integer>();
        tileIndices = new ArrayList<Integer>();
        tileTileIndices = new ArrayList<Integer>();
        tilesetIndices = new ArrayList<Integer>();
        tilesetTileSizes = new ArrayList<Point>();
        this.lp = lp;
        //reload(str);
        //chooser.setCurrentDirectory();
        ImageObserver io = new ImageObserver() {
            @Override
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return false;
            }
        };
        File dir = new File(str);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                Image image = getImage(this, child.getPath());
                if (image != null) {
                    tileIndices.add(images.size());
                    images.add(image);
                    names.add(child.getName().substring(0, child.getName().length() - 4));
                    namesLengths.add(child.getName().length());
                    tileTypes.add(0);
                }
            }
            dir = new File(str+"\\tiles");
            directoryListing = dir.listFiles();
            int tilesetsCount =  0;
            for (File child : directoryListing) {
                Image image = getImage(this, child.getPath());
                if (image != null) {
                    if (!child.getName().contains("tileset")) {
                        tileIndices.add(tilesetsCount);
                        tilesets.add(image);
                        names.add("tiles\\" + child.getName().substring(0, child.getName().length() - 4));
                        namesLengths.add(child.getName().length());
                        tileTypes.add(1);
                        tileTileIndices.add(0);
                    } else {
                        tilesetIndices.add(tilesetsCount);
                        Pattern p = Pattern.compile("(\\d+)x(\\d+)",
                                Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(child.getName());
                        int hhh = image.getHeight(io)/lp.gameLevel.tileHeight;
                        int www = image.getWidth(io)/lp.gameLevel.tileWidth;
                        if (m.find()) {
                            www = Integer.parseInt(m.group(1));
                            hhh = Integer.parseInt(m.group(2));
                            tilesetTileSizes.add(new Point(www, hhh));
                        } else {
                            www = 3;
                            hhh = 4;
                            tilesetTileSizes.add(new Point(3, 4));
                        }
                        int ind = tilesets.size();
                        for (int i = 0; i < hhh; ++i) {
                            for (int j = 0; j < www; ++j) {
                                tileIndices.add(tilesetsCount);
                                if (i == 0 && j == 0) {
                                    tilesets.add(image);
                                    names.add("tiles\\" + child.getName().substring(0, child.getName().length() - 4));
                                    namesLengths.add(child.getName().length());
                                }
                                else {
                                    tilesets.add(null);
                                    names.add(null);
                                    namesLengths.add(0);
                                }
                                tileTypes.add(1);
                                tileTileIndices.add(i * www + j);
                            }
                        }
                    }
                    tilesetsCount++;
                }
            }
            dir = new File(str+"\\anim");
            directoryListing = dir.listFiles();
            //if (directoryListing != null) {
                for (File child : directoryListing) {
                    Image image = getImage(this, child.getPath());
                    if (image != null) {
                        tileIndices.add(animations.size());
                        animations.add(image);
                        names.add("anim\\" + child.getName().substring(0, child.getName().length() - 4));
                        namesLengths.add(child.getName().length());
                        tileTypes.add(2);
                    }
                }
            //}
        }
        imagesCount = images.size();
        tilesetsCount = tilesets.size();
        animationsCount = animations.size();
        imgLinesCount = tileIndices.size()/imgColumnsCount + 2;
        calculateImagesRect();
        grid.setSize(imgColumnsCount, imgLinesCount, getWidth(), getHeight());
        addMouseListener((MouseListener)this);
        addMouseMotionListener((MouseMotionListener)this);
        loaded = true;
    }

    public void updateTiles() {
        collapseTilesets();
        ArrayList<String> preNames = new ArrayList<String>(names);
        ArrayList<Integer> preTileIndices = new ArrayList<Integer>(tileIndices);
        ArrayList<Integer> preTileTypes = new ArrayList<Integer>(tileTypes);
        ArrayList<String> unusedNames = new ArrayList<String>();
        ArrayList<Integer> unusedTypes = new ArrayList<Integer>();
        ArrayList<Integer> unusedIndices = new ArrayList<Integer>();
        for (int i = 0; i < names.size(); ++i) {
            if (!newNames.contains(names.get(i))) {
                unusedNames.add(names.get(i));
                unusedTypes.add(tileTypes.get(i));
                unusedIndices.add(tileIndices.get(i));
            }
        }
        for (int i = 0; i < newNames.size(); ++i) {
            int id = names.indexOf(newNames.get(i));
            if (id != -1) {
                preTileTypes.set(i, tileTypes.get(id));
                preTileIndices.set(i, tileIndices.get(id));
                preNames.set(i, newNames.get(i));
            } else {
                preTileTypes.add(i, -1);
                preTileIndices.add(i, -1);
                preNames.add(i, "");
            }
        }
        for (int i = 0; i < unusedNames.size(); ++i) {
            preTileTypes.set(newNames.size() + i, unusedTypes.get(i));
            preTileIndices.set(newNames.size() + i, unusedIndices.get(i));
            preNames.set(newNames.size() + i, unusedNames.get(i));
        }
        tileIndices = preTileIndices;
        tileTypes = preTileTypes;
        names = preNames;
        expandTilesets();
    }

    private Rect getImageRect(int index) {
       Rect rect = new Rect();
       if (index < 0 || index >= imagesRect.size()) return rect;
       return imagesRect.get(index);
    }

    private void calculateImagesRect() {
        if (!loaded) return;
       if (imagesRect==null || grid.isNull()) return;
       int line, column, imageID;
       for (line = 0; line < imgLinesCount; ++line) {
           for (column = 0; column < imgColumnsCount; ++column) {
                imageID = line * imgColumnsCount + column;
                //if (imageID >= imagesRect.size()) return;
                imagesRect.add(new Rect());
                imagesRect.get(imageID).left = grid.getLineX(column);
                imagesRect.get(imageID).top = grid.getLineY(line);
                imagesRect.get(imageID).right = grid.getLineX(column + 1);
                imagesRect.get(imageID).bottom = grid.getLineY(line+1);
           }
       }
    }

    public Image getRealTileSet(int ii) {
        int tilesCount = 0;
        for (int i = 0; i < tilesets.size(); ++i) {
            if (tilesets.get(i) != null) {
                if (tilesCount == ii) return tilesets.get(i);
                tilesCount++;
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics g) {
       if (imagesRect == null || imagesRect.size()==0) return;
       if (grid.isNull()) return;
       g.setColor(Color.white);
       g.fillRect(0, 0, getWidth(), getHeight());
        //g.setColor(Color.GRAY);
        for (int i = 0; i < tileTypes.size(); ++i) {
            if (i == selectedImage) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.GRAY);
            }
            g.fillRect(imagesRect.get(i).left, imagesRect.get(i).top, imagesRect.get(i).width(), imagesRect.get(i).height());
            if (tileTypes.get(i) == 0) {
                g.drawImage(images.get(tileIndices.get(i)), imagesRect.get(i).left, imagesRect.get(i).top, imagesRect.get(i).width(), imagesRect.get(i).height(), this);
            } else if (tileTypes.get(i) == 1) {
                int width;
                int height;
                if (!tilesetIndices.contains(tileIndices.get(i))) {
                    Image tileset = getRealTileSet(tileIndices.get(i));
                    width = tileset.getWidth(this)/3;
                    height = tileset.getHeight(this)/4;
                    g.drawImage(tileset, imagesRect.get(i).left, imagesRect.get(i).top, imagesRect.get(i).right, imagesRect.get(i).bottom, 0, 0, width, height, this);
                } else {
                    /*int startIndex = -1;
                    int startFirst = -1;
                    for (int ii =0; ii < tileIndices.size(); ++ii) {
                        if (startFirst != -1 && tileIndices.get(ii).equals(tileIndices.get(i))) {
                            if (ii == startFirst+1)startIndex = startFirst;
                            else startIndex = ii;
                            break;
                        } else if (startFirst == -1 && tileIndices.get(ii).equals(tileIndices.get(i))) {
                            startFirst = ii;
                        }
                    }*/
                    int startIndex = -1;
                    for (int ii =0; ii < tileIndices.size(); ++ii) {
                        if (tileIndices.get(ii).equals(tileIndices.get(i)) && tileTypes.get(ii) == 1) {
                            startIndex = ii;
                            break;
                        }
                    }
                    //if (startIndex == -1) startIndex = startFirst;
                    int tilesetWidth = (getRealTileSet(tileIndices.get(i)).getWidth(this)/lp.gameLevel.tileWidth);
                    int iy = (i-startIndex) / tilesetWidth;
                    int ix = (i-startIndex) - iy * tilesetWidth;
                    Image img = getRealTileSet(tileIndices.get(i));
                    int w = img.getWidth(this)/tilesetTileSizes.get(tilesetIndices.indexOf(tileIndices.get(i))).x;
                    int h = img.getHeight(this)/tilesetTileSizes.get(tilesetIndices.indexOf(tileIndices.get(i))).y;
                    g.drawImage(img, imagesRect.get(i).left, imagesRect.get(i).top, imagesRect.get(i).right, imagesRect.get(i).bottom,
                            ix*w, h*iy, w*ix + w, h*iy + h, this);
                }
            } else if (tileTypes.get(i) == 2) {
                int width = animations.get(tileIndices.get(i)).getHeight(this);
                int height = width;
                //boolean left =
                g.drawImage(animations.get(tileIndices.get(i)), imagesRect.get(i).left, imagesRect.get(i).top, imagesRect.get(i).right, imagesRect.get(i).bottom, 0, 0, width, height, this);
            }
            if (i == selectedImage)
                g.setColor(Color.red);
            else
                g.setColor(Color.black);
            g.drawRect(imagesRect.get(i).left, imagesRect.get(i).top, imagesRect.get(i).width(), imagesRect.get(i).height());
        }
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 100);
    }

    public static Image getImage(Component component, String name) {
        if (!name.contains(".png"))return null;
        Image img = null;
        /*URLClassLoader urlLoader =
            (URLClassLoader)component.getClass().getClassLoader();
        URL fileLoc = urlLoader.findResource(name);*/
        img = component.getToolkit().createImage(name);
        //img = component.getToolkit().

        MediaTracker tracker = new MediaTracker(component);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
            if (tracker.isErrorAny()) {
                System.out.println("Error loading image " + name);
            }
        } catch (Exception ex) { ex.toString(); }
        return img;
    }

    public static Image getImageAtPath(JPanel panel, String path) {
        Image img = null;
        URLClassLoader urlLoader =
            (URLClassLoader)panel.getClass().getClassLoader();
        //URL fileLoc = urlLoader.
        URL fileLoc;
        try {
            File file = new File(path);
            fileLoc = file.toURI().toURL();
            img = panel.getToolkit().createImage(fileLoc);

            MediaTracker tracker = new MediaTracker(panel);
            tracker.addImage(img, 0);
            try {
                tracker.waitForID(0);
                if (tracker.isErrorAny()) {
                    System.out.println("Error loading image " + path);
                }
            } catch (Exception ex) { ex.toString(); }
            return img;
        } catch (MalformedURLException ex) {
            Logger.getLogger(ObjectsChooserPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Image getImageFromZipFile(ZipFile zip, String entryName, boolean startWith) {
        InputStream fis = null;
        ZipEntry ze = null;
        try {
            Enumeration<? extends ZipEntry> enm = zip.entries();
            do {
                ze = enm.nextElement();
            } while (((startWith) ? !ze.getName().startsWith(entryName)
                    : ze.getName().compareTo(entryName) != 0) && enm.hasMoreElements());
            if (!ze.getName().startsWith(entryName)) return null;
            fis = zip.getInputStream(ze);
            BufferedImage bImage = ImageIO.read(fis);
            fis.close();
            return bImage;
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Image getImageFromZip(String zipPath, String entryName, boolean startWith) {
        File file = new File(zipPath);
        if (!file.exists()) return null;
        ZipFile zip;
        try {
            zip = new ZipFile(file);
            return getImageFromZipFile(zip, entryName, startWith);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void writeImageFromZipToZipOutputStream(ZipOutputStream zos, String zipPath,
            String entryName, boolean startWith) {
        File file = new File(zipPath);
        if (!file.exists()) return;
        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        InputStream fis = null;
        ZipEntry ze = null;
        try {
            Enumeration<? extends ZipEntry> enm = zip.entries();
            do {
                ze = enm.nextElement();
            } while (((startWith) ? !ze.getName().startsWith(entryName)
                    : ze.getName().compareTo(entryName) != 0) && enm.hasMoreElements());
            entryName = ze.getName();
            fis = zip.getInputStream(ze);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fis == null) {
            try {
                zip.close();
            } catch (IOException ex) {
                Logger.getLogger(ObjectsChooserPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        ze = new ZipEntry(entryName);
        int bufferSize = 1024;
        byte[] buff = new byte[bufferSize];
        int buffRead;
        try {
            zos.putNextEntry(ze);
            while ((buffRead = fis.read(buff)) > 0) {
                zos.write(buff, 0, buffRead);
            }
            zos.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fis.close();
            zip.close();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static class ImageBufferAndName {
        byte[] buffer = null;
        String name;
    }

    public static ImageBufferAndName getImageFileFromZip(String zipPath, String entryName, boolean startWith) {
        ImageBufferAndName image = new ImageBufferAndName();
        File file = new File(zipPath);
        if (!file.exists()) return image;
        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return image;
        }
        InputStream fis = null;
        ZipEntry ze = null;
        try {
            Enumeration<? extends ZipEntry> enm = zip.entries();
            do {
                ze = enm.nextElement();
            } while (((startWith) ? !ze.getName().startsWith(entryName)
                    : ze.getName().compareTo(entryName) != 0) && enm.hasMoreElements());
            fis = zip.getInputStream(ze);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fis == null) {
            try {
                zip.close();
            } catch (IOException ex) {
                Logger.getLogger(ObjectsChooserPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return image;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buff = new byte[bufferSize];
        int buffRead;
        try {
            while ((buffRead = fis.read(buff)) > 0) {
                baos.write(buff, 0, buffRead);
            }
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        image.buffer = baos.toByteArray();
        image.name = ze.getName();
        try {
            fis.close();
            zip.close();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    public static void writeImageBufferToZipOutputStream(ZipOutputStream zos, ImageBufferAndName image, int id) {
        ZipEntry ze = null;
        if (image.name.startsWith("platform_decoration/image")) {
            String ext = GameLevel.getExtension(image.name);
            image.name = "platform_decoration/image" + String.format("%03d", id) + ext;
        }
        ze = new ZipEntry(image.name);
        try {
            zos.putNextEntry(ze);
            zos.write(image.buffer, 0, image.buffer.length);
            zos.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
