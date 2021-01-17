package lol.maki.socks.payment.web;

import lol.maki.socks.config.PaymentProps;
import lol.maki.socks.payment.AuthorizationResult;
import lol.maki.socks.payment.Payment;
import lol.maki.socks.payment.spec.Authorization;
import lol.maki.socks.payment.spec.AuthorizationRequest;
import lol.maki.socks.payment.spec.AuthorizationResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@CrossOrigin
public class PaymentController {
	private final PaymentProps props;

	public PaymentController(PaymentProps props) {
		this.props = props;
	}

	@PostMapping(path = "/paymentAuth")
	public ResponseEntity<AuthorizationResponse> authorizePayment(@Validated @RequestBody AuthorizationRequest req) {
		final Payment payment = new Payment(
				req.getAmount(),
				props.declineOverAmount());
		final AuthorizationResult result = payment.authorize();
		if (!result.valid()) {
			return ResponseEntity.badRequest().body(toResponse(result));
		}
		if (!result.authorized()) {
			return ResponseEntity.status(CONFLICT).body(toResponse(result));
		}
		return ResponseEntity.ok(toResponse(result));
	}

	AuthorizationResponse toResponse(AuthorizationResult result) {
		return new AuthorizationResponse()
				.authorization(new Authorization()
						.authorised(result.authorized())
						.message(result.message()));
	}
}
