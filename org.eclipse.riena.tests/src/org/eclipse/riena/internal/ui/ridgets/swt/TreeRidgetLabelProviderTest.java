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
package org.eclipse.riena.internal.ui.ridgets.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.riena.beans.common.WordNode;
import org.eclipse.riena.core.test.collect.UITestCase;
import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.ui.ridgets.IColumnFormatter;
import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;

/**
 * Tests for the class {@link TreeRidgetLabelProvider}.
 */
@UITestCase
public class TreeRidgetLabelProviderTest extends TestCase {

	private final static String ICON_ECLIPSE = "eclipse.gif";
	private static final String[] COLUMN_PROPERTIES = { "word", "upperCase" };

	private Shell shell;
	private TreeViewer viewer;
	private TreeRidgetLabelProvider labelProvider;
	private WordNode node;
	private WordNode leaf;
	private WordNode alpha;
	private Color colorA;
	private Color colorB;
	private Font fontA;
	private Font fontB;
	private IColumnFormatter[] formatters;
	private IColumnFormatter[] noFormatters;

	@Override
	protected void setUp() throws Exception {
		final Display display = Display.getDefault();
		final Realm realm = SWTObservables.getRealm(display);
		ReflectionUtils.invokeHidden(realm, "setDefault", realm);
		colorA = display.getSystemColor(SWT.COLOR_RED);
		colorB = display.getSystemColor(SWT.COLOR_GREEN);
		fontA = new Font(display, "Arial", 12, SWT.NORMAL);
		fontB = new Font(display, "Courier", 12, SWT.NORMAL);

		shell = new Shell(display);
		viewer = new TreeViewer(createTree(shell));

		final IObservableSet elements = createElements();
		formatters = new IColumnFormatter[] { new TestColumnFormatter(), null };
		noFormatters = new IColumnFormatter[COLUMN_PROPERTIES.length];
		labelProvider = TreeRidgetLabelProvider.createLabelProvider(viewer, WordNode.class, elements,
				COLUMN_PROPERTIES, null, null, null, noFormatters);

		viewer.setContentProvider(new FTTreeContentProvider());
		viewer.setLabelProvider(labelProvider);
		viewer.setInput(elements.toArray());
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		fontA.dispose();
		fontB.dispose();
	}

	public void testGetText() {
		assertEquals("Node", labelProvider.getText(node));
		assertEquals("LEAF", labelProvider.getText(leaf));
	}

	@SuppressWarnings("rawtypes")
	public void testGetColumnText() {
		assertEquals("Node", labelProvider.getColumnText(node, 0));
		assertEquals("LEAF", labelProvider.getColumnText(leaf, 0));

		assertEquals("false", labelProvider.getColumnText(node, 1));
		assertEquals("true", labelProvider.getColumnText(leaf, 1));

		assertNull(labelProvider.getColumnText(node, 99));

		ReflectionUtils.invokeHidden(labelProvider, "setFormatters", (Object) new IColumnFormatter[] { null, null });
		ReflectionUtils.setHidden(labelProvider, "attributeMaps", new IObservableMap[] {
				new ObservableMap(new HashMap()), new ObservableMap(new HashMap()) });
		final String[] valuesAccessors = new String[] { "aCount" };
		ReflectionUtils.setHidden(labelProvider, "valueAccessors", valuesAccessors);
		node = new WordNode("AAAaaaa");
		assertEquals("7", labelProvider.getColumnText(node, 0));
	}

	public void testGetImage() {
		viewer.collapseAll();

		final Image siCollapsed = Activator.getSharedImage(SharedImages.IMG_NODE_COLLAPSED);
		assertNotNull(siCollapsed);
		final Image nodeCollapsed = labelProvider.getImage(node);
		assertSame(siCollapsed, nodeCollapsed);

		viewer.expandAll();

		final Image siExpanded = Activator.getSharedImage(SharedImages.IMG_NODE_EXPANDED);
		assertNotNull(siExpanded);
		final Image nodeExpanded = labelProvider.getImage(node);
		assertSame(siExpanded, nodeExpanded);

		viewer.collapseToLevel(node, 1);

		assertSame(siCollapsed, labelProvider.getImage(node));

		viewer.expandToLevel(node, 1);

		assertSame(siExpanded, labelProvider.getImage(node));

		final Image siLeaf = Activator.getSharedImage(SharedImages.IMG_LEAF);
		assertNotNull(siLeaf);
		final Image imgLeaf = labelProvider.getImage(leaf);
		assertSame(siLeaf, imgLeaf);

		// sanity check
		assertNotSame(nodeExpanded, nodeCollapsed);
		assertNotSame(nodeExpanded, imgLeaf);
		assertNotSame(nodeCollapsed, imgLeaf);
	}

