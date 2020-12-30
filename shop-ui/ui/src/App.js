import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Link, Route, Switch, useParams} from "react-router-dom";

export default function App() {
    return (
        <Router>
            <div>
                <h1>Spring Socks</h1>
                <nav>
                    <ul>
                        <li>
                            <Link to="/">Home</Link>
                        </li>
                        <li>
                            <Link to="/cart">Cart</Link>
                        </li>
                    </ul>
                </nav>
                <Switch>
                    <Route path="/cart">
                        <Cart/>
                    </Route>
                    <Route exact path="/details/:id">
                        <Sock/>
                    </Route>
                    <Route exact path="/tags/:tag">
                        <Tag/>
                    </Route>
                    <Route path="/">
                        <Home/>
                    </Route>
                </Switch>
            </div>
        </Router>
    );
}

function Home() {
    const [socks, setSocks] = useState([]);
    useEffect(() => {
        fetchSocks('featured', 1, 6).then(setSocks);
    }, []);
    return <div>
        <h2>Spring Socks</h2>
        <ul>
            {socks.map(sock => <li key={sock.id}><Link
                to={`/details/${sock.id}`}>{sock.name}</Link></li>)}
        </ul>
        <Tags/>
    </div>;
}

function Cart() {
    const [cart, setCart] = useState({
        items: []
    });
    useEffect(() => {
        fetchCart().then(setCart);
    }, []);
    return <div>
        <h2>Cart</h2>
        <table>
            <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Total</th>
            </tr>
            {cart.items.map(item => <tr key={item.itemId}>
                <td><img src={item.imageUrl} alt={item.name} width={'100px'}/> <Link
                    to={`/details/${item.itemId}`}>{item.name}</Link></td>
                <td>${item.unitPrice}</td>
                <td>{item.quantity}</td>
                <td>${item.total}</td>
            </tr>)}
        </table>
        <p>
            Total: ${cart.total}
        </p>
    </div>;
}

function Sock() {
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

function Tag() {
    const {tag} = useParams();
    const [socks, setSocks] = useState([]);
    useEffect(() => {
        fetchSocks(tag).then(setSocks);
    }, [tag]);
    return <div>
        <h2>Tag: {tag}</h2>
        <ul>
            {socks.map(sock => <li key={sock.id}><Link
                to={`/details/${sock.id}`}>{sock.name}</Link></li>)}
        </ul>
        <Tags/>
    </div>;
}

function Tags() {
    const [tags, setTags] = useState({tags: []});
    useEffect(() => {
        fetchTags().then(setTags);
    }, []);
    return <div>
        <h3>Tags</h3>
        <ul>
            {tags.tags.map(tag => <li key={tag}><Link
                to={`/tags/${tag}`}>{tag}</Link></li>)}
        </ul>
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

function fetchSocks(tag, page, size) {
    return fetch(`/tags/${tag}?page=${page || 1}&size=${size || 10}`, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}

function fetchTags() {
    return fetch('/tags', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
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