import React from "react";
import {Link} from "react-router-dom";

export function CartSummary({cart}) {
    return <p><Link to="/cart">Cart</Link> {cart.itemSize} items : ${cart.total}</p>;
}