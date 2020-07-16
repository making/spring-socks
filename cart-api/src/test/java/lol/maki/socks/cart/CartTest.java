package lol.maki.socks.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartTest {

	@Test
	void addItemToNewCart() {
		final Cart cart = new Cart("1234");
		cart.mergeItem(new CartItem("1", 1, BigDecimal.TEN));
		final Optional<CartItem> item = cart.findItem("1");
		assertThat(item.isPresent()).isTrue();
		final CartItem cartItem = item.get();
		assertThat(cartItem.itemId()).isEqualTo("1");
		assertThat(cartItem.quantity()).isEqualTo(1);
		assertThat(cartItem.unitPrice()).isEqualTo(BigDecimal.TEN);
	}

	@Test
	void addItemToExistingCart() {
		final Cart cart = new Cart("1234", new ArrayList<>() {
			{
				add(new CartItem("0", 1, BigDecimal.ONE));
			}
		});
		cart.mergeItem(new CartItem("1", 1, BigDecimal.TEN));
		final Optional<CartItem> item = cart.findItem("1");
		assertThat(item.isPresent()).isTrue();
		final CartItem cartItem = item.get();
		assertThat(cartItem.itemId()).isEqualTo("1");
		assertThat(cartItem.quantity()).isEqualTo(1);
		assertThat(cartItem.unitPrice()).isEqualTo(BigDecimal.TEN);
	}

	@Test
	void increaseItem() {
		final Cart cart = new Cart("1234", new ArrayList<>() {
			{
				add(new CartItem("1", 1, BigDecimal.ONE));
			}
		});
		cart.mergeItem(new CartItem("1", 1, BigDecimal.TEN));
		final Optional<CartItem> item = cart.findItem("1");
		assertThat(item.isPresent()).isTrue();
		final CartItem cartItem = item.get();
		assertThat(cartItem.itemId()).isEqualTo("1");
		assertThat(cartItem.quantity()).isEqualTo(2);
		assertThat(cartItem.unitPrice()).isEqualTo(BigDecimal.TEN);
	}

	@Test
	void updateExistingItem() {
		final Cart cart = new Cart("1234", new ArrayList<>() {
			{
				add(new CartItem("1", 1, BigDecimal.ONE));
			}
		});
		cart.replaceItem(new CartItem("1", 1, BigDecimal.TEN));
		final Optional<CartItem> item = cart.findItem("1");
		assertThat(item.isPresent()).isTrue();
		final CartItem cartItem = item.get();
		assertThat(cartItem.itemId()).isEqualTo("1");
		assertThat(cartItem.quantity()).isEqualTo(1);
		assertThat(cartItem.unitPrice()).isEqualTo(BigDecimal.TEN);
	}

	@Test
	void updateNewItem() {
		final Cart cart = new Cart("1234", new ArrayList<>() {
			{
				add(new CartItem("0", 1, BigDecimal.ONE));
			}
		});
		cart.replaceItem(new CartItem("1", 1, BigDecimal.TEN));
		final Optional<CartItem> item = cart.findItem("1");
		assertThat(item.isPresent()).isTrue();
		final CartItem cartItem = item.get();
		assertThat(cartItem.itemId()).isEqualTo("1");
		assertThat(cartItem.quantity()).isEqualTo(1);
		assertThat(cartItem.unitPrice()).isEqualTo(BigDecimal.TEN);
	}

	@Test
	void removeItem() {
		final Cart cart = new Cart("1234", new ArrayList<>() {
			{
				add(new CartItem("1", 1, BigDecimal.ONE));
			}
		});
		cart.removeItem("1");
		final Optional<CartItem> item = cart.findItem("1");
		assertThat(item.isPresent()).isFalse();
	}

	@Test
	void mergeCart() {
		final CartItem cartItem1 = new CartItem("1", 1, BigDecimal.ONE);
		final CartItem cartItem2 = new CartItem("2", 1, BigDecimal.ONE);
		final CartItem cartItem3 = new CartItem("3", 1, BigDecimal.ONE);
		final CartItem cartItem4 = new CartItem("4", 1, BigDecimal.ONE);
		final Cart cart1 = new Cart("1234", new ArrayList<>() {
			{
				add(cartItem1);
				add(cartItem2);
			}
		});
		final Cart cart2 = new Cart("5678", new ArrayList<>() {
			{
				add(cartItem3);
				add(cartItem4);
			}
		});
		cart1.mergeCart(cart2);
		assertThat(cart1.customerId()).isEqualTo("1234");
		assertThat(cart1.items()).hasSize(4);
		assertThat(cart1.items()).containsExactly(cartItem1, cartItem2, cartItem3, cartItem4);
	}

	@Test
	void mergeCartHavingConflicts() {
		final CartItem cartItem1 = new CartItem("1", 1, BigDecimal.ONE);
		final CartItem cartItem2 = new CartItem("2", 1, BigDecimal.ONE);
		final CartItem cartItem3 = new CartItem("2", 2, BigDecimal.ONE);
		final CartItem cartItem4 = new CartItem("3", 1, BigDecimal.ONE);
		final Cart cart1 = new Cart("1234", new ArrayList<>() {
			{
				add(cartItem1);
				add(cartItem2);
			}
		});
		final Cart cart2 = new Cart("5678", new ArrayList<>() {
			{
				add(cartItem3);
				add(cartItem4);
			}
		});
		cart1.mergeCart(cart2);
		assertThat(cart1.customerId()).isEqualTo("1234");
		assertThat(cart1.items()).hasSize(3);
		assertThat(cart1.items()).containsExactly(cartItem1, new CartItem("2", 3, BigDecimal.ONE), cartItem4);
	}
}