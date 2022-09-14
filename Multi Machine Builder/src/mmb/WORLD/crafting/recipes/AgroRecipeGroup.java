/**
 * 
 */
package mmb.WORLD.crafting.recipes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mmb.WORLD.crafting.Craftings;
import mmb.WORLD.crafting.Recipe;
import mmb.WORLD.crafting.RecipeOutput;
import mmb.WORLD.electric.VoltageTier;
import mmb.WORLD.gui.craft.AgroRecipeView;
import mmb.WORLD.gui.craft.RecipeView;
import mmb.WORLD.inventory.Inventory;
import mmb.WORLD.items.ItemEntry;
import monniasza.collects.Collects;
import monniasza.collects.Identifiable;
import monniasza.collects.selfset.HashSelfSet;
import monniasza.collects.selfset.SelfSet;

/**
 * @author oskar
 *
 */
public class AgroRecipeGroup extends AbstractRecipeGroup<AgroRecipeGroup.AgroProcessingRecipe>{
	public AgroRecipeGroup(String id) {
		super(id);
	}
	
	/**
	 * @author oskar
	 * A recipe with one input item and output
	 */
	public class AgroProcessingRecipe implements Identifiable<ItemEntry>, Recipe<AgroProcessingRecipe>{
		@Nonnull public final ItemEntry input;
		@Nonnull public final RecipeOutput output;
		public final int duration;
		@Nonnull public final AgroRecipeGroup group;
		public AgroProcessingRecipe(ItemEntry input, RecipeOutput output, int duration) {
			this.input = input;
			this.output = output;
			this.duration = duration;
			group = AgroRecipeGroup.this;
		}
		@Override
		public ItemEntry id() {
			return input;
		}
		@Override
		public int maxCraftable(Inventory src, int amount) {
			return Inventory.howManyTimesThisContainsThat(src, input);
		}
		@Override
		public int craft(Inventory src, Inventory tgt, int amount) {
			return Craftings.transact(input, output, tgt, src, amount);
		}
		@Override
		public RecipeOutput output() {
			return output;
		}
		@Override
		public RecipeOutput inputs() {
			return input;
		}
		@Override
		public ItemEntry catalyst() {
			return null;
		}
		@Override
		public AgroRecipeGroup group() {
			return group;
		}
		@Override
		public AgroProcessingRecipe that() {
			return this;
		}
		@Override
		public double energy() {
			return 0;
		}
		@Override
		public VoltageTier voltTier() {
			return VoltageTier.V1;
		}
	}
	@Nonnull private final SelfSet<ItemEntry, AgroProcessingRecipe> _recipes = new HashSelfSet<>();
	@Nonnull public final SelfSet<ItemEntry, AgroProcessingRecipe> recipes = Collects.unmodifiableSelfSet(_recipes);
	@Override
	public Set<? extends ItemEntry> supportedItems() {
		return supported0;
	}
	private final Set<ItemEntry> supported = new HashSet<>();
	private final Set<ItemEntry> supported0 = Collections.unmodifiableSet(supported);
	/**
	 * Adds a recipes to this recipe group
	 * @param in input item
	 * @param out output
	 * @param duration time between successive drops
	 * @return the recipe
	 */
	public AgroProcessingRecipe add(ItemEntry in, RecipeOutput out, int duration) {
		AgroProcessingRecipe recipe = new AgroProcessingRecipe(in, out, duration);
		_recipes.add(recipe);
		GlobalRecipeRegistrar.addRecipe(recipe);
		supported.add(in);
		return recipe;
	}
	/**
	 * @param in input item
	 * @param out output item
	 * @param amount amount of output item
	 * @param duration time between successive drops
	 * @return the recipe
	 */
	public AgroProcessingRecipe add(ItemEntry in, ItemEntry out, int amount, int duration) {
		return add(in, out.stack(amount), duration);
	}
	@Override
	public SelfSet<ItemEntry, AgroProcessingRecipe> recipes() {
		return recipes;
	}
	@Override
	public RecipeView<AgroProcessingRecipe> createView() {
		return new AgroRecipeView();
	}
	@Override
	public boolean isCatalyzed() {
		return false;
	}
}