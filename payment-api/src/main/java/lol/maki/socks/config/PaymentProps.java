package lol.maki.socks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "payment")
@ConstructorBinding
public class PaymentProps {
	private final float declineOverAmount;

	public PaymentProps(float declineOverAmount) {
		this.declineOverAmount = declineOverAmount;
	}

	public float declineOverAmount() {
		return declineOverAmount;
	}
}
