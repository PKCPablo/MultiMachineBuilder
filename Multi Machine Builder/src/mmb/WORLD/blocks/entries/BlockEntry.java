/**
 * 
 */
package mmb.WORLD.blocks.entries;

import com.google.gson.JsonElement;

import mmb.WORLD.blocks.defs.BlockDef;

/**
 * @author oskar
 *
 */
public interface BlockEntry {
	public JsonElement save(JsonElement e);
	public BlockDef getBlock();
}
