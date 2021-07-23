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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.internal.AbstractFieldSupport;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.HasItems;
import com.vaadin.flow.dom.Style;


/**
 * This component has a combobox with available items and displays the selected items as "chips" underneath it.<br/>
 * It behaves somewhat similar to a {@link Select}.
 * 
 * @author DL
 * @author AB
 */
public class ChipComboBox<T> extends AbstractCompositeField<VerticalLayout, ChipComboBox<T>, Collection<T>> implements
	HasItems<T>,
	HasStyle,
	HasSize
{
	
	/*
	 * UI-Components
	 */
	protected ComboBox<T> cbAvailableItems = new ComboBox<>();
	protected FlexLayout chipsContainer = new FlexLayout();
	
	/*
	 * Suppliers / Configuration
	 */
	protected Function<T, ChipComponent<T>> chipsSupplier = ChipComponent::new;
	protected ItemLabelGenerator<T> chipItemLabelGenerator = Object::toString;
	
	/*
	 * Fields
	 */
	protected final List<T> allAvailableItems = new ArrayList<>();
	protected final List<ChipComponent<T>> selectedComponents = new ArrayList<>();
	
	public ChipComboBox()
	{
		super(new ArrayList<>());
		
		this.initUI();
		this.initListeners();
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
	}
	
	protected void initListeners()
	{
		this.cbAvailableItems.addValueChangeListener(this::onCbAvailableItemsValueChanged);
	}
	
	protected void onCbAvailableItemsValueChanged(final ComponentValueChangeEvent<ComboBox<T>, T> event)
	{
		if(event.getValue() == null || this.isReadOnly())
		{
			return;
		}
		
		this.addItem(event.getValue(), event.isFromClient());
	}
	
	@Override
	protected void setPresentationValue(final Collection<T> newPresentationValue)
	{
		/*
		 * Update the component list
		 */
		
		// Remove components
		this.selectedComponents.removeIf(comp -> !newPresentationValue.contains(comp.getItem()));
		
		// Find new values and build components
		// @formatter:off
		final Collection<T> existingValues =
			this.selectedComponents.stream()
				.map(ChipComponent::getItem)
				.collect(Collectors.toList());
		
		newPresentationValue.stream()
			.filter(v -> !existingValues.contains(v))
			.map(item ->
			{
				final ChipComponent<T> chipComponent = this.chipsSupplier.apply(item);
				chipComponent.setItemLabelGenerator(this.chipItemLabelGenerator);
				chipComponent.addBtnDeleteClickListener(ev ->
				{
					if(this.isReadOnly())
					{
						return;
					}
					
					this.removeItem(item, ev.isFromClient());
				});
				return chipComponent;
			})
			.forEach(this.selectedComponents::add);
		// @formatter:on
		
		this.updateUI();
	}
	
	protected void addItem(final T item, final boolean isFromClient)
	{
		final List<T> values = new ArrayList<>(this.getValue());
		values.add(item);
		this.updateValues(values, isFromClient);
	}
	
	protected void removeItem(final T item, final boolean isFromClient)
	{
		final List<T> values = new ArrayList<>(this.getValue());
		values.remove(item);
		this.updateValues(values, isFromClient);
	}
	
	/**
	 * Updates the underlying values (if the newValues doesn't equals the oldValue)
	 * 
	 * @implNote
	 *           This is a "workaround" for
	 *           <a href="https://github.com/vaadin/flow/issues/11392">vaadin/flow#11392</a><br/>
	 *           The following behaviors may be unexpected:
	 *           <ul>
	 *           <li>The {@link ValueChangeEvent} is fired before the UI is updated</li>
	 *           <li>No internal data management like in {@link AbstractFieldSupport}</li>
	 *           </ul>
	 * @param newValues
	 * @param isFromClient
	 */
	protected void updateValues(final Collection<T> newValues, final boolean isFromClient)
	{
		final Collection<T> oldValue = this.getValue();
		this.setModelValue(newValues, isFromClient);
		
		if(!this.valueEquals(oldValue, newValues))
		{
			this.setPresentationValue(newValues);
		}
	}
	
	/**
	 * Updates/Rebuilds the UI from the fields
	 * 
	 * @implNote Will not fire a {@link ValueChangeEvent}
	 */
	protected void updateUI()
	{
		this.updateSelectedChips();
		this.updateAvailableItems();
	}
	
	protected void updateSelectedChips()
	{
		this.chipsContainer.removeAll();
		this.chipsContainer.add(this.selectedComponents.toArray(new ChipComponent[]{}));
	}
	
	protected void updateAvailableItems()
	{
		final List<T> availableItems = new ArrayList<>(this.allAvailableItems);
		availableItems.removeAll(this.getValue());
		this.cbAvailableItems.setItems(availableItems);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @apiNote
	 *          Currently selected/set values that no longer exist in the new items collection
	 *          will be removed.
	 */
	@Override
	public void setItems(final Collection<T> items)
	{
		Objects.requireNonNull(items);
		this.allAvailableItems.clear();
		this.allAvailableItems.addAll(items);
		
		// Remove selected values that are not in allAvailableItems
		final Collection<T> values = new ArrayList<>(this.getValue());
		values.removeIf(v -> !this.allAvailableItems.contains(v));
		this.updateValues(values, false);
		
		// Force UI update here to ensure everything (selected + available) is shown correctly
		this.updateUI();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// setters + getters //
	///////////////////////
	
	/*
	 * Chips Supplier
	 */
	
	/**
	 * Returns the current supplier for creating new {@link ChipComponent ChipComponents}
	 * 
	 * @return the current supplier for creating new {@link ChipComponent ChipComponents}
	 */
	public Function<T, ChipComponent<T>> getChipsSupplier()
	{
		return this.chipsSupplier;
	}
	
	/**
	 * @see ChipComboBox#setChipsSupplier(Supplier)
	 * 
	 * @param chipsSupplier
	 * @return the component itself
	 */
	public ChipComboBox<T> withChipsSupplier(final Function<T, ChipComponent<T>> chipsSupplier)
	{
		this.setChipsSupplier(chipsSupplier);
		return this;
	}
	
	/**
	 * Sets the supplier for creating new {@link ChipComponent ChipComponents}
	 * 
	 * @param chipsSupplier
	 *            supplier for creating new {@link ChipComponent ChipComponents}
	 */
	public void setChipsSupplier(final Function<T, ChipComponent<T>> chipsSupplier)
	{
		this.chipsSupplier = Objects.requireNonNull(chipsSupplier);
	}
	
	/*
	 * All available items
	 */
	
	/**
	 * Get all available items, that can potentially get selected
	 * 
	 * @return
	 */
	public List<T> getAllAvailableItems()
	{
		return new ArrayList<>(this.allAvailableItems);
	}
	
	public ChipComboBox<T> withAllAvailableItems(final Collection<T> allAvailableItems)
	{
		this.setItems(allAvailableItems);
		
		return this;
	}
	
	/*
	 * Label (of the ComboBox)
	 */
	
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
	
	/*
	 * Placeholder (of the ComboBox)
	 */
	
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
	
	/*
	 * FullComboBoxWidth
	 */
	
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
	
	/*
	 * Item Label generator
	 */
	
	/**
	 * Sets the item label generator used by the individual {@link ChipComponent}s.
	 * 
	 * @return
	 */
	public void setChipItemLabelGenerator(final ItemLabelGenerator<T> generator)
	{
		this.chipItemLabelGenerator = generator;
		this.selectedComponents.forEach(chipComp ->
		{
			chipComp.setItemLabelGenerator(this.chipItemLabelGenerator);
			chipComp.updateTextFromItemLabelGenerator();
		});
	}
	
	/**
	 * Sets the item label generator used by the individual {@link ChipComponent}s. Equal to setChipItemLabelGenerator,
	 * but allows in-line usage for easier component creation.
	 * 
	 * @return this
	 */
	public ChipComboBox<T> withChipItemLabelGenerator(final ItemLabelGenerator<T> generator)
	{
		this.setChipItemLabelGenerator(generator);
		return this;
	}
	
	/**
	 * Convenience method, which sets the item label generator used by *BOTH* {@link ComboBox} and the
	 * {@link ChipComponent}s.
	 * 
	 * @return
	 */
	public void setItemLabelGenerator(final ItemLabelGenerator<T> generator)
	{
		this.cbAvailableItems.setItemLabelGenerator(generator);
		this.setChipItemLabelGenerator(generator);
	}
	
	/**
	 * Convenience method, which sets the item label generator used by *BOTH* {@link ComboBox} and the
	 * {@link ChipComponent}s. Identical with setItemLabelGenerator, but allows in-line usage for easier component
	 * creation.
	 * 
	 * @return this
	 */
	public ChipComboBox<T> withItemLabelGenerator(final ItemLabelGenerator<T> generator)
	{
		this.setItemLabelGenerator(generator);
		return this;
	}
	
	/*
	 * Other
	 */
	
	@Override
	public void setValue(final Collection<T> value)
	{
		super.setValue(
			Objects.requireNonNull(
				value,
				"Cannot set a null value. Use the clear-method to reset the component's value."));
	}
	
	@Override
	public void setReadOnly(final boolean readOnly)
	{
		super.setReadOnly(readOnly);
		
		this.cbAvailableItems.setReadOnly(readOnly);
		this.selectedComponents.forEach(comp -> comp.setReadonly(readOnly));
	}
	
	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible)
	{
		super.setRequiredIndicatorVisible(requiredIndicatorVisible);
		
		this.cbAvailableItems.setRequiredIndicatorVisible(requiredIndicatorVisible);
	}
	
	/*
	 * UI-Components
	 */
	
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
