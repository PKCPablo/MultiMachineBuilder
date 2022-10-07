/**
 * 
 */
package mmb.MENU.wtool;

import static mmb.GlobalSettings.$res;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mmb.BEANS.BlockActivateListener;
import mmb.DATA.contents.Textures;
import mmb.MENU.world.Placer;
import mmb.MENU.world.window.WorldFrame;
import mmb.MENU.world.window.WorldWindow;
import mmb.WORLD.block.BlockEntry;
import mmb.WORLD.inventory.ItemRecord;
import mmb.WORLD.items.ItemEntry;
import mmb.WORLD.worlds.world.World;
import mmb.debug.Debugger;

/**
 * @author oskar
 * This class defines a standard tool, applicable to most items.
 */
public class ToolStandard extends WindowTool{
	@Override
	public void mousePressed(MouseEvent e) {
		if(frame.ctrlPressed()) {
			mousePressedCtrl(e);
			return;
		}
		if(frame.shiftPressed()) {
			mousePressedShift(e);
			return;
		}
		int x = frame.getMouseoverBlockX();
		int y = frame.getMouseoverBlockY();
		switch(e.getButton()) {
		case 0:
			break;
		case 1: //LMB
			//If the block has ActionListener, run the listener
			World map = frame.getMap();
			if(!map.inBounds(x, y)) break;
			BlockEntry ent = map.get(x, y);
			if(ent instanceof BlockActivateListener) {
				((BlockActivateListener) ent).click(x, y, map, window, 0, 0);
				debug.printl("Running BlockActivateListener for: ["+x+","+y+"]");
			}else {
				placeBlock(x, y, map, window);
			}
			break;
		case 3: //RMB
			frame.showPopup(e);
			break;
		case 2: //MMB
			break;
		default:
			break;
		}
	}
	/**
	 * @param x
	 * @param y
	 * @param map
	 */
	public static void placeBlock(int x, int y, World map, WorldWindow window) {
		//Place the block
		ItemRecord irecord = window.getPlacer().getSelectedValue();
		if(irecord == null) return;
		ItemEntry item = irecord.item();
		if(item instanceof Placer) {
			//If in survival, consume items
			if(window.getPlayer().isSurvival()) {
				//In survival
				int extracted = irecord.extract(1);
				if(extracted == 0) return;
			}
			 ((Placer)item).place(x, y, map);
		}
	}
	private void mousePressedCtrl(MouseEvent e) {
		int x = frame.getMouseoverBlockX();
		int y = frame.getMouseoverBlockY();
		BlockEntry block = frame.getMap().get(x, y);
		switch(e.getButton()) {
		case 1: //LMB
			//Turn CCW
			block.ccw();
			break;
		case 2: //MMB
			//Turn around
			block.ccw();
			block.ccw();
			break;
		case 3: //RMB
			//Turn CW
			block.cw();
		}
	}
	private void mousePressedShift(MouseEvent e) {
		int x = frame.getMouseoverBlockX();
		int y = frame.getMouseoverBlockY();
		World map = frame.getMap();
		switch(e.getButton()) {
		case 1: //LMB
			placeBlock(x, y, map, window);
			break;
		case 2: //MMB
			//Go here
			frame.perspective.x = -x;
			frame.perspective.y = -y;
			break;
		case 3: //RMB
			//Mine
			if(map.removeMachine(x, y)) {
				debug.printl("Removed machine");
				return;
			}
			//Drop if needed
			if(frame.getPlayer().isSurvival()) {
				//The player is survival, requires pickaxe
				return;
			}
			BlockEntry block = frame.getMap().get(x, y);
			block.type().leaveBehind().place(x, y, map);
		}
	}
	private WorldFrame frame;
	@Override
	public void setWindow(WorldWindow window) {
		this.window = window;
		frame = window.getWorldFrame();
	}
	private static final Debugger debug = new Debugger("TOOL-STANDARD");
	public ToolStandard() {
		super("standard");
	}
	public static final Icon ICON_NORMAL = new ImageIcon(Textures.get("tool/normal.png"));
	@Override
	public Icon getIcon() {
		return ICON_NORMAL;
	}
	private final String title = $res("toolstd");
	@Override
	public String title() {
		return title;
	}
	@Override
	public void preview(int x, int y, double scale, Graphics g) {
		ItemRecord irecord = frame.getPlacer().getSelectedValue();
		if(irecord == null) return;
		ItemEntry placer0 = irecord.item();
		if(placer0 instanceof Placer) ((Placer) placer0).preview(g, new Point(x, y), frame.getMap(), frame.getMouseoverBlock(), (int)Math.ceil(scale));
	}
	private static final String descr1 = $res("toolstd-1");
	private static final String descr2 = $res("toolstd-2");
	private static final String descr3 = $res("toolstd-3");
	private static final String descr4 = $res("toolstd-4");
	private static final String descr = descr1+'\n'+descr2+'\n'+descr3+'\n'+descr4;
	@Override
	public String description() {
		return descr;
	}
	@Override
	public void keyPressed(KeyEvent e) {
		World map = frame.getMap();
		if(!map.inBounds(bx, by)) return;
		BlockEntry ent = map.get(bx, by);
		switch(e.getKeyCode()) {
		case KeyEvent.VK_I:
			ent.flipH();
			debug.printl("Flipped");
			break;
		case KeyEvent.VK_J:
			ent.flipNW();
			debug.printl("Flipped");
			break;
		case KeyEvent.VK_K:
			ent.flipV();
			debug.printl("Flipped");
			break;
		case KeyEvent.VK_L:
			ent.flipNE();
			debug.printl("Flipped");
			break;
		default:
			break;
		}
	}
	private int bx, by;
	@Override
	public void mouseMoved(MouseEvent e) {
		bx = frame.getMouseoverBlockX();
		by = frame.getMouseoverBlockY();
	}
	
}