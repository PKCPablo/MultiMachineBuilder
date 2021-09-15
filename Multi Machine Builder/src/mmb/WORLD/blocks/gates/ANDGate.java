/**
 * 
 */
package mmb.WORLD.blocks.gates;

import mmb.DATA.contents.texture.Textures;
import mmb.WORLD.RotatedImageGroup;
import mmb.WORLD.block.BlockType;
import mmb.WORLD.blocks.ContentsBlocks;

/**
 * @author oskar
 *
 */
public class ANDGate extends AbstractBiGateBase {

	@Override
	public BlockType type() {
		return ContentsBlocks.AND;
	}

	@Override
	protected boolean run(boolean a, boolean b) {
		return a && b;
	}

	private static final RotatedImageGroup texture = RotatedImageGroup.create(Textures.get("logic/AND.png"));
	@Override
	public RotatedImageGroup getImage() {
		return texture;
	}

}
