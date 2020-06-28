package lol.maki.socks.payment.web;

import lol.maki.socks.config.PaymentProps;
import lol.maki.socks.payment.AuthorizationResult;
import lol.maki.socks.payment.ImmutablePayment;
import lol.maki.socks.payment.Payment;
import lol.maki.socks.payment.spec.Authorization;
import lol.maki.socks.payment.spec.AuthorizationRequest;
import lol.maki.socks.payment.spec.AuthorizationResponse;
import lol.maki.socks.payment.spec.PaymentAuthApi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@CrossOrigin
public class PaymentController implements PaymentAuthApi {
	private final PaymentProps props;

	public PaymentController(PaymentProps props) {
		this.props = props;
	}

	@Override
	public ResponseEntity<AuthorizationResponse> authorizePayment(AuthorizationRequest req) {
		final Payment payment = ImmutablePayment.builder()
				.amount(req.getAmount())
				.declineOverAmount(props.declineOverAmount())
				.build();
		final AuthorizationResult result = payment.authorize();
		if (!result.valid()) {
			return ResponseEntity.badRequest().body(toResponse(result));
		}
		if (!result.authorized()) {
			return ResponseEntity.status(UNAUTHORIZED).body(toResponse(result));
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