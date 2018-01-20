/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LevelEditorFrame.java
 *
 * Created on 01.12.2010, 0:00:11
 */

package tank;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author IVO
 */
public class LevelEditorFrame extends javax.swing.JFrame implements KeyListener {
    //ObjectsChooserPanel objectCC = new ObjectsChooserPanel("C:\\cool\\java\\School\\School\\android\\assets\\worlds\\all_platform");
    ObjectsChooserPanel objectCC;// = new ObjectsChooserPanel("C:\\cool\\java\\School\\School\\android\\assets\\worlds\\all");
    ObjectsChooserPanel attributesOCC;// = new ObjectsChooserPanel("C:\\cool\\java\\School\\School\\android\\assets\\worlds\\all");
    EditorLevelPanel editorLP = new EditorLevelPanel (this);

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        //System.out.println(e.getKeyCode());
        if (e.getKeyCode() == 109) {
            if (editorLP.currentZLayer < editorLP.gameLevel.worldHeight-1) {
                editorLP.currentZLayer++;
            }
            //editorLP.shiftTiles(1);

        } else if (e.getKeyCode() == 107) {
            if (editorLP.currentZLayer > 0) {
                editorLP.currentZLayer--;
            }
            //editorLP.shiftTiles(-1);
        } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            editorLP.removeRoom(editorLP.selectedId);
        }
        editorLP.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    static class FromStupidProtectionKeyListener implements KeyListener {
        private JTextField textField;
        private int maxNumber;

        public FromStupidProtectionKeyListener(JTextField textField, int maxNumber) {
            this.textField = textField;
            this.maxNumber = maxNumber;
        }

        public void keyTyped(KeyEvent evt) {
            if ((evt.getKeyChar() < '0' || evt.getKeyChar() > '9')
                    && evt.getKeyChar() != java.awt.event.KeyEvent.VK_BACK_SPACE) evt.consume();
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            String text = textField.getText();
            if (text.startsWith("0") && text.length() > 1)
                textField.setText(text.substring(1, text.length()));
            if(text.isEmpty()) {
                textField.setText("0");
            } else {
                int number = Integer.valueOf(textField.getText());
                if (number > maxNumber)
                    textField.setText(String.valueOf(maxNumber));
            }
        }

    }

    private String path = "";

    private void initAll() {
        editorLP = new EditorLevelPanel(this);
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("C:\\cool\\java\\School\\School\\android\\assets\\worlds"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select world resources directory!");
        chooser.showOpenDialog(this);
        chooser.addKeyListener(this);
        path = chooser.getSelectedFile().getAbsolutePath();
        objectCC = new ObjectsChooserPanel(path);

        initComponents();
        initPanels(path);

        setTahomaFont(getContentPane());

        Image img = ObjectsChooserPanel.getImage(this, "player_tank.png");
        setIconImage(img);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        /*setLocation((screenSize.width - getWidth())/2,
                (screenSize.height - getHeight())/2);*/
        setBounds(300,
//                300, 2150, 1000);
                0, 1000, 700);
        int a = (byte) + (char) - (int) + (long) - 1;
    }

    public LevelEditorFrame() {
        initAll();
    }

    public static void setTahomaFont(Container parent) {
        Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
        setFont(parent, tahoma);
    }

    public static void setFont(Container parent, Font font) {
        for (Component component : parent.getComponents()) {
            component.setFont(font);
            if (component instanceof Container)
                setFont((Container)component, font);
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

    private void initPanels(String path) {
        ObjectListScrollPane.setViewportView(objectCC);
        //ObjectListScrollPane2.setViewportView(attributesOCC);
        LevelPanel.add(editorLP, java.awt.BorderLayout.CENTER);
        WidthTextField.setText("" + editorLP.getLevelWidth());
        HeightTextField.setText(""+editorLP.getLevelHeight());
        TileWidthTextField.setText(""+editorLP.gameLevel.tileWidth);
        TileHeightTextField.setText(""+editorLP.gameLevel.tileHeight);
        PlatformModeCheckBox.setSelected(editorLP.gameLevel.platformMode);
        WorldWidthTextField.setText("" + editorLP.gameLevel.worldWidth);
        WorldHeightTextField.setText(""+editorLP.gameLevel.worldHeight);
        CoordXTextField.setText(""+editorLP.gameLevel.coordX);
        CoordYTextField.setText(""+editorLP.gameLevel.coordY);
        CoordZTextField.setText(""+editorLP.gameLevel.coordZ);
        setFocusable(true);
        this.addKeyListener(this);

        editorLP.setObjectsChooserPanel(objectCC);
        editorLP.setAttributesChooserPanel(attributesOCC);
        editorLP.addMouseWheelListener(editorLP);
        editorLP.setFocusable(true);
        editorLP.addKeyListener(this);

        File f = new File(path + "\\world1.tlw");
        if(f.exists()) {
            editorLP.gameLevel.load(path + "\\world1.tlw", true, true);
            editorLP.currentZLayer = editorLP.gameLevel.worldHeight/2;
            try {
                copyFileUsingIO(f, new File(path + "\\backup_world1.tlw"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                f.createNewFile();
                editorLP.gameLevel.fileName = path + "\\world1.tlw";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        WidthTextField.addKeyListener(new FromStupidProtectionKeyListener(WidthTextField, 300));
        HeightTextField.addKeyListener(new FromStupidProtectionKeyListener(HeightTextField, 300));

        //backgroundChangeButton.setIcon(getIcon("background_icon.png"));
        //movingPlatformCreateButton.setIcon(getIcon("plat_icon.png"));
        //changeBlockIconButton.setIcon(getIcon("change_icon.png"));

        pack();
    }

    public Icon getIcon(String name) {
        URLClassLoader urlLoader =
            (URLClassLoader)getClass().getClassLoader();
        URL fileLoc = urlLoader.findResource("src/images/tank/gui/" + name);
        return new ImageIcon(fileLoc);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        LoadButton = new javax.swing.JButton();
        RemoveButton = new javax.swing.JButton();
        ShiftButton = new javax.swing.JButton();
        TestButton = new javax.swing.JButton();
        MenuTabbedPane = new javax.swing.JTabbedPane();
        LevelPropertiesPanel = new javax.swing.JPanel();
        NameTextField = new javax.swing.JTextField();
        NameLabel = new javax.swing.JLabel();
        AreaWidthLabel = new javax.swing.JLabel();
        AreaHeightLabel = new javax.swing.JLabel();
        TileWidthLabel = new javax.swing.JLabel();
        TileHeightLabel = new javax.swing.JLabel();
        ChunkWidthLabel = new javax.swing.JLabel();
        ChunkHeightLabel = new javax.swing.JLabel();
        PlatformModeLabel = new javax.swing.JLabel();
        WorldWidthLabel = new javax.swing.JLabel();
        WorldHeightLabel = new javax.swing.JLabel();
        RoomNameLabel = new javax.swing.JLabel();
        RoomAmbientNameLabel = new javax.swing.JLabel();
        CurXLabel = new javax.swing.JLabel();
        CurYLabel = new javax.swing.JLabel();
        CurZLabel = new javax.swing.JLabel();
        OffsetXLabel = new javax.swing.JLabel();
        OffsetYLabel = new javax.swing.JLabel();
        ObjectWidthLabel = new javax.swing.JLabel();
        ObjectHeightLabel = new javax.swing.JLabel();
        AngleLabel = new javax.swing.JLabel();
        TypeLabel = new javax.swing.JLabel();
        OffsetXTextField = new javax.swing.JTextField();
        OffsetYTextField = new javax.swing.JTextField();
        ObjectWidthTextField = new javax.swing.JTextField();
        ObjectHeightTextField = new javax.swing.JTextField();
        AngleTextField = new javax.swing.JTextField();
        TypeTextField = new javax.swing.JTextField();
        WidthTextField = new javax.swing.JTextField();
        HeightTextField = new javax.swing.JTextField();
        NewDirTextField = new javax.swing.JTextField();
        WorldWidthTextField = new javax.swing.JTextField();
        WorldHeightTextField = new javax.swing.JTextField();
        ChunkWidthTextField = new javax.swing.JTextField();
        ChunkHeightTextField = new javax.swing.JTextField();
        CoordXTextField = new javax.swing.JTextField();
        CoordYTextField = new javax.swing.JTextField();
        CoordZTextField = new javax.swing.JTextField();
        CoordX2TextField = new javax.swing.JTextField();
        CoordY2TextField = new javax.swing.JTextField();
        RoomNameTextField = new javax.swing.JTextField();
        RoomAmbientNameTextField = new javax.swing.JTextField();
        TileWidthTextField = new javax.swing.JTextField();
        PlayerWidthTextField = new javax.swing.JTextField();
        TileHeightTextField = new javax.swing.JTextField();
        PlayerHeightTextField = new javax.swing.JTextField();
        ApplyLevelPropertiesButton = new javax.swing.JButton();
        Apply2Button = new javax.swing.JButton();
        Apply3Button = new javax.swing.JButton();
        SaveButton = new javax.swing.JButton();
        ObjectsPanel = new javax.swing.JPanel();
        AttributesPanel = new javax.swing.JPanel();
        ObjectListScrollPane = new javax.swing.JScrollPane();
        ObjectListScrollPane2 = new javax.swing.JScrollPane();
        ToolsPanel = new javax.swing.JPanel();
        //backgroundChangeButton = new javax.swing.JButton();
        //movingPlatformCreateButton = new javax.swing.JButton();
        //changeBlockIconButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        gridVisibleCheckBox = new javax.swing.JCheckBox();
        PlatformModeCheckBox = new javax.swing.JCheckBox();
        backgroundVisibleCheckBox = new javax.swing.JCheckBox();
        LevelPanel = new javax.swing.JPanel();
        Layer0RadioButton = new javax.swing.JRadioButton();
        Layer1RadioButton = new javax.swing.JRadioButton();
        Layer2RadioButton = new javax.swing.JRadioButton();
        Layer3RadioButton = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tiled Area Editor v.1.0");

        jToolBar1.setRollover(true);
        jToolBar1.setPreferredSize(new java.awt.Dimension(100, 20));

        /*SaveButton.setFocusable(false);
        SaveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        SaveButton.setLabel("Save");
        SaveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        SaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(SaveButton);*/

        LoadButton.setText("Load");
        LoadButton.setFocusable(false);
        LoadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        LoadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        LoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(LoadButton);

        /*TestButton.setText("Add");
        TestButton.setFocusable(false);
        TestButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        TestButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(TestButton);

        RemoveButton.setText("Remove");
        RemoveButton.setFocusable(false);
        RemoveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        RemoveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        RemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(RemoveButton);

        ShiftButton.setText("Shift");
        ShiftButton.setFocusable(false);
        ShiftButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ShiftButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ShiftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShiftButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(ShiftButton);*/

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        NameTextField.setText("no name");

        NameLabel.setText("Name:");
        AreaWidthLabel.setText("Area Width:");
        AreaHeightLabel.setText("Area Height:");
        TileWidthLabel.setText("Tile Width:");
        TileHeightLabel.setText("Tile Height:");
        ChunkWidthLabel.setText("Chunk Width:");
        ChunkHeightLabel.setText("Chunk Height:");
        PlatformModeLabel.setText("Platform Mode:");
        WorldWidthLabel.setText("World Width:");
        WorldHeightLabel.setText("World Height:");
        CurXLabel.setText("Current Area X:");
        CurYLabel.setText("Current Area Y:");
        CurZLabel.setText("Current Area Z:");
        OffsetXLabel.setText("X Offset:");
        OffsetYLabel.setText("Y Offset:");
        ObjectWidthLabel.setText("Object Width:");
        ObjectHeightLabel.setText("Object Height:");
        AngleLabel.setText("Orientation:");
        TypeLabel.setText("Type:");
        RoomNameLabel.setText("Room name:");
        RoomAmbientNameLabel.setText("Room ambient:");

        Layer1RadioButton.setSelected(true);

        ApplyLevelPropertiesButton.setText("Apply");
        ApplyLevelPropertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ApplyLevelPropertiesButtonActionPerformed(evt, false);
            }
        });
        SaveButton.setText("Save");
        SaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ApplyLevelPropertiesButtonActionPerformed(evt, true);
            }
        });

        Apply3Button.setText("Change");
        Apply3Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Apply3ButtonActionPerformed(evt);
            }
        });
        Apply2Button.setText("Next One");
        Apply2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Apply2ButtonActionPerformed(evt);
            }
        });
        Layer0RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Layer0RadioButtonActionPerformed(evt);
            }
        });Layer1RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Layer1RadioButtonActionPerformed(evt);
            }
        });Layer2RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Layer2RadioButtonActionPerformed(evt);
            }
        });
        Layer3RadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Layer3RadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LevelPropertiesPanelLayout = new javax.swing.GroupLayout(LevelPropertiesPanel);
        LevelPropertiesPanel.setLayout(LevelPropertiesPanelLayout);
        LevelPropertiesPanelLayout.setHorizontalGroup(
                LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(LevelPropertiesPanelLayout.createSequentialGroup()
                                .addContainerGap(22, Short.MAX_VALUE)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(NameLabel)
                                                .addComponent(AreaWidthLabel)
                                                .addComponent(AreaHeightLabel)
                                                .addComponent(TileWidthLabel)
                                                .addComponent(TileHeightLabel)
                                                .addComponent(ChunkWidthLabel)
                                                .addComponent(ChunkHeightLabel)
                                                .addComponent(PlatformModeLabel)
                                                .addComponent(WorldWidthLabel)
                                                .addComponent(WorldHeightLabel)
                                                .addComponent(CurXLabel)
                                                .addComponent(CurYLabel)
                                                .addComponent(CurZLabel)
                                                .addComponent(OffsetXLabel)
                                                .addComponent(OffsetYLabel)
                                                .addComponent(ObjectWidthLabel)
                                                .addComponent(ObjectHeightLabel)
                                                .addComponent(TypeLabel)
                                                .addComponent(AngleLabel)
                                                .addComponent(RoomNameLabel)
                                                .addComponent(RoomAmbientNameLabel)
                                )
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(HeightTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(PlatformModeCheckBox, javax.swing.GroupLayout.Alignment.TRAILING)
                                                //.addComponent(PlayerHeightTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                                //.addComponent(PlayerWidthTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(TileHeightTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(TileWidthTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(ChunkHeightTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(ChunkWidthTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(HeightTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(WidthTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(WorldHeightTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(WorldWidthTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(CoordXTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(CoordYTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(CoordZTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(ApplyLevelPropertiesButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(OffsetXTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(OffsetYTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(ObjectWidthTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(ObjectHeightTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(TypeTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(AngleTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(RoomNameTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(RoomAmbientNameTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(SaveButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                                //.addComponent(Apply2Button, javax.swing.GroupLayout.Alignment.TRAILING)
                                                //.addComponent(CoordY2TextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                                //.addComponent(CoordX2TextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                                //.addComponent(NewDirTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                                                //.addComponent(Apply3Button, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(NameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                                .addContainerGap())
                        .addGroup(LevelPropertiesPanelLayout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addComponent(ApplyLevelPropertiesButton)

                                .addContainerGap(52, Short.MAX_VALUE))
        );
        LevelPropertiesPanelLayout.setVerticalGroup(
                LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(LevelPropertiesPanelLayout.createSequentialGroup()
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(NameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(NameLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(WidthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(AreaWidthLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(HeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(AreaHeightLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(TileWidthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(TileWidthLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(TileHeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(TileHeightLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(ChunkWidthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ChunkWidthLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(ChunkHeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ChunkHeightLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(PlatformModeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(PlatformModeLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(WorldWidthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(WorldWidthLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(WorldHeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(WorldHeightLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(CoordXTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(CurXLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(CoordYTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(CurYLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(CoordZTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(CurZLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                    /*.addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CoordX2TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WidthLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CoordY2TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HeightLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Apply2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HeightLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(NewDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HeightLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Apply3Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HeightLabel))*/
                                .addComponent(ApplyLevelPropertiesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(OffsetXTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(OffsetXLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(OffsetYTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(OffsetYLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(ObjectWidthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ObjectWidthLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(ObjectHeightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ObjectHeightLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(TypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(TypeLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(AngleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(AngleLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(RoomNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(RoomNameLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LevelPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(RoomAmbientNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(RoomAmbientNameLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SaveButton)
                                .addContainerGap(151, Short.MAX_VALUE))
        );

        MenuTabbedPane.addTab("Level properties", LevelPropertiesPanel);

        javax.swing.GroupLayout ObjectsPanelLayout = new javax.swing.GroupLayout(ObjectsPanel);
        ObjectsPanel.setLayout(ObjectsPanelLayout);
        ObjectsPanelLayout.setHorizontalGroup(
                ObjectsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ObjectsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(Layer0RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Layer1RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Layer2RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Layer3RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addGroup(ObjectsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ObjectListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                                .addContainerGap())
        );
        ObjectsPanelLayout.setVerticalGroup(
                ObjectsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(Layer0RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Layer1RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Layer2RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Layer3RadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(ObjectsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGap(28, 28, 28)
                                .addComponent(ObjectListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout AttributesPanelLayout = new javax.swing.GroupLayout(AttributesPanel);
        AttributesPanel.setLayout(AttributesPanelLayout);
        AttributesPanelLayout.setHorizontalGroup(
                AttributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AttributesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addContainerGap())
                        .addGroup(AttributesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(ObjectListScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                                .addContainerGap())
        );
        AttributesPanelLayout.setVerticalGroup(
                AttributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AttributesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGap(28, 28, 28)
                                .addComponent(ObjectListScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                                .addContainerGap())
        );

        MenuTabbedPane.addTab("Objects", ObjectsPanel);

        //MenuTabbedPane.addTab("Attributes", AttributesPanel);
        getContentPane().add(MenuTabbedPane, java.awt.BorderLayout.LINE_START);

        LevelPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(LevelPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Layer0RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplyLevelPropertiesButtonActionPerformed
        Layer0RadioButton.setSelected(true);
        Layer1RadioButton.setSelected(false);
        Layer2RadioButton.setSelected(false);
        Layer3RadioButton.setSelected(false);
        editorLP.setCurrentLayer(0);
        editorLP.invalidateGameLevel();
        editorLP.repaint();
    }
    private void Layer1RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplyLevelPropertiesButtonActionPerformed
        Layer0RadioButton.setSelected(false);
        Layer1RadioButton.setSelected(true);
        Layer2RadioButton.setSelected(false);
        Layer3RadioButton.setSelected(false);
        editorLP.setCurrentLayer(1);
        editorLP.invalidateGameLevel();
        editorLP.repaint();
    }
    private void Layer2RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplyLevelPropertiesButtonActionPerformed
        Layer0RadioButton.setSelected(false);
        Layer1RadioButton.setSelected(false);
        Layer2RadioButton.setSelected(true);
        Layer3RadioButton.setSelected(false);
        editorLP.setCurrentLayer(2);
        editorLP.invalidateGameLevel();
        editorLP.repaint();
    }
    private void Layer3RadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplyLevelPropertiesButtonActionPerformed
        Layer0RadioButton.setSelected(false);
        Layer1RadioButton.setSelected(false);
        Layer2RadioButton.setSelected(false);
        Layer3RadioButton.setSelected(true);
        editorLP.setCurrentLayer(3);
        editorLP.invalidateGameLevel();
        editorLP.repaint();
    }

    private void Apply2ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            editorLP.gameLevel.nextLevelX = new Integer(CoordX2TextField.getText());
            editorLP.gameLevel.nextLevelY = new Integer(CoordY2TextField.getText());
        }catch (NumberFormatException ex) {
            editorLP.gameLevel.nextLevelX = 0;
            editorLP.gameLevel.nextLevelY = 0;
        }
    }

    private void Apply3ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //objectCC.reload(NewDirTextField.getText());
    }

    private void ApplyLevelPropertiesButtonActionPerformed(java.awt.event.ActionEvent evt, boolean save) {//GEN-FIRST:event_ApplyLevelPropertiesButtonActionPerformed
        int numberWidth, numberHeight, numberWidth2, numberHeight2, numberWidth3, numberHeight3, z;
        Integer i;
        try {
            i = new Integer(WidthTextField.getText());
            numberWidth = i.intValue();
        } catch(NumberFormatException ex) {
            numberWidth = 0;
        }
        try {
            i = new Integer(HeightTextField.getText());
            numberHeight = i.intValue();
        } catch(NumberFormatException ex) {
            numberHeight = 0;
        }
        editorLP.setLevelSize(numberWidth, numberHeight);
        try {
            i = new Integer(TileWidthTextField.getText());
            numberWidth = i.intValue();
        } catch(NumberFormatException ex) {
            numberWidth = 0;
        }
        try {
            i = new Integer(TileHeightTextField.getText());
            numberHeight = i.intValue();
        } catch(NumberFormatException ex) {
            numberHeight = 0;
        }
        try {
            i = new Integer(ChunkWidthTextField.getText());
            editorLP.gameLevel.chunkWidth = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.chunkWidth =  0;
        }
        try {
            i = new Integer(ChunkHeightTextField.getText());
            editorLP.gameLevel.chunkHeight = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.chunkHeight = 0;
        }
        try {
            i = new Integer(PlayerWidthTextField.getText());
            numberWidth2 = i.intValue();
        } catch(NumberFormatException ex) {
            numberWidth2 = 0;
        }
        try {
            i = new Integer(PlayerHeightTextField.getText());
            numberHeight2 = i.intValue();
        } catch(NumberFormatException ex) {
            numberHeight2 = 0;
        }
        try {
            i = new Integer(WorldWidthTextField.getText());
            numberWidth3 = i.intValue();
        } catch(NumberFormatException ex) {
            numberWidth3 = 0;
        }
        try {
            i = new Integer(WorldHeightTextField.getText());
            numberHeight3 = i.intValue();
        } catch(NumberFormatException ex) {
            numberHeight3 = 0;
        }
        editorLP.setWorldSize(numberWidth3, numberHeight3);
        try {
            i = new Integer(CoordXTextField.getText());
            numberWidth3 = i.intValue();
        } catch(NumberFormatException ex) {
            numberWidth3 = 0;
        }
        try {
            i = new Integer(CoordYTextField.getText());
            numberHeight3 = i.intValue();
        } catch(NumberFormatException ex) {
            numberHeight3 = 0;
        }
        try {
            i = new Integer(CoordZTextField.getText());
            z = i.intValue();
        } catch(NumberFormatException ex) {
            z = 0;
        }
        try {
            i = new Integer(OffsetXTextField.getText());
            editorLP.gameLevel.curXOffset = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.curXOffset = 0;
        }
        try {
            i = new Integer(OffsetYTextField.getText());
            editorLP.gameLevel.curYOffset = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.curYOffset = 0;
        }
        try {
            i = new Integer(ObjectWidthTextField.getText());
            editorLP.gameLevel.curObjectWidth = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.curObjectWidth = 0;
        }
        try {
            i = new Integer(ObjectHeightTextField.getText());
            editorLP.gameLevel.curObjectHeight = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.curObjectHeight = 0;
        }
        try {
            i = new Integer(TypeTextField.getText());
            editorLP.gameLevel.curType = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.curType = 0;
        }
        try {
            i = new Integer(AngleTextField.getText());
            editorLP.gameLevel.curAngle = i.intValue();
        } catch(NumberFormatException ex) {
            editorLP.gameLevel.curAngle = 0;
        }
        editorLP.gameLevel.setName(NameTextField.getText());
        editorLP.setAreaCoords(numberWidth3, numberHeight3, z);
        editorLP.setLevelValues(numberWidth, numberHeight, numberWidth2, numberHeight2, PlatformModeCheckBox.isSelected());
        WidthTextField.setText("" + editorLP.getLevelWidth());
        HeightTextField.setText("" + editorLP.getLevelHeight());
        TileWidthTextField.setText(""+editorLP.gameLevel.tileWidth);
        TileHeightTextField.setText(""+editorLP.gameLevel.tileHeight);
        ChunkWidthTextField.setText(""+editorLP.gameLevel.chunkWidth);
        ChunkHeightTextField.setText(""+editorLP.gameLevel.chunkHeight);
        PlatformModeCheckBox.setSelected(editorLP.gameLevel.platformMode);
        WorldWidthTextField.setText("" + editorLP.gameLevel.worldWidth);
        WorldHeightTextField.setText(""+editorLP.gameLevel.worldHeight);
        CoordXTextField.setText(""+editorLP.gameLevel.coordX);
        CoordYTextField.setText(""+editorLP.gameLevel.coordY);
        CoordZTextField.setText(""+editorLP.gameLevel.coordZ);
        for (int zzz = 0; zzz < editorLP.gameLevel.roomsCoords.size(); ++zzz) {
            ArrayList<Integer> coord = editorLP.gameLevel.roomsCoords.get(zzz);
            if (coord.get(0) == editorLP.gameLevel.coordX && coord.get(1) == editorLP.gameLevel.coordY && coord.get(2) == editorLP.gameLevel.coordZ) {
                editorLP.gameLevel.roomsNames.set(zzz, RoomNameTextField.getText());
                editorLP.gameLevel.roomsAmbientNames.set(zzz, RoomAmbientNameTextField.getText());
                editorLP.gameLevel.currentRoomId = zzz;
            }
        }
        editorLP.requestFocus();
        if (save) editorLP.gameLevel.save(path + "\\world1.tlw");
    }//GEN-LAST:event_ApplyLevelPropertiesButtonActionPerformed

    /*private void PlatformModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {

    }*/

    private void SaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        URLClassLoader urlLoader =
            (URLClassLoader)getClass().getClassLoader();
        int urlID = 0;
        for (int i=0; i<urlLoader.getURLs().length; ++i) {
            if (urlLoader.getURLs()[i].toString().contains("images\\")) {
                urlID = i;
                break;
            }
        }
        //File dir = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+currentDir);
        File file = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+objectCC.currentDir);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Tiled World", "tlw");
        //file.renameTo(new File(file.getName()+".tlw"));
        chooser.setCurrentDirectory(new File("C:\\cool\\java\\School\\School\\android\\assets\\worlds"));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            editorLP.saveLevel(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_SaveButtonActionPerformed

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        URLClassLoader urlLoader =
                (URLClassLoader)getClass().getClassLoader();
        int urlID = 0;
        for (int i=0; i<urlLoader.getURLs().length; ++i) {
            if (urlLoader.getURLs()[i].toString().contains("images\\")) {
                urlID = i;
                break;
            }
        }
        File file = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+objectCC.currentDir);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Tiled World", "tlw");
        chooser.setCurrentDirectory(new File("C:\\cool\\java\\School\\School\\android\\assets\\worlds"));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            editorLP.addLevel(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void RemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        URLClassLoader urlLoader =
                (URLClassLoader)getClass().getClassLoader();
        int urlID = 0;
        for (int i=0; i<urlLoader.getURLs().length; ++i) {
            if (urlLoader.getURLs()[i].toString().contains("images\\")) {
                urlID = i;
                break;
            }
        }
        File file = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+objectCC.currentDir);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Tiled World", "tlw");
        chooser.setCurrentDirectory(new File("C:\\cool\\java\\School\\School\\android\\assets\\worlds"));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            editorLP.removeLevel(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void ShiftButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        URLClassLoader urlLoader =
                (URLClassLoader)getClass().getClassLoader();
        int urlID = 0;
        for (int i=0; i<urlLoader.getURLs().length; ++i) {
            if (urlLoader.getURLs()[i].toString().contains("images\\")) {
                urlID = i;
                break;
            }
        }
        File file = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+objectCC.currentDir);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Tiled World", "tlw");
        chooser.setCurrentDirectory(new File("C:\\cool\\java\\School\\School\\android\\assets\\worlds"));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            editorLP.shiftLevel(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void updateFieldsAfterLoad() {
        NameTextField.setText(""+editorLP.gameLevel.getName());
        WidthTextField.setText("" + editorLP.getLevelWidth());
        HeightTextField.setText("" + editorLP.getLevelHeight());
        TileWidthTextField.setText(""+editorLP.gameLevel.tileWidth);
        TileHeightTextField.setText(""+editorLP.gameLevel.tileHeight);
        ChunkWidthTextField.setText(""+editorLP.gameLevel.chunkWidth);
        ChunkHeightTextField.setText(""+editorLP.gameLevel.chunkHeight);
        PlatformModeCheckBox.setSelected(editorLP.gameLevel.platformMode);
        WorldWidthTextField.setText("" + editorLP.gameLevel.worldWidth);
        WorldHeightTextField.setText(""+editorLP.gameLevel.worldHeight);
        CoordXTextField.setText(""+editorLP.gameLevel.coordX);
        CoordYTextField.setText(""+editorLP.gameLevel.coordY);
        CoordZTextField.setText(""+editorLP.gameLevel.coordZ);
        if (editorLP.gameLevel.currentRoomId != -1) {
            RoomNameTextField.setText(""+editorLP.gameLevel.roomsNames.get(editorLP.gameLevel.currentRoomId));
            RoomAmbientNameTextField.setText(""+editorLP.gameLevel.roomsAmbientNames.get(editorLP.gameLevel.currentRoomId));
        }
    }

    private void LoadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadButtonActionPerformed
        //LevelPanel.remove(editorLP);
        //initAll();
        LevelEditorFrame newFrame = new LevelEditorFrame();
        newFrame.setVisible(true);
        dispose();
        /*JFileChooser chooser = new JFileChooser();
        URLClassLoader urlLoader =
                (URLClassLoader)getClass().getClassLoader();
        int urlID = 0;
        for (int i=0; i<urlLoader.getURLs().length; ++i) {
            if (urlLoader.getURLs()[i].toString().contains("images\\")) {
                urlID = i;
                break;
            }
        }
        File file = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+objectCC.currentDir);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Tiled World", "tlw");
        chooser.setCurrentDirectory(new File("C:\\cool\\java\\School\\School\\android\\assets\\worlds"));
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            editorLP.loadLevel(chooser.getSelectedFile().getAbsolutePath());
        }*/
        //updateFieldsAfterLoad();
    }//GEN-LAST:event_LoadButtonActionPerformed

    public void updateFields() {
        TypeTextField.setText(""+editorLP.gameLevel.curType);
        OffsetXTextField.setText(""+editorLP.gameLevel.curXOffset);
        OffsetYTextField.setText(""+editorLP.gameLevel.curYOffset);
        ObjectWidthTextField.setText(""+editorLP.gameLevel.curObjectWidth);
        ObjectHeightTextField.setText(""+editorLP.gameLevel.curObjectHeight);
        AngleTextField.setText(""+editorLP.gameLevel.curAngle);
    }
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LevelEditorFrame().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ApplyLevelPropertiesButton;
    private javax.swing.JButton Apply2Button;
    private javax.swing.JTextField HeightTextField;
    private javax.swing.JTextField TileHeightTextField;
    private javax.swing.JTextField PlayerHeightTextField;
    private javax.swing.JPanel LevelPanel;
    private javax.swing.JPanel LevelPropertiesPanel;
    private javax.swing.JButton LoadButton;
    private javax.swing.JTabbedPane MenuTabbedPane;
    private javax.swing.JLabel NameLabel;
    private javax.swing.JLabel AreaWidthLabel;
    private javax.swing.JLabel AreaHeightLabel;
    private javax.swing.JLabel TileWidthLabel;
    private javax.swing.JLabel TileHeightLabel;
    private javax.swing.JLabel ChunkWidthLabel;
    private javax.swing.JLabel ChunkHeightLabel;
    private javax.swing.JLabel PlatformModeLabel;
    private javax.swing.JLabel WorldWidthLabel;
    private javax.swing.JLabel WorldHeightLabel;
    private javax.swing.JLabel CurXLabel;
    private javax.swing.JLabel CurYLabel;
    private javax.swing.JLabel CurZLabel;
    private javax.swing.JLabel OffsetXLabel;
    private javax.swing.JLabel OffsetYLabel;
    private javax.swing.JLabel ObjectWidthLabel;
    private javax.swing.JLabel ObjectHeightLabel;
    private javax.swing.JLabel AngleLabel;
    private javax.swing.JLabel TypeLabel;
    private javax.swing.JLabel RoomNameLabel;
    private javax.swing.JLabel RoomAmbientNameLabel;
    private javax.swing.JTextField NameTextField;
    private javax.swing.JScrollPane ObjectListScrollPane;
    private javax.swing.JScrollPane ObjectListScrollPane2;
    private javax.swing.JPanel ObjectsPanel;
    private javax.swing.JPanel AttributesPanel;
    private javax.swing.JButton TestButton;
    private javax.swing.JButton RemoveButton;
    private javax.swing.JButton ShiftButton;
    private javax.swing.JPanel ToolsPanel;
    private javax.swing.JTextField WidthTextField;
    private javax.swing.JTextField TileWidthTextField;
    private javax.swing.JTextField PlayerWidthTextField;
    private javax.swing.JTextField WorldWidthTextField;
    private javax.swing.JTextField WorldHeightTextField;
    private javax.swing.JTextField ChunkWidthTextField;
    private javax.swing.JTextField ChunkHeightTextField;
    private javax.swing.JTextField CoordXTextField;
    private javax.swing.JTextField CoordYTextField;
    private javax.swing.JTextField CoordZTextField;
    private javax.swing.JTextField CoordX2TextField;
    private javax.swing.JTextField CoordY2TextField;
    private javax.swing.JTextField TypeTextField;
    private javax.swing.JTextField AngleTextField;
    private javax.swing.JTextField OffsetXTextField;
    private javax.swing.JTextField OffsetYTextField;
    private javax.swing.JTextField ObjectWidthTextField;
    private javax.swing.JTextField ObjectHeightTextField;
    private javax.swing.JTextField RoomNameTextField;
    private javax.swing.JTextField RoomAmbientNameTextField;
    private javax.swing.JTextField NewDirTextField;
    private javax.swing.JButton Apply3Button;
    private javax.swing.JButton SaveButton;
    //private javax.swing.JButton backgroundChangeButton;
    private javax.swing.JCheckBox backgroundVisibleCheckBox;
    private javax.swing.JCheckBox PlatformModeCheckBox;
    //private javax.swing.JButton changeBlockIconButton;
    private javax.swing.JCheckBox gridVisibleCheckBox;
    private javax.swing.JButton jButton2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JRadioButton Layer0RadioButton;
    private javax.swing.JRadioButton Layer1RadioButton;
    private javax.swing.JRadioButton Layer2RadioButton;
    private javax.swing.JRadioButton Layer3RadioButton;
    //private javax.swing.JButton movingPlatformCreateButton;
    // End of variables declaration//GEN-END:variables

}
