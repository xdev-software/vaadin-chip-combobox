package software.xdev.vaadin.chips;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@Route("binder")
@PageTitle("Binder Demo")
public class BinderDemo extends VerticalLayout
{
	private final Binder<TestBean> binder = new Binder<>();
	
	public BinderDemo()
	{
		final ChipComboBox<String> ccb = new ChipComboBox<String>()
			.withLabel("Test");
		ccb.setItems("A", "B", "C");
		
		this.binder.forField(ccb)
			.asRequired("A value is required")
			.bind(TestBean::getStrings, TestBean::setStrings);
		
		this.add(ccb, new Button("Validate", ev -> this.binder.validate()));
	}
	
	@Override
	protected void onAttach(final AttachEvent attachEvent)
	{
		this.binder.readBean(new TestBean(new ArrayList<>()));
	}
	
	public static class TestBean
	{
		private Collection<String> strings;
		
		public TestBean(final Collection<String> strings)
		{
			super();
			this.strings = strings;
		}
		
		public Collection<String> getStrings()
		{
			return this.strings;
		}
		
		public void setStrings(final Collection<String> strings)
		{
			this.strings = strings;
		}
		
	}
	
}
