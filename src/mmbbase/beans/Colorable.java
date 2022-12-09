/**
 * 
 */
package mmbbase.beans;

import java.awt.Color;

import javax.annotation.Nonnull;

import mmbbase.data.variables.Variable;

/**
 * @author oskar
 * Used for objects which have a color
 */
public interface Colorable {
	/**
	 * @return current color
	 */
	@Nonnull public Color getColor();
	/**
	 * Sets the color of the object
	 * @param c new color
	 */
	public void setColor(Color c);
	/**
	 * @return this object's color as a variable object
	 */
	@Nonnull public default Variable<Color> getColorVariable(){
		return Variable.delegate(this::getColor, this::setColor);
	}
}