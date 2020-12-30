import React, {useEffect, useState} from "react";
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
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
            <DataTable value={cart.items}>
                <Column body={item => <React.Fragment>
                    <img src={item.imageUrl} alt={item.name}
                         width={'100px'}/>&nbsp;
                    <Link
                        to={`/details/${item.itemId}`}>{item.name}</Link>
                </React.Fragment>}
                        header="Name"></Column>
                <Column body={item => `$${item.unitPrice}`} header="Unit Price"></Column>
                <Column field="quantity" header="Quantity"></Column>
                <Column body={item => `$${item.total}`} header="Total"></Column>
            </DataTable>
            <p>
                Total: ${cart.total}
            </p>
        </div>
        <div className=" p-col-order-2">
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