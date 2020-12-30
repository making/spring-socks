import React from "react";
import {Link} from "react-router-dom";

export function CartSummary({cart}) {
    return <div className="p-d-flex p-jc-end">
        <p>
            <Link to="/cart"><span className="p-overlay-badge p-mr-5"><i
                className="pi pi-shopping-cart" style={{fontSize: '2em'}}></i><span
                className="p-badge">{cart.itemSize}</span></span></Link> : ${cart.total}
        </p>
    </div>;
}