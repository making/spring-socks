import React, {useEffect, useState} from "react";
import {Link} from "react-router-dom";

export function CartSummary() {
    const [cart, setCart] = useState({
        items: []
    });
    useEffect(() => {
        fetchCart().then(setCart);
    }, []);
    return <p><Link to="/cart">Cart</Link> {cart.itemSize} items : ${cart.total}</p>;
}

function fetchCart() {
    return fetch('/cart', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}