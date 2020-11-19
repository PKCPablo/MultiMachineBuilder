/**
 * 
 */
package mmb.ui.game;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import mmb.DATA.save.SaveLoad;
import mmb.WORLD.tileworld.block.Blocks;
import mmb.WORLD.tileworld.map.TileMap;
import mmb.WORLD.tileworld.map.World;
import mmb.debug.Debugger;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.awt.event.ActionEvent;

/**
 * @author oskar
 *
 */
@SuppressWarnings("serial")
public class NewGame extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final JTextField txtHeight;
	private final JTextField txtWidth;
	private final JTextField txtName;
	private final Debugger debug = new Debugger("NewGame");

	/**
	 * Create the dialog.
	 */
	public NewGame() {
		setTitle("Create a new world");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow]", "[][][]"));
		
			JLabel lblHeight = new JLabel("Height");
			contentPanel.add(lblHeight, "cell 0 0,alignx trailing");
		
			txtHeight = new JTextField();
			txtHeight.setText("Height");
			contentPanel.add(txtHeight, "cell 1 0,growx");
			txtHeight.setColumns(10);
		
			JLabel lblWidth = new JLabel("Width");
			contentPanel.add(lblWidth, "cell 0 1,alignx trailing");
		
			txtWidth = new JTextField();
			txtWidth.setText("Width");
			contentPanel.add(txtWidth, "cell 1 1,growx");
			txtWidth.setColumns(10);
		
			JLabel lblName = new JLabel("Name");
			contentPanel.add(lblName, "cell 0 2,alignx trailing");
		
			txtName = new JTextField();
			txtName.setText("Name");
			contentPanel.add(txtName, "cell 1 2,growx");
			txtName.setColumns(10);
	
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int w;
					int h;
					try {
						w = Integer.valueOf(txtWidth.getText());
						h = Integer.valueOf(txtWidth.getText());
					} catch (NumberFormatException e2) {
						debug.pstm(e2, "Incorrect dimensions: "+txtWidth.getText()+","+txtHeight.getText());
						return;
					}
					String n = txtName.getText();
					try {
						File newFile = new File("maps/"+n+".mworld");
						if(newFile.exists()) {
							debug.printl('"'+n+'"'+" already exists");
							return;
						}
						TileMap newMap = new TileMap(-w, -h, (2*w)+1, (2*h)+1);
						Arrays.fill(newMap.blocks, Blocks.grass); //Initialize the array
						newFile.createNewFile();
						OutputStream os = new FileOutputStream(newFile);
						try {
							String text = SaveLoad.save(new World(newMap));
							byte[] bin = text.getBytes();
							os.write(bin);
							os.flush();
							os.close();
						} catch (Exception e1) {
							debug.pstm(e1, "Failed to write the new world.");
							return;
						}
						debug.printl("Successfully created "+n);
						dispose();
					} catch (Exception e1) {
						debug.pstm(e1, "Failed to create the world file");
						return;
					}
				}
			});
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
		
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
			
		pack();
	}

}