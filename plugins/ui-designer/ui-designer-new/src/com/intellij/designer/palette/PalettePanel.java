/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.designer.palette;

import com.intellij.designer.designSurface.DesignerEditorPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.ui.ScrollPaneFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Lobas
 */
public class PalettePanel extends JPanel {
  private final JPanel myPaletteContainer = new PaletteContainer();
  private List<PaletteGroupComponent> myGroupComponents = Collections.emptyList();
  private List<PaletteItemsComponent> myItemsComponents = Collections.emptyList();
  private List<PaletteGroup> myGroups = Collections.emptyList();
  private DesignerEditorPanel myDesigner;
  private final ListSelectionListener mySelectionListener = new ListSelectionListener() {
    @Override
    public void valueChanged(ListSelectionEvent event) {
      notifySelection(event);
    }
  };

  public PalettePanel() {
    super(new GridLayout(1, 1));

    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myPaletteContainer);
    scrollPane.setBorder(null);
    add(scrollPane);

    new AnAction() {
      @Override
      public void actionPerformed(AnActionEvent e) {
        clearActiveItem();
      }
    }.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)), scrollPane);
  }

  @Nullable
  public PaletteItem getActiveItem() {
    for (PaletteGroupComponent groupComponent : myGroupComponents) {
      if (groupComponent.isSelected()) {
        PaletteItem paletteItem = (PaletteItem)groupComponent.getItemsComponent().getSelectedValue();
        if (paletteItem != null) {
          return paletteItem;
        }
      }
    }
    return null;
  }

  public void clearActiveItem() {
    if (getActiveItem() != null) {
      for (PaletteItemsComponent itemsComponent : myItemsComponents) {
        itemsComponent.clearSelection();
      }
      notifySelection(null);
    }
  }

  public boolean isEmpty() {
    return myGroups.isEmpty();
  }

  public void loadPalette(@Nullable DesignerEditorPanel designer) {
    for (PaletteItemsComponent itemsComponent : myItemsComponents) {
      itemsComponent.removeListSelectionListener(mySelectionListener);
    }

    myDesigner = designer;
    myPaletteContainer.removeAll();

    if (designer == null) {
      myGroups = Collections.emptyList();
      myGroupComponents = Collections.emptyList();
      myItemsComponents = Collections.emptyList();
    }
    else {
      myGroups = designer.getPaletteGroups();
      myGroupComponents = new ArrayList<PaletteGroupComponent>();
      myItemsComponents = new ArrayList<PaletteItemsComponent>();
    }

    for (PaletteGroup group : myGroups) {
      PaletteGroupComponent groupComponent = new PaletteGroupComponent(group);
      PaletteItemsComponent itemsComponent = new PaletteItemsComponent(group);

      groupComponent.setItemsComponent(itemsComponent);
      myPaletteContainer.add(groupComponent);
      myPaletteContainer.add(itemsComponent);

      myGroupComponents.add(groupComponent);

      itemsComponent.addListSelectionListener(mySelectionListener);
      myItemsComponents.add(itemsComponent);
    }

    myPaletteContainer.revalidate();
  }

  private void notifySelection(@Nullable ListSelectionEvent event) {
    if (event != null) {
      PaletteItemsComponent sourceItemsComponent = (PaletteItemsComponent)event.getSource();
      for (int i = event.getFirstIndex(); i <= event.getLastIndex(); i++) {
        if (sourceItemsComponent.isSelectedIndex(i)) {
          for (PaletteItemsComponent itemsComponent : myItemsComponents) {
            if (itemsComponent != sourceItemsComponent) {
              itemsComponent.clearSelection();
            }
          }
          break;
        }
      }
    }
    if (myDesigner != null) {
      myDesigner.activatePaletteItem(getActiveItem());
    }
  }
}