package lol.maki.socks.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.math.BigDecimal.ZERO;

public class Order {
	private final String id;

	private final Customer customer;

	private final Address address;

	private final Card card;

	private final List<Item> items;

	private final Shipment shipment;

	private final OffsetDateTime date;

	private final OrderStatus status;

	public Order(String id, Customer customer, Address address, Card card, List<Item> items, Shipment shipment, OffsetDateTime date, OrderStatus status) {
		this.id = id;
		this.customer = customer;
		this.address = address;
		this.card = card;
		this.items = items;
		this.shipment = shipment;
		this.date = date;
		this.status = status;
	}

	public String id() {
		return id;
	}

	public Customer customer() {
		return customer;
	}

	public Address address() {
		return address;
	}

	public Card card() {
		return card;
	}

	public List<Item> items() {
		return items;
	}

	public Shipment shipment() {
		return shipment;
	}

	public OffsetDateTime date() {
		return date;
	}

	public OrderStatus status() {
		return status;
	}

	public Order addItem(Item item) {
		this.items.add(item);
		return this;
	}

	public Order withShipment(Shipment shipment) {
		return new Order(this.id, this.customer, this.address, this.card, this.items, shipment, this.date, this.status);
	}

	public static String newOrderId(Supplier<UUID> idGenerator) {
		return idGenerator.get().toString().substring(0, 8);
	}

	public final BigDecimal total() {
		return this.items()
				.stream()
				.map(Item::subTotal)
				.reduce(ZERO, BigDecimal::add);
	}

	public final int itemCount() {
		return this.items()
				.stream()
				.mapToInt(Item::quantity)
				.sum();
	}
}
