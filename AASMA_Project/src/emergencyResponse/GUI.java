package emergencyResponse;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class GUI extends JFrame {

    private static final long serialVersionUID = 1L;

    static JTextField speed;
    static JPanel worldPanel;
    static JButton run, reset, step;
    private int x, y;

    public class Cell extends JPanel {

        private static final long serialVersionUID = 1L;

        public List<ObjectType> objects = new ArrayList<ObjectType>();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for(ObjectType objectType : objects) {
                if(objectType instanceof CommunicationEmergencyCenter) {
                    try {
                        BufferedImage image = null;
                        if(((CommunicationEmergencyCenter) objectType).id == 1) {
                            image = ImageIO.read(new File("images/center1.png"));
                        }
                        else if(((CommunicationEmergencyCenter) objectType).id == 2) {
                            image = ImageIO.read(new File("images/center2.png"));
                        }
                        else if(((CommunicationEmergencyCenter) objectType).id == 3) {
                            image = ImageIO.read(new File("images/center3.png"));
                        }
                        else if(((CommunicationEmergencyCenter) objectType).id == 4) {
                            image = ImageIO.read(new File("images/center4.png"));
                        }
                        g.drawImage(image,3, 0,40,35, null);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (objectType instanceof  Hospital) {
                    try {

                        BufferedImage image = null;
                        if(((Hospital) objectType).getCenterId() == 1) {
                            if(((Hospital) objectType).getCurrCapacity() == ((Hospital) objectType).getMaxCapacity()) {
                                image = ImageIO.read(new File("images/hospital_center1_Full.png"));
                            }
                            else {
                                image = ImageIO.read(new File("images/hospital_center1.png"));
                            }
                        }

                        else if(((Hospital) objectType).getCenterId() == 2) {
                            if(((Hospital) objectType).getCurrCapacity() == ((Hospital) objectType).getMaxCapacity()) {
                                image = ImageIO.read(new File("images/hospital_center2_Full.png"));
                            }
                            else {
                                image = ImageIO.read(new File("images/hospital_center2.png"));
                            }
                        }

                        else if(((Hospital) objectType).getCenterId() == 3) {
                            if(((Hospital) objectType).getCurrCapacity() == ((Hospital) objectType).getMaxCapacity()) {
                                image = ImageIO.read(new File("images/hospital_center3_Full.png"));
                            }
                            else {
                                image = ImageIO.read(new File("images/hospital_center3.png"));
                            }
                        }

                        else if(((Hospital) objectType).getCenterId() == 4) {
                            if(((Hospital) objectType).getCurrCapacity() == ((Hospital) objectType).getMaxCapacity()) {
                                image = ImageIO.read(new File("images/hospital_center4_Full.png"));
                            }
                            else {
                                image = ImageIO.read(new File("images/hospital_center4.png"));
                            }
                        }

                        g.drawImage(image, 7, 0,33,27, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else if(objectType instanceof Emergency) {
                    try {
                        BufferedImage image = null;
                        if(((Emergency) objectType).getSeverity() == 1) {
                            image = ImageIO.read(new File("images/emergency1.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 2) {
                            image = ImageIO.read(new File("images/emergency2.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 3) {
                            image = ImageIO.read(new File("images/emergency3.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 4) {
                            image = ImageIO.read(new File("images/emergency4.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 5) {
                            image = ImageIO.read(new File("images/emergency5.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 6) {
                            image = ImageIO.read(new File("images/emergency6.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 7) {
                            image = ImageIO.read(new File("images/emergency7.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 8) {
                            image = ImageIO.read(new File("images/emergency8.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 9) {
                            image = ImageIO.read(new File("images/emergency9.png"));
                        }
                        else if(((Emergency) objectType).getSeverity() == 10) {
                            image = ImageIO.read(new File("images/emergency10.png"));
                        }
                        g.drawImage(image, 7, 0,35,27, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else if(objectType instanceof Ambulance) {
                    g.setColor(((Ambulance) objectType).getColor());
                    if(((Ambulance)objectType).getNumPatients() == 0) {
                        switch (((Ambulance) objectType).getDirection()) {
                            case 0:
                                g.fillPolygon(new int[]{10, 25, 40}, new int[]{30, 0, 30}, 3);
                                break;
                            case 90:
                                g.fillPolygon(new int[]{10, 40, 10}, new int[]{0, 15, 30}, 3);
                                break;
                            case 180:
                                g.fillPolygon(new int[]{10, 40, 25}, new int[]{0, 0, 30}, 3);
                                break;
                            default:
                                g.fillPolygon(new int[]{10, 40, 40}, new int[]{15, 0, 30}, 3);
                        }
                    }
                    else {
                        try {
                            switch (((Ambulance) objectType).getDirection()) {
                                case 0:
                                    g.fillPolygon(new int[]{10, 25, 40}, new int[]{30, 0, 30}, 3);
                                    break;
                                case 90:
                                    g.fillPolygon(new int[]{10, 40, 10}, new int[]{0, 15, 30}, 3);
                                    break;
                                case 180:
                                    g.fillPolygon(new int[]{10, 40, 25}, new int[]{0, 0, 30}, 3);
                                    break;
                                default:
                                    g.fillPolygon(new int[]{10, 40, 40}, new int[]{15, 0, 30}, 3);
                            }
                            BufferedImage image2 = ImageIO.read(new File("images/blackCircle.png"));
                            g.drawImage(image2, 20, 10,10,10, null);
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public GUI() {
        setTitle("EmergencyResponse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setSize(1000, 650); //Window
        add(createButtonPanel());

        World.initialize();
        World.setGUI(this);

        worldPanel = new JPanel();
        worldPanel.setSize(new Dimension(945,540)); // world
        worldPanel.setLocation(new Point(20,60));

        x = World.x;
        y = World.y;
        worldPanel.setLayout(new GridLayout(x,y));
        for(int i=0; i < x; i++)
            for(int j=0; j < y; j++)
                worldPanel.add(new Cell());

        displayWorld();
        World.displayObjects();
        update();
        add(worldPanel);
    }

    public void displayWorld() {
        for(int i=0; i < x; i++){
            for(int j=0; j < y; j++){
                int row=y-j-1, col=i;
                Tile Tile = World.getTile(new Point(i,j));
                JPanel p = ((JPanel)worldPanel.getComponent(row*x+col));
                p.setBackground(Tile.color);
                p.setBorder(BorderFactory.createLineBorder(Color.white));
            }
        }
    }

    public void removeObject(ObjectType object) {
        int row=y-object.getPoint().y-1, col=object.getPoint().x;
        Cell p = (Cell)worldPanel.getComponent(row*x+col);
        p.setBorder(BorderFactory.createLineBorder(Color.white));
        p.objects.remove(object);
    }

    public void displayObject(ObjectType object) {
        int row=y-object.getPoint().y-1, col=object.getPoint().x;
        Cell p = (Cell)worldPanel.getComponent(row*x+col);
        p.setBorder(BorderFactory.createLineBorder(Color.white));
        p.objects.add(object);
    }

    public void update() {
        worldPanel.invalidate();
    }

    private Component createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(600,50));
        panel.setLocation(new Point(200 ,0));

        step = new JButton("Step");
        panel.add(step);
        step.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if(run.getText().equals("Run")) World.step();
                else World.stop();
            }
        });
        reset = new JButton("Reset");
        panel.add(reset);
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                World.reset();
            }
        });
        run = new JButton("Run");
        panel.add(run);
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if(run.getText().equals("Run")){
                    int time = -1;
                    try {
                        time = Integer.valueOf(speed.getText());
                    } catch(Exception e){
                        JTextPane output = new JTextPane();
                        output.setText("Please insert an integer value to set the time per step\nValue inserted = "+speed.getText());
                        JOptionPane.showMessageDialog(null, output, "Error", JOptionPane.PLAIN_MESSAGE);
                    }
                    if(time>0){
                        World.run(time);
                        run.setText("Stop");
                    }
                } else {
                    World.stop();
                    run.setText("Run");
                }
            }
        });
        speed = new JTextField(" time per step in [1,100] ");
        speed.setMargin(new Insets(5,5,5,5));
        panel.add(speed);

        return panel;
    }
}
