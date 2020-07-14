package lol.maki.socks.order;

public enum OrderStatus {
	/**
	 * Order was created.
	 */
	CREATED(1),

	/**
	 * Payment was processed successfully.
	 */
	PAID(5),

	/**
	 * Order was shipped.
	 */
	SHIPPED(10),

	/**
	 * Payment was not authorized.
	 */
	PAYMENT_FAILED(15),

	/**
	 * Order shipment failed.
	 */
	SHIPMENT_FAILED(20);

	private final int value;

	OrderStatus(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static OrderStatus fromValue(int value) {
		for (OrderStatus orderStatus : values()) {
			if (orderStatus.value == value) {
				return orderStatus;
			}
		}
		throw new IllegalArgumentException(String.format("'%d' is not allowed for OrderStatus.", value));
	}
}
