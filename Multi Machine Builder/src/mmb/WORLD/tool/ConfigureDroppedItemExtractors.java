/**
 * 
 */
package mmb.WORLD.tool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mmb.DATA.contents.texture.Textures;
import mmb.WORLD.block.BlockEntry;
import mmb.WORLD.blocks.machine.Collector;

/**
 * @author oskar
 *
 */
public class ConfigureDroppedItemExtractors extends WindowTool {
	public ConfigureDroppedItemExtractors() {
		super("droppedItems");
	}

	@Override
	public String title() {
		return "Configure dropped item extrators";
	}

	public static final Icon icon = new ImageIcon(Textures.get("hoover.png"));
	@Override
	public Icon getIcon() {
		return icon;
	}

	@SuppressWarnings("null")
	@Override
	public void preview(int startX, int startY, int scale, Graphics g) {
		if(collector == null) {
			//frame.renderBlockRange(mouse.x, mouse.y, mouse.x, mouse.y, Color.RED, g);
		}else {
			int rangeX = Collector.clamp(4, mouse.x-collector.posX(), 16);
			int rangeY = Collector.clamp(4, mouse.y-collector.posY(), 16);
			frame.renderBlockRange(collector.posX(), collector.posY(), collector.posX()+collector.getRangeX()-1, collector.posY()+collector.getRangeY()-1, Color.BLUE, g);
			frame.renderBlockRange(collector.posX(), collector.posY(), collector.posX()+rangeX-1, collector.posY()+rangeY-1, Color.ORANGE, g);
		}
	}
	@Nonnull private Point mouse = new Point();
	private Collector collector;
	@Override
	public void mouseClicked(MouseEvent e) {
		frame.blockAt(e.getX(), e.getY(), mouse);
		switch(e.getButton()) {
		case 1: //LMB
			if(collector == null) {
				//select a  collector
				BlockEntry block = frame.getMap().get(mouse);
				if(block instanceof Collector) {
					collector = (Collector)block;
				}
			}else {
				//Select range
				int rangeX = Collector.clamp(4, mouse.x-collector.posX(), 16);
				int rangeY = Collector.clamp(4, mouse.y-collector.posY(), 16);
				collector.setRangeX(rangeX);
				collector.setRangeY(rangeY);
				collector = null;
			}
			
			break;
		case 2: //MMB
			break;
		case 3: //RMB
			collector = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		frame.blockAt(e.getX(), e.getY(), mouse);
	}

	@Override
	public String description() {
		if(collector == null) {
			return "Select collector with LMB";
		}
		return "Select DR corner of collection area with LMB, or click RMB to cancel. The orange frame shows collection area";
	}

}