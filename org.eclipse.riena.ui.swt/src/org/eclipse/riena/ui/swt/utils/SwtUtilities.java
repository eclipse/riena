/*******************************************************************************
 * Copyright (c) 2007, 2014 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.ui.swt.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.text.MessageFormat;
import java.util.Map;

import org.osgi.service.log.LogService;

import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.log.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.riena.core.Log4r;
import org.eclipse.riena.core.cache.LRUHashMap;
import org.eclipse.riena.internal.ui.swt.Activator;
import org.eclipse.riena.internal.ui.swt.utils.RcpUtilities;
import org.eclipse.riena.ui.swt.facades.GCFacade;
import org.eclipse.riena.ui.swt.lnf.LnfManager;

/**
 * A collection of utility methods for SWT.
 */
public final class SwtUtilities {

	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), SwtUtilities.class);
	private static final String THREE_DOTS = "..."; //$NON-NLS-1$
	private static final Map<GCString, Integer> TEXT_WIDTH_CACHE = LRUHashMap.createLRUHashMap(2048);
	private static final Map<GCChar, Integer> CHAR_WIDTH_CACHE = LRUHashMap.createLRUHashMap(1024);
	private final static float DEFAULT_DPI_X = 96.0f;
	private final static float DEFAULT_DPI_Y = 96.0f;
	private final static Point DEFAULT_DPI = new Point(Math.round(DEFAULT_DPI_X), Math.round(DEFAULT_DPI_Y));
	private static float[] cachedDpiFactors = new float[] { 0.0f, 0.0f };
	private static Point cachedDpi = null;

	/**
	 * This class contains only static methods. So it is not necessary to create an instance.
	 */
	private SwtUtilities() {
		throw new Error("SwtUtilities is just a container for static methods"); //$NON-NLS-1$
	}

	/**
	 * The text will be clipped, if the width of the given text is greater than the maximum width.<br>
	 * The clipped text always ends with three dots ("...").
	 * 
	 * @param gc
	 *            graphics context
	 * @param text
	 *            text
	 * @param maxWidth
	 *            maximum of the text
	 * @return truncated text
	 */
	public static String clipText(final GC gc, final String text, final int maxWidth) {
		int textwidth = calcTextWidth(gc, text);
		if (textwidth <= maxWidth) {
			return text;
		}
		final int threeDotsWidth = calcTextWidth(gc, THREE_DOTS);
		final StringBuilder shortenedText = new StringBuilder(text);
		while (textwidth + threeDotsWidth > maxWidth && shortenedText.length() != 0) {
			shortenedText.setLength(shortenedText.length() - 1);
			textwidth = calcTextWidth(gc, shortenedText);
		}
		shortenedText.append(THREE_DOTS);
		return shortenedText.toString();
	}

	/**
	 * Calculates the width of the given text based on the current settings of the given graphics context.
	 * 
	 * @param gc
	 *            graphics context
	 * @param text
	 *            text
	 * @return width of text
	 */
	public static int calcTextWidth(final GC gc, final CharSequence text) {
		if (text == null) {
			return 0;
		}
		final GCString lookupKey = new GCString(gc, text);
		Integer width = TEXT_WIDTH_CACHE.get(lookupKey);
		if (width == null) {
			int w = 0;
			for (int i = 0; i < text.length(); i++) {
				w += calcCharWidth(gc, text.charAt(i));
			}
			width = w;
			TEXT_WIDTH_CACHE.put(lookupKey, width);
		}
		return width;
	}

	/**
	 * Calculates the width of the given char based on the current settings of the given graphics context.
	 * 
	 * @param gc
	 *            graphics context
	 * @param ch
	 *            character
	 * @return width of character
	 */
	public static int calcCharWidth(final GC gc, final char ch) {
		final GCChar lookupKey = new GCChar(gc, ch);
		Integer width = CHAR_WIDTH_CACHE.get(lookupKey);
		if (width == null) {
			width = GCFacade.getDefault().getAdvanceWidth(gc, ch);
			CHAR_WIDTH_CACHE.put(lookupKey, width);
		}
		return width;
	}

	/**
	 * Disposes the given resource, if the resource is not null and isn't already disposed.
	 * 
	 * @param resource
	 *            resource to dispose
	 * 
	 * @since 3.0
	 */
	public static void dispose(final Resource resource) {
		if (!isDisposed(resource)) {
			resource.dispose();
		}
	}

	/**
	 * Disposes the given widget, if the widget is not {@code null} and isn't already disposed.
	 * 
	 * @param widget
	 *            widget to dispose
	 * 
	 * @since 3.0
	 */
	public static void dispose(final Widget widget) {
		if (!isDisposed(widget)) {
			widget.dispose();
		}
	}

	/**
	 * Returns the 0 based index of the column at {@code pt}. The code can handle re-ordered columns. The index refers to the original ordering (as used by SWT
	 * API).
	 * <p>
	 * Will return -1 if no column could be computed -- this is the case when all columns are resized to have width 0.
	 * 
	 * @since 5.0
	 */
	public static int findColumn(final Tree tree, final Point pt) {
		int width = 0;
		final int[] colOrder = tree.getColumnOrder();
		// compute the current column ordering
		final TreeColumn[] columns = new TreeColumn[colOrder.length];
		for (int i = 0; i < colOrder.length; i++) {
			final int idx = colOrder[i];
			columns[i] = tree.getColumn(idx);
		}
		// find the column under Point pt
		for (final TreeColumn col : columns) {
			final int colWidth = col.getWidth();
			if (width < pt.x && pt.x < width + colWidth) {
				return tree.indexOf(col);
			}
			width += colWidth;
		}
		return -1;
	}

	/**
	 * Returns the 0 based index of the column at {@code pt}. The code can handle re-ordered columns. The index refers to the original ordering (as used by SWT
	 * API).
	 * <p>
	 * Will return -1 if no column could be computed -- this is the case when all columns are resized to have width 0.
	 * 
	 * @since 3.0
	 */
	public static int findColumn(final Table table, final Point pt) {
		int width = 0;
		final int[] colOrder = table.getColumnOrder();
		// compute the current column ordering
		final TableColumn[] columns = new TableColumn[colOrder.length];
		for (int i = 0; i < colOrder.length; i++) {
			final int idx = colOrder[i];
			columns[i] = table.getColumn(idx);
		}
		// find the column under Point pt
		for (final TableColumn col : columns) {
			final int colWidth = col.getWidth();
			if (width < pt.x && pt.x < width + colWidth) {
				return table.indexOf(col);
			}
			width += colWidth;
		}
		return -1;
	}

	/**
	 * Returns true if the given {@code styleBit} is turned on, on the given {@code widget} instance. Returns false otherwise.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * SwtUtilities.hasStyle(button, SWT.CHECK);
	 * </pre>
	 * 
	 * @param widget
	 *            a Widget instance; may be null.
	 * @param styleBit
	 *            an SWT style bit value
	 * @since 3.0
	 */
	public static boolean hasStyle(final Widget widget, final int styleBit) {
		return widget == null ? false : (widget.getStyle() & styleBit) == styleBit;
	}

	/**
	 * Returns {@code true}, if the given widget is disposed or {@code null}.
	 * 
	 * @param widget
	 *            widget to check
	 * @return {@code true}, if the widget is disposed or {@code null}; otherwise {@code false}.
	 */
	public static boolean isDisposed(final Widget widget) {
		return widget == null || widget.isDisposed();
	}

	/**
	 * Returns {@code true}, if the given resource is disposed or {@code null}.
	 * 
	 * @param resource
	 *            resource to check
	 * @return {@code true}, if the resource is disposed or {@code null}; otherwise {@code false}.
	 */
	public static boolean isDisposed(final Resource resource) {
		return !((resource != null) && (!resource.isDisposed()));
	}

	/**
	 * Returns a point whose x coordinate is the horizontal dots per inch of the (current or default) display, and whose y coordinate is the vertical dots per
	 * inch of the display.
	 * 
	 * @return the horizontal and vertical DPI
	 * @since 6.0
	 */
	public static Point getDpi() {
		return getDpi(null);
	}

	/**
	 * @since 6.0
	 */
	public static Point getDefaultDpi() {
		return DEFAULT_DPI;
	}

	/**
	 * Returns a point whose x coordinate is the horizontal dots per inch of the display, and whose y coordinate is the vertical dots per inch of the display.
	 * 
	 * @param widget
	 *            if widget is not {@code null} return DPI of the display that's associated with the widget; otherwise return the DPI of the current or default
	 *            display
	 * @return the horizontal and vertical DPI
	 * @since 6.0
	 */
	public static Point getDpi(final Widget widget) {

		if (cachedDpi == null) {
			Display display = null;
			if (widget != null) {
				display = widget.getDisplay();
			}
			if (display == null) {
				display = getDisplay();
			}
			Assert.isNotNull(display, "No display exits"); //$NON-NLS-1$
			final Display d = display;
			display.syncExec(new Runnable() {
				public void run() {
					cachedDpi = d.getDPI();
				}
			});
		}

		return cachedDpi;

	}

	/**
	 * Returns whether scaling is enabled or disabled
	 * 
	 * @return {@code true} scaling is enabled; otherwise {@code false}
	 * @since 6.0
	 */
	public static boolean isDpiScalingEnabled() {
		return isDpiScalingEnabled(null);
	}

	/**
	 * Returns whether scaling is enabled or disabled
	 * 
	 * @param widget
	 *            if widget is not {@code null} the display that's associated with the widget is used; otherwise the current or default display
	 * @return {@code true} scaling is enabled; otherwise scaling disabled
	 * @since 6.0
	 */
	public static boolean isDpiScalingEnabled(final Widget widget) {
		final float[] dpiFactors = getDpiFactors(widget);
		for (final float factor : dpiFactors) {
			if (factor > 1.0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the Display.
	 * 
	 * @return instance of Display or null if there is no display
	 * @since 6.0
	 */
	public static Display getDisplay() {

		Display display = null;
		if (display == null) {
			try {
				display = RcpUtilities.getDisplay();
			} catch (final RuntimeException e) {
				display = null;
			}
		}
		if (display == null) {
			display = Display.getCurrent();
		}
		if (display == null) {
			display = Display.getDefault();
		}

		return display;

	}

	/**
	 * Returns the scaling factors which can be used to scale pixel values the same amount as the current DPI differs from the standard DPI (on Windows: 96
	 * DPI).
	 * 
	 * @return x-factor and y-factors
	 * @since 6.0
	 */
	public static float[] getDpiFactors() {
		return getDpiFactors(null);
	}

	/**
	 * Returns the scaling factors which can be used to scale pixel values the same amount as the current DPI differs from the standard DPI (on Windows: 96
	 * DPI).
	 * <p>
	 * The Riena Look&Feel can manipulate the scaling factors.
	 * 
	 * @param widget
	 *            if widget is not {@code null} the display that's associated with the widget is used; otherwise the current or default display
	 * 
	 * @return x-factor and y-factors
	 * @since 6.0
	 */
	public static float[] getDpiFactors(final Widget widget) {
		if ((cachedDpiFactors[0] <= 0.001f) || (cachedDpiFactors[1] <= 0.001f)) {
			final Point dpi = getDpi(widget);
			final float[] lnfDpiFactors = LnfManager.getLnf().getDpiFactors(dpi);
			if (lnfDpiFactors.length == 2) {
				cachedDpiFactors[0] = lnfDpiFactors[0];
				cachedDpiFactors[1] = lnfDpiFactors[1];
			}
		}
		if ((cachedDpiFactors[0] <= 0.001f) || (cachedDpiFactors[1] <= 0.001f)) {
			final float[] factors = getCalculatedDpiFactors(widget);
			cachedDpiFactors[0] = factors[0];
			cachedDpiFactors[1] = factors[1];
		}
		return cachedDpiFactors;
	}

	/**
	 * Returns the scaling factors which can be used to scale pixel values the same amount as the current DPI differs from the standard DPI (on Windows: 96
	 * DPI).
	 * 
	 * @return x-factor and y-factors
	 * @since 6.0
	 */
	public static float[] getCalculatedDpiFactors() {
		return getCalculatedDpiFactors(null);
	}

	/**
	 * Returns the scaling factors which can be used to scale pixel values the same amount as the current DPI differs from the standard DPI (on Windows: 96
	 * DPI).
	 * 
	 * @return x-factor and y-factors
	 * @since 6.0
	 */
	public static float[] getCalculatedDpiFactors(final Widget widget) {
		final Point dpi = getDpi(widget);
		final float[] factors = new float[2];
		factors[0] = dpi.x / DEFAULT_DPI_X;
		factors[1] = dpi.y / DEFAULT_DPI_Y;
		return factors;
	}

	/**
	 * Scales the given value in X-direction.
	 * <p>
	 * Rounds to nearest int value.
	 * 
	 * @param x
	 *            value to scale
	 * @return scaled value
	 * @since 6.0
	 */
	public static int convertXToDpi(final int x) {
		final float factorX = getDpiFactors()[0];
		return convertPixelToDpi(x, factorX);
	}

	/**
	 * Scales the given value in Y-direction.
	 * <p>
	 * Rounds to nearest int value.
	 * 
	 * @param y
	 *            value to scale
	 * @return scaled value
	 * @since 6.0
	 */
	public static int convertYToDpi(final int y) {
		final float factorY = getDpiFactors()[1];
		return convertPixelToDpi(y, factorY);
	}

	/**
	 * @since 6.0
	 */
	public static Point convertPointToDpi(final Point pixelPoint) {
		final int x = convertXToDpi(pixelPoint.x);
		final int y = convertYToDpi(pixelPoint.y);
		return new Point(x, y);
	}

	/**
	 * @since 6.0
	 */
	public static int convertPixelToDpi(final int pixel) {
		final float factor = Math.min(getDpiFactors()[0], getDpiFactors()[1]);
		return convertPixelToDpi(pixel, factor);
	}

	private static int convertPixelToDpi(final int px, final float factor) {
		if (px < 0) {
			return -(int) (-px * factor + 0.5);
		}
		return (int) (px * factor + 0.5);
	}

	/**
	 * Scales the given value in X-direction.
	 * <p>
	 * Truncates the decimal portion.
	 * 
	 * @param y
	 *            value to scale
	 * @return scaled value
	 * @since 6.0
	 */
	public static int convertXToDpiTruncate(final int x) {
		final float factorX = getDpiFactors()[0];
		return convertPixelToDpiTruncate(x, factorX);
	}

	/**
	 * Scales the given value in Y-direction.
	 * <p>
	 * Truncates the decimal portion.
	 * 
	 * @param y
	 *            value to scale
	 * @return scaled value
	 * @since 6.0
	 */
	public static int convertYToDpiTruncate(final int y) {
		final float factorY = getDpiFactors()[1];
		return convertPixelToDpiTruncate(y, factorY);
	}

	/**
	 * @since 6.0
	 */
	public static int convertPixelToDpiTruncate(final int pixel) {
		final float factor = Math.min(getDpiFactors()[0], getDpiFactors()[1]);
		return convertPixelToDpiTruncate(pixel, factor);
	}

	private static int convertPixelToDpiTruncate(final int px, final float factor) {
		return (int) (px * factor);
	}

	/**
	 * Creates a new instance of <code>Color</code> that is a brighter version of the given color.
	 * 
	 * @param color
	 *            the color to make brighter; never null
	 * @param f
	 *            the factor.
	 * @return a new <code>Color</code> object that is a brighter version of this given color.
	 */
	public static Color makeBrighter(final Color color, final float f) {

		Assert.isNotNull(color);
		Assert.isTrue(f >= 0.0);

		final float[] hsb = color.getRGB().getHSB();
		final float h = hsb[0];
		final float s = hsb[1];
		float b = hsb[2];

		b = b * f;
		if (b > 1.0f) {
			b = 1.0f;
		}

		final RGB rgb = new RGB(h, s, b);

		return new Color(color.getDevice(), rgb);
	}

	/**
	 * Converts the given AWT image to a SWT imgae ({@code ImageData}).
	 * <p>
	 * <i>Copy of org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet156.java</i>
	 * 
	 * @param bufferedImage
	 *            AWT image
	 * @return SWT image or {@code null} if conversion isn't possible
	 * @since 6.0
	 */
	public static ImageData convertAwtImageToImageData(final BufferedImage bufferedImage) {

		if (bufferedImage.getColorModel() instanceof DirectColorModel) {

			final DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			final PaletteData palette = convertColorModelToPalette(colorModel);
			final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					final int rgb = bufferedImage.getRGB(x, y);
					final int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
					data.setPixel(x, y, pixel);
					if (colorModel.hasAlpha()) {
						data.setAlpha(x, y, (rgb >> 24) & 0xFF);
					}
				}
			}
			return data;

		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {

			final IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
			final PaletteData palette = convertColorModelToPalette(colorModel);
			final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			final WritableRaster raster = bufferedImage.getRaster();
			final int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;

		} else if (bufferedImage.getColorModel() instanceof ComponentColorModel && bufferedImage.getType() == BufferedImage.TYPE_3BYTE_BGR) {

			final ComponentColorModel colorModel = (ComponentColorModel) bufferedImage.getColorModel();
			final PaletteData palette = convertColorModelToPalette(colorModel);
			final WritableRaster raster = bufferedImage.getRaster();
			final int scanlinePad = colorModel.getColorSpace().getNumComponents();
			final byte[] pixels = ((DataBufferByte) raster.getDataBuffer()).getData();
			final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette, scanlinePad, pixels);
			return data;

		} else {

			if (bufferedImage.getColorModel() != null) {
				final String colorModelName = bufferedImage.getColorModel().getClass().getSimpleName();
				final String msg = MessageFormat.format("The color model {0} is not supported!", colorModelName); //$NON-NLS-1$
				LOGGER.log(LogService.LOG_ERROR, msg);
			} else {
				LOGGER.log(LogService.LOG_ERROR, "No color model!"); //$NON-NLS-1$
			}

		}

		return null;
	}

	/**
	 * Converts the given color model of AWT to a SWT palette.
	 * 
	 * @param colorModel
	 *            AWT color model
	 * @return SWT palette
	 */
	private static PaletteData convertColorModelToPalette(final DirectColorModel colorModel) {
		return new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
	}

	/**
	 * Converts the given color model of AWT to a SWT palette.
	 * 
	 * @param colorModel
	 *            AWT color model
	 * @return SWT palette
	 */
	private static PaletteData convertColorModelToPalette(final ComponentColorModel colorModel) {
		return new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
	}

	/**
	 * Converts the given color model of AWT to a SWT palette.
	 * 
	 * @param colorModel
	 *            AWT color model
	 * @return SWT palette
	 */
	private static PaletteData convertColorModelToPalette(final IndexColorModel colorModel) {

		final int size = colorModel.getMapSize();
		final byte[] reds = new byte[size];
		final byte[] greens = new byte[size];
		final byte[] blues = new byte[size];
		colorModel.getReds(reds);
		colorModel.getGreens(greens);
		colorModel.getBlues(blues);
		final RGB[] rgbs = new RGB[size];
		for (int i = 0; i < rgbs.length; i++) {
			rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
		}

		final PaletteData palette = new PaletteData(rgbs);
		return palette;

	}

	private final static class GCChar {
		private final char ch;
		private final Font font;

		private GCChar(final GC gc, final char ch) {
			this.font = gc.getFont();
			this.ch = ch;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ch;
			result = prime * result + ((font == null) ? 0 : font.hashCode());
			return result;
		}

		// Note: There is no type check here because we do not mix types in the map!! (performance) 
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			final GCChar other = (GCChar) obj;
			if (ch != other.ch) {
				return false;
			}
			if (font == null) {
				if (other.font != null) {
					return false;
				}
			} else if (!font.equals(other.font)) {
				return false;
			}
			return true;
		}

	}

	private final static class GCString {
		private final String text;
		private final Font font;

		private GCString(final GC gc, final CharSequence seq) {
			this.font = gc.getFont();
			this.text = seq.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((font == null) ? 0 : font.hashCode());
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}

		// Note: There is no type check here because we do not mix types in the map!! (performance) 
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			final GCString other = (GCString) obj;
			if (font == null) {
				if (other.font != null) {
					return false;
				}
			} else if (!font.equals(other.font)) {
				return false;
			}
			if (text == null) {
				if (other.text != null) {
					return false;
				}
			} else if (!text.equals(other.text)) {
				return false;
			}
			return true;
		}

	}

}
