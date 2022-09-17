/**
 * 
 */
package mmb.WORLD.worlds.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeEvent;

import org.joml.Vector2d;
import org.joml.Vector2dc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pploder.events.Event;
import com.rainerhahnekamp.sneakythrow.Sneaky;

import io.vavr.Tuple2;
import mmb.CatchingEvent;
import mmb.GameObject;
import mmb.BEANS.Saver;
import mmb.DATA.contents.Sound;
import mmb.DATA.contents.Sounds;
import mmb.DATA.json.JsonTool;
import mmb.DATA.variables.ListenerBooleanVariable;
import mmb.WORLD.inventory.ItemRecord;
import mmb.WORLD.inventory.storage.ListenableSimpleInventory;
import mmb.WORLD.inventory.storage.SimpleInventory;
import mmb.WORLD.items.ItemEntry;
import mmb.WORLD.worlds.player.PlayerPhysics;
import mmb.WORLD.worlds.player.PlayerPhysicsNormal;
import mmb.debug.Debugger;

/**
 * @author oskar
 * A {@code Player} is an object, which represents player data
 */
public class Player implements GameObject, Saver<JsonNode> {
	@Nonnull private Debugger debug = new Debugger("PLAYERS");
	@Nonnull private static final Debugger sdebug = new Debugger("PLAYERS");
	
	@Override
	public String id() {
		return "Player";
	}
	@Override
	public String getUTID() {
		return "Player";
	}
	@Override
	public void destroy() {
		// unused
	}
	@Override
	public GameObject getOwner() {
		return null;
	}
	
	/**
	 * Creates a new player object
	 */
	public Player(World w) {
		playerHP.addChangeListener(this::changeHP);
		smack = new CatchingEvent<>(debug, "Failed to process smack event");
		smack.addListener(this::smack);
		Sound sound = Sounds.getSound("377157__pfranzen__smashing-head-on-wall.ogg");
		Sneaky.sneaked(sound::open).accept(clip);
		Sound death = Sounds.getSound("220203__gameaudio__casual-death-loose.wav");
		Sneaky.sneaked(death::open).accept(deathClip);
		world = w;
	}
	
	@Nonnull public final ListenableSimpleInventory inv = new ListenableSimpleInventory(debug);
	@Nonnull public final ListenerBooleanVariable creative = new ListenerBooleanVariable();
	@Nonnull public final World world;
	public boolean isCreative() {
		return creative.getValue();
	}
	public boolean isSurvival() {
		return !isCreative();
	}
	public void setCreative(boolean value) {
		creative.setValue(value);
	}

	public static final Event<Tuple2<Player, ObjectNode>> onPlayerSaved
	= new CatchingEvent<>(sdebug, "Failed to save mod player data");
	public static final Event<Tuple2<Player, ObjectNode>> onPlayerLoaded
	= new CatchingEvent<>(sdebug, "Failed to load mod player data");
	
	@Override
	public void load(@Nullable JsonNode data) {
		if(data == null) return;
		ObjectNode on = (ObjectNode) data;
		
		JsonNode nodeCreative = on.get("creative");
		if(nodeCreative != null) creative.setValue(nodeCreative.asBoolean(true));
		
		inv.load(JsonTool.requestArray("inventory", on));
		inv.setCapacity(128);
		
		JsonNode posX = on.get("x");
		if(posX != null) pos.x = posX.asDouble();
		JsonNode posY = on.get("y");
		if(posY != null) pos.y = posY.asDouble();
		
		JsonNode alc1 = on.get("alcohol1");
		if(alc1 != null) digestibleAlcohol = alc1.asDouble();
		if(digestibleAlcohol < 0) {
			debug.printl("Inavlid digestible alcohol, expected >=0, got "+digestibleAlcohol);
			digestibleAlcohol = 0;
		}
		JsonNode alc2 = on.get("alcohol2");
		if(alc2 != null) BAC = alc2.asDouble();
		if(BAC < 0) {
			debug.printl("Inavlid BAC, expected >=0, got "+BAC);
			BAC = 0;
		}
		
		onPlayerLoaded.trigger(new Tuple2<>(this, on));
		debug = new Debugger("PLAYERS/"+world.getName());
	}
	@Override
	public JsonNode save() {
		ObjectNode result = JsonTool.newObjectNode();
		onPlayerSaved.trigger(new Tuple2<>(this, result));
		result.set("inventory", inv.save());
		result.put("creative", creative.getValue());
		result.put("x", pos.x);
		result.put("y", pos.y);
		result.put("alcohol1", digestibleAlcohol);
		result.put("alcohol2", BAC);
		return result;
	}
	
	private Clip clip = Sneaky.sneak(AudioSystem::getClip);
	private Clip deathClip = Sneaky.sneak(AudioSystem::getClip);
	//Player physics
	/**
	 * The center position of the player
	 */
	@Nonnull public final Vector2d pos = new Vector2d();
	@Nonnull private final Vector2d speedTrue0 = new Vector2d();
	@Nonnull public final Vector2dc speedTrue = speedTrue0;
	@Nonnull public final Vector2d speed = new Vector2d();
	@Nonnull public PlayerPhysics physics = new PlayerPhysicsNormal();
	void onTick(World world) {
		alcohol();
		blink();
		if(!(Double.isFinite(pos.x) && Double.isFinite(pos.y))){
			physics = new PlayerPhysicsNormal();
			pos.set(0, 0);
		}
		Vector2d posOld = new Vector2d(pos);
		speedTrue0.set(pos);
		
		//Input controls
		double ctrlX=controls.x+jitter.x;
		double ctrlY=controls.y+jitter.y;
		
		//Handle the physics model
		Vector2d oldSpeed = new Vector2d(speed);
		physics.onTick(world, this, ctrlX, ctrlY);
		double speedDiff = oldSpeed.distance(speed);
		
		//Play head smacked sound when deccelrating more than 36 km/h in one tick
		if(speedDiff > 10) {
			clip.setFramePosition(0);
			clip.start();
			smack.trigger(speedDiff);
		}
		
		speedTrue0.sub(pos).mul(-50); //calculate true speed
		
		//reject infinite positions
		if(Double.isFinite(pos.x) && Double.isFinite(pos.y)) return;
		physics = new PlayerPhysicsNormal();
		pos.set(posOld);
	}
	
