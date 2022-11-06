/**
 * 
 */
package mmb.world.modulars.chest;

import javax.annotation.Nonnull;

import mmb.menu.world.inv.AbstractInventoryController;
import mmb.menu.world.inv.InventoryController;
import mmb.world.crafting.SingleItem;
import mmb.world.inventory.storage.SimpleInventory;
import mmb.world.inventory.storage.SingleItemInventory;
import mmb.world.item.ItemEntityType;
import mmb.world.items.ItemEntry;

/**
 * @author oskar
 *
 */
public class ChestCoreSingle extends ChestCore<SingleItemInventory> {
	@Nonnull private final ItemEntityType type;
	/**
	 * Creates a simple chest core (many different items)
	 * @param type item entity type
	 * @param size volume in cubic meters
	 */
	public ChestCoreSingle(ItemEntityType type, double size) {
		super(new SingleItemInventory().setCapacity(size));
		this.type = type;
	}

	@Override
	public ItemEntry itemClone() {
		ChestCoreSingle copy = new ChestCoreSingle(type, 0);
		copy.inventory.set(inventory);
		return copy;
	}

	@Override
	public ItemEntityType type() {
		return type;
	}

	@Override
	public AbstractInventoryController invctrl() {
		return new InventoryController(inventory);
	}

	@Override
	public ChestCore<SingleItemInventory> makeEmpty() {
		return new ChestCoreSingle(type, inventory.capacity());
	}

	@Override
	public SingleItem getRenderItem() {
		return inventory.getContents();
	}

}
