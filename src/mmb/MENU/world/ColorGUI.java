/**
 * 
 */
package mmb.MENU.world;

import javax.swing.JColorChooser;

import mmb.MENU.world.window.GUITab;
import mmb.MENU.world.window.WorldWindow;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Component;
import java.util.function.Consumer;

import net.miginfocom.swing.MigLayout;

/**
 * @author oskar
 *
 */
public class ColorGUI extends GUITab {
	private static final long serialVersionUID = 5195216057761670896L;

	/**
	 * Create the panel.
	 * @param c color reference
	 * @param win world window
	 */
	@SuppressWarnings("null")
	public ColorGUI(Color initial, Consumer<Color> action, WorldWindow win) {
		setLayout(new MigLayout("", "[fill]", "[450px][21px][21px]"));
		
		JColorChooser colorChooser = new JColorChooser();
		add(colorChooser, "cell 0 0,grow,aligny center");
		colorChooser.setColor(initial);
		
		JButton btnNewButton = new JButton("OK");
		btnNewButton.setBackground(Color.GREEN);
		btnNewButton.addActionListener(e -> {
			action.accept(colorChooser.getColor());
			win.closeWindow(this);
		});
		btnNewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(btnNewButton, "cell 0 1,growx,aligny center");
		
		JButton btnNewButton_1 = new JButton("Cancel");
		btnNewButton_1.setBackground(Color.RED);
		btnNewButton_1.addActionListener(e -> win.closeWindow(this));
		btnNewButton_1.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(btnNewButton_1, "cell 0 2,growx,aligny center");
	}

	@Override
	public void createTab(WorldWindow window) {
		//unused
	}

	@Override
	public void destroyTab(WorldWindow window) {
		//unused
	}

}