	public void testGetImageFromProviderMethod() {
		final WordNodeWithIcon nodeWithIcon = new WordNodeWithIcon("nwi");
		nodeWithIcon.setIcon("closed_16.gif");
		nodeWithIcon.setOpenIcon("open_16.gif");
		final WordNodeWithIcon leafWithIcon = new WordNodeWithIcon(nodeWithIcon, "lwi");
		leafWithIcon.setIcon("eclipse.gif");
		final IObservableSet elements = new WritableSet(Realm.getDefault(), Arrays.asList(new WordNode[] {
				nodeWithIcon, leafWithIcon }), WordNodeWithIcon.class);
		labelProvider = TreeRidgetLabelProvider.createLabelProvider(viewer, WordNodeWithIcon.class, elements,
				COLUMN_PROPERTIES, null, "icon", "openIcon", noFormatters);
		viewer.setLabelProvider(labelProvider);
		viewer.setInput(elements.toArray());

		viewer.collapseAll();

		final Image siCollapsed = Activator.getSharedImage("closed_16.gif");
		assertNotNull(siCollapsed);
		final Image nodeCollapsed = labelProvider.getImage(nodeWithIcon);
		assertSame(siCollapsed, nodeCollapsed);

		viewer.expandAll();

		final Image siExpanded = Activator.getSharedImage("open_16.gif");
		assertNotNull(siExpanded);
		final Image nodeExpanded = labelProvider.getImage(nodeWithIcon);
		assertSame(siExpanded, nodeExpanded);

		final Image siLeaf = Activator.getSharedImage("eclipse.gif");
		assertNotNull(siLeaf);
		final Image imgLeaf = labelProvider.getImage(leafWithIcon);
		assertSame(siLeaf, imgLeaf);

		// sanity check
		assertNotSame(nodeExpanded, nodeCollapsed);
		assertNotSame(nodeExpanded, imgLeaf);
		assertNotSame(nodeCollapsed, imgLeaf);
	}

	public void testGetColumnImage() {
		viewer.collapseAll();

		final Image siCollapsed = Activator.getSharedImage(SharedImages.IMG_NODE_COLLAPSED);
		assertNotNull(siCollapsed);
		assertSame(siCollapsed, labelProvider.getColumnImage(node, 0));

		final Image siLeaf = Activator.getSharedImage(SharedImages.IMG_LEAF);
		assertNotNull(siLeaf);
		assertSame(siLeaf, labelProvider.getColumnImage(leaf, 0));
		assertNotSame(siLeaf, siCollapsed);

		final Image siUnchecked = Activator.getSharedImage(SharedImages.IMG_UNCHECKED);
		assertNotNull(siUnchecked);
		assertEquals(siUnchecked, labelProvider.getColumnImage(node, 1));

		final Image siChecked = Activator.getSharedImage(SharedImages.IMG_CHECKED);
		assertNotNull(siChecked);
		assertEquals(siChecked, labelProvider.getColumnImage(leaf, 1));

		assertNotSame(siChecked, siUnchecked);

		assertNull(labelProvider.getColumnImage(node, 99));
	}

	public void testGetForeground() {
		final IObservableSet elements = createElements();
		final WordNode wordNode = leaf;
		// using upperCase as the enablement accessor; true => enabled; false => disabled
		labelProvider = TreeRidgetLabelProvider.createLabelProvider(viewer, WordNode.class, elements,
				COLUMN_PROPERTIES, "upperCase", null, null, noFormatters);

		wordNode.setUpperCase(true);
		final Color colorEnabled = labelProvider.getForeground(wordNode);
		assertNull(colorEnabled);

		wordNode.setUpperCase(false);
		final Color colorDisabled = labelProvider.getForeground(wordNode);
		assertNotNull(colorDisabled);
	}

