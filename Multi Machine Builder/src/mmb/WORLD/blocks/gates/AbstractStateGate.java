/**
 * 
 */
package mmb.WORLD.blocks.gates;

import com.fasterxml.jackson.databind.node.ObjectNode;

import mmb.WORLD.RotatedImageGroup;

/**
 * @author oskar
 *
 */
public abstract class AbstractStateGate extends AbstractUnaryGateBase {

	@Override
	public RotatedImageGroup getImage() {
		if(state) 
			return getOnImage();
		return getOffImage();
	}

	protected boolean state;
	@Override
	protected void save1(ObjectNode node) {
		node.put("state", state);
	}

	@Override
	protected void load1(ObjectNode node) {
		state = node.get("state").asBoolean();
	}
	
	protected void save2(ObjectNode node) {}

	protected void load2(ObjectNode node) {}
	
	protected abstract RotatedImageGroup getOnImage();
	protected abstract RotatedImageGroup getOffImage();
}
