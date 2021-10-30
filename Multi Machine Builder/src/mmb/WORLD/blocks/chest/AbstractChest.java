/**
 * 
 */
package mmb.WORLD.blocks.chest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mmb.DATA.json.JsonTool;
import mmb.WORLD.block.BlockEntity;
import mmb.WORLD.block.BlockEntityData;
import mmb.WORLD.inventory.Inventory;
import mmb.WORLD.inventory.storage.SimpleInventory;
import mmb.WORLD.rotate.Side;

/**
 * @author oskar
 *
 */
public abstract class AbstractChest extends BlockEntityData{
	@Nonnull protected SimpleInventory inv = new SimpleInventory();
	@Override
	public final void load(@Nullable JsonNode data) {
		if(data == null) return;
		inv.load(JsonTool.requestArray("inventory", (ObjectNode) data));
		load1((ObjectNode)data);
	}
	@Override
	protected final void save0(ObjectNode node) {
		node.set("inventory", inv.save());
		save1(node);
	}
	
	/**
	 * Additional function used to save additional data
	 * @param node node, to which data can be saved
	 */
	protected void save1(ObjectNode node) {
		//optional
	}
	/**
	 * Additional function used to save additional data
	 * @param node node, to which data can be loaded
	 */
	protected void load1(ObjectNode node) {
		//optional
	}
	@Override
	public Inventory getInventory(Side s) {
		return inv;
	}
	@Override
	public BlockEntity clone() {
		AbstractChest copy = (AbstractChest) super.clone();
		copy.inv = new SimpleInventory(inv);
		return copy;
	}	
}
