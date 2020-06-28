package lol.maki.socks.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "payment")
@ConstructorBinding
public class PaymentProps {
	private final BigDecimal declineOverAmount;

	public PaymentProps(BigDecimal declineOverAmount) {
		this.declineOverAmount = declineOverAmount;
	}

	public BigDecimal declineOverAmount() {
		return declineOverAmount;
	}
}
