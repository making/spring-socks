import React, {useEffect, useState} from "react";
import {Link} from "react-router-dom";

export function Cart() {
    const [cart, setCart] = useState({
        items: []
    });
    useEffect(() => {
        fetchCart().then(setCart);
    }, []);
    return <div className="p-grid p-dir-rev">
        <div className="p-col-10">
            <h2>Cart</h2>
            <table>
                <thead>
                <tr>
                    <th>Product</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Total</th>
                </tr>
                </thead>
                <tbody>
                {cart.items.map(item => <tr key={item.itemId}>
                    <td><img src={item.imageUrl} alt={item.name} width={'100px'}/> <Link
                        to={`/details/${item.itemId}`}>{item.name}</Link></td>
                    <td>${item.unitPrice}</td>
                    <td>{item.quantity}</td>
                    <td>${item.total}</td>
                </tr>)}
                </tbody>
            </table>
            <p>
                Total: ${cart.total}
            </p>
        </div>
        <div className="p-col-order-2">
        </div>
    </div>;
}

function fetchCart() {
    return fetch('/cart?latest', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}