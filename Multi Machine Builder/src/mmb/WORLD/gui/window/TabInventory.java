/**
 * 
 */
package mmb.WORLD.gui.window;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import com.pploder.events.CatchingEvent;
import com.pploder.events.Event;

import mmb.debug.Debugger;
import monniasza.collects.Collects;

import javax.swing.JButton;
import javax.swing.JLabel;

import mmb.WORLD.block.Block;
import mmb.WORLD.block.BlockEntityType;
import mmb.WORLD.block.BlockType;
import mmb.WORLD.crafting.Recipe;
import mmb.WORLD.gui.CreativeItemList;
import mmb.WORLD.inventory.Inventory;
import mmb.WORLD.inventory.ItemRecord;
import mmb.WORLD.item.Item;
import mmb.WORLD.item.ItemEntityType;
import mmb.WORLD.item.ItemType;
import mmb.WORLD.item.Items;
import mmb.WORLD.items.ItemEntry;
import mmb.WORLD.worlds.world.Player;
import static mmb.GlobalSettings.*;

import java.awt.Color;
import mmb.MENU.components.BoundCheckBox;
import javax.swing.JSpinner;
import mmb.WORLD.gui.inv.CraftGUI;
import mmb.WORLD.gui.inv.InventoryController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import mmb.WORLD.gui.SelectSortItemTypes;

import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;

import javax.swing.JCheckBox;
import javax.swing.JList;
import java.util.ResourceBundle;

/**
 * @author oskar
 *
 */
public class TabInventory extends JPanel {
	private static final long serialVersionUID = 4210914960590758120L;
	
	private JScrollPane creativeScrollPane;
	private CreativeItemList creativeItemList;
	
	private static final Debugger debug = new Debugger("PLAYER INVENTORY");
	private Player player;
	public final Event<Player> playerChanged = new CatchingEvent<>(debug, "Failed to process player changed event");
	public final WorldWindow window;
	
	public static interface Tagsel{
		public DefaultListModel<ItemType> eligible();
		public String title();
	}
	
	private static class AllTagsel implements Tagsel{
		@Override
		public DefaultListModel<ItemType> eligible() {
			return CreativeItemList.model;
		}

		@Override
		public String title() {
			return "1 All tags";
		}

		@Override
		public String toString() {
			return "1 All tags";
		}
	}
	
	public static class FilterTagsel implements Tagsel{
		public final String tag;
		public final DefaultListModel<ItemType> set;
		public FilterTagsel(String s, Predicate<ItemType> filter) {
			tag = "2 "+s;
			set = new DefaultListModel<ItemType>();
			for(ItemType item: Items.items) {
				if(filter.test(item)) set.addElement(item);
			}
		}
		@Override
		public DefaultListModel<ItemType> eligible() {
			return set;
		}
		@Override
		public String title() {
			return tag;
		}
		@Override
		public String toString() {
			return tag;
		}
	}
	
	public static class TaggedSel implements Tagsel{
		public final String tag;
		public final DefaultListModel<ItemType> set;
		public TaggedSel(String s, Set<ItemType> set2) {
			tag = "3 "+s;
			set = new DefaultListModel<ItemType>();
			for(ItemType item: set2) {
				set.addElement(item);
			}
		}
		@Override
		public DefaultListModel<ItemType> eligible() {
			return set;
		}
		@Override
		public String title() {
			return tag;
		}
		@Override
		public String toString() {
			return tag;
		}
	}
	
