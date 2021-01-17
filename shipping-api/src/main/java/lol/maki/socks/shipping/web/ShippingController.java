package lol.maki.socks.shipping.web;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import lol.maki.socks.shipping.Carrier;
import lol.maki.socks.shipping.Shipment;
import lol.maki.socks.shipping.ShipmentMapper;
import lol.maki.socks.shipping.spec.ShipmentRequest;
import lol.maki.socks.shipping.spec.ShipmentResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.util.IdGenerator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@CrossOrigin
public class ShippingController {
	private final ShipmentMapper shipmentMapper;

	private final Clock clock;

	private final IdGenerator idGenerator;

	public ShippingController(ShipmentMapper shipmentMapper, Clock clock, IdGenerator idGenerator) {
		this.shipmentMapper = shipmentMapper;
		this.clock = clock;
		this.idGenerator = idGenerator;
	}

	@GetMapping(path = "/shipping/{orderId}")
	public ResponseEntity<ShipmentResponse> getShippingById(@PathVariable("orderId") String orderId) {
		return ResponseEntity.of(this.shipmentMapper.findByOrderId(orderId).map(this::toResponse));
	}

	@GetMapping(path = "/shipping")
	public ResponseEntity<List<ShipmentResponse>> getShippings() {
		return ResponseEntity.ok(this.shipmentMapper.findAll().stream().map(this::toResponse).collect(toUnmodifiableList()));
	}

	@PostMapping(path = "/shipping")
	public ResponseEntity<ShipmentResponse> postShipping(@Validated @RequestBody ShipmentRequest req, UriComponentsBuilder builder) {
		final Shipment shipment = new Shipment(
				Carrier.chooseByItemCount(req.getItemCount()),
				req.getOrderId(),
				LocalDate.now(this.clock),
				this.idGenerator.generateId());
		this.shipmentMapper.insert(shipment);
		return ResponseEntity.created(builder.replacePath("shipping/{orderId}").build(shipment.orderId()))
				.body(toResponse(shipment));
	}

	private ShipmentResponse toResponse(Shipment shipment) {
		return new ShipmentResponse()
				.orderId(shipment.orderId())
				.carrier(shipment.carrier().name())
				.deliveryDate(shipment.carrier().deliveryDate(shipment.shipmentDate()))
				.trackingNumber(shipment.trackingNumber());
	}
}
