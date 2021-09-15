/**
 * 
 */
package mmb.WORLD.block;

import java.awt.Graphics;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mmb.BEANS.Rotable;
import mmb.WORLD.RotatedImageGroup;
import mmb.WORLD.Rotation;

/**
 * @author oskar
 *
 */
public abstract class SkeletalBlockEntityRotary extends BlockEntityData implements Rotable {
	@Override
	public void render(int x, int y, Graphics g, int ss) {
		getImage().get(side).draw(this, x, y, g, ss);
	}
	public abstract RotatedImageGroup getImage();
	@SuppressWarnings({ "null", "unused" })
	@Override
	public final void load(JsonNode data) {
		if(data == null) return;
		side = Rotation.valueOf(data.get("side").asText());
		if(side == null) side = Rotation.N;
		load1((ObjectNode) data);
	}
	@Nonnull protected Rotation side = Rotation.N;
	@Override
	public void setRotation(Rotation rotation) {
		side = rotation;
	}
	@Override
	public Rotation getRotation() {
		return side;
	}
	@Override
	protected final void save0(ObjectNode node) {
		node.put("side", side.toString());
		save1(node);
	}
	protected void save1(ObjectNode node) {}
	protected void load1(ObjectNode node) {}
}
