package software.xdev.vaadin.chips;

/*-
 * #%L
 * ChipComboBox for Vaadin
 * %%
 * Copyright (C) 2021 XDEV Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.HasItems;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;


/**
 * This component has a combobox with available items and displays the selected items as "chips" underneath it.<br/>
 * It behaves somewhat similar to a {@link Select}.
 * 
 * @author DL
 * @author AB
 */
public class ChipComboBox<T> extends Composite<VerticalLayout> implements
	HasValue<ComponentValueChangeEvent<ChipComboBox<T>, Collection<T>>, Collection<T>>,
	HasStyle,
	HasSize,
	HasItems<T>
{
	
	/*
	 * UI-Components
	 */
	protected ComboBox<T> cbAvailableItems = new ComboBox<>();
	protected FlexLayout chipsContainer = new FlexLayout();
	
	/*
	 * Suppliers / Configuration
	 */
	protected Supplier<ChipComponent> chipsSupplier = ChipComponent::new;
	protected ItemLabelGenerator<T> itemLabelGenerator = Object::toString;
	
	/*
	 * Fields
	 */
	protected List<T> allAvailableItems = new ArrayList<>();
	
	protected Map<T, ChipComponent> selectedItems = new LinkedHashMap<>();
	
	public ChipComboBox()
	{
		this.initUI();
	}
	
	protected void initUI()
	{
		final Style chipsContainerStyle = this.chipsContainer.getStyle();
		chipsContainerStyle.set("flex-flow", "wrap");
		chipsContainerStyle.set("flex-direction", "row");
		
		this.getContent().setPadding(false);
		this.getContent().setSpacing(false);
		this.setSizeUndefined();
		
		this.getContent().add(this.cbAvailableItems, this.chipsContainer);
		
		this.cbAvailableItems.addValueChangeListener(this::onCbAvailableItemsValueChanged);
	}
	
	protected void onCbAvailableItemsValueChanged(final ComponentValueChangeEvent<ComboBox<T>, T> event)
	{
		if(event.getValue() == null)
		{
			return;
		}
		if(this.isReadOnly())
		{
			return;
		}
		
		final Collection<T> oldValues = new LinkedHashSet<>(this.selectedItems.keySet());
		
		this.addNewItem(event.getValue());
		
		this.fireValueChange(oldValues, event.isFromClient());
		
		this.updateUI();
	}
	
	protected void addNewItem(final T newItem)
	{
		final ChipComponent chipComponent = this.chipsSupplier.get();
		
		chipComponent.withLabelText(this.itemLabelGenerator.apply(newItem));
		
		chipComponent.addBtnDeleteClickListener(ev ->
		{
			if(this.isReadOnly())
			{
				return;
			}
			
			final Collection<T> oldValues = new LinkedHashSet<>(this.selectedItems.keySet());
			if(this.selectedItems.remove(newItem) != null)
			{
				// Only update when value was removed
				this.updateUI();
				
				this.fireValueChange(oldValues, ev.isFromClient());
			}
		});
		
		this.selectedItems.put(newItem, chipComponent);
	}
	
	/**
	 * Updates/Rebuilds the UI form the fields
	 * 
	 * @implNote Will not fire a {@link ValueChangeEvent}
	 */
	public void updateUI()
	{
		this.updateSelectedChips();
		this.updateAvailableItems();
	}
	
	protected void updateSelectedChips()
	{
		this.chipsContainer.removeAll();
		this.chipsContainer.add(this.selectedItems.values().toArray(new ChipComponent[]{}));
	}
	
	protected void updateAvailableItems()
	{
		final List<T> availableItems = new ArrayList<>(this.allAvailableItems);
		availableItems.removeAll(this.selectedItems.keySet());
		this.cbAvailableItems.setItems(availableItems);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// setters + getters //
	///////////////////////
	
	public Supplier<ChipComponent> getChipsSupplier()
	{
		return this.chipsSupplier;
	}
	
	public ChipComboBox<T> withChipsSupplier(final Supplier<ChipComponent> chipsSupplier)
	{
		this.chipsSupplier = chipsSupplier;
		return this;
	}
	
	public List<T> getAllAvailableItems()
	{
		return new ArrayList<>(this.allAvailableItems);
	}
	
	public ChipComboBox<T> withAllAvailableItems(final List<T> allAvailableItems)
	{
		return this.withAllAvailableItems(allAvailableItems, true);
	}
	
	public ChipComboBox<T> withAllAvailableItems(final List<T> allAvailableItems, final boolean updateUI)
	{
		Objects.requireNonNull(allAvailableItems);
		this.allAvailableItems.clear();
		this.allAvailableItems.addAll(allAvailableItems);
		
		final Collection<T> oldValues = new LinkedHashSet<>(this.selectedItems.keySet());
		if(this.selectedItems.keySet().removeIf(item -> !this.allAvailableItems.contains(item)))
		{
			this.fireValueChange(oldValues, false);
		}
		
		if(updateUI)
		{
			this.updateUI();
		}
		
		return this;
	}
	
	@Override
	public void setItems(final Collection<T> items)
	{
		this.withAllAvailableItems(new ArrayList<>(items), true);
	}
	
	public String getLabel()
	{
		return this.cbAvailableItems.getLabel();
	}
	
	public ChipComboBox<T> withLabel(final String label)
	{
		this.setLabel(label);
		return this;
	}
	
	public void setLabel(final String label)
	{
		Objects.requireNonNull(label);
		this.cbAvailableItems.setLabel(label);
	}
	
	public String getPlaceholder()
	{
		return this.cbAvailableItems.getPlaceholder();
	}
	
	public ChipComboBox<T> withPlaceholder(final String placeholder)
	{
		this.setPlaceholder(placeholder);
		return this;
	}
	
	public void setPlaceholder(final String placeholder)
	{
		Objects.requireNonNull(placeholder);
		this.cbAvailableItems.setPlaceholder(placeholder);
	}
	
	public ChipComboBox<T> withFullComboBoxWidth()
	{
		return this.withFullComboBoxWidth(true);
	}
	
	public ChipComboBox<T> withFullComboBoxWidth(final boolean useFullWidth)
	{
		this.setFullComboBoxWidth(useFullWidth);
		return this;
	}
	
	public void setFullComboBoxWidth(final boolean useFullWidth)
	{
		if(useFullWidth)
		{
			this.cbAvailableItems.setWidthFull();
		}
		else
		{
			this.cbAvailableItems.setWidth(null);
		}
	}
	
	@Override
	public void setValue(final Collection<T> value)
	{
		final Collection<T> oldValues = new LinkedHashSet<>(this.selectedItems.keySet());
		
		this.selectedItems.clear();
		value.forEach(this::addNewItem);
		
		this.fireValueChange(oldValues, false);
		
		this.updateUI();
	}
	
	@Override
	public Collection<T> getValue()
	{
		return new LinkedHashSet<>(this.selectedItems.keySet());
	}
	
	protected void fireValueChange(final Collection<T> oldValue, final boolean fromClient)
	{
		ComponentUtil.fireEvent(this, new ComponentValueChangeEvent<>(this, this, oldValue, fromClient));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Registration addValueChangeListener(
		final ValueChangeListener<? super ComponentValueChangeEvent<ChipComboBox<T>, Collection<T>>> listener)
	{
		@SuppressWarnings("rawtypes")
		final ComponentEventListener componentListener = event ->
		{
			final ComponentValueChangeEvent<ChipComboBox<T>, Collection<T>> valueChangeEvent =
				(ComponentValueChangeEvent<ChipComboBox<T>, Collection<T>>)event;
			listener.valueChanged(valueChangeEvent);
		};
		return ComponentUtil.addListener(
			this,
			ComponentValueChangeEvent.class,
			componentListener);
	}
	
	@Override
	public void setReadOnly(final boolean readOnly)
	{
		this.cbAvailableItems.setReadOnly(readOnly);
		this.selectedItems.values().forEach(comp -> comp.setReadonly(readOnly));
	}
	
	@Override
	public boolean isReadOnly()
	{
		return this.cbAvailableItems.isReadOnly();
	}
	
	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible)
	{
		this.cbAvailableItems.setRequiredIndicatorVisible(requiredIndicatorVisible);
	}
	
	@Override
	public boolean isRequiredIndicatorVisible()
	{
		return this.cbAvailableItems.isRequiredIndicatorVisible();
	}
	
	/**
	 * Returns the {@link ComboBox} which contains the available items.<br/>
	 * NOTE: If the contents of the {@link ComboBox} are modified from the outside this component may break
	 * 
	 * @return
	 */
	public ComboBox<T> getCbAvailableItems()
	{
		return this.cbAvailableItems;
	}
	
	/**
	 * Returns the {@link FlexLayout} with the select items (as {@link ChipComponent}s).<br/>
	 * NOTE: If the contents of the {@link FlexLayout} are modified from the outside this component may break
	 * 
	 * @return
	 */
	public FlexLayout getChipsContainer()
	{
		return this.chipsContainer;
	}
	
}