	public final Vector2d controls = new Vector2d();
	public final Vector2d jitter = new Vector2d();

	/**
	 * Sets the control inputs
	 * @param u
	 * @param d
	 * @param l
	 * @param r
	 */
	public void setControls(boolean u, boolean d, boolean l, boolean r) {
		controls.set(0);
		if(u) controls.y--;
		if(d) controls.y++;
		if(l) controls.x--;
		if(r) controls.x++;
	}

	//Everything about alcohol
	private double digestibleAlcohol;
	/** @return the digestibleAlcohol amount of digestible alcohol remaining */
	public double getDigestibleAlcohol() {
		return digestibleAlcohol;
	}
	/** @param digestibleAlcohol set amount of alcohol that can be digested */
	public void setDigestibleAlcohol(double digestibleAlcohol) {
		this.digestibleAlcohol = digestibleAlcohol;
	}
	/**
	 * Adds alcohol to the player
	 * @param alcohol amount of alcohol
	 */
	public void drinkAlcohol(double alcohol) {
		digestibleAlcohol += alcohol;
	}
	private double BAC;
	/**  @return the BAC */
	public double getBAC() {
		return BAC;
	}
	/** @param BAC the BAC to set */
	public void setBAC(double BAC) {
		this.BAC = BAC;
	}
	private void alcohol() {
		double rate = 0.002 + (digestibleAlcohol * 0.0002);
		//Absorb the alcohol (0.1u/s)
		if(digestibleAlcohol < rate) {
			BAC += digestibleAlcohol;
			digestibleAlcohol = 0;
		}else {
			BAC += rate;
			digestibleAlcohol -= rate;
		}
		
		//Apply effects of alcohol
		//jitter
		double jitterscale = BAC*BAC / 10;
		double angle = 2*Math.PI*Math.random();
		double magnitude = jitterscale*Math.random();
		jitter.set(magnitude*Math.sin(angle), magnitude*Math.cos(angle));
		
		//blink speed
		if(BAC < 0) {
			BAC = 0;
			blinkspeed = 1;
		}else if(BAC < 4) {
			double ex = BAC-2;
			blinkspeed = 3-0.5*ex*ex;
		}else if(BAC < 4.5) {
			blinkspeed = 9-BAC*2;
		}else {
			blinkspeed = 0;
		}
		
		//loss of control
		if(BAC > 10)
			controls.set(0);
		
		//damage by poisoning
		if(BAC > 13) 
			hurt((int)((BAC-13)*5000));
		
		//death by overdose
		if(BAC > 15) playerHP.setValue(-1);
		
		//Metabolize alcohol (0.02u/s)
		if(BAC < 0.0004) {
			BAC = 0;
		}else {
			BAC -= 0.0004;
		}
	}
	
	//Blinking
	private double blinkspeed = 0;
	private double blinkcycle = 0;
	private int blink = 0;
	private void blink() {
		//Blink cycle
		if(blink == 6) blink = 0;
		if(blink >= 1) 
			blink++;
		
		//Blink speed
		blinkcycle += blinkspeed/50;
		if(blinkcycle >= 1) {
			blinkcycle -= 1;
			blink = 1;
		}
	}

	/** @return current blink speed */
	public double getBlinkspeed() {
		return blinkspeed;
	}
	/** @return position of the blink cycle */
	public double getBlinkcycle() {
		return blinkcycle;
	}
	/** @return position of blink animation */
	public int getBlink() {
		return blink;
	}

	//HP and death
	/**
	 * The range model for HP
	 */
	public final BoundedRangeModel playerHP = new DefaultBoundedRangeModel(10000000, 0, 0, 10000000);
	/**
	 * Subtracts given amount from the HP
	 * @param amount amount of damage
	 */
	public void hurt(int amount) {
		playerHP.setValue(playerHP.getValue() - amount);
	}
	/**
	 * Triggered when player "smacks" into something
	 */
	public final Event<Double> smack;
	private void changeHP(ChangeEvent e) {
		if(playerHP.getValue() <= 0) death();
	}
	private void death() {
		//drop all items
		int x = (int)pos.x;
		int y = (int)pos.y;
		for(ItemRecord irecord: inv) {
			ItemEntry item = irecord.item();
			int extract = irecord.extract(Integer.MAX_VALUE);
			for(int i=0; i < extract; i++) {
				world.dropItem(item, x, y);
			}
		}
		
		//reset certain values
		pos.set(0);
		speed.set(0);
		BAC = 0;
		digestibleAlcohol = 0;
		playerHP.setValue(10000000);
		
		//play death sound
		deathClip.setFramePosition(0);
		deathClip.start();
		
		//log it
		debug.printl("DEATH");
	}
	private void smack(Double speed) {
		double speed0 = speed.doubleValue();
		hurt((int)(speed0*speed0*5000));
	}
}