	/**
	 * Create an inventory panel with a player pre-set
	 * @param window world window, for which the tab is created
	 * @param player player represented in this tab
	 */
	public TabInventory(WorldWindow window, Player player) {
		this(window);
		setPlayer(player);
	}	
	/**
	 * Create an inventory panel without a player
	 * @param window world window, for which the tab is created
	 * @wbp.parser.constructor
	 */
	public TabInventory(WorldWindow window) {
		this.window = window;
		setLayout(new MigLayout("", "[:400.00:400.00,grow,fill][][grow]", "[20px,grow]"));
		InventoryController ctrl = new InventoryController();
		craftGUI = new CraftGUI(2, null, null, ctrl);
		timer = new Timer(0, e -> craftGUI.inventoryController.refresh());
		add(craftGUI, "cell 1 0,growy");
		
		creativePanel = new JPanel();
		add(creativePanel, "cell 0 0,grow");
		creativePanel.setLayout(new MigLayout("", "[grow][grow]", "[][grow][grow]"));
		
		
		lblSort = new JLabel($res("wgui-sort"));
		creativePanel.add(lblSort, "cell 0 0");
		
		panel = new JPanel();
		creativePanel.add(panel, "cell 1 0,growx");
		panel.setLayout(new MigLayout("", "[][]", "[][][][][]"));
		
		lblCreativeItems = new JLabel($res("wgui-creati"));
		panel.add(lblCreativeItems, "cell 0 0");
		
		checkSurvival = new BoundCheckBox();
		checkSurvival.setText($res("wgui-creamode"));
		panel.add(checkSurvival, "cell 1 0");
		
		
		lbAddRemoveCount = new JLabel($res("wgui-icount"));
		panel.add(lbAddRemoveCount, "cell 0 1");
		
		itemAmt = new JSpinner();
		panel.add(itemAmt, "cell 1 1,growx");
		
		JButton btnAdd = new JButton($res("wgui-a1"));
		panel.add(btnAdd, "cell 0 2,growx");
		btnAdd.addActionListener(e -> addItems(1));
		btnAdd.setBackground(new Color(0, 170, 0));
		
		JButton btnAddN = new JButton($res("wgui-an"));
		panel.add(btnAddN, "cell 1 2,growx");
		btnAddN.addActionListener(e -> {
			@SuppressWarnings("boxing")
			int amt = (Integer)(itemAmt.getValue());
			addItems(amt);
		});
		btnAddN.setBackground(new Color(0, 204, 0));
		
		JButton btnRemove = new JButton($res("wgui-r1"));
		panel.add(btnRemove, "cell 0 3,growx");
		btnRemove.addActionListener(e -> removeItems(1));
		
		btnRemove.setBackground(new Color(170, 0, 0));
		
		JButton btnRemoveN = new JButton($res("wgui-rn"));
		btnRemoveN.addActionListener(e -> {
			@SuppressWarnings("boxing")
			int amt = (Integer)(itemAmt.getValue());
			removeItems(amt);
		});
		btnRemoveN.setBackground(new Color(204, 0, 0));
		panel.add(btnRemoveN, "cell 1 3");
		
		JButton btnREmoveAll = new JButton($res("wgui-ra"));
		btnREmoveAll.addActionListener(e -> removeItems(Integer.MAX_VALUE));
		btnREmoveAll.setBackground(new Color(255, 0, 0));
		panel.add(btnREmoveAll, "cell 0 4 2 1,growx");
		
		selectSortItemTypes = new SelectSortItemTypes(() -> tags);
		creativePanel.add(selectSortItemTypes, "cell 0 1,grow");
		
		//Creative Item List
		creativeScrollPane = new JScrollPane();
		creativePanel.add(creativeScrollPane, "cell 1 1 1 2,growy");
		creativeItemList = new CreativeItemList();
		creativeScrollPane.setViewportView(creativeItemList);
		
		//Tags
		DefaultListModel<Tagsel> model = new DefaultListModel<>();
		model.addElement(new AllTagsel());
		model.addElement(new FilterTagsel("Blocks", i -> i instanceof BlockType));
		model.addElement(new FilterTagsel("Items", i -> !(i instanceof BlockType)));
		model.addElement(new FilterTagsel("Block entities", i -> i instanceof BlockEntityType));
		model.addElement(new FilterTagsel("Item entities", i -> i instanceof ItemEntityType));
		model.addElement(new FilterTagsel("Simple blocks", i -> i instanceof Block));
		model.addElement(new FilterTagsel("Simple items", i -> i instanceof Item && !(i instanceof BlockType)));
		for(Entry<String, Collection<ItemType>> data : Items.tags.asMap().entrySet()) {
			String s = data.getKey();
			Set<ItemType> set = (Set<ItemType>) data.getValue();
			model.addElement(new TaggedSel(s, set));
		}
		
		scrollPane = new JScrollPane();
		creativePanel.add(scrollPane, "cell 0 2,grow");
		tags = new JList<>();
		scrollPane.setViewportView(tags);
		tags.setModel(model);
		tags.addListSelectionListener(e -> {
			Tagsel sel = tags.getSelectedValue();
			creativeItemList.resort(selectSortItemTypes.getSelectedValue(), sel.eligible());
			creativeItemList.setModel(sel.eligible());
		});
		//Sort the tags
		Collections.sort(Collects.toWritableList(model), (a, b) -> a.title().compareTo(b.title()));
		
		//Crafting		
		craftingsPanel = new JPanel();
		add(craftingsPanel, "cell 2 0,grow");
		craftingsPanel.setLayout(new BoxLayout(craftingsPanel, BoxLayout.Y_AXIS));
		
		//Find recipes
		JLabel lblRecipes = new JLabel($res("wguicf-head"));
		craftingsPanel.add(lblRecipes);
		
		JButton btnCraftTo = new JButton($res("wguicf-prod"));
		btnCraftTo.addActionListener(e -> {
			ItemEntry item = findSourceItem();
			if(item != null) {
				queryRecipes(producing(item));
			}else {
				queryRecipes(all());
			}
		});
		btnCraftTo.setBackground(Color.GREEN);
		craftingsPanel.add(btnCraftTo);
		
		JButton btnCraftRandom = new JButton($res("wguicf-chan"));
		btnCraftRandom.setBackground(new Color(204, 255, 0));
		btnCraftRandom.addActionListener(e -> {
			ItemEntry item = findSourceItem();
			if(item != null) {
				queryRecipes(gambling(item));
			}else {
				queryRecipes(all());
			}
		});
		craftingsPanel.add(btnCraftRandom);
		
		JButton btnCraftFrom = new JButton($res("wguicf-cons"));
		btnCraftFrom.setBackground(Color.RED);
		btnCraftFrom.addActionListener(e -> {
			ItemEntry item = findSourceItem();
			if(item != null) {
				queryRecipes(consuming(item));
			}else {
				queryRecipes(all());
			}
		});
		craftingsPanel.add(btnCraftFrom);
		
		JButton btnCraftBy = new JButton($res("wguicf-mac"));
		btnCraftBy.setEnabled(false);
		btnCraftBy.setBackground(Color.BLUE);
		craftingsPanel.add(btnCraftBy);
		
		JButton btnCraftWith = new JButton($res("wguicf-cat"));
		btnCraftWith.setBackground(new Color(255, 255, 0));
		btnCraftWith.addActionListener(e -> {
			ItemEntry item = findSourceItem();
			if(item != null) {
				queryRecipes(catalysing(item));
			}else {
				queryRecipes(all());
			}
		});
		craftingsPanel.add(btnCraftWith);
		
		JButton btnCraftAll = new JButton("ALL recipes");
		btnCraftAll.setBackground(Color.GRAY);
		btnCraftAll.addActionListener(e -> queryRecipes(all()));
		craftingsPanel.add(btnCraftAll);
		
		checkUseCIL = new JCheckBox("Use the creative item list");
		checkUseCIL.setSelected(true);
		craftingsPanel.add(checkUseCIL);
		
	}
	private void removeItems(int amount) {
		int remain = amount;
		List<ItemRecord> records = craftGUI.inventoryController.getSelectedValuesList();
		for(ItemRecord record: records) {
			if(remain == 0) return;
			remain -= record.extract(amount);
		}
		craftGUI.inventoryController.refresh();
	}
	private void addItems(int amount) {
		Inventory inv = craftGUI.inventoryController.getInv();
		if(inv == null) {
			debug.printl("Got null inventory");
			return;
		}
		List<ItemType> items = creativeItemList.getSelectedValuesList();
		if(items == null) {
			debug.printl("Got null item list");
			return;
		}
		for(ItemType item: items) {
			if(item == null) {
				debug.printl("Got null item");
				continue;
			}
			inv.insert(item.create(), amount);
		}
		craftGUI.inventoryController.refresh();
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}
	/**
	 * @param player the player to set
	 */
	public void setPlayer(Player player) {
		if(this.player == player) return;
		this.player = player;
		playerChanged.trigger(player);
		craftGUI.inventoryController.setInv(player.inv);
		checkSurvival.setVariable(player.creative);
	}
	