	public void testGetColumnTextWithFormatter() {
		ReflectionUtils.invokeHidden(labelProvider, "setFormatters", (Object) formatters);

		assertEquals("no", labelProvider.getColumnText(node, 0));
		assertEquals("yes", labelProvider.getColumnText(leaf, 0));
		assertEquals("Alpha", labelProvider.getColumnText(alpha, 0));

		assertEquals("false", labelProvider.getColumnText(node, 1));
		assertEquals("true", labelProvider.getColumnText(leaf, 1));
		assertEquals("false", labelProvider.getColumnText(alpha, 1));

		assertNull(labelProvider.getColumnText(node, 99));
	}

	public void testGetColumnImageWithFormatter() {
		final IColumnFormatter[] formatters2 = new IColumnFormatter[] { new TestColumnFormatter(),
				new TestColumnFormatter() };
		ReflectionUtils.invokeHidden(labelProvider, "setFormatters", (Object) formatters2);
		final Image siNode = Activator.getSharedImage(SharedImages.IMG_NODE_COLLAPSED);
		final Image siLeaf = Activator.getSharedImage(SharedImages.IMG_LEAF);
		final Image siError = Activator.getSharedImage(SharedImages.IMG_ERROR_DECO);

		assertSame(siNode, labelProvider.getColumnImage(node, 0));
		assertSame(siError, labelProvider.getColumnImage(leaf, 0));
		assertSame(siLeaf, labelProvider.getColumnImage(alpha, 0));

		final Image siUnchecked = Activator.getSharedImage(SharedImages.IMG_UNCHECKED);
		assertSame(siUnchecked, labelProvider.getColumnImage(alpha, 1));

		assertNull(labelProvider.getColumnImage(node, 99));
	}

	/**
	 * As per Bug 316103
	 */
	public void testUpdateColumnImageWithFormatter() {
		final Image image = ImageDescriptor.getMissingImageDescriptor().createImage(); // dummy image
		try {
			final ColumnFormatter formatter = new ColumnFormatter() {
				@Override
				public Image getImage(final Object element) {
					return image;
				}
			};
			final IColumnFormatter[] formatters = new IColumnFormatter[] { formatter, null };
			final TreeRidgetLabelProvider labelProvider = TreeRidgetLabelProvider.createLabelProvider(viewer,
					WordNode.class, createElements(), COLUMN_PROPERTIES, null, null, null, formatters);

			final TreeItem treeItem = new TreeItem(viewer.getTree(), SWT.NONE);
			new TreeItem(treeItem, SWT.NONE);

			assertTrue(treeItem.getItemCount() > 0); // has children
			assertNull(treeItem.getImage());

			ReflectionUtils.invokeHidden(labelProvider, "updateNodeImage", treeItem, true);

			assertSame(image, treeItem.getImage());

			ReflectionUtils.invokeHidden(labelProvider, "updateNodeImage", treeItem, false);

			assertSame(image, treeItem.getImage());
		} finally {
			image.dispose();
		}
	}

	public void testGetForegroundWithFormatter() {
		ReflectionUtils.invokeHidden(labelProvider, "setFormatters", (Object) formatters);

		assertSame(colorA, labelProvider.getForeground(node, 0));
		assertSame(colorB, labelProvider.getForeground(leaf, 0));
		assertNull(labelProvider.getForeground(alpha, 0));

		assertNull(labelProvider.getForeground(node, 1));
		assertNull(labelProvider.getForeground(leaf, 1));
		assertNull(labelProvider.getForeground(alpha, 1));

		assertNull(labelProvider.getForeground(node, 99));
	}

	public void testGetBackgroundWithFormatter() {
		ReflectionUtils.invokeHidden(labelProvider, "setFormatters", (Object) formatters);

		assertSame(colorA, labelProvider.getBackground(node, 0));
		assertSame(colorB, labelProvider.getBackground(leaf, 0));
		assertNull(labelProvider.getBackground(alpha, 0));

		assertNull(labelProvider.getBackground(node, 1));
		assertNull(labelProvider.getBackground(leaf, 1));
		assertNull(labelProvider.getBackground(alpha, 1));

		assertNull(labelProvider.getBackground(node, 99));
	}

