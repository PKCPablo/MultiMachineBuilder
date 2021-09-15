/**
 * 
 */
package mmb.WORLD.blocks;

import mmb.WORLD.block.BlockType;
import mmb.WORLD.block.BlockEntityDataless;
import mmb.WORLD.worlds.MapProxy;
import mmb.WORLD.worlds.SignalUtils;

/**
 * @author oskar
 *
 */
public class WWWire extends BlockEntityDataless {

	@Override
	public BlockType type() {
		return ContentsBlocks.ww_wire;
	}

	@Override
	public void onTick(MapProxy map) {
		int count = SignalUtils.allIncomingSignals(posX(), posY(), owner());
		if(count == 1 || count == 2) {
			map.place(ContentsBlocks.ww_head, posX(), posY());
		}
	}
	
}
