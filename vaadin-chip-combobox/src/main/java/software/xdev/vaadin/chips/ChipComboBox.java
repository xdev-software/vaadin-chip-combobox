/*
 * Copyright © 2021 XDEV Software (https://xdev.software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.xdev.vaadin.chips;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.internal.AbstractFieldSupport;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.HasItems;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.function.SerializableFunction;


/**
 * This component has a ComboBox with available items and displays the selected items as "chips" underneath it.<br/> It
 * behaves somewhat similar to a {@link Select}.
 */
public class ChipComboBox<T> extends AbstractCompositeField<VerticalLayout, ChipComboBox<T>, Collection<T>> implements
	HasItems<T>,
	HasValidation,
	HasStyle,
	HasSize
{
	
	/*
	 * UI-Components
	 */
	protected ComboBox<T> cbAvailableItems = new ComboBox<>();
	protected Button btnClearAll = new Button(VaadinIcon.TRASH.create());
	protected HorizontalLayout comboBoxContainer = new HorizontalLayout();
	protected FlexLayout chipsContainer = new FlexLayout();
	
	/*
	 * Suppliers / Configuration
	 */
	protected SerializableFunction<T, ChipComponent<T>> chipsSupplier = ChipComponent::new;
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
		
		this.btnClearAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
		
		this.comboBoxContainer.setAlignItems(FlexComponent.Alignment.BASELINE);
		this.comboBoxContainer.setWidthFull();
		this.comboBoxContainer.add(this.cbAvailableItems, this.btnClearAll);
		
		this.getContent().setPadding(false);
		this.getContent().setSpacing(false);
		this.setSizeUndefined();
		
		this.getContent().add(this.comboBoxContainer, this.chipsContainer);
		
		// Since version 2.2 the default
		// Long words (> 18 chars) are not displayed correctly with the hardcoded width of 12em
		this.setFullComboBoxWidth(true);
	}
	
	protected void initListeners()
	{
		this.cbAvailableItems.addValueChangeListener(this::onCbAvailableItemsValueChanged);
		this.btnClearAll.addClickListener(this::onClickClearAll);
	}
	
	protected void onCbAvailableItemsValueChanged(final ComponentValueChangeEvent<ComboBox<T>, T> event)
	{
		if(event.getValue() == null || this.isReadOnly())
		{
			return;
		}
		
		this.addItem(event.getValue(), event.isFromClient());
	}
	
	protected void onClickClearAll(final ClickEvent<Button> event)
	{
		if(this.isReadOnly())
		{
			return;
		}
		
		this.updateValues(this.getEmptyValue(), event.isFromClient());
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
	
	// region Chips Supplier
	
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
	 * @return self
	 * @see #setChipsSupplier(SerializableFunction)
	 */
	public ChipComboBox<T> withChipsSupplier(final SerializableFunction<T, ChipComponent<T>> chipsSupplier)
	{
		this.setChipsSupplier(chipsSupplier);
		return this;
	}
	
	/**
	 * Sets the supplier for creating new {@link ChipComponent ChipComponents}
	 *
	 * @param chipsSupplier supplier for creating new {@link ChipComponent ChipComponents}
	 */
	public void setChipsSupplier(final SerializableFunction<T, ChipComponent<T>> chipsSupplier)
	{
		this.chipsSupplier = Objects.requireNonNull(chipsSupplier);
	}
	
	// endregion
	// region All available items
	
	/**
	 * Get all available items, that can potentially get selected
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
	
	// endregion
	// region Label (of the ComboBox)
	
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
		this.cbAvailableItems.setLabel(label);
	}
	
	// endregion
	// region Placeholder (of the ComboBox)
	
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
		this.cbAvailableItems.setPlaceholder(placeholder);
	}
	
	// endregion
	// region ClearAll-Button
	
	/**
	 * @return "Clear All Button" visibility. With this button it is possible to clear all selected items with one
	 * click. The default value is {@code true}.
	 */
	public boolean isClearAllButtonVisible()
	{
		return this.btnClearAll.isVisible();
	}
	
	/**
	 * @return self
	 * @see #setClearAllButtonVisible(boolean)
	 */
	public ChipComboBox<T> withClearAllButtonVisible(final boolean clearAllButtonVisible)
	{
		this.setClearAllButtonVisible(clearAllButtonVisible);
		return this;
	}
	
	/**
	 * Sets the "Clear All Button" to visible or invisible. With this button it is possible to clear all selected items
	 * with one click. The default value is {@code true}.
	 *
	 * @param clearAllButtonVisible defines the visibility of the "Clear All Button".
	 */
	public void setClearAllButtonVisible(final boolean clearAllButtonVisible)
	{
		this.btnClearAll.setVisible(clearAllButtonVisible);
	}
	
	/**
	 * @return "Clear All Button" icon. With this button it is possible to clear all selected items with one click. The
	 * default value is {@link VaadinIcon#TRASH}.
	 */
	public Component getClearAllIcon()
	{
		return this.btnClearAll.getIcon();
	}
	
	/**
	 * @return self
	 * @see #setClearAllIcon(Component)
	 */
	public ChipComboBox<T> withClearAllIcon(final Component clearAllIcon)
	{
		this.setClearAllIcon(clearAllIcon);
		return this;
	}
	
	/**
	 * Sets the "Clear All Button" icon. With this button it is possible to clear all selected items with one click.
	 * The
	 * default value is {@link VaadinIcon#TRASH}.
	 *
	 * @param clearAllIcon the "Clear All Button" icon.
	 */
	public void setClearAllIcon(final Component clearAllIcon)
	{
		this.btnClearAll.setIcon(clearAllIcon);
	}
	
	// endregion
	// region FullComboBoxWidth
	
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
	
	// endregion
	// region Item Label generator
	
	/**
	 * Sets the item label generator used by the individual {@link ChipComponent}s.
	 */
	public void setChipItemLabelGenerator(final ItemLabelGenerator<T> generator)
	{
		this.chipItemLabelGenerator = Objects.requireNonNull(generator, "The item label generator can not be null");
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
	
	// endregion
	
	/*
	 * Other
	 */
	
	@Override
	public void setValue(final Collection<T> value)
	{
		// Cannot set a null value.
		// Using the clear-method to reset the component's value
		if(value == null)
		{
			this.clear();
			return;
		}
		
		super.setValue(value);
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
	 * {@inheritDoc}
	 */
	@Override
	public void setErrorMessage(final String errorMessage)
	{
		this.cbAvailableItems.setErrorMessage(errorMessage);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getErrorMessage()
	{
		return this.cbAvailableItems.getErrorMessage();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInvalid(final boolean invalid)
	{
		this.cbAvailableItems.setInvalid(invalid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInvalid()
	{
		return this.cbAvailableItems.isInvalid();
	}
	
	/*
	 * UI-Components
	 */
	
	/**
	 * Returns the {@link ComboBox} which contains the available items.
	 * <p/>
	 * NOTE: If the contents are modified from the outside this component may break
	 */
	public ComboBox<T> getCbAvailableItems()
	{
		return this.cbAvailableItems;
	}
	
	/**
	 * Returns the {@link HorizontalLayout} which contains {@link #cbAvailableItems}.
	 * <p/>
	 * NOTE: If the contents are modified from the outside this component may break
	 */
	public HorizontalLayout getComboBoxContainer()
	{
		return this.comboBoxContainer;
	}
	
	/**
	 * Returns the {@link FlexLayout} with the select items (as {@link ChipComponent}s).
	 * <p/>
	 * NOTE: If the contents are modified from the outside this component may break
	 */
	public FlexLayout getChipsContainer()
	{
		return this.chipsContainer;
	}
}
