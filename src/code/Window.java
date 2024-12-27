package code;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

public class Window extends JFrame{
    public Window(String language){
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("resources/instalacraft.png"));
        String text[] = new String[]{"Map", "Install", "Files found automatically:", "Click 'Close' to continue", "Files found", "Close", "Starting the installation...", "Map and/or Resourcepack successfully installed!", "Error: File(s) not found", "Installing...", "Install map only", "Install resource only", "Install both", "Only one map and/or resource pack was found. Would you like to install it?", "Automatic installation", "Error when installing automatically: "}; 

        if (language.equals("pt")) {
            text[0] = "Mapa";
            text[1] = "Instalar";
            text[2] = "Arquivos encontrados automaticamente:";
            text[3] = "Clique 'Fechar' para continuar";
            text[4] = "Arquivos encontrados";
            text[5] = "Fechar";
            text[6] = "Iniciando a instalação...";
            text[7] = "Mapa e/ou Resourcepack instalados com sucesso!";
            text[8] = "Erro: arquivo(s) não encontrado(s)";
            text[9] = "Instalando...";
            text[10] = "Instalar apenas mapa";
            text[11] = "Instalar apenas resource";
            text[12] = "Instalar ambos";
            text[13] = "Apenas um mapa e/ou resource pack foi encontrado. Deseja instalar?";
            text[14] = "Instalação automática";
            text[15] = "Erro ao instalar automaticamente: ";
        }

