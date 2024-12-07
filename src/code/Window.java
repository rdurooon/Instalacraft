package code;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Window extends JFrame{
    public Window(String language){
        String text[] = new String[]{"Map", "Install", "Files found automatically:", "Click 'Close' to continue", "Files found", "Close", "Starting the installation...", "Map and Resourcepack successfully installed!", "Error: File(s) not found", "Installing..."}; 

        if(language.equals("pt")){
            text[0] = "Mapa";
            text[1] = "Instalar";
            text[2] = "Arquivos encontrados automaticamente:";
            text[3] = "Clique 'Fechar' para continuar";
            text[4] = "Arquivos encontrados";
            text[5] = "Fechar";
            text[6] = "Iniciando a instalação...";
            text[7] = "Mapa e Resourcepack instalados com sucesso!";
            text[8] = "Erro: arquivo(s) não encontrado(s)";
            text[9] = "Instalando...";
        }

        setTitle("Instalacraft");
        setIconImage(new ImageIcon(getClass().getClassLoader().getResource("resources/instalacraft.png")).getImage());
        setSize(500,250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JPanel panelInputs = new JPanel();
        panelInputs.setLayout(new BoxLayout(panelInputs, BoxLayout.Y_AXIS));
        panelInputs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelMap = new JPanel(new BorderLayout());
        JLabel mapTxt = new JLabel(text[0] + ":");
        JTextField mapName = new JTextField(10);
        panelMap.add(mapTxt,BorderLayout.NORTH);
        panelMap.add(mapName,BorderLayout.CENTER);

        JPanel panelResource = new JPanel(new BorderLayout());
        JLabel resourceTxt = new JLabel("Resourcepack:");
        JTextField resourceName = new JTextField(10);
        panelResource.add(resourceTxt,BorderLayout.NORTH);
        panelResource.add(resourceName,BorderLayout.CENTER);

        JButton installBtn = new JButton(text[1]);
        installBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelInputs.add(panelMap);
        panelInputs.add(Box.createRigidArea(new Dimension(0,10)));
        panelInputs.add(panelResource);
        panelInputs.add(Box.createRigidArea(new Dimension(0,20)));
        panelInputs.add(installBtn);
        
        add(panelInputs,BorderLayout.CENTER);

        File jarDir = new File(System.getProperty("user.dir"));
        File autoMap = Paths.findMap(jarDir);
        File autoRp = Paths.findResource(jarDir);

        if(autoMap != null && autoRp != null){
            String message = text[2] + "\n";
            if(autoMap != null){
                message += "- " + text[0] + ": " + autoMap.getName() + "\n";
            }
            if(autoRp != null){
                message += "- Resourcepack: " + autoRp.getName() + "\n";
            }
            message += "\n" + text[3];

            int response = JOptionPane.showOptionDialog(this, message, text[4], JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{text[5]}, text[5]);
        
            mapName.setText(autoMap.getName());
            resourceName.setText(autoRp.getName());

            if(response == 0){
                dispose();
            }

        }

        installBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                JDialog popup = new JDialog(Window.this, text[9], false);
                JLabel installing = new JLabel(text[6], SwingConstants.CENTER);
                popup.add(installing);
                popup.setSize(300,100);
                popup.setLocationRelativeTo(Window.this);
                popup.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                popup.setVisible(true);

                new Thread(() -> {
                    try { 
                        String map = mapName.getText().trim();
                        String rp = resourceName.getText().trim();

                        String minePath = Paths.getMinecraftOrigin();
                        File fileMap = map.isEmpty() ? autoMap : Paths.searchDir(jarDir.getAbsolutePath(), map);
                        File fileRp = rp.isEmpty() ? autoRp : Paths.searchDir(jarDir.getAbsolutePath(), rp);

                        if(fileMap != null && fileRp != null){
                            Paths.copyFileOrFolder(fileMap, new File(minePath + "\\saves"));
                            Paths.copyFileOrFolder(fileRp, new File(minePath + "\\resourcepacks"));

                            SwingUtilities.invokeLater(() -> {
                            popup.dispose();
                            JOptionPane.showMessageDialog(null, text[7]);
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                            popup.dispose();
                            JOptionPane.showMessageDialog(null, text[8]);
                            });
                        }
                    } catch (Exception x){
                        SwingUtilities.invokeLater(() -> {;
                        popup.dispose();
                        JOptionPane.showMessageDialog(null, "Error: " + x.getMessage());
                        });
                    }
                }).start();
            }
        });

        setVisible(true);
    }
}
