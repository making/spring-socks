import {Link, Redirect, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import {Panel} from 'primereact/panel';
import {Button} from 'primereact/button';
import {InputNumber} from 'primereact/inputnumber';
import {Tags} from "../components/Tags";

export function Sock({refreshCart}) {
    const {id} = useParams();
    const [sock, setSock] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [relatedProducts, setRelatedProducts] = useState([]);
    const [quantity, setQuantity] = useState(1);
    const [redirectToCart, setRedirectToCart] = useState(false);
    useEffect(() => {
        fetchSock(id).then(body => {
            setSock(body.sock);
            setRelatedProducts(body.relatedProducts)
        });
    }, [id]);
    const onChangeQuantity = e => setQuantity(e.target.value);
    const onClickAddCartItem = e => {
        e.preventDefault();
        setIsSubmitting(true);
        addCartItem(id, quantity)
            .then(refreshCart)
            .then(() => setRedirectToCart(true))
            .finally(() => setIsSubmitting(false));
    };
    return <div className="p-grid p-dir-rev">
        {redirectToCart && <Redirect to={{pathname: "/cart"}}/>}
        <div className="p-col-10">
            <Panel header={sock.name}>
                <img alt={sock.name} src={sock.imageUrl && sock.imageUrl[0]}
                     width={'450px'}/>
                <p>${sock.price}</p>
                <p>{sock.description}</p>
                <form>
                    <InputNumber value={quantity} onValueChange={onChangeQuantity}
                                 mode="decimal" showButtons min={0} max={50}
                                 disabled={isSubmitting}/>
                    &nbsp;
                    <Button onClick={onClickAddCartItem}
                            disabled={isSubmitting}>
                        <i className="pi pi-shopping-cart"></i>&nbsp;Add To Cart</Button>
                </form>
            </Panel>
            <h3>Related Products</h3>
            <ul>
                {relatedProducts.map(sock => <li key={sock.id}><Link
                    to={`/details/${sock.id}`}>{sock.name}</Link></li>)}
            </ul>
        </div>
        <div className="p-col-2">
            <Tags/>
        </div>
    </div>;
}

function addCartItem(id, quantity) {
    return fetch(`/cart`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({
            id: id,
            quantity: quantity
        })
    })
        .then(res => res.json());
}

function fetchSock(id) {
    return fetch(`/details/${id}`, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}