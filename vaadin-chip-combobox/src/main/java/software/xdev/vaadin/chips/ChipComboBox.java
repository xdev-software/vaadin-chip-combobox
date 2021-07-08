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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
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
	protected ItemLabelGenerator<T> itemLabelGenerator = Object::toString;
	
	/*
	 * Fields
	 */
	protected final List<T> allAvailableItems = new ArrayList<>();
	protected final List<ChipComponent<T>> selectedComponents = new ArrayList<>();
	
	public ChipComboBox()
	{
		super(Collections.emptyList());
		
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
		this.selectedComponents
			.removeIf(comp -> !newPresentationValue.contains(comp.getItem()));
		
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
				chipComponent.setItemLabelGenerator(this.itemLabelGenerator);
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
		this.setModelValue(values, isFromClient);
	}
	
	protected void removeItem(final T item, final boolean isFromClient)
	{
		final List<T> values = new ArrayList<>(this.getValue());
		values.remove(item);
		this.setModelValue(values, isFromClient);
	}
	
	
	/**
	 * Updates/Rebuilds the UI form the fields
	 */
	protected void updateUI()
	{
		this.updateSelectedChips();
		this.updateAvailableItems();
	}
	
	protected void updateSelectedChips()
	{
		this.chipsContainer.removeAll();
		this.chipsContainer.add(this.selectedComponents.toArray(new ChipComponent[] {}));
	}
	
	protected void updateAvailableItems()
	{
		final List<T> availableItems = new ArrayList<>(this.allAvailableItems);
		availableItems.removeAll(this.getValue());
		this.cbAvailableItems.setItems(availableItems);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// setters + getters  //
	///////////////////////
	
	public Function<T, ChipComponent<T>> getChipsSupplier()
	{
		return this.chipsSupplier;
	}
	
	public ChipComboBox<T> withChipsSupplier(final Function<T, ChipComponent<T>> chipsSupplier)
	{
		this.chipsSupplier = chipsSupplier;
		return this;
	}
	
	public List<T> getAllAvailableItems()
	{
		return new ArrayList<>(this.allAvailableItems);
	}
		
	public ChipComboBox<T> withAllAvailableItems(final Collection<T> allAvailableItems)
	{
		this.setItems(allAvailableItems);
		
		return this;
	}
	
	@Override
	public void setItems(final Collection<T> items)
	{
		Objects.requireNonNull(items);
		this.allAvailableItems.clear();
		this.allAvailableItems.addAll(items);
		
		// Remove selected values that are not in allAvailableItems
		final Collection<T> values = new ArrayList<>(this.getValue());
		values.removeIf(v -> !this.allAvailableItems.contains(v));
		this.setModelValue(values, false);
		
		this.updateUI();
	}
	
	public String getLabel()
	{
		return this.cbAvailableItems.getLabel();
	}
	
	public ChipComboBox<T> withLabel(final String label)
	{
		Objects.requireNonNull(label);
		this.cbAvailableItems.setLabel(label);
		return this;
	}
	
	public String getPlaceholder()
	{
		return this.cbAvailableItems.getPlaceholder();
	}

	public ChipComboBox<T> withPlaceholder(final String placeholder)
	{
		Objects.requireNonNull(placeholder);
		this.cbAvailableItems.setPlaceholder(placeholder);
		return this;
	}
	
	public ChipComboBox<T> withFullComboBoxWidth()
	{
		return this.withFullComboBoxWidth(true);
	}
	
	public ChipComboBox<T> withFullComboBoxWidth(final boolean useFullWidth)
	{
		if(useFullWidth)
		{
			this.cbAvailableItems.setWidthFull();
		}
		else
		{
			this.cbAvailableItems.setWidth(null);
		}
		return this;
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

	/**
	 * Returns the {@link ComboBox} which contains the available items.<br/>
	 * NOTE: If the contents of the {@link ComboBox} are modified from the outside this component may break
	 * @return
	 */
	public ComboBox<T> getCbAvailableItems()
	{
		return this.cbAvailableItems;
	}

	/**
	 * Returns the {@link FlexLayout} with the select items (as {@link ChipComponent}s).<br/>
	 * NOTE: If the contents of the {@link FlexLayout} are modified from the outside this component may break
	 * @return
	 */
	public FlexLayout getChipsContainer()
	{
		return this.chipsContainer;
	}
}