	public void testGetFontWithFormatter() {
		ReflectionUtils.invokeHidden(labelProvider, "setFormatters", (Object) formatters);

		assertSame(fontA, labelProvider.getFont(node, 0));
		assertSame(fontB, labelProvider.getFont(leaf, 0));
		assertNull(labelProvider.getFont(alpha, 0));

		assertNull(labelProvider.getFont(node, 1));
		assertNull(labelProvider.getFont(leaf, 1));
		assertNull(labelProvider.getFont(alpha, 1));

		assertNull(labelProvider.getFont(node, 99));
	}

	// helping methods
	// ////////////////

	private IObservableSet createElements() {
		final Collection<WordNode> collection = new ArrayList<WordNode>();
		node = new WordNode("Node");
		alpha = new WordNode(node, "Alpha");
		leaf = new WordNode("Leaf");
		leaf.setUpperCase(true);
		collection.add(node);
		collection.add(leaf);
		collection.add(alpha);
		final IObservableSet elements = new WritableSet(Realm.getDefault(), collection, WordNode.class);
		return elements;
	}

	private Tree createTree(final Shell shell) {
		shell.setLayout(new FillLayout());
		final Tree result = new Tree(shell, SWT.SINGLE | SWT.BORDER);
		final TreeColumn tc1 = new TreeColumn(result, SWT.NONE);
		tc1.setWidth(200);
		final TreeColumn tc2 = new TreeColumn(result, SWT.NONE);
		tc2.setWidth(200);
		return result;
	}

	// helping classes
	// ////////////////

	private static final class FTTreeContentProvider implements ITreeContentProvider {
		public Object[] getChildren(final Object element) {
			return ((WordNode) element).getChildren().toArray();
		}

		public Object getParent(final Object element) {
			return ((WordNode) element).getParent();
		}

		public boolean hasChildren(final Object element) {
			return ((WordNode) element).getChildren().size() > 0;
		}

		public Object[] getElements(final Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
			// unused
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// unused
		}
	}

	private class WordNodeWithIcon extends WordNode {
		private String icon;
		private String openIcon;

		public WordNodeWithIcon(final String word) {
			super(word);
		}

		public WordNodeWithIcon(final WordNodeWithIcon parent, final String word) {
			super(parent, word);
		}

		@SuppressWarnings("unused")
		public String getIcon() {
			return icon;
		}

		@SuppressWarnings("unused")
		public String getOpenIcon() {
			return openIcon;
		}

		public void setIcon(final String icon) {
			this.icon = icon;
		}

		public void setOpenIcon(final String openIcon) {
			this.openIcon = openIcon;
		}
	}

	private final class TestColumnFormatter extends ColumnFormatter {
		@Override
		public String getText(final Object element) {
			if (element == leaf || element == node) {
				final WordNode wordNode = (WordNode) element;
				return wordNode.isUpperCase() ? "yes" : "no";
			}
			return null;
		}

		@Override
		public Image getImage(final Object element) {
			final String word = ((WordNode) element).getWord();
			if ("Leaf".equalsIgnoreCase(word)) {
				return Activator.getSharedImage(SharedImages.IMG_ERROR_DECO);
			}
			return null;
		}

		@Override
		public Color getForeground(final Object element) {
			final String word = ((WordNode) element).getWord();
			if ("Node".equalsIgnoreCase(word)) {
				return colorA;
			}
			if ("Leaf".equalsIgnoreCase(word)) {
				return colorB;
			}
			return null;
		}

		@Override
		public Color getBackground(final Object element) {
			final String word = ((WordNode) element).getWord();
			if ("Node".equalsIgnoreCase(word)) {
				return colorA;
			}
			if ("Leaf".equalsIgnoreCase(word)) {
				return colorB;
			}
			return null;
		}

		@Override
		public Font getFont(final Object element) {
			final String word = ((WordNode) element).getWord();
			if ("Node".equalsIgnoreCase(word)) {
				return fontA;
			}
			if ("Leaf".equalsIgnoreCase(word)) {
				return fontB;
			}
			return null;
		}
	}

}
