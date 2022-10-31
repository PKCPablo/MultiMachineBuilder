/**
 * 
 */
package mmbmods.stn.block;

import java.awt.Color;

import javax.annotation.Nonnull;

import mmb.menu.world.window.GUITab;
import mmb.menu.world.window.WorldWindow;
import net.miginfocom.swing.MigLayout;
import mmbmods.stn.STNInventoryController;
import mmb.menu.world.inv.InventoryController;
import mmb.menu.world.inv.MoveItems;
import mmb.menu.helper.MenuHelper;
import mmb.menu.world.inv.AbstractInventoryController;
import javax.swing.JButton;

/**
 * @author oskar
 *
 */
public class STNTerminalGUI extends GUITab {
	private static final long serialVersionUID = -2283860749818164470L;
	
	@Nonnull private final STNTerminal term;
	STNTerminalGUI(STNTerminal term, WorldWindow window){
		this.term = term;
		setLayout(new MigLayout("", "[grow][grow][grow]", "[grow][][grow]"));
		
		InventoryController player = new InventoryController();
		window.playerInventory(player);
		add(player, "cell 0 0 1 3,grow");
		
		STNInventoryController stn = new STNInventoryController(term.network().inv);
		add(stn, "cell 2 0 1 2,grow");
		
		MoveItems moveItemsSTN = new MoveItems(player, stn);
		add(moveItemsSTN, "cell 1 0,grow");
		
		JButton btnNewButton = MenuHelper.newButton("#exit", Color.RED, e -> window.closeWindow(this));
		add(btnNewButton, "cell 1 1,growx");
		
		InventoryController stnQueue = new InventoryController();
		add(stnQueue, "cell 2 2,grow");
		
		MoveItems moveItemsQueue = new MoveItems(player, stnQueue);
		add(moveItemsQueue, "cell 1 2,grow");
	}
	@Override
	public void createTab(WorldWindow window) {
		//unused
	}

	@Override
	public void destroyTab(WorldWindow window) {
		term.gui = null;
	}

}