	private final Timer timer;
	private JLabel lblCreativeItems;
	private BoundCheckBox checkSurvival;
	private JLabel lbAddRemoveCount;
	private JPanel panel;
	private JSpinner itemAmt;
	public final CraftGUI craftGUI;
	private SelectSortItemTypes selectSortItemTypes;
	private JLabel lblSort;
	private JPanel creativePanel;
	private JPanel craftingsPanel;
	private JCheckBox checkUseCIL;
	private JList<Tagsel> tags;
	private JScrollPane scrollPane;

	/**
	 * @author oskar
	 * Describes a rule or set of rules to filter recipes
	 */
	public static interface RecipeQuery{
		/**
		 * @return the displayed query string
		 */
		public String name();
		/**
		 * @param recipe the recipe to test
		 * @return does recipe match?
		 */
		public boolean filter(Recipe<?> recipe);
	}
	private void queryRecipes(RecipeQuery query) {
		debug.printl("Querying recipes");
		window.openAndShowWindow(new QueriedRecipes(query, window), "Recipe search: "+query.name());
		debug.printl("Query finished");
	}
	
	@Nonnull public static RecipeQuery all() {
		return new RecipeQuery() {
			@Override
			public String name() {
				return "All recipes";
			}

			@Override
			public boolean filter(Recipe<?> recipe) {
				return true;
			}
		};
	}
	@Nonnull public static RecipeQuery consuming(ItemEntry item) {
		return new RecipeQuery() {
			@Override
			public String name() {
				return "Recipes which consume: "+item.title();
			}

			@Override
			public boolean filter(Recipe<?> recipe) {
				return recipe.inputs().contains(item);
			}
		};
	}
	@Nonnull public static RecipeQuery producing(ItemEntry item) {
		return new RecipeQuery() {
			@Override
			public String name() {
				return "Recipes which produce: "+item.title();
			}

			@Override
			public boolean filter(Recipe<?> recipe) {
				return recipe.output().contains(item);
			}
		};
	}
	@Nonnull public static RecipeQuery gambling(ItemEntry item) {
		return new RecipeQuery() {
			@Override
			public String name() {
				return "Recipes which produce: "+item.title();
			}

			@Override
			public boolean filter(Recipe<?> recipe) {
				return recipe.luck().contains(item);
			}
		};
	}
	@Nonnull public static RecipeQuery catalysing(ItemEntry item) {
		return new RecipeQuery() {
			@Override
			public String name() {
				return "Recipes which produce: "+item.title();
			}

			@Override
			public boolean filter(Recipe<?> recipe) {
				return Objects.equals(item, recipe.catalyst());
			}
		};
	}
	
	private ItemEntry findSourceItem() {
		ItemRecord record = craftGUI.inventoryController.getSelectedValue();
		if(record != null) return record.item();
		ItemType type = creativeItemList.getSelectedValue();
		if(type == null) return null;
		return type.create();
	}
	
	public void dispose() {
		timer.stop();
	}
}
