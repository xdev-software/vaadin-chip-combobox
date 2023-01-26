package software.xdev.vaadin.chips;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;


/**
 * Shows how to set the ChipComboBox items through a DataProvider. In this case the data is never changed, but the
 * filter of the DataProvider is.
 */
public class DataProviderDemoComponent extends VerticalLayout
{
	private static final Random RANDOM = new Random();
	private final ListDataProvider<Integer> dataProvider =
		DataProvider.ofCollection(IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList()));
	private final ChipComboBox<Integer> intBox = new ChipComboBox<Integer>()
		.withPlaceholder("Select Integer chips");
	
	private final Button btnSetAvailableInts1to10 = new Button("Set available ints 1-10");
	private final Button btnSetAvailableInts5to15 = new Button("Set available ints 5-15");
	private final Button btnSetAvailableInts11to20 = new Button("Set available ints 11-20");
	private final Button btnSetRandomAvailableInts = new Button("Set available ints random");
	private final Button btnShowSelectedInt = new Button("What Integers are selected?");
	
	private final TextArea taValueChangeInt =
		new TextArea("ValueChangeEvent", "Change something in the chip combobox to see the result");
	
	public DataProviderDemoComponent()
	{
		this.initUI();
		
		this.btnSetAvailableInts1to10.addClickListener(ev -> this.setAvailableInts(1, 10));
		this.btnSetAvailableInts5to15.addClickListener(ev -> this.setAvailableInts(5, 15));
		this.btnSetAvailableInts11to20.addClickListener(ev -> this.setAvailableInts(11, 20));
		this.btnSetRandomAvailableInts.addClickListener(ev -> this.setAvailableIntsRandom());
		
		this.btnShowSelectedInt.addClickListener(ev ->
			Notification.show(
				"Selected: " + this.intBox.getValue().stream().map(Object::toString).collect(Collectors.joining(", ")))
		);
		
		this.intBox.addValueChangeListener(ev ->
				// @formatter:off
			this.taValueChangeInt.setValue(
					"Value: [" + ev.getValue().stream().map(Object::toString).collect(Collectors.joining(", ")) + "] \r\n" +
					"OldValue: [" + ev.getOldValue().stream().map(Object::toString).collect(Collectors.joining(", ")) + "] \r\n" +
					"IsFromClient: " + ev.isFromClient()
			)
			// @formatter:on
		);
		this.intBox.setItems(this.dataProvider);
	}
	
	private void initUI()
	{
		this.taValueChangeInt.setReadOnly(true);
		this.add(
			this.intBox,
			this.btnSetAvailableInts1to10,
			this.btnSetAvailableInts5to15,
			this.btnSetAvailableInts11to20,
			this.btnSetRandomAvailableInts,
			this.btnShowSelectedInt,
			this.taValueChangeInt);
	}
	
	private void setAvailableIntsRandom()
	{
		final int startValue = RANDOM.nextInt(100) + 1;
		final int count = RANDOM.nextInt(5) + 5;
		
		this.setAvailableInts(startValue, startValue + count);
	}
	
	private void setAvailableInts(final int startInclusive, final int endInclusive)
	{
		this.dataProvider.setFilter(intToCheck -> intToCheck >= startInclusive && intToCheck <= endInclusive);
	}
}
