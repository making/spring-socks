import {Link, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import {Tags} from "../components/Tags";

export function Sock() {
    const {id} = useParams();
    const [sock, setSock] = useState({});
    const [relatedProducts, setRelatedProducts] = useState([]);

    useEffect(() => {
        fetchSock(id).then(body => {
            setSock(body.sock);
            setRelatedProducts(body.relatedProducts)
        });
    }, [id]);
    return <div>
        <h2>{sock.name}</h2>
        <img alt={sock.name} src={sock.imageUrl && sock.imageUrl[0]} width={'450px'}/>
        <p>${sock.price}</p>
        <p>{sock.description}</p>
        <h3>Related Products</h3>
        <ul>
            {relatedProducts.map(sock => <li key={sock.id}><Link
                to={`/details/${sock.id}`}>{sock.name}</Link></li>)}
        </ul>
        <Tags/>
    </div>;
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