        setTitle("Instalacraft");
        setIconImage(icon.getImage());
        setSize(500,250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JComboBox<String> mapCbBox = new JComboBox<>();
        JComboBox<String> rpCbBox = new JComboBox<>();

        String jarDir = Pathes.getJarDir();
        List<String> items = Loader.findMapsAndResources(jarDir);
        Map<String, File> autoDetect = Loader.firstLoad(jarDir);

        if(autoDetect != null){
            if(autoInstall(text, autoDetect) != 0){
                return;
            }
        }

        if(items != null){
            for(String item : items){
                if(new File(jarDir + "/" + item, "level.dat").exists()){
                    mapCbBox.addItem(item);
                } else if (new File(jarDir + "/" + item, "pack.mcmeta").exists() || item.endsWith(".zip")){
                    rpCbBox.addItem(item);
                }
            }
        }


        JPanel panelInputs = new JPanel();
        panelInputs.setLayout(new BoxLayout(panelInputs, BoxLayout.Y_AXIS));
        panelInputs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelMap = new JPanel(new BorderLayout());
        JLabel mapTxt = new JLabel(text[0] + ":");
        panelMap.add(mapTxt,BorderLayout.NORTH);
        panelMap.add(mapCbBox,BorderLayout.CENTER);

        JPanel panelResource = new JPanel(new BorderLayout());
        JLabel resourceTxt = new JLabel("Resourcepack:");
        panelResource.add(resourceTxt,BorderLayout.NORTH);
        panelResource.add(rpCbBox,BorderLayout.CENTER);

        JButton installBtn = new JButton(text[1]);
        installBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelInputs.add(panelMap);
        panelInputs.add(Box.createRigidArea(new Dimension(0,10)));
        panelInputs.add(panelResource);
        panelInputs.add(Box.createRigidArea(new Dimension(0,20)));
        panelInputs.add(installBtn);
        
        add(panelInputs,BorderLayout.CENTER);

        installBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                JDialog popup = new JDialog(Window.this, text[9], true);
                JLabel installing = new JLabel(text[6], SwingConstants.CENTER);
                JProgressBar pb = new JProgressBar(0,100);
                pb.setStringPainted(true);
                popup.add(installing);
                popup.add(pb, BorderLayout.SOUTH);
                popup.setSize(300,100);
                popup.setLocationRelativeTo(Window.this);
                popup.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                SwingUtilities.invokeLater(() -> popup.setVisible(true));

                SwingWorker<Void, Integer> installProcess = new SwingWorker<Void,Integer>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        installBtn.setEnabled(false);

                        String map = mapCbBox.getSelectedItem().toString();
                        String rp = rpCbBox.getSelectedItem().toString();

                        String minePath = Pathes.getMinecraftOrigin();
                        File fileMap = Pathes.searchDir(jarDir, map);
                        File fileRp = Pathes.searchDir(jarDir, rp);

                        if(fileMap == null || fileRp == null){
                            publish(0);
                            return null;
                        }

                        if(fileMap != null && fileRp != null){ 
                            long totalBytes = fileMap.length() + fileRp.length();
                            final long[] copiedBytes = {0};

                            Files.walk(fileMap.toPath()).forEach(sourcePath -> {
                                try {
                                    Path mapFolderPath = Paths.get(minePath + File.separator + "saves" + File.separator + map);
                                    Path relativePath = fileMap.toPath().relativize(sourcePath);
                                    Path tPath = mapFolderPath.resolve(relativePath);

                                    if(Files.isDirectory(sourcePath)){
                                        if(!Files.exists(tPath))
                                        Files.createDirectories(tPath);
                                    } else {
                                        Files.copy(sourcePath, tPath, StandardCopyOption.REPLACE_EXISTING);
                                    }

                                    copiedBytes[0] += Files.size(sourcePath);
                                    int progress = (int) ((copiedBytes[0] * 100) / totalBytes);
                                    publish(progress);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });

                            Files.walk(fileRp.toPath()).forEach(sourcePath -> {
                                try {
                                    Path rpFolderPath = Paths.get(minePath + File.separator + "resourcepacks" + File.separator + map);
                                    Path relativePath = fileMap.toPath().relativize(sourcePath);
                                    Path tPath = rpFolderPath.resolve(relativePath);

                                    if(Files.isDirectory(sourcePath)){
                                        if(!Files.exists(tPath))
                                        Files.createDirectories(tPath);
                                    } else {
                                        Files.copy(sourcePath, tPath, StandardCopyOption.REPLACE_EXISTING);
                                    }


                                    copiedBytes[0] += Files.size(sourcePath);
                                    int progress = (int) ((copiedBytes[0] * 100) / totalBytes);
                                    publish(progress);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });

                            return null;
                        } else {
                            return null;
                        }
                    }

                    @Override
                    protected void process(List<Integer> chunks) {

                        int process = chunks.get(chunks.size() - 1);
                        SwingUtilities.invokeLater(() -> pb.setValue(process));
                    }

                    @Override
                    protected void done(){
                        try {
                            if(get() == null){
                                SwingUtilities.invokeLater(() -> {
                                    popup.dispose();
                                    JOptionPane.showMessageDialog(null, text[7]);
                                    Window.this.toFront();
                                    Window.this.requestFocus();
                                });
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    popup.dispose();
                                    JOptionPane.showMessageDialog(null, text[8]);
                                    Window.this.toFront();
                                    Window.this.requestFocus();
                                });
                        }
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(() -> {
                                popup.dispose();
                                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                                Window.this.toFront();
                                Window.this.requestFocus();
                            });
                            
                        }
                        installBtn.setEnabled(true);
                    }
                };

                installProcess.execute();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if(installProcess.isDone()){
                        installProcess.cancel(true);
                    }
                }));
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e){
                super.windowClosing(e);
            }
        });

        setVisible(true);
    }

    private int autoInstall(String[] text, Map<String, File> autoDetect){
        int option = JOptionPane.showOptionDialog(this, 
                text[13], 
                text[14], 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                new String[]{text[10],text[11],text[12]}, 
                text[12]
                );

                if(option == JOptionPane.CLOSED_OPTION){
                    return 0;
                }

                JOptionPane.showMessageDialog(this, text[6], text[9], JOptionPane.INFORMATION_MESSAGE);

                try {
                    String minePath = Pathes.getMinecraftOrigin();
                    if(option == 0 && autoDetect.containsKey("map")){
                        Pathes.copyFileOrFolder(autoDetect.get("map"), new File(minePath + "\\saves"));
                    } else if(option == 1 && autoDetect.containsKey("resource")){
                        Pathes.copyFileOrFolder(autoDetect.get("resource"), new File(minePath + "\\resourcepacks"));
                    } else if(option == 2){
                        if(autoDetect.containsKey("map")){
                            Pathes.copyFileOrFolder(autoDetect.get("map"), new File(minePath + "\\saves"));
                        }
                        if(autoDetect.containsKey("resource")){
                            Pathes.copyFileOrFolder(autoDetect.get("resource"), new File(minePath + "\\resourcepacks")); 
                        }
                    }
                    JOptionPane.showMessageDialog(this, text[7]);
                    return 1;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, text[15] + e.getMessage());
                    return 0;
                }
    }
}
