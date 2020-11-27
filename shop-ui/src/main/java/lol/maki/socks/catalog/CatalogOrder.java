package lol.maki.socks.catalog;

public enum CatalogOrder {
	PRICE, NAME;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
