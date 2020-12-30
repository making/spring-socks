import React, {useEffect, useState} from "react";
import {BrowserRouter as Router, Link, Route, Switch} from "react-router-dom";
import {Cart} from "./routes/Cart";
import {Sock} from "./routes/Sock";
import {Tag} from "./routes/Tag";
import {Home} from "./routes/Home";
import {CartSummary} from "./components/CartSummary";
import {UserInfo} from "./components/UserInfo";

export default function App() {
    const [cart, setCart] = useState({
        items: []
    });
    const refreshCart = () => fetchCart().then(setCart);
    useEffect(refreshCart, []);
    return (
        <Router>
            <div>
                <h1><Link to="/">Spring Socks</Link></h1>
                <UserInfo/>
                <CartSummary cart={cart}/>
                <Switch>
                    <Route path="/cart">
                        <Cart/>
                    </Route>
                    <Route exact path="/details/:id">
                        <Sock refreshCart={refreshCart}/>
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

function fetchCart() {
    return fetch('/cart', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}

