package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

public abstract class EnumFoxFactory<EnumType extends Enum<EnumType>, Product> extends FoxFactory<EnumType, Product> {

	protected final Class<EnumType> type;

	protected EnumFoxFactory(Class<EnumType> type, Class<Product> product,
			Supplier<Collection<Class<? extends Product>>> source) {
		super(product, source);
		this.type = type;
	}

}
