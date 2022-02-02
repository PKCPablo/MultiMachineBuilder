/**
 * 
 */
package mmb.WORLD.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.Runnables;

import mmb.WORLD.crafting.recipes.SimpleProcessingRecipeGroup;
import mmb.WORLD.crafting.recipes.SimpleProcessingRecipeGroup.SimpleProcessingRecipe;
import mmb.WORLD.electric.Battery;
import mmb.WORLD.electric.VoltageTier;
import mmb.WORLD.inventory.Inventory;
import mmb.WORLD.inventory.ItemRecord;
import mmb.WORLD.items.ItemEntry;

/**
 * @author oskar
 * A class to help make simple item processors
 */
public class ElectroItemProcessHelper{
	@Nonnull private final SimpleProcessingRecipeGroup recipes;
	@Nonnull private final Inventory input;
	@Nonnull private final Inventory output;
	private final double speed;
	@Nonnull private final Battery elec;
	@Nonnull private final VoltageTier volt;
	/** The object which is currently refreshed. It may be null */
	public Refreshable refreshable;
	
	/** Energy put into item to smelt it */
	public double progress;
	
	//Info about recipe
	public double currRequired;
	
	/** The item which is currently smelted */
	public SimpleProcessingRecipe underway;
	public VoltageTier voltRequired;
	/**
	 * @param recipes list of recipes to use
	 * @param input input inventory
	 * @param output output inventory
	 * @param speed processing current in joules per tick at ULV
	 * @param elec the power source
	 * @param volt voltage tier
	 */
	public ElectroItemProcessHelper(SimpleProcessingRecipeGroup recipes, Inventory input, Inventory output,
			double speed, Battery elec, VoltageTier volt) {
		super();
		this.recipes = recipes;
		this.input = input;
		this.output = output;
		this.speed = speed;
		this.elec = elec;
		this.volt = volt;
	}
	
	public void save(ObjectNode node) {
		JsonNode smeltData = null;
		if(underway != null) smeltData = ItemEntry.saveItem(underway.input);
		node.set("smelt", smeltData);
		node.set("remain", new DoubleNode(progress));
	}
	public void load(JsonNode data) {
		JsonNode itemUnderWay = data.get("smelt");
		ItemEntry item = ItemEntry.loadFromJson(itemUnderWay);
		underway = recipes.recipes.get(item);
		JsonNode remainNode = data.get("remain");
		if(remainNode != null) progress = remainNode.asDouble();
	}
	
	public void cycle() {
		if(underway == null || progress > underway.energy) {
			//Time to take a new item
			loop:
			for(ItemRecord ir: input) {
				/*
				 * The criteria are:
				 * * The item exists in the machine
				 * * The item is a supported recipe
				 * * The recipe's voltage tier is not higher than this processor's voltage tier
				 */
				if(ir.amount() == 0) {
					//Item does not exist
					continue loop;
				}
				SimpleProcessingRecipe candidate = recipes.recipes.get(ir.item());
				if(candidate == null) {
					//Recipe does not exist
					continue loop;
				}
				if(candidate.voltage.compareTo(volt) > 0) {
					//The voltage tier is too high
					continue loop;
				}
				//Item is smeltable, take it
				int extracted = ir.extract(1);
				if(extracted == 1) {
					//Extracted
					progress = 0;
					underway = candidate;
					currRequired = candidate.energy;
					if(refreshable != null) refreshable.refreshInputs();
					return;
				}
				//else item is not smeltable, do not take it
			}
		}else if(progress < 0) {
			progress = 0;
		}else{
			//Continue smelting
			double amps = volt.speedMul * speed / volt.volts;
			double extract = elec.extract(amps, volt, Runnables.doNothing());
			elec.pressure -= (amps-extract)*volt.volts;
			progress += volt.volts * extract;
			if(progress > currRequired) {
				//Time to eject an item
				RecipeOutput result = underway.output;
				//Eject expected item
				result.produceResults(output.createWriter());
				progress -= currRequired;
				underway = null;
				if(refreshable != null) refreshable.refreshOutputs();
			}// else continue smelting
		}
		if(refreshable != null) refreshable.refreshProgress(progress, underway);
	}
	/**
	 * @author oskar
	 * An object which is refreshed during processing
	 */
	public static interface Refreshable{
		/** Refreshes the input list */
		public void refreshInputs();
		/** Refreshes the output list */
		public void refreshOutputs();
		/** Refreshes the progress bar 
		 * @param progress processing progress in ticks
		 * @param output item which is currently smelted
		 */
		public void refreshProgress(double progress, @Nullable SimpleProcessingRecipe output);
	}
}