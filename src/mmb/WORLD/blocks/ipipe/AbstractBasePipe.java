/**
 * 
 */
package mmb.WORLD.blocks.ipipe;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mmb.DATA.json.JsonTool;
import mmb.WORLD.block.BlockEntityChirotable;
import mmb.WORLD.block.BlockType;
import mmb.WORLD.crafting.RecipeOutput;
import mmb.WORLD.inventory.io.InventoryReader;
import mmb.WORLD.inventory.io.InventoryWriter;
import mmb.WORLD.inventory.storage.SingleItemInventory;
import mmb.WORLD.items.ItemEntry;
import mmb.WORLD.rotate.ChirotatedImageGroup;
import mmb.WORLD.rotate.Side;

/**
 * @author oskar
 * A base class for pipes.
 */
public abstract class AbstractBasePipe extends BlockEntityChirotable {
	
	@Override
	public InventoryReader getOutput(Side s) {
		if(s == getRotation().U() && outU != null) return outU;
		if(s == getRotation().D() && outD != null) return outD;
		if(s == getRotation().L() && outL != null) return outL;
		if(s == getRotation().R() && outR != null) return outR;
		return InventoryReader.NONE;
	}

	@Override
	public InventoryWriter getInput(Side s) {
		if(s == getRotation().U() && inU != null) return inU;
		if(s == getRotation().D() && inD != null) return inD;
		if(s == getRotation().L() && inL != null) return inL;
		if(s == getRotation().R() && inR != null) return inR;
		return InventoryWriter.NONE;
	}
	
	protected void setIn(InventoryWriter in, Side s) {
		switch(s) {
		case U:
			inU = in;
			break;
		case D:
			inD = in;
			break;
		case L:
			inL = in;
			break;
		case R:
			inR = in;
			break;
		default:
			break;
		}
	}
	
	protected void setOut(InventoryReader out, Side s) {
		switch(s) {
		case U:
			outU = out;
			break;
		case D:
			outD = out;
			break;
		case L:
			outL = out;
			break;
		case R:
			outR = out;
			break;
		default:
			break;
		}
	}

	@Nonnull private final BlockType type;

	@Override
	protected void save1(ObjectNode node) {
		node.set("items", JsonTool.saveArray(i -> ItemEntry.saveItem(i.getContents()), items));
	}

	@Override
	protected void load1(ObjectNode node) {
		JsonNode data = node.get("items");
		if(data == null || data.isNull() || data.isMissingNode()) return;
		JsonTool.loadToArray(ItemEntry::loadFromJson, (ArrayNode) data, items);
	}

	protected InventoryWriter inU, inD, inL, inR;
	protected InventoryReader outU, outD, outL, outR;
	
	@Nonnull protected final SingleItemInventory[] items;
	
	private final ChirotatedImageGroup texture;
	protected AbstractBasePipe(BlockType type, int numItems, ChirotatedImageGroup texture) {
		this.type = type;
		this.items = new SingleItemInventory[numItems];
		for(int i = 0; i < numItems; i++)
			items[i] = new SingleItemInventory();
		this.texture = texture;
		eventDemolition.addListener(event -> {
			for(SingleItemInventory item: items) {
				ItemEntry ent = item.getContents();
				if(ent != null)
					event.world.dropItem(ent, posX(), posY());
			}
		});
	}

	@Override
	public ChirotatedImageGroup getImage() {
		return texture;
	}

	@Override
	public BlockType type() {
		return type;
	}
	
	/**
	 * 
	 * @author oskar
	 *
	 */
	protected class Pusher implements InventoryWriter{
		private final SingleItemInventory from;
		@Nonnull private final Side other;
		/**
		 * 
		 * @param from item entry source variable
		 * @param other the side, to which items are pushed
		 */
		public Pusher(SingleItemInventory from, Side other) {
			this.from = from;
			this.other = other;
		}
		@Override
		public int write(ItemEntry ent, int amount) {
			return from.insert(ent, amount);
		}
		//Returns: did pushing make assigned slot free
		public boolean push() {
			Side cother = getRotation().apply(other);
			Side nother = cother.negate();
			InventoryWriter writer = owner().getAtSide(cother, posX(), posY()).getInput(nother);
			ItemEntry ent = from.getContents();
			if(ent == null) return true;
			int amt = writer.write(ent);
			if(amt == 1) {
				from.setContents(null);
				return true;
			}
			return false;
		}
		@Override
		public int bulkInsert(RecipeOutput block, int amount) {
			return from.bulkInsert(block, amount);
		}
	}		
}