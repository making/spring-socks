package lol.maki.socks.catalog;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CatalogOrderConverter implements Converter<String, CatalogOrder> {
	@Override
	public CatalogOrder convert(String source) {
		return CatalogOrder.valueOf(source.toUpperCase());
	}
}
