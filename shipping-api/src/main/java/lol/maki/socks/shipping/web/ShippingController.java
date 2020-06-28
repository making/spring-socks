package lol.maki.socks.shipping.web;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import lol.maki.socks.shipping.Carrier;
import lol.maki.socks.shipping.ImmutableShipment;
import lol.maki.socks.shipping.Shipment;
import lol.maki.socks.shipping.ShipmentMapper;
import lol.maki.socks.shipping.spec.ShipmentRequest;
import lol.maki.socks.shipping.spec.ShipmentResponse;
import lol.maki.socks.shipping.spec.ShippingApi;

import org.springframework.http.ResponseEntity;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@CrossOrigin
public class ShippingController implements ShippingApi {
	private final ShipmentMapper shipmentMapper;

	private final Clock clock;

	private final IdGenerator idGenerator;

	public ShippingController(ShipmentMapper shipmentMapper, Clock clock, IdGenerator idGenerator) {
		this.shipmentMapper = shipmentMapper;
		this.clock = clock;
		this.idGenerator = idGenerator;
	}

	@Override
	public ResponseEntity<ShipmentResponse> getShippingById(String orderId) {
		return ResponseEntity.of(this.shipmentMapper.findByOrderId(orderId).map(this::toResponse));
	}

	@Override
	public ResponseEntity<List<ShipmentResponse>> getShippings() {
		return ResponseEntity.ok(this.shipmentMapper.findAll().stream().map(this::toResponse).collect(toUnmodifiableList()));
	}

	@Override
	public ResponseEntity<ShipmentResponse> postShipping(ShipmentRequest req) {
		final Shipment shipment = ImmutableShipment.builder()
				.orderId(req.getOrderId())
				.carrier(Carrier.chooseByItemCount(req.getItemCount()))
				.shipmentDate(LocalDate.now(this.clock))
				.trackingNumber(this.idGenerator.generateId())
				.build();
		this.shipmentMapper.insert(shipment);
		final ServletUriComponentsBuilder uriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest();
		return ResponseEntity.created(uriComponentsBuilder.replacePath("shipping/{orderId}").build(shipment.orderId()))
				.body(toResponse(shipment));
	}

	private ShipmentResponse toResponse(Shipment shipment) {
		return new ShipmentResponse()
				.orderId(shipment.orderId())
				.carrier(shipment.carrier().name())
				.deliveryDate(shipment.carrier().deliveryDate(shipment.shipmentDate()))
				.trackingNumber(shipment.trackingNumber().toString());
	}
}
